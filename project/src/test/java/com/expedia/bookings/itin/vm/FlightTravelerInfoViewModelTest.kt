package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class FlightTravelerInfoViewModelTest {
    lateinit private var sut: FlightTravelerInfoViewModel
    private val testString = "123456"
    private val createTravelerSubscriber = TestObserver<Traveler>()
    private val createTravelerNameSubscriber = TestObserver<CharSequence>()
    private val createTravelerEmailSubscriber = TestObserver<CharSequence>()
    private val createTicketNumberSubscriber = TestObserver<CharSequence>()
    private val createPhoneNumberSubscriber = TestObserver<CharSequence>()
    private val createInfantInLapSubscriber = TestObserver<CharSequence>()

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