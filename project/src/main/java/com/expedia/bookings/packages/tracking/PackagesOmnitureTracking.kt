package com.expedia.bookings.packages.tracking

import android.text.TextUtils
import com.expedia.bookings.analytics.AppAnalytics
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.MIDItinDetailsResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.TrackingUtils
import com.mobiata.android.Log
import java.util.ArrayList

object PackagesOmnitureTracking : OmnitureTracking() {

    private val TAG = "PackagesOmnitureTracking"

    private val PACKAGES_LOB = "package:FH"
    private val CHECKOUT_ERROR_PAGE_NAME = "App.Checkout.Error"
    private val PACKAGES_DESTINATION_SEARCH = "App.Package.Dest-Search"
    private val PACKAGES_HOTEL_SEARCH_MAP_LOAD = "App.Package.Hotels.Search.Map"
    private val PACKAGES_HOTEL_SEARCH_RESULT_FILTERS_LOAD = "App.Package.Hotels.Search.Filtered"
    private val PACKAGES_HOTEL_SEARCH_RESULT_LOAD = "App.Package.Hotels.Search"
    private val PACKAGES_HOTEL_SEARCH_SPONSORED_PRESENT = "App.Package.Hotels.Search.Sponsored.YES"
    private val PACKAGES_HOTEL_SEARCH_SPONSORED_NOT_PRESENT = "App.Package.Hotels.Search.Sponsored.NO"
    private val PACKAGES_HOTEL_SEARCH_ZERO_RESULT_LOAD = "App.Package.Hotels-Search.NoResults"
    private val PACKAGES_HOTEL_MAP_TO_LIST_VIEW = "App.Package.Hotels.Search.Expand.List"
    private val PACKAGES_HOTEL_MAP_PIN_TAP = "App.Package.Hotels.Search.TapPin"
    private val PACKAGES_HOTEL_CAROUSEL_TAP = "App.Package.Hotels.Search.Expand.Package"
    private val PACKAGES_HOTEL_MAP_SEARCH_AREA = "App.Package.Hotels.Search.AreaSearch"
    private val PACKAGES_CHECKOUT_PAYMENT_SELECT = "App.Package.Checkout.Payment.Select"
    private val PACKAGES_CHECKOUT_PAYMENT_SELECT_STORED_CC = "App.Package.CKO.Payment.StoredCard"
    private val PACKAGES_MID_SERVER_ERROR = "App.Package.Checkout.Error"
    private val PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION = "App.Package.Checkout.Confirmation"
    private val PACKAGES_HOTEL_RT_OUT_RESULTS = "App.Package.Flight.Search.Roundtrip.Out"
    private val PACKAGES_HOTEL_RT_IN_RESULTS = "App.Package.Flight.Search.Roundtrip.In"
    private val PACKAGES_HOTEL_RT_OUT_DETAILS = "App.Package.Flight.Search.Roundtrip.Out.Details"
    private val PACKAGES_HOTEL_RT_IN_DETAILS = "App.Package.Flight.Search.Roundtrip.In.Details"
    private val PACKAGES_HOTEL_DETAILS_LOAD = "App.Package.Hotels.Infosite"
    private val PACKAGES_HOTEL_DETAILS_BOOK_BY_PHONE = "App.Package.Infosite.BookPhone"
    private val PACKAGES_HOTEL_DETAILS_SELECT_ROOM_TEMPLATE = "App.Package.Infosite.SelectRoom."
    private val PACKAGES_HOTELS_DETAIL_GALLERY_CLICK = "App.Package.Hotels.IS.Gallery.Hotel"
    private val PACKAGES_HOTELS_DETAIL_ROOM_GALLERY_CLICK = "App.Package.Hotels.IS.Gallery.Room"
    private val PACKAGES_HOTEL_DETAILS_REVIEWS = "App.Package.Reviews"
    private val PACKAGES_HOTEL_DETAILS_REVIEWS_CATEGORY_TEMPLATE = "App.Package.Reviews."
    private val PACKAGES_HOTEL_DETAILS_RESORT_FEE_INFO = "App.Package.ResortFeeInfo"
    private val PACKAGES_HOTEL_DETAILS_RENOVATION_INFO = "App.Package.RenovationInfo"
    private val PACKAGES_BUNDLE_VIEW_OVERVIEW_LOAD = "App.Package.BundleView"
    private val PACKAGES_BUNDLE_VIEW_TAP = "App.Package.BundleWidget.Tap"
    private val PACKAGES_BUNDLE_OVERVIEW_LOAD = "App.Package.RateDetails"
    private val PACKAGES_BUNDLE_OVERVIEW_PRODUCT_EXPAND_TEMPLATE = "App.Package.RD.Details."
    private val PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_SAVINGS_STRIP_CLICK = "App.Package.RD.SavingsStrip"
    private val PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_SAVINGS_BUTTON_CLICK = "App.Package.RD.SavingsButton"
    private val PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_INFO_ICON_CLICK = "App.Package.RD.InfoIcon"
    private val PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_BUNDLE_PRICE_CLICK = "App.Package.RD.BundlePrice"
    private val PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_BUNDLE_WIDGET_CLICK_TEMPLATE = "App.Package.RD.BundleWidget."
    private val PACKAGES_SEARCH_TRAVELER_PICKER_CLICK_TEMPLATE = "App.Package.Traveler."
    private val PACKAGES_FLIGHT_BAGGAGE_FEE_CLICK = "App.Package.Flight.Search.BaggageFee"
    private val PACKAGES_FLIGHT_SORT_FILTER_LOAD = "App.Package.Flight.Search.Filter"
    private val PACKAGES_FLIGHT_SORTBY_TEMPLATE = "App.Package.Flight.Search.Sort."
    private val PACKAGES_FLIGHT_FILTER_STOPS_TEMPLATE = "App.Package.Flight.Search.Filter."
    private val PACKAGES_FLIGHT_FILTER_AIRLINES_TEMPLATE = "App.Package.Flight.Search.Filter.Airline."
    private val PACKAGES_FLIGHT_FILTER_TIME_TEMPLATE = "App.Package.Flight.Search.Filter.Time."
    private val PACKAGES_FLIGHT_FILTER_DURATION = "App.Package.Flight.Search.Filter.Duration"
    private val PACKAGES_HOTEL_DETAILS_BOOK_ROOM = "App.Package.Hotels.IS.BookRoom"
    private val PACKAGES_HOTEL_DETAILS_VIEW_ROOM = "App.Package.Hotels.IS.ViewRoom"
    private val PACKAGES_HOTEL_DETAILS_ROOM_INFO = "App.Package.Hotels.IS.MoreRoomInfo"
    private val PACKAGES_HOTEL_DETAILS_MAP = "App.Package.Infosite.Map"
    private val PACKAGES_HOTEL_DETAILS_MAP_SELECT_ROOM = "App.Package.IS.Map.SelectRoom"
    private val PACKAGES_SHOPPING_ERROR = "App.Package.Shopping.Error"
    private val PACKAGES_CHECKOUT_ERROR = "App.Package.Checkout.Error"
    private val PACKAGES_CHECKOUT_ERROR_RETRY = "App.Package.CKO.Error.Retry"
    private val PACKAGES_SEARCH_VALIDATION_ERROR = "App.Package.Search.Validation.Error"
    private val PACKAGES_CHECKOUT_SELECT_TRAVELER = "App.Package.Checkout.Traveler.Select"
    private val PACKAGES_CHECKOUT_EDIT_TRAVELER = "App.Package.Checkout.Traveler.Edit.Info"
    private val PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE = "App.Package.Checkout.SlideToPurchase"
    private val PACKAGES_CHECKOUT_PAYMENT_CID = "App.Package.Checkout.Payment.CID"
    private val PACKAGES_BUNDLE_PRICE_CHANGE = "App.Package.RD.PriceChange"
    private val PACKAGES_CHECKOUT_PRICE_CHANGE = "App.Package.CKO.PriceChange"
    private val PACKAGES_HOTELS_SEARCH_REFINE = "App.Package.Hotels.Search.Filter"
    private val PACKAGES_HOTELS_SORT_BY_TEMPLATE = "App.Package.Hotels.Search.Sort."
    private val PACKAGES_HOTELS_FILTER_PRICE = "App.Package.Hotels.Search.Price"
    private val PACKAGES_HOTELS_FILTER_VIP_TEMPLATE = "App.Package.Hotels.Search.Filter.VIP."
    private val PACKAGES_HOTELS_FILTER_NEIGHBOURHOOD = "App.Package.Hotels.Search.Neighborhood"
    private val PACKAGES_HOTELS_FILTER_BY_NAME = "App.Package.Hotels.Search.HotelName"
    private val PACKAGES_HOTELS_FILTER_CLEAR = "App.Package.Hotels.Search.ClearFilter"
    private val PACKAGES_HOTELS_FILTER_APPLIED = "App.Package.Hotels.Search.Filter.Apply"
    private val PACKAGES_BUNDLE_EDIT = "App.Package.RD.Edit"
    private val PACKAGES_FHC_TAB = "App.Package.DS.FHC.TabClicked"
    private val PACKAGES_DORMANT_REDIRECT = "APP.PACKAGE.DORMANT.HOMEREDIRECT"
    private val PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_LOAD = "App.Package.RD.PriceSummary"
    private val PACKAGES_HOTEL_SEARCH_RESULTS_SCROLL = "App.Package.Hotels.Search.Scroll."

