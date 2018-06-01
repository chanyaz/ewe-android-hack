package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.vm.FlightResultsViewModel
import io.reactivex.disposables.CompositeDisposable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightResultsViewModelTest {
    private var context: Context = RuntimeEnvironment.application
    private lateinit var sut: FlightResultsViewModel
    val FLIGHT_LEG_ID = "ab64aefca28e772ca024d4a00e6ae131"

    @Before
    fun setup() {
        sut = FlightResultsViewModel(context)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentObserver() {
        val richContentObserver = TestObserver<Map<String, RichContent>>()
        sut.richContentStream.subscribe(richContentObserver)
        sut.makeRichContentObserver().onNext(getRichContentResponse())
        richContentObserver.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentMap() {
        val richContentMap = sut.getRichContentMap(getRichContentList())
        assertEquals(FLIGHT_LEG_ID, richContentMap.keys.first())

        val richContent = richContentMap.values.first()
        assertEquals(FLIGHT_LEG_ID, richContent.legId)
        assertEquals(7.9F, richContent.score)
        assertEquals(RichContentUtils.ScoreExpression.VERY_GOOD.name, richContent.scoreExpression)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testShowRichContentGuideForFirstSession() {
        //When first time
        val editor = sut.sharedPref.edit()
        editor.putInt("counter", 1)
        editor.apply()
        assertEquals(true, sut.showRichContentGuide())
        assertEquals(false, sut.isRichContentGuideDisplayed)
        //In the same session, rich content should not be displayed
        sut.isRichContentGuideDisplayed = true
        editor.putInt("counter", 3)
        editor.apply()
        assertEquals(false, sut.showRichContentGuide())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentGuideNotDisplayed() {
        //When first time
        val editor = sut.sharedPref.edit()
        editor.putInt("counter", 2)
        editor.apply()
        assertEquals(false, sut.showRichContentGuide())
        sut.isRichContentGuideDisplayed = true
        assertEquals(false, sut.showRichContentGuide())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUpdateRichContentCounter() {
        sut.updateRichContentCounter()
        val counter = sut.sharedPref.getInt("counter", 1)
        assertEquals(2, counter)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testAbortRichContentCallObservable() {
        sut.richContentInboundSubscription = CompositeDisposable()
        sut.richContentOutboundSubscription = CompositeDisposable()
        assertEquals(false, sut.richContentInboundSubscription!!.isDisposed)
        assertEquals(false, sut.richContentOutboundSubscription!!.isDisposed)
        sut.abortRichContentCallObservable.onNext(Unit)
        assertEquals(true, sut.richContentInboundSubscription!!.isDisposed)
        assertEquals(true, sut.richContentOutboundSubscription!!.isDisposed)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentGuideDisplayedOnOutbound() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        val testSubscriber = TestObserver<Unit>()
        sut = FlightResultsViewModel(context)
        sut.richContentGuide.subscribe(testSubscriber)
        sut.isOutboundResults.onNext(true)
        val flightLeg = createFakeFlightLeg()
        sut.flightResultsObservable.onNext(listOf(flightLeg))
        testSubscriber.assertValueCount(1)
        val counter = sut.sharedPref.getInt("counter", 1)
        assertEquals(2, counter)
        assertEquals(true, sut.isRichContentGuideDisplayed)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentGuideNotDisplayedOnInbound() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        val testSubscriber = TestObserver<Unit>()
        sut = FlightResultsViewModel(context)
        sut.richContentGuide.subscribe(testSubscriber)
        sut.isOutboundResults.onNext(false)
        val flightLeg = createFakeFlightLeg()
        sut.flightResultsObservable.onNext(listOf(flightLeg))
        testSubscriber.assertValueCount(0)
        val counter = sut.sharedPref.getInt("counter", 1)
        assertEquals(1, counter)
        assertEquals(false, sut.isRichContentGuideDisplayed)
    }

    private fun getRichContentResponse(): RichContentResponse {
        val richContentResponse = RichContentResponse()
        richContentResponse.richContentList = getRichContentList()
        return richContentResponse
    }

    private fun getRichContentList(): List<RichContent> {
        return listOf(getRichContent())
    }

    private fun getRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = FLIGHT_LEG_ID
        richContent.score = 7.9F
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        return richContent
    }

    private fun createFakeFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        val airline = Airline("United Airlines", "")

        flightLeg.legId = FLIGHT_LEG_ID
        flightLeg.naturalKey = ""
        flightLeg.airlines = listOf(airline)
        flightLeg.durationHour = 13
        flightLeg.durationMinute = 59
        flightLeg.stopCount = 1
        flightLeg.departureDateTimeISO = "2016-03-09T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-03-10T12:20:00.000-07:00"
        flightLeg.elapsedDays = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("1200.90", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal("1200.90")

        return flightLeg
    }
}
