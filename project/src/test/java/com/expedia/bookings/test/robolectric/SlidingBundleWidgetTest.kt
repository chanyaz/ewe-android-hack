package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class) @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class SlidingBundleWidgetTest {
    private lateinit var packageFlightPresenter: PackageFlightPresenter
    private lateinit var packageHotelPresenter: PackageHotelPresenter
    private lateinit var activity: Activity
    lateinit var params: PackageSearchParams
    lateinit var flightResponse: BundleSearchResponse
    val context = RuntimeEnvironment.application

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

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
        mockPackageServiceRule.getMIDHotelResponse()
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
        mockPackageServiceRule.getMIDHotelResponse()
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
        params = mockPackageServiceRule.getPackageParams()
        mockPackageServiceRule.getMIDHotelResponse()
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
        assertEquals("Trip to LHR. $contentDescDates, 4 travelers", packageHotelPresenter.bundleSlidingWidget.bundlePriceWidget.contentDescription)
    }

    @Test
    fun testBundleWidgetViewsVisibilityEndBundleTransitionBackward() {
        mockPackageServiceRule.getMIDHotelResponse()
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
        mockPackageServiceRule.getMIDHotelResponse()
        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.openBundleOverview()

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundleWidgetTopShadow = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_shadow)

        assertEquals(View.VISIBLE, bundleTitle.visibility)
        assertEquals(View.VISIBLE, bundleSubtitle.visibility)
        assertEquals(View.GONE, bundleTotalPrice.visibility)
        assertEquals(View.GONE, bundleTotalIncludes.visibility)
        assertEquals(View.GONE, bundleTotalText.visibility)
        assertEquals(View.GONE, bundleWidgetTopShadow.visibility)
    }

    @Test
    fun testBundleWidgetViewsInCloseBundleOverviewState() {
        mockPackageServiceRule.getMIDHotelResponse()
        packageHotelPresenter = getHotelPresenter()
        packageHotelPresenter.bundleSlidingWidget.closeBundleOverview()

        val bundleTitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_title)
        val bundleSubtitle = packageHotelPresenter.findViewById<TextView>(R.id.bundle_subtitle)
        val bundleTotalPrice = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_price)
        val bundleTotalIncludes = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_includes_text)
        val bundleTotalText = packageHotelPresenter.findViewById<TextView>(R.id.bundle_total_text)
        val bundleWidgetTopShadow = packageHotelPresenter.findViewById<View>(R.id.bundle_price_widget_shadow)

        assertEquals(View.GONE, bundleTitle.visibility)
        assertEquals(View.GONE, bundleSubtitle.visibility)
        assertEquals(View.VISIBLE, bundleTotalPrice.visibility)
        assertEquals(View.VISIBLE, bundleTotalIncludes.visibility)
        assertEquals(View.VISIBLE, bundleTotalText.visibility)
        assertEquals(View.VISIBLE, bundleWidgetTopShadow.visibility)
    }

    @Test
    fun testBundlePriceWidgetStringForJPPoS() {
        RoboTestHelper.setPOS(PointOfSaleId.JAPAN)
        mockPackageServiceRule.getMIDHotelResponse()

        packageHotelPresenter = getHotelPresenter()
        assertEquals(StrUtils.bundleTotalWithTaxesString(context).toString(), packageHotelPresenter.bundleSlidingWidget.bundlePriceFooter.bundleTotalText.text.toString())
    }

    @Test
    fun testBundleWidgetViewsUpdatedOnOutboundFlightSelection() {
        val roomResponse = mockPackageServiceRule.getMIDRoomsResponse().getBundleRoomResponse()[0]
        addCurrentOfferToDB(roomResponse)
        Db.setPackageSelectedHotel(mockPackageServiceRule.getMIDHotelResponse().getHotels()[0], roomResponse)
        flightResponse = mockPackageServiceRule.getMIDFlightsResponse()
        flightResponse.setCurrentOfferPrice(flightResponse.getFlightLegs()[0].packageOfferModel.price)

        packageFlightPresenter = getFlightPresneter()
        packageFlightPresenter.detailsPresenter.vm.selectedFlightLegSubject.onNext(flightResponse.getFlightLegs()[0])

        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.travelInfoText.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightCardText.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightIcon.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightDetailsIcon.alpha, 1f)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.isEnabled, false)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundleOverViewWidget.outboundFlightWidget.flightDetailsIcon.isEnabled, false)

        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundlePriceFooter.bundleTotalPrice.text.toString(), Money(flightResponse.getFlightLegs()[0].packageOfferModel.price.packageTotalPrice.amount.toString(), flightResponse.getFlightLegs()[0].packageOfferModel.price.packageTotalPrice.currency).formattedMoney)
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundlePriceFooter.bundleSavings.text.toString(), Money(flightResponse.getFlightLegs()[0].packageOfferModel.price.tripSavings.amount.toString(), flightResponse.getFlightLegs()[0].packageOfferModel.price.tripSavings.currency).formattedMoney + " Saved")
        assertEquals(packageFlightPresenter.bundleSlidingWidget.bundlePriceWidget.contentDescription, "Bundle price is ${flightResponse.getFlightLegs()[0].packageOfferModel.price.pricePerPersonFormatted} per person. This price includes taxes, fees for both flights and hotel. Button to view bundle.")
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
