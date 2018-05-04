package com.expedia.bookings.itin.hotel.taxi

import android.os.Bundle
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinTaxiActivityTest {
    lateinit var sut: HotelItinTaxiActivity

    @Before
    fun setup() {
        sut = Robolectric.buildActivity(TestHotelItinTaxiActivity::class.java).create().resume().get()
    }

    @Test
    fun invalidItinShouldFinishTest() {
        val shadow = shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.repo.liveDataInvalidItin.postValue(Unit)
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun backNavClickShouldFinishTest() {
        val shadow = shadowOf(sut)
        assertFalse(shadow.isFinishing)
        sut.navigationButton.performClick()
        assertTrue(shadow.isFinishing)
    }

    @Test
    fun textViewsHappy() {
        assertEquals(View.GONE, sut.localizedAddressTextView.visibility)
        assertEquals("", sut.localizedAddressTextView.text.toString())
        assertEquals(View.GONE, sut.localizedLocationNameTextView.visibility)
        assertEquals("", sut.localizedLocationNameTextView.text.toString())
        assertEquals(View.GONE, sut.nonLocalizedAddressTextView.visibility)
        assertEquals("", sut.nonLocalizedAddressTextView.text.toString())
        assertEquals(View.GONE, sut.nonLocalizedLocationNameTextView.visibility)
        assertEquals("", sut.nonLocalizedLocationNameTextView.text.toString())

        val localizedAddress = "localizedAddressTest"
        val nonLocalizedAddress = "nonLocalizedAddressTest"
        val localizedLocationName = "localizedLocationNameTest"
        val nonLocalizedLocationName = "nonLocalizedLocationNameTest"

        sut.viewModel.localizedAddressSubject.onNext(localizedAddress)
        sut.viewModel.localizedLocationNameSubject.onNext(localizedLocationName)
        sut.viewModel.nonLocalizedAddressSubject.onNext(nonLocalizedAddress)
        sut.viewModel.nonLocalizedLocationNameSubject.onNext(nonLocalizedLocationName)

        assertEquals(View.VISIBLE, sut.localizedAddressTextView.visibility)
        assertEquals(localizedAddress, sut.localizedAddressTextView.text.toString())
        assertEquals(View.VISIBLE, sut.localizedLocationNameTextView.visibility)
        assertEquals(localizedLocationName, sut.localizedLocationNameTextView.text.toString())
        assertEquals(View.VISIBLE, sut.nonLocalizedAddressTextView.visibility)
        assertEquals(nonLocalizedAddress, sut.nonLocalizedAddressTextView.text.toString())
        assertEquals(View.VISIBLE, sut.nonLocalizedLocationNameTextView.visibility)
        assertEquals(nonLocalizedLocationName, sut.nonLocalizedLocationNameTextView.text.toString())
    }

    class TestHotelItinTaxiActivity : HotelItinTaxiActivity() {
        override val repo: ItinHotelRepoInterface = MockHotelRepo()
        override fun onCreate(savedInstanceState: Bundle?) {
            setTheme(R.style.ItinTheme)
            super.onCreate(savedInstanceState)
        }
    }
}
