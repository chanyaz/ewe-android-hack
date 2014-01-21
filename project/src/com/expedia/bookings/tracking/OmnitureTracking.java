package com.expedia.bookings.tracking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.TextUtils;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.adobe.adms.measurement.ADMS_ReferrerHandler;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.PriceRange;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.LocalExpertSite;
import com.expedia.bookings.data.LocalExpertSite.Destination;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
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

	public static void init(Context context) {
		Log.d(TAG, "init");
		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);
		s.configureMeasurement(getReportSuiteIds(context), getTrackingServer());

		sMarketingDate = SettingUtils.get(context, context.getString(R.string.preference_marketing_date),
				sMarketingDate);
	}

	public static void onResume(Activity activity) {
		Log.v(TAG, "onResume");
		ADMS_Measurement measurement = ADMS_Measurement.sharedInstance(activity);
		measurement.startActivity(activity);

	}

	public static void onPause() {
		Log.v(TAG, "onPause");
		ADMS_Measurement measurement = ADMS_Measurement.sharedInstance();
		measurement.stopActivity();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Hotels tracking
	//
	// There does not appear to be an official spec for hotels tracking...
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String HOTELS_ROOMS_RATES = "App.Hotels.RoomsRates";
	private static final String HOTELS_RATE_DETAILS = "App.Hotels.RateDetails";
	private static final String HOTELS_CHECKOUT_INFO = "App.Hotels.Checkout.Info";
	private static final String HOTELS_CHECKOUT_LOGIN = "App.Hotels.Checkout.Login";
	private static final String HOTELS_CHECKOUT_LOGIN_FORGOT = "App.Hotels.Checkout.Login.Forgot";
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
	private static final String HOTELS_SEARCH_REFINE = "App.Hotels.Search.Refine";
	private static final String HOTELS_SEARCH_REFINE_NAME = "App.Hotels.Search.Refine.Name";
	private static final String HOTELS_SEARCH_REFINE_PRICE_RANGE = "App.Hotels.Search.Refine.PriceRange";
	private static final String HOTELS_SEARCH_REFINE_SEARCH_RADIUS = "App.Hotels.Search.Refine.SearchRadius";
	private static final String HOTELS_SEARCH_REFINE_VIP = "App.Hotels.Search.Refine.VIPAccess";
	private static final String HOTELS_COUPON_APPLIED = "Coupon Applied";
	private static final String HOTELS_COUPON_REMOVED = "Coupon Removed";
	private static final String HOTELS_CONF_CROSSSELL_FLIGHTS = "CrossSell.Hotels.Flights";
	private static final String HOTELS_CONF_ADD_TO_CALENDAR = "App.Hotels.Checkout.Confirmation.Add.Calendar";
	private static final String HOTELS_CONF_SHARE_EMAIL = "App.Hotels.Checkout.Confirmation.Share.Mail";

	public static final String HOTELS_SEARCH_SORT_POPULAR = "App.Hotels.Search.Sort.Popular";
	public static final String HOTELS_SEARCH_SORT_PRICE = "App.Hotels.Search.Sort.Price";
	public static final String HOTELS_SEARCH_SORT_DISTANCE = "App.Hotels.Search.Sort.Distance";
	public static final String HOTELS_SEARCH_SORT_RATING = "App.Hotels.Search.Sort.Rating";
	public static final String HOTELS_SEARCH_SORT_DEALS = "App.Hotels.Search.Sort.Deals";

	public static void trackAppHotelsSearchWithoutRefinements(Context context, HotelSearchParams searchParams,
			HotelSearchResponse searchResponse) {
		internalTrackHotelsSearch(context, searchParams, searchResponse, null);
	}

	public static void trackAppHotelsSearch(Context context, HotelSearchParams searchParams,
			HotelSearchParams oldSearchParams,
			HotelFilter filter, HotelFilter oldFilter, HotelSearchResponse searchResponse) {
		String refinements = getHotelSearchRefinements(searchParams, oldSearchParams, filter, oldFilter);
		internalTrackHotelsSearch(context, searchParams, searchResponse, refinements);
	}

	private static void internalTrackHotelsSearch(Context context, HotelSearchParams searchParams,
			HotelSearchResponse searchResponse, String refinements) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"App.Hotels.Search\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState("App.Hotels.Search");

		if (refinements != null) {
			// Whether this was the first search or a refined search
			s.setEvents("event31");

			// Refinement
			s.setEvar(28, refinements);
			s.setProp(16, refinements);
		}
		else {
			s.setEvents("event30");
		}

		// LOB Search
		s.setEvar(2, "hotels");
		s.setProp(2, "hotels");

		// Region
		DecimalFormat df = new DecimalFormat("#.######");
		String region = null;
		if (!TextUtils.isEmpty(searchParams.getQuery())) {
			region = searchParams.getQuery();
		}
		else {
			region = df.format(searchParams.getSearchLatitude()) + "|" + df.format(searchParams.getSearchLongitude());
		}
		s.setEvar(4, region);
		s.setProp(4, region);

		// Check in/check out date
		String days5 = Integer.toString(JodaUtils.daysBetween(LocalDate.now(), searchParams.getCheckInDate()));
		s.setEvar(5, days5);
		s.setProp(5, days5);

		String days6 = Integer.toString(JodaUtils.daysBetween(searchParams.getCheckInDate(),
				searchParams.getCheckInDate()));
		s.setEvar(6, days6);
		s.setProp(6, days6);

		// Number adults searched for
		s.setEvar(47, "A" + searchParams.getNumAdults() + "|C" + searchParams.getNumChildren());

		// Freeform location
		if (!TextUtils.isEmpty(searchParams.getUserQuery())) {
			s.setEvar(48, searchParams.getUserQuery());
		}

		// Number of search results
		if (searchResponse != null && searchResponse.getFilteredAndSortedProperties(searchParams) != null) {
			s.setProp(1, searchResponse.getFilteredAndSortedProperties(searchParams).size() + "");
		}

		// Send the tracking data
		s.track();
	}

	/**
	 * 	If we already have results, check for refinements; if there were none, it's possible
	 * 	that the user just opened/closed a search param change without changing anything.
	 *
	 * 	This is a somewhat lazy way of doing things, but it is easiest and catches a bunch
	 * 	of refinements at once instead of flooding the system with a ton of different refinements
	 *
	 */
	private static String getHotelSearchRefinements(HotelSearchParams searchParams, HotelSearchParams oldSearchParams,
			HotelFilter filter, HotelFilter oldFilter) {
		if (oldFilter != null && oldSearchParams != null) {
			List<String> refinements = new ArrayList<String>();

			// Sort change
			if (oldFilter.getSort() != filter.getSort()) {
				HotelFilter.Sort sort = filter.getSort();
				if (sort == HotelFilter.Sort.POPULAR) {
					refinements.add("App.Hotels.Search.Sort.Popular");
				}
				else if (sort == HotelFilter.Sort.PRICE) {
					refinements.add("App.Hotels.Search.Sort.Price");
				}
				else if (sort == HotelFilter.Sort.DISTANCE) {
					refinements.add("App.Hotels.Search.Sort.Distance");
				}
				else if (sort == HotelFilter.Sort.RATING) {
					refinements.add("App.Hotels.Search.Sort.Rating");
				}
			}

			// Number of travelers change
			if (searchParams.getNumAdults() != oldSearchParams.getNumAdults()
					|| searchParams.getNumChildren() != oldSearchParams.getNumChildren()) {
				refinements.add("App.Hotels.Search.Refine.NumberTravelers");
			}

			// Location change
			// Checks that the search type is the same, or else that a search of a particular type hasn't
			// been modified (e.g., freeform text changing on a freeform search)
			if (!searchParams.equals(oldSearchParams.getSearchType())) {
				refinements.add("App.Hotels.Search.Refine.Location");
			}
			else if (searchParams.getSearchType() == HotelSearchParams.SearchType.MY_LOCATION
					|| searchParams.getSearchType() == HotelSearchParams.SearchType.VISIBLE_MAP_AREA) {
				if (searchParams.getSearchLatitude() != oldSearchParams.getSearchLatitude()
						|| searchParams.getSearchLongitude() != oldSearchParams.getSearchLongitude()) {
					refinements.add("App.Hotels.Search.Refine.Location");
				}
			}
			else {
				if (!searchParams.getQuery().equals(oldSearchParams.getQuery())) {
					refinements.add("App.Hotels.Search.Refine.Location");
				}
			}

			// Checkin date change
			if (!searchParams.getCheckInDate().equals(oldSearchParams.getCheckInDate())) {
				refinements.add("App.Hotels.Search.Refine.CheckinDate");
			}

			// Checkout date change
			if (!searchParams.getCheckOutDate().equals(oldSearchParams.getCheckOutDate())) {
				refinements.add("App.Hotels.Search.Refine.CheckoutDate");
			}

			// Search radius change
			if (filter.getSearchRadius() != oldFilter.getSearchRadius()) {
				refinements.add("App.Hotels.Search.Refine.SearchRadius");
			}

			// Price range change
			if (filter.getPriceRange() != oldFilter.getPriceRange()) {
				refinements.add("App.Hotels.Search.Refine.PriceRange");
			}

			// Star rating change
			double minStarRating = filter.getMinimumStarRating();
			if (minStarRating != oldFilter.getMinimumStarRating()) {
				if (minStarRating == 5) {
					refinements.add("App.Hotels.Search.Refine.AllStars");
				}
				else {
					refinements.add("App.Hotels.Search.Refine." + minStarRating + "Stars");
				}
			}

			boolean hasHotelFilter = filter.getHotelName() != null;
			boolean oldHasHotelFilter = oldFilter.getHotelName() != null;
			if (hasHotelFilter != oldHasHotelFilter
					|| (hasHotelFilter && !filter.getHotelName().equals(oldFilter.getHotelName()))) {
				refinements.add("App.Hotels.Search.Refine.Name");
			}

			int numRefinements = refinements.size();
			if (numRefinements == 0) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			for (int a = 0; a < numRefinements; a++) {
				if (a != 0) {
					sb.append("|");
				}
				sb.append(refinements.get(a));
			}

			return sb.toString();
		}

		return null;
	}

	public static void trackAppHotelsRoomsRates(Context context, Property property, String referrer) {
		Log.d(TAG, "Tracking \"App.Hotels.RoomsRates\" event");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState("App.Hotels.RoomsRates");

		// Promo description
		s.setEvar(9, property.getLowestRate().getPromoDescription());

		// Rating or highly rated
		addHotelRating(s, property);

		// Products
		addProducts(s, property);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutConfirmation(Context context, HotelSearchParams searchParams,
			Property property, BillingInfo billingInfo, Rate rate, BookingResponse response) {
		Log.d(TAG, "Tracking \"App.Hotels.Checkout.Confirmation\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState("App.Hotels.Checkout.Confirmation");

		s.setEvents("purchase");

		// Promo description
		if (rate != null) {
			s.setEvar(9, rate.getPromoDescription());
		}

		// Product details
		DateTimeFormatter dtf = ISODateTimeFormat.basicDate();
		String checkIn = dtf.print(searchParams.getCheckInDate());
		String checkOut = dtf.print(searchParams.getCheckOutDate());
		s.setEvar(30, "Hotel:" + checkIn + "-" + checkOut + ":N");

		// Unique confirmation id
		// 14103: Remove timestamp from the purchaseID variable
		s.setProp(15, response.getItineraryId());
		s.setProp(71, response.getItineraryId());
		s.setProp(72, response.getOrderNumber());
		s.setPurchaseID("onum" + response.getOrderNumber());

		if (billingInfo != null) {
			Location location = billingInfo.getLocation();
			// Not all POS need a location so it is null in some cases
			if (location != null) {
				s.setProp(46, location.getCountryCode());
				s.setGeoState(location.getCountryCode());

				s.setProp(49, location.getPostalCode());
				s.setGeoZip(location.getPostalCode());
			}
		}

		// Products
		int numDays = searchParams.getStayDuration();
		double totalCost = 0;
		if (rate != null && rate.getTotalAmountAfterTax() != null) {
			totalCost = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		}
		addProducts(s, property, numDays, totalCost);

		// Currency code
		s.setCurrencyCode(rate.getTotalAmountAfterTax().getCurrency());

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelsInfosite(Context context, int position) {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState("App.Hotels.Infosite");

		s.setEvents("event32");

		// Rating or highly rated
		Property property = Db.getHotelSearch().getSelectedProperty();
		addHotelRating(s, property);

		// Products
		addProducts(s, property);

		// Position, if opened from list

		if (position != -1) {
			s.setEvar(39, position + "");
		}

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelsInfositeMap(Context context) {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite.Map\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState("App.Hotels.Infosite.Map");

		// Products
		addProducts(s, Db.getHotelSearch().getSelectedProperty());

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelDetails(Context context, Property property) {
		// Track that the full details has a pageload
		Log.d(TAG, "Tracking \"App.Hotels.Details\" pageLoad");

		ADMS_Measurement s = createSimpleEvent(context, "App.Hotels.Details", "event32", null);

		addHotelRating(s, property);

		Rate rate = property.getLowestRate();
		if (rate != null) {
			s.setEvar(9, property.getLowestRate().getPromoDescription());
		}

		s.track();
	}

	public static void trackPageLoadHotelsSearchQuickView(Context context, Property property, String referrer) {
		// Track that the mini details has a pageload
		Log.d(TAG, "Tracking \"App.Hotels.Search.QuickView\" onClick");

		ADMS_Measurement s = createSimpleEvent(context, "App.Hotels.Search.QuickView", null, referrer);

		Rate rate = property.getLowestRate();
		if (rate != null) {
			s.setEvar(9, rate.getPromoDescription());
		}

		s.track();
	}

	private static void addProducts(ADMS_Measurement s, Property property) {
		// The "products" field uses this format:
		// Hotel;<supplier> Hotel:<hotel id>

		// Determine supplier type
		String supplierType = property.getSupplierType();
		s.setProducts("Hotel;" + supplierType + " Hotel:" + property.getPropertyId());
	}

	private static void addProducts(ADMS_Measurement s, Property property, int numNights, double totalCost) {
		addProducts(s, property);

		DecimalFormat df = new DecimalFormat("#.##");
		String products = s.getProducts();
		products += ";" + numNights + ";" + df.format(totalCost);
		s.setProducts(products);
	}

	public static void trackPageLoadHotelsRoomsRates(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_ROOMS_RATES);
	}

	public static void trackPageLoadHotelsRateDetails(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_RATE_DETAILS);
	}

	public static void trackPageLoadHotelsCheckoutInfo(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_INFO);
	}

	public static void trackPageLoadHotelsLogin(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_LOGIN);
	}

	public static void trackLinkHotelRefineName(Context context, String refinement) {
		String link = HOTELS_SEARCH_REFINE_NAME + "." + refinement;
		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefinePriceRange(Context context, PriceRange priceRange) {
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

		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefineSearchRadius(Context context, SearchRadius searchRadius) {
		String link = HOTELS_SEARCH_REFINE_SEARCH_RADIUS;

		if (searchRadius != HotelFilter.SearchRadius.ALL) {
			final DistanceUnit distanceUnit = DistanceUnit.getDefaultDistanceUnit();
			final String unitString = distanceUnit.equals(DistanceUnit.MILES) ? "mi" : "km";

			link += "." + new DecimalFormat("##.#").format(searchRadius.getRadius(distanceUnit)) + unitString;
		}
		else {
			link += ".All";
		}

		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefineRating(Context context, String rating) {
		String link = HOTELS_SEARCH_REFINE + "." + rating;
		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefineVip(Context context, boolean enabled) {
		String pageName = enabled ? HOTELS_SEARCH_REFINE_VIP + ".On" : HOTELS_SEARCH_REFINE_VIP + ".Off";
		internalTrackLink(context, pageName);
	}

	public static void trackLinkHotelSort(Context context, String pageName) {
		internalTrackLink(context, pageName);
	}

	public static void trackLinkHotelsCheckoutLoginForgot(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_LOGIN_FORGOT);
	}

	public static void trackPageLoadHotelsTravelerEditInfo(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_TRAVELER_EDIT_INFO);
	}

	public static void trackPageLoadHotelsTravelerSelect(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_TRAVELER_SELECT);
	}

	public static void trackLinkHotelsCheckoutTravelerEnterManually(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	public static void trackPageLoadHotelsCheckoutPaymentSelect(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPageLoadHotelsCheckoutPaymentEditCard(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_EDIT_CARD);
	}

	public static void trackPageLoadHotelsCheckoutPaymentEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_EDIT_SAVE);
	}

	public static void trackLinkHotelsCheckoutPaymentSelectExisting(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkHotelsCheckoutPaymentEnterManually(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	public static void trackPageLoadHotelsCheckoutSlideToPurchase(Context context) {
		Log.d(TAG, "Tracking \"" + HOTELS_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, HOTELS_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, getPaymentType(context));
		s.track();
	}

	public static void trackPageLoadHotelsCheckoutWarsaw(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_WARSAW);
	}

	public static void trackPageLoadHotelsCheckoutPaymentCid(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_CID);
	}

	// Coupon tracking: https://mingle/projects/eb_ad_app/cards/1003

	public static void trackHotelCouponApplied(Context context, String couponCode) {
		Log.d(TAG, "Tracking \"" + HOTELS_COUPON_APPLIED + "\" click");
		ADMS_Measurement s = getFreshTrackingObject(context);
		addStandardFields(context, s);
		s.setEvents("event21");
		s.setEvar(24, couponCode);
		s.trackLink(null, "o", HOTELS_COUPON_APPLIED, null, null);
	}

	public static void trackHotelCouponRemoved(Context context) {
		Log.d(TAG, "Tracking \"" + HOTELS_COUPON_REMOVED + "\" click");
		ADMS_Measurement s = getFreshTrackingObject(context);
		addStandardFields(context, s);
		s.setEvar(24, HOTELS_COUPON_REMOVED);
		s.trackLink(null, "o", HOTELS_COUPON_REMOVED, null, null);
	}

	public static void trackHotelConfirmationFlightsXSell(Context context) {
		ADMS_Measurement s = createTrackLinkEvent(context, HOTELS_CONF_CROSSSELL_FLIGHTS);
		s.setEvar(12, HOTELS_CONF_CROSSSELL_FLIGHTS);
		internalTrackLink(s);
	}

	public static void trackHotelConfirmationAddToCalendar(Context context) {
		internalTrackLink(context, HOTELS_CONF_ADD_TO_CALENDAR);
	}

	public static void trackHotelConfirmationShareEmail(Context context) {
		internalTrackLink(context, HOTELS_CONF_SHARE_EMAIL);
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
	private static final String FLIGHT_CHECKOUT_LOGIN_FORGOT = "App.Flight.Checkout.Login.Forgot";
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
	private static final String PREFIX_FLIGHT_SEARCH_ONE_WAY_SORT = "App.Flight.Search.OneWay.Sort";
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
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT = "App.Flight.Search.Roundtrip.Out.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE = "App.Flight.Search.Roundtrip.Out.RefineSearch";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT = "App.Flight.Search.Roundtrip.In.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT = "App.Flight.Search.Roundtrip.In.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE = "App.Flight.Search.Roundtrip.In.RefineSearch";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT = "App.Flight.Search.Roundtrip.In.RemoveOut";

	private static final String FLIGHT_CONF_ADD_TO_CALENDAR = "App.Flights.Checkout.Confirmation.Add.Calendar";
	private static final String FLIGHT_CONF_SHARE_EMAIL = "App.Flights.Checkout.Confirmation.Share.Mail";

	public static void trackPageLoadFlightCheckoutConfirmation(Context context) {
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_CONFIRMATION + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, FLIGHT_CHECKOUT_CONFIRMATION);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// Flight: <departure Airport Code>-<Destination Airport Code>:<departure date YYYYMMDD>-<return date YYYYMMDD>:<promo code applied N/Y>
		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();
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

		// products variable, described here: http://confluence/display/Omniture/Product+string+format
		String airlineCode = trip.getLeg(0).getPrimaryAirlines().iterator().next();
		String tripType = getOmnitureStringCodeRepresentingTripTypeByNumLegs(trip.getLegCount());
		String numTravelers = Integer.toString(Db.getFlightSearch().getSearchParams().getNumAdults());
		String price = trip.getTotalFare().getAmount().toString();

		s.setProducts("Flight;Agency Flight:" + airlineCode + ":" + tripType + ";" + numTravelers + ";" + price);

		s.setCurrencyCode(trip.getTotalFare().getCurrency());
		s.setEvents("purchase");

		// order number with an "onum" prefix, described here: http://confluence/pages/viewpage.action?pageId=419913476
		final String orderId = Db.getFlightCheckout().getOrderId();
		s.setPurchaseID("onum" + orderId);

		// TRL
		Itinerary itin = Db.getItinerary(trip.getItineraryNumber());
		s.setProp(71, itin.getItineraryNumber());

		// order #
		s.setProp(72, orderId);

		s.track();
	}

	public static void trackPageLoadFlightCheckoutPaymentCid(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_CID);
	}

	public static void trackPageLoadFlightCheckoutSlideToPurchase(Context context) {
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE);
		s.setEvar(37, getPaymentType(context));
		s.track();
	}

	public static void trackPageLoadFlightCheckoutPaymentEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditCard(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditAddress(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS);
	}

	public static void trackPageLoadFlightCheckoutPaymentSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_SELECT);
	}

	public static void trackPageLoadFlightCheckoutWarsaw(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_WARSAW);
	}

	public static void trackPageLoadFlightTravelerEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE);
	}

	public static void trackPageLoadFlightTravelerEditPassport(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT);
	}

	public static void trackPageLoadFlightTravelerEditDetails(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS);
	}

	public static void trackPageLoadFlightTravelerEditInfo(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO);
	}

	public static void trackPageLoadFlightTravelerSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_SELECT);
	}

	public static void trackPageLoadFlightLogin(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_LOGIN);
	}

	public static void trackPageLoadFlightCheckoutInfo(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_INFO);
	}

	public static void trackPageLoadFlightRateDetailsOverview(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_RATE_DETAILS);
	}

	private static boolean mTrackNewSearchResultSet;

	/**
	 * This method is used in the client and internally as bookkeeping to ensure that the special events, outbound list
	 * and oneway list of search results get tracked once and only once for a given set of search parameters. Clients
	 * will use this method with 'true' when performing a new search, and internally this method is used with 'false' to
	 * disallow additional tracking events after the first event has been tracked (until explicitly set true by client).
	 *
	 * Most importantly, this keeps the silly Omniture tracking details (mostly) out of client code and in this class.
	 *
	 * @param markTrackNewSearchResultSet
	 */
	public static void markTrackNewSearchResultSet(boolean markTrackNewSearchResultSet) {
		mTrackNewSearchResultSet = markTrackNewSearchResultSet;
	}

	public static void trackPageLoadFlightSearchResults(Context context, int legPosition) {
		if (legPosition == 0) {
			// Note: according the spec we want only to track the FlightSearchResults if it represents a new set of data
			if (mTrackNewSearchResultSet) {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					OmnitureTracking.trackPageLoadFlightSearchResultsOutboundList(context);
				}
				else {
					OmnitureTracking.trackPageLoadFlightSearchResultsOneWay(context);
				}
			}
		}

		// According to spec, we want to track the inbound list as many times as the user rotates device, etc...
		else if (legPosition == 1) {
			OmnitureTracking.trackPageLoadFlightSearchResultsInboundList(context);
		}
	}

	private static void trackPageLoadFlightSearchResultsOutboundList(Context context) {
		markTrackNewSearchResultSet(false);

		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_ROUNDTRIP_OUT + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(context, FLIGHT_SEARCH_ROUNDTRIP_OUT);

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
		LocalDate now = LocalDate.now();

		// num days between current day (now) and flight departure date
		String numDaysOut = Integer.toString(JodaUtils.daysBetween(now, departureDate));
		s.setEvar(5, numDaysOut);
		s.setProp(5, numDaysOut);

		// num days between departure and return dates
		String numDays = Integer.toString(JodaUtils.daysBetween(departureDate, returnDate));
		s.setEvar(6, numDays);
		s.setProp(6, numDays);

		s.setEvar(47, getEvar47String(searchParams));

		// Success event for 'Search'
		s.setEvents("event30");

		s.track();
	}

	private static void trackPageLoadFlightSearchResultsInboundList(Context context) {
		if (mTrackPageLoadFromFSRA) {
			internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN);
		}
	}

	private static void trackPageLoadFlightSearchResultsOneWay(Context context) {
		markTrackNewSearchResultSet(false);

		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_RESULTS_ONE_WAY + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventBase(context, FLIGHT_SEARCH_RESULTS_ONE_WAY);

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
		LocalDate now = LocalDate.now();

		// num days between current day (now) and flight departure date
		String daysOut = Integer.toString(JodaUtils.daysBetween(now, departureDate));
		s.setEvar(5, daysOut);
		s.setProp(5, daysOut);

		s.setEvar(47, getEvar47String(searchParams));

		// Success event for 'Search'
		s.setEvents("event30");

		s.track();
	}

	public static void trackPageLoadFlightBaggageFeeOneWay(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE);
	}

	public static void trackPageLoadFlightBaggageFeeOutbound(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE);
	}

	public static void trackPageLoadFlightBaggageFeeInbound(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE);
	}

	private static boolean mTrackPageLoadFromFSRA = true;

	public static void setPageLoadTrackingFromFSRAEnabled(boolean trackingEnabled) {
		Log.d("OmnitureTracking", "set FSRA tracking: " + trackingEnabled);
		mTrackPageLoadFromFSRA = trackingEnabled;
	}

	public static void trackPageLoadFlightSearchResultsDetails(Context context, int legPosition) {
		if (mTrackPageLoadFromFSRA) {
			if (legPosition == 0) {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS);
				}
				else {
					internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ONE_WAY_DETAILS);
				}
			}
			else if (legPosition == 1) {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS);

			}
		}
	}

	public static void trackPageLoadFlightSearchResultsPlaneLoadingFragment(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_INTERSTITIAL);
	}

	public static void trackPageLoadFlightSearch(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH);
	}

	public static void trackLinkFlightSearchSelect(Context context, int selectPos, int legPos) {
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

		internalTrackLink(context, link);
	}

	public static void trackLinkFlightRefine(Context context, int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE);
			}
			else {
				internalTrackLink(context, FLIGHT_SEARCH_ONE_WAY_REFINE);
			}
		}
		else if (legPosition == 1) {
			internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE);
		}
	}

	public static void trackLinkFlightSort(Context context, String sortType, int legPosition) {
		String prefix = "";

		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT;
			}
			else {
				prefix = PREFIX_FLIGHT_SEARCH_ONE_WAY_SORT;
			}
		}
		else if (legPosition == 1) {
			prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT;
		}

		String link = prefix + "." + sortType;
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightRemoveOutboundSelection(Context context) {
		internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT);
	}

	public static void trackLinkFlightCheckoutLoginForgot(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_LOGIN_FORGOT);
	}

	public static void trackLinkFlightCheckoutTravelerSelectExisting(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutTravelerEnterManually(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	public static void trackLinkFlightCheckoutPaymentSelectExisting(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutPaymentEnterManually(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	public static void trackErrorPageLoadFlightUnsupportedPOS(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_NOT_YET_AVAILABLE);
	}

	public static void trackErrorPageLoadFlightCheckout(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_ERROR_CHECKOUT);
	}

	public static void trackErrorPageLoadFlightPriceChangeTicket(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET);
	}

	public static void trackErrorPageLoadFlightPaymentFailed(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED);
	}

	public static void trackErrorPageLoadFlightIncorrectCVV(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV);
	}

	public static void trackErrorPageLoadFlightSoldOut(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_SOLD_OUT);
	}

	public static void trackErrorPageLoadFlightSearchExpired(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_SEARCH_EXPIRED);
	}

	public static void trackFlightConfirmationAddToCalendar(Context context) {
		internalTrackLink(context, FLIGHT_CONF_ADD_TO_CALENDAR);
	}

	public static void trackFlightConfirmationShareEmail(Context context) {
		internalTrackLink(context, FLIGHT_CONF_SHARE_EMAIL);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Samsung Wallet Click Tracking
	//

	private static final String SAMSUNG_WALLET = "App.Hotels.Checkout.Confirmation.SamsungWallet";

	public static void trackSamsungWalletDownloadShown(Context context) {
		internalTrackSamsungWallet(context, "Download.Shown");
	}

	public static void trackSamsungWalletDownloadClicked(Context context) {
		internalTrackSamsungWallet(context, "Download.Clicked");
	}

	public static void trackSamsungWalletLoadShown(Context context) {
		internalTrackSamsungWallet(context, "Load.Shown");
	}

	public static void trackSamsungWalletLoadClicked(Context context) {
		internalTrackSamsungWallet(context, "Load.Clicked");
	}

	public static void trackSamsungWalletViewShown(Context context) {
		internalTrackSamsungWallet(context, "View.Shown");
	}

	public static void trackSamsungWalletViewClicked(Context context) {
		internalTrackSamsungWallet(context, "View.Clicked");
	}

	private static void internalTrackSamsungWallet(Context context, String which) {
		internalTrackLink(context, SAMSUNG_WALLET + "." + which);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itin Tracking
	//
	// Spec: https://confluence/display/Omniture/App+Itinerary
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String ITIN_EMPTY = "App.Itinerary.Empty";
	private static final String ITIN_ADD_SUCCESS = "App.Itinerary.Add.Success";
	private static final String ITIN = "App.Itinerary";
	private static final String ITIN_HOTEL = "App.Itinerary.Hotel";
	private static final String ITIN_HOTEL_DIRECTIONS = "App.Itinerary.Hotel.Directions";
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
	private static final String ITIN_LOCAL_EXPERT = "App.Itinerary.LocalExpert";

	public static void trackItinEmpty(Context context) {
		internalTrackPageLoadEventStandard(context, ITIN_EMPTY);
	}

	/**
	 * Track the itin card sharing click
	 * @param context
	 * @param type which itin card type was being shared
	 * @param isLongMessage true denotes it was a share message long, false denotes share message short
	 */
	public static void trackItinShare(Context context, Type type, boolean isLongMessage) {
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

		internalTrackLink(context, pageName);
	}

	public static void trackItinReload(Context context, Type type) {
		String value = type.toString();
		String formatted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
		internalTrackLink(context, String.format(ITIN_RELOAD_TEMPLATE, formatted));
	}

	/**
	 * The new style of tracking "shared itins" via shareable urls.
	 * https://confluence/display/Omniture/Itinerary+Sharing
	 */
	public static void trackItinShareNew(Context context, Type type, Intent intent) {
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
			itinType = "Hotel";
		}
		else {
			boolean isLong = shareType.equals("Mail") ? true : false;
			trackItinShare(context, type, isLong);
			return;
		}

		String pageName = ITIN + "." + itinType + ".Share." + shareType;

		ADMS_Measurement s = createTrackLinkEvent(context, pageName);
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
	private static Set<String> KNOWN_DEEP_LINK_ARGS = new HashSet<String>() {
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

	public static void trackItinAdd(Context context, Trip trip) {
		boolean track = mPendingManualAddGuestItin != null && mPendingManualAddGuestItin.isSameGuest(trip);
		if (track) {
			mPendingManualAddGuestItin = null;
			internalTrackLink(context, ITIN_ADD_SUCCESS);
		}
	}

	public static void trackItin(Context context, String localExpertDests) {
		Log.d(TAG, "Tracking \"" + ITIN + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, ITIN);

		addEvent15And16Maybe(context, s);

		if (!TextUtils.isEmpty(localExpertDests)) {
			s.setEvents(s.getEvents() + ",event6");

			String rfrrId = "App.Itinerary.LocalExpert." + localExpertDests;
			s.setProp(16, rfrrId);
			s.setEvar(28, rfrrId);
		}

		s.track();
	}

	public static void trackItinHotel(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_HOTEL + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, ITIN_HOTEL);
		addEvent15And16Maybe(context, s);
		s.track();
	}

	public static void trackItinHotelDirections(Context context) {
		internalTrackLink(context, ITIN_HOTEL_DIRECTIONS);
	}

	public static void trackItinHotelCall(Context context) {
		internalTrackLink(context, ITIN_HOTEL_CALL);
	}

	public static void trackItinHotelInfo(Context context) {
		internalTrackLink(context, ITIN_HOTEL_INFO);
	}

	public static void trackItinInfoClicked(Context context, Type type) {
		switch (type) {
		case ACTIVITY:
			trackItinActivityInfo(context);
			break;
		case CAR:
			trackItinCarInfo(context);
			break;
		case FLIGHT:
			trackItinFlightInfo(context);
			break;
		case HOTEL:
			trackItinHotelInfo(context);
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
		ADMS_Measurement s = createTrackPageLoadEventBase(context, ITIN_FLIGHT);
		addEvent15And16Maybe(context, s);
		s.track();
	}

	public static void trackItinFlightDirections(Context context) {
		internalTrackLink(context, ITIN_FLIGHT_DIRECTIONS);
	}

	public static void trackItinFlightTerminalMaps(Context context) {
		internalTrackLink(context, ITIN_FLIGHT_TERMINAL_MAPS);
	}

	public static void trackItinFlightInfo(Context context) {
		internalTrackLink(context, ITIN_FLIGHT_INFO);
	}

	public static void trackItinFlightCopyPNR(Context context) {
		internalTrackLink(context, ITIN_FLIGHT_COPY_PNR);
	}

	public static void trackItinCar(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_CAR + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, ITIN_CAR);
		addEvent15And16Maybe(context, s);
		s.track();
	}

	public static void trackItinCarDirections(Context context) {
		internalTrackLink(context, ITIN_CAR_DIRECTIONS);
	}

	public static void trackItinCarCall(Context context) {
		internalTrackLink(context, ITIN_CAR_CALL);
	}

	public static void trackItinCarInfo(Context context) {
		internalTrackLink(context, ITIN_CAR_INFO);
	}

	public static void trackItinActivity(Context context) {
		Log.d(TAG, "Tracking \"" + ITIN_ACTIVITY + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, ITIN_ACTIVITY);
		addEvent15And16Maybe(context, s);
		s.track();
	}

	public static void trackItinActivityRedeem(Context context) {
		internalTrackLink(context, ITIN_ACTIVITY_REDEEM);
	}

	public static void trackItinActivitySupport(Context context) {
		internalTrackLink(context, ITIN_ACTIVITY_SUPPORT);
	}

	public static void trackItinActivityInfo(Context context) {
		internalTrackLink(context, ITIN_ACTIVITY_INFO);
	}

	public static void trackItinLocalExpertHide(Context context, Destination destination) {
		internalTrackLink(context, "App.Itinerary.LocalExpert." + destination.getTrackingId() + ".Hide");
	}

	public static void trackItinLocalExpertHideForever(Context context, Destination destination) {
		internalTrackLink(context, "App.Itinerary.LocalExpert." + destination.getTrackingId() + ".NeverShowAgain");
	}

	public static void trackItinLocalExpertHideCancel(Context context, Destination destination) {
		internalTrackLink(context, "App.Itinerary.LocalExpert." + destination.getTrackingId() + ".Cancel");
	}

	public static void trackLocalExpert(Context context, LocalExpertSite site) {
		Log.d(TAG, "Tracking \"" + ITIN_LOCAL_EXPERT + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, ITIN_LOCAL_EXPERT);

		s.setEvents("event7");

		String rfrrId = "App.Itinerary.LocalExpert." + site.getTrackingId();
		s.setProp(16, rfrrId);
		s.setEvar(28, rfrrId);

		s.track();
	}

	public static void trackLocalExpertCall(Context context, LocalExpertSite site) {
		ADMS_Measurement s = createTrackLinkEvent(context, "App.Itinerary.LocalExpert." + site.getTrackingId()
				+ ".Call");
		s.setEvents("event8");
		internalTrackLink(s);
	}

	private static void addEvent15And16Maybe(Context context, ADMS_Measurement s) {
		String event = "event15";
		if (!NetUtils.isOnline(context)) {
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
	private static final String NOTIFICATION_FLIGHT_CANCELLED = "Itinerary.Flight.Cancelled";
	private static final String NOTIFICATION_FLIGHT_GATE_TIME_CHANGE = "Itinerary.Flight.GateTimeChange";
	private static final String NOTIFICATION_FLIGHT_GATE_NUMBER_CHANGE = "Itinerary.Flight.GateNumberChange";
	private static final String NOTIFICATION_FLIGHT_BAGGAGE_CLAIM = "Itinerary.Flight.BaggageClaim";
	private static final String NOTIFICATION_HOTEL_CHECK_IN = "Itinerary.Hotel.CheckIn";
	private static final String NOTIFICATION_HOTEL_CHECK_OUT = "Itinerary.Hotel.CheckOut";
	private static final String NOTIFICATION_FLIGHT_DEPARTURE_REMINDER = "Itinerary.Flight.DepartureReminder";

	public static void trackNotificationClick(Context context, Notification notification) {
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
		default:
			link = "Itinerary." + type.name();
			Log.w(TAG, "Unknown Notification Type \"" + type.name() + "\". Taking a guess.");
			break;
		}

		Log.d(TAG, "Tracking \"" + link + "\" click");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setEvar(11, link);
		s.setEvents("event12");

		s.trackLink(null, "o", link, null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Itinerary Notification Click Tracking
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String CROSS_SELL_ITIN_TO_HOTEL = "CrossSell.Itinerary.Hotels";
	private static final String CROSS_SELL_FLIGHT_TO_HOTEL = "CrossSell.Flights.Hotels";

	public static void trackCrossSellItinToHotel(Context context) {
		trackCrossSell(context, CROSS_SELL_ITIN_TO_HOTEL);
	}

	public static void trackCrossSellFlightToHotel(Context context) {
		trackCrossSell(context, CROSS_SELL_FLIGHT_TO_HOTEL);
	}

	private static void trackCrossSell(Context context, String link) {
		Log.d(TAG, "Tracking \"" + link + "\"");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setEvar(12, link);

		s.trackLink(null, "o", link, null, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Other tracking events
	//
	// This is the place for tracking events that don't quite fit within the hotels/flights/itin confines. Right now,
	// contains tracking events for the launch screen, login, launching of app, ad campaigns, etc...
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String LAUNCH_SCREEN = "App.LaunchScreen";
	private static final String LOGIN_SUCCESS_TEMPLATE = "App.%s.Login.Success";
	private static final String ITIN_LOGIN_PARAM = "Itinerary";
	private static final String HOTEL_LOGIN_PARAM = "Hotels.Checkout";
	private static final String FLIGHT_LOGIN_PARAM = "Flight.Checkout";

	public static void trackLoginSuccess(Context ctx, LineOfBusiness lob, boolean loggedInWithFb, boolean isRewards) {
		// Construct the pageName via LOB
		String lobParam;
		switch (lob) {
		case ITIN:
			lobParam = ITIN_LOGIN_PARAM;
			break;
		case FLIGHTS:
			lobParam = FLIGHT_LOGIN_PARAM;
			break;
		case HOTELS:
			lobParam = HOTEL_LOGIN_PARAM;
			break;
		default:
			// Should never get here, but no sense in crashing the app over tracking
			lobParam = HOTEL_LOGIN_PARAM;
			break;
		}
		String pageName = String.format(LOGIN_SUCCESS_TEMPLATE, lobParam);
		ADMS_Measurement s = createTrackLinkEvent(ctx, pageName);

		String var55;
		if (loggedInWithFb) {
			var55 = "Facebook";
		}
		else {
			var55 = "Registered";
		}

		if (isRewards) {
			var55 += " Rewards";
		}

		s.setEvar(55, var55);
		s.setEvents("event26");

		internalTrackLink(s);
	}

	public static void trackLinkLaunchScreenToHotels(Context context) {
		String link = LAUNCH_SCREEN + "." + "Hotel";
		internalTrackLink(context, link);
	}

	public static void trackLinkLaunchScreenToFlights(Context context) {
		String link = LAUNCH_SCREEN + "." + "Flight";
		internalTrackLink(context, link);
	}

	public static void trackPageLoadLaunchScreen(Context context) {
		internalTrackPageLoadEventStandard(context, LAUNCH_SCREEN);
	}

	public static void trackCrash(Context context, Throwable ex) {
		// Log the crash
		Log.d(TAG, "Tracking \"crash\" onClick");
		ADMS_Measurement s = getFreshTrackingObject(context);
		addStandardFields(context, s);
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

	public static void trackAppLaunch(Context context) {
		Log.d(TAG, "Tracking \"App Launch\" pageLoad");
		ADMS_Measurement s = getFreshTrackingObject(context);
		s.setEvar(10, sMarketingDate);
		s.setEvar(27, "App Launch");
		s.track();
	}

	private static final String TRACK_VERSION = "tracking_version"; // The SettingUtils key for the last version tracked

	public static void trackAppLoading(Context context) {
		Log.d(TAG, "Tracking \"App.Loading\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState("App.Loading");

		// Determine if this is a new install, an upgrade, or just a regular launch
		String trackVersion = SettingUtils.get(context, TRACK_VERSION, null);
		String currentVersion = AndroidUtils.getAppVersion(context);

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
		else {
			// Regular launch
			s.setEvents("event27");
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

		ADMS_Measurement s = getFreshTrackingObject(context);

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

	public static void trackAdXReferralLink(Context context, String referral) {
		if (ORGANIC_ADX_DOWNLOAD_REFERRAL_STRING.equals(referral)) {
			Log.d(TAG, "Tracking \"" + ADX_ORGANIC_EVENT + "\"");

			ADMS_Measurement s = getFreshTrackingObject(context);

			addStandardFields(context, s);
			s.setEvar(8, referral);

			s.trackLink(null, "o", ADX_ORGANIC_EVENT, null, null);
		}
		else {
			Log.d(TAG, "Tracking \"" + ADX_EVENT + "\"");

			ADMS_Measurement s = getFreshTrackingObject(context);
			addStandardFields(context, s);

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
	 *
	 * @param context the context
	 * @param pageName the page name if this is a pageLoad event; for onClick, this should be null
	 * @param events The "events" variable, if one needs to be set.  Can be null.
	 * @param referrerId The "referrer" for an event.  Typically this is the name of the onClick event.
	 */
	public static void trackSimpleEvent(Context context, String pageName, String events, String referrerId) {
		ADMS_Measurement s = createSimpleEvent(context, pageName, events, referrerId);

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
	public static void trackErrorPage(Context context, String errorName) {
		Log.d("Tracking \"App.Error." + errorName + "\" pageLoad.");
		trackSimpleEvent(context, "App.Error." + errorName, "event38", null);
	}

	private static void trackOnClick(ADMS_Measurement s) {
		internalTrackLink(s);
	}

	private static ADMS_Measurement createSimpleEvent(Context context, String pageName, String events, String referrerId) {
		ADMS_Measurement s = OmnitureTracking.getFreshTrackingObject(context);

		addStandardFields(context, s);

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
	private static ADMS_Measurement getFreshTrackingObject(Context context) {
		ADMS_Measurement a = ADMS_Measurement.sharedInstance(context);
		a.clearVars();
		return a;
	}

	private static void internalTrackPageLoadEventStandard(Context context, String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventBase(context, pageName).track();
	}

	private static void internalTrackPageLoadEventPriceChange(Context context, String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventPriceChange(context, pageName).track();
	}

	private static void internalTrackLink(Context context, String link) {
		ADMS_Measurement s = createTrackLinkEvent(context, link);
		internalTrackLink(s);
	}

	private static void internalTrackLink(ADMS_Measurement s) {
		Log.d(TAG, "Tracking \"" + s.getProp(16) + "\" linkClick");
		s.trackLink(null, "o", s.getEvar(28), null, null);
	}

	private static ADMS_Measurement createTrackLinkEvent(Context context, String link) {
		ADMS_Measurement s = getFreshTrackingObject(context);

		addStandardFields(context, s);

		// link
		s.setEvar(28, link);
		s.setProp(16, link);

		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventBase(Context context, String pageName) {
		ADMS_Measurement s = getFreshTrackingObject(context);

		// set the pageName
		s.setAppState(pageName);
		s.setEvar(18, pageName);

		addStandardFields(context, s);

		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventPriceChange(Context context, String pageName) {
		ADMS_Measurement s = createTrackPageLoadEventBase(context, pageName);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// This is only to be included when there is a price change shown on the page. This should be the % increase or
		// decrease in price. Round to whole integers.
		String priceChange = trip.computePercentagePriceChangeForOmnitureTracking();
		if (priceChange != null) {
			s.setEvents("event62");
			s.setProp(9, priceChange);
		}

		return s;
	}

	private static void addStandardFields(Context context, ADMS_Measurement s) {
		// Add debugging flag if not release
		if (!AndroidUtils.isRelease(context) || DebugUtils.isLogEnablerInstalled(context)) {
			s.setDebugLogging(true);
		}

		// Add offline tracking, so user doesn't have to be online to be tracked
		s.setOfflineTrackingEnabled(true);

		// account
		s.setReportSuiteIDs(getReportSuiteIds(context));

		// Marketing date tracking
		s.setEvar(10, sMarketingDate);

		// Deep Link tracking
		addDeepLinkData(s);

		// Server
		s.setTrackingServer(getTrackingServer());
		s.setSSL(false);

		// Add the country locale
		s.setEvar(31, Locale.getDefault().getCountry());

		// Experience segmentation
		boolean usingTabletInterface = (ExpediaBookingApp.useTabletInterface(context));
		s.setEvar(50, (usingTabletInterface) ? "app.tablet.android" : "app.phone.android");

		// TPID
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		// Unique device id
		String id = Installation.id(context);
		if (id != null) {
			s.setProp(12, md5(id));
		}

		// Device local time
		s.setEvar(60, sFormatter.print(DateTime.now()));

		// App version
		s.setProp(35, AndroidUtils.getAppVersion(context));

		// Language/locale
		s.setProp(37, Locale.getDefault().getLanguage());

		String email = null;

		// If the user is logged in, we want to send their email address along with request
		if (User.isLoggedIn(context)) {
			// Load the user into the Db if it has not been done (which will most likely be the case on app launch)
			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null) {
				email = Db.getUser().getPrimaryTraveler().getEmail();
			}
		}

		// If the email is still null, check against the BillingInfo in Db which is populated from manual forms
		if (TextUtils.isEmpty(email)) {
			if (Db.loadBillingInfo(context)) {
				if (Db.hasBillingInfo()) {
					email = Db.getBillingInfo().getEmail();
				}
			}
		}

		if (!TextUtils.isEmpty(email)) {
			s.setProp(11, md5(email));
		}

		// Screen orientation
		Configuration config = context.getResources().getConfiguration();
		switch (config.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			s.setProp(39, "landscape");
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			s.setProp(39, "portrait");
			break;
		case Configuration.ORIENTATION_SQUARE:
			s.setProp(39, "square");
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			s.setProp(39, "undefined");
			break;
		}

		// User location
		android.location.Location bestLastLocation = LocationServices.getLastBestLocation(context, 0);
		if (bestLastLocation != null) {
			s.setProp(40, bestLastLocation.getLatitude() + "," + bestLastLocation.getLongitude() + "|"
					+ bestLastLocation.getAccuracy());
		}
	}

	private static void addDeepLinkData(ADMS_Measurement s) {
		if (sDeepLinkKey != null && sDeepLinkValue != null) {
			String var;
			boolean useEvar22 = true;

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
				useEvar22 = false;
				var = "SEO.";
			}
			else {
				Log.w(TAG, "Received Deep Link tracking parameters we don't know how to handle. Ignoring");
				sDeepLinkKey = null;
				sDeepLinkValue = null;
				return;
			}

			int evar = useEvar22 ? 22 : 27;
			var += sDeepLinkValue;
			s.setEvar(evar, var);

			sDeepLinkKey = null;
			sDeepLinkValue = null;
		}
	}

	private static void addHotelRating(ADMS_Measurement s, Property property) {
		s.setProp(38, property.getAverageExpediaRating() + "");
	}

	private static String getReportSuiteIds(Context context) {
		String id;
		boolean usingTabletInterface = (ExpediaBookingApp.useTabletInterface(context));
		if (AndroidUtils.isRelease(context)) {
			id = (usingTabletInterface) ? "expedia1tabletandroid" : "expedia1androidcom";
			if (ExpediaBookingApp.IS_VSC) {
				id += ",expedia7androidapp";
			}
			else {
				id += ",expediaglobalapp";
			}

		}
		else {
			id = (usingTabletInterface) ? "expedia1tabletandroiddev" : "expedia1androidcomdev";
			if (ExpediaBookingApp.IS_VSC) {
				id += ",expedia7androidappdev";
			}
			else {
				id += ",expediaglobalappdev";
			}
		}
		return id;
	}

	private static String getTrackingServer() {
		return "om.expedia.com";
	}

	private static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

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
		// TODO update for when we add children support
		String str = "FLT|";
		if (params.isRoundTrip()) {
			str += "RT|A";
		}
		else {
			str += "OW|A";
		}

		str += params.getNumAdults();
		str += "|C0";

		return str;
	}

	private static String getPaymentType(Context context) {
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
}
