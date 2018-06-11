package com.expedia.account.util

import android.content.Context
import android.net.ConnectivityManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class AndroidNetworkConnectivityTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun isOnline_returnsTrue_whenConnected() {
        val sut = AndroidNetworkConnectivity(context)

        assertTrue(sut.isOnline())
    }

    @Test
    fun isOnline_returnsFalse_whenNetworkNull() {
        val shadowConnectivityManager = Shadows.shadowOf(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        shadowConnectivityManager.activeNetworkInfo = null
        val sut = AndroidNetworkConnectivity(context)

        assertFalse(sut.isOnline())
    }
}
