package com.expedia.vm.test.traveler

import android.app.Activity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.TravelerPhoneViewModel
import com.expedia.vm.traveler.SimpleTravelerViewModel
import com.expedia.vm.traveler.TravelerEmailViewModel
import com.expedia.vm.traveler.TravelerNameViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class SimpleTravelerViewModelTest {
    lateinit var testViewModel: SimpleTravelerViewModel

    var mockNameViewModel = Mockito.mock(TravelerNameViewModel::class.java)
    var mockEmailViewModel = Mockito.mock(TravelerEmailViewModel::class.java)
    var mockPhoneViewModel = Mockito.mock(TravelerPhoneViewModel::class.java)

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
        addTravelerToDb(Traveler())
        testViewModel = SimpleTravelerViewModel(activity, 0)
    }

    @Test
    fun testValidationBadName() {
        Mockito.`when`(mockNameViewModel.validate()).thenReturn(false)
        Mockito.`when`(mockEmailViewModel.validate()).thenReturn(true)
        Mockito.`when`(mockPhoneViewModel.validate()).thenReturn(true)
        wireUpMockViewModels(testViewModel)

        assertFalse(testViewModel.validate())
    }

    @Test
    fun testValidationBadEmail() {
        Mockito.`when`(mockNameViewModel.validate()).thenReturn(true)
        Mockito.`when`(mockEmailViewModel.validate()).thenReturn(false)
        Mockito.`when`(mockPhoneViewModel.validate()).thenReturn(true)
        wireUpMockViewModels(testViewModel)

        assertFalse(testViewModel.validate())
    }

    @Test
    fun testValidationBadPhone() {
        Mockito.`when`(mockNameViewModel.validate()).thenReturn(true)
        Mockito.`when`(mockEmailViewModel.validate()).thenReturn(true)
        Mockito.`when`(mockPhoneViewModel.validate()).thenReturn(false)
        wireUpMockViewModels(testViewModel)

        assertFalse(testViewModel.validate())
    }

    @Test
    fun testValidationValid() {
        Mockito.`when`(mockNameViewModel.validate()).thenReturn(true)
        Mockito.`when`(mockEmailViewModel.validate()).thenReturn(true)
        Mockito.`when`(mockPhoneViewModel.validate()).thenReturn(true)
        wireUpMockViewModels(testViewModel)

        assertTrue(testViewModel.validate())
    }

    fun wireUpMockViewModels(testVM: SimpleTravelerViewModel) {
        testViewModel.nameViewModel = mockNameViewModel
        testViewModel.emailViewModel = mockEmailViewModel
        testViewModel.phoneViewModel = mockPhoneViewModel
    }

    fun addTravelerToDb(traveler: Traveler) {
        val travelers = Db.getTravelers()
        travelers.clear()
        travelers.add(traveler)
    }
}