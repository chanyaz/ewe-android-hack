package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.TextView
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class) @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class SlidingBundleWidgetTest {
    lateinit private var packageFlightPresenter: PackageFlightPresenter
    lateinit private var packageHotelPresenter: PackageHotelPresenter
    lateinit private var activity: Activity
    lateinit var params: PackageSearchParams
    lateinit var flightResponse: BundleSearchResponse
    lateinit var hotelResponse: BundleSearchResponse
    lateinit var roomResponse: PackageOffersResponse
    val context = RuntimeEnvironment.application

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    var hotelObserver = TestSubscriber<BundleSearchResponse>()
    var flightObserver = TestSubscriber<BundleSearchResponse>()
    var offerObserver = TestSubscriber<PackageOffersResponse>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
    }

    @Test
    fun testBundleWidgetViewsVisibilityStartBundleTransitionForward() {
        setPackageResponseHotels()
        packageHotelPresenter = getHotelPresenter()

        packageHotelPresenter.bundleSlidingWidget.bundlePriceWidget.viewTreeObserver.dispatchOnGlobalLayout()
        packageHotelPresenter.bundleSlidingWidget.startBundleTransition(true)

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundleWidgetTopShadow = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_shadow)

        assertEquals(packageHotelPresenter.bundleSlidingWidget.isMoving, true)
        assertEquals(View.VISIBLE, bundleTitle.visibility)
        assertEquals(View.VISIBLE, bundleSubtitle.visibility)
        assertEquals(View.VISIBLE, bundleTotalPrice.visibility)
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals(View.GONE, bundleWidgetTopShadow.visibility)
    }

    @Test
    fun testBundleWidgetViewsVisibilityStartBundleTransitionBackward() {
        setPackageResponseHotels()
        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.startBundleTransition(false)

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundleWidgetTopShadow = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_shadow)

        assertEquals(packageHotelPresenter.bundleSlidingWidget.isMoving, true)
        assertEquals(View.VISIBLE, bundleTitle.visibility)
        assertEquals(View.VISIBLE, bundleSubtitle.visibility)
        assertEquals(View.VISIBLE, bundleTotalPrice.visibility)
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals(View.VISIBLE, bundleWidgetTopShadow.visibility)
    }

    @Test
    fun testBundleWidgetViewsVisibilitySEndBundleTransitionForward() {
        setPackageResponseHotels()

        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.finalizeBundleTransition(true)

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val contentDescDates = LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate) + " to " + LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!)
        assertEquals(View.VISIBLE, bundleTitle.visibility)
        assertEquals(View.VISIBLE, bundleSubtitle.visibility)
        assertEquals(View.GONE, bundleTotalPrice.visibility)
        assertEquals(View.GONE, bundleTotalIncludes.visibility)
        assertEquals(View.GONE, bundleTotalText.visibility)
        assertEquals(packageHotelPresenter.bundleSlidingWidget.isMoving, false)
        assertEquals(packageHotelPresenter.bundleSlidingWidget.translationY, Ui.getStatusBarHeight(context).toFloat())
        assertEquals(packageHotelPresenter.bundleSlidingWidget.bundlePriceWidget.contentDescription, "Trip to London. $contentDescDates, 4 travelers")
    }

    @Test
    fun testBundleWidgetViewsVisibilityEndBundleTransitionBackward() {
        setPackageResponseHotels()

        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.finalizeBundleTransition(false)

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundlePriceWidgetContainer = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_container)

        assertEquals(View.GONE, bundleTitle.visibility)
        assertEquals(View.GONE, bundleSubtitle.visibility)
        assertEquals(View.VISIBLE, bundleTotalPrice.visibility)
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals((packageHotelPresenter.bundleSlidingWidget.height - bundlePriceWidgetContainer.height).toFloat(), packageHotelPresenter.bundleSlidingWidget.translationY)
        assertEquals(packageHotelPresenter.bundleSlidingWidget.isMoving, false)
        assertEquals(packageHotelPresenter.bundleSlidingWidget.bundlePriceWidget.contentDescription, "")
    }

    @Test
    fun testBundleWidgetViewsInOpenBundleOverviewState() {
        setPackageResponseHotels()

        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.openBundleOverview()

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundleWidgetTopShadow = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_shadow)

        assertEquals(packageHotelPresenter.bundleSlidingWidget.isMoving, true)
        assertEquals(View.VISIBLE, bundleTitle.visibility)
        assertEquals(View.VISIBLE, bundleSubtitle.visibility)
        assertEquals(View.VISIBLE, bundleTotalPrice.visibility)
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals(View.GONE, bundleWidgetTopShadow.visibility)
    }

    @Test
    fun testBundleWidgetViewsInCloseBundleOverviewState() {
        setPackageResponseHotels()

        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.closeBundleOverview()

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundleWidgetTopShadow = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_shadow)

        assertEquals(packageHotelPresenter.bundleSlidingWidget.isMoving, true)
        assertEquals(View.VISIBLE, bundleTitle.visibility)
        assertEquals(View.VISIBLE, bundleSubtitle.visibility)
        assertEquals(View.VISIBLE, bundleTotalPrice.visibility)
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals(View.VISIBLE, bundleWidgetTopShadow.visibility)
    }

    @Test
    fun testBundlePriceWidgetStringForJPPoS() {
        RoboTestHelper.setPOS(PointOfSaleId.JAPAN)
        setPackageResponseHotels()

        packageHotelPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter
        assertEquals(StrUtils.bundleTotalWithTaxesString(context).toString(), packageHotelPresenter.bundleSlidingWidget.bundlePriceFooter.bundleTotalText.text.toString())
    }

    @Test
    fun testBundleWidgetViewsUpdatedOnOutboundFlightSelection() {
        setPackageResponseOutboundFlight()

        packageFlightPresenter = getFlightPresneter()
        packageFlightPresenter.overviewPresenter.vm.selectedFlightLegSubject.onNext(flightResponse.getFlightLegs()[0])

        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.travelInfoText.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightCardText.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightIcon.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightDetailsIcon.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.isEnabled, false)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightDetailsIcon.isEnabled, false)

        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundlePriceFooter.bundleTotalPrice.text.toString(), Money(flightResponse.getFlightLegs()[0].packageOfferModel.price.packageTotalPrice.amount.toString(), flightResponse.getFlightLegs()[0].packageOfferModel.price.packageTotalPrice.currency).formattedMoney)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundlePriceFooter.bundleSavings.text.toString(), Money(flightResponse.getFlightLegs()[0].packageOfferModel.price.tripSavings.amount.toString(), flightResponse.getFlightLegs()[0].packageOfferModel.price.tripSavings.currency).formattedMoney + " Saved")
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundlePriceWidget.contentDescription, "Bundle price is $2,105.95 per person. This price includes taxes, fees for both flights and hotel. Button to view bundle.")
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "London"
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = "happy"
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }

    private fun buildPackagesSearchParams() {
        params = PackageSearchParams.Builder(26, 329)
                .infantSeatingInLap(true)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .children(listOf(16, 10, 1))
                .build() as PackageSearchParams
    }

    private fun setPackageResponseHotels() {
        buildPackagesSearchParams()
        Db.setPackageParams(params)
        searchHotels()
        hotelResponse = hotelObserver.onNextEvents.get(0)
        Db.setPackageResponse(hotelResponse)
    }

    private fun setPackageResponseOutboundFlight() {
        buildPackagesSearchParams()
        searchHotels()
        hotelResponse = hotelObserver.onNextEvents.get(0)
        Db.setPackageResponse(hotelResponse)

        params.packagePIID = hotelResponse.getHotels()[0].hotelId
        params.currentFlights = arrayOf("legs")
        params.ratePlanCode = "flight_outbound_happy"
        params.roomTypeCode = "flight_outbound_happy"
        searchRooms()
        roomResponse = offerObserver.onNextEvents[0]
        addCurrentOfferToDB(roomResponse.getBundleRoomResponse()[0])
        Db.setPackageSelectedHotel(hotelResponse.getHotels().get(0), roomResponse.getBundleRoomResponse()[0])

        params.packagePIID = "happy_outbound_flight"
        params.numberOfRooms = "1"
        params.searchProduct = Constants.PRODUCT_FLIGHT
        params.currentFlights = arrayOf("legs")
        params.isOutboundSearch(true)
        Db.setPackageParams(params)
        searchFLights()
        flightResponse  = flightObserver.onNextEvents.get(0)
        flightResponse.setCurrentOfferPrice(flightObserver.onNextEvents[0].getFlightLegs()[0].packageOfferModel.price)
        Db.setPackageResponse(flightResponse)
    }

    private fun searchRooms() {
        packageServiceRule.services!!.hotelOffer(params.packagePIID!!, params.startDate.toString(), params.endDate.toString(), params.ratePlanCode!!, params.roomTypeCode, params.adults, params.childAges!![0].toInt()).subscribe(offerObserver)
        offerObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun searchHotels() {
        packageServiceRule.services!!.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(hotelObserver)
        hotelObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun searchFLights() {
        packageServiceRule.services!!.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(flightObserver)
        flightObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun addCurrentOfferToDB(offer: HotelOffersResponse.HotelRoomResponse) {
        var response = Db.getPackageResponse()
        val price = PackageOfferModel.PackagePrice()
        price.packageTotalPrice = offer.rateInfo.chargeableRateInfo.packageTotalPrice
        price.tripSavings = offer.rateInfo.chargeableRateInfo.packageSavings
        price.pricePerPerson = offer.rateInfo.chargeableRateInfo.packagePricePerPerson
        response.setCurrentOfferPrice(price)
    }

    private fun getHotelPresenter(): PackageHotelPresenter {
        return LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter, null)
                as PackageHotelPresenter
    }

    private fun getFlightPresneter(): PackageFlightPresenter {
        return LayoutInflater.from(activity).inflate(R.layout.package_flight_activity, null, false)
                as PackageFlightPresenter
    }
}