    fun trackPackagesDestinationSearchInit(pageUsableData: PageUsableData) {
        val abTests = ArrayList<ABTest>()
        abTests.add(AbacusUtils.EBAndroidAppPackagesWebviewFHC)
        abTests.add(AbacusUtils.EBAndroidAppPackagesAATest)
        abTests.add(AbacusUtils.EBAndroidAppPackagesFFPremiumClass)
        abTests.add(AbacusUtils.EBAndroidAppPackagesSearchFormRenameToFrom)
        trackPackagePageLoadEventStandard(PACKAGES_DESTINATION_SEARCH, pageUsableData, abTests)
    }

    private fun trackPackagePageLoadEventStandard(pageName: String, pageUsableData: PageUsableData, abTests: List<ABTest>) {
        Log.d(TAG, "Tracking \"$pageName\" pageLoad")
        val s = createTrackPackagePageLoadEventBase(pageName, pageUsableData)
        for (testKey in abTests) {
            trackAbacusTest(s, testKey)
        }
        s.track()
    }

    fun trackPackagesHSRMapInit() {
        trackPackagePageLoadEventStandard(PACKAGES_HOTEL_SEARCH_MAP_LOAD, null)
    }

    fun trackPackageFilteredHSRLoad(response: BundleSearchResponse, pageUsableData: PageUsableData) {
        val s = getFreshTrackingObject()
        if (response.getHotelResultsCount() > 0) {
            Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_SEARCH_RESULT_FILTERS_LOAD + "\"")
            s.appState = PACKAGES_HOTEL_SEARCH_RESULT_FILTERS_LOAD
            s.setEvar(18, PACKAGES_HOTEL_SEARCH_RESULT_FILTERS_LOAD)
            addPackagesCommonFields(s)
            s.setProp(1, response.getHotelResultsCount().toString())
            addPageLoadTimeTrackingEvents(s, pageUsableData)
        }
        s.track()
    }

    fun trackPackagesHSRLoad(response: BundleSearchResponse, pageUsableData: PageUsableData) {
        val s = getFreshTrackingObject()

        if (response.getHotelResultsCount() > 0) {
            Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_SEARCH_RESULT_LOAD + "\"")
            s.appState = PACKAGES_HOTEL_SEARCH_RESULT_LOAD
            s.setEvar(18, PACKAGES_HOTEL_SEARCH_RESULT_LOAD)
            addPackagesCommonFields(s)
            s.appendEvents("event12,event53")
            s.setProp(1, response.getHotelResultsCount().toString())

            if (response.hasSponsoredHotelListing()) {
                s.setEvar(28, PACKAGES_HOTEL_SEARCH_SPONSORED_PRESENT)
                s.setProp(16, PACKAGES_HOTEL_SEARCH_SPONSORED_PRESENT)
            } else {
                s.setEvar(28, PACKAGES_HOTEL_SEARCH_SPONSORED_NOT_PRESENT)
                s.setProp(16, PACKAGES_HOTEL_SEARCH_SPONSORED_NOT_PRESENT)
            }

            /*
				1R = num of rooms booked, since we don't support multi-room booking on the app yet hard coding it.
				RT = Round Trip package
		 	*/
            val packageSearchParams = Db.sharedInstance.packageParams
            val children = getChildCount(packageSearchParams.children)
            val infantInLap = getInfantInLap(packageSearchParams.children,
                    packageSearchParams.infantSeatingInLap)
            val youth = getYouthCount(packageSearchParams.children)
            val infantInseat = packageSearchParams.children.size - (infantInLap + youth + children)

            val evar47String = StringBuilder("PKG|1R|RT|")
            evar47String.append("A" + packageSearchParams.adults + "|")
            evar47String.append("C$children|")
            evar47String.append("YTH$youth|")
            evar47String.append("IL$infantInLap|")
            evar47String.append("IS" + infantInseat)

            if (packageSearchParams.flightCabinClass != null) {
                val cabinCodeName = FlightServiceClassType
                        .getCabinCodeFromMIDParam(packageSearchParams.flightCabinClass!!).name
                evar47String.append("|" + FlightServiceClassType.getCabinClassTrackCode(cabinCodeName))
            }

            s.setEvar(47, evar47String.toString())

            // Freeform location
            if (!TextUtils.isEmpty(Db.sharedInstance.packageParams.destination!!.regionNames.fullName)) {
                s.setEvar(48, Db.sharedInstance.packageParams.destination!!.regionNames.fullName)
            }
            addPageLoadTimeTrackingEvents(s, pageUsableData)
        } else {
            Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_SEARCH_ZERO_RESULT_LOAD + "\"")
            s.appState = PACKAGES_HOTEL_SEARCH_ZERO_RESULT_LOAD
            s.setEvar(2, PACKAGES_LOB)
            s.setProp(2, "D=c2")
            s.setProp(36, response.firstError.toString())
        }
        trackAbacusTest(s, AbacusUtils.EBAndroidAppPackagesServerSideFiltering)
        trackAbacusTest(s, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        trackAbacusTest(s, AbacusUtils.EBAndroidAppPackagesHighlightSortFilter)
        s.track()
    }

    fun trackPackagesHotelMapToList() {
        trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_TO_LIST_VIEW)
    }

    fun trackPackagesHotelMapPinTap() {
        trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_PIN_TAP)
    }

    fun trackPackagesHotelMapCarouselPropertyClick() {
        trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_CAROUSEL_TAP)
    }

    fun trackPackagesHotelMapSearchThisAreaClick() {
        trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_SEARCH_AREA)
    }

    private fun trackPackagesHotelMapLinkEvent(link: String) {
        createAndTrackLinkEvent(link, "Search Results Map View")
    }

    fun trackPackagesPaymentSelect() {
        trackPackagePageLoadEventStandard(PACKAGES_CHECKOUT_PAYMENT_SELECT, null)
    }

    fun trackPackagesPaymentStoredCCSelect() {
        createAndTrackLinkEvent(PACKAGES_CHECKOUT_PAYMENT_SELECT_STORED_CC, "Package Checkout")
    }

    fun trackPackagesMIDCreateTripError(errorType: String) {
        Log.d(TAG, "Tracking \"$PACKAGES_MID_SERVER_ERROR\" pageLoad...")
        val s = createTrackPageLoadEventBase(PACKAGES_MID_SERVER_ERROR)
        s.setProp(36, errorType)
        s.track()
    }

    fun trackMIDConfirmation(response: MIDItinDetailsResponse, hotelSupplierType: String,
                             pageUsableData: PageUsableData) {
        Log.d(TAG, "Tracking \"$PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION\" pageLoad")
        val s = createTrackPackagePageLoadEventBase(PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION, null)
        var totalPaidMoney = response.responseData.getTotalPaidMoney()
        if (totalPaidMoney == null) {
            totalPaidMoney = Money("0", "")
            trackPackagesShoppingError(ApiCallFailing.ConfirmationPaymentSummaryMissing().getErrorStringForTracking())
        }
        setMidProducts(s, totalPaidMoney.amount.toDouble(), true, true, hotelSupplierType,
                response.responseData.insurance)
        s.setCurrencyCode(totalPaidMoney.currencyCode)
        s.appendEvents("purchase")
        val orderId = response.responseData.hotels[0].orderNumber
        s.setPurchaseID("onum" + orderId)
        s.setProp(72, orderId)
        addPageLoadTimeTrackingEvents(s, pageUsableData)
        s.track()
    }

    private fun setMidProducts(s: AppAnalytics, productPrice: Double?, addEvarInventory: Boolean,
                               isConfirmation: Boolean, hotelSupplierType: String,
                               insurances: List<AbstractItinDetailsResponse.ResponseData.Insurance>?) {
        var productsString = getPackageProductsString(productPrice, addEvarInventory, isConfirmation,
                hotelSupplierType)
        if (insurances != null) {
            productsString += "," + TrackingUtils.getInsuranceProductsString(insurances)
        }

        s.setProducts(productsString)
    }

    fun trackPackagesFlightRoundTripOutLoad(pageUsableData: PageUsableData) {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_OUT_RESULTS, pageUsableData)
    }

    fun trackPackagesFlightRoundTripOutDetailsLoad(pageUsableData: PageUsableData, flight: FlightLeg) {
        trackPackagesFlightDetailsLoadWithPageName(PACKAGES_HOTEL_RT_OUT_DETAILS, pageUsableData, flight)
    }

    fun trackPackagesFlightRoundTripInLoad(pageUsableData: PageUsableData) {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_IN_RESULTS, pageUsableData)
    }

    fun trackPackagesFlightRoundTripInDetailsLoad(pageUsableData: PageUsableData, flight: FlightLeg) {
        trackPackagesFlightDetailsLoadWithPageName(PACKAGES_HOTEL_RT_IN_DETAILS, pageUsableData, flight)
    }

    fun trackPackagesFlightDetailsLoadWithPageName(pageName: String, pageUsableData: PageUsableData,
                                                   flight: FlightLeg, vararg abTests: ABTest) {
        val s = trackPackagesCommonDetails(pageName, pageUsableData, *abTests)
        appendEmptyFareRulesTracking(s, flight)
        s.track()
    }

    fun trackPackagesHotelInfoLoad(hotelId: String, pageUsableData: PageUsableData) {
        val s = createTrackPackagePageLoadEventBase(PACKAGES_HOTEL_DETAILS_LOAD, null)
        s.appendEvents("event3")
        addPageLoadTimeTrackingEvents(s, pageUsableData)
        val product = ";Hotel:$hotelId;;"
        s.setProducts(product)
        s.track()
    }

    fun trackPackagesHotelInfoActionBookPhone() {
        Log.d(TAG, "Tracking \"$PACKAGES_HOTEL_DETAILS_BOOK_BY_PHONE\" click...")
        val s = createTrackLinkEvent(PACKAGES_HOTEL_DETAILS_BOOK_BY_PHONE)
        s.appendEvents("event34")
        s.trackLink("Package Infosite")
    }

    fun trackPackagesHotelInfoActionSelectRoom(stickyButton: Boolean) {
        val link = StringBuilder(PACKAGES_HOTEL_DETAILS_SELECT_ROOM_TEMPLATE)
        if (stickyButton) {
            link.append("Sticky")
        } else {
            link.append("Top")
        }
        createAndTrackLinkEvent(link.toString(), "Package Infosite")
    }

    fun trackPackageHotelDetailGalleryClick() {
        Log.d(TAG, "Tracking \"$PACKAGES_HOTELS_DETAIL_GALLERY_CLICK\" click...")

        val s = createTrackLinkEvent(PACKAGES_HOTELS_DETAIL_GALLERY_CLICK)

        s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().tpid))

        s.trackLink("Gallery View")
    }

    fun trackPackageHotelDetailRoomGalleryClick() {
        Log.d(TAG, "Tracking \"$PACKAGES_HOTELS_DETAIL_ROOM_GALLERY_CLICK\" click...")

        val s = createTrackLinkEvent(PACKAGES_HOTELS_DETAIL_ROOM_GALLERY_CLICK)

        s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().tpid))

        s.trackLink("Gallery View")
    }

    fun trackPackagesHotelReviewPageLoad() {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_REVIEWS, null,
                AbacusUtils.HotelUGCTranslations)
    }

    fun trackPackagesHotelReviewCategoryChange(category: String) {
        val link = PACKAGES_HOTEL_DETAILS_REVIEWS_CATEGORY_TEMPLATE + category
        createAndTrackLinkEvent(link, "Package Reviews")
    }

    fun trackPackagesHotelResortFeeInfo() {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_RESORT_FEE_INFO, null)
    }

    fun trackPackagesHotelRenovationInfo() {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_RENOVATION_INFO, null)
    }

    fun trackPackagesViewBundleLoad(isFirstBundleLaunch: Boolean) {
        Log.d(TAG, "Tracking \"$PACKAGES_BUNDLE_VIEW_OVERVIEW_LOAD\" pageLoad")
        val s = createTrackPackagePageLoadEventBase(PACKAGES_BUNDLE_VIEW_OVERVIEW_LOAD, null)
        if (isFirstBundleLaunch) {
            //track AB tests here
        }
        s.track()
    }

    fun trackPackagesBundleWidgetTap() {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_VIEW_TAP, "Bundle Widget Tap")
    }

    fun trackPackagesBundlePageLoad(packageTotal: Double?, pageUsableData: PageUsableData) {
        Log.d(TAG, "Tracking \"" + PACKAGES_BUNDLE_OVERVIEW_LOAD + "\"")

        val s = createTrackPageLoadEventBase(PACKAGES_BUNDLE_OVERVIEW_LOAD)
        addPackagesCommonFields(s)
        setPackageProducts(s, packageTotal!!)
        s.appendEvents("event4")
        addPageLoadTimeTrackingEvents(s, pageUsableData)
        trackAbacusTest(s, AbacusUtils.EBAndroidAppPackagesBetterSavingsOnRateDetails)
        s.track()
    }

    private fun setPackageProducts(s: AppAnalytics, productPrice: Double) {
        setPackageProducts(s, productPrice, false, false, null)
    }

    fun trackPackagesBundleProductExpandClick(lobClicked: String, isExpanding: Boolean) {
        val link = StringBuilder(PACKAGES_BUNDLE_OVERVIEW_PRODUCT_EXPAND_TEMPLATE)
        link.append(lobClicked)
        link.append(if (isExpanding) ".Expand" else ".Collapse")
        createAndTrackLinkEvent(link.toString(), "Rate Details")
    }

    fun trackPackagesBundleCostBreakdownSavingsStripClick() {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_SAVINGS_STRIP_CLICK, "Rate Details")
    }

    fun trackPackagesBundleCostBreakdownSavingsButtonClick() {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_SAVINGS_BUTTON_CLICK, "Rate Details")
    }

    fun trackPackagesBundleCostBreakdownInfoIconClick() {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_INFO_ICON_CLICK, "Rate Details")
    }

    fun trackPackagesBundleCostBreakdownBundlePriceClick() {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_BUNDLE_PRICE_CLICK, "Rate Details")
    }

    fun trackPackagesBundleCostBreakdownBundleWidgetClick(shouldShowSavings: Boolean) {
        val suffix = if (shouldShowSavings) "SSST" else "SSSF"
        createAndTrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_BUNDLE_WIDGET_CLICK_TEMPLATE + suffix,
                "Rate Details")
    }

    fun trackPackagesSearchTravelerPickerChooser(text: String) {
        createAndTrackLinkEvent(PACKAGES_SEARCH_TRAVELER_PICKER_CLICK_TEMPLATE + text,
                "Search Results Update")
    }

    fun trackPackagesFlightBaggageFeeClick() {
        createAndTrackLinkEvent(PACKAGES_FLIGHT_BAGGAGE_FEE_CLICK, "Flight Baggage Fee")
    }

    fun trackPackagesFlightSortFilterLoad() {
        trackPackagesPageLoadWithDPageName(PACKAGES_FLIGHT_SORT_FILTER_LOAD, null)
    }

    fun trackPackagesFlightSortBy(sortedBy: String) {
        createAndTrackLinkEvent(PACKAGES_FLIGHT_SORTBY_TEMPLATE + sortedBy, "Search Results Sort")
    }

    fun trackPackagesFlightFilterStops(stops: String) {
        createAndTrackLinkEvent(PACKAGES_FLIGHT_FILTER_STOPS_TEMPLATE + stops, "Search Results Filter")
    }

    fun trackPackagesFlightFilterAirlines(selectedAirlineTag: String) {
        createAndTrackLinkEvent(PACKAGES_FLIGHT_FILTER_AIRLINES_TEMPLATE + selectedAirlineTag,
                "Search Results Filter")
    }

    fun trackPackagesFlightFilterArrivalDeparture(isDeparture: Boolean) {
        trackFlightFilterArrivalDepartureTime(PACKAGES_FLIGHT_FILTER_TIME_TEMPLATE, isDeparture)
    }

    fun trackPackagesFlightFilterDuration() {
        createAndTrackLinkEvent(PACKAGES_FLIGHT_FILTER_DURATION, "Search Results Filter")
    }

    fun trackPackagesHotelRoomBookClick() {
        createAndTrackLinkEvent(PACKAGES_HOTEL_DETAILS_BOOK_ROOM, "Room Info")
    }

    fun trackPackagesHotelViewBookClick() {
        createAndTrackLinkEvent(PACKAGES_HOTEL_DETAILS_VIEW_ROOM, "Room Info")
    }

    fun trackPackagesHotelRoomInfoClick() {
        createAndTrackLinkEvent(PACKAGES_HOTEL_DETAILS_ROOM_INFO, "Room Info")
    }

    fun trackPackagesHotelMapViewClick() {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_MAP, null)
    }

    fun trackPackagesHotelMapSelectRoomClick() {
        createAndTrackLinkEvent(PACKAGES_HOTEL_DETAILS_MAP_SELECT_ROOM, "Infosite Map")
    }

    fun trackPackagesShoppingError(errorInfo: String) {
        Log.d(TAG, "Tracking \"$PACKAGES_SHOPPING_ERROR\" pageLoad...")
        val s = createTrackPageLoadEventBase(PACKAGES_SHOPPING_ERROR)
        s.setProp(36, errorInfo)
        s.track()
    }

    fun trackPackagesCheckoutError(errorType: String) {
        Log.d(TAG, "Tracking \"$PACKAGES_CHECKOUT_ERROR\" pageLoad...")
        val s = createTrackCheckoutErrorPageLoadEventBase(CHECKOUT_ERROR_PAGE_NAME, PACKAGES_CHECKOUT_ERROR)
        s.setProp(36, errorType)
        s.track()
    }

    fun trackPackagesCheckoutErrorRetry() {
        createAndTrackLinkEvent(PACKAGES_CHECKOUT_ERROR_RETRY, "Package Checkout")
    }

    fun trackPackagesSearchValidationError(errorTag: String) {
        val s = createTrackPageLoadEventBase(PACKAGES_SEARCH_VALIDATION_ERROR)
        s.setProp(36, errorTag)
        s.track()
    }

    fun trackPackagesCheckoutSelectTraveler() {
        createTrackPageLoadEventBase(PACKAGES_CHECKOUT_SELECT_TRAVELER).track()
    }

    fun trackPackagesCheckoutEditTraveler() {
        createTrackPageLoadEventBase(PACKAGES_CHECKOUT_EDIT_TRAVELER).track()
    }

    fun trackPackagesCheckoutShowSlideToPurchase(flexStatus: String, cardType: String) {
        Log.d(TAG, "Tracking \"$PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE\" load...")
        trackShowSlidetoPurchase(PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE, cardType, flexStatus)
    }

    fun trackPackagesCheckoutPaymentCID() {
        createTrackPageLoadEventBase(PACKAGES_CHECKOUT_PAYMENT_CID).track()
    }

    fun trackPackagesCreateTripPriceChange(priceDiff: Int) {
        val s = getFreshTrackingObject()
        trackPriceChange(s, priceDiff, PACKAGES_BUNDLE_PRICE_CHANGE, "PKG|", "Rate Details View")
    }

    fun trackPackagesCheckoutPriceChange(priceDiff: Int) {
        val s = getFreshTrackingObject()
        trackPriceChange(s, priceDiff, PACKAGES_CHECKOUT_PRICE_CHANGE, "PKG|", "Package Checkout")
    }

    fun trackPackagesHotelFilterPageLoad() {
        trackPackagesPageLoadWithDPageName(PACKAGES_HOTELS_SEARCH_REFINE, null)
    }

    fun trackPackagesHotelFilterRating(rating: String) {
        val pageName = PACKAGES_HOTELS_SEARCH_REFINE + "." + rating
        createAndTrackLinkEvent(pageName, "Search Results Sort")
    }

    fun trackPackagesHotelSortBy(type: String) {
        val pageName = PACKAGES_HOTELS_SORT_BY_TEMPLATE + type
        createAndTrackLinkEvent(pageName, "Search Results Sort")
    }

    fun trackPackagesHotelFilterPriceSlider() {
        createAndTrackLinkEvent(PACKAGES_HOTELS_FILTER_PRICE, "Search Results Sort")
    }

    fun trackPackagesHotelFilterVIP(type: String) {
        val pageName = PACKAGES_HOTELS_FILTER_VIP_TEMPLATE + type
        createAndTrackLinkEvent(pageName, "Search Results Sort")
    }

    fun trackPackagesHotelFilterNeighborhood() {
        createAndTrackLinkEvent(PACKAGES_HOTELS_FILTER_NEIGHBOURHOOD, "Search Results Sort")
    }

    fun trackPackagesHotelFilterByName() {
        createAndTrackLinkEvent(PACKAGES_HOTELS_FILTER_BY_NAME, "Search Results Sort")
    }

    fun trackPackagesHotelClearFilter() {
        createAndTrackLinkEvent(PACKAGES_HOTELS_FILTER_CLEAR, "Search Results Sort")
    }

    fun trackPackagesHotelFilterApplied() {
        createAndTrackLinkEvent(PACKAGES_HOTELS_FILTER_APPLIED, "Search Results Sort")
    }

    fun trackPackagesBundleEditClick() {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_EDIT, "Rate Details")
    }

    fun trackPackagesBundleEditItemClick(itemType: String) {
        createAndTrackLinkEvent(PACKAGES_BUNDLE_EDIT + "." + itemType, "Rate Details")
    }

    fun trackPackagesFHCTabClick() {
        createAndTrackLinkEvent(PACKAGES_FHC_TAB, "FHC tab")
    }

    fun trackPackagesDormantUserHomeRedirect() {
        createAndTrackLinkEvent(PACKAGES_DORMANT_REDIRECT, "Dormant Redirect")
    }

    private fun addPackagesCommonFields(s: AppAnalytics) {
        s.setProp(2, PACKAGES_LOB)
        s.setEvar(2, "D=c2")
        s.setProp(3, "pkg:" + Db.sharedInstance.packageParams.origin!!.hierarchyInfo!!.airport!!.airportCode)
        s.setEvar(3, "D=c3")
        s.setProp(4, "pkg:" + Db.sharedInstance.packageParams.destination!!.hierarchyInfo!!.airport!!.airportCode
                + ":"
                + Db.sharedInstance.packageParams.destination!!.gaiaId)
        s.setEvar(4, "D=c4")
        setDateValues(s, Db.sharedInstance.packageParams.startDate,
                Db.sharedInstance.packageParams.endDate)
    }

    private fun trackPackagesPageLoadWithDPageName(pageName: String, pageUsableData: PageUsableData?, vararg abTests: ABTest) {
        val s = trackPackagesCommonDetails(pageName, pageUsableData, *abTests)
        s.track()
    }

    private fun trackPackagesCommonDetails(pageName: String, pageUsableData: PageUsableData?,
                                           vararg abTests: ABTest): AppAnalytics {
        Log.d(TAG, "Tracking \"$pageName\" pageLoad")
        val s = createTrackPackagePageLoadEventBase(pageName, null)
        s.setEvar(18, "D=pageName")
        if (pageUsableData != null) {
            addPageLoadTimeTrackingEvents(s, pageUsableData)
        }
        for (testKey in abTests) {
            trackAbacusTest(s, testKey)
        }
        return s
    }

    fun trackPackagesBundleOverviewCostBreakdownLoad() {
        trackPackagePageLoadEventStandard(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN_LOAD, null)
    }

    fun trackPackagesScrollDepth(scrollDepth: String) {
        createAndTrackLinkEvent(PACKAGES_HOTEL_SEARCH_RESULTS_SCROLL + scrollDepth, "PackagesScrollDepth")
    }
}
