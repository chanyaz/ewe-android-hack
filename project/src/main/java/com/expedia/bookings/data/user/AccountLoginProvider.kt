package com.expedia.bookings.data.user

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity
import android.os.Bundle
import android.os.Handler

open class AccountLoginProvider(val manager: AccountManager? = null) {
    open fun getAccountsByType(type: String): Array<out Account>? = manager?.getAccountsByType(type)

    open fun getAuthToken(account: Account, accountType: String, options: Bundle?, activity: Activity, callback: AccountManagerCallback<Bundle>?, handler: Handler?) {
        manager?.getAuthToken(account, accountType, options, activity, callback, handler)
    }

    open fun addAccount(
        accountType: String,
        authTokenType: String,
        requiredFeatures: Array<out String>?,
        addAccountOptions: Bundle?,
        activity: Activity,
        callback: AccountManagerCallback<Bundle>?,
        handler: Handler?
    ): AccountManagerFuture<Bundle>? = manager?.addAccount(accountType, authTokenType, requiredFeatures, addAccountOptions, activity, callback, handler)
}
