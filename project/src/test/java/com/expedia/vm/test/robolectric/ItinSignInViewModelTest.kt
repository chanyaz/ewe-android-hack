package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.itin.ItinSignInViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinSignInViewModelTest {
    lateinit private var activity: Activity
    lateinit private var sut: ItinSignInViewModel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = ItinSignInViewModel(activity)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun signInButtonText() {
        assertEquals("Sign in with Expedia", sut.getSignInText())
        assertEquals("Sign in with Expedia button", sut.getSignInContentDescription())
    }
}
