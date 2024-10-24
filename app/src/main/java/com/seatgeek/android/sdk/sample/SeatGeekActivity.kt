package com.seatgeek.android.sdk.sample

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.seatgeek.android.sdk.auth.OAuthClient
import com.seatgeek.android.sdk.ticket.TicketClient
import com.seatgeek.domain.common.model.auth.AccessToken
import com.seatgeek.domain.common.model.event.UpcomingEventResponse
import com.seatgeek.domain.common.model.user.AuthUser
import com.seatgeek.domain.common.model.user.SgUser

class SeatGeekActivity : AppCompatActivity() {

    private val authStatusChangeCallback = object : OAuthClient.AuthStatusChangeCallback {
        override fun onAuthStatusChange(newUser: AuthUser?) {
            updateMenuVisibility(newUser != null)
        }

        override fun onSgUserStatusChange(newUser: SgUser?) {
            updateMenuVisibility(newUser != null)
        }
    }

    private val upcomingEventChangeListener = object : TicketClient.UpcomingEventChangeListener {
        override fun onUpcomingEventChange(upcomingEventResponse: UpcomingEventResponse) {
            //handle the upcoming event
        }

        override fun onError(throwable: Throwable) {
            //handle the error
        }
    }

    private val sampleApp: SampleApplication by lazy { application as SampleApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.seatgeek_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.seatgeekActivityContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupOrientation()
        setupMenu()
        registerListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OAuthClient.REQUEST_OAUTH && resultCode == OAuthClient.RESULT_OK) {
            val accessToken =
                data?.extras?.getParcelable<AccessToken>(OAuthClient.EXTRA_AUTH_ACCESS_TOKEN)
            val user = data?.extras?.getParcelable<SgUser>(OAuthClient.EXTRA_SG_USER)
            val crmId = user?.sgUserAccounts?.accounts?.mapNotNull { it?.crmId }?.firstOrNull()
        }
    }

    private fun setupOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun setupMenu() {
        findViewById<MaterialToolbar>(R.id.toolbar).setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    updateMenuVisibility(false)
                    sampleApp.logOut()
                    true
                }

                R.id.fetchUpcomingEvent -> {
                    sampleApp.fetchUpcomingEvent()
                    true
                }

                R.id.setToken -> {
                    sampleApp.beginOAuthWithExistingToken(
                        this,
                        "your previous token",
                    )
                    true
                }

                else -> false
            }
        }
    }

    private fun registerListeners() {
        sampleApp.registerUpcomingEventChangeListener(upcomingEventChangeListener)
        sampleApp.registerAuthStatusChangeCallback(authStatusChangeCallback)
    }

    private fun updateMenuVisibility(visible: Boolean) {
        findViewById<MaterialToolbar>(R.id.toolbar).menu.setGroupVisible(R.id.menuGroup, visible)
        invalidateOptionsMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        sampleApp.unregisterAuthStatusChangeCallback(authStatusChangeCallback)
        sampleApp.unregisterUpcomingEventChangeListeners(upcomingEventChangeListener)
    }
}