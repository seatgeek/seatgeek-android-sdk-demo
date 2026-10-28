package com.seatgeek.android.sdk.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var hostAuth: HostAuth

    // Your app's own AppAuth login; the SeatGeek SDK's sign-in runs separately inside SeatGeekActivity.
    private val hostSignIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) {
            showHostAuthStatus(getString(R.string.host_sign_in_canceled))
            return@registerForActivityResult
        }
        hostAuth.completeSignIn(data) { authState, exception ->
            showHostAuthStatus(
                if (authState != null) {
                    getString(R.string.host_signed_in)
                } else {
                    getString(R.string.host_sign_in_failed, exception?.errorDescription ?: exception?.error)
                },
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivityContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hostAuth = HostAuth(this)

        findViewById<Button>(R.id.runSdkBtn).setOnClickListener {
            startActivity(Intent(this, SeatGeekActivity::class.java))
        }

        findViewById<Button>(R.id.hostSignInBtn).setOnClickListener {
            hostAuth.createSignInIntent { intent, exception ->
                if (intent != null) {
                    hostSignIn.launch(intent)
                } else {
                    showHostAuthStatus(getString(R.string.host_sign_in_failed, exception?.errorDescription ?: exception?.error))
                }
            }
        }
    }

    private fun showHostAuthStatus(status: String) {
        findViewById<TextView>(R.id.hostAuthStatusTxt).text = status
    }

    override fun onDestroy() {
        super.onDestroy()
        hostAuth.dispose()
    }
}
