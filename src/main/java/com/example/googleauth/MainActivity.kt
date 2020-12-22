package com.example.googleauth

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserMatcher
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {
    var TAG: String = "AppCompatActivity"

    lateinit var mAuthorizationService: AuthorizationService;
    var mAuthItentLatch: CountDownLatch = CountDownLatch(1)
    var mBrowserMatcher: BrowserMatcher = AnyBrowserMatcher.INSTANCE

    lateinit var mAuthorizationServiceConfiguration: AuthorizationServiceConfiguration
    private var executor: ExecutorService? = Executors.newSingleThreadExecutor()

    var request: AuthorizationRequest? = null
    var response:AuthorizationResponse? = null

    private val authIntent = AtomicReference<CustomTabsIntent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        Log.i(TAG, "Inside AuthResultHandlerActivity")
        if (resp != null) {
            // authorization succeeded
            Log.i(TAG, "Auth succeeded")
            response = resp
            getToken()
            return
        } else {
            // authorization failed, check ex for more details
            Log.i(TAG, "Auth failed")
        }

        val discoveryServiceUri: Uri = Uri.parse("https://apac.cloudgroundcontrol.com/auth/realms/cgcs-dev/")
        AuthorizationServiceConfiguration
                .fetchFromIssuer(discoveryServiceUri
                ) { authorizationServiceConfiguration: AuthorizationServiceConfiguration?, authorizationException: AuthorizationException? ->
                    if (authorizationException != null) {
                        Log.w(TAG, "Failed to retrieve configuration")
                    } else {
                        Log.i(TAG, "Retrieved configuration successfully")
                        if (authorizationServiceConfiguration != null) {
                            mAuthorizationServiceConfiguration = authorizationServiceConfiguration
                        }
                    }
//                    mAuthItentLatch.countDown()
                }

        val btnLogin = findViewById(R.id.btnLogin) as Button
        btnLogin.setOnClickListener {
            executor?.submit {
                this.doAuth()
//                warmUpBrowser()
            }
        }

        val btnWarmUp = findViewById(R.id.btnWarmUpBrowser) as Button
        btnWarmUp.setOnClickListener {
            this.warmUpBrowser()
        }

//        mAuthItentLatch.await()

//        Log.i(TAG, "Start authorization service")
//        val authorizationService = AuthorizationService(applicationContext)
//        val clientId = "cloudgroundcontrol-stg"
//        val redirectUri = Uri.parse("https://apac.cloudgroundcontrol.com/android")
//        val req = AuthorizationRequest.Builder(mAuthorizationServiceConfiguration, clientId, AuthorizationRequest.CODE_CHALLENGE_METHOD_PLAIN, redirectUri)
//                .build()
//
//        val intent = Intent(applicationContext, AuthResultHandlerActivity::class.java)
//
//        val arrIntent = arrayOf(intent)
//        authorizationService.performAuthorizationRequest(req, PendingIntent.getActivities(applicationContext, 0, arrIntent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    fun doAuth() {
        Log.i(TAG, "Start authorization service")
        recreateAuthorizationService()
        val clientId = "cloudgroundcontrol-stg"
//        val redirectUri = Uri.parse("https://apac.cloudgroundcontrol.com")
        val redirectUri = Uri.parse("")// Uri.parse("cgc://main_activity")
//        val req =
//                AuthorizationRequest.Builder(mAuthorizationServiceConfiguration, clientId,
//                        "code", redirectUri)
//                .build()
//        request = req

        val completionIntent = Intent(this, AuthResultHandlerActivity::class.java)
        completionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val cancelIntent = Intent(this, AuthResultHandlerActivity::class.java)
        cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        cancelIntent.putExtra("EXTRA_FAILED", true)
        completionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntentComplete = PendingIntent.getActivity(this, 0, completionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        val pendingIntentCancel = PendingIntent.getActivity(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val authorizationRequestIntent =
            mAuthorizationService.getAuthorizationRequestIntent(request!!)
        startActivityForResult(authorizationRequestIntent, 0)

//        mAuthorizationService.performAuthorizationRequest(request!!,
//                pendingIntentComplete,
//                pendingIntentCancel,
//                authIntent.get()
//        )
    }

    fun getToken() {
        Log.i(TAG, "Getting token")
        mAuthorizationService.performTokenRequest(response!!.createTokenExchangeRequest()) {
            res: TokenResponse?, exp: AuthorizationException? ->
            if (res != null) {
                Log.i(TAG, "Successfully getting token access_token: " + res.accessToken)
            } else {
                Log.w(TAG, "Failed to get token")
            }
        }
    }

    fun recreateAuthorizationService() {
//        mAuthorizationService?.dispose()

        mAuthorizationService = createAuthorizationService()
    }

    fun createAuthorizationService(): AuthorizationService {
        Log.i("AppCompatActivity", "Creating authorization service")

        val builder: AppAuthConfiguration.Builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(mBrowserMatcher)
        builder.setConnectionBuilder(getConnectionBuilder())

        return AuthorizationService(this, builder.build())
    }

    //
    fun getConnectionBuilder(): ConnectionBuilder {
        return DefaultConnectionBuilder.INSTANCE
    }

    private fun warmUpBrowser() {
//        authIntentLatch = CountDownLatch(1)
        recreateAuthorizationService()
        val redirectUri = Uri.parse("cgc://main_activity")
        val clientId = "cloudgroundcontrol-stg"
        val req =
            AuthorizationRequest.Builder(mAuthorizationServiceConfiguration, clientId,
                ResponseTypeValues.CODE, redirectUri)
                .setScope("openid")
                .build()
        request = req

        executor?.execute {
            Log.i(
                TAG,
                "Warming up browser instance for auth request"
            )
//            val uri = Uri.parse("https://apac.cloudgroundcontrol.com/auth/realms/cgcs-dev/protocol/openid-connect/auth?response_type=code&client_id=cloudgroundcontrol-stg&scope=openid&redirect_uri=https://apac.cloudgroundcontrol.com")
            val uri = request!!.toUri()
            val intentBuilder: CustomTabsIntent.Builder =
                mAuthorizationService?.createCustomTabsIntentBuilder(uri)
            intentBuilder.setToolbarColor(
                ResourcesCompat.getColor(
                    resources,
                    net.openid.appauth.R.color.browser_actions_divider_color,
                    null
                )
            )
            authIntent.set(intentBuilder.build())
//            authIntentLatch.countDown()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val resp = AuthorizationResponse.fromIntent(data!!)
        val ex = AuthorizationException.fromIntent(data)
        Log.i(TAG, "Inside AuthResultHandlerActivity")
        if (resp != null) {
            // authorization succeeded
            Log.i(TAG, "Auth succeeded")

            val tokenRequest = resp.createTokenExchangeRequest()
            mAuthorizationService.performTokenRequest(tokenRequest) {
                tokenRes: TokenResponse?, ex: AuthorizationException? ->
                if (tokenRes != null) {
                    Log.d("accessToken", tokenRes.accessToken!!)
                }
            }
        } else {
            // authorization failed, check ex for more details
            Log.i(TAG, "Auth failed")
        }
    }
}