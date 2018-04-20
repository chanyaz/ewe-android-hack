package com.expedia.account.handler

import android.content.Context
import android.support.v7.app.AlertDialog
import com.expedia.account.Config
import com.expedia.account.R
import com.expedia.account.data.AccountResponse
import com.expedia.account.recaptcha.RecaptchaHandler
import com.expedia.account.util.Utils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class NewSignInHandler(val context: Context, val config: Config, private val email: String, private val password: String) : RecaptchaHandler {

    private var accountLoadingDisposable: Disposable? = null

    override fun onRecaptchaSuccess(recaptchaResponseToken: String) {
        doSignIn(recaptchaResponseToken)
    }

    override fun onRecaptchaFailure() {
        doSignIn(null)
    }

    fun doSignIn(recaptchaResponseToken: String?) {
        config.service.signIn(email, password, recaptchaResponseToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<AccountResponse> {
                    override fun onComplete() {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                    }

                    override fun onError(e: Throwable) {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                        showSignInError()
                    }

                    override fun onSubscribe(d: Disposable) {
                        accountLoadingDisposable = d
                    }

                    override fun onNext(response: AccountResponse) {
                        if (!response.success) {
                            showSignInError(response)
                        } else {
                            doSignInSuccessful()
                        }
                    }
                })
    }

    private fun doSignInSuccessful() {
        config.analyticsListener?.signInSucceeded()
        config.accountSignInListener?.onSignInSuccessful()
    }

    // Networking error
    private fun showSignInError() {
        config.analyticsListener?.userReceivedErrorOnSignInAttempt("Account:local")
        showSignInErrorGeneric()
    }

    // API returned !success
    private fun showSignInError(response: AccountResponse) {
        config.analyticsListener?.userReceivedErrorOnSignInAttempt("Account:" + (response.errors?.get(0)?.errorInfo?.cause ?: "server"))
        showSignInErrorGeneric(response.SignInFailureError())
    }

    private fun showSignInErrorGeneric() {
        showSignInErrorGeneric(AccountResponse().SignInFailureError())
    }

    private fun showSignInErrorGeneric(signInError: AccountResponse.SignInError) {
        val errorMessage: Int
        val errorTitle: Int

        when (signInError) {
            AccountResponse.SignInError.ACCOUNT_LOCKED -> {
                errorMessage = R.string.acct__Sign_in_locked
                errorTitle = R.string.acct__Sign_in_locked_TITLE
            }
            AccountResponse.SignInError.INVALID_CREDENTIALS -> {
                errorMessage = R.string.acct__Sign_in_failed
                errorTitle = R.string.acct__Sign_in_failed_TITLE
            }
            else -> {
                errorMessage = R.string.acct__Sign_in_failed_generic
                errorTitle = R.string.acct__Sign_in_failed_TITLE
            }
        }

        AlertDialog.Builder(context)
                .setTitle(errorTitle)
                .setMessage(
                        if (Utils.isOnline(context)) errorMessage else R.string.acct__no_network_connection)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show()
    }
}
