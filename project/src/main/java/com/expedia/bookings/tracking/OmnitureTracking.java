package com.expedia.bookings.tracking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.adobe.adms.measurement.ADMS_ReferrerHandler;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusTest;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarTrackingData;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.data.packages.PackageCheckoutResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.packages.PackageSearchResponse;
import com.expedia.bookings.data.payment.PaymentSplitsType;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.enums.CheckoutTripBucketState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NumberUtils;
import com.expedia.bookings.utils.PackageFlightUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * The basic premise behind this class is to encapsulate the tracking logic as much possible such that tracking events
 * can be inserted into the business logic as cleanly as possible. The events rely on Db.java to populate values when
 * needed, and exceptions are made to accommodate the events that require extra parameters to be sent. This is why there
 * exist so many methods, one for each event that is being tracked.
 */

public class OmnitureTracking {

	private static final String TAG = "OmnitureTracking";

	// So we don't have to keep reloading this from settings
	private static String sMarketingDate = "";

	private static final DateTimeFormatter sFormatter = DateTimeFormat.forPattern("E|hh:mma");

	private static Context sContext = null;

	public static void init(ExpediaBookingApp app) {
		Log.d(TAG, "init");
		sContext = app.getApplicationContext();
		ADMS_Measurement.sharedInstance(sContext);
		app.registerActivityLifecycleCallbacks(sOmnitureActivityCallbacks);
		sMarketingDate = SettingUtils.get(sContext, sContext.getString(R.string.preference_marketing_date), sMarketingDate);
	}

	private static final Application.ActivityLifecycleCallbacks sOmnitureActivityCallbacks = new Application.ActivityLifecycleCallbacks() {
		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			// ignore
		}

		@Override
		public void onActivityStarted(Activity activity) {
			// ignore
		}

		@Override
		public void onActivityResumed(Activity activity) {
			Log.v(TAG, "onResume - " + activity.getClass().getSimpleName());
			ADMS_Measurement.sharedInstance(sContext).startActivity(sContext);
		}

		@Override
		public void onActivityPaused(Activity activity) {
			Log.v(TAG, "onPause - " + activity.getClass().getSimpleName());
			ADMS_Measurement.sharedInstance().stopActivity();
		}

