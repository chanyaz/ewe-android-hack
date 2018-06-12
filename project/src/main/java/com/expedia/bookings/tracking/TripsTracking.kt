package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.AppAnalytics
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.TripUtils
import com.mobiata.android.Log

object TripsTracking : OmnitureTracking(), ITripsTracking {

    //Tags
    private const val TAG = "TripsTracking"

    //Trip List Tracking
    private const val ITIN_LIST = "App.Trips"
    private const val ITIN_LIST_UPCOMING_TAB = "App.Trips.Upcoming"
    private const val ITIN_LIST_PAST_TAB = "App.Trips.Past"
    private const val ITIN_LIST_CANCELLED_TAB = "App.Trips.Cancelled"

    override fun trackTripFolderAbTest() {
        val s = createTrackLinkEvent(ITIN_LIST)
        trackAbacusTest(s, AbacusUtils.TripFoldersFragment)
        s.trackLink("Itinerary Action")
    }

    override fun trackTripListUpcomingTabVisit() {
        val s = createTrackPageLoadEventBase(ITIN_LIST_UPCOMING_TAB)
        s.appendEvents("event63")
        s.track()
    }

    override fun trackTripListPastTabVisit() {
        val s = createTrackPageLoadEventBase(ITIN_LIST_PAST_TAB)
        s.appendEvents("event63")
        s.track()
    }

    override fun trackTripListCancelledTabVisit() {
        val s = createTrackPageLoadEventBase(ITIN_LIST_CANCELLED_TAB)
        s.appendEvents("event63")
        s.track()
    }

    //General Trips Tracking
    private val ITIN_TRIP_REFRESH_CALL_MADE = "App.Itinerary.Call.Made"
    private val ITIN_TRIP_REFRESH_CALL_SUCCESS = "App.Itinerary.Call.Success"
    private val ITIN_TRIP_REFRESH_CALL_FAILURE = "App.Itinerary.Call.Failure"
    private val ITIN_EMPTY = "App.Itinerary.Empty"
    private val ITIN_NEW_SIGN_IN = "App.Itinerary.Login.Start"
    private val ITIN_USER_REFRESH = "App.Itinerary.User.Refresh"
    private val ITIN_CHANGE_POSA = "App.Itinerary.POSa"
    private val ITIN_FIND_GUEST = "App.Itinerary.Find.Guest"
    private val ITIN_ERROR = "App.Itinerary.Error"
    private val ITIN_ADD_GUEST = "App.Itinerary.Guest.Itin"
    private const val ITIN_MAP_DIRECTIONS = "App.Map.Directions.Drive"
    private const val ITIN_MAP_PAN = "App.Map.Directions.Pan"
    private const val ITIN_MAP_ZOOM_IN = "App.Map.Directions.ZoomIn"
    private const val ITIN_MAP_ZOOM_OUT = "App.Map.Directions.ZoomOut"

    @JvmStatic
    fun trackItinTripRefreshCallMade() {
        val s = createTrackLinkEvent(ITIN_TRIP_REFRESH_CALL_MADE)
        s.appendEvents("event286")
        s.trackLink("Trips Call")
    }

    @JvmStatic
    fun trackItinTripRefreshCallSuccess() {
        val s = createTrackLinkEvent(ITIN_TRIP_REFRESH_CALL_SUCCESS)
        s.appendEvents("event287")
        s.trackLink("Trips Call")
    }

    @JvmStatic
    fun trackItinTripRefreshCallFailure(error: String) {
        val s = createTrackLinkEvent(ITIN_TRIP_REFRESH_CALL_FAILURE)
        s.appendEvents("event288")
        s.setProp(36, error)
        s.trackLink("Trips Call")
    }

    @JvmStatic
    fun trackItinEmpty() {
        internalTrackPageLoadEventStandard(ITIN_EMPTY)
    }

    fun trackItinError() {
        Log.d(TAG, "Tracking \"$ITIN_ERROR\" pageLoad")
        val s = createTrackPageLoadEventBase(ITIN_ERROR)
        s.appendEvents("event98")
        s.setEvar(18, ITIN_ERROR)
        s.setProp(36, "itin:unable to retrieve trip summary")
        s.track()
    }

