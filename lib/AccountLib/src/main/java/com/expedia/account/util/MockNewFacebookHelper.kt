package com.expedia.account.util

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import com.expedia.account.Config
import com.expedia.account.NewAccountView
import com.facebook.AccessToken
import com.facebook.login.LoginResult
import com.mobiata.android.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.HashSet

class MockNewFacebookHelper(context: Context, config: Config, brand: String, newAccountView: NewAccountView) : NewFacebookHelper(context, config, brand, newAccountView) {

    companion object {
        const val CANCEL = "cancel"
        const val DENIED_EMAIL = "Denied email permission"
        const val MISSING_EMAIL = "Missing email"
        const val NOT_LINKED = "notLinked"
        const val EXISTING = "existing"
        const val SUCCESS = "success"
        const val ERROR = "generic Facebook error"

        const val DENIED_EMAIL_ADDRESS = "deniedemail@fb.com"
        const val NOT_LINKED_ADDRESS = "notlinked@fb.com"
        const val EXISTING_ADDRESS = "existing@fb.com"
        const val SUCCESS_ADDRESS = "success@fb.com"
        const val MISSING_EMAIL_USERID = "1234"
    }

    override fun doFacebookLogin(context: Context) {
        val codeList = arrayOf(CANCEL, DENIED_EMAIL, MISSING_EMAIL, NOT_LINKED, EXISTING, SUCCESS, ERROR)
        val listener = DialogInterface.OnClickListener { _, which ->
            val code = codeList[which]
            when (code) {
                CANCEL -> onFacebookLoginCancelled()
                DENIED_EMAIL -> onFacebookLoginSuccess(context, MockedLoginResult.deniedEmailInstance)
                MISSING_EMAIL -> onFacebookLoginSuccess(context, MockedLoginResult.missingEmailInstance)
                NOT_LINKED -> onFacebookLoginSuccess(context, MockedLoginResult.notLinkedInstance)
                EXISTING -> onFacebookLoginSuccess(context, MockedLoginResult.existingInstance)
                SUCCESS -> onFacebookLoginSuccess(context, MockedLoginResult.successInstance)
                ERROR -> onFacebookLoginError(context)
            }
        }
        AlertDialog.Builder(context)
                .setTitle("Choose scenario")
                .setItems(codeList, listener)
                .setCancelable(false)
                .create()
                .show()
    }

    private class MockedLoginResult : LoginResult {

        private constructor(accessToken: AccessToken, denied: Set<String>) : super(accessToken, HashSet<String>(), denied)
        private constructor(accessToken: AccessToken) : super(accessToken, HashSet<String>(), HashSet<String>())

        companion object {
            const val APP_ID = "AccountCreation"

            @JvmStatic private fun getAccessToken(scenario: CharSequence, emailAddress: String): AccessToken {
                val permissions = HashSet<String>()
                return AccessToken(scenario.toString(), APP_ID, emailAddress, null, permissions, null, null, null)
            }

            val deniedEmailInstance: MockedLoginResult
                get() {
                    val denied = HashSet<String>()
                    denied.add("email")
                    val token = AccessToken(DENIED_EMAIL, APP_ID, DENIED_EMAIL_ADDRESS, null, denied, null, null, null)
                    return MockedLoginResult(token, denied)
                }

            val missingEmailInstance: MockedLoginResult
                get() {
                    val token = getAccessToken(MISSING_EMAIL, MISSING_EMAIL_USERID)
                    return MockedLoginResult(token)
                }

            val notLinkedInstance: MockedLoginResult
                get() {
                    val token = getAccessToken(NOT_LINKED, NOT_LINKED_ADDRESS)
                    return MockedLoginResult(token)
                }

            val existingInstance: MockedLoginResult
                get() {
                    val token = getAccessToken(EXISTING, EXISTING_ADDRESS)
                    return MockedLoginResult(token)
                }

            val successInstance: MockedLoginResult
                get() {
                    val token = getAccessToken(SUCCESS, SUCCESS_ADDRESS)
                    return MockedLoginResult(token)
                }
        }
    }

    override fun fetchFacebookUserInfo(context: Context, token: AccessToken) {
        try {
            val jsonObject = JSONObject()
            if (!token.declinedPermissions.contains("email") && token.userId != MISSING_EMAIL_USERID) {
                jsonObject.put("email", token.userId)
            }
            jsonObject.put("first_name", "Test")
            jsonObject.put("last_name", "Testerson")
            onFacebookUserInfoFetched(context, jsonObject)
        } catch (e: JSONException) {
            Log.e(e.toString())
        }
    }
}
