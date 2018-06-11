package com.expedia.account

interface CreateAccountErrorRecoveryActions {
    fun showSignInPage()
    fun focusEmailAddressField()
    fun focusPasswordField()
    fun focusFirstNameField()
    fun focusLastNameField()
}
