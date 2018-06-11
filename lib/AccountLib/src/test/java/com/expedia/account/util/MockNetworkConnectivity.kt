package com.expedia.account.util

class MockNetworkConnectivity : NetworkConnectivity {
    var networkConnected = true

    override fun isOnline(): Boolean {
        return networkConnected
    }
}
