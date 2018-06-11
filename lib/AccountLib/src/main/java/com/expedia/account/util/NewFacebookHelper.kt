package com.expedia.account.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import com.expedia.account.AccountService
import com.expedia.account.Config
import com.expedia.account.NewAccountView
import com.expedia.account.R
import com.expedia.account.data.AccountResponse
import com.expedia.account.data.Db
import com.expedia.account.data.FacebookLinkResponse
import com.expedia.account.view.FacebookLinkAccountsLayout
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.mobiata.android.Log
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.json.JSONObject

open class NewFacebookHelper(val context: Context, val config: Config, val brand: String, val newAccountView: NewAccountView) {

    private val facebookCallbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }

    init {
        FacebookSdk.sdkInitialize(context.applicationContext)
        val facebookCallback = object : FacebookCallback<LoginResult> {
            override fun onCancel() {
                onFacebookLoginCancelled()
            }
            override fun onError(exception: FacebookException) {
                onFacebookLoginError(context)
            }
            override fun onSuccess(loginResult: LoginResult) {
                onFacebookLoginSuccess(context, loginResult)
            }
        }
        LoginManager.getInstance().registerCallback(facebookCallbackManager, facebookCallback)
    }

    /**
     * Login with Facebook.
     *
     * This kicks off the Facebook login using their own Activity.
     * It uses the Facebook app if it is installed, otherwise it'll use a webview.
     */
    open fun doFacebookLogin(context: Context) {
        Log.d("FACEBOOK: doFacebookLogin")
        val loginManager = LoginManager.getInstance()
        val permissions = listOf("email", "public_profile")
        loginManager.logInWithReadPermissions(context as Activity, permissions)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun onLinkClicked(context: Context) {
        facebookLinkExistingAccount(context)
    }

    protected fun onFacebookLoginCancelled() {
        Log.d("FACEBOOK: LoginResult: onCancel!")
        Handler().postDelayed({ AccountService.facebookLogOut() }, 1000)
    }

    protected fun onFacebookLoginError(context: Context) {
        Log.d("FACEBOOK: LoginResult: onError!")
        showErrorFacebookUnknown(context)
    }

    protected fun onFacebookLoginSuccess(context: Context, loginResult: LoginResult) {
        // A successful login from Facebook means now we're ready to try
        // connecting the Facebook account to the Expedia account.
        newAccountView.showLoading()
        Log.d("FACEBOOK: LoginResult: onSuccess!")
        val token = loginResult.accessToken

        if (token.declinedPermissions.contains("email")) {
            showErrorFacebookDeclinedEmailAddress(context)
            return
        }

        val user = Db.getNewUser()
        user.isFacebookUser = true
        user.facebookUserId = token.userId
        user.facebookToken = token.token
        fetchFacebookUserInfo(context, token)
    }

    /**
     * Ok so we have a user's facebook session, but we need the users information for
     * that to be useful. So let's get it.
     */
    open fun fetchFacebookUserInfo(context: Context, token: AccessToken) {
        Log.d("FACEBOOK: fetchFacebookUserInfo")

        val request = GraphRequest.newMeRequest(token, GraphRequest.GraphJSONObjectCallback { jsonObject, _ ->
            if (jsonObject == null) {
                Log.d("FACEBOOK: nullJsonObject")
                showErrorFacebookUnknown(context)
                return@GraphJSONObjectCallback
            }
            onFacebookUserInfoFetched(context, jsonObject)
        })
        val parameters = Bundle()
        parameters.putString("fields", "email,first_name,last_name")
        request.parameters = parameters
        request.executeAsync()
    }

    protected fun onFacebookUserInfoFetched(context: Context, jsonObject: JSONObject?) {
        Log.d("FACEBOOK: meRequest: " + jsonObject!!.toString())

        val user = Db.getNewUser()
        user.email = jsonObject.optString("email")
        user.firstName = jsonObject.optString("first_name")
        user.lastName = jsonObject.optString("last_name")

        if (TextUtils.isEmpty(user.email)) {
            // This happens if user created their FB account with phone number only
            showErrorFacebookMissingEmailAddress(context)
        } else {
            facebookAutoLogin(context)
        }
    }

    /**
     * This attempts to hand our Facebook info to Expedia and tries to auto login based on that info.
     * This will only succeed if the user has at some point granted Expedia access to fbconnect.
     */
    private fun facebookAutoLogin(context: Context) {
        val user = Db.getNewUser()
        if (!user.isFacebookUser) {
            throw RuntimeException("Not a Facebook user")
        }
        config.service.facebookAutoLogin(user.facebookUserId, user.facebookToken)
                .subscribe(object : Observer<FacebookLinkResponse> {
                    override fun onComplete() {
                    }

                    override fun onError(e: Throwable) {
                        Log.d("FACEBOOK: unable to facebookAutoLogin: $e")
                        showErrorFacebookUnknown(context)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(facebookLinkResponse: FacebookLinkResponse?) {
                        if (facebookLinkResponse?.status == null) {
                            Log.d("FACEBOOK: facebookAutoLogin response arrived null")
                            showErrorFacebookUnknown(context)
                            return
                        }
                        Log.d("FACEBOOK: facebookAutoLogin response: " + facebookLinkResponse.status.name)
                        when (facebookLinkResponse.status) {
                            FacebookLinkResponse.FacebookLinkResponseCode.notLinked -> showNewOrExistingAccountDialog(context)
                            FacebookLinkResponse.FacebookLinkResponseCode.success -> facebookSignInRefreshProfile(context)
                            FacebookLinkResponse.FacebookLinkResponseCode.existing -> {
                                newAccountView.cancelLoading()
                                newAccountView.showFacebookLinkAccountsView(FacebookLinkAccountsLayout.SetupType.ACCOUNT_EXISTING)
                            }
                            FacebookLinkResponse.FacebookLinkResponseCode.error -> showErrorFacebookUnknown(context)
                            FacebookLinkResponse.FacebookLinkResponseCode.loginFailed -> showErrorFacebookUnknown(context)
                            else -> return
                        }
                    }
                })
    }

    /**
     * Create a new Expedia user and associate the newly created user with the provided Facebook account and
     * primary email address from Facebook.
     */
    private fun facebookLinkNewAccount(context: Context) {
        val user = Db.getNewUser()
        if (!user.isFacebookUser) {
            throw RuntimeException("Not a Facebook user")
        }
        config.service.facebookLinkNewAccount(user.facebookUserId, user.facebookToken, user.email)
                .subscribe(object : Observer<FacebookLinkResponse> {
                    override fun onComplete() {
                    }

                    override fun onError(e: Throwable) {
                        Log.d("FACEBOOK: unable to facebookLinkNewAccount: $e")
                        showErrorFacebookUnknown(context)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(response: FacebookLinkResponse) {
                        if (response.isSuccess) {
                            facebookSignInRefreshProfile(context)
                        } else {
                            Log.d("FACEBOOK: facebookLinkNewAccount failure: $response")
                            showErrorFacebookUnknown(context)
                        }
                    }
                })
    }

    /**
     * Create a new Expedia user and associate the newly created user with the provided Facebook account and
     * primary email address from Facebook.
     */
    private fun facebookLinkExistingAccount(context: Context) {
        val user = Db.getNewUser()
        if (!user.isFacebookUser) {
            throw RuntimeException("Not a Facebook user")
        }

        config.service.facebookLinkExistingAccount(user.facebookUserId,
                user.facebookToken, user.email, user.password)
                .subscribe(object : Observer<FacebookLinkResponse> {
                    override fun onComplete() {
                    }

                    override fun onError(e: Throwable) {
                        showErrorFacebookLinkExisting(context)
                    }

                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(response: FacebookLinkResponse) {
                        if (response.isSuccess) {
                            facebookSignInRefreshProfile(context)
                        } else {
                            showErrorFacebookLinkExisting(context)
                        }
                    }
                })
    }

    private fun facebookSignInRefreshProfile(context: Context) {
        config.service.signInProfileOnly()
                .subscribe(object : Observer<AccountResponse> {
                    override fun onComplete() {
                    }

                    override fun onError(e: Throwable) {
                        Log.d("FACEBOOK: unable to facebookSignInRefreshProfile: $e")
                        showErrorFacebookUnknown(context)
                    }

                    override fun onSubscribe(d: Disposable) {
                        Log.d("FACEBOOK: onsubscribe")
                    }

                    override fun onNext(response: AccountResponse) {
                        if (response.success) {
                            facebookSignInSuccessful()
                        } else {
                            Log.d("FACEBOOK: facebookSignInRefreshProfile not successful: $response")
                            showErrorFacebookUnknown(context)
                        }
                    }
                })
    }

    private fun facebookSignInSuccessful() {
        config.accountSignInListener.onFacebookSignInSuccess()
        config.accountSignInListener.onSignInSuccessful()
    }

    private fun showNewOrExistingAccountDialog(context: Context) {
        newAccountView.cancelLoading()
        val message = Utils.obtainBrandedPhrase(context,
                R.string.acct__fb_notLinked_description_TEMPLATE, brand)
                .put("email_address", Db.getNewUser().email).format()

        AlertDialog.Builder(context)
                .setTitle(R.string.acct__fb_notLinked_title)
                .setMessage(message)
                .setNegativeButton(R.string.acct__fb_notLinked_new_button) { _, _ ->
                    newAccountView.showLoading()
                    facebookLinkNewAccount(context)
                }
                .setPositiveButton(R.string.acct__fb_notLinked_existing_button) { _, _ ->
                    Db.getNewUser().email = ""
                    newAccountView.showFacebookLinkAccountsView(FacebookLinkAccountsLayout.SetupType.ACCOUNT_NOT_LINKED)
                    // We don't recognize your facebook email address,
                    // and you choose "sign in with existing account",
                    // meaning you want to use a different email address.
                }
                .setCancelable(true)
                .setOnCancelListener { onFacebookLoginCancelled() }
                .create()
                .show()
    }

    private fun showErrorFacebookUnknown(context: Context) {
        facebookLogOut()
        showErrorDialog(R.string.acct__Sign_in_failed_TITLE,
                context.getString(R.string.acct__fb_unable_to_sign_into_facebook))
    }

    private fun showErrorFacebookLinkExisting(context: Context) {
        facebookLogOut()
        showErrorDialog(R.string.acct__Sign_in_failed_TITLE,
                Utils.obtainBrandedPhrase(context, R.string.acct__fb_link_existing_failed, brand).format())
    }

    private fun showErrorFacebookDeclinedEmailAddress(context: Context) {
        facebookLogOut()
        showErrorDialog(R.string.acct__fb_user_denied_email_heading,
                Utils.obtainBrandedPhrase(context, R.string.acct__fb_user_denied_email_message, brand).format())
    }

    private fun showErrorFacebookMissingEmailAddress(context: Context) {
        facebookLogOut()
        showErrorDialog(R.string.acct__fb_user_missing_email_heading,
                Utils.obtainBrandedPhrase(context, R.string.acct__fb_user_missing_email_message, brand).format())
    }

    private fun showErrorDialog(@StringRes titleId: Int, message: CharSequence) {
        AlertDialog.Builder(context)
                .setTitle(titleId)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show()
    }

    /**
     * Log out from facebook when we failed signing in at our end.
     */
    private fun facebookLogOut() {
        newAccountView.cancelLoading()
        AccountService.facebookLogOut()
    }
}
