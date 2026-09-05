package com.kenabechaju.app.ui.auth

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.kenabechaju.app.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

/**
 * The *Web* OAuth client ID (`GOOGLE_CLIENT_ID` on the backend — the same
 * one web's own "Sign in with Google" flow already uses) passed as
 * Credential Manager's serverClientId, so the ID token it returns is
 * audienced to this — exactly what `POST /auth/google` verifies against.
 * Not a secret: Google's own docs describe client IDs as public
 * identifiers, safe to embed in an app (unlike a client *secret*, which
 * this is not and never has one for the native flow). A *separate*
 * Android-type OAuth client (this app's package name + its signing
 * certificate's SHA-1, registered in the same Google Cloud project) is
 * also required, but only so Google can attest this specific app's
 * identity — that client ID is configured entirely on the Google Cloud
 * Console side and never appears in code or in any token.
 */
private const val WEB_CLIENT_ID = "777828703576-e69rirfb25a584etfl0hbukj6us1hh9g.apps.googleusercontent.com"

sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data class Failure(val message: String) : GoogleSignInResult

    /** The account picker was dismissed, or there's no Google account on the device to offer — not a real error, nothing to show. */
    data object Cancelled : GoogleSignInResult
}

/**
 * Launches the system "choose a Google account" sheet via Credential
 * Manager and returns the resulting ID token — the backend does all real
 * verification (signature + audience + expiry), this just gets the token
 * off the device. `filterByAuthorizedAccounts = false` so a device with no
 * account previously used with this app still shows the full picker
 * instead of silently failing.
 */
suspend fun signInWithGoogle(context: Context): GoogleSignInResult {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

    return try {
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
        GoogleSignInResult.Success(credential.idToken)
    } catch (e: GetCredentialCancellationException) {
        GoogleSignInResult.Cancelled
    } catch (e: NoCredentialException) {
        GoogleSignInResult.Failure("No Google account found on this device.")
    } catch (e: GetCredentialException) {
        GoogleSignInResult.Failure(e.message ?: "Google sign-in isn't available right now.")
    } catch (e: GoogleIdTokenParsingException) {
        GoogleSignInResult.Failure("Couldn't read the Google sign-in response.")
    }
}

/** Same "outlined button, real multi-color G, no tint" treatment web's own Google button uses — shared by Login and Signup. */
@Composable
fun GoogleSignInButton(
    onResult: (GoogleSignInResult) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = { scope.launch { onResult(signInWithGoogle(context)) } },
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().height(50.dp),
    ) {
        // tint = Unspecified so the icon's own 4 brand colors render as-is,
        // instead of Icon's default behavior of flattening it to one color.
        Icon(
            painter = painterResource(R.drawable.ic_google_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google", color = MaterialTheme.colorScheme.onSurface)
    }
}
