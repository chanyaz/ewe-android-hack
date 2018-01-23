package com.expedia.bookings.itin

import android.content.Context
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.itin.activity.FlightItinTravelerInfoActivity
import com.expedia.bookings.itin.vm.FlightItinTravelerViewModel
import com.expedia.bookings.itin.vm.FlightTravelerInfoViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.squareup.phrase.Phrase
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightItinTravelerInfoActivityTest {

    lateinit var sut: FlightItinTravelerInfoActivity
    lateinit var context: Context
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private val travelerTicketSubscriber = TestObserver<CharSequence>()
    private val travelerPhoneSubscriber = TestObserver<CharSequence>()
    lateinit var testItinCardData: ItinCardDataFlight

    @Before
    fun setup() {
        sut = Robolectric.buildActivity(FlightItinTravelerInfoActivity::class.java).get()
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        sut.viewModel = FlightItinTravelerViewModel(context, "test123")
        sut.travelerInfoViewModel = FlightTravelerInfoViewModel()
    }

    @Test
    fun testOnFinish() {
        val sutSpy = Mockito.spy(sut)
        sutSpy.finishActivity()
        Mockito.verify(sutSpy, Mockito.times(1)).finish()
        Mockito.verify(sutSpy, Mockito.times(1)).overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }

    @Test
    fun testCreateIntent() {
        val testId = "988877742"
        val intent = FlightItinTravelerInfoActivity.createIntent(context, testId)
        assertEquals(intent.extras.getString("FLIGHT_ITIN_ID"), testId)
    }

    @Test
    fun testOmniture() {
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
        sut.trackOmniture()
        OmnitureTestUtils.assertStateTracked("App.Itinerary.Flight.TravelerInfo",
                Matchers.allOf(
                        OmnitureMatchers.withProps(mapOf(2 to "itinerary")),
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to "App.Itinerary.Flight.TravelerInfo"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testTravelerTicketsNull() {
        sut.travelerInfoViewModel.ticketNumberSubject.subscribe(travelerTicketSubscriber)
        travelerTicketSubscriber.assertNoValues()
        val traveler = Traveler()
        sut.travelerInfoViewModel.travelerObservable.onNext(traveler)
        travelerTicketSubscriber.assertNoValues()
    }

    @Test
    fun testTravelerTicketsEmpty() {
        sut.travelerInfoViewModel.ticketNumberSubject.subscribe(travelerTicketSubscriber)
        travelerTicketSubscriber.assertNoValues()
        val traveler = Traveler()
        traveler.ticketNumbers = arrayListOf()
        sut.travelerInfoViewModel.travelerObservable.onNext(traveler)
        travelerTicketSubscriber.assertNoValues()
    }

    @Test
    fun testTravelerTicketsHasNumbers() {
        sut.travelerInfoViewModel.ticketNumberSubject.subscribe(travelerTicketSubscriber)
        travelerTicketSubscriber.assertNoValues()
        val traveler = Traveler()
        traveler.ticketNumbers = arrayListOf("21323", "t55raa33")
        sut.travelerInfoViewModel.travelerObservable.onNext(traveler)
        travelerTicketSubscriber.assertValue(Phrase.from(context, R.string.itin_traveler_ticket_number_TEMPLATE)
                .put("number", "21323, t55raa33")
                .format().toString())
    }

    @Test
    fun testTravelerPhoneNumber() {
        sut.travelerInfoViewModel.travelerPhoneSubject.subscribe(travelerPhoneSubscriber)
        travelerPhoneSubscriber.assertNoValues()
        val traveler = Traveler()
        traveler.phoneCountryCode = "1"
        traveler.phoneNumber = "333 444 5555"
        sut.travelerInfoViewModel.travelerObservable.onNext(traveler)
        travelerPhoneSubscriber.assertValue("+1 333 444 5555")
    }

    @Test
    fun testTravelerPhoneNumberNoCountryCode() {
        sut.travelerInfoViewModel.travelerPhoneSubject.subscribe(travelerPhoneSubscriber)
        travelerPhoneSubscriber.assertNoValues()
        val traveler = Traveler()
        traveler.phoneNumber = "333 444 5555"
        sut.travelerInfoViewModel.travelerObservable.onNext(traveler)
        travelerPhoneSubscriber.assertNoValues()
    }

    @Test
    fun testTravelerPhoneNumberNoNumber() {
        sut.travelerInfoViewModel.travelerPhoneSubject.subscribe(travelerPhoneSubscriber)
        travelerPhoneSubscriber.assertNoValues()
        val traveler = Traveler()
        traveler.phoneCountryCode = "1"
        sut.travelerInfoViewModel.travelerObservable.onNext(traveler)
        travelerPhoneSubscriber.assertNoValues()
    }

    @Test
    fun testInvalidCard() {
        val activityShadow = Shadows.shadowOf(sut)
        sut.viewModel.itinCardDataNotValidSubject.onNext(Unit)
        assertTrue(activityShadow.isFinishing)
    }
}
