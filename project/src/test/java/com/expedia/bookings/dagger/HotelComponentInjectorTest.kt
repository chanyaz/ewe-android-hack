package com.expedia.bookings.dagger

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelComponentInjectorTest {
    private val context = RuntimeEnvironment.application
    private val testInjector = HotelComponentInjector()

    @Test
    fun testInject() {
        Ui.getApplication(context).setHotelComponent(null)
        assertNull(Ui.getApplication(context).hotelComponent())
        testInjector.inject(context)
        assertNotNull(Ui.getApplication(context).hotelComponent())
    }

    @Test
    fun testClearSameInstance() {
        testInjector.inject(context)
        assertNotNull(Ui.getApplication(context).hotelComponent())
        testInjector.clear(context)
        assertNull(Ui.getApplication(context).hotelComponent())
    }

    //https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/5694
    @Test
    fun testClearDifferentInstance() {
        testInjector.inject(context)
        val newInjector = HotelComponentInjector() // This simulates the HotelActivity being recreated, with a new instance of the injector.
        newInjector.inject(context)

        testInjector.clear(context)
        assertNotNull(Ui.getApplication(context).hotelComponent(), "If we overwrite the original hotel component " +
                "we don't need to null out the new one existing in the application")
    }
}
