package com.expedia.bookings.tracking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.adobe.adms.measurement.ADMS_ReferrerHandler;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.LineOfBusiness;
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
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.enums.CheckoutTripBucketState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.ExpediaNetUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.SettingUtils;

/**
 *
 * The basic premise behind this class is to encapsulate the tracking logic as much possible such that tracking events
 * can be inserted into the business logic as cleanly as possible. The events rely on Db.java to populate values when
 * needed, and exceptions are made to accommodate the events that require extra parameters to be sent. This is why there
 * exist so many methods, one for each event that is being tracked.
 *
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
	// Hotels tracking
	//
	// There does not appear to be an official spec for hotels tracking...
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String HOTELS_RATE_DETAILS = "App.Hotels.RateDetails";
	private static final String HOTELS_DETAILS_REVIEWS = "App.Hotels.Reviews";
	private static final String HOTELS_CHECKOUT_INFO = "App.Hotels.Checkout.Info";
	private static final String HOTELS_CHECKOUT_LOGIN = "App.Hotels.Checkout.Login";
	private static final String HOTELS_CHECKOUT_TRAVELER_SELECT = "App.Hotels.Checkout.Traveler.Select";
	private static final String HOTELS_CHECKOUT_TRAVELER_EDIT_INFO = "App.Hotels.Checkout.Traveler.Edit.Info";
	private static final String HOTELS_CHECKOUT_TRAVELER_ENTER_MANUALLY = "App.Hotels.Checkout.Traveler.EnterManually";
	private static final String HOTELS_CHECKOUT_WARSAW = "App.Hotels.Checkout.Warsaw";
	private static final String HOTELS_CHECKOUT_PAYMENT_SELECT = "App.Hotels.Checkout.Payment.Select";
	private static final String HOTELS_CHECKOUT_PAYMENT_EDIT_CARD = "App.Hotels.Checkout.Payment.Edit.Card";
	private static final String HOTELS_CHECKOUT_PAYMENT_EDIT_SAVE = "App.Hotels.Checkout.Payment.Edit.Save";
	private static final String HOTELS_CHECKOUT_PAYMENT_SELECT_EXISTING = "App.Hotels.Checkout.Payment.Select.Existing";
	private static final String HOTELS_CHECKOUT_PAYMENT_ENTER_MANUALLY = "App.Hotels.Checkout.Payment.EnterManually";
	private static final String HOTELS_CHECKOUT_SLIDE_TO_PURCHASE = "App.Hotels.Checkout.SlideToPurchase";
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

	public static final String HOTELS_ETP_INFO_PAGE = "App.Hotels.ETPInfo";
	public static final String HOTELS_ETP_TOGGLE_LINK_NAME = "ETP Toggle";
	public static final String HOTELS_ETP_TOGGLE_PAY_LATER = "App.Hotels.RR.Toggle.PayLater";
	public static final String HOTELS_ETP_TOGGLE_PAY_NOW = "App.Hotels.RR.Toggle.PayNow";
	public static final String HOTELS_ETP_PAYMENT = "App.Hotels.RR.ETP";

	private static final String HOTELS_MER_EMAIL_OPT_IN = "App.Mktg.Opt-in";
	private static final String HOTELS_MER_EMAIL_OPT_OUT = "App.Mktg.Opt-Out";

	//////////////////////////////
	// Coupon tracking
	public static final String HOTELS_COUPON_LINK_NAME = "CKO:Coupon Action";
	public static final String HOTELS_COUPON_EXPAND = "App.CKO.Coupon.Expand";
	public static final String HOTELS_COUPON_SUCCESS = "App.CKO.Coupon.Success";
	public static final String HOTELS_COUPON_REMOVE = "App.CKO.Coupon.Remove";
	public static final String HOTELS_COUPON_FAIL = "App.CKO.Coupon.Fail";

	public static void trackAppHotelsSearch() {
		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		HotelSearchResponse searchResponse = Db.getHotelSearch().getSearchResponse();
		internalTrackHotelsSearch(searchParams, searchResponse);
	}

	private static void internalTrackHotelsSearch(HotelSearchParams searchParams,
		HotelSearchResponse searchResponse) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"App.Hotels.Search\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setAppState("App.Hotels.Search");
		s.setEvents("event30,event51");

		// LOB Search
		s.setEvar(2, "hotels");
		s.setProp(2, "hotels");

		// Region
		addHotelRegionId(s, searchParams);

		// Check in/check out date
		addAdvancePurchaseWindow(s, searchParams);

		s.setEvar(47, getEvar47String(searchParams));

		// prop and evar 5, 6
		setDateValues(s, searchParams.getCheckInDate(), searchParams.getCheckOutDate());

		// Freeform location
		if (!TextUtils.isEmpty(searchParams.getUserQuery())) {
			s.setEvar(48, searchParams.getUserQuery());
		}

		// Number of search results
		if (searchResponse != null && searchResponse.getFilteredAndSortedProperties(searchParams) != null) {
			s.setProp(1, searchResponse.getFilteredAndSortedProperties(searchParams).size() + "");
		}

		if (searchResponse != null) {
			// Has at least one sponsored Listing
			if (searchResponse.hasSponsoredListing()) {
				s.setEvar(28, HOTELS_SEARCH_SPONSORED_PRESENT);
				s.setProp(16, HOTELS_SEARCH_SPONSORED_PRESENT);
			}
			else {
				s.setEvar(28, HOTELS_SEARCH_SPONSORED_NOT_PRESENT);
				s.setProp(16, HOTELS_SEARCH_SPONSORED_NOT_PRESENT);
			}
		}

		trackAbacusTest(s, AbacusUtils.EBAndroidAATest);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppSRPercentRecommend);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelETPSearchResults);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHSRMapIconTest);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsRoomsRates(Context context, Property property) {
		Log.d(TAG, "Tracking \"App.Hotels.RoomsRates\" event");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setAppState("App.Hotels.RoomsRates");

		// Promo description
		s.setEvar(9, internalGenerateDRRString(context, property));

		if (ProductFlavorFeatureConfiguration.getInstance().isETPEnabled() && property.hasEtpOffer()) {
			s.setEvents("event5");
		}

		// Products
		addProducts(s, property);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsETPInfoPage() {
		Log.d(TAG, "Tracking \"" + HOTELS_ETP_INFO_PAGE + "\" event");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setAppState(HOTELS_ETP_INFO_PAGE);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutConfirmation(Context context, HotelSearchParams searchParams,
			Property property, String supplierType, Rate rate, HotelBookingResponse response) {
		Log.d(TAG, "Tracking \"App.Hotels.Checkout.Confirmation\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject();

		String pageName = "App.Hotels.Checkout.Confirmation";
		s.setAppState(pageName);
		s.setEvar(18, pageName);
		s.setEvents("purchase");

		// Promo description
		if (rate != null) {
			s.setEvar(9, internalGenerateDRRString(context, property));
		}

		// Product details
		DateTimeFormatter dtf = ISODateTimeFormat.basicDate();
		String checkIn = dtf.print(searchParams.getCheckInDate());
		String checkOut = dtf.print(searchParams.getCheckOutDate());
		s.setEvar(30, "Hotel:" + checkIn + "-" + checkOut + ":N");

		// Unique confirmation id
		// 14103: Remove timestamp from the purchaseID variable
		s.setProp(71, response.getItineraryId());
		s.setProp(72, response.getOrderNumber());
		s.setPurchaseID("onum" + response.getOrderNumber());

		// Products
		int numDays = searchParams.getStayDuration();
		double totalCost = 0;
		if (rate != null && rate.getTotalAmountAfterTax() != null) {
			totalCost = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		}
		addProducts(s, property, supplierType, numDays, totalCost);

		// Currency code
		s.setCurrencyCode(rate.getTotalAmountAfterTax().getCurrency());

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelsInfosite(Context context) {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState("App.Hotels.Rooms.Infosite");
		s.setEvents("event32");

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

		String drrString = internalGenerateDRRString(context, property);
		s.setEvar(9, drrString);

		// 4761 - AB Test: Collapse Amenities, Policies, and fees on Infosite
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelCollapseAmenities);

		// Send the tracking data
		s.track();
	}

	private static String internalGenerateDRRString(Context context, Property property) {
		StringBuilder sb = new StringBuilder("Hotels | ");
		if (property != null) {
			if (property.isLowestRateMobileExclusive()) {
				sb.append("Mobile Exclusive");
				if (property.getLowestRate() != null && property.getLowestRate().isOnSale()) {
					sb.append(": ");
				}
			}
			if (property.getLowestRate() != null && property.getLowestRate().isOnSale()) {
				String discount = context.getString(R.string.percent_off_template,
					(float) property.getLowestRate().getDiscountPercent());
				sb.append(discount);
			}
			return sb.toString();
		}
		return null;
	}

	public static void trackPageLoadHotelsInfositeMap() {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite.Map\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setAppState("App.Hotels.Infosite.Map");

		// Products
		addProducts(s, Db.getHotelSearch().getSelectedProperty());

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

	private static void addProducts(ADMS_Measurement s, Property property, String supplierType, int numNights, double totalCost) {
		// The "products" field uses this format:
		// Hotel;Hotel;<supplier> Hotel:<hotel id>

		if (TextUtils.isEmpty(supplierType)) {
			supplierType = "";
		}
		String properCaseSupplierType = Strings.splitAndCapitalizeFirstLetters(supplierType);

		s.setProducts("Hotel;" + properCaseSupplierType + " Hotel:" + property.getPropertyId());

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

	public static void trackPageLoadHotelsRateDetails() {
		Log.d(TAG, "Tracking \"" + HOTELS_RATE_DETAILS + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELS_RATE_DETAILS);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelShowAddressMapInReceipt);
		s.track();
	}

	public static void trackPageLoadHotelsDetailsReviews() {
		internalTrackPageLoadEventStandard(HOTELS_DETAILS_REVIEWS, LineOfBusiness.HOTELS);
	}

	public static void trackLinkReviewTypeSelected(String linkName) {
		internalTrackLink(linkName);
	}

	public static void trackPageLoadHotelsCheckoutInfo() {
		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELS_CHECKOUT_INFO);
		s.setEvents("event70");

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelHCKOTraveler);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelPayLaterCouponMessaging);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotel3xMessaging);

		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		s.setEvar(47, getEvar47String(params));
		addHotelRegionId(s, params);
		addProducts(s, Db.getTripBucket().getHotel().getProperty(), Db.getTripBucket().getHotel().getCreateTripResponse().getSupplierType());
		addStandardHotelFields(s, params);

		s.track();
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

	public static void trackPageLoadHotelsTravelerEditInfo() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_TRAVELER_EDIT_INFO);
	}

	public static void trackPageLoadHotelsTravelerSelect() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_TRAVELER_SELECT);
	}

	public static void trackLinkHotelsCheckoutTravelerEnterManually() {
		internalTrackLink(HOTELS_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	public static void trackPageLoadHotelsCheckoutPaymentSelect() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPageLoadHotelsCheckoutPaymentEditCard() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_PAYMENT_EDIT_CARD);
	}

	public static void trackPageLoadHotelsCheckoutPaymentEditSave() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_PAYMENT_EDIT_SAVE);
	}

	public static void trackLinkHotelsCheckoutPaymentSelectExisting() {
		internalTrackLink(HOTELS_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkHotelsCheckoutPaymentEnterManually() {
		internalTrackLink(HOTELS_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	public static void trackPageLoadHotelsCheckoutSlideToPurchase() {
		Log.d(TAG, "Tracking \"" + HOTELS_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(HOTELS_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, getPaymentType());
		s.track();
	}

	public static void trackPageLoadHotelsCheckoutWarsaw() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_WARSAW);
	}

	public static void trackPageLoadHotelsCheckoutPaymentCid() {
		internalTrackPageLoadEventStandard(HOTELS_CHECKOUT_PAYMENT_CID);
	}

	public static void trackHotelSearchMapSwitch() {
		ADMS_Measurement s = OmnitureTracking.getFreshTrackingObject();
		s.setAppState("App.Hotels.Search.Map");

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelHSRSalePinTest);

		s.track();
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
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setProp(7, posTpid);
		s.setEvar(61, posTpid);
		internalTrackLink(s);
	}

	public static void trackHotelETPPayToggle(boolean isPayLater) {
		String refererId = isPayLater ? HOTELS_ETP_TOGGLE_PAY_LATER : HOTELS_ETP_TOGGLE_PAY_NOW;
		ADMS_Measurement s = createTrackLinkEvent(refererId);
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setProp(7, posTpid);
		s.setEvar(61, posTpid);

		s.trackLink(null, "o", HOTELS_ETP_TOGGLE_LINK_NAME, null, null);
	}

	public static void trackHotelETPRoomSelected(boolean isPayLater) {
		ADMS_Measurement s = createTrackLinkEvent(HOTELS_ETP_PAYMENT);
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setProp(7, posTpid);
		s.setEvar(61, posTpid);
		if (isPayLater) {
			s.setEvar(52, "Pay Later");
		}
		else {
			s.setEvar(52, "Pay Now");
		}
		s.trackLink(null, "o", HOTELS_ETP_TOGGLE_LINK_NAME, null, null);
	}

	public static void trackHotelsGuestMerEmailOptIn() {
		Log.d(TAG, "Tracking \"" + HOTELS_MER_EMAIL_OPT_IN + "\"");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvents("event42");

		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setProp(7, posTpid);
		s.setEvar(28, HOTELS_MER_EMAIL_OPT_IN);
		s.setProp(16, HOTELS_MER_EMAIL_OPT_IN);
		s.setEvar(61, posTpid);

		// AB Test: Opt-in/out checkbox for MER email on Guest HCKO
		trackAbacusTest(s, AbacusUtils.EBAndroidHotelCKOMerEmailGuestOpt);

		s.trackLink(null, "o", "Marketing Choice", null, null);
	}

	public static void trackHotelsGuestMerEmailOptOut() {
		Log.d(TAG, "Tracking \"" + HOTELS_MER_EMAIL_OPT_OUT + "\"");

		ADMS_Measurement s = getFreshTrackingObject();

		s.setEvents("event43");

		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		s.setProp(7, posTpid);
		s.setEvar(28, HOTELS_MER_EMAIL_OPT_OUT);
		s.setProp(16, HOTELS_MER_EMAIL_OPT_OUT);
		s.setEvar(61, posTpid);

		// AB Test: Opt-in/out checkbox for MER email on Guest HCKO
		trackAbacusTest(s, AbacusUtils.EBAndroidHotelCKOMerEmailGuestOpt);

		s.trackLink(null, "o", "Marketing Choice", null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights Tracking
	//
	// Spec: http://confluence/display/Omniture/Mobile+App+Flight+Tracking
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String FLIGHT_SEARCH = "App.Flight.Search";
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
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_CONFIRMATION + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_CHECKOUT_CONFIRMATION);

		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();

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

		addProducts(s);

		s.setCurrencyCode(trip.getTotalFare().getCurrency());
		s.setEvents("purchase");

		// order number with an "onum" prefix, described here: http://confluence/pages/viewpage.action?pageId=419913476
		final String orderId = Db.getTripBucket().getFlight().getCheckoutResponse().getOrderId();
		s.setPurchaseID("onum" + orderId);

		// TRL
		Itinerary itin = Db.getTripBucket().getFlight().getItinerary();
		s.setProp(71, itin.getItineraryNumber());

		// order #
		s.setProp(72, orderId);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightConfCarsXsell);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightConfLXXsell);
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
		ADMS_Measurement s = createTrackPageLoadEventBase(FLIGHT_CHECKOUT_INFO);
		s.setEvents("event74");
		FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
		s.setEvar(47, getEvar47String(params));

		String origin = params.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);
		String dest = params.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		addProducts(s);
		internalSetFlightDateProps(s, params);
		addStandardFlightFields(s);

		trackAbacusTest(s, AbacusUtils.EBAndroidAppPaySuppressGoogleWallet);

		// AB Test: FCKO - More prominent callout for missing traveler details
		trackAbacusTest(s, AbacusUtils.EBAndroidAppFlightMissingTravelerInfoCallout);

		s.track();
	}



	public static void trackPageLoadFlightRateDetailsOverview() {
		internalTrackPageLoadEventPriceChange(FLIGHT_RATE_DETAILS);
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
	public static final String LX_DESTINATION_SEARCH = "App.LX.Dest-Search";
	public static final String LX_INFOSITE_INFORMATION = "App.LX.Infosite.Information";
	public static final String LX_CHECKOUT_INFO = "App.LX.Checkout.Info";
	public static final String LX_CHECKOUT_CONFIRMATION = "App.LX.Checkout.Confirmation";

	public static final String LX_TICKET_SELECT = "App.LX.Ticket.Select";
	public static final String LX_CHANGE_DATE = "App.LX.Info.DateChange";
	public static final String LX_INFO = "LX_INFO";
	public static final String LX_TICKET = "App.LX.Ticket.";
	private static final String LX_CHECKOUT_TRAVELER_INFO = "App.LX.Checkout.Traveler.Edit.Info";
	private static final String LX_CHECKOUT_LOGIN_SUCCESS = "App.LX.Checkout.Login.Success";
	private static final String LX_CHECKOUT_PAYMENT_INFO = "App.LX.Checkout.Payment.Edit.Info";
	private static final String LX_CHECKOUT_SLIDE_TO_PURCHASE = "App.LX.Checkout.SlideToPurchase";
	private static final String LX_CHECKOUT_CVV_SCREEN = "App.LX.Checkout.Payment.CID";
	private static final String LX_NO_SEARCH_RESULTS = "App.LX.NoResults";

	public static void trackAppLXSearch(LXSearchParams lxSearchParams,
		LXSearchResponse lxSearchResponse) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"" + LX_SEARCH + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_SEARCH);

		// Destination
		s.setProp(4, lxSearchResponse.regionId);
		s.setEvar(4, "D=c4");

		// Success event for Product Search, Local Expert Search
		s.setEvents("event30,event56");

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

	public static void trackAppLXNoSearchResults(ApiError apiError) {
		Log.d(TAG, "Tracking \"" + LX_NO_SEARCH_RESULTS + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_NO_SEARCH_RESULTS);

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

	public static void trackAppLXSearchBox() {
		Log.d(TAG, "Tracking \"" + LX_DESTINATION_SEARCH + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_DESTINATION_SEARCH);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXProductInformation(ActivityDetailsResponse activityDetailsResponse,
		LXSearchParams lxSearchParams) {
		Log.d(TAG, "Tracking \"" + LX_INFOSITE_INFORMATION + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_INFOSITE_INFORMATION);

		s.setEvents("event32");

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
		int selectedTicketsCount, String totalPriceFormattedTo2DecimalPlaces) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_INFO + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_CHECKOUT_INFO);
		s.setEvents("event75");
		s.setProducts(addLXProducts(lxActivityId, totalPriceFormattedTo2DecimalPlaces, selectedTicketsCount));
		setLXDateValues(lxActivityStartDate, s);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutConfirmation(LXCheckoutResponse checkoutResponse,
		String lxActivityId, LocalDate lxActivityStartDate, int selectedTicketsCount) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_CONFIRMATION + "\" pageLoad...");

		ADMS_Measurement s = internalTrackAppLX(LX_CHECKOUT_CONFIRMATION);
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

		setLXDateValues(lxActivityStartDate, s);

		// Send the tracking data
		s.track();
	}

	public static void trackAppLXCheckoutTraveler() {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_TRAVELER_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(LX_CHECKOUT_TRAVELER_INFO);
		s.track();

	}

	public static void trackAppLXCheckoutLoginSuccess() {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_LOGIN_SUCCESS + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(28, LX_CHECKOUT_LOGIN_SUCCESS);
		s.setProp(16, LX_CHECKOUT_LOGIN_SUCCESS);
		s.setEvents("event26");

		s.trackLink(null, "o", "User Login", null, null);
	}

	public static void trackAppLXCheckoutPayment() {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_PAYMENT_INFO + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(LX_CHECKOUT_PAYMENT_INFO);
		s.setEvar(18, LX_CHECKOUT_PAYMENT_INFO);
		s.track();
	}

	public static void trackAppLXCheckoutSlideToPurchase(Context context, CreditCardType creditCardType) {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(LX_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(18, LX_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37,
			creditCardType != CreditCardType.UNKNOWN ? Strings.capitalizeFirstLetter(creditCardType.toString())
				: context.getString(R.string.lx_omniture_checkout_no_credit_card));
		s.track();
	}

	public static void trackAppLXCheckoutCvvScreen() {
		Log.d(TAG, "Tracking \"" + LX_CHECKOUT_CVV_SCREEN + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();

		s.setAppState(LX_CHECKOUT_CVV_SCREEN);
		s.setEvar(18, LX_CHECKOUT_CVV_SCREEN);

		s.track();
	}

	public static String addLXProducts(String activityId, String totalMoney, int ticketCount) {
		return "LX;Merchant LX:" + activityId + ";" + ticketCount + ";" + totalMoney;
	}

	public static void trackLinkLXChangeDate() {
		trackLinkLX(LX_CHANGE_DATE);
	}

	public static void trackLinkLXSelectTicket() {
		trackLinkLX(LX_TICKET_SELECT);
	}

	public static void trackLinkLXAddRemoveTicket(String rffr) {

		StringBuilder sb = new StringBuilder();
		sb.append(LX_TICKET);
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

		trackAbacusTest(s, AbacusUtils.EBAndroidAATest);

		s.track();
	}

	private static void trackAbacusTest(ADMS_Measurement s, int testKey) {
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
			s.setEvents("event74");
			FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
			s.setEvar(47, getEvar47String(params));

			String origin = params.getDepartureLocation().getDestinationId();
			s.setEvar(3, origin);
			s.setProp(3, origin);
			String dest = params.getArrivalLocation().getDestinationId();
			s.setEvar(4, dest);
			s.setProp(4, dest);

			addProducts(s);
			internalSetFlightDateProps(s, params);
			addStandardFlightFields(s);
		}
		else {
			s.setEvents("event70");
			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			s.setEvar(47, getEvar47String(params));
			addHotelRegionId(s, params);
			addProducts(s, Db.getTripBucket().getHotel().getProperty());
			addStandardHotelFields(s, params);
		}
		s.track();
	}

	public static void trackItemSoldOutOnCheckoutLink(LineOfBusiness lob) {
		String soldOutLink = getBase(lob == LineOfBusiness.FLIGHTS) +  ".Checkout.Error";
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
			FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
			FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
			s.setCurrencyCode(trip.getTotalFare().getCurrency());

			addStandardFlightFields(s);
			setEvar30(s, trip, params);

			if (isConfirmation) {
				s.setEvents("purchase");
				addProducts(s);
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

	public static void trackAddLxClick() {
		ADMS_Measurement s = getFreshTrackingObject();
		s.setEvar(28, ADD_ATTACH_LX);
		s.setProp(16, ADD_ATTACH_LX);
		s.setEvar(12, CROSS_SELL_LX_FROM_FLIGHT);
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

	public static void trackItinEmpty() {
		internalTrackPageLoadEventStandard(ITIN_EMPTY);
	}

	public static void trackFindItin() {
		internalTrackPageLoadEventStandard(ITIN_FIND);
	}

	/**
	 * Track the itin card sharing click
	 * @param type which itin card type was being shared
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
	 *
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

		addEvent15And16Maybe(context, s);

		s.track();
	}

	public static void trackItinHotel(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_HOTEL + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(ITIN_HOTEL);
		addEvent15And16Maybe(context, s);
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
		addEvent15And16Maybe(context, s);
		s.track();
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
		addEvent15And16Maybe(context, s);
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
		addEvent15And16Maybe(context, s);
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

	private static void addEvent15And16Maybe(Context context, ADMS_Measurement s) {
		String event = "event15";
		if (!ExpediaNetUtils.isOnline(context)) {
			event += ",event16";
		}
		s.setEvents(event);
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
		s.setEvents("event12");

		s.trackLink(null, "o", link, null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Lean Plum Notification Tracking
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void trackLeanPlumNotification(String campaignText) {
		Log.d(TAG, "Tracking LeanPlumNotification \"" + campaignText + "\"");

		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(11, campaignText);
		s.setEvents("event12");

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

		trackAbacusTest(s, AbacusUtils.EBAndroidAppHotelItinLXXsell);
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

	public static void trackLoginSuccess() {
		ADMS_Measurement s = createTrackLinkEvent(LOGIN_SUCCESS);
		s.setEvents("event26");
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.setEvar(61, Integer.toString(PointOfSale.getPointOfSale().getTpid()));
		s.trackLink(null, "o", "Accounts", null, null);
	}

	public static void trackLoginScreen() {
		ADMS_Measurement s = getFreshTrackingObject();
		// set the pageName
		s.setAppState(LOGIN_SCREEN);
		s.setEvar(18, LOGIN_SCREEN);
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
		s.setProp(2, "storefront");
		s.setEvar(2, "storefront");
		s.track();
	}

	public static void trackPageLoadAbacusTestResults() {
		ADMS_Measurement s = getFreshTrackingObject();
		final String link = "LogExperiement";

		addStandardFields(s);
		trackAbacusTest(s, AbacusUtils.EBAndroidAppLaunchScreenTest);

		s.trackLink(null, "o", link, null, null);
	}

	public static void trackGroundTransportTest() {
		ADMS_Measurement s = getFreshTrackingObject();

		trackAbacusTest(s, AbacusUtils.EBAndroidAppSplitGTandActivities);
		s.track();
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

	// Documentation: https://confluence/display/Omniture/Ad-X+Campaign+Measurement

	private static final String ADX_EVENT = "Ad-X Download";
	private static final String ADX_ORGANIC_EVENT = "Ad-X Organic";
	private static final String ORGANIC_ADX_DOWNLOAD_REFERRAL_STRING = "Mob :: Brand";

	public static void trackAdXReferralLink(String referral) {
		if (ORGANIC_ADX_DOWNLOAD_REFERRAL_STRING.equals(referral)) {
			Log.d(TAG, "Tracking \"" + ADX_ORGANIC_EVENT + "\"");

			ADMS_Measurement s = getFreshTrackingObject();

			s.setEvar(8, referral);

			s.trackLink(null, "o", ADX_ORGANIC_EVENT, null, null);
		}
		else {
			Log.d(TAG, "Tracking \"" + ADX_EVENT + "\"");

			ADMS_Measurement s = getFreshTrackingObject();

			s.setEvar(8, referral);
			s.setEvents("event20");

			s.trackLink(null, "o", ADX_EVENT, null, null);
		}
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
	 *  @param pageName the page name if this is a pageLoad event; for onClick, this should be null
	 * @param events The "events" variable, if one needs to be set.  Can be null.
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

		String evar55 =  User.isLoggedIn(sContext) ? "loggedin | hard" : "unknown user";
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

		// User location
		android.location.Location bestLastLocation = LocationServices.getLastBestLocation(sContext, 0);
		if (bestLastLocation != null) {
			s.setProp(40, bestLastLocation.getLatitude() + "," + bestLastLocation.getLongitude() + "|"
				+ bestLastLocation.getAccuracy());
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
		switch (user.getPrimaryTraveler().getLoyaltyMembershipTier()) {
		case BLUE:
			return "blue";
		case SILVER:
			return "silver";
		case GOLD:
			return "gold";
		default:
			return null;
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
		return ProductFlavorFeatureConfiguration.getInstance().getOmnitureReportSuiteIds();
	}

	private static String getTrackingServer(Context context) {
		EndPoint endpoint = Ui.getApplication(context).appComponent().endpointProvider().getEndPoint();
		if (endpoint == EndPoint.CUSTOM_SERVER) {
			return SettingUtils.get(context, context.getString(R.string.preference_proxy_server_address),
				"localhost:3000");
		}
		else {
			return ProductFlavorFeatureConfiguration.getInstance().getOmnitureTrackingServer();
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
		CreditCardType type;
		if (scc != null) {
			type = scc.getType();
		}
		else {
			type = CurrencyUtils.detectCreditCardBrand(billingInfo.getNumber());
		}

		if (type != null) {
			switch (type) {
			case AMERICAN_EXPRESS:
				return "AmericanExpress";
			case CARTE_BLANCHE:
				return "CarteBlanche";
			case CHINA_UNION_PAY:
				return "ChinaUnionPay";
			case DINERS_CLUB:
				return "DinersClub";
			case DISCOVER:
				return "Discover";
			case JAPAN_CREDIT_BUREAU:
				return "JapanCreditBureau";
			case MAESTRO:
				return "Maestro";
			case MASTERCARD:
				return "MasterCard";
			case VISA:
				return "Visa";
			case GOOGLE_WALLET:
				return "GoogleWallet";
			case CARTE_BLEUE:
				return "CarteBleue";
			case CARTA_SI:
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
	private static final String CAR_CHECKOUT_LOGIN_SUCCESS = "App.Cars.Checkout.Login.Success";
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

	public static void trackAppCarNoResults(String errorMessage) {
		Log.d(TAG, "Tracking \"" + CAR_NO_RESULT + "\" pageLoad...");
		ADMS_Measurement s = internalTrackAppCar(CAR_NO_RESULT);
		s.setProp(36, errorMessage);
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
		trackAbacusTest(s, AbacusUtils.EBAndroidAppCarRatesCollapseTopListing);
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

	public static void trackAppCarCheckoutLoginSuccess() {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_LOGIN_SUCCESS + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();


		s.setEvar(28, CAR_CHECKOUT_LOGIN_SUCCESS);
		s.setProp(16, CAR_CHECKOUT_LOGIN_SUCCESS);
		s.setEvents("event26");

		s.trackLink(null, "o", "User Login", null, null);

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

	public static void trackAppCarCheckoutSlideToPurchase(Context context, CreditCardType creditCardType) {
		Log.d(TAG, "Tracking \"" + CAR_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad...");
		ADMS_Measurement s = getFreshTrackingObject();
		s.setAppState(CAR_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(18, CAR_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37,
			creditCardType != CreditCardType.UNKNOWN ? Strings.capitalizeFirstLetter(creditCardType.toString())
				: context.getString(R.string.car_omniture_checkout_no_credit_card));
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


	public static void trackCheckoutSlideToPurchase(LineOfBusiness lineOfBusiness, Context context, CreditCardType creditCardType) {
		if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
			trackAppCarCheckoutSlideToPurchase(context, creditCardType);
		}
		else if (lineOfBusiness.equals(LineOfBusiness.LX)) {
			trackAppLXCheckoutSlideToPurchase(context, creditCardType);
		}
	}

	public static void trackCheckoutLoginSuccess(LineOfBusiness lineOfBusiness) {
		if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
			trackAppCarCheckoutLoginSuccess();
		}
		else if (lineOfBusiness.equals(LineOfBusiness.LX)) {
			trackAppLXCheckoutLoginSuccess();
		}
	}

	public static void trackCheckoutPayment(LineOfBusiness lineOfBusiness) {
		if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
			trackAppCarCheckoutPayment();
		}
		else if (lineOfBusiness.equals(LineOfBusiness.LX)) {
			trackAppLXCheckoutPayment();
		}
	}

	public static void trackCheckoutTraveler(LineOfBusiness lineOfBusiness) {
		if (lineOfBusiness.equals(LineOfBusiness.CARS)) {
			trackAppCarCheckoutTraveler();
		}
		else if (lineOfBusiness.equals(LineOfBusiness.LX)) {
			trackAppLXCheckoutTraveler();
		}
	}

}
