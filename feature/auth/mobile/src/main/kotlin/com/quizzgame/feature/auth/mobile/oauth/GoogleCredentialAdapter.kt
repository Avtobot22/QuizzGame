package com.quizzgame.feature.auth.mobile.oauth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.quizzgame.feature.auth.mobile.R

internal object GoogleCredentialAdapter {
    fun available(context: Context): Boolean = context.getString(R.string.auth_google_client_id).isNotEmpty() &&
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    suspend fun request(activity: Activity, nonce: String): ProviderCredential {
        val option = GetSignInWithGoogleOption.Builder(activity.getString(R.string.auth_google_client_id))
            .setNonce(nonce)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        return try {
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                ProviderCredential.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                ProviderCredential.Failure
            }
        } catch (_: GetCredentialCancellationException) {
            ProviderCredential.Cancelled
        } catch (_: GetCredentialException) {
            ProviderCredential.Failure
        } catch (_: GoogleIdTokenParsingException) {
            ProviderCredential.Failure
        }
    }
}

internal sealed interface ProviderCredential {
    data object Cancelled : ProviderCredential
    data object Failure : ProviderCredential
    class Token(val value: String) : ProviderCredential {
        override fun toString(): String = "ProviderCredential.Token(<redacted>)"
    }
}
