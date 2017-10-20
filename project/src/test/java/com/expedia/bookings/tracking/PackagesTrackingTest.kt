package com.expedia.bookings.tracking

import android.content.Context
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.BaseFlightFilterViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

//TODO : Add corresponding test case for any new method in PackageTracking
@RunWith(RobolectricRunner::class)
class PackagesTrackingTest {

    private lateinit var sut: PackagesTracking
    private lateinit var context: Context
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        sut = PackagesTracking()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelMapLoad() {
        sut.trackHotelMapLoad()
        val controlEvar = mapOf(18 to "App.Package.Hotels.Search.Map")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutSelectPaymentClick() {
        sut.trackCheckoutSelectPaymentClick()
        val controlEvar = mapOf(18 to "App.Package.Checkout.Payment.Select")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelReviewPageLoad() {
        sut.trackHotelReviewPageLoad()
        val controlEvar = mapOf(18 to "App.Package.Reviews")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelResortFeeInfoClick() {
        sut.trackHotelResortFeeInfoClick()
        val controlEvar = mapOf(18 to "App.Package.ResortFeeInfo")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelRenovationInfoClick() {
        sut.trackHotelRenovationInfoClick()
        val controlEvar = mapOf(18 to "App.Package.RenovationInfo")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackViewBundlePageLoad() {
        sut.trackViewBundlePageLoad()
        val controlEvar = mapOf(18 to "App.Package.BundleView")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelDetailMapViewClick() {
        sut.trackHotelDetailMapViewClick()
        val controlEvar = mapOf(18 to "App.Package.Infosite.Map")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackDestinationSearchInit() {
        sut.trackDestinationSearchInit()
        val controlEvar = mapOf(18 to "App.Package.Dest-Search")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelMapToList() {
        sut.trackHotelMapToList()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Expand.List")
        OmnitureTestUtils.assertLinkTracked("Search Results Map View", "App.Package.Hotels.Search.Expand.List", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelMapPinTap() {
        sut.trackHotelMapPinTap()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.TapPin")
        OmnitureTestUtils.assertLinkTracked("Search Results Map View", "App.Package.Hotels.Search.TapPin", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelMapCarouselPropertyClick() {
        sut.trackHotelMapCarouselPropertyClick()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Expand.Package")
        OmnitureTestUtils.assertLinkTracked("Search Results Map View", "App.Package.Hotels.Search.Expand.Package", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelMapSearchThisAreaClick() {
        sut.trackHotelMapSearchThisAreaClick()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.AreaSearch")
        OmnitureTestUtils.assertLinkTracked("Search Results Map View", "App.Package.Hotels.Search.AreaSearch", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelFilterLoad() {
        sut.trackHotelFilterLoad()
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightRoundTripLoadWhenOutBound() {
        sut.trackFlightRoundTripLoad(true, getDummyPackageSearchParams())
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightRoundTripLoadWhenInBound() {
        sut.trackFlightRoundTripLoad(false, getDummyPackageSearchParams())
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightRoundTripDetailsLoadOutBound() {
        sut.trackFlightRoundTripDetailsLoad(true)
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightRoundTripDetailsLoadInBound() {
        sut.trackFlightRoundTripDetailsLoad(false)
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightSortFilterLoad() {
        sut.trackFlightSortFilterLoad()
        val controlEvar = mapOf(18 to "D=pageName")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelSortBy() {
        sut.trackHotelSortBy("Rating")
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Sort.Rating")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.Sort.Rating", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelFilterPriceSlider() {
        sut.trackHotelFilterPriceSlider()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Price")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.Price", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelFilterVIPWhenTrue() {
        sut.trackHotelFilterVIP(true)
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Filter.VIP.On")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.Filter.VIP.On", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelFilterVIPWhenFalse() {
        sut.trackHotelFilterVIP(false)
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Filter.VIP.Off")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.Filter.VIP.Off", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelFilterNeighbourhood() {
        sut.trackHotelFilterNeighbourhood()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Neighborhood")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.Neighborhood", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelFilterByName() {
        sut.trackHotelFilterByName()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.PackageName")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.PackageName", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelClearFilter() {
        sut.trackHotelClearFilter()
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.ClearFilter")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.ClearFilter", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelRefineRating() {
        sut.trackHotelRefineRating("5")
        val controlEvar = mapOf(28 to "App.Package.Hotels.Search.Filter.5Star")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Hotels.Search.Filter.5Star", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutPaymentSelectStoredCard() {
        sut.trackCheckoutPaymentSelectStoredCard()
        val controlEvar = mapOf(28 to "App.Package.CKO.Payment.StoredCard")
        OmnitureTestUtils.assertLinkTracked("Package Checkout", "App.Package.CKO.Payment.StoredCard", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelDetailLoad() {
        sut.trackHotelDetailLoad("1")
        val controlEvar = mapOf(18 to "App.Package.Hotels.Infosite",
                61 to "1")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelDetailBookPhoneClick() {
        sut.trackHotelDetailBookPhoneClick()
        val controlEvar = mapOf(28 to "App.Package.Infosite.BookPhone")
        OmnitureTestUtils.assertLinkTracked("Package Infosite", "App.Package.Infosite.BookPhone", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelDetailSelectRoomClickWhenStickyTrue() {
        sut.trackHotelDetailSelectRoomClick(true)
        val controlEvar = mapOf(28 to "App.Package.Infosite.SelectRoom.Sticky")
        OmnitureTestUtils.assertLinkTracked("Package Infosite", "App.Package.Infosite.SelectRoom.Sticky", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelDetailSelectRoomClickWhenStickyFalse() {
        sut.trackHotelDetailSelectRoomClick(false)
        val controlEvar = mapOf(28 to "App.Package.Infosite.SelectRoom.Top")
        OmnitureTestUtils.assertLinkTracked("Package Infosite", "App.Package.Infosite.SelectRoom.Top", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelDetailGalleryClick() {
        sut.trackHotelDetailGalleryClick()
        val controlEvar = mapOf(28 to "App.Package.Hotels.IS.Gallery.Hotel")
        OmnitureTestUtils.assertLinkTracked("Gallery View", "App.Package.Hotels.IS.Gallery.Hotel", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelReviewCategoryChangeWhenRecent() {
        sut.trackHotelReviewCategoryChange(0)
        val controlEvar = mapOf(28 to "App.Package.Reviews.Recent")
        OmnitureTestUtils.assertLinkTracked("Package Reviews", "App.Package.Reviews.Recent", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelReviewCategoryChangeWhenFavorable() {
        sut.trackHotelReviewCategoryChange(1)
        val controlEvar = mapOf(28 to "App.Package.Reviews.Favorable")
        OmnitureTestUtils.assertLinkTracked("Package Reviews", "App.Package.Reviews.Favorable", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelReviewCategoryChangeWhenCritical() {
        sut.trackHotelReviewCategoryChange(2)
        val controlEvar = mapOf(28 to "App.Package.Reviews.Critical")
        OmnitureTestUtils.assertLinkTracked("Package Reviews", "App.Package.Reviews.Critical", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelReviewCategoryChangeWhenNone() {
        sut.trackHotelReviewCategoryChange(3)
        val controlEvar = mapOf(28 to "App.Package.Reviews.N/A")
        OmnitureTestUtils.assertLinkTracked("Package Reviews", "App.Package.Reviews.N/A", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleOverviewHotelExpandClickWhenTrue() {
        sut.trackBundleOverviewHotelExpandClick(true)
        val controlEvar = mapOf(28 to "App.Package.RD.Details.Hotel.Expand")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.Details.Hotel.Expand", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleOverviewHotelExpandClickWhenFalse() {
        sut.trackBundleOverviewHotelExpandClick(false)
        val controlEvar = mapOf(28 to "App.Package.RD.Details.Hotel.Collapse")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.Details.Hotel.Collapse", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleOverviewFlightExpandClickWhenTrue() {
        sut.trackBundleOverviewFlightExpandClick(true)
        val controlEvar = mapOf(28 to "App.Package.RD.Details.Flight.Expand")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.Details.Flight.Expand", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleOverviewFlightExpandClickWhenFalse() {
        sut.trackBundleOverviewFlightExpandClick(false)
        val controlEvar = mapOf(28 to "App.Package.RD.Details.Flight.Collapse")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.Details.Flight.Collapse", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleOverviewCostBreakdownClick() {
        sut.trackBundleOverviewCostBreakdownClick()
        val controlEvar = mapOf(28 to "App.Package.RD.TotalCost")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.TotalCost", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackSearchTravelerPickerChooserClick() {
        sut.trackSearchTravelerPickerChooserClick("Text")
        val controlEvar = mapOf(28 to "App.Package.Traveler.Text")
        OmnitureTestUtils.assertLinkTracked("Search Results Update", "App.Package.Traveler.Text", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightBaggageFeeClick() {
        sut.trackFlightBaggageFeeClick()
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.BaggageFee")
        OmnitureTestUtils.assertLinkTracked("Flight Baggage Fee", "App.Package.Flight.Search.BaggageFee", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightSortByPrice() {
        sut.trackFlightSortBy(FlightFilter.Sort.PRICE)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Sort.Price")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Flight.Search.Sort.Price", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightSortByArrival() {
        sut.trackFlightSortBy(FlightFilter.Sort.ARRIVAL)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Sort.Arrival")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Flight.Search.Sort.Arrival", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightSortByDeparture() {
        sut.trackFlightSortBy(FlightFilter.Sort.DEPARTURE)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Sort.Departure")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Flight.Search.Sort.Departure", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightSortByDuration() {
        sut.trackFlightSortBy(FlightFilter.Sort.DURATION)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Sort.Duration")
        OmnitureTestUtils.assertLinkTracked("Search Results Sort", "App.Package.Flight.Search.Sort.Duration", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightFilterStopsWhenNonStop() {
        sut.trackFlightFilterStops(BaseFlightFilterViewModel.Stops.NONSTOP)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Filter.No Stops")
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Package.Flight.Search.Filter.No Stops", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightFilterStopsWhenOneStop() {
        sut.trackFlightFilterStops(BaseFlightFilterViewModel.Stops.ONE_STOP)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Filter.1 Stop")
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Package.Flight.Search.Filter.1 Stop", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightFilterStopsWhenTwoPlusStop() {
        sut.trackFlightFilterStops(BaseFlightFilterViewModel.Stops.TWO_PLUS_STOPS)
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Filter.2 Stops")
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Package.Flight.Search.Filter.2 Stops", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackFlightFilterAirlines() {
        sut.trackFlightFilterAirlines()
        val controlEvar = mapOf(28 to "App.Package.Flight.Search.Filter.Airline")
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Package.Flight.Search.Filter.Airline", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelRoomBookClick() {
        sut.trackHotelRoomBookClick()
        val controlEvar = mapOf(28 to "App.Package.Hotels.IS.BookRoom")
        OmnitureTestUtils.assertLinkTracked("Room Info", "App.Package.Hotels.IS.BookRoom", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelViewBookClick() {
        sut.trackHotelViewBookClick()
        val controlEvar = mapOf(28 to "App.Package.Hotels.IS.ViewRoom")
        OmnitureTestUtils.assertLinkTracked("Room Info", "App.Package.Hotels.IS.ViewRoom", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelRoomMoreInfoClick() {
        sut.trackHotelRoomMoreInfoClick()
        val controlEvar = mapOf(28 to "App.Package.Hotels.IS.MoreRoomInfo")
        OmnitureTestUtils.assertLinkTracked("Room Info", "App.Package.Hotels.IS.MoreRoomInfo", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelMapViewSelectRoomClick() {
        sut.trackHotelMapViewSelectRoomClick()
        val controlEvar = mapOf(28 to "App.Package.IS.Map.SelectRoom")
        OmnitureTestUtils.assertLinkTracked("Infosite Map", "App.Package.IS.Map.SelectRoom", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackSearchError() {
        sut.trackSearchError("ErrorType")
        val controlEvar = mapOf(18 to "App.Package.Hotels-Search.NoResults")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutError() {
        sut.trackCheckoutError(ApiError(ApiError.Code.UNMAPPED_ERROR))
        val controlEvar = mapOf(18 to "App.Package.Checkout.Error")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutErrorRetry() {
        sut.trackCheckoutErrorRetry()
        val controlEvar = mapOf(28 to "App.Package.CKO.Error.Retry")
        OmnitureTestUtils.assertLinkTracked("Package Checkout", "App.Package.CKO.Error.Retry", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutPriceChange() {
        sut.trackCheckoutPriceChange(20)
        val controlEvar = mapOf(28 to "App.Package.CKO.PriceChange")
        OmnitureTestUtils.assertLinkTracked("Package Checkout", "App.Package.CKO.PriceChange", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCreateTripPriceChange() {
        sut.trackCreateTripPriceChange(20)
        val controlEvar = mapOf(28 to "App.Package.RD.PriceChange")
        OmnitureTestUtils.assertLinkTracked("Rate Details View", "App.Package.RD.PriceChange", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutSelectTraveler() {
        sut.trackCheckoutSelectTraveler()
        val controlEvar = mapOf(18 to "App.Package.Checkout.Traveler.Select")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutEditTraveler() {
        sut.trackCheckoutEditTraveler()
        val controlEvar = mapOf(18 to "App.Package.Checkout.Traveler.Edit.Info")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackCheckoutPaymentCID() {
        sut.trackCheckoutPaymentCID()
        val controlEvar = mapOf(18 to "App.Package.Checkout.Payment.CID")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleEditClick() {
        sut.trackBundleEditClick()
        val controlEvar = mapOf(28 to "App.Package.RD.Edit")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.Edit", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackBundleEditItemClick() {
        sut.trackBundleEditItemClick("Item")
        val controlEvar = mapOf(28 to "App.Package.RD.Edit.Item")
        OmnitureTestUtils.assertLinkTracked("Rate Details", "App.Package.RD.Edit.Item", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    private fun getDummyPackageSearchParams(): PackageSearchParams {
        return PackageSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                context.resources.getInteger(R.integer.max_calendar_selectable_date_range))
                .origin(getDummySuggestion("123"))
                .destination(getDummySuggestion("456"))
                .adults(1)
                .children(listOf(10,2))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams
    }

    private fun getDummySuggestion(code: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = "1011"
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport =  SuggestionV4.Airport();
        airport.airportCode = "";
        airport.multicity = code;
        hierarchyInfo.airport = airport
        suggestion.hierarchyInfo = hierarchyInfo;
        return suggestion
    }

}