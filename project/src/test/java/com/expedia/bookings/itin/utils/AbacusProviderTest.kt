package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AbacusProviderTest {
    lateinit var sut: AbacusProvider
    lateinit var context: Context
    lateinit var abtest: ABTest

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = AbacusProvider(context)
        abtest = ABTest(1337)
    }

    @Test
    fun unbucketedTest() {
        assertFalse(sut.isBucketedForTest(abtest))
    }

    @Test
    fun bucketedTest() {
        AbacusTestUtils.bucketTests(abtest)
        assertTrue(sut.isBucketedForTest(abtest))
    }

    @After
    fun tearDown() {
        AbacusTestUtils.unbucketTests(abtest)
    }
}
