package com.expedia.bookings.tracking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Pair;

import com.adobe.mobile.Config;
import com.expedia.bookings.ADMS_Measurement;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightItinDetailsResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelItinDetailsResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.abacus.ABTest;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusTest;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.flights.FlightCheckoutResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightItineraryType;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.flights.FlightLeg.FlightSegment;
import com.expedia.bookings.data.flights.FlightServiceClassType;
import com.expedia.bookings.data.flights.KrazyglueResponse;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.insurance.InsuranceProduct;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.packages.PackageCheckoutResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.payment.PaymentSplitsType;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.rail.requests.RailSearchRequest;
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse;
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse;
import com.expedia.bookings.data.rail.responses.RailLeg;
import com.expedia.bookings.data.rail.responses.RailLegOption;
import com.expedia.bookings.data.rail.responses.RailTripOffer;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripUtils;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.enums.OnboardingPagerState;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData;
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData;
import com.expedia.bookings.tracking.hotel.PageUsableData;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.DebugInfoUtils;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.FlightV2Utils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.NumberUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.util.PackageUtil;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

import kotlin.NotImplementedError;

import static com.expedia.bookings.utils.FeatureUtilKt.isMidAPIEnabled;
import static com.expedia.bookings.utils.FeatureUtilKt.isPackagesMISRealWorldGeoEnabled;

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
	private static UserStateManager userStateManager;

	public static void init(ExpediaBookingApp app) {
		Log.d(TAG, "init");
		Config.setContext(app.getApplicationContext());

		//TODO clean up the following code
		sContext = app.getApplicationContext();
		userStateManager = app.appComponent().userStateManager();
		ADMS_Measurement.sharedInstance(sContext);
		app.registerActivityLifecycleCallbacks(sOmnitureActivityCallbacks);
		sMarketingDate = SettingUtils
			.get(sContext, sContext.getString(R.string.preference_marketing_date), sMarketingDate);
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
			ADMS_Measurement.sharedInstance().resumeActivity(activity);
		}

		@Override
		public void onActivityPaused(Activity activity) {
			Log.v(TAG, "onPause - " + activity.getClass().getSimpleName());
			ADMS_Measurement.sharedInstance().pauseActivity();
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
	private static final String HOTELSV2_GEO_SUGGESTION_CLICK = "App.Hotels.DS.DestSuggest";
	private static final String HOTELSV2_TRAVELER = "App.Hotels.Traveler.";
	private static final String HOTELSV2_RESULT = "App.Hotels.Search";
	private static final String HOTELSV2_NO_RESULT = "App.Hotels.Search.NoResults";
	private static final String HOTELSV2_NO_PINNED_RESULT = "App.Hotels.Search.SelectedHotelNotFound";
	private static final String HOTELSV2_SORT = "App.Hotels.Search.Sort.";
	private static final String HOTELSV2_SORT_PRICE_SLIDER = "App.Hotels.Search.Price";
	private static final String HOTELSV2_SEARCH_FILTER_VIP = "App.Hotels.Search.Filter.VIP.";
	private static final String HOTELSV2_SEARCH_FILTER_NEIGHBOURHOOD = "App.Hotels.Search.Neighborhood";
	private static final String HOTELSV2_SEARCH_FILTER_BY_NAME = "App.Hotels.Search.HotelName";
	private static final String HOTELSV2_CLEAR_FILTER = "App.Hotels.Search.ClearFilter";
	private static final String HOTELSV2_SUPER_SEARCH_FILTER = "App.Hotels.Dest-Search.Filter";
	private static final String HOTELSV2_SUPER_SEARCH_SORT_BY = "App.Hotels.Dest-Search.Sort.";
	private static final String HOTELSV2_SUPER_SEARCH_STAR_RATING = "App.Hotels.Dest-Search.Filter.";
	private static final String HOTELSV2_SUPER_SEARCH_FILTER_VIP = "App.Hotels.Dest-Search.Filter.VIP.";
	private static final String HOTELSV2_SUPER_SEARCH_CLEAR_FILTER = "App.Hotels.Dest-Search.ClearFilter";
	private static final String HOTELSV2_SEARCH_MAP = "App.Hotels.Search.Map";
	private static final String HOTELSV2_SEARCH_MAP_TO_LIST = "App.Hotels.Search.Expand.List";
	private static final String HOTELSV2_SEARCH_MAP_TAP_PIN = "App.Hotels.Search.TapPin";
	private static final String HOTELSV2_SEARCH_THIS_AREA = "App.Hotels.Search.AreaSearch";
	private static final String HOTELSV2_DETAILS_PAGE = "App.Hotels.Infosite";
	private static final String HOTELSV2_SOLD_OUT_PAGE = "App.Hotels.Infosite.SoldOut";
	private static final String HOTELSV2_DETAILS_ERROR = "App.Hotels.Infosite.Error";
	private static final String HOTEL_URGENCY_COMPRESSION_SCORE = "HOT.SR.RegionCompression.Score.";

	private static final String HOTELSV2_DETAILS_ETP = "App.Hotels.IS.Select.";
	private static final String HOTELSV2_DETAIL_VIEW_ROOM = "App.Hotels.IS.ViewRoom";
	private static final String HOTELSV2_DETAIL_ROOM_INFO = "App.Hotels.IS.MoreRoomInfo";
	private static final String HOTELSV2_DETAIL_ROOM_BOOK = "App.Hotels.IS.BookNow";
	private static final String HOTELSV2_DETAIL_MAP_VIEW = "App.Hotels.Infosite.Map";
	private static final String HOTELSV2_DETAIL_BOOK_PHONE = "App.Hotels.Infosite.BookPhone";
	private static final String HOTELSV2_DETAIL_SELECT_ROOM = "App.Hotels.Infosite.SelectRoom";
	private static final String HOTELSV2_MAP_SELECT_ROOM = "App.Hotels.IS.Map.SelectRoom";
	private static final String HOTELSV2_DETAIL_GALLERY_CLICK = "App.Hotels.IS.Gallery.Hotel";
	private static final String HOTELSV2_DETAIL_ROOM_GALLERY_CLICK = "App.Hotels.IS.Gallery.Room";
	private static final String HOTELSV2_DETAIL_CHANGE_DATE = "App.Hotels.IS.ChangeDates";

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
	private static final String ITIN_RATE_APP = "App.RateApp";

	private static final String APP_CKO_PAYMENT_SAVE = "App.CKO.Payment.Save";
	private static final String APP_CKO_PAYMENT_DECLINE_SAVE = "App.CKO.Payment.DeclineSave";
	private static final String APP_CKO_TRAVELER_SAVE = "App.CKO.Traveler.Save";
	private static final String APP_CKO_TRAVELER_DECLINE_SAVE = "App.CKO.Traveler.DeclineSave";
	private static final String APP_CKO_SLIDE_TO_BOOK = "App.CKO.SlideToBook";

	private static final String UNIVERSAL_CHECKOUT = "Universal Checkout";

	public static void trackSlideToBookAction() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, APP_CKO_SLIDE_TO_BOOK);
		s.setProp(16, APP_CKO_SLIDE_TO_BOOK);
		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", UNIVERSAL_CHECKOUT, null, null);
	}

	public static void trackHolidayPromotionClick() {
		ADMS_Measurement s = createTrackLinkEvent("App.LS.HolidayPromo");
		s.setEvents("event331");
		s.trackLink(null, "o", "Holiday Promotion", null, null);
	}

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

		//SWP is visible and toggle is ON, when user lands on Search Screen
		if (swpIsVisibleAndToggleIsOn) {
			s.setEvents("event118");
		}

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelGreedySearch);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelSuperSearch);
		trackAbacusTest(s, AbacusUtils.HotelAutoSuggestSameAsWeb);
		// Send the tracking data
		s.track();
	}

	public static void trackHotelSuggestionBehavior(SuggestionTrackingData trackingData) {
		// https://confluence/pages/viewpage.action?pageId=679373062

		StringBuilder linkSb = new StringBuilder("HTL.UpdateSearch.H");
		linkSb.append(".").append(PointOfSale.getPointOfSale().getLocaleIdentifier())
			.append(".").append(PointOfSale.getPointOfSale().getSiteId())
			.append(".").append(getTypeAheadTriggerEventString(trackingData));

		linkSb.append(".P");
		if (trackingData.isParent() && !trackingData.isHistory()) {
			linkSb.append(1);
		}
		else {
			linkSb.append(0);
		}
		linkSb.append("C");
		if (trackingData.isChild() && !trackingData.isHistory()) {
			linkSb.append(1);
		}
		else {
			linkSb.append(0);
		}
		linkSb.append("L").append(trackingData.getSelectedSuggestionPosition())
			.append(".ESS#")
			.append(trackingData.getSuggestionsShownCount() - trackingData.getPreviousSuggestionsShownCount())
			.append(".UH#").append(trackingData.getPreviousSuggestionsShownCount());
		String link = linkSb.toString();

		ADMS_Measurement s = createTrackLinkEvent(link);

		StringBuilder eventBuilder = new StringBuilder("event45");
		eventBuilder.append(",event44=");
		eventBuilder.append(trackingData.getSelectedSuggestionPosition());
		eventBuilder.append(",event46=");
		eventBuilder.append(trackingData.getCharactersTypedCount());
		s.setEvents(eventBuilder.toString());

		StringBuilder infoBuilder = new StringBuilder();
		infoBuilder.append("GAI:").append(trackingData.getSuggestionGaiaId());
		infoBuilder.append("|").append(trackingData.getSuggestionType());
		infoBuilder.append("|").append("R#");
		if (trackingData.isChild()) {
			infoBuilder.append("child");
		}
		else {
			infoBuilder.append("-");
		}
		infoBuilder.append("|").append(trackingData.getDisplayName());
		if (trackingData.isHistory()) {
			infoBuilder.append("|").append("USERHISTORY");
		}
		s.setEvar(48, infoBuilder.toString());
		s.setProp(73, infoBuilder.toString());

		s.trackLink(null, "o", "Hotel Search", null, null);
	}

	private static String getTypeAheadTriggerEventString(SuggestionTrackingData trackingData) {
		StringBuilder sb = new StringBuilder();
		if (trackingData.getSuggestionsFocused()) {
			sb.append("TAShow.");
		}
		else {
			sb.append("TANoShow.");
		}
		if (trackingData.getSuggestionSelected()) {
			if (trackingData.getCharactersTypedCount() > 0) {
				sb.append("TASelection");
			}
			else {
				sb.append("TAFocus");
			}
		}
		else if (trackingData.getSuggestionGaiaId() != null) {
			sb.append("TAPrevSearch");
		}
		else {
			sb.append("TaNoSelect");
		}
		return sb.toString();
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

	public static void trackUserChoosesNotToSaveCard() {
		ADMS_Measurement s = createTrackLinkEvent(APP_CKO_PAYMENT_DECLINE_SAVE);
		s.trackLink(null, "o", UNIVERSAL_CHECKOUT, null, null);
	}

	public static void trackUserChoosesToSaveCard() {
		ADMS_Measurement s = createTrackLinkEvent(APP_CKO_PAYMENT_SAVE);
		s.trackLink(null, "o", UNIVERSAL_CHECKOUT, null, null);
	}

	public static void trackUserChoosesToSaveTraveler() {
		ADMS_Measurement s = createTrackLinkEvent(APP_CKO_TRAVELER_SAVE);
		s.trackLink(null, "o", UNIVERSAL_CHECKOUT, null, null);
	}

	public static void trackUserChoosesNotToSaveTraveler() {
		ADMS_Measurement s = createTrackLinkEvent(APP_CKO_TRAVELER_DECLINE_SAVE);
		s.trackLink(null, "o", UNIVERSAL_CHECKOUT, null, null);
	}

	public static void trackHotelsV2Search(HotelSearchTrackingData searchTrackingData) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"" + HOTELSV2_RESULT + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_RESULT);
		s.setEvar(18, HOTELSV2_RESULT);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Region
		s.setProp(4, searchTrackingData.getRegion());
		s.setEvar(4, "D=c4");

		String checkInString = searchTrackingData.getCheckInDate().toString(PROP_DATE_FORMAT);
		String checkoutString;
		if (searchTrackingData.getCheckoutDate() != null) {
			checkoutString = searchTrackingData.getCheckoutDate().toString(PROP_DATE_FORMAT);
		}
		else {
			checkoutString = "nil";
		}

		s.setProp(5, checkInString);
		s.setEvar(5, searchTrackingData.getSearchWindowDays());

		s.setProp(6, checkoutString);
		s.setEvar(6, searchTrackingData.getDuration().toString());

		StringBuilder searchDataPipeList = new StringBuilder("HOT|A");
		searchDataPipeList.append(searchTrackingData.getNumberOfAdults());
		searchDataPipeList.append("|C");
		searchDataPipeList.append(searchTrackingData.getNumberOfChildren());
		s.setEvar(47, searchDataPipeList.toString());

		if (searchTrackingData.getFreeFormRegion() != null) {
			s.setEvar(48, searchTrackingData.getFreeFormRegion());
		}

		if (searchTrackingData.hasResponse()) {
			s.setProp(1, searchTrackingData.getNumberOfResults());

			String sponsoredListingPresent = "App.Hotels.Search.Sponsored.No";
			if (searchTrackingData.getHasSponsoredListingPresent()) {
				sponsoredListingPresent = "App.Hotels.Search.Sponsored.Yes";
			}
			s.setEvar(28, sponsoredListingPresent);
			s.setProp(16, sponsoredListingPresent);
		}

		String events = "event12,event51";
		if (searchTrackingData.getSwpEnabled()) {
			events += ",event118";
		}

		String products = getSearchResultsHotelProductStrings(searchTrackingData.getHotels());
		s.setProducts(products);

		if (searchTrackingData.getHasPinnedHotel()) {
			if (searchTrackingData.getPinnedHotelSoldOut()) {
				events += ",event283";
			}
			else {
				events += ",event282";
			}
		}

		if (searchTrackingData.getHasSoldOutHotel()) {
			events += ",event14";
		}

		setEventsForSearchTracking(s, searchTrackingData.getPerformanceData(), events);
		trackAbacusTest(s, AbacusUtils.ExpediaAndroidAppAATestSep2015);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelUrgencyMessage);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence);
		trackAbacusTest(s, AbacusUtils.HotelShowSoldOutResults);
		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2NoResult(String errorMessage) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_NO_RESULT + "\" pageLoad...");

		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELSV2_NO_RESULT);

		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		s.setProp(36, errorMessage);

		s.track();
	}

	public static void trackHotelV2NoPinnedResult(String errorReason) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_NO_PINNED_RESULT + "\" pageLoad...");

		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELSV2_NO_PINNED_RESULT);

		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		s.setProp(36, errorReason);

		s.track();
	}

	public static void trackHotelV2SponsoredListingClick() {
		Log.d(TAG, "Tracking \"" + HOTELS_SPONSORED_LISTING_CLICK + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_SPONSORED_LISTING_CLICK);
		s.trackLink(null, "o", "Sponsored Click", null, null);
	}

	public static void trackHotelV2SuperSearchFilter() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SUPER_SEARCH_FILTER + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_SUPER_SEARCH_FILTER);
		s.setEvar(18, HOTELSV2_SUPER_SEARCH_FILTER);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2SuperSearchSortBy(String type) {
		String pageName = HOTELSV2_SUPER_SEARCH_SORT_BY + type;
		createAndtrackLinkEvent(pageName, "Super Search Sort By");
	}

	public static void trackLinkHotelV2SuperSearchStarRating(String rating) {
		String pageName = HOTELSV2_SUPER_SEARCH_STAR_RATING + rating;
		createAndtrackLinkEvent(pageName, "Super Search Star Rating");
	}

	public static void trackLinkHotelV2SuperSearchVip(String state) {
		String pageName = HOTELSV2_SUPER_SEARCH_FILTER_VIP + state;
		createAndtrackLinkEvent(pageName, "Super Search Vip");
	}

	public static void trackLinkHotelV2SuperSearchClearFilter() {
		createAndtrackLinkEvent(HOTELSV2_SUPER_SEARCH_CLEAR_FILTER, "Super Search Clear Filter");
	}

	public static void trackHotelNarrowSearchPrompt() {
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_FILTER_PROMPT_TRIGGER);
		s.trackLink(null, "o", "Filter Prompt Triggered", null, null);
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
		createAndtrackLinkEvent(pageName, "Search Results Sort");
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

	public static void trackHotelV2SearchMap(boolean swpEnabled) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP + "\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(HOTELSV2_SEARCH_MAP);
		s.setEvar(18, HOTELSV2_SEARCH_MAP);

		// LOB Search
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		if (swpEnabled) {
			s.setEvents("event118");
		}

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2MapToList(boolean swpEnabled) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP_TO_LIST + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_MAP_TO_LIST);
		if (swpEnabled) {
			s.setEvents("event118");
		}
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackHotelV2MapTapPin() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_MAP_TAP_PIN + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_MAP_TAP_PIN);
		s.trackLink(null, "o", "Search Results Map View", null, null);
	}

	public static void trackHotelV2AreaSearchClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_SEARCH_THIS_AREA + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_SEARCH_THIS_AREA);
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
		boolean isCurrentLocationSearch, boolean isHotelSoldOut, boolean isRoomSoldOut,
		PageUsableData pageLoadTimeData, boolean swpEnabled) {

		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAILS_PAGE + "\" pageload");

		ADMS_Measurement s = getFreshTrackingObject();

		final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

		LocalDate checkInDate = dtf.parseLocalDateTime(hotelOffersResponse.checkInDate).toLocalDate();
		LocalDate checkOutDate = dtf.parseLocalDateTime(hotelOffersResponse.checkOutDate).toLocalDate();

		s.setAppState(HOTELSV2_DETAILS_PAGE);
		s.setEvar(18, HOTELSV2_DETAILS_PAGE);

		String drrString = internalGenerateHotelV2DRRString(hotelOffersResponse);
		s.setEvar(9, drrString);

		String events = "event3";
		if (isHotelSoldOut) {
			events += ",event14";
		}
		else {
			if (isETPEligible) {
				events += ",event5";
			}
			if (isRoomSoldOut) {
				events += ",event18";
			}
		}

		if (swpEnabled) {
			events += ",event118";
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

				events += ",event57";
			}
		}

		s.setEvents(events);

		addPageLoadTimeTrackingEvents(s, pageLoadTimeData);

		trackAbacusTest(s, AbacusUtils.HotelEnableInfositeChangeDate);
		trackAbacusTest(s, AbacusUtils.HotelRoomImageGallery);

		// Send the tracking data
		s.track();
	}

	public static void trackHotelV2InfositeError(String errorMessage) {
		String pageName = HOTELSV2_DETAILS_ERROR;
		Log.d(TAG, "Tracking \"" + pageName + "\" pageload");

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);

		s.setProp(36, errorMessage);

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

	public static void trackLinkHotelV2AirAttachEligible(HotelOffersResponse.HotelRoomResponse hotelRoomResponse,
		String hotelId) {
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

	public static void trackHotelV2RoomBookClick(HotelOffersResponse.HotelRoomResponse hotelRoomResponse,
		boolean hasETP) {
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

		if (PointOfSale.getPointOfSale().isHotelsWebCheckoutABTestEnabled()) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelsWebCheckout);
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

	public static void trackHotelDetailGalleryClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_GALLERY_CLICK + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_GALLERY_CLICK);

		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		s.trackLink(null, "o", "Gallery View", null, null);
	}

	public static void trackHotelDetailRoomGalleryClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_ROOM_GALLERY_CLICK + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_ROOM_GALLERY_CLICK);

		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		s.trackLink(null, "o", "Gallery View", null, null);
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

	public static void trackHotelV2InfositeChangeDateClick() {
		Log.d(TAG, "Tracking \"" + HOTELSV2_DETAIL_CHANGE_DATE + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(HOTELSV2_DETAIL_CHANGE_DATE);

		s.trackLink(null, "o", "Infosite Change Dates", null, null);
	}

	public static void trackPageLoadHotelV2CheckoutInfo(
		HotelCreateTripResponse trip,
		com.expedia.bookings.data.hotels.HotelSearchParams searchParams,
		PageUsableData pageUsableData) {

		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELS_CHECKOUT_INFO);

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_enable_disabled_stp_hotels)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppDisabledSTPStateHotels);
		}

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_enable_hotel_material_forms)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelMaterialForms);
		}
		trackAbacusTest(s, AbacusUtils.EBAndroidAppSavedCoupons);

		StringBuilder events = new StringBuilder("event70");
		if (trip.isRewardsRedeemable()) {
			events.append(",");
			events.append(ProductFlavorFeatureConfiguration.getInstance()
				.getOmnitureEventValue(OmnitureEventName.HOTEL_CHECKOUT_START_REWARDS_REDEEMABLE));
			BigDecimal amountPaidWithPoints = trip.getPointDetails().getMaxPayableWithPoints().getAmount().amount;
			BigDecimal totalAmount = trip.getTripTotalExcludingFee().amount;
			int percentagePaidWithPoints = NumberUtils.getPercentagePaidWithPointsForOmniture(amountPaidWithPoints,
				totalAmount);
			String rewardAppliedPercentage = ProductFlavorFeatureConfiguration.getInstance()
				.getOmnitureEventValue(OmnitureEventName.REWARD_APPLIED_PERCENTAGE_TEMPLATE);
			s.setEvar(53, String.format(Locale.getDefault(), rewardAppliedPercentage, percentagePaidWithPoints));
		}

		s.setEvents(events.toString());

		addPageLoadTimeTrackingEvents(s, pageUsableData);

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

		trackAbacusTest(s, AbacusUtils.EBAndroidAppFreeCancellationTooltip);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelCheckinCheckoutDatesInline);

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.pay_later_credit_card_messaging) && isPayLaterHotel(trip)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelPayLaterCreditCardMessaging);
		}

		s.setProducts(
			"Hotel;" + properCaseSupplierType + " Hotel:" + hotelProductResponse.hotelId + ";" + numOfNights + ";"
				+ price);


		addStandardHotelV2Fields(s, searchParams);
		s.track();
	}

	private static boolean isPayLaterHotel(HotelCreateTripResponse trip) {
		boolean isPayLater = trip.newHotelProductResponse.hotelRoomResponse.isPayLater;
		boolean hasDeposit = trip.newHotelProductResponse.hotelRoomResponse.depositRequired;
		return isPayLater && !hasDeposit;
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
		trackAbacusTest(s, AbacusUtils.EBAndroidPopulateCardholderName);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppAllowUnknownCardTypes);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppDisplayEligibleCardsOnPaymentForm);
		s.track();
	}

	public static void trackHotelV2ShowSlideToPurchase(PaymentType paymentType, PaymentSplitsType paymentSplitsType) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad...");
		trackShowSlidetoPurchase(HOTELSV2_CHECKOUT_SLIDE_TO_PURCHASE,
			getPaymentTypeOmnitureCode(paymentType, paymentSplitsType), null);
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
		ADMS_Measurement s = createTrackCheckoutErrorPageLoadEventBase(CHECKOUT_ERROR_PAGE_NAME,
			HOTELSV2_CHECKOUT_ERROR);
		s.setProp(36, errorType);
		s.track();
	}

	public static void trackHotelV2CheckoutErrorRetry() {
		Log.d(TAG, "Tracking \"" + "App.Hotels.CKO.Error.Retry" + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent("App.Hotels.CKO.Error.Retry");
		s.trackLink(null, "o", "Hotel Checkout", null, null);
	}

	public static void trackHotelV2PurchaseConfirmation(HotelCheckoutResponse hotelCheckoutResponse,
		int percentagePaidWithPoints, String totalAppliedRewardCurrency, PageUsableData pageUsableData) {
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
		s.setProp(8, getHotelConfirmationTripNumberString(hotelCheckoutResponse));
		s.setPurchaseID("onum" + hotelCheckoutResponse.orderId);
		s.setEvar(2, "D=c2");

		int numNights = JodaUtils.daysBetween(checkInDate, checkOutDate);
		String totalCost = hotelCheckoutResponse.totalCharges;
		String supplierType = hotelCheckoutResponse.checkoutResponse.bookingResponse.supplierType;
		if (Strings.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType = Strings.splitAndCapitalizeFirstLetters(supplierType);

		String products = "Hotel;" + properCaseSupplierType + " Hotel:"
			+ hotelCheckoutResponse.checkoutResponse.productResponse.hotelId;

		products += ";" + numNights + ";" + totalCost;
		s.setProducts(products);

		// Currency code
		s.setCurrencyCode(hotelCheckoutResponse.currencyCode);

		s.setEvar(53, getPercentageOfAmountPaidWithPoints(percentagePaidWithPoints));

		addPageLoadTimeTrackingEvents(s, pageUsableData);

		// LX Cross sell
		boolean isLXEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.LX);
		if (isLXEnabled) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest);
		}

		s.track();
	}

	public static void trackHotelV2PurchaseFromWebView(HotelItinDetailsResponse hotelItinDetailsResponse) {
		Log.d(TAG, "Tracking \"" + HOTELSV2_PURCHASE_CONFIRMATION + "\" pageLoad");
		HotelItinDetailsResponse response = hotelItinDetailsResponse;
		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELSV2_PURCHASE_CONFIRMATION);
		// Product details
		DateTimeFormatter dtf = ISODateTimeFormat.basicDate();
		LocalDate checkInDate = new LocalDate(
			hotelItinDetailsResponse.responseData.getHotels().get(0).checkInDateTime.toLocalDate());
		LocalDate checkOutDate = new LocalDate(
			hotelItinDetailsResponse.responseData.getHotels().get(0).checkOutDateTime.toLocalDate());
		String checkIn = dtf.print(checkInDate);
		String checkOut = dtf.print(checkOutDate);
		s.setEvar(30, "Hotel:" + checkIn + "-" + checkOut + ":N");

		s.setProp(72, hotelItinDetailsResponse.responseData.getOrderNumber().toString());
		s.setProp(2, "hotels");

		s.setEvents("purchase,event196");
		s.setPurchaseID("onum" + hotelItinDetailsResponse.responseData.getOrderNumber().toString());

		s.setEvar(2, "D=c2");

		int numNights = JodaUtils.daysBetween(checkInDate, checkOutDate);

		String totalCost = hotelItinDetailsResponse.responseData.getTotalTripPrice().getTotalFormatted();

		String supplierType = hotelItinDetailsResponse.responseData.getHotels().get(0).getInventoryType();
		if (Strings.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType = Strings.splitAndCapitalizeFirstLetters(supplierType);

		s.setProducts(getHotelProductString(hotelItinDetailsResponse.responseData.getHotels().get(0).getHotelId(),
			numNights, totalCost, properCaseSupplierType));

		// Currency code
		s.setCurrencyCode(
			hotelItinDetailsResponse.responseData.getHotels().get(0).getTotalPriceDetails().primaryCurrencyCode);

		// LX Cross sell
		boolean isLXEnabled = PointOfSale.getPointOfSale().supports(LineOfBusiness.LX);
		if (isLXEnabled) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest);
		}

		// Send the tracking data
		s.track();
	}

	public static String getHotelProductString(String hotelId, int numNights,
		String totalCost, String properCaseSupplierType) {
		if (hotelId == null) {
			hotelId = "";
		}
		if (totalCost == null) {
			totalCost = "";
		}
		String products = "Hotel;" + properCaseSupplierType + " Hotel:"
			+ hotelId;

		products += ";" + numNights + ";" + totalCost;
		return products;
	}

	public static String getSearchResultsHotelProductStrings(List<Hotel> hotels) {
		String products = "";
		int i = 1;

		for (Hotel hotel : hotels) {
			if (hotel.lowRateInfo == null) {
				i++;
				continue;
			}
			String hotelId = hotel.hotelId;
			String listPosition = Integer.toString(i);
			String airAttachedCode = hotel.lowRateInfo.airAttached ? "-MIP" : "";
			String sponsoredCode = hotel.isSponsoredListing ? "-AD" : "";
			String memberDealCode = hotel.isMemberDeal ? "-MOD" : "";

			String strikeThroughPriceString = "";
			float strikeThroughPriceFloat = hotel.lowRateInfo.strikethroughPriceToShowUsers;

			if (strikeThroughPriceFloat > 0) {
				BigDecimal strikeThroughBd = new BigDecimal(Float.toString(strikeThroughPriceFloat));
				strikeThroughBd = strikeThroughBd.setScale(0, BigDecimal.ROUND_HALF_UP);

				strikeThroughPriceString = strikeThroughBd.toString() + "-";
			}

			float priceToShowUsersFloat =
				hotel.lowRateInfo.priceToShowUsers < 0 ? 0 : hotel.lowRateInfo.priceToShowUsers;
			BigDecimal priceToShowUsers = new BigDecimal(Float.toString(priceToShowUsersFloat));
			priceToShowUsers = priceToShowUsers.setScale(0, BigDecimal.ROUND_HALF_UP);

			String priceToShowUsersString = priceToShowUsers.toString();

			String product = (i == 1) ? ";Hotel:" : ",;Hotel:";

			product += hotelId + ";;;;eVar39=" + listPosition + airAttachedCode + sponsoredCode + memberDealCode +
				"|eVar30=" + strikeThroughPriceString + priceToShowUsersString;

			products += product;
			i++;
		}

		return products;
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

	private static void addPageLoadTimeTrackingEvents(ADMS_Measurement s, PageUsableData pageLoadTimeData) {
		if (pageLoadTimeData == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		if (s.getEvents() != null) {
			sb.append(s.getEvents());
		}
		pageLoadTimeEvents(s, pageLoadTimeData, sb);
	}

	private static void pageLoadTimeEvents(ADMS_Measurement s, PageUsableData pageLoadTimeData, StringBuilder sb) {
		appendPageLoadTimeEvents(sb, pageLoadTimeData.getLoadTimeInSeconds());
		if (sb.length() > 0) {
			s.setEvents(sb.toString());
		}
	}

	private static void setEventsForSearchTracking(ADMS_Measurement s,
		AbstractSearchTrackingData.PerformanceData performanceData, String eventString) {
		StringBuilder eventStringBuilder = new StringBuilder();
		eventStringBuilder.append(eventString);

		appendPageLoadTimeEvents(eventStringBuilder, performanceData.getPageLoadTime());

		if (eventStringBuilder.length() > 0) {
			s.setEvents(eventStringBuilder.toString());
		}
	}

	private static void appendPageLoadTimeEvents(StringBuilder eventStringBuilder, String pageLoadTimeString) {
		if (!TextUtils.isEmpty(pageLoadTimeString)) {
			if (eventStringBuilder.length() > 0) {
				eventStringBuilder.append(",");
			}
			eventStringBuilder.append("event220,event221=").append(pageLoadTimeString);
		}
	}

	private static void addHotelV2Products(ADMS_Measurement s, HotelOffersResponse.HotelRoomResponse hotelRoomResponse,
		String hotelId) {
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

	private static void addStandardHotelV2Fields(ADMS_Measurement s,
		com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		s.setEvar(2, HOTELV2_LOB);
		s.setProp(2, HOTELV2_LOB);
		s.setEvar(6, Integer.toString(JodaUtils.daysBetween(searchParams.getCheckIn(), searchParams.getCheckOut())));
		internalSetHotelV2DateProps(s, searchParams);
	}

	private static void internalSetHotelV2DateProps(ADMS_Measurement s,
		com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
		LocalDate checkInDate = searchParams.getCheckIn();
		LocalDate checkOutDate = searchParams.getCheckOut();
		setDateValues(s, checkInDate, checkOutDate);
	}


	private static void addHotelV2RegionId(ADMS_Measurement s,
		com.expedia.bookings.data.hotels.HotelSearchParams searchParams) {
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

	private static final String HOTELS_CHECKOUT_INFO = "App.Hotels.Checkout.Info";
	private static final String HOTELS_CHECKOUT_WARSAW = "App.Hotels.Checkout.Warsaw";
	private static final String HOTELS_CHECKOUT_PAYMENT_CID = "App.Hotels.Checkout.Payment.CID";
	private static final String HOTELS_SEARCH_REFINE = "App.Hotels.Search.Filter";

	private static final String HOTELS_SPONSORED_LISTING_CLICK = "App.Hotels.Search.Sponsored.Click";
	private static final String HOTELS_FILTER_PROMPT_TRIGGER = "App.Hotels.Search.FilterPrompt.Trigger";

	private static final String HOTELS_REVIEWS_ERROR = "App.Hotels.Reviews.Error";

	public static final String HOTELS = "App.Hotels";

	public static void trackReviewLoadingError(String errorMsg) {
		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELS_REVIEWS_ERROR);
		s.setEvar(2, "D=c2");
		s.setProp(2, HOTELV2_LOB);
		s.setProp(36, errorMsg);
		s.track();
	}

	public static void trackPageLoadHotelsCheckoutWarsaw() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_WARSAW);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights Tracking
	//
	// Spec: http://confluence/display/Omniture/Mobile+App+Flight+Tracking
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String FLIGHT_SEATING_CLASS_SELECT = "App.Flight.DS.SeatingClass.";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT = "App.Flight.Search.Roundtrip.Out";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_HOTELBANNER_SELECT = "App.Flight.Search.Roundtrip.Out.HotelBanner.Select";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS = "App.Flight.Search.Roundtrip.Out.Details";
	private static final String FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.Out.BaggageFee";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN = "App.Flight.Search.Roundtrip.In";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS = "App.Flight.Search.Roundtrip.In.Details";
	private static final String FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.In.BaggageFee";
	private static final String FLIGHT_CHECKOUT_INFO = "App.Flight.Checkout.Info";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO = "App.Flight.Checkout.Traveler.Edit.Info";
	private static final String FLIGHT_CHECKOUT_WARSAW = "App.Flight.Checkout.Warsaw";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD = "App.Flight.Checkout.Payment.Edit.Card";
	private static final String FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE = "App.Flight.Checkout.SlideToPurchase";
	private static final String FLIGHT_CHECKOUT_CONFIRMATION = "App.Flight.Checkout.Confirmation";

	private static final String FLIGHT_SEARCH_ONE_WAY_DETAILS = "App.Flight.Search.OneWay.Details";
	private static final String FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE = "App.Flight.Search.OneWay.BaggageFee";

	private static final String PREFIX_FLIGHT_SEARCH_FILTER = "App.Flight.Search.Filter";

	private static final String FLIGHT_PRICE_CHANGE_ERROR = "App.Flight.PriceChange.Error";

	public static void trackFlightPriceChangeError() {
		Log.d(TAG, "Tracking \"" + FLIGHT_PRICE_CHANGE_ERROR + "\" pageLoad...");
		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_PRICE_CHANGE_ERROR);
		s.setProp(36, "Total Price is null");
		s.track();
	}

	private static void trackPageLoadFlightCheckoutPaymentEditCard() {
		String pageName = FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD;
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(pageName);
		s.setEvar(18, pageName);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHideApacBillingAddressFields);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppAllowUnknownCardTypes);

		s.track();
	}

	public static void trackPageLoadFlightCheckoutWarsaw() {
		internalTrackPageLoadEventStandard(FLIGHT_CHECKOUT_WARSAW);
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

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// LX tracking
	//
	// Official Spec : https://confluence/display/Omniture/Mobile+App%3A+Local+Expert
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String LX_LOB = "local expert";
	private static final String LX_SEARCH = "App.LX.Search";
	private static final String LX_GT_SEARCH = "App.LX-GT.Search";
	private static final String LX_DESTINATION_SEARCH = "App.LX.Dest-Search";
	private static final String LX_GT_DESTINATION_SEARCH = "App.LX-GT.Dest-Search";
	private static final String LX_INFOSITE_INFORMATION = "App.LX.Infosite.Information";
	private static final String LX_GT_INFOSITE_INFORMATION = "App.LX-GT.Infosite.Information";
	private static final String LX_CHECKOUT_INFO = "App.LX.Checkout.Info";
	private static final String LX_GT_CHECKOUT_INFO = "App.LX-GT.Checkout.Info";
	private static final String LX_SEARCH_FILTER = "App.LX.Search.Filter";
	private static final String LX_SEARCH_FILTER_CLEAR = "App.LX.Search.Filter.Clear";
	private static final String LX_CHECKOUT_CONFIRMATION = "App.LX.Checkout.Confirmation";
	private static final String LX_GT_CHECKOUT_CONFIRMATION = "App.LX-GT.Checkout.Confirmation";
	private static final String LX_TICKET_SELECT = "App.LX.Ticket.Select";
	private static final String LX_GT_TICKET_SELECT = "App.LX-GT.Ticket.Select";
	private static final String LX_CHANGE_DATE = "App.LX.Info.DateChange";
	private static final String LX_GT_CHANGE_DATE = "App.LX-GT.Info.DateChange";
	private static final String LX_INFO = "LX_INFO";
	private static final String LX_TICKET = "App.LX.Ticket.";
	private static final String LX_GT_TICKET = "App.LX-GT.Ticket.";
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
	private static final String LX_FILTER = ".Filter";
	private static final String LX_TEXT_SEARCH = ".Keyword";

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

	public static void trackAppLXSearch(LxSearchParams lxSearchParams,
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
		setDateValues(s, lxSearchParams.getActivityStartDate(), lxSearchParams.getActivityEndDate());

		// Freeform location
		if (!TextUtils.isEmpty(lxSearchParams.getLocation())) {
			s.setEvar(48, lxSearchParams.getLocation());
		}

		// Number of search results
		if (lxSearchResponse.activities.size() > 0) {
			s.setProp(1, Integer.toString(lxSearchResponse.activities.size()));
		}

		trackAbacusTest(s, AbacusUtils.EBAndroidLXMOD);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXSearchCategories(LxSearchParams lxSearchParams,
		LXSearchResponse lxSearchResponse) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"" + LX_SEARCH_CATEGORIES + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_SEARCH_CATEGORIES);

		// Destination
		s.setProp(4, lxSearchResponse.regionId);
		s.setEvar(4, "D=c4");

		// prop and evar 5, 6
		setDateValues(s, lxSearchParams.getActivityStartDate(), lxSearchParams.getActivityEndDate());

		// Freeform location
		if (!TextUtils.isEmpty(lxSearchParams.getLocation())) {
			s.setEvar(48, lxSearchParams.getLocation());
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
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLXDisablePOISearch);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXSortAndFilterOpen() {
		Log.d(TAG, "Tracking \"" + LX_SEARCH_FILTER + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_SEARCH_FILTER);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLXFilterSearch);

		// Send the tracking data
		s.track();
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
		sb.append("." + categoryKey);
		trackLinkLXSearch(sb.toString());
	}

	public static void trackLinkLXSortAndFilterCleared() {
		trackLinkLXSearch(LX_SEARCH_FILTER_CLEAR);
	}

	public static void trackLinkLXTextSearch() {
		StringBuilder sb = new StringBuilder();
		sb.append(LX_SEARCH);
		sb.append(LX_FILTER);
		sb.append(LX_TEXT_SEARCH);
		trackLinkLXSearch(sb.toString());
	}

	private static void trackLinkLXSearch(String rffr) {
		String tpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		ADMS_Measurement s = internalTrackAppLX(LX_SEARCH + LX_FILTER);
		s.setProp(7, tpid);
		s.setEvar(28, rffr);
		s.setProp(16, rffr);
		s.setProp(61, tpid);
		s.trackLink(null, "o", LX_SEARCH, null, null);
	}

	public static void trackAppLXProductInformation(ActivityDetailsResponse activityDetailsResponse,
		LxSearchParams lxSearchParams, boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_INFOSITE_INFORMATION + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(
			isGroundTransport ? LX_GT_INFOSITE_INFORMATION : LX_INFOSITE_INFORMATION);

		s.setEvents(isGroundTransport ? "event3" : "event32");

		s.setProducts("LX;Merchant LX:" + activityDetailsResponse.id);

		// Destination
		s.setProp(4, activityDetailsResponse.regionId);
		s.setEvar(4, "D=c4");

		// prop and evar 5, 6
		setDateValues(s, lxSearchParams.getActivityStartDate(), lxSearchParams.getActivityEndDate());

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutPayment(String lxActivityId, LocalDate lxActivityStartDate,
		int selectedTicketsCount, String totalPriceFormattedTo2DecimalPlaces, boolean isGroundTransport) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_INFO + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(isGroundTransport ? LX_GT_CHECKOUT_INFO : LX_CHECKOUT_INFO);
		s.setEvents("event75");
		s.setProducts(addLXProducts(lxActivityId, totalPriceFormattedTo2DecimalPlaces, selectedTicketsCount));

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_enable_universal_checkout_on_lx)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppBringUniversalCheckoutToLX);
		}

		setLXDateValues(lxActivityStartDate, s);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutConfirmation(LXCheckoutResponse checkoutResponse,
		String lxActivityId, LocalDate lxActivityStartDate, LocalDate lxActivityEndDate, int selectedTicketsCount,
		boolean isGroundTransport) {
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

	private static void trackAppLXCheckoutTraveler(LineOfBusiness lob) {
		boolean isGroundTransport = lob.equals(LineOfBusiness.TRANSPORT);

		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_TRAVELER_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(isGroundTransport ? LX_GT_CHECKOUT_TRAVELER_INFO : LX_CHECKOUT_TRAVELER_INFO);
		s.setEvar(18, isGroundTransport ? LX_GT_CHECKOUT_TRAVELER_INFO : LX_CHECKOUT_TRAVELER_INFO);
		s.track();

	}

	private static void trackAppLXCheckoutPayment(LineOfBusiness lob) {
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

	private static String addLXProducts(String activityId, String totalMoney, int ticketCount) {
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

	private static void trackLinkLX(String rffr) {
		Log.d(TAG, "Tracking \"" + LX_CHANGE_DATE + "\" Link..." + "RFFR : " + rffr);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.setEvar(28, rffr);
		s.setProp(16, rffr);
		s.trackLink(null, "o", LX_INFO, null, null);
	}

	private static ADMS_Measurement internalTrackAppLX(String pageName) {
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

	///////////////////////////
	// Launch Screen

	private static final String LAUNCH_SIGN_IN = "App.LS.Account.SignIn";
	private static final String LAUNCH_GUEST_ITIN = "App.LS.Itin.Guest";
	private static final String LAUNCH_ACTIVE_ITIN = "App.LS.Itin.Active";
	private static final String LAUNCH_LAST_MINUTE_DEAL = "App.LS.LastMinuteDeals";
	private static final String LAUNCH_MEMBER_PRICING = "App.LS.MemberDeals";
	private static final String MEMBER_PRICING_SHOP = "App.MemberDeals.Shop";


	public static void trackLaunchSignIn() {
		ADMS_Measurement s = createTrackLinkEvent(LAUNCH_SIGN_IN);
		addStandardFields(s);
		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackLaunchActiveItin() {
		ADMS_Measurement s = createTrackLinkEvent(LAUNCH_ACTIVE_ITIN);
		addStandardFields(s);
		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackLaunchMemberPricing() {
		ADMS_Measurement s = createTrackLinkEvent(LAUNCH_MEMBER_PRICING);
		addStandardFields(s);
		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackLaunchLastMinuteDeal() {
		ADMS_Measurement s = createTrackLinkEvent(LAUNCH_LAST_MINUTE_DEAL);
		addStandardFields(s);
		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackMemberPricingShop() {
		ADMS_Measurement s = createTrackLinkEvent(MEMBER_PRICING_SHOP);
		addStandardFields(s);
		s.trackLink(null, "o", "Member Deals", null, null);
	}

	private static void trackProWizardTest(ADMS_Measurement s, int testValue) {
		if (!ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			return;
		}
		AbacusTest test = Db.getAbacusResponse().testForKey(AbacusUtils.ProWizardTest);
		if (test == null) {
			return;
		}
		test.value = testValue;

		appendAbacusTest(s, test);
		logAbacusQuery(test);
	}

	@VisibleForTesting
	static void trackAbacusTest(ADMS_Measurement s, ABTest abTest) {
		if (!ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			return;
		}

		if (!AbacusFeatureConfigManager.shouldTrackTest(sContext, abTest)) {
			return;
		}

		AbacusTest test = Db.getAbacusResponse().testForKey(abTest);

		if (test == null) {
			return;
		}

		appendAbacusTest(s, test);
		logAbacusQuery(test);
	}

	private static void appendAbacusTest(ADMS_Measurement s, AbacusTest test) {
		// Adds piping for multivariate AB Tests.
		String analyticsString = AbacusUtils.appendString(s.getProp(34)) + AbacusUtils.getAnalyticsString(test);
		if (!TextUtils.isEmpty(analyticsString)) {
			s.setEvar(34, analyticsString);
			s.setProp(34, analyticsString);
		}
	}

	private static void logAbacusQuery(AbacusTest test) {
		AbacusLogQuery query = new AbacusLogQuery(Db.getAbacusGuid(), PointOfSale.getPointOfSale().getTpid(), 0);
		query.addExperiment(test);
		Ui.getApplication(sContext).appComponent().abacus().logExperiment(query);
	}

	///////////////////////////
	// Search Results Screen - Hotels

	private static final String PROP_DATE_FORMAT = "yyyy-MM-dd";
	private static final String LX_CONFIRMATION_PROP_DATE_FORMAT = "yyyyMMdd";

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

	////////////////////////////
	// Air Attach

	private static final String AIR_ATTACH_ELIGIBLE = "App.Flight.CKO.AttachEligible";
	private static final String AIR_ATTACH_HOTEL_ADD = "App.Hotels.IS.AddTrip";
	private static final String ADD_ATTACH_HOTEL = "App.Flight.CKO.Add.AttachHotel";
	private static final String AIR_ATTACH_ITIN_XSELL = "Itinerary X-Sell";
	private static final String AIR_ATTACH_ITIN_XSELL_REF = "App.Itin.X-Sell.Hotel";
	private static final String AIR_ATTACH_PHONE_LAUNCH_SCREEN = "App Landing";
	private static final String AIR_ATTACH_PHONE_LAUNCH_SCREEN_CLICK = "App.LS.Attach.Hotel";

	public static void trackAirAttachItinCrossSell() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, AIR_ATTACH_ITIN_XSELL_REF);
		s.setProp(16, AIR_ATTACH_ITIN_XSELL_REF);
		s.trackLink(null, "o", AIR_ATTACH_ITIN_XSELL, null, null);
	}

	public static void trackPhoneAirAttachLaunchScreenClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(12, "Launch.Attach.Hotel");
		s.setEvar(28, AIR_ATTACH_PHONE_LAUNCH_SCREEN_CLICK);
		s.setProp(16, AIR_ATTACH_PHONE_LAUNCH_SCREEN_CLICK);
		s.trackLink(null, "o", AIR_ATTACH_PHONE_LAUNCH_SCREEN, null, null);
	}

	public static void trackFlightConfirmationAirAttachEligible() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, AIR_ATTACH_ELIGIBLE);
		s.setProp(16, AIR_ATTACH_ELIGIBLE);
		s.trackLink(null, "o", "Checkout", null, null);
	}

	// Evar 30 doc: https://confluence/display/Omniture/eVar30+-+Product+Details
	private static final String EVAR30_DATE_FORMAT = "yyyyMMdd";

	public static void trackFlightConfirmationAirAttachClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, ADD_ATTACH_HOTEL);
		s.setProp(16, ADD_ATTACH_HOTEL);
		s.trackLink(null, "o", "Checkout", null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itin Tracking
	//
	// Spec: https://confluence/display/Omniture/App+Itinerary
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String ITIN_EMPTY = "App.Itinerary.Empty";
	private static final String ITIN_FIND = "App.Itinerary.Find";
	private static final String ITIN_FIND_GUEST = "App.Itinerary.Find.Guest";
	private static final String ITIN_ADD_SUCCESS = "App.Itinerary.Add.Success";
	private static final String ITIN = "App.Itinerary";
	private static final String ITIN_HOTEL = "App.Itinerary.Hotel";
	private static final String ITIN_HOTEL_UPGRADE = "App.Itinerary.Hotel.Upgrade";
	private static final String ITIN_HOTEL_DIRECTIONS = "App.Itinerary.Hotels.Directions";
	private static final String ITIN_NEW_HOTEL_DIRECTIONS = "App.Itinerary.Hotel.Directions";
	private static final String ITIN_HOTEL_CALL = "App.Itinerary.Hotel.Call";
	private static final String ITIN_HOTEL_INFO = "App.Itinerary.Hotel.Info.Additional";
	private static final String ITIN_HOTEL_SHARE_PREFIX = "App.Itinerary.Hotel.Share.";
	private static final String ITIN_FLIGHT = "App.Itinerary.Flight";
	private static final String ITIN_FLIGHT_CHECKIN = "App.Itinerary.Flight.CheckInNow";
	private static final String ITIN_FLIGHT_CHECKIN_SUCCESS = "App.Itinerary.Flight.CheckIn.Yes";
	private static final String ITIN_FLIGHT_CHECKIN_FAILURE = "App.Itinerary.Flight.CheckIn.No";
	private static final String ITIN_FLIGHT_CHECKIN_VISIT = "App.Itinerary.Flight.VisitAirline";
	private static final String ITIN_FLIGHT_BAGGAGEINFO = "App.Itinerary.Flight.Baggage.Info";
	private static final String ITIN_FLIGHT_DIRECTIONS = "App.Itinerary.Flight.Airport.Directions";
	private static final String ITIN_FLIGHT_TERMINAL_MAPS = "App.Itinerary.Flight.Airport.TerminalMaps";
	private static final String ITIN_FLIGHT_INFO = "App.Itinerary.Flight.Info.Additional";
	private static final String ITIN_FLIGHT_SHARE_PREFIX = "App.Itinerary.Flight.Share.";
	private static final String ITIN_FLIGHT_COPY_PNR = "App.Itinerary.Flight.CopyPNR";
	private static final String ITIN_FLIGHT_MANAGE_BOOKING = "App.Itinerary.Flight.ManageBooking";
	private static final String ITIN_FLIGHT_TRAVELER_INFO = "App.Itinerary.Flight.TravelerInfo";
	private static final String ITIN_FLIGHT_INFO_ADDITIONAL = "App.Itinerary.Flight.Info.Additional";
	private static final String ITIN_FLIGHT_PRICE_SUMMARY = "App.Itinerary.Flight.PriceSummary";
	private static final String ITIN_FLIGHT_MAP_OPEN = "App.Itinerary.Flight.Map";
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
	private static final String ITIN_ERROR = "App.Itinerary.Error";
	private static final String ITIN_HOTEL_GALLERY_OPEN = "App.Itinerary.Hotel.Photos";
	private static final String ITIN_HOTEL_MAP_OPEN = "App.Itinerary.Hotel.Map";
	private static final String ITIN_CHANGE_POSA = "App.Itinerary.POSa";
	private static final String ITIN_HOTEL_INFO_EDIT_ROOM = "App.Itinerary.Hotel.Info.EditRoom";
	private static final String ITIN_NEW_SIGN_IN = "App.Itinerary.Login.Start";
	private static final String ITIN_USER_REFRESH = "App.Itinerary.User.Refresh";
	private static final String ITIN_HOTEL_MANAGE_BOOKING = "App.Itinerary.Hotel.ManageBooking";
	private static final String ITIN_HOTEL_PRICING_REWARDS = "App.Itinerary.Hotel.PricingRewards";
	private static final String ITIN_HOTEL_CHECK_IN_POLICIES = "App.Itinerary.Hotel.Info.Check-in";
	private static final String ITIN_HOTEL_CHANGE_CANCEL_RULES = "App.Itinerary.Hotel.Manage.Info.Change-Cancel";
	private static final String ITIN_FLIGHT_LEG_DETAIL_WIDGET_RULES_RESTRICTION = "App.Itinerary.Flight.Manage.AirlineRules";
	private static final String ITIN_HOTEL_CALL_HOTEL = "App.Itinerary.Hotel.Manage.Call.Hotel";
	private static final String ITIN_HOTEL_CALL_EXPEDIA = "App.Itinerary.Hotel.Manage.Call.Expedia";
	private static final String ITIN_HOTEL_OPEN_SUPPORT_WEBSITE = "App.Itinerary.Hotel.Manage.CSP";
	private static final String ITIN_FLIGHT_OPEN_SUPPORT_WEBSITE = "App.Itinerary.Flight.Manage.CSP";
	private static final String ITIN_FLIGHT_CALL_EXPEDIA = "App.Itinerary.Flight.Manage.Call.Expedia";
	private static final String ITIN_TRIP_REFRESH_CALL_MADE = "App.Itinerary.Call.Made";
	private static final String ITIN_TRIP_REFRESH_CALL_SUCCESS = "App.Itinerary.Call.Success";
	private static final String ITIN_TRIP_REFRESH_CALL_FAILURE = "App.Itinerary.Call.Failure";
	private static final String ITIN_NEW_FLIGHT_DIRECTIONS = "App.Itinerary.Flight.Directions";
	private static final String ITIN_NEW_FLIGHT_TERMINAL_MAPS = "App.Itinerary.Flight.TerminalMaps";
	private static final String ITIN_HOTEL_MAP_DIRECTIONS = "App.Map.Directions.Drive";
	private static final String ITIN_HOTEL_MAP_PAN = "App.Map.Directions.Pan";
	private static final String ITIN_HOTEL_MAP_ZOOM_IN = "App.Map.Directions.ZoomIn";
	private static final String ITIN_HOTEL_MAP_ZOOM_OUT = "App.Map.Directions.ZoomOut";

	public static void trackItinTripRefreshCallMade() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_TRIP_REFRESH_CALL_MADE);
		s.setEvents("event286");
		s.trackLink(null, "o", "Trips Call", null, null);
	}

	public static void trackItinTripRefreshCallSuccess(boolean tripHasHotel, boolean tripHasFlight) {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_TRIP_REFRESH_CALL_SUCCESS);
		s.setEvents("event287");
		if (tripHasHotel) {
			trackAbacusTest(s, AbacusUtils.TripsHotelScheduledNotificationsV2);
		}
		if (tripHasFlight) {
			trackAbacusTest(s, AbacusUtils.TripsNewFlightAlerts);
		}
		s.trackLink(null, "o", "Trips Call", null, null);
	}

	public static void trackItinTripRefreshCallFailure(String error) {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_TRIP_REFRESH_CALL_FAILURE);
		s.setEvents("event288");
		s.setProp(36, error);
		s.trackLink(null, "o", "Trips Call", null, null);
	}

	public static void trackItinNewFlightDirections() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_NEW_FLIGHT_DIRECTIONS);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinNewFlightTerminalMaps() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_NEW_FLIGHT_TERMINAL_MAPS);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinEmpty() {
		internalTrackPageLoadEventStandard(ITIN_EMPTY);
	}

	public static void trackItinError() {
		Log.d(TAG, "Tracking \"" + ITIN_ERROR + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_ERROR);
		s.setEvents("event98");
		s.setEvar(18, ITIN_ERROR);
		s.setProp(36, "itin:unable to retrieve trip summary");
		s.track();
	}

	public static void trackHotelItinGalleryOpen() {
		createAndtrackLinkEvent(ITIN_HOTEL_GALLERY_OPEN, "Itinerary Action");
	}

	public static void trackHotelItinRoomUpgradeClick() {
		createAndtrackLinkEvent(ITIN_HOTEL_UPGRADE, "Itinerary Action");
	}

	public static void trackItinChangePOS() {
		trackItineraryClickAction(ITIN_CHANGE_POSA);
	}

	public static void trackItinEditRoomInfoWebViewOpen() {
		trackItineraryClickAction(ITIN_HOTEL_INFO_EDIT_ROOM);
	}

	private static void trackItineraryClickAction(String trackingId) {
		Log.d(TAG, "Tracking \"" + trackingId + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(trackingId);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackFindItin() {
		internalTrackPageLoadEventStandard(ITIN_FIND);
	}

	public static void trackFindGuestItin() {
		internalTrackPageLoadEventStandard(ITIN_FIND_GUEST);
	}

	/**
	 * Track the itin card sharing click
	 *
	 * @param type          which itin card type was being shared
	 * @param isLongMessage true denotes it was a share message long, false denotes share message short
	 */
	public static void trackItinShare(Type type, boolean isLongMessage) {
		String pageName = getTrackSharePrefix(type);
		if (pageName.isEmpty()) {
			return;
		}

		if (isLongMessage) {
			pageName += "Mail";
		}
		else {
			pageName += "Message";
		}

		internalTrackLink(pageName);
	}

	public static void trackItinShareStart(Type type) {
		String pageName = getTrackSharePrefix(type);
		if (pageName.isEmpty()) {
			return;
		}
		pageName += "Start";

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsDetailRemoveCalendar);
		s.trackLink(null, "o", "Itinerary Sharing", null, null);
	}

	private static String getTrackSharePrefix(Type type) {
		switch (type) {
		case FLIGHT:
			return ITIN_FLIGHT_SHARE_PREFIX;
		case HOTEL:
			return ITIN_HOTEL_SHARE_PREFIX;
		case CAR:
			return ITIN_CAR_SHARE_PREFIX;
		case ACTIVITY:
			return ITIN_ACTIVITY_SHARE_PREFIX;
		default:
			return "";
		}
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
			boolean isLong = shareType.equals("Mail");
			trackItinShare(type, isLong);
			return;
		}

		String pageName = ITIN + "." + itinType + ".Share." + shareType;

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.setEvar(2, itinType);
		s.setEvents("event48");

		internalTrackLink(s);
	}

	public static void trackItinShareAppChosen(String tripType, String shareApp) {
		String pageName = ITIN + "." + tripType + ".Share." + shareApp;

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.setEvar(2, tripType);
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

	public static void trackHotelItinManageBookingClick() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_MANAGE_BOOKING);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackHotelItinPricingRewardsClick() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_PRICING_REWARDS);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackHotelItinAdditionalInfoClick() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_INFO);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackHotelItinCheckInPoliciesDialogClick() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_CHECK_IN_POLICIES);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackHotelItinChangeAndCancelRulesDialogClick() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_CHANGE_CANCEL_RULES);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackFlightItinLegDetailWidgetRulesAndRestrictionsDialogClick() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_LEG_DETAIL_WIDGET_RULES_RESTRICTION);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}
	private static HashMap<String, String> deepLinkArgs = new HashMap<>();

	/* This is a separate method because other classes also use it */
	public static void setDeepLinkTrackingParams(String key, String value) {
		deepLinkArgs.put(key, value);
	}

	@VisibleForTesting
	public static HashMap<String, String> getDeepLinkArgs() {
		return deepLinkArgs;
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

	public static void trackItin(PageUsableData pageLoadTimeData) {
		Log.d(TAG, "Tracking \"" + ITIN + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN);
		if (userStateManager.isUserAuthenticated()) {
			String usersTripComponentTypeEventString = getUsersTripComponentTypeEventString();
			if (!usersTripComponentTypeEventString.isEmpty()) {
				s.setEvents("event63" + "," + usersTripComponentTypeEventString);
				s.setProp(75, TripUtils.createUsersProp75String(getUsersTrips()));
			}
			else {
				s.setEvents("event63"); //we still need to track event63 even if the users does not have any trips
			}
		}
		else {
			s.setEvents("event63");
		}
		if (pageLoadTimeData != null) {
			addPageLoadTimeTrackingEvents(s, pageLoadTimeData);
		}
		s.track();
	}

	public static void trackItinHotel(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_HOTEL + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_HOTEL);
		s.setEvents("event63");
		trackAbacusTest(s, AbacusUtils.EBAndroidAppItinHotelRedesign);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelUpgrade);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsDetailRemoveCalendar);
		s.track();
	}

	public static void trackItinHotelRedesign() {
		Log.d(TAG, "Tracking \"" + ITIN_HOTEL + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_HOTEL);
		s.setEvents("event63");
		trackAbacusTest(s, AbacusUtils.EBAndroidAppItinHotelRedesign);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelUpgrade);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsDetailRemoveCalendar);
		trackAbacusTest(s, AbacusUtils.TripsHotelMap);
		s.track();
	}

	public static void trackItinHotelDirections() {
		internalTrackLink(ITIN_HOTEL_DIRECTIONS);
	}

	public static void trackRedesignItinHotelDirections() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_NEW_HOTEL_DIRECTIONS);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinHotelDirectionsButton() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_MAP_DIRECTIONS);
		s.trackLink(null, "o", "Map Action", null, null);
	}

	public static void trackItinExpandedMapZoomIn() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_MAP_ZOOM_IN);
		s.trackLink(null, "o", "Map Action", null, null);
	}

	public static void trackItinExpandedMapZoomOut() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_MAP_ZOOM_OUT);
		s.trackLink(null, "o", "Map Action", null, null);
	}

	public static void trackItinExpandedMapZoomPan() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_MAP_PAN);
		s.trackLink(null, "o", "Map Action", null, null);
	}

	public static void trackItinHotelExpandMap() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_MAP_OPEN);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightExpandMap() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_MAP_OPEN);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinHotelCallHotel() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_CALL_HOTEL);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinHotelCallSupport() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_CALL_EXPEDIA);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinHotelOpenSupportWebsite() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_HOTEL_OPEN_SUPPORT_WEBSITE);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightOpenSupportWebsite() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_OPEN_SUPPORT_WEBSITE);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightCallSupport() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_CALL_EXPEDIA);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinHotelCall() {
		internalTrackLink(ITIN_HOTEL_CALL);
	}

	private static void trackItinHotelInfo() {
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
		case RAILS:
			//TODO:track rails info
			break;
		default:
			break;
		}
	}

	public static void trackItinTravelerInfo() {
		Log.d(TAG, "Tracking \"" + ITIN_FLIGHT_TRAVELER_INFO + "\" pageLoad" );
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_FLIGHT_TRAVELER_INFO);
		if (userStateManager.isUserAuthenticated()) {
			String usersTripComponentTypeEventString = getUsersTripComponentTypeEventString();
			if (!usersTripComponentTypeEventString.isEmpty()) {
				s.setEvents("event63" + "," + usersTripComponentTypeEventString);
				s.setProp(75, TripUtils.createUsersProp75String(getUsersTrips()));
			}
			else {
				s.setEvents("event63");
			}
		}
		s.setProp(2, "itinerary");
		s.setEvar(2, "D=c2");
		s.track();
	}

	public static void trackItinFlight(Context context, Map trip) {
		Log.d(TAG, "Tracking \"" + ITIN_FLIGHT + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_FLIGHT);
		if (userStateManager.isUserAuthenticated()) {
			String usersTripComponentTypeEventString = getUsersTripComponentTypeEventString();
			if (!usersTripComponentTypeEventString.isEmpty()) {
				s.setEvents("event63" + "," + usersTripComponentTypeEventString);
				s.setProp(75, TripUtils.createUsersProp75String(getUsersTrips()));
			}
			else {
				s.setEvents("event63");
			}
		}
		if (trip != null) {
			s.setProducts(String.valueOf(trip.get("productString")));
			s.setProp(8, String.valueOf(trip.get("orderAndTripNumbers")));

			if (String.valueOf(trip.get("duration")) != null) {
				s.setEvar(6, String.valueOf(trip.get("duration")));
			}
			if (String.valueOf(trip.get("tripStartDate")) != null) {
				s.setProp(5, String.valueOf(trip.get("tripStartDate")));
			}
			if (String.valueOf(trip.get("tripEndDate")) != null) {
				s.setProp(6, String.valueOf(trip.get("tripEndDate")));
			}
			if (String.valueOf(trip.get("daysUntilTrip")) != null) {
				s.setEvar(5, String.valueOf(trip.get("daysUntilTrip")));
			}
		}
		trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsDetailRemoveCalendar);
		trackAbacusTest(s, AbacusUtils.TripsFlightsNewdesign);
		s.setProp(2, "itinerary");
		s.setEvar(2, "D=c2");
		s.track();
	}

	public static void trackItinFlightManageBookingActivity(Context context, Map trip) {
		Log.d(TAG, "Tracking \"" + ITIN_FLIGHT_MANAGE_BOOKING + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_FLIGHT_MANAGE_BOOKING);
		if (userStateManager.isUserAuthenticated()) {
			String usersTripComponentTypeEventString = getUsersTripComponentTypeEventString();
			if (!usersTripComponentTypeEventString.isEmpty()) {
				s.setEvents("event63" + "," + usersTripComponentTypeEventString);
				s.setProp(75, TripUtils.createUsersProp75String(getUsersTrips()));
			}
			else {
				s.setEvents("event63");
			}
		}
		if (trip != null) {
			s.setProducts(String.valueOf(trip.get("productString")));
			s.setProp(8, String.valueOf(trip.get("orderAndTripNumbers")));

			if (String.valueOf(trip.get("duration")) != null) {
				s.setEvar(6, String.valueOf(trip.get("duration")));
			}
			if (String.valueOf(trip.get("tripStartDate")) != null) {
				s.setProp(5, String.valueOf(trip.get("tripStartDate")));
			}
			if (String.valueOf(trip.get("tripEndDate")) != null) {
				s.setProp(6, String.valueOf(trip.get("tripEndDate")));
			}
			if (String.valueOf(trip.get("daysUntilTrip")) != null) {
				s.setEvar(5, String.valueOf(trip.get("daysUntilTrip")));
			}
		}
		s.setProp(2, "itinerary");
		s.setEvar(2, "D=c2");
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

	public static void trackItinFlightBaggageInfoClicked() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_BAGGAGEINFO);
		s.trackLink(null, "o", "Itinerary Action", null, null);
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

	private static void trackItinFlightInfo() {
		internalTrackLink(ITIN_FLIGHT_INFO);
	}

	public static void trackItinFlightCopyPNR() {
		internalTrackLink(ITIN_FLIGHT_COPY_PNR);
	}

	public static void trackItinFlightManageBooking() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_MANAGE_BOOKING);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}
	public static void trackItinFlightTravelerInfo() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_TRAVELER_INFO);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightPriceSummary() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_PRICE_SUMMARY);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinFlightAdditionalInfo() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_FLIGHT_INFO_ADDITIONAL);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinCar(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_CAR + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_CAR);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsDetailRemoveCalendar);
		s.setEvents("event63");
		s.track();
	}

	public static void trackItinCarDirections() {
		internalTrackLink(ITIN_CAR_DIRECTIONS);
	}

	public static void trackItinCarCall() {
		internalTrackLink(ITIN_CAR_CALL);
	}

	private static void trackItinCarInfo() {
		internalTrackLink(ITIN_CAR_INFO);
	}

	public static void trackItinActivity(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_ACTIVITY + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_ACTIVITY);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppTripsDetailRemoveCalendar);
		s.setEvents("event63");
		s.track();
	}

	public static void trackItinActivityRedeem() {
		internalTrackLink(ITIN_ACTIVITY_REDEEM);
	}

	public static void trackItinActivitySupport() {
		internalTrackLink(ITIN_ACTIVITY_SUPPORT);
	}

	private static void trackItinActivityInfo() {
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
	private static final String NOTIFICATION_HOTEL_GET_READY = "Itinerary.Hotel.GetReady";
	private static final String NOTIFICATION_HOTEL_ACTIVITY_CROSSSEll = "Itinerary.Hotel.CrossSell.Activity";
	private static final String NOTIFICATION_HOTEL_ACTIVITY_IN_TRIP = "Itinerary.Hotel.InTrip.Activity";

	public static void trackNotificationClick(Notification notification) {
		String link = setItinNotificationLink(notification);
		Log.d(TAG, "Tracking \"" + link + "\" click");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(11, link);
		s.setEvents("event212");
		s.trackLink(null, "o", link, null, null);
	}

	public static void trackNotificationShown(Notification notification) {
		String link = setItinNotificationLink(notification);
		ADMS_Measurement s = createTrackLinkEvent(link);
		s.setEvents("event208");
		s.trackLink(null, "o", link, null, null);
	}

	@VisibleForTesting
	public static String setItinNotificationLink(Notification notification) {
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
		case HOTEL_GET_READY:
		case HOTEL_PRE_TRIP:
			link = NOTIFICATION_HOTEL_GET_READY;
			break;
		case HOTEL_ACTIVITY_CROSSSEll:
			link = NOTIFICATION_HOTEL_ACTIVITY_CROSSSEll;
			break;
		case HOTEL_ACTIVITY_IN_TRIP:
			link = NOTIFICATION_HOTEL_ACTIVITY_IN_TRIP;
			break;
		default:
			link = "Itinerary." + type.name();
			Log.w(TAG, "Unknown Notification Type \"" + type.name() + "\". Taking a guess.");
			break;
		}
		return link;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itinerary Notification Click Tracking
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String CROSS_SELL_ITIN_TO_HOTEL = "CrossSell.Itinerary.Hotels";
	private static final String CROSS_SELL_LX_FROM_ITIN = "Itinerary.CrossSell.LX";
	private static final String ADD_LX_ITIN = "App.Itin.XSell.LX";

	public static void trackCrossSellItinToHotel() {
		trackCrossSell(CROSS_SELL_ITIN_TO_HOTEL);
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
	private static final String LAUNCH_SCREEN_GLOBAL_NAVIGATION = "App.Global";
	private static final String LAUNCH_SCREEN_EXPANDED_LOB = "App.LS.Srch.ExpandSrch";
	private static final String LAUNCH_SCREEN_LOCATION_SOFT_PROMPT_ACCEPT = "App.LS.LocPermSP.Accept";
	private static final String LAUNCH_SCREEN_LOCATION_SOFT_PROMPT_CANCEL = "App.LS.LocPermSP.Cancel";
	private static final String LAUNCH_SCREEN_LOCATION_NATIVE_PROMPT_ACCEPT = "App.DeviceLocation.Ok";
	private static final String LAUNCH_SCREEN_LOCATION_NATIVE_PROMPT_CANCEL = "App.DeviceLocation.Opt-Out";
	private static final String LOGIN_SCREEN = "App.Account.SignIn";
	private static final String LOGIN_SUCCESS = "App.Account.Login.Success";
	private static final String LOGOUT_SELECT = "App.Account.Logout.Select";
	private static final String LOGOUT_CANCEL = "App.Account.Logout.Cancel";
	private static final String LOGOUT_SUCCESS = "App.Account.Logout";
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
	private static final String LOGIN_SINGLE_PAGE = "App.Account.Create";
	private static final String LOGIN_MARKETING_OPT_IN = "App.Account.Terms.Email.Opt-In";
	private static final String LOGIN_MARKETING_OPT_OUT = "App.Account.Terms.Email.Opt-Out";
	private static final String LOGIN_ACCOUNT_CREATE_SUCCESS = "App.Account.Create.Success";
	private static final String LOGIN_ACCOUNT_CREATE_ERROR = "App.Account.Create.Error";
	private static final String LOGIN_ACCOUNT_FACEBOOK_SIGN_IN = "App.Account.FacebookSignIn";
	private static final String ACCOUNT_SCREEN = "App.Account.MyAccount";
	private static final String ACCOUNT_COUNTRY_SETTING = "App.Account.Settings.Country";
	private static final String ACCOUNT_SUPPORT_WEBSITE = "App.Account.Support.Website";
	private static final String ACCOUNT_SUPPORT_BOOKING = "App.Account.Support.Booking";
	private static final String ACCOUNT_SUPPORT_APP = "App.Account.Support.App";
	private static final String ACCOUNT_COMMUNICATE_RATE = "App.Account.Communicate.Rate";
	private static final String ACCOUNT_LEGAL_CLEAR_DATA = "App.Account.Legal.ClearData";
	private static final String ACCOUNT_LEGAL_TERMS = "App.Account.Legal.Terms";
	private static final String ACCOUNT_LEGAL_PRIVACY = "App.Account.Legal.Privacy";
	private static final String ACCOUNT_LEGAL_ATOL = "App.Account.Legal.ATOL";
	private static final String ACCOUNT_LEGAL_LICENSES = "App.Account.Legal.OpenSourceLicenses";
	private static final String ACCOUNT_APP_DOWNLOAD = "App.Account.Download";
	private static final String ACCOUNT_SIGN_OUT = "App.Account.Logout";
	private static final String MEMBER_PRICING_SCREEN = "App.MemberDeals";
	private static final String NEW_USER_ONBOARDING_LOB = "App.Onboarding.MultiLOB";
	private static final String NEW_USER_ONBOARDING_ITINERARY = "App.Onboarding.Itinerary";
	private static final String NEW_USER_ONBOARDING_LOYALTY = "App.Onboarding.Loyalty";
	private static final String NEW_USER_ONBOARDING_GO_SIGNIN = "App.Onboarding.SignIn";
	private static final String PENDING_POINTS_TAP = "App.PointsToolTip.Tap";
	private static final String LEGACY_USER_APP_UPDATE_TAP = "App.LS.Package.AppUpdate";

	public static void trackLoginSuccess() {
		ADMS_Measurement s = createTrackLinkEvent(LOGIN_SUCCESS);
		s.setEvents("event26");
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackSmartLockPasswordAutoSignIn() {
		ADMS_Measurement s = createTrackLinkEvent(LOGIN_SUCCESS);
		s.setEvents("event26,event216");
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackSmartLockPasswordSignIn() {
		ADMS_Measurement s = createTrackLinkEvent(LOGIN_SUCCESS);
		s.setEvents("event26,event218");
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public enum LogOut {
		SELECT(LOGOUT_SELECT),
		CANCEL(LOGOUT_CANCEL),
		SUCCESS(LOGOUT_SUCCESS);

		private String pageName;

		LogOut(String logoutSelect) {
			pageName = logoutSelect;
		}

		public String getPageName() {
			return this.pageName;
		}
	}

	public static void trackLogOutAction(LogOut type) {
		ADMS_Measurement s = createTrackLinkEvent(type.getPageName());
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackLoginScreen() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SCREEN);
		s.setEvar(18, LOGIN_SCREEN);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppSmartLockTest);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppAccountRecaptcha);
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

	public static void trackLocationSoftPrompt(boolean accept) {
		ADMS_Measurement s;
		if (accept) {
			s = createTrackLinkEvent(LAUNCH_SCREEN_LOCATION_SOFT_PROMPT_ACCEPT);
		}
		else {
			s = createTrackLinkEvent(LAUNCH_SCREEN_LOCATION_SOFT_PROMPT_CANCEL);
		}
		s.trackLink(null, "o", "Soft Prompt", null, null);
	}

	public static void trackLocationNativePrompt(boolean accept) {
		ADMS_Measurement s;
		if (accept) {
			s = createTrackLinkEvent(LAUNCH_SCREEN_LOCATION_NATIVE_PROMPT_ACCEPT);
			s.setEvents("event41");
		}
		else {
			s = createTrackLinkEvent(LAUNCH_SCREEN_LOCATION_NATIVE_PROMPT_CANCEL);
			s.setEvents("event40");
		}
		s.trackLink(null, "o", "App Message", null, null);
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

	public static void trackSinglePage() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SINGLE_PAGE);
		s.setEvar(18, LOGIN_SINGLE_PAGE);
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
		String pageName;
		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppAccountSinglePageSignUp)) {
			pageName = LOGIN_SINGLE_PAGE;
		}
		else {
			pageName = LOGIN_ACCOUNT_CREATE_SUCCESS;
		}

		s.setAppState(pageName);
		s.setEvar(18, pageName);
		s.setEvents("event25,event26");
		s.track();
	}

	public static void trackSignInError(String error) {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SCREEN);
		s.setEvar(18, LOGIN_SCREEN);
		s.setProp(36, error);
		s.track();
	}

	public static void trackAccountCreationError(String error) {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		String pageName;
		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppAccountSinglePageSignUp)) {
			pageName = LOGIN_SINGLE_PAGE;
		}
		else {
			pageName = LOGIN_ACCOUNT_CREATE_ERROR;
		}

		s.setAppState(pageName);
		s.setEvar(18, pageName);
		s.setProp(36, error);
		s.track();
	}

	public static void trackFacebookSignIn() {
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(ACCOUNT_SCREEN);
		s.setEvar(18, ACCOUNT_SCREEN);
		s.setEvar(28, LOGIN_ACCOUNT_FACEBOOK_SIGN_IN);
		s.setProp(16, LOGIN_ACCOUNT_FACEBOOK_SIGN_IN);
		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		s.trackLink("nil", "o", "Accounts", null, null);
	}

	// todo : https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/5759
	public static void trackPageLoadLaunchScreen(int proWizardTestValue) {
		ADMS_Measurement s = createTrackPageLoadEventBase(LAUNCH_SCREEN);
		boolean isFirstAppLaunch =
			ExpediaBookingApp.isFirstLaunchEver() || ExpediaBookingApp.isFirstLaunchOfAppVersion();
		if (userStateManager.isUserAuthenticated()) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppShowMemberPricingCardOnLaunchScreen);
		}

		if (PackageUtil.INSTANCE.isPackagesLobTitleABTestEnabled()) {
			trackAbacusTest(s, AbacusUtils.PackagesTitleChange);
		}

		if (PackageUtil.INSTANCE.isPackageLOBUnderABTest()) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppPackagesEnablePOS);
		}

		trackProWizardTest(s, proWizardTestValue);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsIndiaRebootBrowserExperience);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppAccountSinglePageSignUp);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppSoftPromptLocation);

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_enable_meso_destination_card)) {
			trackAbacusTest(s, AbacusUtils.MesoDestination);
		}

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_enable_launch_screen_brand_colors)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppBrandColors);
		}

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_enable_last_minute_deals)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppLastMinuteDeals);
		}

		if (AbacusFeatureConfigManager.isUserBucketedForTest(sContext, AbacusUtils.HolidayFun)) {
			s.setEvents("event330");
		}

		if (userStateManager.isUserAuthenticated()) {
			String usersTripComponentTypeEventString = getUsersTripComponentTypeEventString();
			if (!usersTripComponentTypeEventString.isEmpty()) {
				s.setEvents(usersTripComponentTypeEventString);
				s.setProp(75, TripUtils.createUsersProp75String(getUsersTrips()));
			}
		}
		s.setProp(2, "storefront");
		s.setEvar(2, "storefront");
		s.track();
	}

	public static void trackMemberPricingPageLoad() {
		ADMS_Measurement s = createTrackPageLoadEventBase(MEMBER_PRICING_SCREEN);
		s.setProp(2, "Merch");
		s.setEvar(2, "D=c2");
		s.setEvar(12, MEMBER_PRICING_SCREEN);
		s.track();
	}

	public static void trackNewUserOnboardingPage(OnboardingPagerState pagerState) {
		String pageName = "";
		switch (pagerState) {
		case BOOKING_PAGE:
			pageName = NEW_USER_ONBOARDING_LOB;
			break;
		case TRIP_PAGE:
			pageName = NEW_USER_ONBOARDING_ITINERARY;
			break;
		case REWARD_PAGE:
			pageName = NEW_USER_ONBOARDING_LOYALTY;
			break;
		}
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.track();
	}

	public static void trackNewUserOnboardingGoSignIn() {
		Log.d(TAG, "Tracking \"Let's Go Button\" onClick");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(16, NEW_USER_ONBOARDING_GO_SIGNIN);
		s.setEvar(28, NEW_USER_ONBOARDING_GO_SIGNIN);
		s.trackLink(null, "o", "New User Onboarding", null, null);
	}

	public static void trackAccountPageLoad() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(ACCOUNT_SCREEN);
		s.setEvar(18, ACCOUNT_SCREEN);
		s.track();
	}

	public static void trackPendingPointsTooltipTapped() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(PENDING_POINTS_TAP);
		s.setEvar(28, PENDING_POINTS_TAP);
		s.track();
	}

	public static void trackGlobalNavigation(int tabPosition) {
		String link = LAUNCH_SCREEN_GLOBAL_NAVIGATION;
		switch (tabPosition) {
		case 0:
			link += ".Shop";
			break;
		case 1:
			link += ".Trips";
			break;
		case 2:
			link += ".Info";
			break;
		}
		ADMS_Measurement s = createTrackLinkEvent(link);

		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackExpandedLobView() {
		ADMS_Measurement s = createTrackLinkEvent(LAUNCH_SCREEN_EXPANDED_LOB);
		s.trackLink(null, "o", "App Landing", null, null);
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

	public static void trackForceUpgradeBanner() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(LAUNCH_SCREEN_PACKAGE_NAVIGATION);
		s.setEvents("event335");
		s.track();
	}

	public static void trackAppUpgradeClick() {
		ADMS_Measurement s = createTrackLinkEvent(LEGACY_USER_APP_UPDATE_TAP);
		s.trackLink(null, "o", "App Landing", null, null);
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
	private static final String LAUNCH_SCREEN_PACKAGE_NAVIGATION = "App.LS.Srch.Package";

	private static final String HOTEL_LOB_NAVIGATION = "Hotel";
	private static final String FLIGHT_LOB_NAVIGATION = "Flight";
	private static final String PACKAGE_LOB_NAVIGATION = "Package";
	private static final String RAIL_LOB_NAVIGATION = "Rail";
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
		case PACKAGES:
			lobString = PACKAGE_LOB_NAVIGATION;
			break;
		case RAILS:
			lobString = RAIL_LOB_NAVIGATION;
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
		default:
			throw new NotImplementedError("This LOB Navigation is not tracked in Omniture");
		}
		String link = LAUNCH_SCREEN_LOB_NAVIGATION + "." + lobString;

		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvar(12, LAUNCH_SEARCH + "." + lobString);
		s.setEvar(28, link);
		s.setProp(16, link);

		s.trackLink(null, "o", "App Landing", null, null);
	}

	public static void trackProWizardClick() {
		String link = LAUNCH_SCREEN_LOB_NAVIGATION;

		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvar(12, LAUNCH_SEARCH);
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
		addStandardFields(s);
		return s;
	}

	private static void addStandardFields(ADMS_Measurement s) {
		// Add debugging flag if not release
		if (BuildConfig.DEBUG || DebugUtils.isLogEnablerInstalled(sContext)) {
			s.setDebugLogging(true);
		}

		// Marketing date tracking
		s.setEvar(10, sMarketingDate);

		// Deep Link tracking
		addDeepLinkData(s);

		// Add the country locale
		s.setEvar(31, Locale.getDefault().getCountry());

		// Experience segmentation -- deliberately set to device type
		boolean isTabletDevice = AndroidUtils.isTablet(sContext);
		s.setEvar(50, (isTabletDevice) ? "app.tablet.android" : "app.phone.android");

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
		if (userStateManager.isUserAuthenticated()) {
			// Load the user into the Db if it has not been done (which will most likely be the case on app launch)
			User user = userStateManager.getUserSource().getUser();

			if (user != null && user.getPrimaryTraveler() != null) {
				email = user.getPrimaryTraveler().getEmail();
				expediaId = user.getExpediaUserId();
				rewardsStatus = getRewardsStatusString(user);
				tuid = user.getTuidString();
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
			s.setProp(11, hashEmail(email));
		}

		if (!TextUtils.isEmpty(expediaId)) {
			s.setProp(13, expediaId);
		}

		String evar55 = userStateManager.isUserAuthenticated() ? "loggedin | hard" : "unknown user";
		s.setEvar(55, evar55);

		s.setEvar(56, rewardsStatus);

		// TripBucket State
		if (Db.getTripBucket() != null) {
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

		String mc1Guid = DebugInfoUtils.getMC1CookieStr(sContext);
		if (mc1Guid != null) {
			s.setProp(23, mc1Guid.replace("GUID=", ""));
		}

		// Google Play Services Version
		int gpsVersion;
		try {
			gpsVersion = sContext.getPackageManager().getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0).versionCode;
		}
		catch (PackageManager.NameNotFoundException e) {
			gpsVersion = 0;
		}
		s.setProp(27, Integer.toString(gpsVersion));
	}

	private static void internalTrackPageLoadEventStandard(String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventBase(pageName).track();
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

	private static ADMS_Measurement createTrackCheckoutErrorPageLoadEventBase(String pageName, String lobPageName) {
		ADMS_Measurement s = getFreshTrackingObject();

		// set the pageName
		s.setAppState(pageName);
		s.setEvar(18, lobPageName);
		s.setEvents("event38");

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

	private static String getRewardsStatusString(User user) {
		UserLoyaltyMembershipInformation loyaltyInfo = user.getLoyaltyMembershipInformation();
		LoyaltyMembershipTier userTier = null;
		if (loyaltyInfo != null) {
			userTier = loyaltyInfo.getLoyaltyMembershipTier();
		}

		if (userTier != null) {
			String apiValue = userTier.toApiValue();
			if (apiValue != null) {
				return apiValue.toLowerCase(Locale.US);
			}
		}
		return null;
	}

	private static String getHotelConfirmationTripNumberString(HotelCheckoutResponse checkoutResponse) {
		String travelRecordLocator = checkoutResponse.checkoutResponse.bookingResponse.travelRecordLocator;
		String itinNumber = checkoutResponse.checkoutResponse.bookingResponse.itineraryNumber;
		return createConfirmationTripNumberString(travelRecordLocator, itinNumber);
	}

	private static String getFlightConfirmationTripNumberString(FlightCheckoutResponse checkoutResponse) {
		String travelRecordLocator = checkoutResponse.getNewTrip().getTravelRecordLocator();
		String itinNumber = checkoutResponse.getNewTrip().getItineraryNumber();
		return createConfirmationTripNumberString(travelRecordLocator, itinNumber);
	}

	private static String getFlightConfirmationTripNumberStringFromCreateTripResponse() {
		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		String travelRecordLocator = trip.getNewTrip().getTravelRecordLocator();
		String itinNumber = trip.getNewTrip().getItineraryNumber();
		return createConfirmationTripNumberString(travelRecordLocator, itinNumber);
	}

	private static String createConfirmationTripNumberString(String travelRecordLocator, String itinNumber) {
		if (Strings.isEmpty(travelRecordLocator)) {
			travelRecordLocator = "NA";
		}
		if (Strings.isEmpty(itinNumber)) {
			itinNumber = "NA";
		}
		return travelRecordLocator + "|" + itinNumber;
	}

	@VisibleForTesting
	protected static void addDeepLinkData(ADMS_Measurement s) {
		// Yes this logic is ugly (but is as desired by marketing).
		// See https://eiwork.mingle.thoughtworks.com/projects/eb_ad_app/cards/9353 for details

		if (!deepLinkArgs.isEmpty()) {

			String var = null;
			String deepLinkValue = null;

			// eVar22 items
			if ((deepLinkValue = deepLinkArgs.get("emlcid")) != null) {
				var = "EML." + deepLinkValue;
			}
			else if ((deepLinkValue = deepLinkArgs.get("semcid")) != null) {
				var = "SEM." + deepLinkValue;
			}
			else if ((deepLinkValue = deepLinkArgs.get("olacid")) != null) {
				var = "OLA." + deepLinkValue;
				if ((deepLinkValue = deepLinkArgs.get("oladtl")) != null) {
					var += "&OLADTL=" + deepLinkValue;
				}
			}
			else if ((deepLinkValue = deepLinkArgs.get("brandcid")) != null) {
				var = "Brand." + deepLinkValue;
			}
			else if ((deepLinkValue = deepLinkArgs.get("seocid")) != null) {
				var = "SEO." + deepLinkValue;
			}
			else if ((deepLinkValue = deepLinkArgs.get("mdpcid")) != null) {
				var = "MDP." + deepLinkValue;
				if ((deepLinkValue = deepLinkArgs.get("mdpdtl")) != null) {
					var += "&MDPDTL=" + deepLinkValue;
				}
			}
			else if ((deepLinkValue = deepLinkArgs.get("affcid")) != null) {
				var = "AFF." + deepLinkValue;
				if ((deepLinkValue = deepLinkArgs.get("afflid")) != null) {
					var += "&AFFLID=" + deepLinkValue;
				}
			}
			else if ((deepLinkValue = deepLinkArgs.get("icmcid")) != null) {
				var = "ICM." + deepLinkValue;
				if ((deepLinkValue = deepLinkArgs.get("icmdtl")) != null) {
					var += "&ICMDTL=" + deepLinkValue;
				}
			}

			if (var != null) {
				s.setEvar(22, var);
			}


			// kword eVar15
			if ((deepLinkValue = deepLinkArgs.get("kword")) != null) {
				s.setEvar(15, deepLinkValue);
			}

			// eVar26
			if ((deepLinkValue = deepLinkArgs.get("gclid")) != null) {
				s.setEvar(26, deepLinkValue);
			}

			// eVar36
			if ((deepLinkValue = deepLinkArgs.get("semdtl")) != null) {
				s.setEvar(36, deepLinkValue);
			}

			deepLinkArgs.clear();
		}
	}

	private static String hashEmail(String s) {
		try {
			// Create SHA256 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
			digest.update(s.getBytes());
			byte[] messageDigest = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte rawByte : messageDigest) {
				String hexByte = Integer.toHexString(0xFF & rawByte);
				if (hexByte.length() == 1) {
					hexByte = "0" + hexByte;
				}
				hexString.append(hexByte);
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

	private static Collection<Trip> getUsersTrips() {
		return ItineraryManager.getInstance().getTrips();
	}

	private static String getUsersTripComponentTypeEventString() {
		return TripUtils
			.createUsersTripComponentTypeEventString(getUsersTrips());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Car Tracking
	//
	// Spec: https://confluence/display/Omniture/Mobile+App%3A+Cars
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String CAR_WEBVIEW_RETRY = "App.Cars.WebView.Retry";
	private static final String CAR_WEBVIEW_LOGOUT = "App.Cars.WebView.Logout";
	private static final String CAR_WEBVIEW_CLOSE = "App.Cars.WebView.Close";
	private static final String CAR_WEBVIEW_BACK = "App.Cars.WebView.Back";
	private static final String CAR_WEBVIEW_SIGNIN = "App.Cars.WebView.SignIn";
	private static final String RAIL_WEBVIEW_RETRY = "App.Rails.WebView.Retry";
	private static final String RAIL_WEBVIEW_LOGOUT = "App.Rails.WebView.Logout";
	private static final String RAIL_WEBVIEW_CLOSE = "App.Rails.WebView.Close";
	private static final String RAIL_WEBVIEW_BACK = "App.Rails.WebView.Back";
	private static final String RAIL_WEBVIEW_SIGNIN = "App.Rails.WebView.SignIn";

	public static void trackAppCarWebViewRetry() {
		createAndtrackLinkEvent(CAR_WEBVIEW_RETRY, "Car Webview");
	}

	public static void trackAppCarWebViewBack() {
		createAndtrackLinkEvent(CAR_WEBVIEW_BACK, "Car Webview");
	}

	public static void trackAppCarWebViewSignIn() {
		createAndtrackLinkEvent(CAR_WEBVIEW_SIGNIN, "Car Webview");
	}

	public static void trackAppCarWebViewLogOut() {
		createAndtrackLinkEvent(CAR_WEBVIEW_LOGOUT, "Car Webview");
	}

	public static void trackAppCarWebViewClose() {
		createAndtrackLinkEvent(CAR_WEBVIEW_CLOSE, "Car Webview");
	}

	public static void trackAppCarFlexViewABTest() {
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppCarsFlexView);
		s.trackLink(null, "o", "Car Flexview", null, null);
	}

	public static void trackAppCarAATest() {
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppCarsAATest);
		s.trackLink(null, "o", "Car AA test", null, null);
	}

	public static void trackAppRailWebViewRetry() {
		createAndtrackLinkEvent(RAIL_WEBVIEW_RETRY, "Rail Webview");
	}

	public static void trackAppRailWebViewBack() {
		createAndtrackLinkEvent(RAIL_WEBVIEW_BACK, "Rail Webview");
	}

	public static void trackAppRailWebViewSignIn() {
		createAndtrackLinkEvent(RAIL_WEBVIEW_SIGNIN, "Rail Webview");
	}

	public static void trackAppRailWebViewLogOut() {
		createAndtrackLinkEvent(RAIL_WEBVIEW_LOGOUT, "Rail Webview");
	}

	public static void trackAppRailWebViewClose() {
		createAndtrackLinkEvent(RAIL_WEBVIEW_CLOSE, "Rail Webview");
	}

	public static void trackAppRailWebViewABTest() {
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidRailHybridAppForDEEnabled);
		s.trackLink(null, "o", "Rail Webview", null, null);
	}

	public static void trackCheckoutPayment(LineOfBusiness lineOfBusiness) {
		switch (lineOfBusiness) {
		case FLIGHTS_V2:
			trackPageLoadFlightCheckoutPaymentEditCard();
			break;
		case HOTELS:
			trackHotelV2PaymentEdit();
			break;
		case PACKAGES:
			trackPackagesPaymentEdit();
			break;
		case LX:
		case TRANSPORT:
			trackAppLXCheckoutPayment(lineOfBusiness);
			break;
		}
	}

	public static void trackCheckoutTraveler(LineOfBusiness lineOfBusiness) {
		if (lineOfBusiness.equals(LineOfBusiness.LX) || lineOfBusiness.equals(LineOfBusiness.TRANSPORT)) {
			trackAppLXCheckoutTraveler(lineOfBusiness);
		}
	}

	public static void trackFlightCheckoutTravelerEditInfo() {
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber);
		s.track();
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
		s.setEvar(53,
			ProductFlavorFeatureConfiguration.getInstance().getOmnitureEventValue(OmnitureEventName.NO_REWARDS_USED));
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

	public static void trackPinnedSearch() {
		ADMS_Measurement s = getFreshTrackingObject();
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelPinnedSearch);

		s.trackLink(null, "o", "Pinned Search Hit", null, null);
	}

	public static void trackUrgencyScore(int score) {
		String compressionVar = new StringBuilder(HOTEL_URGENCY_COMPRESSION_SCORE).append(score).toString();
		Log.d(TAG, "Tracking \"" + compressionVar);

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, compressionVar);
		s.setProp(16, compressionVar);
		s.trackLink(null, "o", "Compression Score", null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Packages tracking
	//
	// https://confluence/display/Omniture/Mobile+App%3A+Flight+and+Hotel+Package
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String PACKAGES_LOB = "package:FH";
	private static final String CHECKOUT_ERROR_PAGE_NAME = "App.Checkout.Error";
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
	private static final String PACKAGES_CHECKOUT_PAYMENT_SELECT = "App.Package.Checkout.Payment.Select";
	private static final String PACKAGES_CHECKOUT_PAYMENT_EDIT = "App.Package.Checkout.Payment.Edit.Card";
	private static final String PACKAGES_CHECKOUT_PAYMENT_SELECT_STORED_CC = "App.Package.CKO.Payment.StoredCard";
	private static final String PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION = "App.Package.Checkout.Confirmation";
	private static final String PACKAGES_ENTER_CARD = "App.Package.CKO.Payment.EnterManually";

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
	private static final String PACKAGES_HOTELS_DETAIL_GALLERY_CLICK = "App.Package.Hotels.IS.Gallery.Hotel";
	private static final String PACKAGES_HOTELS_DETAIL_ROOM_GALLERY_CLICK = "App.Package.Hotels.IS.Gallery.Room";

	private static final String PACKAGES_HOTELS_SEARCH_REFINE = "App.Package.Hotels.Search.Filter";
	private static final String PACKAGES_HOTELS_SORT_BY_TEMPLATE = "App.Package.Hotels.Search.Sort.";
	private static final String PACKAGES_HOTELS_FILTER_PRICE = "App.Package.Hotels.Search.Price";
	private static final String PACKAGES_HOTELS_FILTER_VIP_TEMPLATE = "App.Package.Hotels.Search.Filter.VIP.";
	private static final String PACKAGES_HOTELS_FILTER_NEIGHBOURHOOD = "App.Package.Hotels.Search.Neighborhood";
	private static final String PACKAGES_HOTELS_FILTER_BY_NAME = "App.Package.Hotels.Search.PackageName";
	private static final String PACKAGES_HOTELS_FILTER_CLEAR = "App.Package.Hotels.Search.ClearFilter";

	private static final String PACKAGES_BUNDLE_VIEW_OVERVIEW_LOAD = "App.Package.BundleView";
	private static final String PACKAGES_BUNDLE_OVERVIEW_LOAD = "App.Package.RateDetails";
	private static final String PACKAGES_BUNDLE_OVERVIEW_PRODUCT_EXPAND_TEMPLATE = "App.Package.RD.Details.";
	private static final String PACKAGES_BUNDLE_OVERVIEW_COST_BREAKDOWN = "App.Package.RD.TotalCost";
	private static final String PACKAGES_BUNDLE_EDIT = "App.Package.RD.Edit";

	private static final String PACKAGES_FLIGHT_BAGGAGE_FEE_CLICK = "App.Package.Flight.Search.BaggageFee";
	private static final String PACKAGES_FLIGHT_SORT_FILTER_LOAD = "App.Package.Flight.Search.Filter";
	private static final String PACKAGES_FLIGHT_SORTBY_TEMPLATE = "App.Package.Flight.Search.Sort.";
	private static final String PACKAGES_FLIGHT_FILTER_STOPS_TEMPLATE = "App.Package.Flight.Search.Filter.";
	private static final String PACKAGES_FLIGHT_AIRLINES = "App.Package.Flight.Search.Filter.Airline";

	private static final String PACKAGES_INFOSITE_ERROR = "App.Package.Infosite.Error";
	private static final String PACKAGES_SEARCH_ERROR = "App.Package.Hotels-Search.NoResults";
	private static final String PACKAGES_CHECKOUT_ERROR = "App.Package.Checkout.Error";
	private static final String PACKAGES_CHECKOUT_ERROR_RETRY = "App.Package.CKO.Error.Retry";

	private static final String PACKAGES_CHECKOUT_SELECT_TRAVELER = "App.Package.Checkout.Traveler.Select";
	private static final String PACKAGES_CHECKOUT_EDIT_TRAVELER = "App.Package.Checkout.Traveler.Edit.Info";
	private static final String PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE = "App.Package.Checkout.SlideToPurchase";
	private static final String PACKAGES_CHECKOUT_PAYMENT_CID = "App.Package.Checkout.Payment.CID";
	private static final String PACKAGES_CHECKOUT_PRICE_CHANGE = "App.Package.CKO.PriceChange";
	private static final String PACKAGES_BUNDLE_PRICE_CHANGE = "App.Package.RD.PriceChange";


	private static void addPackagesCommonFields(ADMS_Measurement s) {
		s.setProp(2, PACKAGES_LOB);
		s.setEvar(2, "D=c2");
		s.setProp(3, "pkg:" + Db.getPackageParams().getOrigin().hierarchyInfo.airport.airportCode);
		s.setEvar(3, "D=c3");
		s.setProp(4, "pkg:" + Db.getPackageParams().getDestination().hierarchyInfo.airport.airportCode + ":" + Db
			.getPackageParams().getDestination().gaiaId);
		s.setEvar(4, "D=c4");
		setDateValues(s, Db.getPackageParams().getStartDate(), Db.getPackageParams().getEndDate());
	}

	/**
	 * https://confluence/display/Omniture/Mobile+App%3A+Flight+and+Hotel+Package#MobileApp:FlightandHotelPackage-PackageCheckout:CheckoutStart
	 *
	 * @param packageDetails
	 */
	public static void trackPackagesCheckoutStart(PackageCreateTripResponse.PackageDetails packageDetails,
		String hotelSupplierType) {
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

	private static void setPackageProducts(ADMS_Measurement s, double productPrice, boolean addEvar63,
		String hotelSupplierType) {
		setPackageProducts(s, productPrice, addEvar63, false, hotelSupplierType);
	}

	private static void setPackageProducts(ADMS_Measurement s, double productPrice, boolean addEvarInventory,
		boolean isConfirmation, String hotelSupplierType) {
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
		String flightInventoryType =
			FlightV2Utils.isFlightMerchant(Db.getPackageSelectedOutboundFlight()) ? "Merchant" : "Agency";

		if (addEvarInventory) {
			String packageSupplierType =
				hotelSupplierType.toLowerCase(Locale.ENGLISH).equals(flightInventoryType.toLowerCase(Locale.ENGLISH))
					? flightInventoryType : "Mixed";
			productString.append(eVarNumber + "=" + packageSupplierType + ":PKG");
		}

		String eVar30DurationString = null;
		if (isConfirmation) {
			eVar30DurationString =
				":" + Db.getPackageParams().getStartDate().toString(EVAR30_DATE_FORMAT) + "-" + Db.getPackageParams()
					.getEndDate().toString(EVAR30_DATE_FORMAT);
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
			productString.append(
				":FLT:" + Db.getPackageParams().getOrigin().hierarchyInfo.airport.airportCode + "-" + Db
					.getPackageParams().getDestination().hierarchyInfo.airport.airportCode);
			productString.append(eVar30DurationString);
		}

		productString.append(",;");

		productString.append("Hotel:" + Db.getPackageSelectedHotel().hotelId + ";");
		String duration = "0";
		if (Db.getPackageParams().getEndDate() != null) {
			duration = Integer.toString(
				JodaUtils.daysBetween(Db.getPackageParams().getStartDate(), Db.getPackageParams().getEndDate()));
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

	private static ADMS_Measurement createTrackPackagePageLoadEventBase(String pageName, PageUsableData pageUsableData) {
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.setEvar(2, "D=c2");
		s.setProp(2, PACKAGES_LOB);
		addPageLoadTimeTrackingEvents(s, pageUsableData);
		return s;
	}

	private static void trackPackagePageLoadEventStandard(String pageName, PageUsableData pageUsableData) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPackagePageLoadEventBase(pageName, pageUsableData).track();
	}

	private static void trackPackagePageLoadEventStandard(String pageName, PageUsableData pageUsableData, List<ABTest> abTests) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(pageName, pageUsableData);
		for (ABTest testKey : abTests) {
			trackAbacusTest(s, testKey);
		}
		s.track();
	}

	public static void trackPackagesDestinationSearchInit(PageUsableData pageUsableData) {
		List<ABTest> abTests = new ArrayList<>();
		if (isMidAPIEnabled(sContext)) {
			abTests.add(AbacusUtils.EBAndroidAppPackagesMidApi);
		}
		if (isPackagesMISRealWorldGeoEnabled(sContext)) {
			abTests.add(AbacusUtils.EBAndroidAppPackagesMISRealWorldGeo);
		}
		trackPackagePageLoadEventStandard(PACKAGES_DESTINATION_SEARCH, pageUsableData, abTests);
	}

	public static void trackPackagesHSRMapInit() {
		trackPackagePageLoadEventStandard(PACKAGES_HOTEL_SEARCH_MAP_LOAD, null);
	}

	public static void trackPackagesHSRLoad(BundleSearchResponse response, PageUsableData pageUsableData) {
		ADMS_Measurement s = getFreshTrackingObject();

		if (response.getHotelResultsCount() > 0) {
			Log.d(TAG, "Tracking \"" + PACKAGES_HOTEL_SEARCH_RESULT_LOAD + "\"");
			s.setAppState(PACKAGES_HOTEL_SEARCH_RESULT_LOAD);
			s.setEvar(18, PACKAGES_HOTEL_SEARCH_RESULT_LOAD);
			addPackagesCommonFields(s);
			s.setEvents("event12,event53");
			s.setProp(1, String.valueOf(response.getHotelResultsCount()));

			if (response.hasSponsoredHotelListing()) {
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
			int children = getChildCount(Db.getPackageParams().getChildren());
			int infantInLap = getInfantInLap(Db.getPackageParams().getChildren(),
				Db.getPackageParams().getInfantSeatingInLap());
			int youth = getYouthCount(Db.getPackageParams().getChildren());
			int infantInseat = (Db.getPackageParams().getChildren().size() - (infantInLap + youth + children));

			StringBuilder evar47String = new StringBuilder("PKG|1R|RT|");
			evar47String.append("A" + Db.getPackageParams().getAdults() + "|");
			if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
				evar47String.append("C" + children + "|");
				evar47String.append("YTH" + youth + "|");
				evar47String.append("IL" + infantInLap + "|");
				evar47String.append("IS" + infantInseat);
			}
			else {
				evar47String.append("C" + Db.getPackageParams().getChildren().size() + "|");
				evar47String.append("L" + (Db.getPackageParams().getChildren().size() - Db.getPackageParams()
					.getNumberOfSeatedChildren()));
			}

			s.setEvar(47, evar47String.toString());

			// Freeform location
			if (!TextUtils.isEmpty(Db.getPackageParams().getDestination().regionNames.fullName)) {
				s.setEvar(48, Db.getPackageParams().getDestination().regionNames.fullName);
			}
			addPageLoadTimeTrackingEvents(s, pageUsableData);
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

	private static void createAndtrackLinkEvent(String link, String linkName) {
		Log.d(TAG, "Tracking \"" + link + "\" click...");
		ADMS_Measurement s = createTrackLinkEvent(link);
		s.trackLink(null, "o", linkName, null, null);
	}

	private static void trackPackagesHotelMapLinkEvent(String link) {
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

	public static void trackPackagesHotelMapSearchThisAreaClick() {
		trackPackagesHotelMapLinkEvent(PACKAGES_HOTEL_MAP_SEARCH_AREA);
	}

	public static void trackPackagesPaymentSelect() {
		trackPackagePageLoadEventStandard(PACKAGES_CHECKOUT_PAYMENT_SELECT, null);
	}

	public static void trackPackagesPaymentEdit() {
		trackPackagePageLoadEventStandard(PACKAGES_CHECKOUT_PAYMENT_EDIT, null);
	}

	public static void trackPackagesPaymentStoredCCSelect() {
		createAndtrackLinkEvent(PACKAGES_CHECKOUT_PAYMENT_SELECT_STORED_CC, "Package Checkout");
	}

	public static void trackPackagesConfirmation(PackageCheckoutResponse response, String hotelSupplierType,
		PageUsableData pageUsableData) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION + "\" pageLoad");
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(PACKAGES_CHECKOUT_PAYMENT_CONFIRMATION, null);
		setPackageProducts(s, response.getTotalChargesPrice().amount.doubleValue(), true, true, hotelSupplierType);
		s.setCurrencyCode(response.getTotalChargesPrice().currencyCode);
		s.setEvents("purchase");
		s.setPurchaseID("onum" + response.getOrderId());
		if (response.getNewTrip().getTravelRecordLocator() != null) {
			s.setProp(71, response.getNewTrip().getTravelRecordLocator());
		}
		s.setProp(72, response.getOrderId());
		addPageLoadTimeTrackingEvents(s, pageUsableData);
		s.track();
	}

	private static void trackPackagesPageLoadWithDPageName(String pageName, PageUsableData pageUsableData) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(pageName, null);
		s.setEvar(18, "D=pageName");
		if (pageUsableData != null) {
			addPageLoadTimeTrackingEvents(s, pageUsableData);
		}
		s.track();
	}

	public static void trackPackagesFlightRoundTripOutLoad(PageUsableData pageUsableData) {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_OUT_RESULTS, pageUsableData);
	}

	public static void trackPackagesFlightRoundTripOutDetailsLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_OUT_DETAILS, null);
	}

	public static void trackPackagesFlightRoundTripInLoad(PageUsableData pageUsableData) {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_IN_RESULTS, pageUsableData);
	}

	public static void trackPackagesFlightRoundTripInDetailsLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_RT_IN_DETAILS, null);
	}

	public static void trackPackagesHotelInfoLoad(String hotelId, PageUsableData pageUsableData) {
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(PACKAGES_HOTEL_DETAILS_LOAD, null);
		s.setEvents("event3");
		addPageLoadTimeTrackingEvents(s, pageUsableData);
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

	public static void trackPackageHotelDetailGalleryClick() {
		Log.d(TAG, "Tracking \"" + PACKAGES_HOTELS_DETAIL_GALLERY_CLICK + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(PACKAGES_HOTELS_DETAIL_GALLERY_CLICK);

		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		s.trackLink(null, "o", "Gallery View", null, null);
	}

	public static void trackPackageHotelDetailRoomGalleryClick() {
		Log.d(TAG, "Tracking \"" + PACKAGES_HOTELS_DETAIL_ROOM_GALLERY_CLICK + "\" click...");

		ADMS_Measurement s = createTrackLinkEvent(PACKAGES_HOTELS_DETAIL_ROOM_GALLERY_CLICK);

		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		s.trackLink(null, "o", "Gallery View", null, null);
	}

	public static void trackPackagesHotelReviewPageLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_REVIEWS, null);
	}

	public static void trackPackagesHotelReviewCategoryChange(String category) {
		String link = PACKAGES_HOTEL_DETAILS_REVIEWS_CATEGORY_TEMPLATE + category;
		createAndtrackLinkEvent(link, "Package Reviews");
	}

	public static void trackPackagesHotelResortFeeInfo() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_RESORT_FEE_INFO, null);
	}

	public static void trackPackagesHotelRenovationInfo() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_RENOVATION_INFO, null);
	}

	public static void trackPackagesViewBundleLoad(boolean isFirstBundleLaunch) {
		Log.d(TAG, "Tracking \"" + PACKAGES_BUNDLE_VIEW_OVERVIEW_LOAD + "\" pageLoad");
		ADMS_Measurement s = createTrackPackagePageLoadEventBase(PACKAGES_BUNDLE_VIEW_OVERVIEW_LOAD, null);
		if (isFirstBundleLaunch) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav);
		}
		s.track();
	}

	public static void trackPackagesBundlePageLoad(PackageCreateTripResponse.PackageDetails packageDetails, PageUsableData pageUsableData) {
		Log.d(TAG, "Tracking \"" + PACKAGES_BUNDLE_OVERVIEW_LOAD + "\"");

		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_BUNDLE_OVERVIEW_LOAD);
		addPackagesCommonFields(s);
		setPackageProducts(s, packageDetails.pricing.packageTotal.amount.doubleValue());
		s.setEvents("event4");
		addPageLoadTimeTrackingEvents(s, pageUsableData);
		s.track();
	}

	public static void trackPackagesBundleProductExpandClick(String lobClicked, boolean isExpanding) {
		StringBuilder link = new StringBuilder(PACKAGES_BUNDLE_OVERVIEW_PRODUCT_EXPAND_TEMPLATE);
		link.append(lobClicked);
		link.append(isExpanding ? ".Expand" : ".Collapse");
		createAndtrackLinkEvent(link.toString(), "Rate Details");
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
		trackPackagesPageLoadWithDPageName(PACKAGES_FLIGHT_SORT_FILTER_LOAD, null);
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
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTEL_DETAILS_MAP, null);
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

	public static void trackPackagesInfositeError(String errorType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_INFOSITE_ERROR + "\" pageLoad...");
		ADMS_Measurement s = createTrackPageLoadEventBase(PACKAGES_INFOSITE_ERROR);
		s.setProp(36, errorType);
		s.track();
	}

	public static void trackPackagesCheckoutError(String errorType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_ERROR + "\" pageLoad...");
		ADMS_Measurement s = createTrackCheckoutErrorPageLoadEventBase(CHECKOUT_ERROR_PAGE_NAME,
			PACKAGES_CHECKOUT_ERROR);
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

	public static void trackPackagesCheckoutShowSlideToPurchase(String flexStatus, String cardType) {
		Log.d(TAG, "Tracking \"" + PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE + "\" load...");
		trackShowSlidetoPurchase(PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE, cardType, flexStatus);
	}

	public static void trackPackagesCheckoutPaymentCID() {
		createTrackPageLoadEventBase(PACKAGES_CHECKOUT_PAYMENT_CID).track();
	}

	public static void trackPackagesCreateTripPriceChange(int priceDiff) {
		ADMS_Measurement s = getFreshTrackingObject();
		trackPriceChange(s, priceDiff, PACKAGES_BUNDLE_PRICE_CHANGE, "PKG|", "Rate Details View");
	}

	public static void trackPackagesCheckoutPriceChange(int priceDiff) {
		ADMS_Measurement s = getFreshTrackingObject();
		trackPriceChange(s, priceDiff, PACKAGES_CHECKOUT_PRICE_CHANGE, "PKG|", "Package Checkout");
	}

	public static void trackPackagesHotelFilterPageLoad() {
		trackPackagesPageLoadWithDPageName(PACKAGES_HOTELS_SEARCH_REFINE, null);
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

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights V2
	//
	// https://confluence/display/Omniture/Mobile+App%3A+Flights+Material+Redesign
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String FLIGHTS_V2_SEARCH_ONEWAY = "App.Flight.Search.Oneway";
	private static final String FLIGHT_INSURANCE_ACTION_TEMPLATE = "App.Flight.CKO.INS.";
	private static final String FLIGHT_INSURANCE_BENEFITS_VIEW = "App.Flight.CKO.INS.Reasons";
	private static final String FLIGHT_INSURANCE_ERROR = "App.Flight.CKO.INS.Error";
	private static final String FLIGHT_INSURANCE_TERMS_VIEW = "App.Flight.CKO.INS.Terms";
	private static final String FLIGHT_SEARCH_V2 = "App.Flight.Dest-Search";
	private static final String FLIGHT_LOB = "App.Flight.";
	private static final String FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK = "App.Flight.Search.BaggageFee";
	private static final String FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK = "App.Flight.Search.PaymentFee";
	private static final String FLIGHTS_V2_SEARCH_FORM_INTERACTED = "App.Flight.DS.Form.Interacted";
	private static final String FLIGHTS_V2_SEARCH_FORM_CHANGE_PREFIX = "App.Flight.DS.";
	private static final String FLIGHTS_V2_TRAVELER_LINK_NAME = "Search Results Update";
	private static final String FLIGHTS_V2_CROSS_SELL_PACKAGE_LINK_NAME = "Package Xsell Banner";
	private static final String FLIGHTS_V2_SORTBY_TEMPLATE = "App.Flight.Search.Sort.";
	private static final String FLIGHTS_V2_FILTER_STOPS_TEMPLATE = "App.Flight.Search.Filter.";
	private static final String FLIGHTS_V2_FLIGHT_FILTER_ZERO_RESULTS = "App.Flight.Search.Filter.ZeroResult";
	private static final String FLIGHTS_V2_FLIGHT_FILTER_DURATION = "App.Flight.Search.Filter.Duration";
	private static final String FLIGHTS_V2_FLIGHT_FILTER_TIME = "App.Flight.Search.Filter.Time";
	private static final String FLIGHTS_V2_FLIGHT_AIRLINES = "App.Flight.Search.Filter.Airline";
	private static final String FLIGHTS_V2_RATE_DETAILS = "App.Flight.RateDetails";
	private static final String FLIGHTS_V2_DETAILS_EXPAND = "App.Flight.RD.Details.";
	private static final String FLIGHTS_V2_FARE_FAMILY_UPGRADE_FLIGHT = "App.Flight.RD.UpgradeFlights";
	private static final String FLIGHTS_V2_FARE_FAMILY_CHANGE_CLASS = "App.Flight.RD.ChangeClass";
	private static final String FLIGHTS_V2_COST_SUMMARY = "App.Flight.RD.TotalCost";
	private static final String FLIGHTS_V2_RATE_DETAILS_PRICE_CHANGE = "App.Flight.RD.PriceChange";
	private static final String FLIGHTS_V2_CHECKOUT_PRICE_CHANGE = "App.Flight.CKO.PriceChange";
	private static final String FLIGHTS_V2_SELECT_TRAVELER = "App.Flight.CKO.Traveler.Select.Existing";
	private static final String FLIGHTS_V2_SELECT_CARD = "App.Flight.CKO.Payment.Select.Existing";
	private static final String FLIGHTS_V2_ENTER_CARD = "App.Flight.CKO.Payment.EnterManually";
	private static final String FLIGHTS_V2_CHECKOUT_PAYMENT_SELECT = "App.Flight.Checkout.Payment.Select";
	private static final String FLIGHTS_V2_PAYMENT_CID = "App.Flight.Checkout.Payment.CID";
	private static final String FLIGHTS_V2_ERROR = "App.Flight.Error";
	private static final String FLIGHTS_V2_CHECKOUT_ERROR = "App.Flight.CKO.Error";
	private static final String FLIGHTS_V2_ITIN_SHARE_CLICK = "App.Flight.CKO.Share.Start";
	private static final String FLIGHTS_V2_SHARE = "App.Flight.CKO.Share";
	private static final String FLIGHTS_V2_SWITCH_TO_FROM = "App.Flight.DS.SwitchFields.Clicked";
	private static final String FLIGHTS_V2_KRAZY_GLUE_PAGE_NAME = "App.Kg.expedia.conf";
	private static final String FLIGHTS_V2_KRAZY_GLUE_WEB_TRACKING_LINK = "mip.hot.kg.expedia.conf";
	private static final String FLIGHTS_V2_KRAZY_GLUE_CLICK_LINK = "Krazyglue Click";
	private static final String FLIGHTS_V2_KRAZY_GLUE_HOTEL_CLICKED = "mip.hot.app.kg.flight.conf.HSR.tile";
	private static final String FLIGHTS_V2_KRAZY_GLUE_SEE_MORE_CLICKED = "mip.hot.app.kg.flight.conf.HSR.see_more";

	private static Pair<com.expedia.bookings.data.flights.FlightLeg,
		com.expedia.bookings.data.flights.FlightLeg> getFirstAndLastFlightLegs() {

		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		return new Pair<>(trip.getDetails().legs.get(0),
			(trip.getDetails().legs.size() > 1) ? trip.getDetails().legs.get(trip.getDetails().legs.size() - 1) : null);
	}

	private static Pair<FlightSegment, FlightSegment> getFirstAndLastFlightSegments() {
		Pair<com.expedia.bookings.data.flights.FlightLeg, com.expedia.bookings.data.flights.FlightLeg> legs =
			getFirstAndLastFlightLegs();

		return new Pair<>(legs.first.segments.get(0),
			(legs.second != null) ? legs.second.segments.get(legs.second.segments.size() - 1) : null);
	}

	private static String getFlightInsuranceProductStringOnCheckout() {
		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		List<InsuranceProduct> insuranceProducts = trip.getAvailableInsuranceProducts();
		if (!insuranceProducts.isEmpty()) {
			InsuranceProduct insuranceProduct = insuranceProducts.get(0);
			return String.format(Locale.ENGLISH, ",;Insurance:%s;%s;%.2f%s",
				insuranceProduct.typeId, trip.getDetails().offer.numberOfTickets,
				insuranceProduct.totalPrice.amount, ";;eVar63=Merchant:SA");
		}
		else {
			return "";
		}
	}

	private static String getFlightInsuranceProductStringOnConfirmation() {
		com.expedia.bookings.data.flights.FlightCheckoutResponse trip = Db.getTripBucket()
			.getFlightV2().flightCheckoutResponse;
		InsuranceProduct selectedInsuranceProduct = trip.getSelectedInsuranceProduct();
		if (selectedInsuranceProduct != null) {
			return String.format(Locale.ENGLISH, ",;Insurance:%s;%s;%.2f",
				selectedInsuranceProduct.typeId, trip.getFirstFlightTripDetails().offer.numberOfTickets,
				selectedInsuranceProduct.totalPrice.amount);
		}
		else {
			return "";
		}
	}

	private static String getFlightInsuranceProductStringFromItinResponse(FlightItinDetailsResponse response) {
		com.expedia.bookings.data.flights.FlightCreateTripResponse trip = Db.getTripBucket()
			.getFlightV2().flightCreateTripResponse;
		List<FlightItinDetailsResponse.FlightResponseData.Insurance> insuranceList = response.getResponseData().getInsurance();
		if (insuranceList != null && !insuranceList.isEmpty()) {
			FlightItinDetailsResponse.FlightResponseData.Insurance insurance = insuranceList.get(0);
			return String.format(Locale.ENGLISH, ",;Insurance:%s;%s;%.2f",
				insurance.getInsuranceTypeId(), trip.getDetails().offer.numberOfTickets,
				insurance.price.total);
		}
		else {
			return "";
		}
	}

	private static String getFlightInventoryTypeString() {
		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		return Arrays.asList("CHARTER", "CHARTER_NET", "LOW_COST_CARRIER", "PSEUDO_PUBLISHED", "PUBLISHED")
			.contains(trip.getDetails().offer.fareType) ? "Agency" : "Merchant";
	}

	private static FlightItineraryType getFlightItineraryType() {
		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		List<com.expedia.bookings.data.flights.FlightLeg> legs = trip.getDetails().legs;
		Pair<FlightSegment, FlightSegment> segments = getFirstAndLastFlightSegments();

		if (trip.getDetails().offer.isSplitTicket) {
			return FlightItineraryType.SPLIT_TICKET;
		}
		else if (legs.size() == 1) {
			return FlightItineraryType.ONE_WAY;
		}
		else if (legs.size() == 2 && segments.first.departureAirportCode.equals(segments.second.arrivalAirportCode)) {
			return FlightItineraryType.ROUND_TRIP;
		}
		else {
			return FlightItineraryType.MULTI_DESTINATION;
		}
	}

	private static String getFlightSubpubProductString(String products) {
		StringBuilder subpubStringBuilder = new StringBuilder(products);
		subpubStringBuilder.append(";eVar59=FLT:");
		subpubStringBuilder.append(getFlightItineraryTypeCode());
		return subpubStringBuilder.append(":SubPub").toString();
	}

	private static String getFlightItineraryTypeCode() {
		switch (getFlightItineraryType()) {
		case SPLIT_TICKET:
			return "ST";
		case ONE_WAY:
			return "OW";
		case ROUND_TRIP:
			return "RT";
		default:
			return "MD";
		}
	}

	private static String getFlightProductString(boolean isConfirmation) {
		Pair<FlightSegment, FlightSegment> segments = getFirstAndLastFlightSegments();
		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;

		String itineraryType = getFlightItineraryTypeCode();
		BigDecimal outBoundFlightPrice = trip.getDetails().offer.totalPrice.amount;
		if (itineraryType.equalsIgnoreCase("ST")) {
			outBoundFlightPrice = trip.getDetails().offer.splitFarePrice.get(0).totalPrice.amount;
		}

		String evarValuesOutBound, evarValuesInBound = "";
		if (isConfirmation) {
			if (!itineraryType.equalsIgnoreCase("ST")) {
				evarValuesOutBound = String.format(Locale.ENGLISH, "eVar30=%s:FLT", getFlightInventoryTypeString());
			}
			else {
				Pair<String, String> airportCodes = getFlightSearchDepartureAndArrivalAirportCodes();
				Pair<String, String> takeoffDateStrings = getFlightSearchDepartureAndReturnDateStrings();
				String departureInfo = airportCodes.first + "-" + airportCodes.second + ":" + takeoffDateStrings.first;
				evarValuesOutBound = String
					.format(Locale.ENGLISH, "eVar30=%s:FLT:%s", getFlightInventoryTypeString(), departureInfo);

				String arrivalInfo = airportCodes.second + "-" + airportCodes.first + ":" + takeoffDateStrings.second;
				evarValuesInBound = String
					.format(Locale.ENGLISH, "eVar30=%s:FLT:%s", getFlightInventoryTypeString(), arrivalInfo);
			}
		}
		else {
			evarValuesOutBound = String.format(Locale.US, "eVar63=%s:SA", getFlightInventoryTypeString());
			evarValuesInBound = evarValuesOutBound;
		}

		String outBoundFlight = String.format(Locale.ENGLISH, ";Flight:%s:%s;%s;%.2f;;%s", segments.first.airlineCode,
			itineraryType, trip.getDetails().offer.numberOfTickets, outBoundFlightPrice, evarValuesOutBound);

		if (itineraryType.equalsIgnoreCase("ST")) {
			BigDecimal inBoundFlightPrice = trip.getDetails().offer.splitFarePrice.get(1).totalPrice.amount;
			String inBoundFlight = String
				.format(Locale.ENGLISH, ";Flight:%s:%s;%s;%.2f;;%s", segments.second.airlineCode,
					itineraryType, trip.getDetails().offer.numberOfTickets, inBoundFlightPrice, evarValuesInBound);

			return outBoundFlight + "," + inBoundFlight;

		}
		return outBoundFlight;
	}

	private static Pair<String, String> getFlightSearchDepartureAndArrivalAirportCodes() {
		com.expedia.bookings.data.flights.FlightSearchParams search = Db.getFlightSearchParams();

		String departureAirportCode = "nil";
		if ((search.getDepartureAirport().hierarchyInfo != null) &&
			(search.getDepartureAirport().hierarchyInfo.airport != null)) {
			departureAirportCode = search.getDepartureAirport().hierarchyInfo.airport.airportCode;
		}
		String arrivalAirportCode = "nil";
		if ((search.getArrivalAirport() != null) && (search.getArrivalAirport().hierarchyInfo != null) &&
			(search.getArrivalAirport().hierarchyInfo.airport != null)) {
			arrivalAirportCode = search.getArrivalAirport().hierarchyInfo.airport.airportCode;
		}

		return new Pair<>(departureAirportCode, arrivalAirportCode);
	}

	private static Pair<LocalDate, LocalDate> getFlightSearchDepartureAndReturnDates() {
		com.expedia.bookings.data.flights.FlightSearchParams search = Db.getFlightSearchParams();
		return new Pair<>(search.getDepartureDate(), search.getReturnDate());
	}

	private static Pair<String, String> getFlightSearchDepartureAndReturnDateStrings() {
		DateTimeFormatter formatter = ISODateTimeFormat.basicDate();
		Pair<LocalDate, LocalDate> takeoffDates = getFlightSearchDepartureAndReturnDates();

		return new Pair<>(takeoffDates.first.toString(formatter), (takeoffDates.second != null) ?
			takeoffDates.second.toString(formatter) : "nil");
	}

	public static void trackFlightBaggageFeesClick() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK);
		s.setProp(16, FLIGHTS_V2_FLIGHT_BAGGAGE_FEE_CLICK);
		s.trackLink(null, "o", "Flight Baggage Fee", null, null);
	}

	public static void trackFlightSearchFormInteracted() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_SEARCH_FORM_INTERACTED + "\" interaction...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_SEARCH_FORM_INTERACTED);
		s.setProp(16, FLIGHTS_V2_SEARCH_FORM_INTERACTED);
		s.trackLink(null, "o", "Form Interaction", null, null);
	}

	public static void trackFlightCheckoutConfirmationPageLoad(PageUsableData pageUsableData) {
		String pageName = FLIGHT_CHECKOUT_CONFIRMATION;
		Log.d(TAG, "Tracking \"" + pageName + "\" page load...");

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		// events
		s.setEvents("purchase");
		boolean isSplitTicket = getFlightItineraryType().equals(FlightItineraryType.SPLIT_TICKET);

		// products
		Pair<String, String> airportCodes = getFlightSearchDepartureAndArrivalAirportCodes();
		Pair<String, String> takeoffDateStrings = getFlightSearchDepartureAndReturnDateStrings();
		String products;
		if (!isSplitTicket) {
			if (takeoffDateStrings.second != null) {
				products = String.format(Locale.ENGLISH, "%s:%s-%s:%s-%s%s", getFlightProductString(true),
					airportCodes.first, airportCodes.second, takeoffDateStrings.first,
					takeoffDateStrings.second, getFlightInsuranceProductStringOnConfirmation());
			}
			else {
				products = String.format(Locale.ENGLISH, "%s:%s-%s:%s%s", getFlightProductString(true),
					airportCodes.first, airportCodes.second, takeoffDateStrings.first,
					getFlightInsuranceProductStringOnConfirmation());
			}
		}
		else {
			products = getFlightProductString(true) + getFlightInsuranceProductStringOnConfirmation();
		}
		s.setProducts(products);
		// miscellaneous variables
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		s.setEvar(3, "D=c3");
		s.setProp(3, airportCodes.first);
		s.setEvar(4, "D=c4");
		s.setProp(4, airportCodes.second);
		s.setEvar(18, pageName);

		// date variables 5, 6
		Pair<LocalDate, LocalDate> takeoffDates = getFlightSearchDepartureAndReturnDates();
		setDateValues(s, takeoffDates.first, takeoffDates.second);

		// checkout variables
		com.expedia.bookings.data.flights.FlightCheckoutResponse checkoutResponse = Db.getTripBucket().getFlightV2()
			.flightCheckoutResponse;
		s.setCurrencyCode(checkoutResponse.getCurrencyCode());
		s.setProp(71, checkoutResponse.getNewTrip().getTravelRecordLocator());
		s.setProp(72, checkoutResponse.getOrderId());
		s.setProp(8, getFlightConfirmationTripNumberString(checkoutResponse));
		s.setPurchaseID("onum" + checkoutResponse.getOrderId());
		addPageLoadTimeTrackingEvents(s, pageUsableData);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsKrazyglue);

		s.track();
	}

	public static void trackWebFlightCheckoutConfirmation(FlightItinDetailsResponse itinDetailsResponse, PageUsableData pageUsableData) {
		String pageName = FLIGHT_CHECKOUT_CONFIRMATION;
		Log.d(TAG, "Tracking \"" + pageName + "\" page load...");

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		// events
		s.setEvents("purchase");
		boolean isSplitTicket = getFlightItineraryType().equals(FlightItineraryType.SPLIT_TICKET);

		// products
		Pair<String, String> airportCodes = getFlightSearchDepartureAndArrivalAirportCodes();
		Pair<String, String> takeoffDateStrings = getFlightSearchDepartureAndReturnDateStrings();
		String products;
		if (!isSplitTicket) {
			if (takeoffDateStrings.second != null) {
				products = String.format(Locale.ENGLISH, "%s:%s-%s:%s-%s%s", getFlightProductString(true),
					airportCodes.first, airportCodes.second, takeoffDateStrings.first,
					takeoffDateStrings.second, getFlightInsuranceProductStringFromItinResponse(itinDetailsResponse));
			}
			else {
				products = String.format(Locale.ENGLISH, "%s:%s-%s:%s%s", getFlightProductString(true),
					airportCodes.first, airportCodes.second, takeoffDateStrings.first,
					getFlightInsuranceProductStringFromItinResponse(itinDetailsResponse));
			}
		}
		else {
			products = getFlightProductString(true) + getFlightInsuranceProductStringFromItinResponse(itinDetailsResponse);
		}
		s.setProducts(products);
		// miscellaneous variables
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		s.setEvar(3, "D=c3");
		s.setProp(3, airportCodes.first);
		s.setEvar(4, "D=c4");
		s.setProp(4, airportCodes.second);
		s.setEvar(18, pageName);

		// date variables 5, 6
		Pair<LocalDate, LocalDate> takeoffDates = getFlightSearchDepartureAndReturnDates();
		setDateValues(s, takeoffDates.first, takeoffDates.second);

		FlightCreateTripResponse trip = Db.getTripBucket().getFlightV2().flightCreateTripResponse;
		String orderId = itinDetailsResponse.getResponseDataForItin().getOrderNumber().toString();
		s.setCurrencyCode(trip.totalPrice.currencyCode);
		s.setProp(71, trip.getNewTrip().getTravelRecordLocator());
		s.setProp(72, orderId);
		s.setProp(8, getFlightConfirmationTripNumberStringFromCreateTripResponse());
		s.setPurchaseID("onum" + orderId);
		addPageLoadTimeTrackingEvents(s, pageUsableData);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsKrazyglue);

		s.track();
	}

	public static void trackFlightsKrazyglueClick(int position) {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_KRAZY_GLUE_CLICK_LINK + "\" interaction...");
		final String rfrrString = FLIGHTS_V2_KRAZY_GLUE_HOTEL_CLICKED + position;
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, rfrrString);
		s.setProp(16, rfrrString);
		s.setEvar(65, Constants.KRAZY_GLUE_PARTNER_ID);
		s.setEvents("event83");

		s.trackLink(null, "o", FLIGHTS_V2_KRAZY_GLUE_CLICK_LINK, null, null);
	}

	public static void trackFlightsKrazyGlueSeeMoreClick() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_KRAZY_GLUE_CLICK_LINK + "\" interaction...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_KRAZY_GLUE_SEE_MORE_CLICKED);
		s.setProp(16, FLIGHTS_V2_KRAZY_GLUE_SEE_MORE_CLICKED);
		s.setEvar(65, Constants.KRAZY_GLUE_PARTNER_ID);
		s.setEvents("event83");

		s.trackLink(null, "o", FLIGHTS_V2_KRAZY_GLUE_CLICK_LINK, null, null);
	}

	public static void trackFlightsKrazyglueExposure(List<KrazyglueResponse.KrazyglueHotel> krazyGlueHotels) {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(FLIGHTS_V2_KRAZY_GLUE_PAGE_NAME);
		s.setEvar(28, FLIGHTS_V2_KRAZY_GLUE_WEB_TRACKING_LINK);
		s.setProp(16, FLIGHTS_V2_KRAZY_GLUE_WEB_TRACKING_LINK);
		s.setEvar(43, FLIGHTS_V2_KRAZY_GLUE_WEB_TRACKING_LINK);
		s.setEvar(65, Constants.KRAZY_GLUE_PARTNER_ID);
		s.setEvar(2, "D=c2");
		s.setProp(2, "krazyglue");
		s.setEvar(4, "D=c4");
		Pair<String, String> airportCodes = getFlightSearchDepartureAndArrivalAirportCodes();
		s.setProp(4, airportCodes.second);
		String krazyGlueProductString = "";
		for (int i = 0; i < krazyGlueHotels.size(); i++) {
			KrazyglueResponse.KrazyglueHotel hotel = krazyGlueHotels.get(i);
			krazyGlueProductString += ("Hotel:" + hotel.getHotelId() + ";;");
			if (i < krazyGlueHotels.size() - 1) {
				krazyGlueProductString += ",";
			}
		}
		s.setProducts(krazyGlueProductString);
		s.track();
	}

	public static void trackFlightConfirmationShareItinClicked() {
		createAndtrackLinkEvent(FLIGHTS_V2_ITIN_SHARE_CLICK, "Itinerary Sharing");
	}

	public static void trackFlightConfirmationShareAppChosen(String tripType, String shareApp) {
		String pageName = FLIGHTS_V2_SHARE + "." + shareApp;

		ADMS_Measurement s = createTrackLinkEvent(pageName);
		s.setEvar(2, tripType);
		s.setEvents("event48");

		internalTrackLink(s);
	}

	public static void trackFlightCheckoutInfoPageLoad(FlightCreateTripResponse tripResponse) {
		String pageName = FLIGHT_CHECKOUT_INFO;
		Log.d(TAG, "Tracking \"" + pageName + "\" page load...");

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		// events
		s.setEvents("event36, event71" /* checkout start, flight checkout start */ +
			(tripResponse.getAvailableInsuranceProducts().isEmpty() ? "" : ", event122" /* insurance present */));

		String products = getFlightProductString(false) + getFlightInsuranceProductStringOnCheckout();

		// products
		s.setProducts(products);
		// miscellaneous variables
		Pair<String, String> airportCodes = getFlightSearchDepartureAndArrivalAirportCodes();
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		s.setEvar(3, "D=c3");
		s.setProp(3, airportCodes.first);
		s.setEvar(4, "D=c4");
		s.setProp(4, airportCodes.second);
		s.setEvar(18, pageName);

		// date variables 5, 6
		Pair<LocalDate, LocalDate> takeoffDates = getFlightSearchDepartureAndReturnDates();
		setDateValues(s, takeoffDates.first, takeoffDates.second);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppSecureCheckoutIcon);
		s.track();
	}

	public static void trackFlightInsuranceAdd(String action) {
		createAndtrackLinkEvent(FLIGHT_INSURANCE_ACTION_TEMPLATE + action, "Flight Checkout");
	}

	public static void trackFlightInsuranceBenefitsClick() {
		createAndtrackLinkEvent(FLIGHT_INSURANCE_BENEFITS_VIEW, "Flight Checkout");
	}

	public static void trackFlightInsuranceError(String message) {
		ADMS_Measurement s = createTrackLinkEvent(FLIGHT_INSURANCE_ERROR);
		s.setProp(36, "ins:" + message);
		s.trackLink(null, "o", "Flight Checkout", null, null);
	}

	public static void trackFlightInsuranceTermsClick() {
		createAndtrackLinkEvent(FLIGHT_INSURANCE_TERMS_VIEW, "Flight Checkout");
	}

	public static void trackFlightPaymentFeesClick() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK + "\" click...");

		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK);
		s.setProp(16, FLIGHTS_V2_FLIGHT_PAYMENT_FEE_CLICK);
		s.trackLink(null, "o", "", null, null);
	}

	public static void trackFlightTravelerPickerClick(String actionLabel) {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_SEARCH_FORM_CHANGE_PREFIX + actionLabel);
		s.setProp(16, FLIGHTS_V2_SEARCH_FORM_CHANGE_PREFIX + actionLabel);
		s.trackLink(null, "o", FLIGHTS_V2_TRAVELER_LINK_NAME, null, null);
	}

	public static void trackFlightSearchButtonClick() {
		StringBuilder link = new StringBuilder(FLIGHTS_V2_SEARCH_FORM_CHANGE_PREFIX);
		link.append("Search.Clicked");
		createAndtrackLinkEvent(link.toString(), "Search Button Clicked");
	}

	public static void trackFlightOpenBrowserButtonClick(boolean openBrowser) {
		StringBuilder link = new StringBuilder(FLIGHT_LOB);
		if (openBrowser) {
			link.append("OpenBrowser");
		}
		else {
			link.append("Popupclose");
		}
		createAndtrackLinkEvent(link.toString(), "Flight Lob Clicked");
	}

	public static void trackFlightAdvanceSearchFiltersClick(String filterLabel, boolean isSelected) {
		StringBuilder link = new StringBuilder(FLIGHTS_V2_SEARCH_FORM_CHANGE_PREFIX);
		link.append(filterLabel);
		link.append(isSelected ? ".Select" : ".Deselect");
		createAndtrackLinkEvent(link.toString(), FLIGHTS_V2_TRAVELER_LINK_NAME);
	}

	public static void trackFlightLocationSwapViewClicked() {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_SWITCH_TO_FROM + "\" click...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_SWITCH_TO_FROM);
		s.setProp(16, FLIGHTS_V2_SWITCH_TO_FROM);
		s.trackLink(null, "o", "Switched to-from fields", null, null);
	}

	public static void trackFlightSRPScrollDepth(int scrollDepth, boolean isOutboundFlight, boolean isRoundTrip,
		int totalCount) {
		String pageName = !isRoundTrip ? FLIGHTS_V2_SEARCH_ONEWAY :
			isOutboundFlight ? FLIGHT_SEARCH_ROUNDTRIP_OUT : FLIGHT_SEARCH_ROUNDTRIP_IN;
		StringBuilder link = new StringBuilder(pageName);
		link.append(".Scroll.").append(scrollDepth);
		ADMS_Measurement s = createTrackLinkEvent(link.toString());
		String event = new StringBuilder("event245=").append(totalCount).toString();
		s.setEvents(event);
		s.trackLink(null, "o", "Scroll", null, null);
	}

	public static void trackPageLoadFlightSearchV2() {
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(FLIGHT_SEARCH_V2);
		s.setEvar(18, FLIGHT_SEARCH_V2);
		s.setEvar(2, "D=c2");

		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightAATest);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightAdvanceSearch);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightTravelerFormRevamp);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightSwitchFields);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightTravelerFormRevamp);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsSearchResultCaching);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightSuggestionOnOneCharacter);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightSearchSuggestionLabel);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsGreedySearchCall);
		s.track();
	}

	public static void trackResultOutBoundFlights(
		FlightSearchTrackingData searchTrackingData, boolean isSubpub, String cacheString) {
		String pageName =
			searchTrackingData.getReturnDate() != null ? FLIGHT_SEARCH_ROUNDTRIP_OUT : FLIGHTS_V2_SEARCH_ONEWAY;

		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);

		// Search Type: value always 'Flight'
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");

		// Search Origin: 3 letter airport code of origin
		String origin = searchTrackingData.getDepartureAirport().hierarchyInfo.airport.airportCode;
		s.setEvar(3, "D=c3");
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = searchTrackingData.getArrivalAirport().hierarchyInfo.airport.airportCode;
		s.setEvar(4, "D=c4");
		s.setProp(4, dest);

		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightsSearchResultCaching) && Strings.isNotEmpty(cacheString)) {
			String cacheTrackingString = pageName + ".MobCache." + cacheString;
			s.setEvar(28, cacheTrackingString);
			s.setProp(16, cacheTrackingString);
		}

		// day computation date
		LocalDate departureDate = searchTrackingData.getDepartureDate();
		LocalDate returnDate = searchTrackingData.getReturnDate();

		setDateValues(s, departureDate, returnDate);
		s.setEvar(47, getFlightV2Evar47String(searchTrackingData));
		StringBuilder events = new StringBuilder("event12,event54");
		if (isSubpub) {
			events.append(",event203");
		}
		setEventsForSearchTracking(s, searchTrackingData.getPerformanceData(), events.toString());
		trackAbacusTest(s, AbacusUtils.EBAndroidAppSimplifyFlightShopping);
		if (pageName.equals(FLIGHT_SEARCH_ROUNDTRIP_OUT)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightByotSearch);
			trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsCrossSellPackageOnFSR);
		}
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightSubpubChange);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsEvolable);

		// This is added to collect data and will be removed by Feb'18. Card# 8787
		if (searchTrackingData.getReturnDate() == null) {
			s.setEvar(70, getTopFlightResults(searchTrackingData.getFlightLegList()));
		}

		s.track();
	}

	private static final int FLIGHT_TOP_RESULTS = 7;
	private static String getTopFlightResults(List<FlightLeg> flightLegs) {
		StringBuilder resultsBuilder = new StringBuilder("");
		if (CollectionUtils.isNotEmpty(flightLegs)) {
			int listSize = (flightLegs.size() > FLIGHT_TOP_RESULTS) ? FLIGHT_TOP_RESULTS : flightLegs.size();
			for (int index = 0; index < listSize; index++) {
				FlightLeg flightLeg = flightLegs.get(index);
				if (flightLeg != null) {
					if (resultsBuilder.length() > 0) {
						resultsBuilder.append(",");
					}
					resultsBuilder.append(FlightV2Utils.formatTimeShort(sContext, flightLeg.departureDateTimeISO));
					resultsBuilder.append("|");
					resultsBuilder.append(FlightV2Utils.formatTimeShort(sContext, flightLeg.arrivalDateTimeISO));
					resultsBuilder.append("|");
					resultsBuilder.append(flightLeg.stopCount);
					resultsBuilder.append("|");
					resultsBuilder.append((getFlightLayoverDuration(flightLeg.flightSegments)));
					resultsBuilder.append("|");
					resultsBuilder.append(flightLeg.flightSegments.get(0).airlineCode);
					resultsBuilder.append("|");
					resultsBuilder.append((flightLeg.durationHour * 60) + flightLeg.durationMinute);
					resultsBuilder.append("|");
					Money flightPrice = flightLeg.packageOfferModel.price.averageTotalPricePerTicket;
					resultsBuilder.append(flightPrice.roundedAmount);
				}
			}
		}
		return resultsBuilder.toString();
	}

	private static int getFlightLayoverDuration(List<FlightSegment> flightSegments) {
		int layoverDuration = 0;
		if (CollectionUtils.isNotEmpty(flightSegments)) {
			for (FlightSegment flightSegment: flightSegments) {
				layoverDuration += ((flightSegment.layoverDurationHours * 60) + flightSegment.layoverDurationMinutes);
			}
		}
		return layoverDuration;
	}

	public static void trackFlightOverview(Boolean isOutboundFlight, Boolean isRoundTrip) {
		String pageName = !isRoundTrip ? FLIGHT_SEARCH_ONE_WAY_DETAILS :
			isOutboundFlight ? FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS : FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS;
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		if (isOutboundFlight) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode);
		}
		s.track();
	}

	public static void trackResultInBoundFlights(FlightSearchTrackingData trackingData,
		kotlin.Pair outboundSelectedAndTotalLegRank) {
		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_SEARCH_ROUNDTRIP_IN);
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		s.setEvar(35, getRankEvent(outboundSelectedAndTotalLegRank, null));

		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightByotSearch)) {
			setEventsForSearchTracking(s, trackingData.getPerformanceData(), "");
		}
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsDeltaPricing);
		s.track();
	}

	public static void trackSortFilterClick() {
		ADMS_Measurement s = createTrackPageLoadEventBase(PREFIX_FLIGHT_SEARCH_FILTER);
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		s.track();
	}

	public static void trackFlightSortBy(String sortedBy) {
		createAndtrackLinkEvent(FLIGHTS_V2_SORTBY_TEMPLATE + sortedBy, "Search Results Sort");
	}

	public static void trackFlightFilterStops(String stops) {
		createAndtrackLinkEvent(FLIGHTS_V2_FILTER_STOPS_TEMPLATE + stops, "Search Results Filter");
	}

	public static void trackFlightFilterAirlines() {
		createAndtrackLinkEvent(FLIGHTS_V2_FLIGHT_AIRLINES, "Search Results Filter");
	}

	public static void trackFlightFilterZeroResults() {
		ADMS_Measurement s = createTrackLinkEvent(FLIGHTS_V2_FLIGHT_FILTER_ZERO_RESULTS);
		s.setEvents("event273");
		s.trackLink(null, "o", "Zero results", null, null);
	}

	public static void trackFlightFilterDuration() {
		createAndtrackLinkEvent(FLIGHTS_V2_FLIGHT_FILTER_DURATION, "Search Results Filter");
	}

	public static void trackFlightFilterArrivalDeparture(boolean isDeparture) {
		StringBuilder link = new StringBuilder(FLIGHTS_V2_FLIGHT_FILTER_TIME);
		if (isDeparture) {
			link.append(".Departure");
		}
		else {
			link.append(".Arrival");
		}
		createAndtrackLinkEvent(link.toString(), "Search Results Filter");
	}

	public static void trackShowFlightOverView(
		com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams,
		PageUsableData overviewPageUsableData, kotlin.Pair outboundSelectedAndTotalLegRank,
		kotlin.Pair inboundSelectedAndTotalLegRank, boolean isFareFamilyAvailable, boolean isFareFamilySelected,
		boolean hasSubPub) {
		Log.d(TAG, "Tracking \"" + FLIGHTS_V2_RATE_DETAILS + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHTS_V2_RATE_DETAILS);

		s.setEvents("event4");
		// Search Type: value always 'Flight'
		s.setEvar(2, "D=c2");
		s.setProp(2, "Flight");
		// Search Origin: 3 letter airport code of origin
		String origin = flightSearchParams.getDepartureAirport().hierarchyInfo.airport.airportCode;
		s.setEvar(3, "D=c3");
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = flightSearchParams.getArrivalAirport().hierarchyInfo.airport.airportCode;
		s.setEvar(4, "D=c4");
		s.setProp(4, dest);

		s.setEvar(35, getRankEvent(outboundSelectedAndTotalLegRank, inboundSelectedAndTotalLegRank));

		// day computation date
		LocalDate departureDate = flightSearchParams.getDepartureDate();
		LocalDate returnDate = flightSearchParams.getReturnDate();

		setDateValues(s, departureDate, returnDate);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppCheckoutButtonText);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppOfferInsuranceInFlightSummary);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsBaggageWebViewHideAd);

		if (FeatureToggleUtil.isFeatureEnabled(sContext, R.string.preference_flight_rate_detail_from_cache)) {
			trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightRateDetailsFromCache);
		}

		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsFrenchLegalBaggageInfo);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview);

		String products = getFlightProductString(false);
		if (hasSubPub) {
			products = getFlightSubpubProductString(products);
		}
		s.setProducts(products);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview);

		StringBuilder eventStringBuilder = new StringBuilder(s.getEvents());

		if (isFareFamilyAvailable) {
			eventStringBuilder.append(isFareFamilySelected ? ",event275" : ",event274");
		}

		if (hasSubPub) {
			eventStringBuilder.append(",event204");
		}

		appendPageLoadTimeEvents(eventStringBuilder, overviewPageUsableData.getLoadTimeInSeconds());

		if (eventStringBuilder.length() > 0) {
			s.setEvents(eventStringBuilder.toString());
		}

		// This is added to collect data and will be removed by Feb'18. Card# 8787
		if (!flightSearchParams.isRoundTrip()) {
			s.setEvar(70, getSelectedFlight(Db.getTripBucket().getFlightV2().flightCreateTripResponse.details.legs,
				Db.getTripBucket().getFlightV2().flightCreateTripResponse.details.offer.averageTotalPricePerTicket));
		}

		s.track();
	}

	private static String getSelectedFlight(List<FlightLeg> flightLegs, Money offerPrice) {
		StringBuilder resultsBuilder = new StringBuilder("");
		if (CollectionUtils.isNotEmpty(flightLegs)) {
			FlightLeg flightLeg = flightLegs.get(0);
			if (flightLeg != null) {
				resultsBuilder.append(getFlightDepartureTimeFromSegments(flightLeg.segments));
				resultsBuilder.append("|");
				resultsBuilder.append(getFlightArrivalTimeFromSegments(flightLeg.segments));
				resultsBuilder.append("|");
				resultsBuilder.append(flightLeg.segments.size() - 1);
				resultsBuilder.append("|");
				resultsBuilder.append(flightLeg.segments.get(0).airlineCode);
				resultsBuilder.append("|");
				resultsBuilder.append(offerPrice.roundedAmount);
			}
		}
		return resultsBuilder.toString();
	}

	private static String getFlightDepartureTimeFromSegments(List<FlightSegment> flightSegments) {
		if (CollectionUtils.isNotEmpty(flightSegments)) {
			FlightSegment flightSegment = flightSegments.get(0);
			if (flightSegment != null) {
				return FlightV2Utils.formatTimeShort(sContext, flightSegment.departureTimeRaw);
			}
		}
		return "";
	}

	private static String getFlightArrivalTimeFromSegments(List<FlightSegment> flightSegments) {
		if (CollectionUtils.isNotEmpty(flightSegments)) {
			FlightSegment flightSegment = flightSegments.get(flightSegments.size() - 1);
			if (flightSegment != null) {
				return FlightV2Utils.formatTimeShort(sContext, flightSegment.arrivalTimeRaw);
			}
		}
		return "";
	}

	public static void trackFareFamilyCardViewClick(boolean isUpgradingFlight) {
		String upgradeString =
			isUpgradingFlight ? FLIGHTS_V2_FARE_FAMILY_CHANGE_CLASS : FLIGHTS_V2_FARE_FAMILY_UPGRADE_FLIGHT;
		Log.d(TAG, "Tracking \"" + upgradeString + "\" click...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, upgradeString);
		s.setProp(16, upgradeString);
		s.trackLink(null, "o", "Rate Details View", null, null);
	}

	private static String getRankEvent(kotlin.Pair outboundSelectedAndTotalLegRank,
		kotlin.Pair inboundSelectedAndTotalLegRank) {
		StringBuilder rank = new StringBuilder(outboundSelectedAndTotalLegRank.getFirst().toString()).append(".")
			.append(outboundSelectedAndTotalLegRank.getSecond());
		if (inboundSelectedAndTotalLegRank != null) {
			rank.append("|").append(inboundSelectedAndTotalLegRank.getFirst()).append(".")
				.append(inboundSelectedAndTotalLegRank.getSecond());
		}
		return rank.toString();
	}

	public static void trackOverviewFlightExpandClick(boolean isExpanding) {
		StringBuilder link = new StringBuilder(FLIGHTS_V2_DETAILS_EXPAND);
		link.append(isExpanding ? "Expand" : "Collapse");
		createAndtrackLinkEvent(link.toString(), "Rate Details");
	}

	public static void trackFlightCostBreakdownClick() {
		createAndtrackLinkEvent(FLIGHTS_V2_COST_SUMMARY, "Rate Details");
	}

	public static void trackFlightCreateTripPriceChange(int priceChangePercentage) {
		ADMS_Measurement s = getFreshTrackingObject();
		trackPriceChange(s, priceChangePercentage, FLIGHTS_V2_RATE_DETAILS_PRICE_CHANGE, "FLT|", "Rate Details View");
	}

	public static void trackFlightCheckoutPriceChange(int priceChangePercentage) {
		ADMS_Measurement s = getFreshTrackingObject();
		trackPriceChange(s, priceChangePercentage, FLIGHTS_V2_CHECKOUT_PRICE_CHANGE, "FLT|", "Flight Checkout");
	}

	public static void trackFlightCabinClassSelect(String cabinClass) {
		ADMS_Measurement s = createTrackLinkEvent(FLIGHT_SEATING_CLASS_SELECT + cabinClass);
		s.trackLink(null, "o", "Search Results Update", null, null);
	}

	private static void trackPriceChange(ADMS_Measurement s, int priceChangePercentage, String trackingId,
		String lobForProp9, String linkName) {
		Log.d(TAG, "Tracking \"" + trackingId + "\" click...");
		s.setEvents("event62");
		s.setProp(9, lobForProp9 + priceChangePercentage);
		s.setEvar(28, trackingId);
		s.setProp(16, trackingId);
		s.trackLink(null, "o", linkName, null, null);
	}

	public static void trackFlightCheckoutSelectTraveler() {
		createAndtrackLinkEvent(FLIGHTS_V2_SELECT_TRAVELER, "Flight Checkout");
	}

	public static void trackPaymentStoredCCSelect() {
		createAndtrackLinkEvent(FLIGHTS_V2_SELECT_CARD, "Flight Checkout");
	}

	public static void trackShowPaymentEnterNewCard(LineOfBusiness lineOfBusiness) {
		switch (lineOfBusiness) {
		case FLIGHTS_V2: {
			createAndtrackLinkEvent(FLIGHTS_V2_ENTER_CARD, "Flight Checkout");
			break;
		}
		case PACKAGES: {
			createAndtrackLinkEvent(PACKAGES_ENTER_CARD, "Package Checkout");
			break;
		}
		default:
			break;
		}
	}

	public static void trackPaymentSelect() {
		trackPackagePageLoadEventStandard(FLIGHTS_V2_CHECKOUT_PAYMENT_SELECT, null);
	}

	public static void trackFlightShowSlideToPurchase(String cardType, String flexStatus) {
		trackShowSlidetoPurchase(FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE, cardType, flexStatus);
	}

	private static void trackShowSlidetoPurchase(String lobPageName, String cardType, String flexStatus) {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(lobPageName);
		s.setEvar(18, lobPageName);
		s.setEvar(37, cardType);
		if (Strings.isNotEmpty(flexStatus)) {
			s.setEvar(44, flexStatus);
		}
		s.track();
	}

	public static void trackFlightCheckoutPaymentCID() {
		createTrackPageLoadEventBase(FLIGHTS_V2_PAYMENT_CID).track();
	}

	public static void trackFlightError(String errorType) {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHTS_V2_ERROR);
		s.setProp(16, FLIGHTS_V2_ERROR);
		s.setProp(36, errorType);
		s.trackLink(null, "o", "Flight Error", null, null);
	}

	public static void trackFlightCheckoutError(String errorType) {
		ADMS_Measurement s = createTrackCheckoutErrorPageLoadEventBase(CHECKOUT_ERROR_PAGE_NAME,
			FLIGHTS_V2_CHECKOUT_ERROR);
		s.setProp(16, FLIGHTS_V2_CHECKOUT_ERROR);
		s.setProp(36, errorType);
		s.trackLink(null, "o", "Flight Checkout", null, null);
	}

	public static void trackCrossSellPackageBannerClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, FLIGHT_SEARCH_ROUNDTRIP_OUT_HOTELBANNER_SELECT);
		s.setProp(16, FLIGHT_SEARCH_ROUNDTRIP_OUT_HOTELBANNER_SELECT);
		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", FLIGHTS_V2_CROSS_SELL_PACKAGE_LINK_NAME, null, null);
	}

	private static String getFlightV2Evar47String(
		FlightSearchTrackingData searchTrackingData) {
		// Pipe delimited list of LOB, flight search type (OW, RT, MD), # of Adults, and # of Children)
		// e.g. FLT|RT|A2|C1
		String str = "FLT|";
		if (searchTrackingData.getReturnDate() != null) {
			str += "RT|A";
		}
		else {
			str += "OW|A";
		}

		int childrenInLap = getInfantInLap(searchTrackingData.getChildren(),
			searchTrackingData.getInfantSeatingInLap());
		int youthCount = getYouthCount(searchTrackingData.getChildren());
		str += searchTrackingData.getAdults();

		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
			str += "|YTH";
			str += youthCount;
		}

		int childrenInSeat = searchTrackingData.getChildren().size() - childrenInLap - youthCount;

		str += "|C";
		str += childrenInSeat;
		str += "|L";
		str += childrenInLap;

		if (searchTrackingData.getFlightCabinClass() != null) {
			str += '|' + FlightServiceClassType.getCabinClassTrackCode(searchTrackingData.getFlightCabinClass());
		}
		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightAdvanceSearch)) {
			if (searchTrackingData.getNonStopFlight() != null && searchTrackingData.getNonStopFlight()) {
				str += "|Dir";
			}
			if (searchTrackingData.getShowRefundableFlight() != null && searchTrackingData.getShowRefundableFlight()) {
				str += "|Rfd";
			}
		}
		return str;
	}

	private static int getInfantInLap(List<Integer> children, boolean infantSeatingInLap) {
		int infantInLap = 0;
		if (infantSeatingInLap) {
			for (int age : children) {
				if (age < 2) {
					++infantInLap;
				}
			}
		}
		return infantInLap;
	}

	private static int getYouthCount(List<Integer> children) {
		int youthCount = 0;
		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
			for (int age : children) {
				if (age > 11 && age < 18) {
					++youthCount;
				}
			}
		}
		return youthCount;
	}

	private static int getChildCount(List<Integer> children) {
		int childCount = 0;
		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
			for (int age : children) {
				if (age > 1 && age < 12) {
					++childCount;
				}
			}
		}
		return childCount;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Rail Tracking
	//
	// Spec: https://confluence/display/Omniture/Mobile+App%3A+Rail
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String RAIL_LOB = "rail";
	private static final String RAIL_SEARCH_BOX = "App.Rail.Dest-Search";
	private static final String RAIL_SEARCH_TRAVELER_PICKER_CLICK_TEMPLATE = "App.Rail.DS.Traveler.";
	private static final String RAIL_CARD_PICKER_PICKER_CLICK_TEMPLATE = "App.Rail.DS.Card.";
	private static final String RAIL_ROUND_TRIP_OUT_DETAILS = "App.Rail.Search.Roundtrip.Out.Details";
	private static final String RAIL_ROUND_TRIP_IN_DETAILS = "App.Rail.Search.Roundtrip.In.Details";
	private static final String RAIL_ONE_WAY_TRIP_DETAILS = "App.Rail.Search.Oneway.Details";
	private static final String RAIL_AMENITIES = "App.Rail.Amenities";
	private static final String RAIL_FARES = "App.Rail.FareRules";
	private static final String RAIL_SEARCH_ONE_WAY = "App.Rail.Search.Oneway";
	private static final String RAIL_SEARCH_ROUND_TRIP_OUT = "App.Rail.Search.Roundtrip.Out";
	private static final String RAIL_SEARCH_ROUND_TRIP_IN = "App.Rail.Search.Roundtrip.In";
	private static final String RAIL_CHECKOUT_CONFIRMATION = "App.Rail.Checkout.Confirmation";
	private static final String RAIL_RATE_DETAILS = "App.Rail.RateDetails";
	private static final String RAIL_RATE_DETAILS_TOTAL_COST = "App.Rail.RD.TotalCost";
	private static final String RAIL_RATE_DETAILS_VIEW_DETAILS = "App.Rail.RD.ViewDetails";
	private static final String RAIL_ERROR = "App.Rail.Error";
	private static final String RAIL_CHECKOUT_ERROR = "App.Rail.CKO.Error";
	private static final String RAIL_CHECKOUT_PRICE_CHANGE = "App.Rail.CKO.PriceChange";
	private static final String RAIL_CHECKOUT_INFO = "App.Rail.Checkout.Info";
	private static final String RAIL_CHECKOUT_TOTAL_COST = "App.Rail.CKO.TotalCost";
	private static final String RAIL_CHECKOUT_EDIT_TRAVELER_INFO = "App.Rail.Checkout.Traveler.Edit.Info";
	private static final String RAIL_CHECKOUT_EDIT_PAYMENT_CARD = "App.Rail.Checkout.Payment.Edit.Card";
	private static final String RAIL_CHECKOUT_SLIDE_TO_PURCHASE = "App.Rail.Checkout.SlideToPurchase";

	private static ADMS_Measurement createTrackRailPageLoadEventBase(String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.setEvar(2, "D=c2");
		s.setProp(2, RAIL_LOB);
		return s;
	}

	public static void trackRailSearchInit() {
		ADMS_Measurement s = createTrackRailPageLoadEventBase(RAIL_SEARCH_BOX);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppAPIMAuth);
		s.track();
	}

	public static void trackRailSearchTravelerPickerChooser(String text) {
		createAndtrackLinkEvent(RAIL_SEARCH_TRAVELER_PICKER_CLICK_TEMPLATE + text, "Search Results Update");
	}

	public static void trackRailCardPicker(String text) {
		createAndtrackLinkEvent(RAIL_CARD_PICKER_PICKER_CLICK_TEMPLATE + text, "Search Results Update");
	}

	public static void trackRailRoundTripJourneyDetailsAndFareOptions() {
		String pageName = RAIL_ROUND_TRIP_OUT_DETAILS;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailRoundTripInDetails() {
		String pageName = RAIL_ROUND_TRIP_IN_DETAILS;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailOneWayTripDetails() {
		String pageName = RAIL_ONE_WAY_TRIP_DETAILS;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailAmenities() {
		String pageName = RAIL_AMENITIES;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailFares() {
		String pageName = RAIL_FARES;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailOneWaySearch(RailLeg outboundLeg, RailSearchRequest railSearchRequest) {
		ADMS_Measurement s = createTrackRailPageLoadEventBase(RAIL_SEARCH_ONE_WAY);
		Log.d(TAG, "Tracking \"" + RAIL_SEARCH_ONE_WAY + "\"");
		s.setAppState(RAIL_SEARCH_ONE_WAY);
		setOutboundTrackingDetails(s, outboundLeg, railSearchRequest);
		s.track();
	}

	public static void trackRailRoundTripOutbound(RailLeg outboundLeg, RailSearchRequest railSearchRequest) {
		ADMS_Measurement s = createTrackRailPageLoadEventBase(RAIL_SEARCH_ROUND_TRIP_OUT);
		Log.d(TAG, "Tracking \"" + RAIL_SEARCH_ROUND_TRIP_OUT + "\"");
		s.setAppState(RAIL_SEARCH_ROUND_TRIP_OUT);
		setOutboundTrackingDetails(s, outboundLeg, railSearchRequest);
		s.track();
	}

	private static void setOutboundTrackingDetails(ADMS_Measurement s, RailLeg outboundLeg,
		RailSearchRequest railSearchRequest) {
		s.setEvents("event12,event124");
		s.setProp(1, String.valueOf(outboundLeg.legOptionList.size()));

		s.setProp(3, railSearchRequest.getOrigin().hierarchyInfo.rails.stationCode);
		s.setEvar(3, "D=c3");
		s.setProp(4, railSearchRequest.getDestination().hierarchyInfo.rails.stationCode);
		s.setEvar(4, "D=c4");
		setDateValues(s, railSearchRequest.getStartDate(), railSearchRequest.getEndDate());

		StringBuilder evar47String = new StringBuilder("RL|");
		if (railSearchRequest.isRoundTripSearch()) {
			evar47String.append("RT|");
		}
		else {
			evar47String.append("OW|");
		}
		evar47String.append("A" + railSearchRequest.getAdults() + "|");
		evar47String.append("S" + railSearchRequest.getSeniors().size() + "|");
		evar47String.append("C" + railSearchRequest.getChildren().size() + "|");
		evar47String.append("Y" + railSearchRequest.getYouths().size());

		s.setEvar(47, evar47String.toString());

		// Freeform location
		if (!TextUtils.isEmpty(railSearchRequest.getDestination().regionNames.fullName)) {
			s.setEvar(48, railSearchRequest.getDestination().regionNames.fullName);
		}
	}

	public static void trackRailRoundTripInbound() {
		createTrackRailPageLoadEventBase(RAIL_SEARCH_ROUND_TRIP_IN).track();
	}

	public static void trackAppRailsCheckoutConfirmation(RailCheckoutResponse checkoutResponse) {
		ADMS_Measurement s = createTrackRailPageLoadEventBase(RAIL_CHECKOUT_CONFIRMATION);
		String orderId = checkoutResponse.orderId;
		String currencyCode = checkoutResponse.currencyCode;
		String itinNumber = checkoutResponse.newTrip.itineraryNumber;
		s.setEvents("purchase");
		s.setPurchaseID("onum" + orderId);
		s.setProp(72, orderId);
		s.setProp(71, itinNumber);     //API doesn't send TRL
		s.setCurrencyCode(currencyCode);
		RailTripOffer railOffer = checkoutResponse.railDomainProduct.railOffer;
		DateTime endDate;
		String destinationStationCode;
		String departureStationCode = railOffer.getOutboundLegOption().departureStation.getStationCode();

		if (railOffer.isRoundTrip()) {
			endDate = railOffer.getInboundLegOption().arrivalDateTime.toDateTime();
			destinationStationCode = railOffer.getInboundLegOption().arrivalStation.getStationCode();
		}
		else {
			endDate = railOffer.getOutboundLegOption().arrivalDateTime.toDateTime();
			destinationStationCode = railOffer.getOutboundLegOption().arrivalStation.getStationCode();
		}

		DateTime startDate = railOffer.getOutboundLegOption().departureDateTime.toDateTime();

		s.setProducts(
			getRailProductString(checkoutResponse, endDate, startDate, departureStationCode, destinationStationCode));
		s.setProp(3, departureStationCode);
		s.setEvar(3, "D=c3");

		s.setProp(4, destinationStationCode);
		s.setEvar(4, "D=c4");
		setDateValues(s, startDate.toLocalDate(), endDate.toLocalDate());
		s.track();
	}

	private static String getRailProductString(RailCheckoutResponse checkoutResponse, DateTime endDate,
		DateTime startDate, String departureStationCode, String destinationStationCode) {
		String carrier = checkoutResponse.railDomainProduct.railOffer.colonSeparatedSegmentOperatingCarriers();

		StringBuilder productString = new StringBuilder(";Rail:");
		productString.append(carrier + ":");

		RailTripOffer railOffer = checkoutResponse.railDomainProduct.railOffer;
		if (checkoutResponse.railDomainProduct.railOffer.isRoundTrip()) {
			productString.append("RT;");
		}
		else {
			productString.append("OW;");
		}
		productString.append(railOffer.passengerList.size() + ";");
		productString.append(checkoutResponse.totalCharges + ";;");
		productString.append("eVar30=Agency:Rail:");
		productString.append(departureStationCode + "-" + destinationStationCode + ":");

		productString.append(startDate.toString(EVAR30_DATE_FORMAT) + "-" + endDate.toString(EVAR30_DATE_FORMAT));

		return productString.toString();
	}

	public static void trackRailDetails(RailCreateTripResponse railCreateTripResponse) {
		String pageName = RAIL_RATE_DETAILS;
		ADMS_Measurement s = createTrackRailPageLoadEventBase(pageName);
		String products = s.getProducts();
		products += ";";

		s.setProducts(products);
		s.setEvents("event4");
		s = processRailCreateTripResponse(s, railCreateTripResponse);
		s.track();
	}

	public static void trackRailCheckoutInfo(RailCreateTripResponse railCreateTripResponse) {
		String pageName = RAIL_CHECKOUT_INFO;
		ADMS_Measurement s = createTrackRailPageLoadEventBase(pageName);
		s = processRailCreateTripResponse(s, railCreateTripResponse);

		String products = s.getProducts();
		products += String.valueOf(railCreateTripResponse.railDomainProduct.railOffer.passengerList.size()) + ";";
		products += String.valueOf(railCreateTripResponse.railDomainProduct.railOffer.totalPrice.amount) + ";;";
		products += "eVar63=Merchant:SA";

		s.setProducts(products);
		s.setEvents("event36,event68");
		s.track();
	}

	private static ADMS_Measurement processRailCreateTripResponse(ADMS_Measurement s,
		RailCreateTripResponse railCreateTripResponse) {
		String products = ";Rail:";
		String departureStation;
		String arrivalStation;
		int searchWindow, searchDuration;

		products += railCreateTripResponse.railDomainProduct.railOffer.colonSeparatedSegmentOperatingCarriers();

		RailLegOption outboundLegOption = railCreateTripResponse.railDomainProduct.railOffer.getOutboundLegOption();
		RailLegOption inboundLegOption = railCreateTripResponse.railDomainProduct.railOffer.getInboundLegOption();

		departureStation = outboundLegOption.departureStation.getStationCode();
		arrivalStation = outboundLegOption.arrivalStation.getStationCode();
		searchWindow = JodaUtils.daysBetween(new DateTime(), outboundLegOption.getDepartureDateTime());

		if (!railCreateTripResponse.railDomainProduct.railOffer.isRoundTrip()
			&& !railCreateTripResponse.railDomainProduct.railOffer.isOpenReturn()) {
			products += ":OW;";
		}
		else {
			products += ":RT;";
			searchDuration = JodaUtils
				.daysBetween(outboundLegOption.getDepartureDateTime(), inboundLegOption.getDepartureDateTime());
			s.setEvar(6, String.valueOf(searchDuration));
			s.setProp(6, DateUtils.localDateToyyyyMMdd(inboundLegOption.departureDateTime.toDateTime().toLocalDate()));
		}

		s.setProducts(products);
		s.setEvar(3, "D=c3");
		s.setProp(3, departureStation);
		s.setEvar(4, "D=c4");
		s.setProp(4, arrivalStation);
		s.setEvar(5, String.valueOf(searchWindow));
		s.setProp(5, DateUtils.localDateToyyyyMMdd(outboundLegOption.departureDateTime.toDateTime().toLocalDate()));

		return s;
	}

	public static void trackRailDetailsTotalCostToolTip() {
		Log.d(TAG, "Tracking \"" + RAIL_RATE_DETAILS_TOTAL_COST + "\" click...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, "1");
		s.setEvar(28, RAIL_RATE_DETAILS_TOTAL_COST);
		s.setProp(16, RAIL_RATE_DETAILS_TOTAL_COST);
		s.setEvar(61, "1");
		s.trackLink(null, "o", "Rate Details", null, null);
	}

	public static void trackRailTripOverviewDetailsExpand() {
		Log.d(TAG, "Tracking \"" + RAIL_RATE_DETAILS_VIEW_DETAILS + "\" click...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, "1");
		s.setEvar(28, RAIL_RATE_DETAILS_VIEW_DETAILS);
		s.setProp(16, RAIL_RATE_DETAILS_VIEW_DETAILS);
		s.setEvar(61, "1");
		s.trackLink(null, "o", "Rate Details View", null, null);
	}

	public static void trackRailError(String errorType) {
		Log.d(TAG, "Tracking \"" + RAIL_ERROR + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, "1");
		s.setEvar(28, RAIL_ERROR);
		s.setProp(16, RAIL_ERROR);
		s.setProp(36, errorType);
		s.setEvar(61, "1");
		s.trackLink(null, "o", "Rail Error", null, null);
	}

	public static void trackRailCheckoutError(String errorType) {
		Log.d(TAG, "Tracking \"" + RAIL_CHECKOUT_ERROR + "\" pageLoad...");
		ADMS_Measurement s = createTrackCheckoutErrorPageLoadEventBase(CHECKOUT_ERROR_PAGE_NAME, RAIL_CHECKOUT_ERROR);
		s.setProp(7, "1");
		s.setProp(16, RAIL_CHECKOUT_ERROR);
		s.setProp(36, errorType);
		s.setEvar(61, "1");
		s.trackLink(null, "o", "Rail Checkout", null, null);
	}

	public static void trackRailCheckoutPriceChange(int priceDiff) {
		Log.d(TAG, "Tracking \"" + RAIL_CHECKOUT_PRICE_CHANGE + "\" click...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvents("event62");
		s.setProp(9, "RAIL|" + priceDiff);
		s.setEvar(28, RAIL_CHECKOUT_PRICE_CHANGE);
		s.setProp(16, RAIL_CHECKOUT_PRICE_CHANGE);
		s.setProp(7, "1");
		s.setEvar(61, "1");
		s.trackLink(null, "o", "Rail Checkout", null, null);
	}

	public static void trackRailCheckoutTotalCostToolTip() {
		Log.d(TAG, "Tracking \"" + RAIL_CHECKOUT_TOTAL_COST + "\" click...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setProp(7, "1");
		s.setEvar(28, RAIL_CHECKOUT_TOTAL_COST);
		s.setProp(16, RAIL_CHECKOUT_TOTAL_COST);
		s.setEvar(61, "1");
		s.trackLink(null, "o", "Rail Checkout", null, null);
	}

	public static void trackRailEditTravelerInfo() {
		String pageName = RAIL_CHECKOUT_EDIT_TRAVELER_INFO;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailEditPaymentInfo() {
		String pageName = RAIL_CHECKOUT_EDIT_PAYMENT_CARD;
		createTrackRailPageLoadEventBase(pageName).track();
	}

	public static void trackRailCheckoutSlideToPurchase(PaymentType paymentType) {
		String pageName = RAIL_CHECKOUT_SLIDE_TO_PURCHASE;
		ADMS_Measurement s = createTrackRailPageLoadEventBase(pageName);
		s.setEvar(37, paymentType.getOmnitureTrackingCode());
		s.track();
	}

	public static void trackItinSignIn() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_NEW_SIGN_IN);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinRefresh() {
		ADMS_Measurement s = createTrackLinkEvent(ITIN_USER_REFRESH);
		s.trackLink(null, "o", "Itinerary Action", null, null);
	}

	public static void trackItinUserRating() {
		String pageName = ITIN_RATE_APP;
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.track();
	}

	public static void trackItinAppRatingClickReview() {
		trackItinAppRatingClick("App.RateApp.Review");
	}

	public static void trackItinAppRatingClickFeedback() {
		trackItinAppRatingClick("App.RateApp.Feedback");
	}

	public static void trackItinAppRatingClickNo() {
		trackItinAppRatingClick("App.RateApp.NoThanks");
	}

	private static void trackItinAppRatingClick(String rfrrid) {
		String pageName = ITIN_RATE_APP;
		ADMS_Measurement s = createTrackPageLoadEventBase(pageName);
		s.setEvar(28, rfrrid);
		s.setProp(16, rfrrid);
		s.trackLink(null, "o", "Rate App Action", null, null);
	}
}
