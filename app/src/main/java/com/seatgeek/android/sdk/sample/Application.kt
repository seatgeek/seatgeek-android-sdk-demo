package com.seatgeek.android.sdk.sample

import android.app.Activity
import android.app.Application
import android.net.Uri
import com.seatgeek.android.sdk.SeatGeek
import com.seatgeek.android.sdk.SeatGeekBuilder
import com.seatgeek.android.sdk.auth.OAuthClient
import com.seatgeek.android.sdk.ticket.TicketClient
import com.seatgeek.domain.common.constraint.ProtectedString
import com.seatgeek.domain.common.model.auth.AccessToken

class SampleApplication : Application() {

    private lateinit var seatgeek: SeatGeek

    override fun onCreate() {
        super.onCreate()

        val instanceName = ""
        val redirectUri = Uri.parse("sg-<instanceName>://oauth")
        val clientId = ""
        val additionalScopes = emptyArray<String>()

        seatgeek = SeatGeekBuilder(this)
            .setClientId(clientId)
            .setInstanceName(instanceName)
            .setRedirectUri(redirectUri)
            .additionalScopes(additionalScopes)
            .disableCrmIdCreation() // If you would like to disable automatic CRM ID creation for users signing in to your app
            .build()
    }

    internal fun logOut() {
        seatgeek.getOAuthClient().logOut()
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
}