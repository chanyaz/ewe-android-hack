package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Traveler
import com.expedia.vm.BaseCheckoutViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BaseCheckoutViewModelTest {
    var testViewModel: BaseCheckoutViewModel by Delegates.notNull()
    var activity : Activity by Delegates.notNull()
    private var LOTS_MORE: Long = 100



    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        testViewModel = object : BaseCheckoutViewModel(activity) {
            // blah
        }
        testViewModel.builder.tripId("4321")
        testViewModel.builder.expectedTotalFare("42")
        testViewModel.builder.expectedFareCurrencyCode("USD")
    }

    @Test
    fun testTravelersCleared() {
        val testSubscriber = TestSubscriber<BaseCheckoutParams>()
        val testInfoCompletedSubscriber = TestSubscriber<Boolean>()
        val expectedResults = arrayListOf(false, true, true, true, false, true)
        testViewModel.checkoutParams.subscribe(testSubscriber)
        testViewModel.infoCompleted.subscribe(testInfoCompletedSubscriber)

        var travelers = arrayListOf(getTraveler(), getTraveler(), getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
        testViewModel.paymentCompleted.onNext(getBillingInfo())
        testViewModel.cvvCompleted.onNext("123")

        testSubscriber.requestMore(LOTS_MORE)
        assertEquals(3, testSubscriber.onNextEvents[0].travelers.size)

        travelers = arrayListOf(getTraveler(), getTraveler(), getTraveler(), getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
        testViewModel.paymentCompleted.onNext(getBillingInfo())
        testViewModel.cvvCompleted.onNext("123")

        testSubscriber.requestMore(LOTS_MORE)
        assertEquals(4, testSubscriber.onNextEvents[0].travelers.size)

        testViewModel.clearTravelers.onNext(Unit)
        testInfoCompletedSubscriber.requestMore(LOTS_MORE)

        travelers = arrayListOf(getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
        testInfoCompletedSubscriber.requestMore(LOTS_MORE)

        testInfoCompletedSubscriber.assertReceivedOnNext(expectedResults)
    }

    @Test
    fun testRequiredInfoCompleted() {
        val testSubscriber = TestSubscriber<BaseCheckoutParams>()
        testViewModel.checkoutParams.subscribe(testSubscriber)

        val testInfoCompletedSubscriber = TestSubscriber<Boolean>()
        testViewModel.infoCompleted.subscribe(testInfoCompletedSubscriber)

        var travelers = arrayListOf(getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
        testViewModel.paymentCompleted.onNext(getBillingInfo())
        testViewModel.cvvCompleted.onNext("123")

        testSubscriber.requestMore(LOTS_MORE)
        testInfoCompletedSubscriber.requestMore(LOTS_MORE)

        assertEquals(false, testInfoCompletedSubscriber.onNextEvents[0])
        assertEquals(true, testInfoCompletedSubscriber.onNextEvents[1])
        assertEquals(1, testSubscriber.onNextEvents.size)
    }

    fun getBillingInfo(): BillingInfo {
        var info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111")
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"
        info.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf("123 street")
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        info.location = location

        return info
    }

    fun getTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "malcolm"
        traveler.lastName = "nguyen"
        traveler.gender = Traveler.Gender.MALE
        traveler.phoneNumber = "9163355329"
        traveler.birthDate = LocalDate.now().minusYears(18)
        return traveler
    }
}
