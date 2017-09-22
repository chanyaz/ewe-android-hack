package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.ForceBucketPref
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ForceBucketPrefTest {

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun userIsForceBucketed() {
        val context: Context = getContext()
        ForceBucketPref.setUserForceBucketed(context, true)

        assertTrue(ForceBucketPref.isForceBucketed(context))
    }

    @Test
    fun forceBucketingCanBeToggledMultipleTimes() {
        val context: Context = getContext()
        ForceBucketPref.setUserForceBucketed(context, true)
        assertTrue(ForceBucketPref.isForceBucketed(context))

        ForceBucketPref.setUserForceBucketed(context, false)
        assertFalse(ForceBucketPref.isForceBucketed(context))
    }

    @Test
    fun userIsNotForceBucketed() {
        val context: Context = getContext()
        ForceBucketPref.setUserForceBucketed(context, false)

        assertFalse(ForceBucketPref.isForceBucketed(context))
    }

    @Test
    fun forceBucketedTestKeyValue() {
        val context: Context = getContext()

        //when the user is not force bucketed
        ForceBucketPref.saveForceBucketedTestKeyValue(context, 1111, 0)
        assertEquals(-1, ForceBucketPref.getForceBucketedTestValue(context, 1111, -1))

        //when the user is force bucketed
        ForceBucketPref.setUserForceBucketed(context, true)
        ForceBucketPref.saveForceBucketedTestKeyValue(context, 1111, 0)
        assertEquals(0, ForceBucketPref.getForceBucketedTestValue(context, 1111, -1))

        //when the user resets force bucketing settings
        ForceBucketPref.setUserForceBucketed(context, false)
        ForceBucketPref.saveForceBucketedTestKeyValue(context, 1111, 0)
        assertEquals(-1, ForceBucketPref.getForceBucketedTestValue(context, 1111, -1))
    }
}