package com.expedia.vm.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.hotel.HotelResultsPricingStructureHeaderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelResultsPricingStructureHeaderViewModelTests {

    var sut: HotelResultsPricingStructureHeaderViewModel by Delegates.notNull()

    var hotelResultsCount: Int by Delegates.notNull<Int>()
    var priceType: HotelRate.UserPriceType by Delegates.notNull<HotelRate.UserPriceType>()
    val testObserverForResultsDescriptionLabel = TestSubscriber.create<String>()

    @Before
    fun before() {
        sut = HotelResultsPricingStructureHeaderViewModel(getContext(), null)
        givenUserPriceType(HotelRate.UserPriceType.UNKNOWN)
        givenHotelsResultsCount(-1)
    }

    @Test
    fun loading() {
        givenLoading()

        assertEquals("Searching hotels…", sut.resultsDescriptionHeaderObservable.value)
        assertEquals(false, sut.loyaltyAvailableObservable.value)
    }

    @Test
    fun rateForWholeStayNoResults() {
        assertExpectedText(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, 0, "No Results")
    }

    @Test
    fun rateForWholeStayOneResult() {
        assertExpectedText(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, 1, "Total price for stay • 1 Result")
    }

    @Test
    fun rateForWholeStayMultipleResults() {
        assertExpectedText(HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES, 3, "Total price for stay • 3 Results")
    }

    @Test
    fun rateForPerNightStayNoResults() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 0, "No Results")
    }

    @Test
    fun rateForPerNightStayOneResult() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 1, "Prices average per night • 1 Result")
    }

    @Test
    fun rateForPerNightStayMultipleResults() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 3, "Prices average per night • 3 Results")
    }

    @Test
    fun loyaltyPointsAppliedHeaderVisible() {
        assertExpectedText(HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES, 3, "Prices average per night • 3 Results", true)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackagesJapanHeaderShowTaxesAndFees() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.JAPAN)
        sut = HotelResultsPricingStructureHeaderViewModel(getContext(), R.string.package_hotel_results_includes_header_TEMPLATE)
        sut.resultsDescriptionLabelObservable.subscribe(testObserverForResultsDescriptionLabel)
        assertExpectedText(HotelRate.UserPriceType.UNKNOWN, 50, "50 Results", false)
        testObserverForResultsDescriptionLabel.assertValue("Total price roundtrip, per person • includes hotel and flights")
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackagesUKHeaderShowTaxesAndFees() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.UNITED_KINGDOM)
        sut = HotelResultsPricingStructureHeaderViewModel(getContext(), R.string.package_hotel_results_header_TEMPLATE)
        sut.resultsDescriptionLabelObservable.subscribe(testObserverForResultsDescriptionLabel)
        assertExpectedText(HotelRate.UserPriceType.UNKNOWN, 50, "50 Results", false)
        testObserverForResultsDescriptionLabel.assertValue("Total price roundtrip, per person • includes hotel and flights")
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackagesListHeaderWithBreadcrumbsEnabled() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.UNITED_KINGDOM)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        sut = HotelResultsPricingStructureHeaderViewModel(getContext(), R.string.package_hotel_results_header)
        sut.resultsDescriptionLabelObservable.subscribe(testObserverForResultsDescriptionLabel)
        assertExpectedText(HotelRate.UserPriceType.UNKNOWN, 50, "50 Results", false)
        testObserverForResultsDescriptionLabel.assertValue("Total price roundtrip, per person • includes hotel and flights")
        setPointOfSale(initialPOSID)
    }

    private fun assertExpectedText(userPriceType: HotelRate.UserPriceType, hotelResultCount: Int, expectedString: String, expectedLoyaltyHeaderVisibility: Boolean = false) {
        givenUserPriceType(userPriceType)
        givenHotelsResultsCount(hotelResultCount)
        setupResultsDeliveredObserver(expectedLoyaltyHeaderVisibility)

        assertEquals(expectedString, sut.resultsDescriptionHeaderObservable.value)
        assertEquals(expectedLoyaltyHeaderVisibility, sut.loyaltyAvailableObservable.value)
    }

    private fun givenLoading() {
        sut.loadingStartedObserver.onNext(Unit)
    }

    private fun givenUserPriceType(userPriceType: HotelRate.UserPriceType) {
        this.priceType = userPriceType
    }

    private fun givenHotelsResultsCount(hotelResultsCount: Int) {
        this.hotelResultsCount = hotelResultsCount
    }

    private fun setupResultsDeliveredObserver(loyaltyInformationAvailable: Boolean) {
        val hotelSearchResponse = HotelSearchResponse()
        hotelSearchResponse.userPriceType = this.priceType
        hotelSearchResponse.hotelList = ArrayList<Hotel>()
        if (loyaltyInformationAvailable) {
            hotelSearchResponse.hasLoyaltyInformation = true
        }
        while (this.hotelResultsCount > 0) {
            hotelSearchResponse.hotelList.add(Hotel())
            this.hotelResultsCount--
        }
        sut.resultsDeliveredObserver.onNext(hotelSearchResponse)
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application;
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(getContext(), "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }
}
