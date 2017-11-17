package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightTravelerInfoViewModelTest {
    lateinit private var sut: FlightTravelerInfoViewModel
    private val testString = "123456"
    private val createTravelerSubscriber = TestSubscriber<Traveler>()
    private val createTravelerNameSubscriber = TestSubscriber<CharSequence>()
    private val createTravelerEmailSubscriber = TestSubscriber<CharSequence>()
    private val createTicketNumberSubscriber = TestSubscriber<CharSequence>()
    private val createPhoneNumberSubscriber = TestSubscriber<CharSequence>()
    private val createInfantInLapSubscriber = TestSubscriber<CharSequence>()

    @Before
    fun setup() {
        sut = FlightTravelerInfoViewModel()
    }

    @Test
    fun testTravelerSubject() {
        sut.travelerObservable.subscribe(createTravelerSubscriber)
        createTravelerSubscriber.assertNoValues()
        val traveler = Traveler()
        sut.travelerObservable.onNext(traveler)
        createTravelerSubscriber.assertValue(traveler)
    }

    @Test
    fun testNameSubject() {
        sut.travelerNameSubject.subscribe(createTravelerNameSubscriber)
        createTravelerNameSubscriber.assertNoValues()
        sut.travelerNameSubject.onNext(testString)
        createTravelerNameSubscriber.assertValue(testString)
    }

    @Test
    fun testEmailSubject() {
        sut.travelerEmailSubject.subscribe(createTravelerEmailSubscriber)
        createTravelerEmailSubscriber.assertNoValues()
        sut.travelerEmailSubject.onNext(testString)
        createTravelerEmailSubscriber.assertValue(testString)
    }

    @Test
    fun testTicketSubject() {
        sut.ticketNumberSubject.subscribe(createTicketNumberSubscriber)
        createTicketNumberSubscriber.assertNoValues()
        sut.ticketNumberSubject.onNext(testString)
        createTicketNumberSubscriber.assertValue(testString)
    }
    @Test
    fun testPhoneSubject() {
        sut.travelerPhoneSubject.subscribe(createPhoneNumberSubscriber)
        createPhoneNumberSubscriber.assertNoValues()
        sut.travelerPhoneSubject.onNext(testString)
        createPhoneNumberSubscriber.assertValue(testString)
    }

    @Test
    fun testInfantSubject() {
        sut.infantInLapSubject.subscribe(createInfantInLapSubscriber)
        createInfantInLapSubscriber.assertNoValues()
        sut.infantInLapSubject.onNext(testString)
        createInfantInLapSubscriber.assertValue(testString)
    }
}