package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.AppAnalytics
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.trips.TripUtils
import com.mobiata.android.Log

object TripsTracking : OmnitureTracking(), ITripsTracking {

    //Tags
    private val TAG = "TripsTracking"

    //Hotel Tracking
    private val ITIN_HOTEL_CALL_EXPEDIA = "App.Itinerary.Hotel.Manage.Call.Expedia"
    private val ITIN_HOTEL_OPEN_SUPPORT_WEBSITE = "App.Itinerary.Hotel.Manage.CSP"
    private val ITIN_HOTEL_CALL_HOTEL = "App.Itinerary.Hotel.Manage.Call.Hotel"
    private val ITIN_HOTEL_MESSAGE = "App.Itinerary.Hotel.Message.Hotel"
    private val ITIN_HOTEL_MANAGE_BOOKING_MESSAGE = "App.Itinerary.Hotel.Manage.Message.Hotel"
    private val ITIN_HOTEL_INFO = "App.Itinerary.Hotel.Info.Additional"
    private val ITIN_HOTEL_PRICING_REWARDS = "App.Itinerary.Hotel.PricingRewards"
    private val ITIN_HOTEL_MANAGE_BOOKING = "App.Itinerary.Hotel.ManageBooking"
    private val ITIN_HOTEL_CHECK_IN_POLICIES = "App.Itinerary.Hotel.Info.Check-in"
    private val ITIN_HOTEL_CHANGE_HOTEL = "App.Itinerary.Hotel.Manage.Change"
    private val ITIN_HOTEL_CANCEL_HOTEL = "App.Itinerary.Hotel.Manage.Cancel"
    private val ITIN_HOTEL_CHANGE_CANCEL_RULES = "App.Itinerary.Hotel.Manage.Info.Change-Cancel"
    private val ITIN_HOTEL_CHANGE_HOTEL_LEARN_MORE = "App.Itinerary.Hotel.Manage.Change.LearnMore"
    private val ITIN_HOTEL_CANCEL_HOTEL_LEARN_MORE = "App.Itinerary.Hotel.Manage.Cancel.LearnMore"
    private val ITIN_HOTEL_CALL = "App.Itinerary.Hotel.Call"
    private val ITIN_HOTEL_MAP_OPEN = "App.Itinerary.Hotel.Map"
    private val ITIN_HOTEL_DIRECTIONS = "App.Itinerary.Hotel.Directions"
    private val ITIN_HOTEL_MAP_DIRECTIONS = "App.Map.Directions.Drive"
    private val ITIN_HOTEL_MAP_PAN = "App.Map.Directions.Pan"
    private val ITIN_HOTEL_MAP_ZOOM_IN = "App.Map.Directions.ZoomIn"
    private val ITIN_HOTEL_MAP_ZOOM_OUT = "App.Map.Directions.ZoomOut"
    private val ITIN_HOTEL = "App.Itinerary.Hotel"

    //LX Tracking
    private val ITIN_ACTIVITY = "App.Itinerary.Activity"

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

    override fun trackHotelItinPricingRewardsClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_PRICING_REWARDS)
        trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsHotelPricing)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinManageBookingClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MANAGE_BOOKING)
        trackAbacusTest(s, AbacusUtils.TripsHotelsM2)
        s.trackLink("Itinerary Action")
    }

    fun trackHotelItinCheckInPoliciesDialogClick() {
        val s = createTrackLinkEvent(ITIN_HOTEL_CHECK_IN_POLICIES)
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

    fun trackItinHotelMapDirectionsButton() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MAP_DIRECTIONS)
        s.trackLink("Map Action")
    }

    fun trackItinExpandedMapZoomIn() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MAP_ZOOM_IN)
        s.trackLink("Map Action")
    }

    fun trackItinExpandedMapZoomOut() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MAP_ZOOM_OUT)
        s.trackLink("Map Action")
    }

    fun trackItinExpandedMapZoomPan() {
        val s = createTrackLinkEvent(ITIN_HOTEL_MAP_PAN)
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
        val userTrips = getUsersTrips()
        if (userStateManager.isUserAuthenticated()) {
            appendUsersEventString(s)
            s.setProp(75, TripUtils.createUsersProp75String(userTrips))
        }
        s.setProducts(trip.get("productString").toString())
        s.setProp(8, trip.get("orderAndTripNumbers").toString())
        s.setEvar(6, trip.get("duration").toString())
        s.setProp(5, trip.get("tripStartDate").toString())
        s.setProp(6, trip.get("tripEndDate").toString())
        s.setEvar(5, trip.get("daysUntilTrip").toString())
        s.appendEvents("event63")
        s.track()
    }

    @JvmStatic
    fun trackItinLx(trip: HashMap<String, String?>) {
        Log.d(TAG, "Tracking \"$ITIN_ACTIVITY\" pageLoad")
        val s = createTrackPageLoadEventBase(ITIN_ACTIVITY)
        val userTrips = getUsersTrips()
        if (userStateManager.isUserAuthenticated()) {
            appendUsersEventString(s)
            s.setProp(75, TripUtils.createUsersProp75String(userTrips))
        }
        s.setProducts(trip.get("productString").toString())
        s.setProp(8, trip.get("orderAndTripNumbers").toString())
        s.setEvar(6, trip.get("duration").toString())
        s.setProp(5, trip.get("tripStartDate").toString())
        s.setProp(6, trip.get("tripEndDate").toString())
        s.setEvar(5, trip.get("daysUntilTrip").toString())
        s.appendEvents("event63")
        s.track()
    }
}