    fun trackFindGuestItin() {
        internalTrackPageLoadEventStandard(ITIN_FIND_GUEST)
    }

    fun trackItinChangePOS() {
        val s = createTrackLinkEvent(ITIN_CHANGE_POSA)
        s.trackLink("Itinerary Action")
    }

    fun trackItinSignIn() {
        val s = createTrackLinkEvent(ITIN_NEW_SIGN_IN)
        s.trackLink("Itinerary Action")
    }

    fun trackItinRefresh() {
        val s = createTrackLinkEvent(ITIN_USER_REFRESH)
        s.trackLink("Itinerary Action")
    }

    @JvmStatic
    fun trackItinGuestAdd() {
        val s = createTrackLinkEvent(ITIN_ADD_GUEST)
        s.trackLink("Itinerary Action")
    }

    //Hotel Tracking
    private const val ITIN_HOTEL_CALL_EXPEDIA = "App.Itinerary.Hotel.Manage.Call.Expedia"
    private const val ITIN_HOTEL_OPEN_SUPPORT_WEBSITE = "App.Itinerary.Hotel.Manage.CSP"
    private const val ITIN_HOTEL_CALL_HOTEL = "App.Itinerary.Hotel.Manage.Call.Hotel"
    private const val ITIN_HOTEL_MESSAGE = "App.Itinerary.Hotel.Message.Hotel"
    private const val ITIN_HOTEL_MANAGE_BOOKING_MESSAGE = "App.Itinerary.Hotel.Manage.Message.Hotel"
    private const val ITIN_HOTEL_INFO = "App.Itinerary.Hotel.Info.Additional"
    private const val ITIN_HOTEL_PRICING_REWARDS = "App.Itinerary.Hotel.PricingRewards"
    private const val ITIN_HOTEL_MANAGE_BOOKING = "App.Itinerary.Hotel.ManageBooking"
    private const val ITIN_HOTEL_CHECK_IN_POLICIES = "App.Itinerary.Hotel.Info.Check-in"
    private const val ITIN_HOTEL_CHANGE_HOTEL = "App.Itinerary.Hotel.Manage.Change"
    private const val ITIN_HOTEL_CANCEL_HOTEL = "App.Itinerary.Hotel.Manage.Cancel"
    private const val ITIN_HOTEL_CHANGE_CANCEL_RULES = "App.Itinerary.Hotel.Manage.Info.Change-Cancel"
    private const val ITIN_HOTEL_CHANGE_HOTEL_LEARN_MORE = "App.Itinerary.Hotel.Manage.Change.LearnMore"
    private const val ITIN_HOTEL_CANCEL_HOTEL_LEARN_MORE = "App.Itinerary.Hotel.Manage.Cancel.LearnMore"
    private const val ITIN_HOTEL_CALL = "App.Itinerary.Hotel.Call"
    private const val ITIN_HOTEL_MAP_OPEN = "App.Itinerary.Hotel.Map"
    private const val ITIN_HOTEL_DIRECTIONS = "App.Itinerary.Hotel.Directions"
    private const val ITIN_HOTEL_VIEW_RECEIPT = "App.Itinerary.Hotel.PricingRewards.ViewReceipt"
    private const val ITIN_HOTEL_TAXI_CARD = "App.Itinerary.Hotel.TaxiCard"
    private const val ITIN_HOTEL_VIEW_REWARDS = "App.Itinerary.Hotel.PricingRewards.ViewRewards"
    private const val ITIN_HOTEL = "App.Itinerary.Hotel"

