package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.TravelerTestUtils
import com.expedia.vm.AbstractCheckoutViewModel
import com.expedia.vm.PaymentViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class AbstractCheckoutViewModelTest {
    var testViewModel: AbstractCheckoutViewModel by Delegates.notNull()
    var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        testViewModel = object : AbstractCheckoutViewModel(activity) {
            override fun injectComponents() {
                paymentViewModel = PaymentViewModel(activity)
            }
            override fun getTripId(): String = createTripResponseObservable.value.value!!.tripId
        }
        testViewModel.builder.tripId("4321")
        testViewModel.builder.expectedTotalFare("42")
        testViewModel.builder.expectedFareCurrencyCode("USD")
    }

    @Test
    fun testTravelersCleared() {
        val testSubscriber = TestObserver<BaseCheckoutParams>()

        testViewModel.checkoutParams.subscribe(testSubscriber)

        var travelers = arrayListOf(TravelerTestUtils.getTraveler(), TravelerTestUtils.getTraveler(), TravelerTestUtils.getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))
        testViewModel.cvvCompleted.onNext("123")

        assertEquals(3, testSubscriber.values()[0].travelers.size)

        travelers = arrayListOf(TravelerTestUtils.getTraveler(), TravelerTestUtils.getTraveler(), TravelerTestUtils.getTraveler(), TravelerTestUtils.getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))
        testViewModel.cvvCompleted.onNext("123")

        assertEquals(4, testSubscriber.values()[0].travelers.size)

        testViewModel.clearTravelers.onNext(Unit)

        travelers = arrayListOf(TravelerTestUtils.getTraveler())
        testViewModel.travelerCompleted.onNext(travelers)
    }

    @Test
    fun testStreetAddress() {
        val testSubscriber = TestObserver<BaseCheckoutParams>()
        testViewModel.checkoutParams.subscribe(testSubscriber)
        testViewModel.travelerCompleted.onNext(arrayListOf(TravelerTestUtils.getTraveler()))
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))
        testViewModel.cvvCompleted.onNext("123")

        assertEquals("123 street", testSubscriber.values()[0].toQueryMap()["streetAddress"])
        assertEquals("apt 69", testSubscriber.values()[0].toQueryMap()["streetAddress2"])
    }

    @Test
    fun testInvalidBillingInfo() {
        testViewModel.travelerCompleted.onNext(arrayListOf(TravelerTestUtils.getTraveler()))
        val incompleteBillingInfo = BillingDetailsTestUtils.getBillingInfo(activity)
        incompleteBillingInfo.value!!.location.countryCode = null
        testViewModel.paymentCompleted.onNext(incompleteBillingInfo)

        assertEquals(false, testViewModel.builder.hasValidCheckoutParams())
    }

    @Test
    fun testValidSavedCard() {
        testViewModel.travelerCompleted.onNext(arrayListOf(TravelerTestUtils.getTraveler()))
        val savedInfo = BillingDetailsTestUtils.getBillingInfo(activity)
        savedInfo.value!!.storedCard = getStoredCard()
        testViewModel.paymentCompleted.onNext(savedInfo)

        assertEquals(true, testViewModel.builder.hasValidCheckoutParams())
    }

    @Test
    fun testInvalidSavedCard() {
        testViewModel.travelerCompleted.onNext(arrayListOf(TravelerTestUtils.getTraveler()))
        val savedInfo = BillingDetailsTestUtils.getBillingInfo(activity)
        savedInfo.value!!.storedCard = getStoredCard()
        savedInfo.value!!.storedCard.isExpired = true
        testViewModel.paymentCompleted.onNext(savedInfo)

        assertEquals(false, testViewModel.builder.hasValidCheckoutParams())
    }

    @Test
    fun testInvalidTravelerInfo() {
        val invalidTraveler = TravelerTestUtils.getTraveler()
        invalidTraveler.email = null
        testViewModel.travelerCompleted.onNext(arrayListOf(invalidTraveler))
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))

        assertEquals(false, testViewModel.builder.hasValidCheckoutParams())
    }

    @Test
    fun testInvalidMultipleTravelers() {
        val firstTraveler = TravelerTestUtils.getTraveler()
        val secondTraveler = TravelerTestUtils.getTraveler()
        secondTraveler.gender = Traveler.Gender.GENDER
        testViewModel.travelerCompleted.onNext(arrayListOf(firstTraveler, secondTraveler))
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))

        assertEquals(false, testViewModel.builder.hasValidCheckoutParams())
    }

    @Test
    fun testValidMultipleTravelers() {
        val firstTraveler = TravelerTestUtils.getTraveler()
        val secondTraveler = getSecondaryTraveler()
        val thirdTraveler = getSecondaryTraveler()

        testViewModel.travelerCompleted.onNext(arrayListOf(firstTraveler, secondTraveler, thirdTraveler))
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))

        assertEquals(true, testViewModel.builder.hasValidCheckoutParams())
    }

    @Test
    fun testValidCheckoutParams() {
        testViewModel.travelerCompleted.onNext(arrayListOf(TravelerTestUtils.getTraveler()))
        testViewModel.paymentCompleted.onNext(BillingDetailsTestUtils.getBillingInfo(activity))

        assertEquals(true, testViewModel.builder.hasValidCheckoutParams())
    }

    private fun getSecondaryTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "test"
        traveler.lastName = "traveler"
        traveler.gender = Traveler.Gender.MALE
        traveler.birthDate = LocalDate.now().minusYears(18)
        return traveler
    }

    private fun getStoredCard(): StoredCreditCard {
        val card = StoredCreditCard()
        card.id = "12345"
        card.cardNumber = "4111111111111111"
        card.type = PaymentType.CARD_VISA
        card.description = "Visa 4111"
        card.isExpired = false
        card.nameOnCard = "test card"
        return card
    }
}
