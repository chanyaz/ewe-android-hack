package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.PaymentViewModel
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
            override fun resetCardFees() {
                // do nothing
            }

            override fun useCardFeeService(): Boolean {
                return false
            }

            override fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?) { }
            override fun injectComponents() {
                paymentViewModel = PaymentViewModel(activity)
            }
            override fun getTripId(): String {
                return createTripResponseObservable.value!!.tripId
            }
        }
        testViewModel.builder.tripId("4321")
        testViewModel.builder.expectedTotalFare("42")
        testViewModel.builder.expectedFareCurrencyCode("USD")
    }

    @Test
    fun testTravelersCleared() {
        val testSubscriber = TestSubscriber<BaseCheckoutParams>()
        val expectedResults = arrayListOf(false, true, true, true, false, true)
        testViewModel.checkoutParams.subscribe(testSubscriber)

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

        travelers = arrayListOf(getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
    }

    @Test
    fun testStreetAddress() {
        val testSubscriber = TestSubscriber<BaseCheckoutParams>()
        testViewModel.checkoutParams.subscribe(testSubscriber)
        testViewModel.travelerCompleted.onNext(arrayListOf(getTraveler()))
        testViewModel.paymentCompleted.onNext(getBillingInfo())
        testViewModel.cvvCompleted.onNext("123")

        testSubscriber.requestMore(LOTS_MORE)
        assertEquals("123 street", testSubscriber.onNextEvents[0].toQueryMap().get("streetAddress"))
        assertEquals("apt 69", testSubscriber.onNextEvents[0].toQueryMap().get("streetAddress2"))
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
        location.streetAddress = arrayListOf("123 street", "apt 69")
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
