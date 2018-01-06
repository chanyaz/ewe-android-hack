package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.util.Optional
import com.expedia.vm.packages.BundleOverviewViewModel
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class PackagePriceChangeTest {


    private var checkout: PackageCheckoutPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private var overview: PackageOverviewPresenter by Delegates.notNull()

    lateinit var travelerValidator: TravelerValidator
    private val priceChangeAlertSubscriber = TestSubscriber<TripResponse>()
    private val showPriceChangeAlertSubscriber = TestSubscriber<Boolean>()

    private val lowPackageTotal = Money(900, "USD")
    private val highPackageTotal = Money(1000, "USD")
    private val lowBundleTotal = Money(950, "USD")
    private val highBundleTotal = Money(1150, "USD")

    val server = MockWebServer()
    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get


    @Before fun before() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        travelerValidator = Ui.getApplication(RuntimeEnvironment.application).travelerComponent().travelerValidator()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        setUpPackageDb()
        travelerValidator.updateForNewSearch(Db.sharedInstance.packageParams)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        overview = activity.findViewById<View>(R.id.package_overview_presenter) as PackageOverviewPresenter
        checkout = overview.getCheckoutPresenter()
        overview.bundleWidget.viewModel = BundleOverviewViewModel(activity.applicationContext, packageServiceRule.services!!)
        overview.bundleWidget.viewModel.hotelParamsObservable.onNext(getPackageSearchParams(1, emptyList(), false))
        overview.resetAndShowTotalPriceWidget()
        checkout.getCreateTripViewModel().priceChangeAlertPriceObservable.map { it.value }.subscribe(priceChangeAlertSubscriber)
        checkout.getCreateTripViewModel().showPriceChangeAlertObservable.subscribe(showPriceChangeAlertSubscriber)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCreateTripPriceChangeWithBundleTotalsPriceDecreased() {
        checkout.getCreateTripViewModel().createTripResponseObservable
                .onNext(Optional(getDummyPackageCreateTripPriceChangeResponse(lowPackageTotal, highPackageTotal, lowBundleTotal, highBundleTotal)))

        priceChangeAlertSubscriber.assertValueCount(1)
        showPriceChangeAlertSubscriber.assertValue(true)
        assertPriceChangeWidgetIsCorrect(lowBundleTotal, highBundleTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCreateTripPriceChangeIncreasedWithoutCardFee() {
        val newBundleTotal = Money(1000, "USD")
        val newPackageTotal = Money(900, "USD")
        val oldBundleTotal = Money(950,"USD")
        val oldPackageTotal = Money(850, "USD")

        checkout.getCreateTripViewModel().createTripResponseObservable
                .onNext(Optional(getDummyPackageCreateTripPriceChangeResponse(newPackageTotal, oldPackageTotal, newBundleTotal, oldBundleTotal, false)))

        priceChangeAlertSubscriber.assertValueCount(1)
        showPriceChangeAlertSubscriber.assertValue(true)
        assertPriceChangeWidgetIsCorrect(newBundleTotal, oldBundleTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCreateTripPriceChangeDecreasedWithoutCardFee() {
        val newBundleTotal = Money(1000, "USD")
        val newPackageTotal = Money(900, "USD")
        val oldBundleTotal = Money(1050,"USD")
        val oldPackageTotal = Money(1000, "USD")

        checkout.getCreateTripViewModel().createTripResponseObservable
                .onNext(Optional(getDummyPackageCreateTripPriceChangeResponse(newPackageTotal, oldPackageTotal, newBundleTotal, oldBundleTotal, false)))

        priceChangeAlertSubscriber.assertValueCount(1)
        showPriceChangeAlertSubscriber.assertValue(true)
        assertPriceChangeWidgetIsCorrect(newBundleTotal, oldBundleTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCreateTripPriceChangeWithoutBundleTotalsPriceIncreased() {
        checkout.getCreateTripViewModel().createTripResponseObservable
                .onNext(Optional(getDummyPackageCreateTripPriceChangeResponse(highPackageTotal, lowPackageTotal)))

        priceChangeAlertSubscriber.assertValueCount(1)
        showPriceChangeAlertSubscriber.assertValue(true)
        assertPriceChangeWidgetIsCorrect(highPackageTotal, lowPackageTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCheckoutPriceChangeWithBundleTotalsPriceIncreased(){
        checkout.getCheckoutViewModel().checkoutPriceChangeObservable
                .onNext(getDummyPackageCreateTripPriceChangeResponse(highPackageTotal, lowPackageTotal, highBundleTotal, lowBundleTotal))

        assertTrue(showPriceChangeAlertSubscriber.onNextEvents.last())
        assertPriceChangeWidgetIsCorrect(highBundleTotal, lowBundleTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCheckoutPriceChangeWithBundleTotalsPriceDecreased(){
        checkout.getCheckoutViewModel().checkoutPriceChangeObservable
                .onNext(getDummyPackageCreateTripPriceChangeResponse(lowPackageTotal, highPackageTotal, lowBundleTotal, highBundleTotal))

        assertTrue(showPriceChangeAlertSubscriber.onNextEvents.last())
        assertPriceChangeWidgetIsCorrect(lowBundleTotal, highBundleTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCheckoutPriceChangeWithoutBundleTotalsPriceDecreased(){
        checkout.getCheckoutViewModel().checkoutPriceChangeObservable
                .onNext(getDummyPackageCreateTripPriceChangeResponse(lowPackageTotal, highPackageTotal))

        assertTrue(showPriceChangeAlertSubscriber.onNextEvents.last())
        assertPriceChangeWidgetIsCorrect(lowPackageTotal, highPackageTotal)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testCheckoutPriceChangeWithoutBundleTotalsPriceIncreased(){
        checkout.getCheckoutViewModel().checkoutPriceChangeObservable
                .onNext(getDummyPackageCreateTripPriceChangeResponse(highPackageTotal, lowPackageTotal))

        assertTrue(showPriceChangeAlertSubscriber.onNextEvents.last())
        assertPriceChangeWidgetIsCorrect(highPackageTotal, lowPackageTotal)
    }

    private fun assertPriceChangeWidgetIsCorrect(newPrice: Money, oldPrice: Money) {
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val okButton = alertDialog.findViewById<View>(android.R.id.button1) as Button
        val errorMessage = alertDialog.findViewById<View>(android.R.id.message) as android.widget.TextView
        assertEquals(true, alertDialog.isShowing)
        assertTrue(errorMessage.text.contains("The price of your trip has changed from " + oldPrice.formattedMoneyFromAmountAndCurrencyCode + " to " +
                newPrice.formattedMoneyFromAmountAndCurrencyCode + ". Rates can change frequently. Book now to lock in this price."))
        assertEquals("OK", okButton.text )
    }

    private fun getDummyPackageCreateTripPriceChangeResponse(newTotal: Money, oldTotal: Money, newBundleTotal: Money ?= null, oldBundleTotal: Money ?= null, withCardFee: Boolean = true): PackageCreateTripResponse? {
        val trip = PackageCreateTripResponse()
        val packageDetails = PackageCreateTripResponse.PackageDetails()
        val oldPackageDetails = PackageCreateTripResponse.PackageDetails()
        oldPackageDetails.pricing = PackageCreateTripResponse.Pricing()
        oldPackageDetails.pricing.bundleTotal = oldBundleTotal
        oldPackageDetails.pricing.packageTotal = oldTotal

        val pricing = PackageCreateTripResponse.Pricing()
        pricing.bundleTotal = newBundleTotal
        pricing.bundleTotal?.formattedPrice = newBundleTotal?.formattedMoneyFromAmountAndCurrencyCode
        pricing.packageTotal = newTotal
        pricing.savings = Money(0, "USD")
        pricing.savings.formattedPrice = pricing.savings.formattedMoneyFromAmountAndCurrencyCode
        pricing.totalTaxesAndFees = newBundleTotal ?: newTotal
        pricing.totalTaxesAndFees.formattedPrice = pricing.totalTaxesAndFees.formattedMoneyFromAmountAndCurrencyCode
        pricing.basePrice = newTotal
        pricing.basePrice.formattedPrice = pricing.basePrice.formattedMoneyFromAmountAndCurrencyCode

        packageDetails.pricing = pricing
        packageDetails.flight = setupFlightProduct()
        packageDetails.tripId = "1234456547656474"
        packageDetails.hotel = setupHotelProductResponse()

        trip.packageDetails = packageDetails
        trip.oldPackageDetails = oldPackageDetails
        if (withCardFee) {
            trip.totalPriceIncludingFees = newBundleTotal ?: newTotal
        }
        return trip
    }

    private fun setUpPackageDb() {
        val hotel = Hotel()
        hotel.packageOfferModel = PackageOfferModel()
        Db.setPackageSelectedHotel(hotel, HotelOffersResponse.HotelRoomResponse())

        val outboundFlight = FlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)

        setPackageSearchParams(1, emptyList(), false)
    }

    private fun setPackageSearchParams(adults: Int, children: List<Int>, infantsInLap: Boolean) {
        Db.setPackageParams(getPackageSearchParams(adults, children, infantsInLap))
    }

    private fun getPackageSearchParams(adults: Int, children: List<Int>, infantsInLap: Boolean): PackageSearchParams {
        val origin = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "SFO"
        hierarchyInfo.airport = airport
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "San Francisco"
        regionNames.shortName = "SFO"
        regionNames.fullName = "SFO - San Francisco"

        origin.hierarchyInfo = hierarchyInfo
        val destination = SuggestionV4()
        destination.hierarchyInfo = hierarchyInfo
        destination.regionNames = regionNames
        origin.regionNames = regionNames

        return PackageSearchParams.Builder(12, 329).infantSeatingInLap(infantsInLap)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(origin)
                .destination(destination)
                .adults(adults)
                .children(children).build() as PackageSearchParams
    }

    private fun setupFlightProduct() : PackageCreateTripResponse.FlightProduct {
        val flightProduct = PackageCreateTripResponse.FlightProduct()
        val flightTripDetails = FlightTripDetails()
        val flightOffer = FlightTripDetails.FlightOffer()
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = lowPackageTotal
        flightTripDetails.offer = flightOffer
        val legs = ArrayList<FlightLeg>()
        val flightLeg = FlightLeg()
        flightLeg.carrierCode = "SFO"
        legs.add(flightLeg)
        flightTripDetails.legs = legs
        flightProduct.details = flightTripDetails
        Db.setPackageSelectedOutboundFlight(legs.first())
        return flightProduct
    }

    private fun setupHotelProductResponse() : HotelCreateTripResponse.HotelProductResponse {
        val hotel = HotelCreateTripResponse.HotelProductResponse()
        hotel.largeThumbnailUrl = "/testurl"
        hotel.hotelCity = "New York"
        hotel.hotelStateProvince = "NY"
        hotel.hotelCountry = "USA"
        hotel.checkInDate = "1989-09-06"
        hotel.checkOutDate = "2021-09-06"
        hotel.numberOfRooms = "1"
        hotel.numberOfNights = "1"
        hotel.hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        val dbHotel = Hotel()
        dbHotel.hotelId = "forOmnitureStability"
        Db.setPackageSelectedHotel(dbHotel, hotel.hotelRoomResponse)
        return hotel
    }
}
