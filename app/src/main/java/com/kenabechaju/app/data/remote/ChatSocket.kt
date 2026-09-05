package com.kenabechaju.app.data.remote

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.kenabechaju.app.BuildConfig
import com.kenabechaju.app.data.remote.dto.MessageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

sealed interface WsEvent {
    data class NewMessage(val conversationId: String, val message: MessageDto) : WsEvent
    data class Read(val conversationId: String, val messageIds: List<String>) : WsEvent
    data class Typing(val conversationId: String, val isTyping: Boolean) : WsEvent
}

/**
 * Single global `wss://.../ws` connection (not per-conversation — routing
 * happens by `conversation_id` inside each frame, matching the backend's
 * own design), authenticated the same way every other request is: the
 * client's `AuthInterceptor` attaches the access-token Bearer header to
 * this upgrade request too, since it's a normal application interceptor
 * OkHttp runs on every request through this client, WebSocket upgrades
 * included — no separate auth path to hand-roll.
 *
 * Foreground-only, matching the web app's own tab-visibility assumption:
 * connects on `ProcessLifecycleOwner` `onStart` (app foregrounded), closes
 * on `onStop` (backgrounded) rather than trying to stay alive as a
 * long-running background service.
 */
@Singleton
class ChatSocket @Inject constructor(private val okHttpClient: OkHttpClient) : DefaultLifecycleObserver {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var shouldStayConnected = false
    private var backoffMs = 1000L

    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events.asSharedFlow()

    fun start() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        shouldStayConnected = true
        openSocket()
    }

    override fun onStop(owner: LifecycleOwner) {
        shouldStayConnected = false
        reconnectJob?.cancel()
        webSocket?.close(1000, "backgrounded")
        webSocket = null
    }

    private fun openSocket() {
        if (webSocket != null) return
        val wsUrl = BuildConfig.API_BASE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://") + "ws"
        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    private fun scheduleReconnect() {
        webSocket = null
        if (!shouldStayConnected) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(backoffMs)
            backoffMs = (backoffMs * 2).coerceAtMost(30_000)
            if (shouldStayConnected) openSocket()
        }
    }

    fun sendTyping(conversationId: String, isTyping: Boolean) {
        webSocket?.send("""{"type":"typing","conversation_id":"$conversationId","is_typing":$isTyping}""")
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            backoffMs = 1000L
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            runCatching { parseFrame(text) }.getOrNull()?.let { scope.launch { _events.emit(it) } }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            scheduleReconnect()
        }
    }

    private fun parseFrame(text: String): WsEvent? {
        val obj = json.parseToJsonElement(text) as? JsonObject ?: return null
        return when (obj["type"]?.jsonPrimitive?.contentOrNull) {
            "message" -> {
                val conversationId = obj["conversation_id"]?.jsonPrimitive?.contentOrNull ?: return null
                val messageJson = obj["message"] ?: return null
                val message = json.decodeFromJsonElement(MessageDto.serializer(), messageJson)
                WsEvent.NewMessage(conversationId, message)
            }
            "read" -> {
                val conversationId = obj["conversation_id"]?.jsonPrimitive?.contentOrNull ?: return null
                val ids = (obj["message_ids"] as? kotlinx.serialization.json.JsonArray)
                    ?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                WsEvent.Read(conversationId, ids)
            }
            "typing" -> {
                val conversationId = obj["conversation_id"]?.jsonPrimitive?.contentOrNull ?: return null
                val isTyping = obj["is_typing"]?.jsonPrimitive?.booleanOrNull ?: true
                WsEvent.Typing(conversationId, isTyping)
            }
            else -> null
        }
    }
}
