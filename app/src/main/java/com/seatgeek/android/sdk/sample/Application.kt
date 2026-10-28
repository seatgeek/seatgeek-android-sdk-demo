package com.seatgeek.android.sdk.sample

import android.app.Activity
import android.app.Application
import com.seatgeek.android.sdk.SeatGeek
import com.seatgeek.android.sdk.SeatGeekBuilder
import com.seatgeek.android.sdk.actions.SdkUserActionCallbacks
import com.seatgeek.android.sdk.analytics.callback.SdkAnalyticsThirdPartyClient
import com.seatgeek.android.sdk.auth.OAuthClient
import com.seatgeek.android.sdk.ticket.TicketClient
import com.seatgeek.domain.common.constraint.ProtectedString
import com.seatgeek.domain.common.model.auth.AccessToken
import com.seatgeek.domain.common.model.user.SgUser
import androidx.core.net.toUri

class SampleApplication : Application() {

    private lateinit var seatgeek: SeatGeek

    override fun onCreate() {
        super.onCreate()

        val instanceName = "yourAppInstanceName"
        // Your app's AppAuth login owns the appAuthRedirectScheme placeholder, so this scheme gets its
        // own intent-filter on RedirectUriReceiverActivity in AndroidManifest.xml.
        val redirectUri = "yourAppScheme://oauth".toUri()
        val clientId = "yourClientId"
        // "openid" switches the SDK to OIDC sign-in (authorization code + PKCE). The SDK adds the
        // "email" and "profile" scopes on its own.
        val additionalScopes = arrayOf("openid")

        seatgeek = SeatGeekBuilder(this)
            .setClientId(clientId)
            .setInstanceName(instanceName)
            .setRedirectUri(redirectUri)
            .additionalScopes(additionalScopes)
//            .disableCrmIdCreation() // If you would like to disable automatic CRM ID creation for users signing in to your app
            .build()
    }

    internal fun logOut() {
        // In OIDC mode, logoutWebSession = true also ends the shared browser session
        // (RP-initiated end-session) so the next sign-in is a fresh login.
        // Pass false (the default) to clear only the local session.
        seatgeek.getOAuthClient().logOut(logoutWebSession = false)
    }

    // Alternative way to receive user's access token
    internal fun getSeatGeekToken(): AccessToken? {
        return seatgeek.getOAuthClient().getSeatGeekAccessToken()
    }

    internal fun isAuthenticated(): Boolean {
        return seatgeek.getOAuthClient().isAuthenticated()
    }

    // If you would like to use an AccessToken previously retrieved from SeatGeek.
    // @param activity is used just for context
    fun beginOAuthWithExistingToken(activity: Activity, yourPreviousTokenString: String) {
        seatgeek.getOAuthClient()
            .beginOAuthWithToken(activity, AccessToken(ProtectedString(yourPreviousTokenString)))
    }

    internal fun registerAuthStatusChangeCallback(callback: OAuthClient.AuthStatusChangeCallback) {
        seatgeek.getOAuthClient().registerAuthStatusChangeCallback(callback)
    }

    internal fun unregisterAuthStatusChangeCallback(callback: OAuthClient.AuthStatusChangeCallback) {
        seatgeek.getOAuthClient().unregisterAuthStatusChangeCallback(callback)
    }

    internal fun fetchUpcomingEvent() {
        seatgeek.getTicketClient().fetchUpcomingEvent()
    }

    internal fun registerUpcomingEventChangeListener(listener: TicketClient.UpcomingEventChangeListener) {
        seatgeek.getTicketClient().registerUpcomingEventChangeListener(listener)
    }

    internal fun unregisterUpcomingEventChangeListeners(listener: TicketClient.UpcomingEventChangeListener) {
        seatgeek.getTicketClient().unregisterUpcomingEventChangeListeners()
    }

    internal fun getSgUser(): SgUser? {
        return seatgeek.getOAuthClient().getSgUser()
    }

    internal fun registerAnalyticsCallback(callback: SdkAnalyticsThirdPartyClient.Callback) {
        seatgeek.getAnalyticsClient().registerAnalyticsCallback(callback)
    }

    internal fun unregisterAnalyticsCallback(callback: SdkAnalyticsThirdPartyClient.Callback) {
        seatgeek.getAnalyticsClient().unregisterAnalyticsCallback(callback)
    }

    internal fun registerUserActionCallbacks(callbacks: SdkUserActionCallbacks) {
        seatgeek.getUserActionsClient().registerUserActionCallbacks(callbacks)
    }

    internal fun unregisterUserActionCallbacks(callbacks: SdkUserActionCallbacks) {
        seatgeek.getUserActionsClient().unregisterUserActionCallbacks(callbacks)
    }
}