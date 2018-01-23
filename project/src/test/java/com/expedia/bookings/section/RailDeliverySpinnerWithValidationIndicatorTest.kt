package com.expedia.bookings.section

import android.view.View
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class RailDeliverySpinnerWithValidationIndicatorTest {

    val context = RuntimeEnvironment.application

    @Test
    fun testViewVisibilities() {
        val spinnerWithValidationIndicator = RailDeliverySpinnerWithValidationIndicator(context, null)
        assertNotNull(spinnerWithValidationIndicator.spinner)
        assertEquals(View.VISIBLE, spinnerWithValidationIndicator.spinner.visibility)

        assertNotNull(spinnerWithValidationIndicator.validationIndicator)
        assertEquals(View.GONE, spinnerWithValidationIndicator.validationIndicator.visibility)
    }
}
