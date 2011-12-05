package com.expedia.bookings.tracking;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Application;
import android.content.Context;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.BookingInfoValidation;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

/**
 * Contains specific events to track.
 */
public class Tracker {

	// The SettingUtils key for the last version tracked
	private static final String TRACK_VERSION = "tracking_version";

	public static void trackAppLoading(final Context context) {
		Log.d("Tracking \"App.Loading\" pageLoad...");

		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		s.pageName = "App.Loading";

		// Determine if this is a new install, an upgrade, or just a regular launch
		String trackVersion = SettingUtils.get(context, TRACK_VERSION, null);
		String currentVersion = AndroidUtils.getAppVersion(context);

		// Start a background thread to do conversion tracking
		new Thread(new Runnable() {
			public void run() {
				// Millennial tracking (possibly)
				if (!MillennialTracking.hasTrackedMillennial(context) && NetUtils.isOnline(context)) {
					MillennialTracking.trackConversion(context);
				}

				// GreyStripe tracking
				GreystripeTracking.trackDownload(context);
			}
		}).start();

		boolean save = false;
		if (trackVersion == null) {
			// New install
			s.events = "event28";
			save = true;
		}
		else if (!trackVersion.equals(currentVersion)) {
			// App was upgraded
			s.events = "event29";
			save = true;
		}
		else {
			// Regular launch
			s.events = "event27";
		}

		if (save) {
			// Save new data
			SettingUtils.save(context, TRACK_VERSION, currentVersion);
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsSearch(Context context, SearchParams searchParams, SearchResponse searchResponse,
			String refinements) {
		// Start actually tracking the search result change
		Log.d("Tracking \"App.Hotels.Search\" pageLoad...");

		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		s.pageName = "App.Hotels.Search";

		if (refinements != null) {
			// Whether this was the first search or a refined search
			s.events = "event31";

			// Refinement  
			s.eVar28 = s.prop16 = refinements;
		}
		else {
			s.events = "event30";
		}

		// LOB Search
		s.eVar2 = s.prop2 = "hotels";

		// Region
		DecimalFormat df = new DecimalFormat("#.######");
		String region = null;
		if (searchParams.getSearchType() == SearchType.FREEFORM) {
			region = searchParams.getFreeformLocation();
		}
		else {
			region = df.format(searchParams.getSearchLatitude()) + "|" + df.format(searchParams.getSearchLongitude());
		}
		s.eVar4 = s.prop4 = region;

		// Check in/check out date
		s.eVar5 = s.prop5 = CalendarUtils.getDaysBetween(searchParams.getCheckInDate(), Calendar.getInstance()) + "";
		s.eVar6 = s.prop16 = CalendarUtils.getDaysBetween(searchParams.getCheckOutDate(),
				searchParams.getCheckInDate())
				+ "";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Number adults searched for
		s.eVar47 = "A" + searchParams.getNumAdults() + "|C" + searchParams.getNumChildren();

		// Freeform location
		if (searchParams.getSearchType() == SearchType.FREEFORM) {
			s.eVar48 = searchParams.getUserFreeformLocation();
		}

		// Number of search results
		if (searchResponse != null && searchResponse.getFilteredAndSortedProperties() != null) {
			s.prop1 = searchResponse.getFilteredAndSortedProperties().length + "";
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsRoomsRates(Context context, Property property) {
		Log.d("Tracking \"App.Hotels.RoomsRates\" event");

		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		s.pageName = "App.Hotels.RoomsRates";

		// Promo description
		s.eVar9 = property.getLowestRate().getPromoDescription();

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Rating or highly rated
		TrackingUtils.addHotelRating(s, property);

		// Products
		TrackingUtils.addProducts(s, property);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutPayment(Context context, Property property,
			BookingInfoValidation validation) {
		Log.d("Tracking \"App.Hotels.Checkout.Payment\" pageLoad");

		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		s.pageName = "App.Hotels.Checkout.Payment";

		s.events = "event34";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Products
		TrackingUtils.addProducts(s, property);

		// If any sections were already complete, fill them in here
		String referrerId = null;
		if (validation.isGuestsSectionCompleted() && validation.isBillingSectionCompleted()) {
			referrerId = "CKO.BD.CompletedGuestInfo|CKO.BD.CompletedBillingInfo";
		}
		else if (validation.isGuestsSectionCompleted()) {
			referrerId = "CKO.BD.CompletedGuestInfo";
		}
		else if (validation.isBillingSectionCompleted()) {
			referrerId = "CKO.BD.CompletedBillingInfo";
		}

		s.eVar28 = s.prop16 = referrerId;

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutConfirmation(Context context, SearchParams searchParams,
			Property property, BillingInfo billingInfo, Rate rate, BookingResponse response) {
		Log.d("Tracking \"App.Hotels.Checkout.Confirmation\" pageLoad");

		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		TrackingUtils.addStandardFields(context, s);

		s.pageName = "App.Hotels.Checkout.Confirmation";

		s.events = "purchase";

		// Promo description
		if (rate != null) {
			s.eVar9 = rate.getPromoDescription();
		}

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Confirmer";

		// Product details
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String checkIn = df.format(searchParams.getCheckInDate().getTime());
		String checkOut = df.format(searchParams.getCheckOutDate().getTime());
		s.eVar30 = "Hotel:" + checkIn + "-" + checkOut + ":N";

		// Unique confirmation id
		s.prop15 = s.purchaseID = response.getConfNumber();

		// Billing country code
		s.prop46 = s.state = billingInfo.getLocation().getCountryCode();

		// Billing zip codes
		s.prop49 = s.zip = billingInfo.getLocation().getPostalCode();

		// Products
		int numDays = searchParams.getStayDuration();
		double totalCost = 0;
		if (rate != null && rate.getTotalAmountAfterTax() != null) {
			totalCost = rate.getTotalAmountAfterTax().getAmount();
		}
		TrackingUtils.addProducts(s, property, numDays, totalCost);

		// Currency code
		s.currencyCode = rate.getTotalAmountAfterTax().getCurrency();

		// Send the tracking data
		s.track();
	}
}
