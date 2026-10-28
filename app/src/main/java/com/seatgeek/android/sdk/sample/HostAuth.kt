package com.seatgeek.android.sdk.sample

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

/**
 * Stands in for your app's own AppAuth login against your identity provider. Nothing here is
 * SeatGeek-specific: it is the code you already have, unchanged by adding the SDK.
 *
 * Its redirect scheme is the appAuthRedirectScheme placeholder in app/build.gradle.kts. The SeatGeek
 * SDK runs its own AppAuth flow with the same AppAuth classes; see the RedirectUriReceiverActivity
 * declaration in AndroidManifest.xml for how both redirects are caught.
 */
internal class HostAuth(context: Context) {

    private val authService = AuthorizationService(context)

    var authState: AuthState? = null
        private set

    /** Builds the intent that starts the login; launch it for a result and pass that to [completeSignIn]. */
    fun createSignInIntent(callback: (Intent?, AuthorizationException?) -> Unit) {
        AuthorizationServiceConfiguration.fetchFromIssuer(ISSUER.toUri()) { configuration, exception ->
            if (configuration == null) {
                callback(null, exception)
                return@fetchFromIssuer
            }
            val request = AuthorizationRequest.Builder(
                configuration,
                CLIENT_ID,
                ResponseTypeValues.CODE,
                REDIRECT_URI.toUri(),
            )
                .setScope(SCOPE)
                .build()
            callback(authService.getAuthorizationRequestIntent(request), null)
        }
    }

    /** Exchanges the authorization code from the login result for tokens. */
    fun completeSignIn(result: Intent, callback: (AuthState?, AuthorizationException?) -> Unit) {
        val response = AuthorizationResponse.fromIntent(result)
        val exception = AuthorizationException.fromIntent(result)
        if (response == null) {
            callback(null, exception)
            return
        }
        val state = AuthState(response, exception)
        authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, tokenException ->
            state.update(tokenResponse, tokenException)
            authState = state.takeIf { tokenResponse != null }
            callback(authState, tokenException)
        }
    }

    fun dispose() {
        authService.dispose()
    }

    private companion object {
        const val ISSUER = "https://your-idp.example.com"
        const val CLIENT_ID = "yourHostClientId"
        // Its scheme is the appAuthRedirectScheme placeholder in app/build.gradle.kts.
        const val REDIRECT_URI = "com.seatgeek.android.sdk.sample:/oauth2redirect"
        const val SCOPE = "openid email profile"
    }
}
