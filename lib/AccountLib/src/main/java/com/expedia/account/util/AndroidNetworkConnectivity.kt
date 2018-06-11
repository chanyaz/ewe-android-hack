package com.expedia.account.util

import android.content.Context

class AndroidNetworkConnectivity(private val context: Context) : NetworkConnectivity {
    override fun isOnline(): Boolean {
        return Utils.isOnline(context)
    }
}
