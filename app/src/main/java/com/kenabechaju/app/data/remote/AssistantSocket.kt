package com.kenabechaju.app.data.remote

import com.kenabechaju.app.BuildConfig
import com.kenabechaju.app.data.remote.dto.AssistantChatRequestDto
import com.kenabechaju.app.data.remote.dto.AssistantTurnDto
import com.kenabechaju.app.data.remote.dto.ListingDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

// OkHttp's SocketTimeoutException.message is the bare word "timeout" —
// technically accurate but meaningless to a user reading it in a chat
// bubble. Everything else keeps its own IOException.message, which is
// already reasonably descriptive (e.g. "Unable to resolve host").
private fun IOException.toUserMessage(fallback: String): String =
    if (this is SocketTimeoutException) "The assistant is taking longer than expected. Please try again." else message ?: fallback

sealed interface AssistantEvent {
    data class Delta(val text: String) : AssistantEvent
    data class Listings(val listings: List<ListingDto>) : AssistantEvent
    data class Error(val message: String) : AssistantEvent
    data object Done : AssistantEvent
}

/**
 * `POST /assistant/chat` returns `text/event-stream`, not a typical GET
 * `EventSource` — so this is a small hand-rolled reader over the raw
 * streaming response body rather than a third-party SSE client, since the
 * protocol here is tiny and precisely known (four event types, one
 * single-line JSON `data:` per event, exactly matching the backend's own
 * `yield f"event: {type}\ndata: {json.dumps(event)}\n\n"` framing).
 */
@Singleton
class AssistantSocket @Inject constructor(private val okHttpClient: OkHttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    fun streamChat(message: String, history: List<AssistantTurnDto>, locale: String): Flow<AssistantEvent> = callbackFlow {
        val bodyJson = json.encodeToString(AssistantChatRequestDto.serializer(), AssistantChatRequestDto(message, history, locale))
        val request = Request.Builder()
            .url(BuildConfig.API_BASE_URL + "assistant/chat")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()
        val call = okHttpClient.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                trySend(AssistantEvent.Error(e.toUserMessage("Network error")))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val source = it.body?.source()
                    if (source == null) {
                        trySend(AssistantEvent.Error("Empty response"))
                        close()
                        return
                    }
                    var eventType: String? = null
                    try {
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            when {
                                line.startsWith("event:") -> eventType = line.removePrefix("event:").trim()
                                line.startsWith("data:") -> {
                                    val data = line.removePrefix("data:").trim()
                                    parseEvent(eventType, data)?.let { event ->
                                        trySend(event)
                                        if (event is AssistantEvent.Done) return
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        trySend(AssistantEvent.Error(e.toUserMessage("Stream error")))
                    } finally {
                        close()
                    }
                }
            }
        })

        awaitClose { call.cancel() }
    }

    private fun parseEvent(type: String?, data: String): AssistantEvent? {
        if (type == "done") return AssistantEvent.Done
        val obj = runCatching { json.parseToJsonElement(data) as? JsonObject }.getOrNull() ?: return null
        return when (type) {
            "delta" -> obj["text"]?.jsonPrimitive?.contentOrNull?.let(AssistantEvent::Delta)
            "listings" -> {
                val listings = obj["listings"]?.jsonArray?.mapNotNull {
                    runCatching { json.decodeFromJsonElement(ListingDto.serializer(), it) }.getOrNull()
                } ?: emptyList()
                AssistantEvent.Listings(listings)
            }
            "error" -> AssistantEvent.Error(obj["message"]?.jsonPrimitive?.contentOrNull ?: "Assistant error")
            else -> null
        }
    }
}
