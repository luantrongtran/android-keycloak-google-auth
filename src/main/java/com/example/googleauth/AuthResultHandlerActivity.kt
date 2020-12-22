package com.example.googleauth

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse


class AuthResultHandlerActivity : AppCompatActivity() {
    val TAG: String = "AppCompatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_result_handler)
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
}