package com.expedia.account

interface AccountSignInListener {
    fun onSignInSuccessful()

    fun onFacebookSignInSuccess()

    fun onSignInCancelled()

    fun onFacebookRequested()

    fun onFacebookClicked()

    fun onForgotPassword()

    fun onRecaptchaError(e: Throwable)
}
