package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.vm.traveler.TravelerSummaryViewModel
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TravelerSummaryViewModelTest {
    lateinit var summaryVM: TravelerSummaryViewModel
    private var activity: Activity by Delegates.notNull()
    private var resources: Resources by Delegates.notNull()

    var expectedEmptyTitle: String by Delegates.notNull()
    var expectedEmptySubTitle: String by Delegates.notNull()
    val expectedEmptyIconStatus = ContactDetailsCompletenessStatus.DEFAULT
    val expectedIncompleteStatus = ContactDetailsCompletenessStatus.INCOMPLETE
    val expectedCompleteStatus = ContactDetailsCompletenessStatus.COMPLETE

    val testName = "Oscar Grouch"
    val testNumber = "773202LUNA"

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
        expectedEmptyTitle = resources.getString(R.string.checkout_enter_traveler_details)
        expectedEmptySubTitle = resources.getString(R.string.checkout_enter_traveler_details_line2)

        summaryVM = TravelerSummaryViewModel(RuntimeEnvironment.application)
    }

    @Test
    fun emptyStateDefaultAfterInit() {
        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, summaryVM.subtitleObservable.value)
        assertEquals(expectedEmptyIconStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToEmptyOneTraveler() {
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.CLEAN)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, summaryVM.subtitleObservable.value)
        assertEquals(expectedEmptyIconStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteOneTravelerNoName() {
        updateDBTravelers(1, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteOneTravelerWithName() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.fullName).thenReturn(testName)
        updateDBTravelers(1, mockTraveler)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(testName, summaryVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToCompleteOneTraveler() {
        updateDBTravelers(1, getCompleteTraveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(testName, summaryVM.titleObservable.value)
        assertEquals(testNumber, summaryVM.subtitleObservable.value)
        assertEquals(expectedCompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun emptyStateDefaultMultipleTravelers() {
        updateDBTravelers(2, getCompleteTraveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.CLEAN)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, summaryVM.subtitleObservable.value)
        assertEquals(expectedEmptyIconStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteMultipleTravelersNoName() {
        updateDBTravelers(2, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(getIncompleteSubTitle(2), summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteMultipleTravelersWithName() {
        updateDBTravelers(2, getCompleteTraveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(testName, summaryVM.titleObservable.value)
        assertEquals(getIncompleteSubTitle(2), summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToCompleteMultipleTraveler() {
        updateDBTravelers(2, getCompleteTraveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(testName, summaryVM.titleObservable.value)
        assertEquals(getIncompleteSubTitle(2), summaryVM.subtitleObservable.value)
        assertEquals(expectedCompleteStatus, summaryVM.iconStatusObservable.value)
    }

    private fun getIncompleteSubTitle(travelerCount: Int) : String {
        return Phrase.from(resources.getQuantityString(R.plurals.checkout_more_travelers_TEMPLATE, travelerCount - 1))
                .put("travelercount", travelerCount - 1).format().toString()
    }

    private fun getCompleteTraveler(): Traveler {
        val mockPhone = Mockito.mock(Phone::class.java)
        Mockito.`when`(mockPhone.number).thenReturn(testNumber)

        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.fullName).thenReturn(testName)
        Mockito.`when`(mockTraveler.primaryPhoneNumber).thenReturn(mockPhone)

        return mockTraveler
    }

    private fun updateDBTravelers(travelerCount: Int, mockTraveler: Traveler) {
        val mockTravelerList = Mockito.mock(List::class.java)
        Mockito.`when`(mockTravelerList.size).thenReturn(travelerCount)
        Mockito.`when`(mockTravelerList.get(Mockito.anyInt())).thenReturn(mockTraveler)
        Db.setTravelers(mockTravelerList as MutableList<Traveler>?)
    }
}