package com.example.googleauth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class SuccessAuthHandlerActivity : AppCompatActivity() {
    private val TAG = "SuccessAuthHandlerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_auth_handler)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        Log.i(TAG, "Inside AuthResultHandlerActivity")
        if (resp != null) {
            // authorization succeeded
            Log.i(TAG, "Auth succeeded")

        } else {
            // authorization failed, check ex for more details
            Log.i(TAG, "Auth failed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        Log.i(TAG, "Inside AuthResultHandlerActivity")
        if (resp != null) {
            // authorization succeeded
            Log.i(TAG, "Auth succeeded")
        } else {
            // authorization failed, check ex for more details
            Log.i(TAG, "Auth failed")
        }
    }
}