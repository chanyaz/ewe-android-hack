package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FlightItinTravelerPreferenceViewModelTest {

    lateinit private var sut: FlightItinTravelerPreferenceViewModel

    val testString = "12345"

    val createTravelerSubscriber = TestSubscriber<Traveler>()
    val createKnownTravelerSubscriber = TestSubscriber<CharSequence>()
    val createRedressSubscriber = TestSubscriber<CharSequence>()
    val createSpecialRequestSubscriber = TestSubscriber<CharSequence>()
    val createFrequentFlyerSubscriber = TestSubscriber<Map<String, TravelerFrequentFlyerMembership>>()

    @Before
    fun setup() {
        sut = FlightItinTravelerPreferenceViewModel()
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
    fun testKnownTravelerSubject() {
        sut.knownTravelerNumberSubject.subscribe(createKnownTravelerSubscriber)
        createKnownTravelerSubscriber.assertNoValues()
        sut.knownTravelerNumberSubject.onNext(testString)
        createKnownTravelerSubscriber.assertValue(testString)
    }

    @Test
    fun testRedressSubject() {
        sut.redressNumberSubject.subscribe(createRedressSubscriber)
        createRedressSubscriber.assertNoValues()
        sut.redressNumberSubject.onNext(testString)
        createRedressSubscriber.assertValue(testString)
    }

    @Test
    fun testSpecialRequestSubject() {
        sut.specialRequestSubject.subscribe(createSpecialRequestSubscriber)
        createSpecialRequestSubscriber.assertNoValues()
        sut.specialRequestSubject.onNext(testString)
        createSpecialRequestSubscriber.assertValue(testString)
    }

    @Test
    fun testFrequentFlyerSubject() {
        sut.frequentFlyerSubject.subscribe(createFrequentFlyerSubscriber)
        createFrequentFlyerSubscriber.assertNoValues()
        val map = HashMap<String, TravelerFrequentFlyerMembership>()
        sut.frequentFlyerSubject.onNext(map)
        createFrequentFlyerSubscriber.assertValue(map)
    }
}