    fun trackItinHotelCallSupport() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CALL_EXPEDIA)
        s.trackLink("Itinerary Action")
    }

    fun trackItinHotelOpenSupportWebsite() {
        val s = createTrackLinkEvent(ITIN_HOTEL_OPEN_SUPPORT_WEBSITE)
        s.trackLink("Itinerary Action")
    }

    fun trackItinHotelCallHotel() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CALL_HOTEL)
        s.trackLink("Itinerary Action")
    }

    fun trackItinHotelMessage(isManageBooking: Boolean?) {
        val s: AppAnalytics
        if (isManageBooking!!) {
            s = createTrackLinkEvent(ITIN_HOTEL_MANAGE_BOOKING_MESSAGE)
        } else {
            s = createTrackLinkEvent(ITIN_HOTEL_MESSAGE)
        }
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinAdditionalInfoClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_INFO)
        s.trackLink("Itinerary Action")
    }

    override fun trackHotelItinPricingRewardsPageLoad(trip: HashMap<String, String?>) {
        Log.d(TAG, "Tracking \"$ITIN_HOTEL_PRICING_REWARDS\" pageLoad")
        val s = createTrackPageLoadEventBase(ITIN_HOTEL_PRICING_REWARDS)
        trackItinPageLoad(s, trip)
    }

    override fun trackHotelItinPricingRewardsClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_PRICING_REWARDS)
        trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsHotelPricing)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinManageBookingClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MANAGE_BOOKING)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinCheckInPoliciesDialogClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CHECK_IN_POLICIES)
        s.trackLink("Itinerary Action")
    }

    override fun trackHotelTaxiCardClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_TAXI_CARD)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinChangeHotel() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CHANGE_HOTEL)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinCancelHotel() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CANCEL_HOTEL)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinChangeAndCancelRulesDialogClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CHANGE_CANCEL_RULES)
        s.trackLink("Itinerary Action")
    }

    fun trackItinHotelCancelLearnMore() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CANCEL_HOTEL_LEARN_MORE)
        s.trackLink("Itinerary Action")
    }

    fun trackItinHotelChangeLearnMore() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CHANGE_HOTEL_LEARN_MORE)
        s.trackLink("Itinerary Action")
    }

    @JvmStatic
    fun trackItinHotelCall() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CALL)
        s.trackLink("Itinerary Action")
    }

    fun trackItinHotelExpandMap() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MAP_OPEN)
        s.trackLink("Itinerary Action")
    }

    @JvmStatic
    fun trackItinHotelDirections() {
        val s = createTrackLinkEvent(ITIN_HOTEL_DIRECTIONS)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinHotelViewReceipt() {
        val s = createTrackLinkEvent(ITIN_HOTEL_VIEW_RECEIPT)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinMapDirectionsButton() {
        val s = createTrackLinkEvent(ITIN_MAP_DIRECTIONS)
        s.trackLink("Map Action")
    }

    override fun trackItinExpandedMapZoomIn() {
        val s = createTrackLinkEvent(ITIN_MAP_ZOOM_IN)
        s.trackLink("Map Action")
    }

    override fun trackItinExpandedMapZoomOut() {
        val s = createTrackLinkEvent(ITIN_MAP_ZOOM_OUT)
        s.trackLink("Map Action")
    }

    override fun trackItinExpandedMapZoomPan() {
        val s = createTrackLinkEvent(ITIN_MAP_PAN)
        s.trackLink("Map Action")
    }

    @JvmStatic
    fun trackItinHotelInfo() {
        internalTrackLink(ITIN_HOTEL_INFO)
    }

    @JvmStatic
    fun trackItinHotel(trip: HashMap<String, String?>) {
        Log.d(TAG, "Tracking \"$ITIN_HOTEL\" pageLoad")
        val s = createTrackPageLoadEventBase(ITIN_HOTEL)
        trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelTripTaxiCard)
        trackItinPageLoad(s, trip)
    }

    override fun trackItinHotelViewRewards() {
        val s = createTrackLinkEvent(ITIN_HOTEL_VIEW_REWARDS)
        s.trackLink("Itinerary Action")
    }

    private const val ITIN_CAR_DETAILS_DIRECTION = "App.Itinerary.Car.Directions"
    private const val ITIN_CAR_DETAILS_MAP = "App.Itinerary.Car.Map"
    private const val ITIN_CAR_MORE_HELP = "App.Itinerary.Car.MoreHelp"
    private const val ITIN_CAR_CALL_SUPPORT = "App.Itinerary.Car.Manage.Call.Car"
    private const val ITIN_CAR_CALL_EXPEDIA = "App.Itinerary.Car.Manage.Call.Expedia"
    private const val ITIN_CAR_CUSTOMER_SUPPORT = "App.Itinerary.Car.Manage.CSP"
    override fun trackItinCarDetailsMap() {
        val s = createTrackLinkEvent(ITIN_CAR_DETAILS_MAP)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinCarDetailsDirections() {
        val s = createTrackLinkEvent(ITIN_CAR_DETAILS_DIRECTION)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinCarMoreHelpClicked() {
        val s = createTrackLinkEvent(ITIN_CAR_MORE_HELP)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinCarCallSupportClicked() {
        val s = createTrackLinkEvent(ITIN_CAR_CALL_SUPPORT)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinCarCustomerServiceLinkClicked() {
        val s = createTrackLinkEvent(ITIN_CAR_CUSTOMER_SUPPORT)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinCarCallCustomerSupportClicked() {
        val s = createTrackLinkEvent(ITIN_CAR_CALL_EXPEDIA)
        s.trackLink("Itinerary Action")
    }

    //LX Tracking
    private const val ITIN_ACTIVITY = "App.Itinerary.Activity"
    private const val ITIN_ACTIVITY_DETAILS_MAP = "App.Itinerary.Activity.Map"
    private const val ITIN_ACTIVITY_DETAILS_DIRECTIONS = "App.Itinerary.Activity.Directions"
    private const val ITIN_ACTIVITY_REDEEM_VOUCHER = "App.Itinerary.Activity.Redeem"
    private const val ITIN_ACTIVITY_CALL_SUPPLIER = "App.Itinerary.Activity.Manage.Call.Activity"
    private const val ITIN_ACTIVITY_MORE_HELP = "App.Itinerary.Activity.MoreHelp"
    private const val ITIN_ACTIVITY_CALL_EXPEDIA = "App.Itinerary.Activity.Manage.Call.Expedia"
    private const val ITIN_ACTIVITY_CUSTOMER_SUPPORT = "App.Itinerary.Activity.Manage.CSP"

    override fun trackItinLx(trip: HashMap<String, String?>) {
        Log.d(TAG, "Tracking \"$ITIN_ACTIVITY\" pageLoad")
        val s = createTrackPageLoadEventBase(ITIN_ACTIVITY)
        trackItinPageLoad(s, trip)
    }

    override fun trackItinLxDetailsMap() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_DETAILS_MAP)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinLxDetailsDirections() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_DETAILS_DIRECTIONS)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinLxRedeemVoucher() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_REDEEM_VOUCHER)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinLxCallSupplierClicked() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_CALL_SUPPLIER)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinLxMoreHelpClicked() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_MORE_HELP)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinLxMoreHelpPageLoad(trip: HashMap<String, String?>) {
        Log.d(TAG, "Tracking \"$ITIN_ACTIVITY_MORE_HELP\" pageLoad")
        val s = createTrackPageLoadEventBase(ITIN_ACTIVITY_MORE_HELP)
        trackItinPageLoad(s, trip)
    }

    override fun trackItinLxCallCustomerSupportClicked() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_CALL_EXPEDIA)
        s.trackLink("Itinerary Action")
    }

    override fun trackItinLxCustomerServiceLinkClicked() {
        val s = createTrackLinkEvent(ITIN_ACTIVITY_CUSTOMER_SUPPORT)
        s.trackLink("Itinerary Action")
    }

    fun trackItinPageLoad(s: AppAnalytics, trip: HashMap<String, String?>) {
        val userTrips = getUsersTrips()
        if (userStateManager.isUserAuthenticated()) {
            appendUsersEventString(s)
            s.setProp(75, TripUtils.createUsersProp75String(userTrips))
        }
        s.setProducts(trip.get("productString").toString())
        s.setEvar(2, "D=c2")
        s.setProp(2, "itinerary")
        s.setProp(8, trip.get("orderAndTripNumbers").toString())
        s.setEvar(6, trip.get("duration").toString())
        s.setProp(5, trip.get("tripStartDate").toString())
        s.setProp(6, trip.get("tripEndDate").toString())
        s.setEvar(5, trip.get("daysUntilTrip").toString())
        s.appendEvents("event63")
        s.track()
    }
}
