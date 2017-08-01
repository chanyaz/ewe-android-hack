package com.expedia.vm.test.rail

import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailCreditCardFeesViewModel
import org.junit.Test
import org.junit.runner.RunWith
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailCreditCardFeesViewModelTest {
    var testViewModel = RailCreditCardFeesViewModel()
    val vfopList = generateVFOPList()

    val expectedAmexNone = RailCreditCardFeesViewModel.CardFeesRow("AmericanExpress", "£15.99")
    val expectedAmexOverNight = RailCreditCardFeesViewModel.CardFeesRow("AmericanExpress", "£16.17")

    val expectedVisaNone = RailCreditCardFeesViewModel.CardFeesRow("Visa", "£9.60")
    val expectedVisaOvernight = RailCreditCardFeesViewModel.CardFeesRow("Visa", "£9.70")

    @Test
    fun testVFOPWithNoTokenDoesNothing() {
        val testSub = TestObserver<List<RailCreditCardFeesViewModel.CardFeesRow>>()
        testViewModel.cardFeesObservable.subscribe(testSub)

        testViewModel.validFormsOfPaymentSubject.onNext(vfopList)

        testSub.assertNoValues()
    }

    @Test
    fun testTokenWithNoVFOPDoesNothing() {
        val testSub = TestObserver<List<RailCreditCardFeesViewModel.CardFeesRow>>()
        testViewModel.cardFeesObservable.subscribe(testSub)

        testViewModel.ticketDeliveryOptionSubject.onNext(TicketDeliveryOption(
                RailCreateTripResponse.RailTicketDeliveryOptionToken.KIOSK_NONE))

        testSub.assertNoValues()
    }

    @Test
    fun testSingleToken() {
        val testSub = TestObserver<List<RailCreditCardFeesViewModel.CardFeesRow>>()
        testViewModel.cardFeesObservable.subscribe(testSub)

        testViewModel.validFormsOfPaymentSubject.onNext(vfopList)
        testViewModel.ticketDeliveryOptionSubject.onNext(TicketDeliveryOption(
                RailCreateTripResponse.RailTicketDeliveryOptionToken.KIOSK_NONE))

        val output = testSub.onNextEvents[0]

        assertEquals(expectedAmexNone.cardName, output[0].cardName)
        assertEquals(expectedAmexNone.fee, output[0].fee)

        assertEquals(expectedVisaNone.cardName, output[1].cardName)
        assertEquals(expectedVisaNone.fee, output[1].fee)
    }

    @Test
    fun testMultipleTokensTriggered() {
        val testSub = TestObserver<List<RailCreditCardFeesViewModel.CardFeesRow>>()
        testViewModel.cardFeesObservable.subscribe(testSub)

        testViewModel.validFormsOfPaymentSubject.onNext(vfopList)
        testViewModel.ticketDeliveryOptionSubject.onNext(TicketDeliveryOption(
                RailCreateTripResponse.RailTicketDeliveryOptionToken.KIOSK_NONE))
        testViewModel.ticketDeliveryOptionSubject.onNext(TicketDeliveryOption(
                RailCreateTripResponse.RailTicketDeliveryOptionToken.SEND_BY_OVERNIGHT_POST_UK))

        val output = testSub.onNextEvents[1]

        assertEquals(expectedAmexOverNight.cardName, output[0].cardName)
        assertEquals(expectedAmexOverNight.fee, output[0].fee)

        assertEquals(expectedVisaOvernight.cardName, output[1].cardName)
        assertEquals(expectedVisaOvernight.fee, output[1].fee)
    }


    private fun generateVFOPList() : List<RailCreateTripResponse.RailValidFormOfPayment> {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_vfop.json")
        val createTripShell = resourceReader.constructUsingGson(RailCreateTripResponse::class.java)

        return createTripShell.validFormsOfPayment
    }
}
