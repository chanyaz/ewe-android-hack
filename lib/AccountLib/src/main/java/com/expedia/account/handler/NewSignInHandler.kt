package com.expedia.account.handler

import com.expedia.account.Config
import com.expedia.account.R
import com.expedia.account.ViewWithLoadingIndicator
import com.expedia.account.data.AccountResponse
import com.expedia.account.recaptcha.RecaptchaHandler
import com.expedia.account.util.NetworkConnectivity
import com.expedia.account.util.SimpleDialogBuilder
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class NewSignInHandler(private val dialogBuilder: SimpleDialogBuilder,
                       private val networkConnectivity: NetworkConnectivity,
                       private val config: Config,
                       private val email: String,
                       private val password: String,
                       private val loadingView: ViewWithLoadingIndicator) : RecaptchaHandler {

    private var accountLoadingDisposable: Disposable? = null

    override fun onRecaptchaSuccess(recaptchaResponseToken: String) {
        doSignIn(recaptchaResponseToken)
    }

    override fun onRecaptchaFailure() {
        showRecaptchaError()
    }

    fun doSignIn(recaptchaResponseToken: String?) {
        config.service.signIn(email, password, recaptchaResponseToken)
                .subscribe(object : Observer<AccountResponse> {
                    override fun onComplete() {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                    }

                    override fun onError(e: Throwable) {
                        accountLoadingDisposable?.dispose()
                        accountLoadingDisposable = null
                        loadingView.cancelLoading()
                        showNetworkSignInError()
                    }

                    override fun onSubscribe(d: Disposable) {
                        accountLoadingDisposable = d
                    }

                    override fun onNext(response: AccountResponse) {
                        if (!response.success) {
                            loadingView.cancelLoading()
                            showResponseSignInError(response)
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

    private fun showRecaptchaError() {
        config.analyticsListener?.userReceivedErrorOnSignInAttempt("Account:recaptcha failure")
        showSignInErrorDialog(AccountResponse().SignInFailureError())
    }

    // Networking error
    private fun showNetworkSignInError() {
        config.analyticsListener?.userReceivedErrorOnSignInAttempt("Account:local")
        showSignInErrorDialog(AccountResponse().SignInFailureError())
    }

    // API returned !success
    private fun showResponseSignInError(response: AccountResponse) {
        config.analyticsListener?.userReceivedErrorOnSignInAttempt("Account:" + (response.errors?.get(0)?.errorInfo?.cause ?: "server"))
        showSignInErrorDialog(response.SignInFailureError())
    }

    private fun showSignInErrorDialog(signInError: AccountResponse.SignInError) {
        val errorTitle: Int
        val errorMessage: Int

        if (networkConnectivity.isOnline()) {
            when (signInError) {
                AccountResponse.SignInError.ACCOUNT_LOCKED -> {
                    errorTitle = R.string.acct__Sign_in_locked_TITLE
                    errorMessage = R.string.acct__Sign_in_locked
                }
                AccountResponse.SignInError.INVALID_CREDENTIALS -> {
                    errorTitle = R.string.acct__Sign_in_failed_TITLE
                    errorMessage = R.string.acct__Sign_in_failed
                }
                else -> {
                    errorTitle = R.string.acct__Sign_in_failed_TITLE
                    errorMessage = R.string.acct__Sign_in_failed_generic
                }
            }
        } else {
            errorTitle = R.string.acct__Sign_in_failed_TITLE
            errorMessage = R.string.acct__no_network_connection
        }

        dialogBuilder.showSimpleDialog(titleResId = errorTitle, messageResId = errorMessage, buttonLabelResId = android.R.string.ok)
    }
}
