package com.expedia.bookings.itin.common

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ItinMapWidgetTest {
    lateinit var sut: ItinMapWidget
    private lateinit var mockVM: MockViewModel

    @Before
    fun setup() {
        val context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_itin_map_widget, null) as ItinMapWidget
        mockVM = MockViewModel()
    }

    @Test
    fun textViewVisibilityAndTextTest() {
        val firstString = "FirstString"
        val secondString = "SecondString"
        sut.setUpViewModel(mockVM)
        assertEquals(View.GONE, sut.addressLineFirst.visibility)
        assertEquals(View.GONE, sut.addressLineSecond.visibility)
        sut.viewModel.addressLineFirstSubject.onNext(firstString)
        sut.viewModel.addressLineSecondSubject.onNext(secondString)
        assertEquals(View.VISIBLE, sut.addressLineFirst.visibility)
        assertEquals(View.VISIBLE, sut.addressLineSecond.visibility)
        assertEquals(firstString, sut.addressLineFirst.text.toString())
        assertEquals(secondString, sut.addressLineSecond.text.toString())
    }

    @Test
    fun phoneNumberTextAndContentDescTest() {
        val number = "123 213 2134"
        val contDesc = "777"
        sut.setUpViewModel(mockVM)
        assertEquals(View.GONE, sut.phoneNumber.visibility)
        assertEquals(View.GONE, sut.divider.visibility)
        assertEquals("", sut.phoneNumber.text)
        assertEquals(null, sut.phoneNumber.contentDescription)

        sut.viewModel.phoneNumberTextSubject.onNext(number)

        assertEquals(View.VISIBLE, sut.phoneNumber.visibility)
        assertEquals(View.VISIBLE, sut.divider.visibility)
        assertEquals(number, sut.phoneNumber.text)
        assertEquals(null, sut.phoneNumber.contentDescription)

        sut.viewModel.phoneNumberContDescriptionSubject.onNext(contDesc)

        assertEquals(contDesc, sut.phoneNumber.contentDescription)
    }

    @Test
    fun phoneNumberClickTest() {
        sut.setUpViewModel(mockVM)
        assertFalse(mockVM.phoneNumberClicked)
        sut.phoneNumber.performClick()
        assertTrue(mockVM.phoneNumberClicked)
    }

    @Test
    fun addressContainerTest() {
        sut.setUpViewModel(mockVM)
        assertFalse(mockVM.addressContainerClicked)
        sut.addressContainer.performClick()
        assertTrue(mockVM.addressContainerClicked)

        assertEquals(null, sut.addressContainer.contentDescription)
        mockVM.addressContainerContentDescription.onNext("Content Description")
        assertEquals("Content Description", sut.addressContainer.contentDescription)
    }

    @Test
    fun clickListenerTest() {
        sut.setUpViewModel(mockVM)
        assertFalse(mockVM.directionSubjectClicked)
        assertFalse(mockVM.mapSubjectClicked)
        sut.map.performClick()
        sut.directionsButton.performClick()
        assertTrue(mockVM.directionSubjectClicked)
        assertTrue(mockVM.mapSubjectClicked)
    }

    private class MockViewModel : ItinMapWidgetViewModel() {
        var mapSubjectClicked = false
        var directionSubjectClicked = false
        var addressContainerClicked = false
        var phoneNumberClicked = false

        init {
            mapClickSubject.subscribe {
                mapSubjectClicked = true
            }
            directionButtonClickSubject.subscribe {
                directionSubjectClicked = true
            }
            addressClickSubject.subscribe {
                addressContainerClicked = true
            }
            phoneNumberClickSubject.subscribe {
                phoneNumberClicked = true
            }
        }
    }
}