		@Override
		public void onActivityStopped(Activity activity) {
			// ignore
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			// ignore
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			// ignore
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// HotelsV2 tracking
	//
	// https://confluence/display/Omniture/Mobile+App:+Hotel+Redesign+-+Android+Material
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String HOTELV2_LOB = "hotels";
	private static final String HOTELSV2_SEARCH_BOX = "App.Hotels.Dest-Search";
	private static final String HOTELSV2_RECENT_SEARCH_CLICK = "App.Hotels.DS.RecentSearch";
	private static final String HOTELSV2_GEO_SUGGESTION_CLICK = "App.Hotels.DS.DestSuggest";
	private static final String HOTELSV2_TRAVELER = "App.Hotels.Traveler.";
	private static final String HOTELSV2_RESULT = "App.Hotels.Search";
	private static final String HOTELSV2_NO_RESULT = "App.Hotels.Search.NoResults";
	private static final String HOTELSV2_SORT = "App.Hotels.Search.Sort.";
	private static final String HOTELSV2_SORT_PRICE_SLIDER = "App.Hotels.Search.Price";
	private static final String HOTELSV2_SEARCH_FILTER_VIP = "App.Hotels.Search.Filter.VIP.";
	private static final String HOTELSV2_SEARCH_FILTER_NEIGHBOURHOOD = "App.Hotels.Search.Neighborhood";
	private static final String HOTELSV2_SEARCH_FILTER_BY_NAME = "App.Hotels.Search.HotelName";
	private static final String HOTELSV2_CLEAR_FILTER = "App.Hotels.Search.ClearFilter";
	private static final String HOTELSV2_SEARCH_MAP = "App.Hotels.Search.Map";
	private static final String HOTELSV2_SEARCH_MAP_TO_LIST = "App.Hotels.Search.Expand.List";
	private static final String HOTELSV2_SEARCH_MAP_TAP_PIN = "App.Hotels.Search.TapPin";
	private static final String HOTELSV2_SEARCH_MAP_TAP_CAROUSAL = "App.Hotels.Search.Expand.Hotel";
	private static final String HOTELSV2_SEARCH_THIS_AREA = "App.Hotels.Search.AreaSearch";
	private static final String HOTELSV2_CAROUSEL_SCROLL = "App.Hotels.Search.ShowNext";
	private static final String HOTELSV2_DETAILS_PAGE = "App.Hotels.Infosite";
	private static final String HOTELSV2_SOLD_OUT_PAGE = "App.Hotels.Infosite.SoldOut";
	private static final String HOTELSV2_DETAILS_ETP = "App.Hotels.IS.Select.";
	private static final String HOTELSV2_DETAIL_VIEW_ROOM = "App.Hotels.IS.ViewRoom";
	private static final String HOTELSV2_DETAIL_ROOM_INFO = "App.Hotels.IS.MoreRoomInfo";
	private static final String HOTELSV2_DETAIL_ROOM_BOOK = "App.Hotels.IS.BookNow";
	private static final String HOTELSV2_DETAIL_MAP_VIEW = "App.Hotels.Infosite.Map";
	private static final String HOTELSV2_DETAIL_BOOK_PHONE = "App.Hotels.Infosite.BookPhone";
	private static final String HOTELSV2_DETAIL_SELECT_ROOM = "App.Hotels.Infosite.SelectRoom";
	private static final String HOTELSV2_MAP_SELECT_ROOM = "App.Hotels.IS.Map.SelectRoom";
	private static final String HOTELSV2_REVIEWS = "App.Hotels.Reviews";

	private static final String HOTELSV2_ETP_INFO = "App.Hotels.ETPInfo";
	private static final String HOTELSV2_RESORT_FEE_INFO = "App.Hotels.ResortFeeInfo";
	private static final String HOTELSV2_RENOVATION_INFO = "App.Hotels.RenovationInfo";
	private static final String HOTELSV2_TRAVELER_LINK_NAME = "Search Results Update";
	private static final String HOTELSV2_CHECKOUT_TRIP_SUMMARY = "App.Hotels.CKO.TripSummary";
	private static final String HOTELSV2_CHECKOUT_PRICE_CHANGE = "App.Hotels.CKO.PriceChange";
	private static final String HOTELSV2_CHECKOUT_TRAVELER_INFO = "App.Hotels.Checkout.Traveler.Edit.Info";
	private static final String HOTELSV2_CHECKOUT_SELECT_STORED_CARD = "App.Hotels.CKO.Payment.StoredCard";
	private static final String HOTELSV2_CHECKOUT_EDIT_PAYMENT = "App.Hotels.Checkout.Payment.Edit.Card";
	private static final String HOTELSV2_CHECKOUT_SLIDE_TO_PURCHASE = "App.Hotels.Checkout.SlideToPurchase";
	private static final String HOTELSV2_CHECKOUT_ERROR = "App.Hotels.Checkout.Error";
	private static final String HOTELSV2_PURCHASE_CONFIRMATION = "App.Hotels.Checkout.Confirmation";
	private static final String HOTELSV2_CONFIRMATION_ADD_CALENDAR = "App.Hotels.CKO.Confirm.CalenderAdd";
	private static final String HOTELSV2_CONFIRMATION_CALL_CUSTOMER_SUPPORT = "App.Hotels.CKO.Confirm.CallSupport";
	private static final String HOTELSV2_CONFIRMATION_DIRECTIONS = "App.Hotels.CKO.Confirm.Directions";
	private static final String HOTELSV2_CONFIRMATION_CROSS_SELL = "App.Hotels.CKO.Confirm.Xsell";
	private static final String HOTELSV2_CONFIRMATION_EXPAND_COUPON = "App.CKO.Coupon.Expand";
	private static final String HOTELSV2_CONFIRMATION_COUPON_SUCCESS = "App.CKO.Coupon.Success";
	private static final String HOTELSV2_CONFIRMATION_COUPON_FAIL = "App.CKO.Coupon.Fail";
	private static final String HOTELSV2_CONFIRMATION_COUPON_REMOVE = "App.CKO.Coupon.Remove";

	private static final String REWARDS_POINTS_UPDATE = "App.Hotels.CKO.Points.Update";
	private static final String PAY_WITH_POINTS_DISABLED = "App.Hotels.CKO.Points.None";
	private static final String PAY_WITH_POINTS_ERROR = "App.Hotels.CKO.Points.Error";
	private static final String SHOP_WITH_POINTS_TOGGLE_STATE = "App.Hotels.DS.SWP.";

	public enum OmnitureEventName {
		REWARD_PROGRAM_NAME,
		HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE,
		REWARD_APPLIED_PERCENTAGE_TEMPLATE,
		NO_REWARDS_USED,
		TOTAL_POINTS_BURNED,
		BRAND_KEY_FOR_OMNITURE
	}

	public static void trackHotelV2SearchBox(boolean swpIsVisibleAndToggleIsOn) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_BOX + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_SEARCH_BOX);
		s.setEvar(18, HOTELSV2_SEARCH_BOX);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelRecentSearchTest);

		//SWP is visible and toggle is ON, when user lands on Search Screen
		if (swpIsVisibleAndToggleIsOn) {
			s.setEvents("event118");
		}

		// Send the tracking data
		s.track();

	}

	public static void trackHotelRecentSearchClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_RECENT_SEARCH_CLICK + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, HOTELSV2_RECENT_SEARCH_CLICK);
		s.setProp(16, HOTELSV2_RECENT_SEARCH_CLICK);
		s.trackLink(null, "o", "Search Results Update", null, null);

	}

	public static void trackSwPToggle(boolean swpToggleState) {
		Log.d(TAG, "Tracking \"" + SHOP_WITH_POINTS_TOGGLE_STATE + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(SHOP_WITH_POINTS_TOGGLE_STATE + (swpToggleState ? "On" : "Off"));

		s.trackLink(null, "o", "Search Results Update", null, null);
	}

	public static void trackHotelTravelerPickerClick(String text) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_BOX + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, HOTELSV2_TRAVELER + text);
		s.setProp(16, HOTELSV2_TRAVELER + text);
		s.trackLink(null, "o", HOTELSV2_TRAVELER_LINK_NAME, null, null);
	}

	public static void trackGeoSuggestionClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_GEO_SUGGESTION_CLICK + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_GEO_SUGGESTION_CLICK);
		s.trackLink(null, "o", "Search Results Update", null, null);
	}

	public static void internalTrackHotelsV2Search(com.expedia.bookings.data.hotels.HotelSearchParams searchParams,
												   com.expedia.bookings.data.hotels.HotelSearchResponse searchResponse) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"" + HOTELSV2_RESULT + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_RESULT);
		s.setEvar(18, HOTELSV2_RESULT);
		s.setEvents("event12,event51");

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Region
		addHotelV2RegionId(s, searchParams);
		// Check in/check out date
		addHotelV2AdvancePurchaseWindow(s, searchParams);

		s.setEvar(47, getHotelV2Evar47String(searchParams));

		// prop and evar 5, 6
		setDateValues(s, searchParams.getCheckIn(), searchParams.getCheckOut());
		// Freeform location
		if (!TextUtils.isEmpty(searchParams.getSuggestion().regionNames.fullName)) {
			s.setEvar(48, searchParams.getSuggestion().regionNames.fullName);
		}

		// Number of search results
		if (searchResponse != null) {
			s.setProp(1, Integer.toString(searchResponse.hotelList.size()));
		}

		if (searchResponse != null) {
			// Has at least one sponsored Listing
			if (searchResponse.hotelList.get(0).isSponsoredListing) {
				s.setEvar(28, HOTELS_SEARCH_SPONSORED_PRESENT);
				s.setProp(16, HOTELS_SEARCH_SPONSORED_PRESENT);
			}
			else {
				s.setEvar(28, HOTELS_SEARCH_SPONSORED_NOT_PRESENT);
				s.setProp(16, HOTELS_SEARCH_SPONSORED_NOT_PRESENT);
			}
		}


		if (!ExpediaBookingApp.isDeviceShitty()) {
			//tracking for Map AB test
			trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelResultMapTest);
		}

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelSearchScreenSoldOutTest);
		trackAbacusTest(s, AbacusUtils.ExpediaAndroidAppAATestSep2015);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelsV2SuperlativeReviewsABTest);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelsMemberDealTest);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2NoResult() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_NO_RESULT + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_NO_RESULT);
		s.setEvar(18, HOTELSV2_NO_RESULT);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2SponsoredListingClick() {
		Log.d(TAG, "Tracking \"" + HOTELS_SPONSORED_LISTING_CLICK + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_SPONSORED_LISTING_CLICK);
		s.trackLink(null, "o", "Sponsored Click", null, null);

	}

	public static void trackHotelV2Filter() {
		Log.d(TAG, "Tracking \"" + HOTELS_SEARCH_REFINE + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELS_SEARCH_REFINE);
		s.setEvar(18, HOTELS_SEARCH_REFINE);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();

	}

	public static void trackHotelV2SortBy(String type) {
		String pageName = HOTELSV2_SORT + type;
		Log.d(TAG, "Tracking \"" + pageName + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.trackLink(null, "o", "Search Results Sort", null, null);
	}

	public static void trackHotelV2PriceSlider() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SORT_PRICE_SLIDER + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SORT_PRICE_SLIDER);
		s.trackLink(null, "o", "Search Results Sort", null, null);
	}

	public static void trackLinkHotelV2FilterRating(String rating) {
		String pageName = HOTELS_SEARCH_REFINE + "." + rating;
		Log.d(TAG, "Tracking \"" + pageName + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.trackLink(null, "o", "Search Results Sort", null, null);

	}

	public static void trackLinkHotelV2FilterVip(String state) {
		String pageName = HOTELSV2_SEARCH_FILTER_VIP + state;
		Log.d(TAG, "Tracking \"" + pageName + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.trackLink(null, "o", "Search Results Sort", null, null);
	}

	public static void trackLinkHotelV2FilterNeighbourhood() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_FILTER_NEIGHBOURHOOD + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_FILTER_NEIGHBOURHOOD);
		s.trackLink(null, "o", "Search Results Sort", null, null);
	}

	public static void trackLinkHotelV2FilterByName() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_FILTER_BY_NAME + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_FILTER_BY_NAME);
		s.trackLink(null, "o", "Search Results Sort", null, null);
	}

	public static void trackLinkHotelV2ClearFilter() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CLEAR_FILTER + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CLEAR_FILTER);
		s.trackLink(null, "o", "Search Results Sort", null, null);
	}

	public static void trackHotelV2SearchMap() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_SEARCH_MAP);
		s.setEvar(18, HOTELSV2_SEARCH_MAP);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();

	}

	public static void trackHotelV2MapToList() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP_TO_LIST + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_MAP_TO_LIST);
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackHotelV2MapTapPin() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP_TAP_PIN + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_MAP_TAP_PIN);
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackHotelV2CarouselClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP_TAP_CAROUSAL + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_MAP_TAP_CAROUSAL);
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackHotelV2AreaSearchClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_THIS_AREA + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_THIS_AREA);
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackHotelV2CarouselScroll() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CAROUSEL_SCROLL + "\" scroll...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CAROUSEL_SCROLL);
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackPageLoadHotelV2SoldOut() {
		String pageName = HOTELSV2_SOLD_OUT_PAGE;
		Log.d(TAG, "Tracking \"" + pageName + "\" pageload");

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelV2Infosite(HotelOffersResponse hotelOffersResponse, boolean isETPEligible,
													boolean isCurrentLocationSearch, boolean isHotelSoldOut, boolean isRoomSoldOut) {

		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAILS_PAGE + "\" pageload");

		ADMS_Measurement s = getFreshTrackingObject();

		final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

		LocalDate checkInDate = dtf.parseDateTime(hotelOffersResponse.checkInDate).toLocalDate();
		LocalDate checkOutDate = dtf.parseDateTime(hotelOffersResponse.checkOutDate).toLocalDate();

		s.setAppState(HOTELSV2_DETAILS_PAGE);
		s.setEvar(18, HOTELSV2_DETAILS_PAGE);

		String drrString = internalGenerateHotelV2DRRString(hotelOffersResponse);
		s.setEvar(9, drrString);

		if (isHotelSoldOut) {
			s.setEvents("event3,event14");
		}
		else if (isRoomSoldOut && isETPEligible) {
			s.setEvents("event3,event5,event18");
		}
		else if (isRoomSoldOut) {
			s.setEvents("event3,event18");
		}
		else if (isETPEligible) {
			s.setEvents("event3,event5");
		}
		else {
			s.setEvents("event3");
		}

		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);
		setDateValues(s, checkInDate, checkOutDate);

		String region;
		if (isCurrentLocationSearch) {
			region = "Current Location";
		}
		else {
			region = hotelOffersResponse.locationId;
		}
		s.setProp(4, region);
		s.setEvar(4, "D=c4");

		if (hotelOffersResponse.hotelRoomResponse != null) {
			addHotelV2Products(s, hotelOffersResponse.hotelRoomResponse.get(0), hotelOffersResponse.hotelId);
			if (hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.airAttached) {
				String products = s.getProducts();
				products += ";;;;eVar66=Flight:Hotel Infosite X-Sell";
				s.setProducts(products);

				String event = s.getEvents();
				event += ",event57";
				s.setEvents(event);
			}
		}
		// Send the tracking data
		s.track();
	}

	public static void trackLinkHotelV2EtpClick(String payType) {
		String pageName = HOTELSV2_DETAILS_ETP + payType;
		Log.d(TAG, "Tracking \"" + pageName + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, pageName);
		s.setProp(16, pageName);
		s.setEvar(52, payType);
		s.trackLink(null, "o", "ETP Selection", null, null);
	}

	public static void trackLinkHotelV2AirAttachEligible(HotelOffersResponse.HotelRoomResponse hotelRoomResponse, String hotelId) {
		String pageName = HOTELSV2_DETAILS_PAGE;
		Log.d(TAG, "Tracking \"" + pageName + "\" air attach...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvents("event58");

		addHotelV2Products(s, hotelRoomResponse, hotelId);
		s.setEvar(28, AIR_ATTACH_HOTEL_ADD);
		s.setProp(16, AIR_ATTACH_HOTEL_ADD);
		s.trackLink(null, "o", "Hotel Infosite", null, null);

	}

	public static void trackLinkHotelV2ViewRoomClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_VIEW_ROOM + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_VIEW_ROOM);
		s.trackLink(null, "o", "Room Info", null, null);
	}

	public static void trackLinkHotelV2RoomInfoClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_ROOM_INFO + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_ROOM_INFO);
		s.trackLink(null, "o", "Room Info", null, null);
	}

	public static void trackHotelV2RoomBookClick(HotelOffersResponse.HotelRoomResponse hotelRoomResponse, boolean hasETP) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_ROOM_BOOK + "\" pageLoad...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_ROOM_BOOK);

		if (!hasETP) {
			s.setEvar(52, "Non Etp");
		}
		else if (hotelRoomResponse.isPayLater) {
			if (hotelRoomResponse.depositRequired) {
				s.setEvar(52, "Pay Later Deposit");
			}
			else {
				s.setEvar(52, "Pay Later");
			}
		}
		else {
			s.setEvar(52, "Pay Now");
		}

		s.trackLink(null, "o", "Hotel Infosite", null, null);
	}

	public static void trackHotelV2DetailMapView() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_MAP_VIEW + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_DETAIL_MAP_VIEW);
		s.setEvar(18, HOTELSV2_DETAIL_MAP_VIEW);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackLinkHotelV2DetailBookPhoneClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_BOOK_PHONE + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_BOOK_PHONE);
		s.setEvents("event34");
		s.trackLink(null, "o", "Hotel Infosite", null, null);
	}

	public static void trackLinkHotelV2DetailSelectRoom() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_SELECT_ROOM + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_SELECT_ROOM);
		s.trackLink(null, "o", "Hotel Infosite", null, null);
	}

	public static void trackLinkHotelV2MapSelectRoom() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_MAP_SELECT_ROOM + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_MAP_SELECT_ROOM);
		s.trackLink(null, "o", "Infosite Map", null, null);
	}

	public static void trackHotelV2Reviews() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_REVIEWS + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_REVIEWS);
		s.setEvar(18, HOTELSV2_REVIEWS);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2ReviewsCategories(String category) {
		String pageName = HOTELSV2_REVIEWS + "." + category;
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad...");
		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.trackLink(null, "o", "Hotel Reviews", null, null);
	}

	public static void trackHotelV2EtpInfo() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_ETP_INFO + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_ETP_INFO);
		s.setEvar(18, HOTELSV2_ETP_INFO);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2ResortFeeInfo() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_RESORT_FEE_INFO + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_RESORT_FEE_INFO);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2RenovationInfo() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_RENOVATION_INFO + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_RENOVATION_INFO);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelV2CheckoutInfo(
		HotelCreateTripResponse trip,
		com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELS_CHECKOUT_INFO);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging);

		StringBuilder events = new StringBuilder("event70");
		if (trip.isRewardsRedeemable()) {
			events.append(",");
			events.append(ProductFlavorFeatureConfiguration.getInstance().getOmnitureEventValue(OmnitureEventName.HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE));
			BigDecimal amountPaidWithPoints = trip.getPointDetails().getMaxPayableWithPoints().getAmount().amount;
			BigDecimal totalAmount = trip.getTripTotalExcludingFee().amount;
			int percentagePaidWithPoints = NumberUtils.getPercentagePaidWithPointsForOmniture(amountPaidWithPoints,
				totalAmount);
			String rewardAppliedPercentage = ProductFlavorFeatureConfiguration.getInstance()
				.getOmnitureEventValue(OmnitureEventName.REWARD_APPLIED_PERCENTAGE_TEMPLATE);
			s.setEvar(53, String.format(Locale.getDefault(), rewardAppliedPercentage, percentagePaidWithPoints));
		}
		s.setEvents(events.toString());
		addHotelV2RegionId(s, searchParams);

		HotelCreateTripResponse.HotelProductResponse hotelProductResponse = trip.newHotelProductResponse;
		String supplierType = hotelProductResponse.hotelRoomResponse.supplierType;
		int numOfNights = JodaUtils.daysBetween(searchParams.getCheckIn(), searchParams.getCheckOut());
		String price = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total + "";


		if (TextUtils.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType;
		if (supplierType.length() > 1) {
			properCaseSupplierType = Strings.splitAndCapitalizeFirstLetters(supplierType);
		}
		else {
			properCaseSupplierType = supplierType;
		}
		s.setProducts(
			"Hotel;" + properCaseSupplierType + " Hotel:" + hotelProductResponse.hotelId + ";" + numOfNights + ";"
				+ price);


		addStandardHotelV2Fields(s, searchParams);
		if (hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.totalMandatoryFees != 0f
			|| hotelProductResponse.hotelRoomResponse.depositRequired) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelPriceBreakDownTest);
		}
		s.track();
	}

	public static void trackTripSummaryClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_TRIP_SUMMARY + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CHECKOUT_TRIP_SUMMARY);
		s.trackLink(null, "o", "Hotel Checkout", null, null);
	}

	public static void trackPriceChange(String priceChange) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_PRICE_CHANGE + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CHECKOUT_PRICE_CHANGE);
		s.setEvents("event62");
		s.setProp(9, "HOT|" + priceChange);
		s.trackLink(null, "o", "Hotel Checkout", null, null);
	}

	public static void trackHotelV2CheckoutTraveler() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_TRAVELER_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(HOTELSV2_CHECKOUT_TRAVELER_INFO);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelTravelerTest);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelShowExampleNamesTest);
		s.track();

	}

	public static void trackHotelV2StoredCardSelect() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_SELECT_STORED_CARD + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CHECKOUT_SELECT_STORED_CARD);
		s.trackLink(null, "o", "Hotel Checkout", null, null);
	}

	public static void trackHotelV2PaymentEdit() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_EDIT_PAYMENT + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(HOTELSV2_CHECKOUT_EDIT_PAYMENT);
		s.setEvar(18, HOTELSV2_CHECKOUT_EDIT_PAYMENT);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelCKOCreditDebitTest);
		s.track();
	}

	public static void trackHotelV2SlideToPurchase(PaymentType paymentType, PaymentSplitsType paymentSplitsType) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(HOTELSV2_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(18, HOTELSV2_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, getPaymentTypeOmnitureCode(paymentType, paymentSplitsType));
		s.track();

	}

	private static String getPaymentTypeOmnitureCode(PaymentType paymentType, PaymentSplitsType paymentSplitsType) {
		String paymentTypeOmnitureCode = null;
		switch (paymentSplitsType) {
		case IS_FULL_PAYABLE_WITH_CARD:
			paymentTypeOmnitureCode = paymentType.getOmnitureTrackingCode();
			break;
		case IS_FULL_PAYABLE_WITH_POINT:
			paymentTypeOmnitureCode = "Points";
			break;
		case IS_PARTIAL_PAYABLE_WITH_CARD:
			paymentTypeOmnitureCode = paymentType.getOmnitureTrackingCode() + " + Points";
			break;
		}
		if (paymentTypeOmnitureCode == null) {
			throw new IllegalArgumentException("Payment type omniture string should be initialized");
		}
		return paymentTypeOmnitureCode;
	}

	public static void trackHotelV2CheckoutPaymentCid() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_PAYMENT_CID);
	}

	public static void trackHotelV2CheckoutError(String errorType) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_ERROR + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		// set the pageName
		s.setAppState(HOTELSV2_CHECKOUT_ERROR);
		s.setEvar(18, HOTELSV2_CHECKOUT_ERROR);
		s.setEvents("event38");
		s.setProp(36, errorType);
		s.track();
	}

	public static void trackHotelV2CheckoutErrorRetry() {
		Log.d(TAG, "Tracking \"" + "App.Hotels.CKO.Error.Retry" + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent("App.Hotels.CKO.Error.Retry");
		s.trackLink(null, "o", "Hotel Checkout", null, null);
	}

	public static void trackHotelV2PurchaseConfirmation(HotelCheckoutResponse hotelCheckoutResponse, int percentagePaidWithPoints, String totalAppliedRewardCurrency) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_PURCHASE_CONFIRMATION + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELSV2_PURCHASE_CONFIRMATION);
		s.setEvents("purchase," + ProductFlavorFeatureConfiguration.getInstance().getOmnitureEventValue(
			OmnitureEventName.TOTAL_POINTS_BURNED) + "=" + totalAppliedRewardCurrency);

		// Product details
		DateTimeFormatter dtf = ISODateTimeFormat.basicDate();
		LocalDate checkInDate = new LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkInDate);
		LocalDate checkOutDate = new LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkOutDate);
		String checkIn = dtf.print(checkInDate);
		String checkOut = dtf.print(checkOutDate);
		s.setEvar(30, "Hotel:" + checkIn + "-" + checkOut + ":N");

		// Unique confirmation id
		// 14103: Remove timestamp from the purchaseID variable
		s.setProp(71, hotelCheckoutResponse.checkoutResponse.bookingResponse.travelRecordLocator);
		s.setProp(72, hotelCheckoutResponse.orderId);
		s.setProp(2, "hotels");
		s.setPurchaseID("onum" + hotelCheckoutResponse.orderId);

		s.setEvar(2, "D=c2");

		int numNights = JodaUtils.daysBetween(checkInDate, checkOutDate);
		String totalCost = hotelCheckoutResponse.totalCharges;
		String supplierType = hotelCheckoutResponse.checkoutResponse.bookingResponse.supplierType;
		if (Strings.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType = Strings.splitAndCapitalizeFirstLetters(supplierType);

		String products = "Hotel;" + properCaseSupplierType + " Hotel:" + hotelCheckoutResponse.checkoutResponse.productResponse.hotelId;

		products += ";" + numNights + ";" + totalCost;
		s.setProducts(products);

		// Currency code
		s.setCurrencyCode(hotelCheckoutResponse.currencyCode);

		s.setEvar(53, getPercentageOfAmountPaidWithPoints(percentagePaidWithPoints));

		// LX Cross sell
		boolean isLXEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.LX);
		if (isLXEnabled) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest);
		}

		// Send the tracking data
		s.track();
	}

	private static String getPercentageOfAmountPaidWithPoints(int percentagePaidWithPoints) {
		if (percentagePaidWithPoints == 0) {
			return ProductFlavorFeatureConfiguration.getInstance()
				.getOmnitureEventValue(OmnitureEventName.NO_REWARDS_USED);
		}
		else {
			return String.format(Locale.ENGLISH, ProductFlavorFeatureConfiguration.getInstance()
				.getOmnitureEventValue(OmnitureEventName.REWARD_APPLIED_PERCENTAGE_TEMPLATE), percentagePaidWithPoints);
		}
	}

	public static void trackHotelV2ConfirmationCalendar() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_ADD_CALENDAR + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_ADD_CALENDAR);
		s.trackLink(null, "o", "Confirmation Trip Action", null, null);
	}

	public static void trackHotelV2CallCustomerSupport() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_CALL_CUSTOMER_SUPPORT + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_CALL_CUSTOMER_SUPPORT);
		s.setEvents("event35");
		s.trackLink(null, "o", "Confirmation Trip Action", null, null);
	}

	public static void trackHotelV2ConfirmationDirection() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_DIRECTIONS + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_DIRECTIONS);
		s.trackLink(null, "o", "Confirmation Trip Action", null, null);
	}

	public static void trackHotelV2ConfirmationCrossSell(String typeOfBusiness) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_CROSS_SELL + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_CROSS_SELL);
		s.setEvar(12, "CrossSell.Hotels.Confirm." + typeOfBusiness);
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setProp(7, posTpid);

		s.trackLink(null, "o", "Confirmation Cross Sell", null, null);
	}

	public static void trackHotelV2ExpandCoupon() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_EXPAND_COUPON + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_EXPAND_COUPON);
		s.trackLink(null, "o", "CKO:Coupon Action", null, null);
	}

	public static void trackHotelV2CouponSuccess(String couponCode) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_COUPON_SUCCESS + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_COUPON_SUCCESS);
		s.setEvents("event21");
		s.setEvar(24, couponCode);
		s.trackLink(null, "o", "CKO:Coupon Action", null, null);
	}

	public static void trackHotelV2CouponFail(String couponCode, String errorMessage) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_COUPON_FAIL + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_COUPON_FAIL);
		s.setEvents("event22");
		s.setEvar(24, couponCode);
		s.setProp(36, errorMessage);
		s.trackLink(null, "o", "CKO:Coupon Action", null, null);
	}

	public static void trackHotelV2CouponRemove(String couponCode) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CONFIRMATION_COUPON_REMOVE + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_CONFIRMATION_COUPON_REMOVE);
		s.setEvar(24, couponCode);
		s.trackLink(null, "o", "CKO:Coupon Action", null, null);
	}


	private static void addHotelV2Products(ADMS_Measurement s, HotelOffersResponse.HotelRoomResponse hotelRoomResponse, String hotelId) {
		// The "products" field uses this format:
		// Hotel;<supplier> Hotel:<hotel id>

		// Determine supplier type
		String supplierType = "";
		supplierType = hotelRoomResponse.supplierType;


		if (TextUtils.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType;
		if (supplierType.length() > 1) {
			properCaseSupplierType = Strings.capitalizeFirstLetter(supplierType);
		}
		else {
			properCaseSupplierType = supplierType;
		}
		s.setProducts("Hotel;" + properCaseSupplierType + " Hotel:" + hotelId);
	}

	private static void addStandardHotelV2Fields(ADMS_Measurement s, com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		s.setEvar(2, HOTELV2_LOB);
		s.setProp(2, HOTELV2_LOB);
		s.setEvar(6, Integer.toString(JodaUtils.daysBetween(searchParams.getCheckIn(), searchParams.getCheckOut())));
		internalSetHotelV2DateProps(s, searchParams);
	}

	private static void internalSetHotelV2DateProps(ADMS_Measurement s, com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		LocalDate checkInDate = searchParams.getCheckIn();
		LocalDate checkOutDate = searchParams.getCheckOut();
		setDateValues(s, checkInDate, checkOutDate);
	}


	private static void addHotelV2RegionId(ADMS_Measurement s, com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		String region;
		if (searchParams.getSuggestion().isCurrentLocationSearch()) {
			region = "Current Location";
		}
		else {
			region = searchParams.getSuggestion().gaiaId;
		}
		s.setProp(4, region);
		s.setEvar(4, "D=c4");
	}

	private static void addHotelV2AdvancePurchaseWindow(ADMS_Measurement s,
														com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		String window = Integer.toString(JodaUtils.daysBetween(LocalDate.now(), searchParams.getCheckIn()));
		s.setEvar(5, window);
		s.setProp(5, window);
	}

	private static String getHotelV2Evar47String(com.expedia.bookings.data.hotels.HotelSearchParams params) {
		StringBuilder sb = new StringBuilder("HOT|A");
		sb.append(params.getAdults());
		sb.append("|C");
		sb.append(params.getChildren().size());
		return sb.toString();
	}

	private static String internalGenerateHotelV2DRRString(HotelOffersResponse hotelOffersResponse) {
		if (hotelOffersResponse != null && CollectionUtils.isNotEmpty(hotelOffersResponse.hotelRoomResponse)) {
			HotelOffersResponse.HotelRoomResponse firstRoomDetails = hotelOffersResponse.hotelRoomResponse.get(0);
			if (Strings.isNotEmpty(firstRoomDetails.promoDescription)) {
				return "Hotels | " + firstRoomDetails.promoDescription;
			}
		}
		return null;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Hotels tracking
	//
	// There does not appear to be an official spec for hotels tracking...
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String HOTELS_DETAILS_REVIEWS = "App.Hotels.Reviews";
	private static final String HOTELS_CHECKOUT_INFO = "App.Hotels.Checkout.Info";
	private static final String HOTELS_CHECKOUT_LOGIN = "App.Hotels.Checkout.Login";
	private static final String HOTELS_CHECKOUT_WARSAW = "App.Hotels.Checkout.Warsaw";
	private static final String HOTELS_CHECKOUT_PAYMENT_CID = "App.Hotels.Checkout.Payment.CID";
	private static final String HOTELS_SEARCH_REFINE = "App.Hotels.Search.Filter";
	private static final String HOTELS_SEARCH_REFINE_NAME = "App.Hotels.Search.Filter.Name";
	private static final String HOTELS_SEARCH_REFINE_PRICE_RANGE = "App.Hotels.Search.Filter.PriceRange";
	private static final String HOTELS_SEARCH_REFINE_SEARCH_RADIUS = "App.Hotels.Search.Filter.SearchRadius";
	private static final String HOTELS_SEARCH_REFINE_VIP = "App.Hotels.Search.Filter.VIPAccess";
	private static final String HOTELS_CONF_CROSSSELL_FLIGHTS = "CrossSell.Hotels.Flights";
	private static final String HOTELS_CONF_ADD_TO_CALENDAR = "App.Hotels.Checkout.Confirmation.Add.Calendar";
	private static final String HOTELS_CONF_SHARE_EMAIL = "App.Hotels.Checkout.Confirmation.Share.Mail";

	public static final String HOTELS_SEARCH_SORT_POPULAR = "App.Hotels.Search.Sort.Popular";
	public static final String HOTELS_SEARCH_SORT_PRICE = "App.Hotels.Search.Sort.Price";
	public static final String HOTELS_SEARCH_SORT_DISTANCE = "App.Hotels.Search.Sort.Distance";
	public static final String HOTELS_SEARCH_SORT_RATING = "App.Hotels.Search.Sort.Rating";
	public static final String HOTELS_SEARCH_SORT_DEALS = "App.Hotels.Search.Sort.Deals";

	public static final String HOTELS_SEARCH_SPONSORED_PRESENT = "App.Hotels.Search.Sponsored.Yes";
	public static final String HOTELS_SEARCH_SPONSORED_NOT_PRESENT = "App.Hotels.Search.Sponsored.No";
	public static final String HOTELS_SPONSORED_LISTING_CLICK = "App.Hotels.Search.Sponsored.Click";

	public static final String HOTELS_REFINE_REVIEWS_FAV = "App.Hotels.Review.Fav";
	public static final String HOTELS_REFINE_REVIEWS_CRIT = "App.Hotels.Review.Crit";
	public static final String HOTELS_REFINE_REVIEWS_RECENT = "App.Hotels.Review.Recent";

	public static final String HOTELS_ETP_TOGGLE_LINK_NAME = "ETP Toggle";
	public static final String HOTELS_ETP_TOGGLE_PAY_LATER = "App.Hotels.RR.Toggle.PayLater";
	public static final String HOTELS_ETP_TOGGLE_PAY_NOW = "App.Hotels.RR.Toggle.PayNow";
	public static final String HOTELS_ETP_PAYMENT = "App.Hotels.RR.ETP";

	//////////////////////////////
	// Coupon tracking
	public static final String HOTELS_COUPON_LINK_NAME = "CKO:Coupon Action";
	public static final String HOTELS_COUPON_EXPAND = "App.CKO.Coupon.Expand";
	public static final String HOTELS_COUPON_SUCCESS = "App.CKO.Coupon.Success";
	public static final String HOTELS_COUPON_REMOVE = "App.CKO.Coupon.Remove";
	public static final String HOTELS_COUPON_FAIL = "App.CKO.Coupon.Fail";

	public static final String HOTELS = "App.Hotels";

	public static void trackPageLoadHotelsInfosite(Context context) {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState("App.Hotels.Rooms.Infosite");
		s.setEvents("event3");

		addStandardHotelFields(s, Db.getHotelSearch().getSearchParams());
		addHotelRegionId(s, Db.getHotelSearch().getSearchParams());

		Property property = Db.getHotelSearch().getSelectedProperty();

		// Products
		// Sometimes we load the infosite when we don't have rate info. In that case,
		// don't add air attach products.
		if (property.getLowestRate() != null && property.getLowestRate().isAirAttached()) {
			addEventsAndProductsForAirAttach(s, property, "event57", "Flight:Hotel Infosite X-Sell");
		}
		else {
			addProducts(s, property);
		}

		// 4761 - AB Test: Collapse Amenities, Policies, and fees on Infosite
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelCollapseAmenities);

		// Send the tracking data
		s.track();
	}

	private static void addProducts(ADMS_Measurement s, Property property) {
		// The "products" field uses this format:
		// Hotel;<supplier> Hotel:<hotel id>

		// Determine supplier type
		String supplierType = property.getSupplierType();
		if (TextUtils.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType;
		if (supplierType.length() > 1) {
			properCaseSupplierType = supplierType.substring(0, 1).toUpperCase(Locale.US) + supplierType.substring(1).toLowerCase(Locale.US);
		}
		else {
			properCaseSupplierType = supplierType;
		}
		s.setProducts("Hotel;" + properCaseSupplierType + " Hotel:" + property.getPropertyId());
	}

	private static void addProducts(ADMS_Measurement s, Property property, int numNights, double totalCost) {
		addProducts(s, property);

		DecimalFormat df = new DecimalFormat("#.##");
		String products = s.getProducts();
		products += ";" + numNights + ";" + df.format(totalCost);
		s.setProducts(products);
	}

	private static void addProducts(ADMS_Measurement s, Property property, String supplierType) {
		// The "products" field uses this format:
		// Hotel;<supplier> Hotel:<hotel id>

		if (TextUtils.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType = Strings.splitAndCapitalizeFirstLetters(supplierType);

		s.setProducts("Hotel;" + properCaseSupplierType + " Hotel:" + property.getPropertyId());
	}

	private static void addEventsAndProductsForAirAttach(ADMS_Measurement s, Property property, String eventVar,
														 String evar66Val) {
		addProducts(s, property);
		String products = s.getProducts();
		products += String.format(";;;;eVar66=%s", evar66Val);
		s.setProducts(products);

		String eventsStr = s.getEvents();
		if (TextUtils.isEmpty(eventsStr)) {
			eventsStr = eventVar;
		}
		else {
			eventsStr += ",";
			eventsStr += eventVar;
		}
		s.setEvents(eventsStr);
	}

	public static void trackPageLoadHotelsDetailsReviews() {
		internalTrackPageLoadEventStandard(HOTELS_DETAILS_REVIEWS, LineOfBusiness.HOTELS);
	}

	public static void trackLinkReviewTypeSelected(String linkName) {
		internalTrackLink(linkName);
	}

	public static void trackPageLoadHotelsLogin() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_LOGIN);
	}

	public static void trackLinkHotelRefineName(String refinement) {
		String link = HOTELS_SEARCH_REFINE_NAME + "." + refinement;
		internalTrackLink(link);
	}

	public static void trackLinkHotelRefinePriceRange(PriceRange priceRange) {
		String link = HOTELS_SEARCH_REFINE_PRICE_RANGE;

		switch (priceRange) {
		case CHEAP: {
			link += ".1$";
			break;
		}
		case MODERATE: {
			link += ".2$";
			break;
		}
		case EXPENSIVE: {
			link += ".3$";
			break;
		}
		case ALL:
		default: {
			link += ".All";
			break;
		}
		}

		internalTrackLink(link);
	}

	public static void trackLinkHotelRefineSearchRadius(SearchRadius searchRadius) {
		String link = HOTELS_SEARCH_REFINE_SEARCH_RADIUS;

		if (searchRadius != HotelFilter.SearchRadius.ALL) {
			final DistanceUnit distanceUnit = DistanceUnit.getDefaultDistanceUnit();
			final String unitString = distanceUnit.equals(DistanceUnit.MILES) ? "mi" : "km";

			link += "." + new DecimalFormat("##.#").format(searchRadius.getRadius(distanceUnit)) + unitString;
		}
		else {
			link += ".All";
		}

		internalTrackLink(link);
	}

	public static void trackLinkHotelRefineRating(String rating) {
		String link = HOTELS_SEARCH_REFINE + "." + rating;
		internalTrackLink(link);
	}

	public static void trackLinkHotelRefineVip(boolean enabled) {
		String pageName = enabled ? HOTELS_SEARCH_REFINE_VIP + ".On" : HOTELS_SEARCH_REFINE_VIP + ".Off";
		internalTrackLink(pageName);
	}

	public static void trackLinkHotelSort(String pageName) {
		internalTrackLink(pageName);
	}


	public static void trackPageLoadHotelsCheckoutWarsaw() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_WARSAW);
	}

	// Coupon tracking: https://mingle/projects/eb_ad_app/cards/1003

	private static void addCouponFields(ADMS_Measurement s, String refererId) {
		s.setEvar(28, refererId);
		s.setProp(16, refererId);
		s.trackLink(null, "o", HOTELS_COUPON_LINK_NAME, null, null);
	}

	public static void trackHotelCouponExpand() {
		Log.d(TAG, "Tracking \"" + HOTELS_COUPON_EXPAND + "\" click");
		ADMS_Measurement s = getFreshTrackingObject();
		addCouponFields(s, HOTELS_COUPON_EXPAND);
	}

	public static void trackHotelCouponApplied(String couponCode) {
		Log.d(TAG, "Tracking \"" + HOTELS_COUPON_SUCCESS + "\" click");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvents("event21");
		s.setEvar(24, couponCode);
		addCouponFields(s, HOTELS_COUPON_SUCCESS);
	}

	public static void trackHotelCouponRemoved() {
		Log.d(TAG, "Tracking \"" + HOTELS_COUPON_REMOVE + "\" click");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(24, "Coupon Removed");
		addCouponFields(s, HOTELS_COUPON_REMOVE);
	}

	public static void trackHotelCouponFail(String couponCode, String errorCode) {
		Log.d(TAG, "Tracking \"" + HOTELS_COUPON_FAIL + "\" click");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvents("event22");
		s.setEvar(24, couponCode);
		s.setProp(36, errorCode);
		addCouponFields(s, HOTELS_COUPON_FAIL);
	}

	public static void trackHotelConfirmationFlightsXSell() {
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_CONF_CROSSSELL_FLIGHTS);
		s.setEvar(12, HOTELS_CONF_CROSSSELL_FLIGHTS);
		internalTrackLink(s);
	}

	public static void trackHotelConfirmationAddToCalendar() {
		internalTrackLink(HOTELS_CONF_ADD_TO_CALENDAR);
	}

	public static void trackHotelConfirmationShareEmail() {
		internalTrackLink(HOTELS_CONF_SHARE_EMAIL);
	}

	public static void trackHotelSponsoredListingClick() {
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_SPONSORED_LISTING_CLICK);
		addPOSTpid(s);
		internalTrackLink(s);
	}

	public static void trackHotelETPPayToggle(boolean isPayLater) {
		String refererId = isPayLater ? HOTELS_ETP_TOGGLE_PAY_LATER : HOTELS_ETP_TOGGLE_PAY_NOW;
		ADMS_Measurement s = createTrackLinkEvent(refererId);
		addPOSTpid(s);

		s.trackLink(null, "o", HOTELS_ETP_TOGGLE_LINK_NAME, null, null);
	}

	public static void trackHotelETPRoomSelected(boolean isPayLater) {
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_ETP_PAYMENT);
		addPOSTpid(s);
		if (isPayLater) {
			s.setEvar(52, "Pay Later");
		}
		else {
			s.setEvar(52, "Pay Now");
		}
		s.trackLink(null, "o", HOTELS_ETP_TOGGLE_LINK_NAME, null, null);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights Tracking
	//
	// Spec: http://confluence/display/Omniture/Mobile+App+Flight+Tracking
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String FLIGHT_SEARCH = "App.Flight.Search";
	private static final String FLIGHT_SEARCH_V2 = "App.Flight.Dest-Search";
	private static final String FLIGHT_RECENT_SEARCH_V2 = "App.Flight.DS.RecentSearch";
	private static final String FLIGHTS_V2_TRAVELER_CHANGE_PREFIX = "App.Flight.DS.";
	private static final String FLIGHTS_V2_TRAVELER_LINK_NAME = "Search Results Update";
	private static final String FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK = "App.Flight.Search.BaggageFee";
	private static final String FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK = "App.Flight.Search.PaymentFee";
	private static final String FLIGHT_SEARCH_INTERSTITIAL = "App.Flight.Search.Interstitial";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT = "App.Flight.Search.Roundtrip.Out";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS = "App.Flight.Search.Roundtrip.Out.Details";
	private static final String FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.Out.BaggageFee";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN = "App.Flight.Search.Roundtrip.In";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS = "App.Flight.Search.Roundtrip.In.Details";
	private static final String FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.In.BaggageFee";
	private static final String FLIGHT_RATE_DETAILS = "App.Flight.RateDetails";
	private static final String FLIGHT_CHECKOUT_INFO = "App.Flight.Checkout.Info";
	private static final String FLIGHT_CHECKOUT_LOGIN = "App.Flight.Checkout.Login";
	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT = "App.Flight.Checkout.Traveler.Select";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO = "App.Flight.Checkout.Traveler.Edit.Info";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS = "App.Flight.Checkout.Traveler.Edit.Details";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT = "App.Flight.Checkout.Traveler.Edit.Passport";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE = "App.Flight.Checkout.Traveler.Edit.Save";
	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING = "App.Flight.Checkout.Traveler.Select.Existing";
	private static final String FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY = "App.Flight.Checkout.Traveler.EnterManually";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING = "App.Flight.Checkout.Payment.Select.Existing";
	private static final String FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY = "App.Flight.Checkout.Payment.EnterManually";
	private static final String FLIGHT_CHECKOUT_WARSAW = "App.Flight.Checkout.Warsaw";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT = "App.Flight.Checkout.Payment.Select";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS = "App.Flight.Checkout.Payment.Edit.Address";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD = "App.Flight.Checkout.Payment.Edit.Card";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE = "App.Flight.Checkout.Payment.Edit.Save";
	private static final String FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE = "App.Flight.Checkout.SlideToPurchase";
	private static final String FLIGHT_CHECKOUT_PAYMENT_CID = "App.Flight.Checkout.Payment.CID";
	private static final String FLIGHT_CHECKOUT_CONFIRMATION = "App.Flight.Checkout.Confirmation";

	private static final String FLIGHT_SEARCH_RESULTS_ONE_WAY = "App.Flight.Search.OneWay";
	private static final String PREFIX_FLIGHT_SEARCH_ONE_WAY_SELECT = "App.Flight.Search.OneWay.Select";
	private static final String FLIGHT_SEARCH_ONE_WAY_REFINE = "App.Flight.Search.OneWay.RefineSearch";
	private static final String FLIGHT_SEARCH_ONE_WAY_DETAILS = "App.Flight.Search.OneWay.Details";
	private static final String FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE = "App.Flight.Search.OneWay.BaggageFee";

	private static final String FLIGHT_ERROR_NOT_YET_AVAILABLE = "App.Flight.Error.NotYetAvailable";
	private static final String FLIGHT_ERROR_CHECKOUT = "App.Flight.Error.Checkout";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET = "App.Flight.Error.Checkout.Payment.PriceChange.Ticket";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED = "App.Flight.Error.Checkout.Payment.Failed";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV = "App.Flight.Error.Checkout.Payment.CVV";
	private static final String FLIGHT_ERROR_SOLD_OUT = "App.Flight.Error.SoldOut";
	private static final String FLIGHT_ERROR_SEARCH_EXPIRED = "App.Flight.Error.Search.Expired";

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT = "App.Flight.Search.Roundtrip.Out.Select";
	private static final String PREFIX_FLIGHT_SEARCH_SORT = "App.Flight.Search.Sort";
	private static final String PREFIX_FLIGHT_SEARCH_FILTER = "App.Flight.Search.Filter";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE = "App.Flight.Search.Roundtrip.Out.RefineSearch";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT = "App.Flight.Search.Roundtrip.In.Select";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE = "App.Flight.Search.Roundtrip.In.RefineSearch";

	private static final String FLIGHT_CONF_ADD_TO_CALENDAR = "App.Flight.Checkout.Confirmation.Add.Calendar";
	private static final String FLIGHT_CONF_SHARE_EMAIL = "App.Flight.Checkout.Confirmation.Share.Mail";

	private static final String FLIGHT_INFANT_ALERT = "App.Flight.Search.LapAlert";

	public static void trackPageLoadFlightCheckoutConfirmation() {
		String pageName = FLIGHT_CHECKOUT_CONFIRMATION;
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		FlightTrip newestFlightOffer = getNewestFlightOffer();
		boolean isSplitTicket = newestFlightOffer.isSplitTicket();

		// Flight: <departure Airport Code>-<Destination Airport Code>:<departure date YYYYMMDD>-<return date YYYYMMDD>:<promo code applied N/Y>
		FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
		String origin = searchParams.getDepartureLocation().getDestinationId();
		String dest = searchParams.getArrivalLocation().getDestinationId();

		String eVar30 = "Flight:";
		eVar30 += origin;
		eVar30 += "-";
		eVar30 += dest;
		eVar30 += ":";

		DateTimeFormatter dtf = ISODateTimeFormat.basicDate();
		eVar30 += dtf.print(searchParams.getDepartureDate());
		if (searchParams.isRoundTrip()) {
			eVar30 += "-";
			eVar30 += dtf.print(searchParams.getReturnDate());
		}

		eVar30 += ":N";
		s.setEvar(30, eVar30);

		if (isSplitTicket) {
			addFlightSplitTicketInfo(s, pageName, newestFlightOffer, true, true);
		}
		else {
			addProducts(s);
		}

		s.setCurrencyCode(newestFlightOffer.getTotalFare().getCurrency());
		s.setEvents("purchase");

		// order number with an "onum" prefix, described here: http://confluence/pages/viewpage.action?pageId=419913476
		final String orderId = Db.getTripBucket().getFlight().getCheckoutResponse().getOrderId();
		s.setPurchaseID("onum" + orderId);

		// TRL
		Itinerary itin = Db.getTripBucket().getFlight().getItinerary();
		s.setProp(71, itin.getItineraryNumber());

		// order #
		s.setProp(72, orderId);

		s.track();
	}

	private static void addProducts(ADMS_Measurement s) {
		// products variable, described here: http://confluence/display/Omniture/Product+string+format
		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
		String airlineCode = trip.getLeg(0).getPrimaryAirlines().iterator().next();
		String tripType = getOmnitureStringCodeRepresentingTripTypeByNumLegs(trip.getLegCount());
		String numTravelers = Integer.toString(Db.getTripBucket().getFlight().getFlightSearchParams().getNumAdults());
		String price = trip.getTotalFare().getAmount().toString();

		s.setProducts("Flight;Agency Flight:" + airlineCode + ":" + tripType + ";" + numTravelers + ";" + price);
	}

	public static void trackPageLoadFlightCheckoutPaymentCid() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_PAYMENT_CID);
	}

	public static void trackPageLoadFlightCheckoutSlideToPurchase() {
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, getPaymentType());
		s.track();
	}

	public static void trackPageLoadFlightCheckoutPaymentEditSave() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditCard() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditAddress() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS);
	}

	public static void trackPageLoadFlightCheckoutPaymentSelect() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPageLoadFlightCheckoutWarsaw() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_WARSAW);
	}

	public static void trackPageLoadFlightTravelerEditSave() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE);
	}

	public static void trackPageLoadFlightTravelerEditPassport() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT);
	}

	public static void trackPageLoadFlightTravelerEditDetails() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS);
	}

	public static void trackPageLoadFlightTravelerEditInfo() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO);
	}

	public static void trackPageLoadFlightTravelerSelect() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_TRAVELER_SELECT);
	}

	public static void trackPageLoadFlightLogin() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_LOGIN);
	}

	public static void trackPageLoadFlightCheckoutInfo() {
		String pageName = FLIGHT_CHECKOUT_INFO;
		FlightTrip searchFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.setEvents("event71");
		FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
		s.setEvar(47, getEvar47String(params));

		String origin = params.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);
		String dest = params.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		internalSetFlightDateProps(s, params);
		addStandardFlightFields(s);

		if (searchFlightTrip.isSplitTicket()) {
			addFlightSplitTicketInfo(s, pageName, searchFlightTrip, false, false);
		}
		else {
			addProducts(s);
		}
		s.track();
	}

	private static FlightTrip getNewestFlightOffer() {
		TripBucketItemFlight tripBucketItemFlight = Db.getTripBucket().getFlight();
		FlightTrip createTripRespOffer = tripBucketItemFlight.getItineraryResponse().getOffer();
		FlightCheckoutResponse checkoutResponse = tripBucketItemFlight.getCheckoutResponse();
		FlightTrip checkoutRespOffer = null;
		if (checkoutResponse != null) {
			checkoutRespOffer = checkoutResponse.getNewOffer();
		}
		return (checkoutRespOffer != null) ? checkoutRespOffer : createTripRespOffer;
	}

	private static void addFlightSplitTicketInfo(ADMS_Measurement s, String pageName, FlightTrip flightTrip, boolean withTotalRevenue, boolean withPassengerCount) {
		boolean isSplitTicket = flightTrip.isSplitTicket();
		if (isSplitTicket) {
			StringBuilder productsSB = new StringBuilder();
			int legCount = 0;
			for (FlightLeg flightLeg : flightTrip.getLegs()) {
				// fetch airline code from flight search response. We don't parse it from createTrip
				String firstAirlineCodeForLeg = getAirlineCodeForLegId(flightLeg.getLegId());

				productsSB.append("Flight;Agency Flight:");
				productsSB.append(firstAirlineCodeForLeg)
					.append(":ST")
					.append(";");

				if (withPassengerCount) {
					productsSB.append(flightTrip.getPassengerCount());
				}
				productsSB.append(";");

				if (withTotalRevenue) {
					String totalRevenue = flightLeg.getTotalFare().getAmount().toString();
					productsSB.append(totalRevenue);
				}
				if (legCount == 0) {
					productsSB.append(",");
				}
				legCount++;
			}

			s.setEvar(18, pageName);
			s.setProducts(productsSB.toString());
		}
	}

	private static String getAirlineCodeForLegId(String legId) {
		FlightTrip searchRespFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
		for (FlightLeg searchFlightLeg : searchRespFlightTrip.getLegs()) {
			if (searchFlightLeg.getLegId().equals(legId)) {
				return searchFlightLeg.getFirstAirlineCode();
			}
		}
		return "";
	}

	public static void trackPageLoadFlightRateDetailsOverview() {
		String pageName = FLIGHT_RATE_DETAILS;
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		FlightTrip searchFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
		ADMS_Measurement s = createTrackPageLoadEventPriceChange(pageName);
		addFlightSplitTicketInfo(s, pageName, searchFlightTrip, false, false);
		s.setEvents("event4");

		s.track();
	}

	public static void trackPageLoadFlightSearchResults(int legPosition) {
		if (legPosition == 0) {
			// Note: according the spec we want only to track the FlightSearchResults if it represents a new set of data
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				OmnitureTracking.trackPageLoadFlightSearchResultsOutboundList();
			}
			else {
				OmnitureTracking.trackPageLoadFlightSearchResultsOneWay();
			}
		}

		// According to spec, we want to track the inbound list as many times as the user rotates device, etc...
		else if (legPosition == 1) {
			OmnitureTracking.trackPageLoadFlightSearchResultsInboundList();
		}
	}

	private static void trackPageLoadFlightSearchResultsOutboundList() {

		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_ROUNDTRIP_OUT + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_SEARCH_ROUNDTRIP_OUT);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsNumberOfTicketsUrgencyTest);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		s.setEvents("event30,event54");
		// Search Type: value always 'Flight'
		s.setEvar(2, "Flight");
		s.setProp(2, "Flight");

		// Search Origin: 3 letter airport code of origin
		String origin = searchParams.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = searchParams.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		// day computation date
		LocalDate departureDate = searchParams.getDepartureDate();
		LocalDate returnDate = searchParams.getReturnDate();

		setDateValues(s, departureDate, returnDate);

		s.setEvar(47, getEvar47String(searchParams));

		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsRoundtripMessageTest);
		s.track();
	}

	private static void trackPageLoadFlightLegDetails(String nameOfPage) {
		Log.d(TAG, "Tracking \"" + nameOfPage + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(nameOfPage);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		// Search Type: value always 'Flight'
		s.setEvar(2, "Flight");
		s.setProp(2, "Flight");

		// Search Origin: 3 letter airport code of origin
		String origin = searchParams.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = searchParams.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		// day computation date
		LocalDate departureDate = searchParams.getDepartureDate();
		LocalDate returnDate = searchParams.getReturnDate();

		setDateValues(s, departureDate, returnDate);

		s.setEvar(47, getEvar47String(searchParams));

		s.track();
	}

	private static void trackPageLoadFlightSearchResultsInboundList() {
		if (mTrackPageLoadFromFSRA) {
			internalTrackPageLoadEventStandard(FLIGHT_SEARCH_ROUNDTRIP_IN);
		}
	}

	private static void trackPageLoadFlightSearchResultsOneWay() {

		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_RESULTS_ONE_WAY + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_SEARCH_RESULTS_ONE_WAY);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsNumberOfTicketsUrgencyTest);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		// Search Type: value always 'Flight'
		s.setEvar(2, "Flight");
		s.setProp(2, "Flight");

		// Search Origin: 3 letter airport code of origin
		String origin = searchParams.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = searchParams.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		setDateValues(s, searchParams.getDepartureDate(), searchParams.getReturnDate());

		s.setEvar(47, getEvar47String(searchParams));

		// Success event for 'Search'
		s.setEvents("event30,event54");

		s.track();
	}

	public static void trackPageLoadFlightBaggageFeeOneWay() {
		internalTrackPageLoadEventStandard(FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE);
	}

	public static void trackPageLoadFlightBaggageFeeOutbound() {
		internalTrackPageLoadEventStandard(FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE);
	}

	public static void trackPageLoadFlightBaggageFeeInbound() {
		internalTrackPageLoadEventStandard(FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE);
	}

	private static boolean mTrackPageLoadFromFSRA = true;

	public static void setPageLoadTrackingFromFSRAEnabled(boolean trackingEnabled) {
		Log.d("OmnitureTracking", "set FSRA tracking: " + trackingEnabled);
		mTrackPageLoadFromFSRA = trackingEnabled;
	}

	public static void trackPageLoadFlightSearchResultsDetails(int legPosition) {
		if (mTrackPageLoadFromFSRA) {
			if (legPosition == 0) {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					trackPageLoadFlightLegDetails(FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS);
				}
				else {
					trackPageLoadFlightLegDetails(FLIGHT_SEARCH_ONE_WAY_DETAILS);
				}
			}
			else if (legPosition == 1) {
				trackPageLoadFlightLegDetails(FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS);
			}
		}
	}

	public static void trackPageLoadFlightSearchResultsPlaneLoadingFragment() {
		internalTrackPageLoadEventStandard(FLIGHT_SEARCH_INTERSTITIAL);
	}

	public static void trackPageLoadFlightSearch() {
		internalTrackPageLoadEventStandard(FLIGHT_SEARCH);
	}

	public static void trackFlightTravelerPickerClick(String actionLabel) {
		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_V2 + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_TRAVELER_CHANGE_PREFIX + actionLabel);
		s.setProp(16, FLIGHTS_V2_TRAVELER_CHANGE_PREFIX + actionLabel);
		s.trackLink(null, "o", FLIGHTS_V2_TRAVELER_LINK_NAME, null, null);
	}

	public static void trackFlightRecentSearchClick() {
		Log.d(TAG, "Tracking \"" + FLIGHT_RECENT_SEARCH_V2 + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHT_RECENT_SEARCH_V2);
		s.setProp(16, FLIGHT_RECENT_SEARCH_V2);
		s.trackLink(null, "o", "Search Results Update", null, null);
	}

	public static void trackFlightBaggageFeesClick() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK);
		s.setProp(16, FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK);
		addPOSTpid(s);
		s.trackLink(null, "o", "Flight Baggage Fee", null, null);
	}

	public static void trackFlightPaymentFeesClick() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK);
		s.setProp(16, FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK);
		addPOSTpid(s);
		s.trackLink(null, "o", "", null, null);
	}

	private static void addPOSTpid(ADMS_Measurement s) {
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setEvar(61, posTpid);
		s.setProp(7, posTpid);
	}

	public static void trackPageLoadFlightSearchV2() {
		internalTrackPageLoadEventStandard(FLIGHT_SEARCH_V2);
	}

	public static void trackLinkFlightSearchSelect(int selectPos, int legPos) {
		String prefix = "";

		if (legPos == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT;
			}
			else {
				prefix = PREFIX_FLIGHT_SEARCH_ONE_WAY_SELECT;
			}
		}
		else if (legPos == 1) {
			prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT;
		}

		FlightFilter filter = Db.getFlightSearch().getFilter(legPos);
		String link = prefix + "." + filter.getSort().name() + "." + Integer.toString(selectPos);

		internalTrackLink(link);
	}

	public static void trackLinkFlightRefine(int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackLink(FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE);
			}
			else {
				internalTrackLink(FLIGHT_SEARCH_ONE_WAY_REFINE);
			}
		}
		else if (legPosition == 1) {
			internalTrackLink(FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE);
		}
	}

	public static void trackLinkFlightSort(String sortType) {
		String link = PREFIX_FLIGHT_SEARCH_SORT + "." + sortType;
		internalTrackLink(link);
	}

	public static void trackLinkFlightFilter(String filterType) {
		String link = PREFIX_FLIGHT_SEARCH_FILTER + "." + filterType;
		internalTrackLink(link);
	}

	public static void trackNumStopsFlightFilter(int numStops) {
		// Has to always be in English, so no getQuantityString allowed.
		String stopsString;
		if (numStops == 0) {
			stopsString = "No Stops";
		}
		else if (numStops == 1) {
			stopsString = "1 Stop";
		}
		else {
			stopsString = numStops + " Stops";
		}
		trackLinkFlightFilter(stopsString);
	}

	public static void trackLinkFlightCheckoutTravelerSelectExisting() {
		internalTrackLink(FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutTravelerEnterManually() {
		internalTrackLink(FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	public static void trackLinkFlightCheckoutPaymentSelectExisting() {
		internalTrackLink(FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutPaymentEnterManually() {
		internalTrackLink(FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	public static void trackErrorPageLoadFlightUnsupportedPOS() {
		internalTrackPageLoadEventStandard(FLIGHT_ERROR_NOT_YET_AVAILABLE);
	}

	public static void trackErrorPageLoadFlightCheckout() {
		internalTrackPageLoadEventPriceChange(FLIGHT_ERROR_CHECKOUT);
	}

	public static void trackErrorPageLoadFlightPriceChangeTicket() {
		internalTrackPageLoadEventPriceChange(FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET);
	}

	public static void trackErrorPageLoadFlightPaymentFailed() {
		internalTrackPageLoadEventStandard(FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED);
	}

	public static void trackErrorPageLoadFlightIncorrectCVV() {
		internalTrackPageLoadEventStandard(FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV);
	}

	public static void trackErrorPageLoadFlightSoldOut() {
		internalTrackPageLoadEventStandard(FLIGHT_ERROR_SOLD_OUT);
	}

	public static void trackErrorPageLoadFlightSearchExpired() {
		internalTrackPageLoadEventStandard(FLIGHT_ERROR_SEARCH_EXPIRED);
	}

	public static void trackFlightConfirmationAddToCalendar() {
		internalTrackLink(FLIGHT_CONF_ADD_TO_CALENDAR);
	}

	public static void trackFlightConfirmationShareEmail() {
		internalTrackLink(FLIGHT_CONF_SHARE_EMAIL);
	}

	public static void trackFlightInfantDialog() {
		createTrackLinkEvent(FLIGHT_INFANT_ALERT).track();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LX tracking
	//
	// Official Spec : https://confluence/display/Omniture/Mobile+App%3A+Local+Expert
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final String LX_LOB = "local expert";
	public static final String LX_SEARCH = "App.LX.Search";
	public static final String LX_GT_SEARCH = "App.LX-GT.Search";
	public static final String LX_DESTINATION_SEARCH = "App.LX.Dest-Search";
	public static final String LX_GT_DESTINATION_SEARCH = "App.LX-GT.Dest-Search";
	public static final String LX_INFOSITE_INFORMATION = "App.LX.Infosite.Information";
	public static final String LX_GT_INFOSITE_INFORMATION = "App.LX-GT.Infosite.Information";
	public static final String LX_CHECKOUT_INFO = "App.LX.Checkout.Info";
	public static final String LX_GT_CHECKOUT_INFO = "App.LX-GT.Checkout.Info";
	public static final String LX_SEARCH_FILTER = "App.LX.Search.Filter";
	public static final String LX_SEARCH_FILTER_CLEAR = "App.LX.Search.Filter.Clear";
	public static final String LX_CHECKOUT_CONFIRMATION = "App.LX.Checkout.Confirmation";
	public static final String LX_GT_CHECKOUT_CONFIRMATION = "App.LX-GT.Checkout.Confirmation";
	public static final String LX_TICKET_SELECT = "App.LX.Ticket.Select";
	public static final String LX_GT_TICKET_SELECT = "App.LX-GT.Ticket.Select";
	public static final String LX_CHANGE_DATE = "App.LX.Info.DateChange";
	public static final String LX_GT_CHANGE_DATE = "App.LX-GT.Info.DateChange";
	public static final String LX_INFO = "LX_INFO";
	public static final String LX_TICKET = "App.LX.Ticket.";
	public static final String LX_GT_TICKET = "App.LX-GT.Ticket.";
	private static final String LX_CHECKOUT_TRAVELER_INFO = "App.LX.Checkout.Traveler.Edit.Info";
	private static final String LX_GT_CHECKOUT_TRAVELER_INFO = "App.LX-GT.Checkout.Traveler.Edit.Info";
	private static final String LX_CHECKOUT_PAYMENT_INFO = "App.LX.Checkout.Payment.Edit.Info";
	private static final String LX_GT_CHECKOUT_PAYMENT_INFO = "App.LX-GT.Checkout.Payment.Edit.Info";
	private static final String LX_CHECKOUT_SLIDE_TO_PURCHASE = "App.LX.Checkout.SlideToPurchase";
	private static final String LX_GT_CHECKOUT_SLIDE_TO_PURCHASE = "App.LX-GT.Checkout.SlideToPurchase";
	private static final String LX_CHECKOUT_CVV_SCREEN = "App.LX.Checkout.Payment.CID";
	private static final String LX_GT_CHECKOUT_CVV_SCREEN = "App.LX-GT.Checkout.Payment.CID";
	private static final String LX_NO_SEARCH_RESULTS = "App.LX.NoResults";
	private static final String LX_GT_NO_SEARCH_RESULTS = "App.LX-GT.NoResults";
	private static final String LX_CATEGORY_TEST = "App.LX.Category";
	private static final String LX_SEARCH_CATEGORIES = "App.LX.Search.Categories";
	private static final String LX_SORT_PRICE = "Price";
	private static final String LX_SORT_POPULARITY = "Popularity";
	private static final String LX_SORT = ".Sort.";
	private static final String LX_FILTER = ".Filter.";
	private static final String LX_RECOMMENDED_ACTIVITY = "App.LX.Info.Recommend";

	public static void trackAppLXRecommendedActivitiesABTest() {
		Log.d(TAG, "Tracking \"" + LX_LOB + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLXRecommendedActivitiesTest);
		s.trackLink(null, "o", "ape:Log Experiment", null, null);
	}

	public static void trackFirstActivityListingExpanded() {
		Log.d(TAG, "Tracking \"" + LX_LOB + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLXFirstActivityListingExpanded);
		s.track();
	}

	public static void trackAppLXCategoryABTest() {
		Log.d(TAG, "Tracking \"" + LX_CATEGORY_TEST + "\" category...");
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLXCategoryABTest);
		s.track();
	}

	public static void trackAppLXRTRABTest() {
		Log.d(TAG, "Tracking \"" + LX_SEARCH + "\" category...");
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLXRTROnSearchAndDetails);
		s.trackLink(null, "o", "ape:Log Experiment", null, null);
	}

	public static void trackAppLXSearch(LXSearchParams lxSearchParams,
										LXSearchResponse lxSearchResponse, boolean isGroundTransport) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"" + LX_SEARCH + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(isGroundTransport ? LX_GT_SEARCH : LX_SEARCH);

		// Destination
		s.setProp(4, lxSearchResponse.regionId);
		s.setEvar(4, "D=c4");

		// Success event for Product Search, Local Expert Search
		s.setEvents(isGroundTransport ? "event30,event47" : "event30,event56");

		// prop and evar 5, 6
		setDateValues(s, lxSearchParams.startDate, lxSearchParams.endDate);

		// Freeform location
		if (!TextUtils.isEmpty(lxSearchParams.location)) {
			s.setEvar(48, lxSearchParams.location);
		}

		// Number of search results
		if (lxSearchResponse.activities.size() > 0) {
			s.setProp(1, Integer.toString(lxSearchResponse.activities.size()));
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXSearchCategories(LXSearchParams lxSearchParams,
												  LXSearchResponse lxSearchResponse) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"" + LX_SEARCH_CATEGORIES + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_SEARCH_CATEGORIES);

		// Destination
		s.setProp(4, lxSearchResponse.regionId);
		s.setEvar(4, "D=c4");

		// prop and evar 5, 6
		setDateValues(s, lxSearchParams.startDate, lxSearchParams.endDate);

		// Freeform location
		if (!TextUtils.isEmpty(lxSearchParams.location)) {
			s.setEvar(48, lxSearchParams.location);
		}

		// Number of search results
		if (lxSearchResponse.activities.size() > 0) {
			s.setProp(1, Integer.toString(lxSearchResponse.activities.size()));
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXNoSearchResults(ApiError apiError, boolean isGrounTransport) {
		Log.d(TAG, "Tracking \"" + LX_NO_SEARCH_RESULTS + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(isGrounTransport ? LX_GT_NO_SEARCH_RESULTS : LX_NO_SEARCH_RESULTS);

		if (apiError != null) {
			// Destination
			if (Strings.isNotEmpty(apiError.regionId)) {
				s.setProp(4, apiError.regionId);
				s.setEvar(4, "D=c4");
			}

			if (apiError.errorInfo != null && Strings.isNotEmpty(apiError.errorInfo.cause)) {
				s.setProp(36, apiError.errorInfo.cause);
			}
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXSearchBox(boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_DESTINATION_SEARCH + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(isGroundTransport ? LX_GT_DESTINATION_SEARCH : LX_DESTINATION_SEARCH);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXSortAndFilterOpen() {
		Log.d(TAG, "Tracking \"" + LX_SEARCH_FILTER + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_SEARCH_FILTER);

		// Send the tracking data
		s.track();
	}

	public static void trackLinkLXRecommendedActivity() {
		String tpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, tpid);
		s.setEvar(28, LX_RECOMMENDED_ACTIVITY);
		s.setProp(16, LX_RECOMMENDED_ACTIVITY);
		s.trackLink(null, "o", LX_INFO, null, null);
	}

	public static void trackLinkLXSort(LXSortType sortType) {
		String sort = sortType.equals(LXSortType.PRICE) ? LX_SORT_PRICE : LX_SORT_POPULARITY;
		StringBuilder sb = new StringBuilder();
		sb.append(LX_SEARCH);
		sb.append(LX_SORT);
		sb.append(sort);
		trackLinkLXSearch(sb.toString());
	}

	public static void trackLinkLXFilter(String categoryKey) {
		StringBuilder sb = new StringBuilder();
		sb.append(LX_SEARCH);
		sb.append(LX_FILTER);
		sb.append(categoryKey);
		trackLinkLXSearch(sb.toString());
	}

	public static void trackLinkLXSortAndFilterCleared() {
		trackLinkLXSearch(LX_SEARCH_FILTER_CLEAR);
	}

	private static void trackLinkLXSearch(String rffr) {
		String tpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, tpid);
		s.setEvar(28, rffr);
		s.setProp(16, rffr);
		s.setProp(61, tpid);
		s.trackLink(null, "o", LX_SEARCH, null, null);
	}

	public static void trackAppLXProductInformation(ActivityDetailsResponse activityDetailsResponse,
													LXSearchParams lxSearchParams, boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_INFOSITE_INFORMATION + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(
			isGroundTransport ? LX_GT_INFOSITE_INFORMATION : LX_INFOSITE_INFORMATION);

		s.setEvents(isGroundTransport ? "event3" : "event32");

		s.setProducts("LX;Merchant LX:" + activityDetailsResponse.id);

		// Destination
		s.setProp(4, activityDetailsResponse.regionId);
		s.setEvar(4, "D=c4");

		// prop and evar 5, 6
		setDateValues(s, lxSearchParams.startDate, lxSearchParams.endDate);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutPayment(String lxActivityId, LocalDate lxActivityStartDate,
												 int selectedTicketsCount, String totalPriceFormattedTo2DecimalPlaces, boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_INFO + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(isGroundTransport ? LX_GT_CHECKOUT_INFO : LX_CHECKOUT_INFO);
		s.setEvents("event75");
		s.setProducts(addLXProducts(lxActivityId, totalPriceFormattedTo2DecimalPlaces, selectedTicketsCount));
		setLXDateValues(lxActivityStartDate, s);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutConfirmation(LXCheckoutResponse checkoutResponse,
													  String lxActivityId, LocalDate lxActivityStartDate, LocalDate lxActivityEndDate, int selectedTicketsCount, boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_CONFIRMATION + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(
			isGroundTransport ? LX_GT_CHECKOUT_CONFIRMATION : LX_CHECKOUT_CONFIRMATION);
		String orderId = checkoutResponse.orderId;
		String currencyCode = checkoutResponse.currencyCode;
		String travelRecordLocator = checkoutResponse.newTrip.travelRecordLocator;
		String totalMoney = checkoutResponse.totalCharges;

		s.setEvents("purchase");

		s.setPurchaseID("onum" + orderId);
		s.setProp(72, orderId);
		s.setProp(71, travelRecordLocator);
		s.setCurrencyCode(currencyCode);
		s.setProducts(addLXProducts(lxActivityId, totalMoney, selectedTicketsCount));

		String activityStartDateString = lxActivityStartDate.toString(LX_CONFIRMATION_PROP_DATE_FORMAT);

		// TODO: Change to correct end date once we have response from the API, for now sending start and end date as same.
		String activityEndDateString = lxActivityStartDate.toString(LX_CONFIRMATION_PROP_DATE_FORMAT);

		// e.g LX:20160622-20160622:N, N/Y if we have used coupon.
		s.setEvar(30, "LX:" + activityStartDateString + "-" + activityEndDateString + ":N");

		setLXDateValues(lxActivityStartDate, s);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutTraveler(LineOfBusiness lob) {
		boolean isGroundTransport = lob.equals(LineOfBusiness.TRANSPORT);

		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_TRAVELER_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(isGroundTransport ? LX_GT_CHECKOUT_TRAVELER_INFO : LX_CHECKOUT_TRAVELER_INFO);
		s.setEvar(18, isGroundTransport ? LX_GT_CHECKOUT_TRAVELER_INFO : LX_CHECKOUT_TRAVELER_INFO);
		s.track();

	}

	public static void trackAppLXCheckoutPayment(LineOfBusiness lob) {
		boolean isGroundTransport = lob.equals(LineOfBusiness.TRANSPORT);

		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_PAYMENT_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(isGroundTransport ? LX_GT_CHECKOUT_PAYMENT_INFO : LX_CHECKOUT_PAYMENT_INFO);
		s.setEvar(18, isGroundTransport ? LX_GT_CHECKOUT_PAYMENT_INFO : LX_CHECKOUT_PAYMENT_INFO);
		s.track();
	}

	public static void trackAppLXCheckoutSlideToPurchase(LineOfBusiness lob, String cardType) {
		boolean isGroundTransport = lob.equals(LineOfBusiness.TRANSPORT);
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(isGroundTransport ? LX_GT_CHECKOUT_SLIDE_TO_PURCHASE : LX_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(18, isGroundTransport ? LX_GT_CHECKOUT_SLIDE_TO_PURCHASE : LX_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, cardType);
		s.track();
	}

	public static void trackAppLXCheckoutCvvScreen(boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_CVV_SCREEN + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(isGroundTransport ? LX_GT_CHECKOUT_CVV_SCREEN : LX_CHECKOUT_CVV_SCREEN);
		s.setEvar(18, isGroundTransport ? LX_GT_CHECKOUT_CVV_SCREEN : LX_CHECKOUT_CVV_SCREEN);

		s.track();
	}

	public static String addLXProducts(String activityId, String totalMoney, int ticketCount) {
		return "LX;Merchant LX:" + activityId + ";" + ticketCount + ";" + totalMoney;
	}

	public static void trackLinkLXChangeDate(boolean isGroundTransport) {
		trackLinkLX(isGroundTransport ? LX_GT_CHANGE_DATE : LX_CHANGE_DATE);
	}

	public static void trackLinkLXSelectTicket(boolean isGroundTransport) {
		trackLinkLX(isGroundTransport ? LX_GT_TICKET_SELECT : LX_TICKET_SELECT);
	}

	public static void trackLinkLXAddRemoveTicket(String rffr, boolean isGroundTransport) {

		StringBuilder sb = new StringBuilder();
		sb.append(isGroundTransport ? LX_GT_TICKET : LX_TICKET);
		sb.append(rffr);
		trackLinkLX(sb.toString());
	}

	public static void trackLinkLXCategoryClicks(String rffr) {
		StringBuilder sb = new StringBuilder();
		sb.append(LX_SEARCH_CATEGORIES);
		sb.append(".");
		sb.append(rffr);
		trackLinkLX(sb.toString());
	}

	public static void trackLinkLX(String rffr) {
		Log.d(TAG, "Tracking \"" + LX_CHANGE_DATE + "\" Link..." + "RFFR : " + rffr);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.setEvar(28, rffr);
		s.setProp(16, rffr);
		s.trackLink(null, "o", LX_INFO, null, null);
	}

	public static ADMS_Measurement internalTrackAppLX(String pageName) {
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(pageName);
		s.setEvar(18, pageName);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, LX_LOB);
		return s;
	}

	private static void setLXDateValues(LocalDate lxActivityStartDate, ADMS_Measurement s) {
		String activityStartDateString = lxActivityStartDate.toString(PROP_DATE_FORMAT);
		s.setProp(5, activityStartDateString);

		// num days between current day (now) and activity start date.
		LocalDate now = LocalDate.now();
		String numDaysOut = Integer.toString(JodaUtils.daysBetween(now, lxActivityStartDate));
		s.setEvar(5, numDaysOut);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Tablet-Specific Tracking
	//

	///////////////////////////
	// Launch Screen

	private static final String TABLET_LAUNCH_DEST_SELECT = "App.Dest-Search";
	private static final String TABLET_SEARCH_RESULTS = "App.Dest-Search.Results";

	private static final String BASE_RFFR_FEATURED_LINK = "App.LS.Featured.";
	private static final String BASE_RFFR_MAP_LINK = "App.LS.Map.";

	private static final String TABLET_COLLECTIONS_EVAR12 = "Launch.Search.Collections";

	// When a bottom tile is clicked – collection selection
	public static void trackTabletLaunchTileSelect(String tileUniqueId) {
		ADMS_Measurement s = createTrackLinkEvent("nil");
		addLaunchScreenCommonParams(s, BASE_RFFR_FEATURED_LINK, tileUniqueId);
		s.setEvar(12, TABLET_COLLECTIONS_EVAR12);
		internalTrackLink(s);
	}

	// When a city is selected within a collection
	public static void trackLaunchCitySelect(String destinationId) {
		ADMS_Measurement s = createTrackLinkEvent("nil");
		addLaunchScreenCommonParams(s, BASE_RFFR_MAP_LINK, destinationId);
		internalTrackLink(s);
	}

	// Destination waypoint screen - Launch
	public static void trackTabletDestinationSearchPageLoad() {
		internalTrackPageLoadEventStandard(TABLET_LAUNCH_DEST_SELECT);
	}

	private static void addOriginAndDestinationVars(ADMS_Measurement s, SuggestionV2 origin, SuggestionV2 destination) {
		String originRegionId =
			(origin != null && origin.getRegionId() != 0) ?
				Integer.toString(origin.getRegionId()) : "No Origin";
		s.setProp(3, originRegionId);
		s.setEvar(3, "D=c3");

		String destinationRegionId =
			(destination != null && destination.getRegionId() != 0) ?
				Integer.toString(destination.getRegionId()) : "No Destination";
		s.setProp(4, destinationRegionId);
		s.setEvar(4, "D=c4");
	}

	private static void addHotelRegionId(ADMS_Measurement s, HotelSearchParams params) {
		String region;
		if (params.getSearchType().equals(HotelSearchParams.SearchType.MY_LOCATION)) {
			region = "Current Location";
		}
		else {
			region = params.getRegionId();
		}
		s.setProp(4, region);
		s.setEvar(4, "D=c4");
	}

	public static void trackTabletSearchResultsPageLoad(SearchParams params) {
		ADMS_Measurement s = createTrackPageLoadEventBase(TABLET_SEARCH_RESULTS);
		internalSetHotelDateProps(s, params.toHotelSearchParams());
		addOriginAndDestinationVars(s, params.getOrigin(), params.getDestination());
		s.setEvents("event2");
		s.setEvar(47, getDSREvar47String(params));
		s.setEvar(48, Html.fromHtml(params.getDestination().getDisplayName()).toString());

		s.track();
	}

	private static void trackAbacusTest(ADMS_Measurement s, int testKey) {
		if (!ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			return;
		}
		AbacusTest test = Db.getAbacusResponse().testForKey(testKey);

		if (test == null) {
			// Just log control
			test = new AbacusTest();
			test.id = testKey;
			test.value = AbacusUtils.DefaultVariate.CONTROL.ordinal();
			test.instanceId = 0;
		}

		// Adds piping for multivariate AB Tests.
		String analyticsString = AbacusUtils.appendString(s.getProp(34)) + AbacusUtils.getAnalyticsString(test);
		if (!TextUtils.isEmpty(analyticsString)) {
			s.setEvar(34, analyticsString);
			s.setProp(34, analyticsString);
		}
		AbacusLogQuery query = new AbacusLogQuery(Db.getAbacusGuid(), PointOfSale.getPointOfSale().getTpid(), 0);
		query.addExperiment(test);
		Ui.getApplication(sContext).appComponent().abacus().logExperiment(query);
	}

	private static void addLaunchScreenCommonParams(ADMS_Measurement s, String baseRef, String refAppend) {
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		String rffrId = baseRef + refAppend;
		String posEapid = Integer.toString(PointOfSale.getPointOfSale().getEAPID());
		s.setProp(7, posTpid);
		s.setProp(16, rffrId);
		s.setEvar(28, rffrId);
		s.setEvar(61, posTpid);
		if (PointOfSale.getPointOfSale().getEAPID() != PointOfSale.INVALID_EAPID) {
			s.setEvar(61, posTpid + "-" + posEapid);
			s.setProp(7, posTpid + "-" + posEapid);
		}
	}

	// For shared screens (e.g. Tablet Search Results) we want to send the
	// the same string as the Flights evar 47 string, but with FLT instead
	// of DSR.
	private static String getDSREvar47String(SearchParams params) {
		String flightsString = getEvar47String(params.toFlightSearchParams());
		return flightsString.replace("FLT", "DSR");
	}

	///////////////////////////
	// Search Results Screen - Refinements

	private static final String CHOOSE_DATES_LINK = "App.DSR.ChangeDates";
	private static final String CHOOSE_ORIGIN_LINK = "App.DSR.Change.Origin";
	private static final String CHOOSE_DEST_LINK = "App.DSR.Change.Destination";

	public static void trackChooseDatesLinkClick() {
		internalTrackLink(CHOOSE_DATES_LINK);
	}

	public static void trackChooseOriginLinkClick() {
		internalTrackLink(CHOOSE_ORIGIN_LINK);
	}

	public static void trackChooseDestinationLinkClick() {
		internalTrackLink(CHOOSE_DEST_LINK);
	}

	///////////////////////////
	// Search Results - Traveler Alerts

	private static final String SEARCH_ALERT_BASE = "App.Alert.";

	public static void trackDateMismatchAlert() {
		String link = SEARCH_ALERT_BASE + "MisMatchDates";
		internalTrackLink(link);
	}

	public static void trackRedeyeAlert() {
		String link = SEARCH_ALERT_BASE + "RedEyeFlight";
		internalTrackLink(link);
	}

	///////////////////////////
	// Search Results Screen - TripBucket

	private static final String BUCKET_HOTEL_REMOVAL = "App.DSR.DeleteHotel";
	private static final String UNDO_BUCKET_HOTEL_REMOVAL = "App.DSR.DeleteHotel.Undo";
	private static final String BUCKET_FLIGHT_REMOVAL = "App.DSR.DeleteFlight";
	private static final String UNDO_BUCKET_FLIGHT_REMOVAL = "App.DSR.DeleteFlight.Undo";

	// Removing and undoing things

	public static void trackTripBucketItemRemoval(LineOfBusiness lob) {
		String link = lob == LineOfBusiness.FLIGHTS ? BUCKET_FLIGHT_REMOVAL : BUCKET_HOTEL_REMOVAL;
		internalTrackLink(link);
	}

	public static void trackTripBucketItemUndoRemoval(LineOfBusiness lob) {
		String link = lob == LineOfBusiness.FLIGHTS ? UNDO_BUCKET_FLIGHT_REMOVAL : UNDO_BUCKET_HOTEL_REMOVAL;
		internalTrackLink(link);
	}

	public static void trackTripBucketPortraitToggle(LineOfBusiness lob,
													 CheckoutTripBucketState newState) {
		String suffix = newState == CheckoutTripBucketState.OPEN ? ".Checkout.DetailsExpand" : ".Checkout.DetailsCollapse";
		internalTrackLink(getBase(lob == LineOfBusiness.FLIGHTS) + suffix);
	}

	///////////////////////////
	// Search Results Screen - Hotels

	// Page names
	private static final String PAGE_NAME_HOTEL_SEARCH = "App.Hotels.Search";
	private static final String PAGE_NAME_HOTEL_SORT_FILTER = "App.Hotels.Search.Refine";

	// Link URLs
	private static final String PIN_CLICK_LINK_NAME = "App.Hotels.SR.TapPin";
	private static final String NEIGHBORHOOD_FILTER_LINK_NAME = "App.Hotels.Search.Filter.Area";


	private static final String PROP_DATE_FORMAT = "yyyy-MM-dd";
	private static final String LX_CONFIRMATION_PROP_DATE_FORMAT = "yyyyMMdd";

	public static void trackTabletHotelListOpen(HotelSearchParams searchParams,
												HotelSearchResponse searchResponse) {
		internalTrackTabletHotelSearchOpen(searchParams, searchResponse);
	}

	private static void internalTrackTabletHotelSearchOpen(HotelSearchParams searchParams,
														   HotelSearchResponse searchResponse) {
		ADMS_Measurement s = createTrackPageLoadEventBase(PAGE_NAME_HOTEL_SEARCH);
		// Events
		s.setEvents("event30,event51");

		// Props
		s.setProp(1, Integer.toString(searchResponse.getPropertiesCount()));
		internalSetHotelDateProps(s, searchParams);
		addHotelRegionId(s, searchParams);

		// Evars
		addStandardHotelFields(s, searchParams);
		s.setEvar(47, getEvar47String(searchParams));

		// Has at least one sponsored Listing
		if (searchResponse.hasSponsoredListing()) {
			s.setEvar(28, HOTELS_SEARCH_SPONSORED_PRESENT);
			s.setProp(16, HOTELS_SEARCH_SPONSORED_PRESENT);
		}
		else {
			s.setEvar(28, HOTELS_SEARCH_SPONSORED_NOT_PRESENT);
			s.setProp(16, HOTELS_SEARCH_SPONSORED_NOT_PRESENT);
		}

		s.track();
	}

	private static void internalSetHotelDateProps(ADMS_Measurement s, HotelSearchParams searchParams) {
		LocalDate checkInDate = searchParams.getCheckInDate();
		LocalDate checkOutDate = searchParams.getCheckOutDate();
		setDateValues(s, checkInDate, checkOutDate);
	}

	private static void internalSetFlightDateProps(ADMS_Measurement s, FlightSearchParams searchParams) {
		LocalDate departureDate = searchParams.getDepartureDate();
		LocalDate returnDate = searchParams.getReturnDate();
		setDateValues(s, departureDate, returnDate);
	}

	private static void setDateValues(ADMS_Measurement s, LocalDate startDate, LocalDate endDate) {
		String checkInString = startDate.toString(PROP_DATE_FORMAT);
		s.setProp(5, checkInString);

		String checkOutString;
		if (endDate != null) {
			checkOutString = endDate.toString(PROP_DATE_FORMAT);
		}
		else {
			checkOutString = "nil";
		}
		s.setProp(6, checkOutString);

		LocalDate now = LocalDate.now();

		// num days between current day (now) and flight departure date
		String numDaysOut = Integer.toString(JodaUtils.daysBetween(now, startDate));
		s.setEvar(5, numDaysOut);

		String duration;
		if (endDate != null) {
			duration = Integer.toString(JodaUtils.daysBetween(startDate, endDate));
		}
		else {
			duration = "0";
		}
		s.setEvar(6, duration);
	}

	public static void trackLinkHotelPinClick() {
		internalTrackLink(PIN_CLICK_LINK_NAME);
	}

	public static void trackTabletHotelsSortAndFilterOpen() {
		internalTrackPageLoadEventStandard(PAGE_NAME_HOTEL_SORT_FILTER);
	}

	public static void trackTabletNeighborhoodFilter() {
		internalTrackLink(NEIGHBORHOOD_FILTER_LINK_NAME);
	}

	private static String getEvar47String(HotelSearchParams params) {
		StringBuilder sb = new StringBuilder("HOT|A");
		sb.append(params.getNumAdults());
		sb.append("|C");
		sb.append(params.getNumChildren());
		return sb.toString();
	}

	// Checkout
	// Calls are similar enough, with only LOB tweaks, so it makes sense to unify this.

	private static final String CHECKOUT_FLIGHT_INFO_TEMPLATE = "App.Flight.Checkout";
	private static final String CHECKOUT_HOTEL_INFO_TEMPLATE = "App.Hotels.Checkout";

	private static final String getBase(boolean isFlights) {
		return isFlights ? CHECKOUT_FLIGHT_INFO_TEMPLATE : CHECKOUT_HOTEL_INFO_TEMPLATE;
	}

	public static void trackTabletCheckoutPageLoad(LineOfBusiness lob) {
		boolean isFlights = lob == LineOfBusiness.FLIGHTS;
		String pageName = getBase(isFlights) + ".Info";
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		if (isFlights) {
			s.setEvents("event71");
			FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
			s.setEvar(47, getEvar47String(params));

			String origin = params.getDepartureLocation().getDestinationId();
			s.setEvar(3, origin);
			s.setProp(3, origin);
			String dest = params.getArrivalLocation().getDestinationId();
			s.setEvar(4, dest);
			s.setProp(4, dest);

			internalSetFlightDateProps(s, params);
			addStandardFlightFields(s);
			FlightTrip searchFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
			if (searchFlightTrip.isSplitTicket()) {
				addFlightSplitTicketInfo(s, pageName, searchFlightTrip, false, false);
			}
			else {
				addProducts(s);
			}
		}
		else {
			s.setEvents("event70");
			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			s.setEvar(47, getEvar47String(params));

			// DepositV2 Omniture Tracking
			if (!Db.getTripBucket().getHotel().getProperty().hasEtpOffer()) {
				s.setEvar(52, "Non Etp");
			}
			else if (Db.getTripBucket().getHotel().getRate().isPayLater()) {
				if (Db.getTripBucket().getHotel().getRate().depositRequired()) {
					s.setEvar(52, "Pay Later Deposit");
				}
				else {
					s.setEvar(52, "Pay Later");
				}
			}
			else {
				s.setEvar(52, "Pay Now");
			}

			addHotelRegionId(s, params);
			addProducts(s, Db.getTripBucket().getHotel().getProperty());
			addStandardHotelFields(s, params);
		}
		s.track();
	}

	public static void trackItemSoldOutOnCheckoutLink(LineOfBusiness lob) {
		String soldOutLink = getBase(lob == LineOfBusiness.FLIGHTS) + ".Checkout.Error";
		internalTrackLink(soldOutLink);
	}

	public static void trackTabletEditTravelerPageLoad(LineOfBusiness lob) {
		internalTrackTabletCheckoutPageLoad(lob, ".Traveler.Edit.Info", false, false);
	}

	public static void trackTabletEditPaymentPageLoad(LineOfBusiness lob) {
		internalTrackTabletCheckoutPageLoad(lob, ".Traveler.Payment.Info", false, false);
	}

	public static void trackTabletSlideToPurchasePageLoad(LineOfBusiness lob) {
		internalTrackTabletCheckoutPageLoad(lob, ".Payment.SlideToPurchase", true, false);
	}

	public static void trackTabletCVVPageLoad(LineOfBusiness lob) {
		internalTrackTabletCheckoutPageLoad(lob, ".Payment.CID", false, false);
	}

	public static void trackTabletConfirmationPageLoad(LineOfBusiness lob) {
		internalTrackTabletCheckoutPageLoad(lob, ".Confirmation", false, true);
	}

	////////////////////////////
	// Air Attach
	// https://confluence/display/Omniture/Tablet+App%3A+Air+Attach

	private static final String AIR_ATTACH_ELIGIBLE = "App.Flight.CKO.AttachEligible";
	private static final String AIR_ATTACH_HOTEL_ADD = "App.Hotels.IS.AddTrip";
	private static final String ADD_ATTACH_HOTEL = "App.Flight.CKO.Add.AttachHotel";
	private static final String ADD_ATTACH_CAR = "App.Flight.CKO.Confirm.Xsell";
	private static final String ADD_ATTACH_LX = "App.Flight.CKO.Confirm.Xsell";
	private static final String CROSS_SELL_CAR_FROM_FLIGHT = "CrossSell.Flight.Confirm.Cars";
	private static final String CROSS_SELL_LX_FROM_FLIGHT = "CrossSell.Flight.Confirm.LX";
	private static final String BOOK_NEXT_ATTACH_HOTEL = "App.Flight.CKO.BookNext";
	private static final String AIR_ATTACH_ITIN_XSELL = "Itinerary X-Sell";
	private static final String AIR_ATTACH_ITIN_XSELL_REF = "App.Itin.X-Sell.Hotel";
	private static final String AIR_ATTACH_PHONE_BANNER = "Launch Screen";
	private static final String AIR_ATTACH_PHONE_BANNER_REF = "App.LS.AttachEligible";
	private static final String AIR_ATTACH_PHONE_BANNER_CLICK = "App.LS.AttachHotel";

	public static void trackAirAttachItinCrossSell() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, AIR_ATTACH_ITIN_XSELL_REF);
		s.setProp(16, AIR_ATTACH_ITIN_XSELL_REF);
		s.trackLink(null, "o", AIR_ATTACH_ITIN_XSELL, null, null);
	}

	public static void trackPhoneAirAttachBanner() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, AIR_ATTACH_PHONE_BANNER_REF);
		s.setProp(16, AIR_ATTACH_PHONE_BANNER_REF);
		s.trackLink(null, "o", AIR_ATTACH_PHONE_BANNER, null, null);
	}

	public static void trackPhoneAirAttachBannerClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, AIR_ATTACH_PHONE_BANNER_CLICK);
		s.setProp(16, AIR_ATTACH_PHONE_BANNER_CLICK);
		s.trackLink(null, "o", AIR_ATTACH_PHONE_BANNER, null, null);
	}

	public static void trackAddAirAttachHotel() {
		Rate rate = Db.getTripBucket().getHotel().getRate();
		if (rate.isAirAttached()) {
			ADMS_Measurement s = getFreshTrackingObject();
			Property property = Db.getTripBucket().getHotel().getProperty();
			addEventsAndProductsForAirAttach(s, property, "event58", "Flight:Hotel Infosite X-Sell");
			s.setEvar(28, AIR_ATTACH_HOTEL_ADD);
			s.setProp(16, AIR_ATTACH_HOTEL_ADD);
			s.trackLink(null, "o", "Infosite", null, null);
		}
	}

	public static void trackFlightConfirmationAirAttach() {
		if (Db.getTripBucket() == null || Db.getTripBucket().getHotel() == null) {
			return;
		}

		Rate rate = Db.getTripBucket().getHotel().getRate();
		if (rate.isAirAttached()) {
			ADMS_Measurement s = getFreshTrackingObject();
			Property property = Db.getTripBucket().getHotel().getProperty();
			addEventsAndProductsForAirAttach(s, property, "event57", "Flight:Hotel CKO X-Sell");
			s.setEvar(28, AIR_ATTACH_ELIGIBLE);
			s.setProp(16, AIR_ATTACH_ELIGIBLE);
			s.trackLink(null, "o", "Checkout", null, null);
		}
	}

	private static void internalTrackTabletCheckoutPageLoad(LineOfBusiness lob,
															String pageNameSuffix,
															boolean includePaymentInfo, boolean isConfirmation) {
		boolean isFlights = lob == LineOfBusiness.FLIGHTS;
		String pageName = getBase(isFlights) + pageNameSuffix;
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		if (includePaymentInfo) {
			s.setEvar(37, getPaymentType());
		}
		if (isFlights) {
			FlightTrip newestFlightOffer = getNewestFlightOffer();
			boolean isSplitTicket = newestFlightOffer.isSplitTicket();
			FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
			FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
			s.setCurrencyCode(trip.getTotalFare().getCurrency());

			addStandardFlightFields(s);
			setEvar30(s, trip, params);

			if (isConfirmation) {
				s.setEvents("purchase");
				if (isSplitTicket) {
					addFlightSplitTicketInfo(s, pageName, newestFlightOffer, true, true);
				}
				else {
					addProducts(s);
				}
				String itinId = trip.getItineraryNumber();
				s.setProp(71, itinId);
				String orderNumber = Db.getTripBucket().getFlight().getCheckoutResponse().getOrderId();
				s.setPurchaseID("onum" + orderNumber);
				s.setProp(72, orderNumber);
			}
		}
		else {
			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			Rate rate = Db.getTripBucket().getHotel().getRate();
			addStandardHotelFields(s, params);
			s.setCurrencyCode(rate.getTotalAmountAfterTax().getCurrency());

			boolean couponApplied = Db.getTripBucket().getHotel().isCouponApplied();
			setEvar30(s, params, couponApplied);

			if (isConfirmation) {
				s.setEvents("purchase");
				Property property = Db.getTripBucket().getHotel().getProperty();
				addProducts(s, property, params.getStayDuration(), rate.getTotalAmountAfterTax().getAmount().doubleValue());

				String itinId = Db.getTripBucket().getHotel().getBookingResponse().getItineraryId();
				s.setProp(71, itinId);
				String orderNumber = Db.getTripBucket().getHotel().getBookingResponse().getOrderNumber();
				s.setPurchaseID("onum" + orderNumber);
				s.setProp(72, orderNumber);
			}
		}
		s.track();
	}

	// Evar 30 doc: https://confluence/display/Omniture/eVar30+-+Product+Details
	private static final String EVAR30_DATE_FORMAT = "yyyyMMdd";

	// TODO: If we someday support multi-destination flights, we'll have to update
	private static void setEvar30(ADMS_Measurement s, FlightTrip trip, FlightSearchParams searchParams) {
		boolean isRoundtrip = searchParams.isRoundTrip();
		String firstWaypointCode = trip.getLeg(0).getFirstWaypoint().mAirportCode;
		String lastWaypointCode = trip.getLeg(0).getLastWaypoint().mAirportCode;

		StringBuilder sb = new StringBuilder("Flight: ");
		sb.append(firstWaypointCode).append('-');
		sb.append(lastWaypointCode);
		if (isRoundtrip) {
			sb.append('-').append(firstWaypointCode);
		}
		sb.append(':');
		sb.append(searchParams.getDepartureDate().toString(EVAR30_DATE_FORMAT));
		if (isRoundtrip) {
			sb.append('-').append(searchParams.getReturnDate().toString(EVAR30_DATE_FORMAT));
		}

		s.setEvar(30, sb.toString());
	}

	public static void setEvar30(ADMS_Measurement s, HotelSearchParams params, boolean couponApplied) {
		String checkInDate = params.getCheckInDate().toString(EVAR30_DATE_FORMAT);
		String checkOutDate = params.getCheckOutDate().toString(EVAR30_DATE_FORMAT);
		String couponUsed = couponApplied ? "Y" : "N";
		StringBuilder sb = new StringBuilder("Hotel: ");
		sb.append(checkInDate).append('-').append(checkOutDate);
		sb.append(':').append(couponUsed);
		s.setEvar(30, sb.toString());
	}

	public static void trackBookNextClick(LineOfBusiness lob, boolean isAirAttachScenario) {
		if (isAirAttachScenario) {
			ADMS_Measurement s = getFreshTrackingObject();
			addEventsAndProductsForAirAttach(s, Db.getTripBucket().getHotel().getProperty(), "event58", "Flight:Hotel CKO X-Sell");
			s.setEvar(28, BOOK_NEXT_ATTACH_HOTEL);
			s.setEvar(16, BOOK_NEXT_ATTACH_HOTEL);
			s.trackLink(null, "o", "Checkout", null, null);
		}
		else {
			String link = getBase(lob == LineOfBusiness.FLIGHTS) + ".Confirm.BookNext";
			internalTrackLink(link);
		}
	}

	public static void trackAddHotelClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, ADD_ATTACH_HOTEL);
		s.setProp(16, ADD_ATTACH_HOTEL);
		s.trackLink(null, "o", "Checkout", null, null);
	}

	public static void trackAddCarClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, ADD_ATTACH_CAR);
		s.setProp(16, ADD_ATTACH_CAR);
		s.setEvar(12, CROSS_SELL_CAR_FROM_FLIGHT);
		s.trackLink(null, "o", "Confirmation Cross Sell", null, null);
	}

	public static void trackDoneBookingClick(LineOfBusiness lob) {
		String link = getBase(lob == LineOfBusiness.FLIGHTS) + ".Confirm.Done";
		internalTrackLink(link);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Guest Picker Tracking

	public static final String PICKER_TRACKING_BASE_TABLET = "App.DSR";
	public static final String PICKER_TRACKING_BASE_HOTELS = "App.Hotels";
	public static final String PICKER_TRACKING_BASE_FLIGHT = "App.Flight";

	private static final String PICKER_ADD = ".Traveler.Add.";
	private static final String PICKER_REMOVE = ".Traveler.Remove.";
	public static final String PICKER_ADULT = "Adult";
	public static final String PICKER_CHILD = "Child";

	public static void trackAddTravelerLink(String base, String travelerType) {
		internalTrackLink(base + PICKER_ADD + travelerType);
	}

	public static void trackRemoveTravelerLink(String base, String travelerType) {
		internalTrackLink(base + PICKER_REMOVE + travelerType);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itin Tracking
	//
	// Spec: https://confluence/display/Omniture/App+Itinerary
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String ITIN_EMPTY = "App.Itinerary.Empty";
	private static final String ITIN_FIND = "App.Itinerary.Find";
	private static final String ITIN_ADD_SUCCESS = "App.Itinerary.Add.Success";
	private static final String ITIN = "App.Itinerary";
	private static final String ITIN_HOTEL = "App.Itinerary.Hotel";
	private static final String ITIN_HOTEL_DIRECTIONS = "App.Itinerary.Hotels.Directions";
	private static final String ITIN_HOTEL_CALL = "App.Itinerary.Hotel.Call";
	private static final String ITIN_HOTEL_INFO = "App.Itinerary.Hotel.Info.Additional";
	private static final String ITIN_HOTEL_SHARE_PREFIX = "App.Itinerary.Hotel.Info.Share.";
	private static final String ITIN_FLIGHT = "App.Itinerary.Flight";
	private static final String ITIN_FLIGHT_CHECKIN = "App.Itinerary.Flight.CheckInNow";
	private static final String ITIN_FLIGHT_CHECKIN_SUCCESS = "App.Itinerary.Flight.CheckIn.Yes";
	private static final String ITIN_FLIGHT_CHECKIN_FAILURE = "App.Itinerary.Flight.CheckIn.No";
	private static final String ITIN_FLIGHT_CHECKIN_VISIT = "App.Itinerary.Flight.VisitAirline";
	private static final String ITIN_FLIGHT_DIRECTIONS = "App.Itinerary.Flight.Airport.Directions";
	private static final String ITIN_FLIGHT_TERMINAL_MAPS = "App.Itinerary.Flight.Airport.TerminalMaps";
	private static final String ITIN_FLIGHT_INFO = "App.Itinerary.Flight.Info.Additional";
	private static final String ITIN_FLIGHT_SHARE_PREFIX = "App.Itinerary.Flight.Share.";
	private static final String ITIN_FLIGHT_COPY_PNR = "App.Itinerary.Flight.CopyPNR";
	private static final String ITIN_CAR = "App.Itinerary.Car";
	private static final String ITIN_CAR_DIRECTIONS = "App.Itinerary.Car.Directions";
	private static final String ITIN_CAR_CALL = "App.Itinerary.Car.Call";
	private static final String ITIN_CAR_INFO = "App.Itinerary.Car.Info.Additional";
	private static final String ITIN_CAR_SHARE_PREFIX = "App.Itinerary.Car.Share.";
	private static final String ITIN_ACTIVITY = "App.Itinerary.Activity";
	private static final String ITIN_ACTIVITY_REDEEM = "App.Itinerary.Activity.Redeem";
	private static final String ITIN_ACTIVITY_SUPPORT = "App.Itinerary.Activity.Support";
	private static final String ITIN_ACTIVITY_INFO = "App.Itinerary.Activity.Info.Additional";
	private static final String ITIN_ACTIVITY_SHARE_PREFIX = "App.Itinerary.Activity.Share.";
	private static final String ITIN_RELOAD_TEMPLATE = "App.Itinerary.%s.Info.Reload";
	private static final String ITIN_HOTEL_ROOM_CANCEL_CLICK = "App.Itinerary.Hotel.Cancel";

	public static void trackItinEmpty() {
		internalTrackPageLoadEventStandard(ITIN_EMPTY);
	}

	public static void trackFindItin() {
		internalTrackPageLoadEventStandard(ITIN_FIND);
	}

	/**
	 * Track the itin card sharing click
	 *
	 * @param type          which itin card type was being shared
	 * @param isLongMessage true denotes it was a share message long, false denotes share message short
	 */
	public static void trackItinShare(Type type, boolean isLongMessage) {
		String pageName;

		switch (type) {
		case FLIGHT:
			pageName = ITIN_FLIGHT_SHARE_PREFIX;
			break;
		case HOTEL:
			pageName = ITIN_HOTEL_SHARE_PREFIX;
			break;
		case CAR:
			pageName = ITIN_CAR_SHARE_PREFIX;
			break;
		case ACTIVITY:
			pageName = ITIN_ACTIVITY_SHARE_PREFIX;
			break;
		default:
			throw new RuntimeException("You are trying to track the sharing of an itin card type not yet supported");
		}

		if (isLongMessage) {
			pageName += "Mail";
		}
		else {
			pageName += "Message";
		}

		internalTrackLink(pageName);
	}

	public static void trackItinReload(Type type) {
		String value = type.toString();
		String formatted = value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1).toLowerCase(Locale.US);
		internalTrackLink(String.format(ITIN_RELOAD_TEMPLATE, formatted));
	}

	/**
	 * The new style of tracking "shared itins" via shareable urls.
	 * https://confluence/display/Omniture/Itinerary+Sharing
	 */
	public static void trackItinShareNew(Type type, Intent intent) {
		// Notes on determining type of share, taken from the ShareUtils spec among other places
		// TYPE message/rfc822 - EMAIL
		// TYPE text/plain - MESSAGE
		// class FacebookShareActivity - our Facebook sharing activity

		String shareType;
		if ("com.expedia.bookings.activity.FacebookShareActivity".equals(intent.getComponent().getClassName())) {
			shareType = "Facebook";
		}
		else if ("message/rfc822".equals(intent.getType())) {
			shareType = "Mail";
		}
		else {
			shareType = "Message";
		}

		String itinType;
		if (type == Type.FLIGHT) {
			itinType = "Flight";
		}
		else if (type == Type.HOTEL) {
			itinType = "hotels";
		}
		else {
			boolean isLong = shareType.equals("Mail") ? true : false;
			trackItinShare(type, isLong);
			return;
		}

		String pageName = ITIN + "." + itinType + ".Share." + shareType;

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.setEvar(2, itinType);
		s.setEvents("event48");

		internalTrackLink(s);
	}

	public static void trackHotelItinCancelRoomClick() {
		Log.d(TAG, "Tracking \"" + ITIN_HOTEL_ROOM_CANCEL_CLICK + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, ITIN_HOTEL_ROOM_CANCEL_CLICK);
		s.setProp(16, ITIN_HOTEL_ROOM_CANCEL_CLICK);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//
	// Deep Link Tracking
	//
	// Documentation:
	// https://confluence/display/Omniture/Download+-+Retargeting+-+Deeplink+Campaign+Tracking

	// TODO candidate for ExpediaPointOfSale JSON?
	private static final Set<String> KNOWN_DEEP_LINK_ARGS = new HashSet<String>() {
		{
			add("emlcid");
			add("semcid");
			add("olacid");
			add("affcid");
			add("brandcid");
			add("seocid");
		}
	};

	private static String sDeepLinkKey;
	private static String sDeepLinkValue;

	public static void parseAndTrackDeepLink(Uri data, Set<String> queryData) {
		for (String key : KNOWN_DEEP_LINK_ARGS) {
			if (queryData.contains(key)) {
				setDeepLinkTrackingParams(key, data.getQueryParameter(key));
				break;
			}
		}
	}

	public static void setDeepLinkTrackingParams(String key, String value) {
		sDeepLinkKey = key;
		sDeepLinkValue = value;
	}

	/**
	 * Note: Due to the way that ItineraryManager interacts with our Fragments + Views, this extra bookkeeping is
	 * required currently to correctly fire the single success event of adding a guest itinerary manually.
	 * <p/>
	 * I thought about adding this bookkeeping into ItineraryManager, but I decided not to bloat ItinManager with stuff
	 * that is only needed for tracking purposes.
	 */
	private static Trip mPendingManualAddGuestItin;

	public static void setPendingManualAddGuestItin(String email, String tripNumber) {
		mPendingManualAddGuestItin = new Trip(email, tripNumber);
	}

	public static void trackItinAdd(Trip trip) {
		boolean track = mPendingManualAddGuestItin != null && mPendingManualAddGuestItin.isSameGuest(trip);
		if (track) {
			mPendingManualAddGuestItin = null;
			internalTrackLink(ITIN_ADD_SUCCESS);
		}
	}

	public static void trackItin(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN);

		s.setEvents("event63");

		s.track();
	}

	public static void trackItinHotel(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_HOTEL + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_HOTEL);
		s.setEvents("event63");
		s.track();
	}

	public static void trackItinHotelDirections() {
		internalTrackLink(ITIN_HOTEL_DIRECTIONS);
	}

	public static void trackItinHotelCall() {
		internalTrackLink(ITIN_HOTEL_CALL);
	}

	public static void trackItinHotelInfo() {
		internalTrackLink(ITIN_HOTEL_INFO);
	}

	public static void trackItinInfoClicked(Type type) {
		switch (type) {
		case ACTIVITY:
			trackItinActivityInfo();
			break;
		case CAR:
			trackItinCarInfo();
			break;
		case FLIGHT:
			trackItinFlightInfo();
			break;
		case HOTEL:
			trackItinHotelInfo();
			break;
		case CRUISE:
			//TODO:track cruise info
			break;
		default:
			break;
		}
	}

	public static void trackItinFlight(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_FLIGHT + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_FLIGHT);
		s.setEvents("event63");
		s.track();
	}

	public static void trackItinFlightCheckIn(String airlineCode, boolean isSplitTicket, int tripLegs) {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_CHECKIN);
		s.setEvents("event95");
		s.setProducts(getFlightCheckInProductString(airlineCode, isSplitTicket, tripLegs));
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	private static String getFlightCheckInProductString(String airlineCode, boolean isSplitTicket, int tripLegs) {
		String tripType;
		if (!isSplitTicket) {
			tripType = getOmnitureStringCodeRepresentingTripTypeByNumLegs(tripLegs);
		}
		else {
			tripType = "ST";
		}
		return "Flight;Agency Flight:" + airlineCode + ":" + tripType + ";;";
	}

	public static void trackItinFlightVisitSite() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_CHECKIN_VISIT);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightCheckInSuccess(String airlineCode, boolean isSplitTicket, int flightLegs) {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_CHECKIN_SUCCESS);
		s.setEvents("event96");
		s.setProducts(getFlightCheckInProductString(airlineCode, isSplitTicket, flightLegs));
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightCheckInFailure(String airlineCode, boolean isSplitTicket, int flightLegs) {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_CHECKIN_FAILURE);
		s.setEvents("event97");
		s.setProducts(getFlightCheckInProductString(airlineCode, isSplitTicket, flightLegs));
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}


	public static void trackItinFlightDirections() {
		internalTrackLink(ITIN_FLIGHT_DIRECTIONS);
	}

	public static void trackItinFlightTerminalMaps() {
		internalTrackLink(ITIN_FLIGHT_TERMINAL_MAPS);
	}

	public static void trackItinFlightInfo() {
		internalTrackLink(ITIN_FLIGHT_INFO);
	}

	public static void trackItinFlightCopyPNR() {
		internalTrackLink(ITIN_FLIGHT_COPY_PNR);
	}

	public static void trackItinCar(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_CAR + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_CAR);
		s.setEvents("event63");
		s.track();
	}

	public static void trackItinCarDirections() {
		internalTrackLink(ITIN_CAR_DIRECTIONS);
	}

	public static void trackItinCarCall() {
		internalTrackLink(ITIN_CAR_CALL);
	}

	public static void trackItinCarInfo() {
		internalTrackLink(ITIN_CAR_INFO);
	}

	public static void trackItinActivity(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_ACTIVITY + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_ACTIVITY);
		s.setEvents("event63");
		s.track();
	}

	public static void trackItinActivityRedeem() {
		internalTrackLink(ITIN_ACTIVITY_REDEEM);
	}

	public static void trackItinActivitySupport() {
		internalTrackLink(ITIN_ACTIVITY_SUPPORT);
	}

	public static void trackItinActivityInfo() {
		internalTrackLink(ITIN_ACTIVITY_INFO);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itinerary Notification Click Tracking
	//
	// Spec: https://confluence/display/Omniture/App+Itinerary#AppItinerary-Version31
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String NOTIFICATION_ACTIVITY_START = "Itinerary.Activity.Start";
	private static final String NOTIFICATION_CAR_DROP_OFF = "Itinerary.Car.DropOff";
	private static final String NOTIFICATION_CAR_PICK_UP = "Itinerary.Car.PickUp";
	private static final String NOTIFICATION_FLIGHT_CHECK_IN = "Itinerary.Flight.CheckIn";
	private static final String NOTIFICATION_FLIGHT_SHARE = "Itinerary.Flight.Share";
	private static final String NOTIFICATION_FLIGHT_CANCELLED = "Itinerary.Flight.Cancelled";
	private static final String NOTIFICATION_FLIGHT_GATE_TIME_CHANGE = "Itinerary.Flight.GateTimeChange";
	private static final String NOTIFICATION_FLIGHT_GATE_NUMBER_CHANGE = "Itinerary.Flight.GateNumberChange";
	private static final String NOTIFICATION_FLIGHT_BAGGAGE_CLAIM = "Itinerary.Flight.BaggageClaim";
	private static final String NOTIFICATION_HOTEL_CHECK_IN = "Itinerary.Hotel.CheckIn";
	private static final String NOTIFICATION_HOTEL_CHECK_OUT = "Itinerary.Hotel.CheckOut";
	private static final String NOTIFICATION_FLIGHT_DEPARTURE_REMINDER = "Itinerary.Flight.DepartureReminder";
	private static final String NOTIFICATION_DESKTOP_BOOKING_CONFIRMATION = "Itinerary.Purchase.Confirmation";

	public static void trackNotificationClick(Notification notification) {
		NotificationType type = notification.getNotificationType();
		String link = null;
		switch (type) {
		case ACTIVITY_START:
			link = NOTIFICATION_ACTIVITY_START;
			break;
		case CAR_DROP_OFF:
			link = NOTIFICATION_CAR_DROP_OFF;
			break;
		case CAR_PICK_UP:
			link = NOTIFICATION_CAR_PICK_UP;
			break;
		case FLIGHT_CHECK_IN:
			link = NOTIFICATION_FLIGHT_CHECK_IN;
			break;
		case FLIGHT_SHARE:
			link = NOTIFICATION_FLIGHT_SHARE;
			break;
		case FLIGHT_CANCELLED:
			link = NOTIFICATION_FLIGHT_CANCELLED;
			break;
		case FLIGHT_GATE_TIME_CHANGE:
			link = NOTIFICATION_FLIGHT_GATE_TIME_CHANGE;
			break;
		case FLIGHT_GATE_NUMBER_CHANGE:
			link = NOTIFICATION_FLIGHT_GATE_NUMBER_CHANGE;
			break;
		case FLIGHT_DEPARTURE_REMINDER:
			link = NOTIFICATION_FLIGHT_DEPARTURE_REMINDER;
			break;
		case FLIGHT_BAGGAGE_CLAIM:
			link = NOTIFICATION_FLIGHT_BAGGAGE_CLAIM;
			break;
		case HOTEL_CHECK_IN:
			link = NOTIFICATION_HOTEL_CHECK_IN;
			break;
		case HOTEL_CHECK_OUT:
			link = NOTIFICATION_HOTEL_CHECK_OUT;
			break;
		case DESKTOP_BOOKING:
			link = NOTIFICATION_DESKTOP_BOOKING_CONFIRMATION;
			break;
		default:
			link = "Itinerary." + type.name();
			Log.w(TAG, "Unknown Notification Type \"" + type.name() + "\". Taking a guess.");
			break;
		}

		Log.d(TAG, "Tracking \"" + link + "\" click");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(11, link);
		s.setEvents("event212");

		s.trackLink(null, "o", link, null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Lean Plum Notification Tracking
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void trackLeanPlumNotification(String campaignText) {
		Log.d(TAG, "Tracking LeanPlumNotification \"" + campaignText + "\"");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(11, campaignText);
		s.setEvents("event212");

		s.trackLink(null, "o", "App Notification", null, null);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// In App Messaging Tracking
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void trackLeanPlumInAppMessage(String campaignText) {
		Log.d(TAG, "Tracking LeanPlumNotification \"" + campaignText + "\"");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(25, campaignText);
		s.setEvents("event12");

		s.setProp(16, "App.Push.In-App Message");
		s.setEvar(28, "App.Push.In-App Message");

		s.trackLink(null, "o", "In-App Notification", null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itinerary Notification Click Tracking
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String CROSS_SELL_ITIN_TO_HOTEL = "CrossSell.Itinerary.Hotels";
	private static final String CROSS_SELL_FLIGHT_TO_HOTEL = "CrossSell.Flights.Hotels";
	private static final String CROSS_SELL_LX_FROM_ITIN = "Itinerary.CrossSell.LX";
	private static final String ADD_LX_ITIN = "App.Itin.XSell.LX";

	public static void trackCrossSellItinToHotel() {
		trackCrossSell(CROSS_SELL_ITIN_TO_HOTEL);
	}

	public static void trackCrossSellFlightToHotel() {
		trackCrossSell(CROSS_SELL_FLIGHT_TO_HOTEL);
	}

	private static void trackCrossSell(String link) {
		Log.d(TAG, "Tracking \"" + link + "\"");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(12, link);

		s.trackLink(null, "o", link, null, null);
	}

	public static void trackAddLxItin() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, ADD_LX_ITIN);
		s.setProp(16, ADD_LX_ITIN);
		s.setEvar(12, CROSS_SELL_LX_FROM_ITIN);

		s.trackLink(null, "o", "Itinerary X-Sell", null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Other tracking events
	//
	// This is the place for tracking events that don't quite fit within the hotels/flights/itin confines. Right now,
	// contains tracking events for the launch screen, login, launching of app, ad campaigns, etc...
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String LAUNCH_SCREEN = "App.LaunchScreen";
	private static final String LOGIN_SCREEN = "App.Account.SignIn";
	private static final String LOGIN_SUCCESS = "App.Account.Login.Success";
	private static final String LOGIN_CONTACT_ACCESS = "App.Account.Create.AccessInfo";
	private static final String LOGIN_CONTACT_ACCESS_ALLOWED = "App.Account.Access.Yes";
	private static final String LOGIN_CONTACT_ACCESS_NOT_ALLOWED = "App.Account.Access.NotNow";
	private static final String LOGIN_SEARCH_CONTACTS = "App.Account.Create.SearchContacts";
	private static final String LOGIN_EMAIL_PROMPT = "App.Account.Email.Prompt";
	private static final String LOGIN_EMAIL_PROMPT_EXISTING = "App.Account.Email.SignIn";
	private static final String LOGIN_EMAIL_PROMPT_NEW = "App.Account.Email.CreateNew";
	private static final String LOGIN_CREATE_USERNAME = "App.Account.Create.UserName";
	private static final String LOGIN_CREATE_PASSWORD = "App.Account.Create.Password";
	private static final String LOGIN_TOS = "App.Account.Create.Terms";
	private static final String LOGIN_MARKETING_OPT_IN = "App.Account.Terms.Email.Opt-In";
	private static final String LOGIN_MARKETING_OPT_OUT = "App.Account.Terms.Email.Opt-Out";
	private static final String LOGIN_ACCOUNT_CREATE_SUCCESS = "App.Account.Create.Success";
	private static final String ACCOUNT_SCREEN = "App.Account.MyAccount";
	private static final String ACCOUNT_COUNTRY_SETTING = "App.Account.Settings.Country";
	private static final String ACCOUNT_SUPPORT_WEBSITE = "App.Account.Support.Website";
	private static final String ACCOUNT_SUPPORT_BOOKING = "App.Account.Support.Booking";
	private static final String ACCOUNT_SUPPORT_APP = "App.Account.Support.App";
	private static final String ACCOUNT_COMMUNICATE_RATE = "App.Account.Communicate.Rate";
	private static final String ACCOUNT_COMMUNICATE_HIRING = "App.Account.Communicate.WereHiring";
	private static final String ACCOUNT_LEGAL_CLEAR_DATA = "App.Account.Legal.ClearData";
	private static final String ACCOUNT_LEGAL_TERMS = "App.Account.Legal.Terms";
	private static final String ACCOUNT_LEGAL_PRIVACY = "App.Account.Legal.Privacy";
	private static final String ACCOUNT_LEGAL_ATOL = "App.Account.Legal.ATOL";
	private static final String ACCOUNT_LEGAL_LICENSES = "App.Account.Legal.OpenSourceLicenses";
	private static final String ACCOUNT_APP_DOWNLOAD = "App.Account.Download";
	private static final String ACCOUNT_SIGN_OUT = "App.Account.Logout";


	public static void trackLoginSuccess() {
		ADMS_Measurement s = createTrackLinkEvent(LOGIN_SUCCESS);
		s.setEvents("event26");
		addPOSTpid(s);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackLoginScreen() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SCREEN);
		s.setEvar(18, LOGIN_SCREEN);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppSignInMessagingTest);
		s.track();
	}

	public static void trackLoginContactAccess() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_CONTACT_ACCESS);
		s.setEvar(18, LOGIN_CONTACT_ACCESS);
		s.track();
	}

	public static void trackAllowContactAccess(boolean isAllowed) {
		ADMS_Measurement s = createTrackLinkEvent(
			isAllowed ? LOGIN_CONTACT_ACCESS_ALLOWED : LOGIN_CONTACT_ACCESS_NOT_ALLOWED);
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackEmailPrompt() {
		ADMS_Measurement s = createTrackLinkEvent(LOGIN_EMAIL_PROMPT);
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackEmailPromptChoice(boolean useExisting) {
		ADMS_Measurement s = createTrackLinkEvent(
			useExisting ? LOGIN_EMAIL_PROMPT_EXISTING : LOGIN_EMAIL_PROMPT_NEW);
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackLoginCreateUsername() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_CREATE_USERNAME);
		s.setEvar(18, LOGIN_CREATE_USERNAME);
		s.track();
	}

	public static void trackLoginCreatePassword() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_CREATE_PASSWORD);
		s.setEvar(18, LOGIN_CREATE_PASSWORD);
		s.track();
	}

	public static void trackLoginEmailsQueried() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SEARCH_CONTACTS);
		s.setEvar(18, LOGIN_SEARCH_CONTACTS);
		s.track();
	}

	public static void trackLoginTOS() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_TOS);
		s.setEvar(18, LOGIN_TOS);
		s.track();
	}

	public static void trackMarketingOptIn(boolean optIn) {
		ADMS_Measurement s = createTrackLinkEvent(optIn ? LOGIN_MARKETING_OPT_IN : LOGIN_MARKETING_OPT_OUT);
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackAccountCreateSuccess() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_ACCOUNT_CREATE_SUCCESS);
		s.setEvar(18, LOGIN_ACCOUNT_CREATE_SUCCESS);
		s.setEvents("event25,event26");
		s.track();
	}

	public static void trackAccountCreateError(String error) {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SCREEN);
		s.setEvar(18, LOGIN_SCREEN);
		s.setProp(36, error);
		s.track();
	}

	public static void trackLinkLaunchScreenToHotels() {
		String link = LAUNCH_SCREEN + "." + "Hotel";
		internalTrackLink(link);
	}

	public static void trackLinkLaunchScreenToFlights() {
		String link = LAUNCH_SCREEN + "." + "Flight";
		internalTrackLink(link);
	}

	public static void trackPageLoadLaunchScreen() {
		ADMS_Measurement s = createTrackPageLoadEventBase(LAUNCH_SCREEN);
		boolean isFirstAppLaunch = ExpediaBookingApp.isFirstLaunchEver() || ExpediaBookingApp.isFirstLaunchOfAppVersion();
		if (isFirstAppLaunch && !User.isLoggedIn(sContext)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppShowSignInOnLaunch);
		}
		s.setProp(2, "storefront");
		s.setEvar(2, "storefront");
		s.track();
	}

	public static void trackPageLoadAbacusTestResults() {
		ADMS_Measurement s = getFreshTrackingObject();
		final String link = "LogExperiement";

		addStandardFields(s);

		s.trackLink(null, "o", link, null, null);
	}

	public static void trackAccountPageLoad() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(ACCOUNT_SCREEN);
		s.setEvar(18, ACCOUNT_SCREEN);
		s.track();
	}

	public static void trackClickCountrySetting() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_COUNTRY_SETTING);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickSupportWebsite() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_SUPPORT_WEBSITE);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickSupportBooking() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_SUPPORT_BOOKING);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickSupportApp() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_SUPPORT_APP);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickRateApp() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_COMMUNICATE_RATE);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickWereHiring() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_COMMUNICATE_HIRING);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickClearPrivateData() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_LEGAL_CLEAR_DATA);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickTermsAndConditions() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_LEGAL_TERMS);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickPrivacyPolicy() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_LEGAL_PRIVACY);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickAtolInformation() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_LEGAL_ATOL);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickOpenSourceLicenses() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_LEGAL_LICENSES);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickDownloadAppLink(String appName) {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_APP_DOWNLOAD);
		s.setEvents("event210");
		s.setEvar(32, appName);
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackClickSignOut() {
		ADMS_Measurement s = createTrackLinkEvent(ACCOUNT_SIGN_OUT);
		s.setEvents("event29");
		s.trackLink(null, "o", "Accounts", null, null);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Tracking events for new launch screen
	//
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String LAUNCH_SCREEN_LOB_NAVIGATION = "App.LS.Srch";
	private static final String LAUNCH_SEARCH = "Launch.Search";
	private static final String LAUNCH_DEALS_TILE = "App.LS.Promo";
	private static final String LAUNCH_MESSAGING = "Launch.TopDeals.Hotel";

	private static final String HOTEL_LOB_NAVIGATION = "Hotel";
	private static final String FLIGHT_LOB_NAVIGATION = "Flight";
	private static final String CAR_LOB_NAVIGATION = "Car";
	private static final String LX_LOB_NAVIGATION = "LX";
	private static final String TRANSPORT_LOB_NAVIGATION = "Transport";

	public static void trackNewLaunchScreenLobNavigation(LineOfBusiness lob) {

		String lobString = "";
		switch (lob) {
		case HOTELS:
			lobString = HOTEL_LOB_NAVIGATION;
			break;
		case FLIGHTS:
			lobString = FLIGHT_LOB_NAVIGATION;
			break;
		case CARS:
			lobString = CAR_LOB_NAVIGATION;
			break;
		case LX:
			lobString = LX_LOB_NAVIGATION;
			break;
		case TRANSPORT:
			lobString = TRANSPORT_LOB_NAVIGATION;
			break;
		}
		String link = LAUNCH_SCREEN_LOB_NAVIGATION + "." + lobString;

		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(12, LAUNCH_SEARCH + "." + lobString);
		s.setEvar(28, link);
		s.setProp(16, link);

		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackNewLaunchScreenSeeAllClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		addCommonLaunchScreenFields(s, LAUNCH_MESSAGING, "SeeAll");

		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackNewLaunchScreenTileClick(boolean isLaunchCollection) {
		String launchMessage = "";
		if (isLaunchCollection) {
			launchMessage = "Launch.StaffPick.Hotel";
		}
		else {
			launchMessage = "Launch.TopDeals.Hotel";
		}
		ADMS_Measurement s = getFreshTrackingObject();
		addCommonLaunchScreenFields(s, launchMessage, "DealTile");

		s.trackLink(null, "o", "App Landing", null, null);
	}

	private static void addCommonLaunchScreenFields(ADMS_Measurement s, String launchMessage,
													String tileType) {

		s.setEvar(28, LAUNCH_DEALS_TILE + "." + tileType);
		s.setProp(16, LAUNCH_DEALS_TILE + "." + tileType);
		s.setEvar(12, launchMessage);
	}

	public static void trackCrash(Throwable ex) {
		// Log the crash
		Log.d(TAG, "Tracking \"crash\" onClick");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvents("event39");
		s.setEvar(28, "App.Crash");
		s.setProp(16, "App.Crash");

		final Writer writer = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		s.setProp(36, ex.getMessage() + "|" + writer.toString());

		Log.i("prop36: " + s.getProp(36));

		trackOnClick(s);
	}

	private static final String TRACK_VERSION = "tracking_version"; // The SettingUtils key for the last version tracked

	public static void trackAppLoading(Context context) {
		Log.d(TAG, "Tracking \"App.Loading\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setAppState("App.Loading");

		// Determine if this is a new install, an upgrade, or just a regular launch
		String trackVersion = SettingUtils.get(context, TRACK_VERSION, null);
		String currentVersion = BuildConfig.VERSION_NAME;

		boolean save = false;
		if (trackVersion == null) {
			// New install
			s.setEvents("event28");
			save = true;
		}
		else if (!trackVersion.equals(currentVersion)) {
			// App was upgraded
			s.setEvents("event29");
			save = true;
		}

		if (save) {
			// Save new data
			SettingUtils.save(context, TRACK_VERSION, currentVersion);
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppInstallCustom(Context context) {
		Log.d(TAG, "Tracking \"App Install\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject();

		sMarketingDate = ISODateTimeFormat.date().print(DateTime.now());
		SettingUtils.save(context, context.getString(R.string.preference_marketing_date), sMarketingDate);

		s.setEvar(10, sMarketingDate);
		s.setEvar(28, "App Install");

		s.track();
	}

	public static void trackGooglePlayReferralLink(Context context, Intent intent) {
		ADMS_ReferrerHandler handler = new ADMS_ReferrerHandler();
		handler.processIntent(context, intent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Public utility tracking events (this pattern has been deprecated)
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Most tracking events are pretty simple and can be captured by these few fields.  This method handles
	 * both onClick and pageLoad events (depending on whether pageName is supplied).
	 *
	 * @param pageName   the page name if this is a pageLoad event; for onClick, this should be null
	 * @param events     The "events" variable, if one needs to be set.  Can be null.
	 * @param referrerId The "referrer" for an event.  Typically this is the name of the onClick event.
	 */
	public static void trackSimpleEvent(String pageName, String events, String referrerId) {
		ADMS_Measurement s = createSimpleEvent(pageName, events, referrerId);

		// Handle the tracking different for pageLoads and onClicks.
		// If there is no pageName, it is an onClick (by default)
		if (pageName != null) {
			s.track();
		}
		else {
			trackOnClick(s);
		}
	}

	// Simplified method for tracking error pages
	public static void trackErrorPage(String errorName) {
		Log.d("Tracking \"App.Error." + errorName + "\" pageLoad.");
		trackSimpleEvent("App.Error." + errorName, "event38", null);
	}

	private static void trackOnClick(ADMS_Measurement s) {
		internalTrackLink(s);
	}

	private static ADMS_Measurement createSimpleEvent(String pageName, String events,
													  String referrerId) {
		ADMS_Measurement s = OmnitureTracking.getFreshTrackingObject();


		s.setAppState(pageName);

		if (events != null) {
			s.setEvents(events);
		}

		if (referrerId != null) {
			s.setProp(16, referrerId);
		}

		return s;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private helper methods

	/**
	 * Method that returns a tracking object without any parameters set to be manipulating for tracking. Because the
	 * library uses a static object for tracking, clearVars() must be called in order to remove potentially set
	 * variables from being erroneously sent along with the tracking call.
	 */
	private static ADMS_Measurement getFreshTrackingObject() {
		ADMS_Measurement s = ADMS_Measurement.sharedInstance(sContext);
		s.clearVars();
		addStandardFields(s);
		return s;
	}

	private static void addStandardFields(ADMS_Measurement s) {
		// Add debugging flag if not release
		if (BuildConfig.DEBUG || DebugUtils.isLogEnablerInstalled(sContext)) {
			s.setDebugLogging(true);
		}

		// Add offline tracking, so user doesn't have to be online to be tracked
		if (ExpediaBookingApp.isAutomation()) {
			s.setOfflineTrackingEnabled(false);
			s.setOffline();
			if (!ExpediaBookingApp.isRobolectric()) {
				s.clearTrackingQueue();
			}
		}
		else {
			s.setOfflineTrackingEnabled(true);
			s.setOnline();
		}

		// account
		s.setReportSuiteIDs(getReportSuiteIds());

		// Marketing date tracking
		s.setEvar(10, sMarketingDate);

		// Deep Link tracking
		addDeepLinkData(s);

		// Server
		s.setTrackingServer(getTrackingServer(sContext));
		s.setSSL(false);

		// Add the country locale
		s.setEvar(31, Locale.getDefault().getCountry());

		// Experience segmentation
		boolean usingTabletInterface = (ExpediaBookingApp.useTabletInterface(sContext));
		s.setEvar(50, (usingTabletInterface) ? "app.tablet.android" : "app.phone.android");

		// TPID
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		if (PointOfSale.getPointOfSale().getEAPID() != PointOfSale.INVALID_EAPID) {
			s.setProp(7, PointOfSale.getPointOfSale().getTpid() + "-" + PointOfSale.getPointOfSale().getEAPID());
		}

		// Unique device id
		String id = AdvertisingIdUtils.getIDFA();
		if (id != null) {
			s.setProp(12, id);
		}

		// Device local time
		s.setEvar(60, sFormatter.print(DateTime.now()));

		// App version
		s.setProp(35, BuildConfig.VERSION_NAME);

		// Language/locale
		s.setProp(37, Locale.getDefault().getLanguage());

		String email = null;
		String expediaId = null;
		String rewardsStatus = null;
		String tuid = null;
		// If the user is logged in, we want to send their email address along with request
		if (User.isLoggedIn(sContext)) {
			// Load the user into the Db if it has not been done (which will most likely be the case on app launch)
			if (Db.getUser() == null) {
				Db.loadUser(sContext);
			}
			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null) {
				email = Db.getUser().getPrimaryTraveler().getEmail();
				expediaId = Db.getUser().getExpediaUserId();
				rewardsStatus = getRewardsStatusString(Db.getUser());
				tuid = Db.getUser().getTuidString();
			}
		}

		if (!TextUtils.isEmpty(tuid)) {
			s.setProp(14, tuid);
		}

		if (TextUtils.isEmpty(rewardsStatus)) {
			rewardsStatus = "notRewardsMember";
		}

		// If the email is still null, check against the BillingInfo in Db which is populated from manual forms
		if (TextUtils.isEmpty(email)) {
			if (Db.hasBillingInfo()) {
				email = Db.getBillingInfo().getEmail();
			}
		}

		if (!TextUtils.isEmpty(email)) {
			s.setProp(11, md5(email));
		}

		if (!TextUtils.isEmpty(expediaId)) {
			s.setProp(13, expediaId);
		}

		String evar55 = User.isLoggedIn(sContext) ? "loggedin | hard" : "unknown user";
		s.setEvar(55, evar55);

		s.setEvar(56, rewardsStatus);

		// TripBucket State
		if (Db.getTripBucket() != null) {
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			String hotelState = hotel == null ? "No Hotel" : (hotel.getState() == TripBucketItemState.PURCHASED ? "Hotel" : "UB Hotel");

			TripBucketItemFlight flight = Db.getTripBucket().getFlight();
			String flightState = flight == null ? "No Flight" : (flight.getState() == TripBucketItemState.PURCHASED ? "Flight" : "UB Flight");

			String tbState = hotelState + "|" + flightState;
			s.setEvar(23, tbState);

			// Air attach state
			boolean userIsAttachEligible = Db.getTripBucket() != null && Db.getTripBucket().isUserAirAttachQualified();
			String airAttachState = userIsAttachEligible ? "Attach|Hotel Eligible" : "Attach|Non Eligible";
			s.setEvar(65, airAttachState);
		}

		String tpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		String posEapid = Integer.toString(PointOfSale.getPointOfSale().getEAPID());
		s.setEvar(61, tpid);
		if (PointOfSale.getPointOfSale().getEAPID() != PointOfSale.INVALID_EAPID) {
			s.setEvar(61, tpid + "-" + posEapid);
		}

		// Screen orientation
		Configuration config = sContext.getResources().getConfiguration();
		switch (config.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			s.setProp(39, "landscape");
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			s.setProp(39, "portrait");
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			s.setProp(39, "undefined");
			break;
		}

		int permissionCheck = ContextCompat.checkSelfPermission(sContext,
			Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			// User location
			android.location.Location bestLastLocation = LocationServices.getLastBestLocation(sContext, 0);
			if (bestLastLocation != null) {
				s.setProp(40, bestLastLocation.getLatitude() + "," + bestLastLocation.getLongitude() + "|"
					+ bestLastLocation.getAccuracy());
			}
		}
	}

	private static void internalTrackPageLoadEventStandard(String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventBase(pageName).track();
	}

	private static void internalTrackPageLoadEventStandard(String pageName, LineOfBusiness lob) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		String lobString = getLobString(lob);
		s.setEvar(2, lobString);
		s.setProp(2, lobString);
		s.track();
	}

	private static void internalTrackPageLoadEventPriceChange(String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventPriceChange(pageName).track();
	}

	private static void internalTrackLink(String link) {
		ADMS_Measurement s = createTrackLinkEvent(link);
		internalTrackLink(s);
	}

	private static void internalTrackLink(ADMS_Measurement s) {
		Log.d(TAG, "Tracking \"" + s.getProp(16) + "\" linkClick");
		s.trackLink(null, "o", s.getEvar(28), null, null);
	}

	private static ADMS_Measurement createTrackLinkEvent(String link) {
		ADMS_Measurement s = getFreshTrackingObject();


		// link
		s.setEvar(28, link);
		s.setProp(16, link);

		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventBase(String pageName) {
		ADMS_Measurement s = getFreshTrackingObject();

		// set the pageName
		s.setAppState(pageName);
		s.setEvar(18, pageName);


		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventPriceChange(String pageName) {
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();

		// This is only to be included when there is a price change shown on the page. This should be the % increase or
		// decrease in price. Round to whole integers.
		String priceChange = trip.computePercentagePriceChangeForOmnitureTracking();
		if (priceChange != null) {
			s.setEvents("event62");
			s.setProp(9, priceChange);
		}

		return s;
	}

	private static void addStandardHotelFields(ADMS_Measurement s, HotelSearchParams searchParams) {
		s.setEvar(2, "hotels");
		s.setProp(2, "hotels");
		s.setEvar(6, Integer.toString(searchParams.getStayDuration()));
		internalSetHotelDateProps(s, searchParams);
	}

	private static void addStandardFlightFields(ADMS_Measurement s) {
		s.setEvar(2, "Flight");
		s.setProp(2, "Flight");
	}

	private static void addAdvancePurchaseWindow(ADMS_Measurement s, HotelSearchParams searchParams) {
		String window = Integer.toString(JodaUtils.daysBetween(LocalDate.now(), searchParams.getCheckInDate()));
		s.setEvar(5, window);
		s.setProp(5, window);
	}

	private static String getLobString(LineOfBusiness lob) {
		return (lob == LineOfBusiness.HOTELS ? "hotels" : "Flight");
	}

	private static String getRewardsStatusString(User user) {
		LoyaltyMembershipTier userTier = user.getPrimaryTraveler().getLoyaltyMembershipTier();
		if (userTier == null || userTier == LoyaltyMembershipTier.NONE) {
			return null;
		}
		else {
			return userTier.toApiValue().toLowerCase(Locale.US);
		}
	}

	private static void addDeepLinkData(ADMS_Measurement s) {
		if (sDeepLinkKey != null && sDeepLinkValue != null) {
			String var;

			if (sDeepLinkKey.equals("emlcid")) {
				var = "EML.";
			}
			else if (sDeepLinkKey.equals("semcid")) {
				var = "SEM.";
			}
			else if (sDeepLinkKey.equals("olacid")) {
				var = "OLA.";
			}
			else if (sDeepLinkKey.equals("affcid")) {
				var = "AFF.";
			}
			else if (sDeepLinkKey.equals("brandcid")) {
				var = "Brand.";
			}
			else if (sDeepLinkKey.equals("seocid")) {
				var = "SEO.";
			}
			else {
				Log.w(TAG, "Received Deep Link tracking parameters we don't know how to handle. Ignoring");
				sDeepLinkKey = null;
				sDeepLinkValue = null;
				return;
			}

			int evar = 22;
			var += sDeepLinkValue;
			s.setEvar(evar, var);

			sDeepLinkKey = null;
			sDeepLinkValue = null;
		}
	}

	private static String getReportSuiteIds() {
		if (BuildConfig.RELEASE) {
			return "expediaglobalapp";
		}
		else {
			return "expediaglobalappdev";
		}
	}

	private static String getTrackingServer(Context context) {
		EndPoint endpoint = Ui.getApplication(context).appComponent().endpointProvider().getEndPoint();
		if (endpoint == EndPoint.CUSTOM_SERVER) {
			return SettingUtils.get(context, context.getString(R.string.preference_proxy_server_address),
				"localhost:3000");
		}
		else {
			return context.getString(R.string.omniture_tracking_server_url);
		}
	}

	private static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte[] messageDigest = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			return hexString.toString();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	// This method will eventually become more useful when we support multi-destination flights
	private static String getOmnitureStringCodeRepresentingTripTypeByNumLegs(final int numLegs) {
		switch (numLegs) {
		case 1:
			return "OW";
		case 2:
			return "RT";
		default:
			return "MD";
		}
	}

	private static String getEvar47String(FlightSearchParams params) {
		// Pipe delimited list of LOB, flight search type (OW, RT, MD), # of Adults, and # of Children)
		// e.g. FLT|RT|A2|C1
		String str = "FLT|";
		if (params.isRoundTrip()) {
			str += "RT|A";
		}
		else {
			str += "OW|A";
		}

		str += params.getNumAdults();
		str += "|C";
		str += params.getNumberOfSeatedChildren();
		str += "|L";
		str += (params.getNumChildren() - params.getNumberOfSeatedChildren());

		return str;
	}

	private static String getPaymentType() {
		BillingInfo billingInfo = Db.getBillingInfo();
		StoredCreditCard scc = billingInfo.getStoredCard();
		PaymentType type;
		if (scc != null) {
			type = scc.getType();
		}
		else {
			type = CurrencyUtils.detectCreditCardBrand(billingInfo.getNumber());
		}

		if (type != null) {
			switch (type) {
			case CARD_AMERICAN_EXPRESS:
				return "AmericanExpress";
			case CARD_CARTE_BLANCHE:
				return "CarteBlanche";
			case CARD_CHINA_UNION_PAY:
				return "ChinaUnionPay";
			case CARD_DINERS_CLUB:
				return "DinersClub";
			case CARD_DISCOVER:
				return "Discover";
			case CARD_JAPAN_CREDIT_BUREAU:
				return "JapanCreditBureau";
			case CARD_MAESTRO:
				return "Maestro";
			case CARD_MASTERCARD:
				return "MasterCard";
			case CARD_VISA:
				return "Visa";
			case CARD_CARTE_BLEUE:
				return "CarteBleue";
			case CARD_CARTA_SI:
				return "CartaSi";
			case UNKNOWN:
				return "Unknown";
			}
		}

		return "Unknown";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Car Tracking
	//
	// Spec: https://confluence/display/Omniture/Mobile+App%3A+Cars
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String CAR_LOB = "cars";
	private static final String CAR_DATE_FORMAT = "HHmm";

	private static final String CAR_DEST_SEARCH = "App.Cars.Dest-Search";
	private static final String CAR_NO_RESULT = "App.Cars.NoResults";
	private static final String CAR_SEARCH = "App.Cars.Search";
	private static final String CAR_FILTERS = "App.Cars.Search.Filter";
	private static final String CAR_RATE_DETAIL = "App.Cars.RateDetails";
	private static final String CAR_VIEW_DETAILS = "App.Cars.RD.ViewDetails";
	private static final String CAR_VIEW_MAP = "App.Cars.RD.ViewMap";
	private static final String CAR_CHECKOUT_PAGE = "App.Cars.Checkout.Info";
	private static final String CAR_CHECKOUT_TRAVELER_INFO = "App.Cars.Checkout.Traveler.Edit.Info";
	private static final String CAR_CHECKOUT_PAYMENT_INFO = "App.Cars.Checkout.Payment.Edit.Info";
	private static final String CAR_CHECKOUT_SLIDE_TO_PURCHASE = "App.Cars.Checkout.SlideToPurchase";
	private static final String CAR_CHECKOUT_CVV_SCREEN = "App.Cars.Checkout.Payment.CID";
	private static final String CAR_CHECKOUT_CONFIRMATION = "App.Cars.Checkout.Confirmation";
	private static final String CAR_CHECKOUT_CONFIRMATION_CROSS_SELL = "App.Cars.CKO.Confirm.Xsell";

	public static void trackAppCarSearchBox() {
		Log.d(TAG, "Tracking \"" + CAR_DEST_SEARCH + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_DEST_SEARCH);
		s.track();
	}

	public static void trackAppCarNoResults(ApiError apiError) {
		Log.d(TAG, "Tracking \"" + CAR_NO_RESULT + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_NO_RESULT);
		if (apiError.errorInfo != null && (Strings.isNotEmpty(apiError.errorInfo.cause) || Strings
			.isNotEmpty(apiError.errorInfo.summary))) {
			s.setProp(36,
				Strings.isNotEmpty(apiError.errorInfo.cause) ? apiError.errorInfo.cause : apiError.errorInfo.summary);
		}
		s.track();
	}

	public static void trackAppCarSearch(CarSearchParams carSearchParams, int resultSize) {
		Log.d(TAG, "Tracking \"" + CAR_SEARCH + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_SEARCH);

		boolean isOffAirportSearch = carSearchParams.shouldSearchByLocationLatLng();
		// Success event for Product Search, Local Expert Search
		s.setEvents(isOffAirportSearch ? "event30,event52,event59" : "event30,event52");

		//Number of results
		s.setProp(1, Integer.toString(resultSize));

		//Search Origin
		s.setEvar(3, "D=c3");
		s.setProp(3, "CAR:" + (isOffAirportSearch ? "Non-Airport" : carSearchParams.origin));

		//Search Destination
		s.setProp(4, "CAR:" + (isOffAirportSearch ? "Non-Airport" : carSearchParams.origin));
		s.setEvar(4, "D=c4");

		setDateValues(s, carSearchParams.startDateTime.toLocalDate(), carSearchParams.endDateTime.toLocalDate());

		s.setEvar(47, getEvar47String(carSearchParams));
		s.setEvar(48, carSearchParams.originDescription);

		s.track();
	}

	public static void trackAppCarFilter() {
		Log.d(TAG, "Tracking \"" + CAR_FILTERS + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_FILTERS);
		s.track();
	}

	public static void trackAppCarFilterUsage(String filter) {
		Log.d(TAG, "Tracking \"" + CAR_FILTERS + "." + filter + "\" trackLink...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvar(28, CAR_FILTERS + "." + filter);
		s.setProp(16, CAR_FILTERS + "." + filter);

		s.trackLink(null, "o", "Car Search", null, null);
	}

	public static void trackAppCarRateDetails(SearchCarOffer offer) {
		Log.d(TAG, "Tracking \"" + CAR_RATE_DETAIL + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_RATE_DETAIL);

		s.setEvents("event4");
		String evar38String = Strings.capitalizeFirstLetter(offer.vehicleInfo.category.toString()) + ":" + Strings
			.capitalizeFirstLetter(offer.vehicleInfo.type.toString().replaceAll("_", " "));

		s.setEvar(38, evar38String);
		s.track();
	}

	public static void trackAppCarViewDetails() {
		Log.d(TAG, "Tracking \"" + CAR_RATE_DETAIL + ".sh" + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvar(28, CAR_VIEW_DETAILS);
		s.setProp(16, CAR_VIEW_DETAILS);
		s.trackLink(null, "o", "Car Details", null, null);
	}

	public static void trackAppCarMapClick() {
		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvar(28, CAR_VIEW_MAP);
		s.setProp(16, CAR_VIEW_MAP);
		s.trackLink(null, "o", "Car Details", null, null);
	}

	public static void trackAppCarCheckoutPage(CreateTripCarOffer carOffer) {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_PAGE + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_CHECKOUT_PAGE);

		s.setEvents("event73");
		s.setCurrencyCode(carOffer.detailedFare.grandTotal.getCurrency());
		trackAbacusTest(s, AbacusUtils.EBAndroidAppCarInsuranceIncludedCKO);
		s.track();
	}

	public static void trackAppCarCheckoutTraveler() {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_TRAVELER_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(CAR_CHECKOUT_TRAVELER_INFO);
		s.track();

	}

	public static void trackAppCarCheckoutPayment() {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_PAYMENT_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(CAR_CHECKOUT_PAYMENT_INFO);
		s.setEvar(18, CAR_CHECKOUT_PAYMENT_INFO);
		s.track();

	}

	public static void trackAppCarCheckoutSlideToPurchase(String cardType) {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(CAR_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(18, CAR_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, cardType);
		s.track();

	}

	public static void trackAppCarCheckoutCvvScreen() {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_CVV_SCREEN + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(CAR_CHECKOUT_CVV_SCREEN);
		s.setEvar(18, CAR_CHECKOUT_CVV_SCREEN);

		s.track();

	}

	public static void trackAppCarCheckoutConfirmation(CarCheckoutResponse carCheckoutResponse) {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_CONFIRMATION + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_CHECKOUT_CONFIRMATION);

		s.setEvents("purchase");
		s.setCurrencyCode(carCheckoutResponse.totalChargesPrice.currencyCode);
		s.setPurchaseID("onum" + carCheckoutResponse.orderId);
		addProducts(s, carCheckoutResponse.newCarProduct, carCheckoutResponse.trackingData);
		setEvar30(s, carCheckoutResponse);

		s.setProp(71, carCheckoutResponse.newTrip.travelRecordLocator);
		s.setProp(72, carCheckoutResponse.orderId);
		s.track();

	}

	public static void trackAppCarCheckoutConfirmationCrossSell(LineOfBusiness lob) {
		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvar(12,
			lob == LineOfBusiness.HOTELS ? "CrossSell.Cars.Confirm.Hotels" : "CrossSell.Cars.Confirm.Flights");
		s.setEvar(28, CAR_CHECKOUT_CONFIRMATION_CROSS_SELL);
		s.setProp(16, CAR_CHECKOUT_CONFIRMATION_CROSS_SELL);
		s.trackLink(null, "o", "Confirmation Cross Sell", null, null);
	}

	private static void addProducts(ADMS_Measurement s, CreateTripCarOffer carOffer, CarTrackingData carTrackingData) {
		String duration = Integer
			.toString(JodaUtils.daysBetween(carOffer.getPickupTime(), carOffer.getDropOffTime()) + 1);
		s.setProducts(
			"Car;Agency Car:" + carOffer.vendor.code + ":" + carTrackingData.sippCode + ";" + duration + ";"
				+ carOffer.detailedFare.grandTotal.amount);
	}

	private static String getEvar47String(CarSearchParams params) {
		StringBuilder sb = new StringBuilder("CAR|RT|");
		SimpleDateFormat sdf = new SimpleDateFormat(CAR_DATE_FORMAT, Locale.US);
		sb.append(sdf.format(params.startDateTime.toDate()));
		sb.append("|");
		sb.append(sdf.format(params.endDateTime.toDate()));
		return sb.toString();
	}

	private static void setEvar30(ADMS_Measurement s, CarCheckoutResponse carCheckoutResponse) {
		String pickUpLocation = carCheckoutResponse.newCarProduct.pickUpLocation.locationCode;
		String dropOffLocation = carCheckoutResponse.newCarProduct.dropOffLocation.locationCode;

		StringBuilder sb = new StringBuilder("Car: ");
		sb.append(pickUpLocation).append('-');
		sb.append(dropOffLocation);
		sb.append(':');
		sb.append(carCheckoutResponse.newCarProduct.getPickupTime().toLocalDate().toString(EVAR30_DATE_FORMAT));
		sb.append('-')
			.append(carCheckoutResponse.newCarProduct.getDropOffTime().toLocalDate().toString(EVAR30_DATE_FORMAT));


		s.setEvar(30, sb.toString());
	}

	private static ADMS_Measurement internalTrackAppCar(String pageName) {
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(pageName);
		s.setEvar(18, pageName);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, CAR_LOB);
		return s;
	}

	public static void trackCheckoutPayment(LineOfBusiness lineOfBusiness) {
		if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
			trackAppCarCheckoutPayment();
		}
		else if (lineOfBusiness.equals(LineOfBusiness.LX) || lineOfBusiness.equals(LineOfBusiness.TRANSPORT)) {
			trackAppLXCheckoutPayment(lineOfBusiness);
		}
	}

	public static void trackCheckoutTraveler(LineOfBusiness lineOfBusiness) {
		if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
			trackAppCarCheckoutTraveler();
		}
		else if (lineOfBusiness.equals(LineOfBusiness.LX) || lineOfBusiness.equals(LineOfBusiness.TRANSPORT)) {
			trackAppLXCheckoutTraveler(lineOfBusiness);
		}
	}

	// Pay with points tracking
	private static final String PAY_WITH_POINTS_CUSTOM_LINK_NAME = "Pay With Points";

	public static void trackPayWithPointsAmountUpdateSuccess(int percentagePaidWithPoints) {
		Log.d(TAG, "Tracking \"" + REWARDS_POINTS_UPDATE);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, REWARDS_POINTS_UPDATE);
		s.setProp(16, REWARDS_POINTS_UPDATE);

		String rewardAppliedPercentage = ProductFlavorFeatureConfiguration.getInstance()
			.getOmnitureEventValue(OmnitureEventName.REWARD_APPLIED_PERCENTAGE_TEMPLATE);
		s.setEvar(53, String.format(Locale.getDefault(), rewardAppliedPercentage, percentagePaidWithPoints));
		s.trackLink(null, "o", PAY_WITH_POINTS_CUSTOM_LINK_NAME, null, null);
	}

	public static void trackPayWithPointsDisabled() {
		Log.d(TAG, "Tracking \"" + PAY_WITH_POINTS_DISABLED);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, PAY_WITH_POINTS_DISABLED);
		s.setProp(16, PAY_WITH_POINTS_DISABLED);
		s.setEvar(53, ProductFlavorFeatureConfiguration.getInstance().getOmnitureEventValue(OmnitureEventName.NO_REWARDS_USED));
		s.trackLink(null, "o", PAY_WITH_POINTS_CUSTOM_LINK_NAME, null, null);
	}

	public static void trackPayWithPointsReEnabled(int percentagePaidWithPoints) {
		String payWithPointsReenabled =
			"App.Hotels.CKO.Points.Select." + ProductFlavorFeatureConfiguration.getInstance()
				.getOmnitureEventValue(OmnitureEventName.BRAND_KEY_FOR_OMNITURE);
		Log.d(TAG, "Tracking \"" + payWithPointsReenabled);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, payWithPointsReenabled);
		s.setProp(16, payWithPointsReenabled);
		String rewardAppliedPercentage = ProductFlavorFeatureConfiguration.getInstance()
			.getOmnitureEventValue(OmnitureEventName.REWARD_APPLIED_PERCENTAGE_TEMPLATE);
		s.setEvar(53, String.format(Locale.getDefault(), rewardAppliedPercentage, percentagePaidWithPoints));
		s.trackLink(null, "o", PAY_WITH_POINTS_CUSTOM_LINK_NAME, null, null);
	}

	public static void trackPayWithPointsError(String errorMessage) {
		Log.d(TAG, "Tracking \"" + PAY_WITH_POINTS_ERROR);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, PAY_WITH_POINTS_ERROR);
		s.setProp(16, PAY_WITH_POINTS_ERROR);
		s.setProp(36, errorMessage);
		s.trackLink(null, "o", PAY_WITH_POINTS_CUSTOM_LINK_NAME, null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Packages tracking
	//
	// https://confluence/display/Omniture/Mobile+App%3A+Flight+and+Hotel+Package
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String PACKAGES_LOB = "package:FH";
	private static final String PACKAGES_CHECKOUT_INFO = "App.Package.Checkout.Info";
	private static final String PACKAGES_DESTINATION_SEARCH = "App.Package.Dest-Search";
	private static final String PACKAGES_SEARCH_TRAVELER_PICKER_CLICK_TEMPLATE = "App.Package.Traveler.";
	private static final String PACKAGES_HOTEL_SEARCH_RESULT_LOAD = "App.Package.Hotels.Search";
	private static final String PACKAGES_HOTEL_SEARCH_ZERO_RESULT_LOAD = "App.Package.Hotels-Search.NoResults";
	private static final String PACKAGES_HOTEL_SEARCH_SPONSORED_PRESENT = "App.Package.Hotels.Search.Sponsored.YES";
	private static final String PACKAGES_HOTEL_SEARCH_SPONSORED_NOT_PRESENT = "App.Package.Hotels.Search.Sponsored.NO";
	private static final String PACKAGES_HOTEL_SEARCH_MAP_LOAD = "App.Package.Hotels.Search.Map";
	private static final String PACKAGES_HOTEL_MAP_TO_LIST_VIEW = "App.Package.Hotels.Search.Expand.List";
	private static final String PACKAGES_HOTEL_MAP_PIN_TAP = "App.Package.Hotels.Search.TapPin";
	private static final String PACKAGES_HOTEL_CAROUSEL_TAP = "App.Package.Hotels.Search.Expand.Package";
	private static final String PACKAGES_HOTEL_MAP_SEARCH_AREA = "App.Package.Hotels.Search.AreaSearch";
	private static final String PACKAGES_HOTEL_MAP_CAROUSEL_SCROLL = "App.Package.Hotels.Search.ShowNext";
	private static final String PACKAGES_CHECKOUT_PAYMENT_SELECT = "App.Package.Checkout.Payment.Select";
	private static final String PACKAGES_CHECKOUT_PAYMENT_EDIT = "App.Package.Checkout.Payment.Edit.Card";
	private static final String PACKAGES_CHECKOUT_PAYMENT_SELECT_STORED_CC = "App.Package.CKO.Payment.StoredCard";
	private static final String PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION = "App.Package.Checkout.Confirmation";

	private static final String PACKAGES_HOTEL_RT_OUT_RESULTS = "App.Package.Flight.Search.Roundtrip.Out";
	private static final String PACKAGES_HOTEL_RT_IN_RESULTS = "App.Package.Flight.Search.Roundtrip.In";
	private static final String PACKAGES_HOTEL_RT_OUT_DETAILS = "App.Package.Flight.Search.Roundtrip.Out.Details";
	private static final String PACKAGES_HOTEL_RT_IN_DETAILS = "App.Package.Flight.Search.Roundtrip.In.Details";

	private static final String PACKAGES_HOTEL_DETAILS_LOAD = "App.Package.Hotels.Infosite";
	private static final String PACKAGES_HOTEL_DETAILS_REVIEWS = "App.Package.Reviews";
	private static final String PACKAGES_HOTEL_DETAILS_REVIEWS_CATEGORY_TEMPLATE = "App.Package.Reviews.";
	private static final String PACKAGES_HOTEL_DETAILS_RESORT_FEE_INFO = "App.Package.ResortFeeInfo";
	private static final String PACKAGES_HOTEL_DETAILS_RENOVATION_INFO = "App.Package.RenovationInfo";
	private static final String PACKAGES_HOTEL_DETAILS_SELECT_ROOM_TEMPLATE = "App.Package.Infosite.SelectRoom.";
	private static final String PACKAGES_HOTEL_DETAILS_BOOK_BY_PHONE = "App.Package.Infosite.BookPhone";
	private static final String PACKAGES_HOTEL_DETAILS_VIEW_ROOM = "App.Package.Hotels.IS.ViewRoom";
	private static final String PACKAGES_HOTEL_DETAILS_BOOK_ROOM = "App.Package.Hotels.IS.BookRoom";
	private static final String PACKAGES_HOTEL_DETAILS_ROOM_INFO = "App.Package.Hotels.IS.MoreRoomInfo";
	private static final String PACKAGES_HOTEL_DETAILS_MAP = "App.Package.Infosite.Map";
	private static final String PACKAGES_HOTEL_DETAILS_MAP_SELECT_ROOM = "App.Package.IS.Map.SelectRoom";

	private static final String PACKAGES_HOTELS_SEARCH_REFINE = "App.Package.Hotels.Search.Filter";
	private static final String PACKAGES_HOTELS_SORT_BY_TEMPLATE = "App.Package.Hotels.Search.Sort.";
	private static final String PACKAGES_HOTELS_FILTER_PRICE = "App.Package.Hotels.Search.Price";
	private static final String PACKAGES_HOTELS_FILTER_VIP_TEMPLATE = "App.Package.Hotels.Search.Filter.VIP.";
	private static final String PACKAGES_HOTELS_FILTER_NEIGHBOURHOOD = "App.Package.Hotels.Search.Neighborhood";
	private static final String PACKAGES_HOTELS_FILTER_BY_NAME = "App.Package.Hotels.Search.PackageName";
	private static final String PACKAGES_HOTELS_FILTER_CLEAR = "App.Package.Hotels.Search.ClearFilter";

	private static final String PACKAGES_BUNDLE_OVERVIEW_LOAD = "App.Package.RateDetails";
	private static final String PACKAGES_BUNDLE_OVERVIEW_PRODUCT_EXPAND_TEMPLATE = "App.Package.RD.ViewDetails.";
	private static final String PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN = "App.Package.RD.TotalCost";
	private static final String PACKAGES_BUNDLE_EDIT = "App.Package.RD.Edit";

	private static final String PACKAGES_FLIGHT_BAGGAGE_FEE_CLICK = "App.Package.Flight.Search.BaggageFee";
	private static final String PACKAGES_FLIGHT_SORT_FILTER_LOAD = "App.Package.Flight.Search.Filter";
	private static final String PACKAGES_FLIGHT_SORTBY_TEMPLATE = "App.Package.Flight.Search.Sort.";
	private static final String PACKAGES_FLIGHT_FILTER_STOPS_TEMPLATE = "App.Package.Flight.Search.Filter.";
	private static final String PACKAGES_FLIGHT_AIRLINES = "App.Package.Flight.Search.Filter.Airline";

	private static final String PACKAGES_SEARCH_ERROR = "App.Package.Hotels-Search.NoResults";
	private static final String PACKAGES_CHECKOUT_ERROR = "App.Package.Checkout.Error";
	private static final String PACKAGES_CHECKOUT_ERROR_RETRY = "App.Package.CKO.Error.Retry";

	private static final String PACKAGES_CHECKOUT_SELECT_TRAVELER = "App.Package.Checkout.Traveler.Select";
	private static final String PACKAGES_CHECKOUT_EDIT_TRAVELER = "App.Package.Checkout.Traveler.Edit.Info";
	private static final String PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE = "App.Package.Checkout.SlideToPurchase";
	private static final String PACKAGES_CHECKOUT_PAYMENT_CID = "App.Package.Checkout.Payment.CID";
	private static final String PACKAGES_CHECKOUT_PRICE_CHANGE = "App.Package.CKO.PriceChange";


	private static void addPackagesCommonFields(ADMS_Measurement s) {
		s.setProp(2, PACKAGES_LOB);
		s.setEvar(2, "D=c2");
		s.setProp(3, "pkg:" + Db.getPackageParams().getOrigin().hierarchyInfo.airport.airportCode);
		s.setEvar(3, "D=c3");
		s.setProp(4, "pkg:" + Db.getPackageParams().getDestination().hierarchyInfo.airport.airportCode + ":" + Db.getPackageParams().getDestination().gaiaId);
		s.setEvar(4, "D=c4");
		setDateValues(s, Db.getPackageParams().getCheckIn(), Db.getPackageParams().getCheckOut());
	}

	/**
	 * https://confluence/display/Omniture/Mobile+App%3A+Flight+and+Hotel+Package#MobileApp:FlightandHotelPackage-PackageCheckout:CheckoutStart
	 *
	 * @param packageDetails
	 */
	public static void trackPackagesCheckoutStart(PackageCreateTripResponse.PackageDetails packageDetails, String hotelSupplierType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_INFO + "\"");

		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_CHECKOUT_INFO);
		s.setEvents("event36, event72");
		addPackagesCommonFields(s);
		setPackageProducts(s, packageDetails.pricing.packageTotal.amount.doubleValue(), true, hotelSupplierType);

		s.track();
	}

	private static void setPackageProducts(ADMS_Measurement s, double productPrice) {
		setPackageProducts(s, productPrice, false, false, null);
	}

	private static void setPackageProducts(ADMS_Measurement s, double productPrice, boolean addEvar63, String hotelSupplierType) {
		setPackageProducts(s, productPrice, addEvar63, false, hotelSupplierType);
	}

	private static void setPackageProducts(ADMS_Measurement s, double productPrice, boolean addEvarInventory, boolean isConfirmation, String hotelSupplierType) {
		StringBuilder productString = new StringBuilder();
		/*
			Trip type:
			RT = Round Trip package
			MD = Multi-destination package
			Currently we don't support MD and just flights+hotels, hardcode this parameter.
		 */
		productString.append(";RT:FLT+HOT;");

		int numTravelers = Db.getPackageParams().getAdults() + Db.getPackageParams().getNumberOfSeatedChildren();
		productString.append(numTravelers + ";" + productPrice + ";;");

		String eVarNumber = isConfirmation ? "eVar30" : "eVar63";
		String flightInventoryType = PackageFlightUtils.isFlightMerchant(Db.getPackageSelectedOutboundFlight()) ? "Merchant" : "Agency";

		if (addEvarInventory) {
			String packageSupplierType = hotelSupplierType.toLowerCase(Locale.ENGLISH).equals(flightInventoryType.toLowerCase(Locale.ENGLISH)) ? flightInventoryType : "Mixed";
			productString.append(eVarNumber + "=" + packageSupplierType + ":PKG");
		}

		String eVar30DurationString = null;
		if (isConfirmation) {
			eVar30DurationString = ":" + Db.getPackageParams().getCheckIn().toString(EVAR30_DATE_FORMAT) + "-" + Db.getPackageParams().getCheckOut().toString(EVAR30_DATE_FORMAT);
			productString.append(eVar30DurationString);
		}

		productString.append(",;");

		productString.append("Flight:" + Db.getPackageSelectedOutboundFlight().carrierCode + ":RT;");
		// We do not expose breakdown prices, so we should hardcode to 0.00
		productString.append(numTravelers + ";0.00;;");

		if (addEvarInventory) {
			productString.append(eVarNumber + "=" + flightInventoryType + ":PKG");
		}

		if (isConfirmation) {
			productString.append(":FLT:" + Db.getPackageParams().getOrigin().hierarchyInfo.airport.airportCode + "-" + Db.getPackageParams().getDestination().hierarchyInfo.airport.airportCode);
			productString.append(eVar30DurationString);
		}

		productString.append(",;");

		productString.append("Hotel:" + Db.getPackageSelectedHotel().hotelId + ";");
		String duration = "0";
		if (Db.getPackageParams().getCheckOut() != null) {
			duration = Integer.toString(JodaUtils.daysBetween(Db.getPackageParams().getCheckIn(), Db.getPackageParams().getCheckOut()));
		}
		productString.append(duration);
		productString.append(";0.00;;");

		if (addEvarInventory) {
			// https://confluence/display/Omniture/Products+String+and+Events#ProductsStringandEvents-Hotels
			productString.append(eVarNumber + "=" + hotelSupplierType + ":PKG");
		}

		if (isConfirmation) {
			productString.append(":HOT:");
			productString.append(eVar30DurationString);
		}

		s.setProducts(productString.toString());
	}

	private static ADMS_Measurement createTrackPackagePageLoadEventBase(String pageName) {
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.setEvar(2, "D=c2");
		s.setProp(2, PACKAGES_LOB);
		return s;
	}

	private static void trackPackagePageLoadEventStandard(String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPackagePageLoadEventBase(pageName).track();
	}

	public static void trackPackagesDestinationSearchInit() {
		trackPackagePageLoadEventStandard(PACKAGES_DESTINATION_SEARCH);
	}

	public static void trackPackagesHSRMapInit() {
		trackPackagePageLoadEventStandard(PACKAGES_HOTEL_SEARCH_MAP_LOAD);
	}

	public static void trackPackagesHSRLoad(PackageSearchResponse response) {
		ADMS_Measurement s = getFreshTrackingObject();

		if (PackageSearchResponse.getHotelResultsCount(response) > 0) {
			Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_SEARCH_RESULT_LOAD + "\"");
			s.setAppState(PACKAGES_HOTEL_SEARCH_RESULT_LOAD);
			addPackagesCommonFields(s);
			s.setEvents("event12,event53");
			s.setProp(1, String.valueOf(PackageSearchResponse.getHotelResultsCount(response)));

			if (PackageSearchResponse.hasSponsoredHotelListing(response)) {
				s.setEvar(28, PACKAGES_HOTEL_SEARCH_SPONSORED_PRESENT);
				s.setProp(16, PACKAGES_HOTEL_SEARCH_SPONSORED_PRESENT);
			}
			else {
				s.setEvar(28, PACKAGES_HOTEL_SEARCH_SPONSORED_NOT_PRESENT);
				s.setProp(16, PACKAGES_HOTEL_SEARCH_SPONSORED_NOT_PRESENT);
			}

			/*
				1R = num of rooms booked, since we don't support multi-room booking on the app yet hard coding it.
				RT = Round Trip package
		 	*/
			StringBuilder evar47String = new StringBuilder("PKG|1R|RT|");
			evar47String.append("A" + Db.getPackageParams().getAdults() + "|");
			evar47String.append("C" + Db.getPackageParams().getChildren().size() + "|");
			evar47String.append("L" + (Db.getPackageParams().getChildren().size() - Db.getPackageParams().getNumberOfSeatedChildren()));
			s.setEvar(47, evar47String.toString());

			// Freeform location
			if (!TextUtils.isEmpty(Db.getPackageParams().getDestination().regionNames.fullName)) {
				s.setEvar(48, Db.getPackageParams().getDestination().regionNames.fullName);
			}
		}
		else {
			Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_SEARCH_ZERO_RESULT_LOAD + "\"");
			s.setAppState(PACKAGES_HOTEL_SEARCH_ZERO_RESULT_LOAD);
			s.setEvar(2, PACKAGES_LOB);
			s.setProp(2, "D=c2");
			s.setProp(36, response.getFirstError().toString());
		}

		s.track();
	}

	public static void createAndtrackLinkEvent(String link, String linkName) {
		Log.d(TAG, "Tracking \"" + link + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(link);
		s.trackLink(null, "o", linkName, null, null);
	}

	public static void trackPackagesHotelMapLinkEvent(String link) {
		createAndtrackLinkEvent(link, "Search Results Map View");
	}

	public static void trackPackagesHotelMapToList() {
		trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_TO_LIST_VIEW);
	}

	public static void trackPackagesHotelMapPinTap() {
		trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_PIN_TAP);
	}

	public static void trackPackagesHotelMapCarouselPropertyClick() {
		trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_CAROUSEL_TAP);
	}

	public static void trackPackagesHotelMapCarouselScroll() {
		trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_CAROUSEL_SCROLL);
	}

	public static void trackPackagesHotelMapSearchThisAreaClick() {
		trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_SEARCH_AREA);
	}

	public static void trackPackagesPaymentSelect() {
		trackPackagePageLoadEventStandard(PACKAGES_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPackagesPaymentEdit() {
		trackPackagePageLoadEventStandard(PACKAGES_CHECKOUT_PAYMENT_EDIT);
	}

	public static void trackPackagesPaymentStoredCCSelect() {
		createAndtrackLinkEvent(PACKAGES_CHECKOUT_PAYMENT_SELECT_STORED_CC, "Package Checkout");
	}

	public static void trackPackagesConfirmation(PackageCheckoutResponse response, String hotelSupplierType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION + "\" pageLoad");
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION);
		setPackageProducts(s, response.getTotalChargesPrice().amount.doubleValue(), true, true, hotelSupplierType);
		s.setCurrencyCode(response.getTotalChargesPrice().currencyCode);
		s.setEvents("purchase");
		s.setPurchaseID("onum" + response.getOrderId());
		s.setProp(71, response.getNewTrip().getTravelRecordLocator());
		s.setProp(72, response.getOrderId());
		s.track();
	}

	public static void trackPackagesPageLoadWithDPageName(String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(pageName);
		s.setEvar(18, "D=pageName");
		s.track();
	}

	public static void trackPackagesFlightRoundTripOutLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_OUT_RESULTS);
	}

	public static void trackPackagesFlightRoundTripOutDetailsLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_OUT_DETAILS);
	}

	public static void trackPackagesFlightRoundTripInLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_IN_RESULTS);
	}

	public static void trackPackagesFlightRoundTripInDetailsLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_IN_DETAILS);
	}

	public static void trackPackagesHotelInfoLoad(String hotelId) {
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(PACKAGES_HOTEL_DETAILS_LOAD);
		s.setEvents("event3");
		String product = ";Hotel:" + hotelId + ";;";
		s.setProducts(product);
		s.track();
	}

	public static void trackPackagesHotelInfoActionBookPhone() {
		Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_DETAILS_BOOK_BY_PHONE + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(PACKAGES_HOTEL_DETAILS_BOOK_BY_PHONE);
		s.setEvents("event34");
		s.trackLink(null, "o", "Package Infosite", null, null);
	}

	public static void trackPackagesHotelInfoActionSelectRoom(boolean stickyButton) {
		StringBuilder link = new StringBuilder(PACKAGES_HOTEL_DETAILS_SELECT_ROOM_TEMPLATE);
		if (stickyButton) {
			link.append("Sticky");
		}
		else {
			link.append("Top");
		}
		createAndtrackLinkEvent(link.toString(), "Package Infosite");
	}

	public static void trackPackagesHotelReviewPageLoad() {
		trackPackagePageLoadEventStandard(PACKAGES_HOTEL_DETAILS_REVIEWS);
	}

	public static void trackPackagesHotelReviewCategoryChange(String category) {
		String link = PACKAGES_HOTEL_DETAILS_REVIEWS_CATEGORY_TEMPLATE + category;
		createAndtrackLinkEvent(link, "Package Reviews");
	}

	public static void trackPackagesHotelResortFeeInfo() {
		trackPackagePageLoadEventStandard(PACKAGES_HOTEL_DETAILS_RESORT_FEE_INFO);
	}

	public static void trackPackagesHotelRenovationInfo() {
		trackPackagePageLoadEventStandard(PACKAGES_HOTEL_DETAILS_RENOVATION_INFO);
	}

	public static void trackPackagesBundlePageLoad(PackageCreateTripResponse.PackageDetails packageDetails) {
		Log.d(TAG, "Tracking \"" + PACKAGES_BUNDLE_OVERVIEW_LOAD + "\"");

		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_BUNDLE_OVERVIEW_LOAD);
		addPackagesCommonFields(s);
		setPackageProducts(s, packageDetails.pricing.packageTotal.amount.doubleValue());
		s.setEvents("event4");
		s.track();
	}

	public static void trackPackagesBundleProductExpandClick(String lobClicked) {
		createAndtrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_PRODUCT_EXPAND_TEMPLATE + lobClicked, "Rate Details");
	}

	public static void trackPackagesBundleCostBreakdownClick() {
		createAndtrackLinkEvent(PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN, "Rate Details");
	}

	public static void trackPackagesSearchTravelerPickerChooser(String text) {
		createAndtrackLinkEvent(PACKAGES_SEARCH_TRAVELER_PICKER_CLICK_TEMPLATE + text, "Search Results Update");
	}

	public static void trackPackagesFlightBaggageFeeClick() {
		createAndtrackLinkEvent(PACKAGES_FLIGHT_BAGGAGE_FEE_CLICK, "Flight Baggage Fee");
	}

	public static void trackPackagesFlightSortFilterLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_FLIGHT_SORT_FILTER_LOAD);
	}

	public static void trackPackagesFlightSortBy(String sortedBy) {
		createAndtrackLinkEvent(PACKAGES_FLIGHT_SORTBY_TEMPLATE + sortedBy, "Search Results Sort");
	}

	public static void trackPackagesFlightFilterStops(String stops) {
		createAndtrackLinkEvent(PACKAGES_FLIGHT_FILTER_STOPS_TEMPLATE + stops, "Search Results Filter");
	}

	public static void trackPackagesFlightFilterAirlines() {
		createAndtrackLinkEvent(PACKAGES_FLIGHT_AIRLINES, "Search Results Filter");
	}

	public static void trackPackagesHotelRoomBookClick() {
		createAndtrackLinkEvent(PACKAGES_HOTEL_DETAILS_BOOK_ROOM, "Room Info");
	}

	public static void trackPackagesHotelViewBookClick() {
		createAndtrackLinkEvent(PACKAGES_HOTEL_DETAILS_VIEW_ROOM, "Room Info");
	}

	public static void trackPackagesHotelRoomInfoClick() {
		createAndtrackLinkEvent(PACKAGES_HOTEL_DETAILS_ROOM_INFO, "Room Info");
	}

	public static void trackPackagesHotelMapViewClick() {
		trackPackagePageLoadEventStandard(PACKAGES_HOTEL_DETAILS_MAP);
	}

	public static void trackPackagesHotelMapSelectRoomClick() {
		createAndtrackLinkEvent(PACKAGES_HOTEL_DETAILS_MAP_SELECT_ROOM, "Infosite Map");
	}

	public static void trackPackagesSearchError(String errorType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_SEARCH_ERROR + "\" pageLoad...");
		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_SEARCH_ERROR);
		s.setProp(36, errorType);
		s.track();
	}

	public static void trackPackagesCheckoutError(String errorType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_ERROR + "\" pageLoad...");
		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_CHECKOUT_ERROR);
		s.setEvents("event38");
		s.setProp(36, errorType);
		s.track();
	}

	public static void trackPackagesCheckoutErrorRetry() {
		createAndtrackLinkEvent(PACKAGES_CHECKOUT_ERROR_RETRY, "Package Checkout");
	}

	public static void trackPackagesCheckoutSelectTraveler() {
		createTrackPageLoadEventBase(PACKAGES_CHECKOUT_SELECT_TRAVELER).track();
	}

	public static void trackPackagesCheckoutEditTraveler() {
		createTrackPageLoadEventBase(PACKAGES_CHECKOUT_EDIT_TRAVELER).track();
	}

	public static void trackPackagesCheckoutSlideToPurchase() {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE + "\" load...");
		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, getPaymentType());
		s.track();
	}

	public static void trackPackagesCheckoutPaymentCID() {
		createTrackPageLoadEventBase(PACKAGES_CHECKOUT_PAYMENT_CID).track();
	}

	public static void trackPackagesCheckoutPriceChange(int priceDiff) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_PRICE_CHANGE + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(PACKAGES_CHECKOUT_PRICE_CHANGE);
		s.setEvents("event62");
		s.setProp(9, "PKG|" + priceDiff);
		s.trackLink(null, "o", "Package Checkout", null, null);
	}

	public static void trackPackagesHotelFilterPageLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTELS_SEARCH_REFINE);
	}

	public static void trackPackagesHotelFilterRating(String rating) {
		String pageName = PACKAGES_HOTELS_SEARCH_REFINE + "." + rating;
		createAndtrackLinkEvent(pageName, "Search Results Sort");
	}

	public static void trackPackagesHotelSortBy(String type) {
		String pageName = PACKAGES_HOTELS_SORT_BY_TEMPLATE + type;
		createAndtrackLinkEvent(pageName, "Search Results Sort");
	}

	public static void trackPackagesHotelFilterPriceSlider() {
		createAndtrackLinkEvent(PACKAGES_HOTELS_FILTER_PRICE, "Search Results Sort");
	}

	public static void trackPackagesHotelFilterVIP(String type) {
		String pageName = PACKAGES_HOTELS_FILTER_VIP_TEMPLATE + type;
		createAndtrackLinkEvent(pageName, "Search Results Sort");
	}

	public static void trackPackagesHotelFilterNeighborhood() {
		createAndtrackLinkEvent(PACKAGES_HOTELS_FILTER_NEIGHBOURHOOD, "Search Results Sort");
	}

	public static void trackPackagesHotelFilterByName() {
		createAndtrackLinkEvent(PACKAGES_HOTELS_FILTER_BY_NAME, "Search Results Sort");
	}

	public static void trackPackagesHotelClearFilter() {
		createAndtrackLinkEvent(PACKAGES_HOTELS_FILTER_CLEAR, "Search Results Sort");
	}

	public static void trackPackagesBundleEditClick() {
		createAndtrackLinkEvent(PACKAGES_BUNDLE_EDIT, "Rate Details");
	}

	public static void trackPackagesBundleEditItemClick(String itemType) {
		createAndtrackLinkEvent(PACKAGES_BUNDLE_EDIT + "." + itemType, "Rate Details");
	}
}
