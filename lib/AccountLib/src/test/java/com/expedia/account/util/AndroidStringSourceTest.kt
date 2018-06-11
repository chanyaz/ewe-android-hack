package com.expedia.account.util

import android.content.Context
import com.expedia.account.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AndroidStringSourceTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun getString_getsStringFromResources() {
        val sut = AndroidStringSource(context)

        val actual = sut.getString(R.string.acct__Sign_in_failed_TITLE)

        assertEquals("Sign in failed", actual)
    }

    @Test
    fun getBrandedString_getsAndBrandsStringFromResources() {
        val sut = AndroidStringSource(context)

        val expediaActual = sut.getBrandedString(R.string.acct__Welcome_to_brand, "Expedia")
        assertEquals("Welcome to Expedia", expediaActual)

        val orbitzActual = sut.getBrandedString(R.string.acct__Welcome_to_brand, "Orbitz")
        assertEquals("Welcome to Orbitz", orbitzActual)
    }
}
