package com.expedia.bookings.tracking;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.text.TextUtils;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.BookingInfoValidation;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * Contains specific events to track.
 */
public class Tracker {

	// The SettingUtils key for the last version tracked
	private static final String TRACK_VERSION = "tracking_version";

	public static void trackAppLoading(final Context context) {
		Log.d("Tracking \"App.Loading\" pageLoad...");

		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Loading");

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

				// Flurry tracking
				FlurryTracking.trackConversion(context);
			}
		}).start();

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

	public static void trackAppHotelsSearch(Context context, SearchParams searchParams, SearchResponse searchResponse,
			String refinements) {
		// Start actually tracking the search result change
		Log.d("Tracking \"App.Hotels.Search\" pageLoad...");

		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);

		TrackingUtils.addStandardFields(context, s);

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
		String days5 = CalendarUtils.getDaysBetween(Calendar.getInstance(), searchParams.getCheckInDate()) + "";
		s.setEvar(5, days5);
		s.setProp(5, days5);

		String days6 = CalendarUtils.getDaysBetween(searchParams.getCheckInDate(), searchParams.getCheckOutDate()) + "";
		s.setEvar(6, days6);
		s.setProp(6, days6);

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Number adults searched for
		s.setEvar(47, "A" + searchParams.getNumAdults() + "|C" + searchParams.getNumChildren());

		// Freeform location
		if (!TextUtils.isEmpty(searchParams.getUserQuery())) {
			s.setEvar(48, searchParams.getUserQuery());
		}

		// Number of search results
		if (searchResponse != null && searchResponse.getFilteredAndSortedProperties() != null) {
			s.setProp(1, searchResponse.getFilteredAndSortedProperties().length + "");
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsRoomsRates(Context context, Property property, String referrer) {
		Log.d("Tracking \"App.Hotels.RoomsRates\" event");

		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.RoomsRates");

		// Promo description
		s.setEvar(9, property.getLowestRate().getPromoDescription());

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Rating or highly rated
		TrackingUtils.addHotelRating(s, property);

		// Products
		TrackingUtils.addProducts(s, property);

		// Referrer (determines whether a specific rate was clicked into here or not - only applicable for
		// the tablet version of the UI).
		// TODO: referrer has been moved in version 3.x of the library, original line s.referrer = referrer;

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutPayment(Context context, Property property,
			BookingInfoValidation validation) {
		Log.d("Tracking \"App.Hotels.Checkout.Payment\" pageLoad");

		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Checkout.Payment");

		s.setEvents("event34");

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

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

		s.setEvar(28, referrerId);
		s.setProp(16, referrerId);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutConfirmation(Context context, SearchParams searchParams,
			Property property, BillingInfo billingInfo, Rate rate, BookingResponse response) {
		Log.d("Tracking \"App.Hotels.Checkout.Confirmation\" pageLoad");

		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Checkout.Confirmation");

		s.setEvents("purchase");

		// Promo description
		if (rate != null) {
			s.setEvar(9, rate.getPromoDescription());
		}

		// Shopper/Confirmer
		s.setEvar(25, "Confirmer");
		s.setProp(25, "Confirmer");

		// Product details
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String checkIn = df.format(searchParams.getCheckInDate().getTime());
		String checkOut = df.format(searchParams.getCheckOutDate().getTime());
		s.setEvar(30, "Hotel:" + checkIn + "-" + checkOut + ":N");

		// Unique confirmation id
		// 14103: Remove timestamp from the purchaseID variable
		s.setProp(15, response.getItineraryId());
		s.setPurchaseID(response.getItineraryId());

		// Billing country code
		s.setProp(46, billingInfo.getLocation().getCountryCode());
		s.setGeoState(billingInfo.getLocation().getCountryCode());

		// Billing zip codes
		s.setProp(49, billingInfo.getLocation().getPostalCode());
		s.setGeoZip(billingInfo.getLocation().getPostalCode());

		// Products
		int numDays = searchParams.getStayDuration();
		double totalCost = 0;
		if (rate != null && rate.getTotalAmountAfterTax() != null) {
			totalCost = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		}
		TrackingUtils.addProducts(s, property, numDays, totalCost);

		// Currency code
		s.setCurrencyCode(rate.getTotalAmountAfterTax().getCurrency());

		// Send the tracking data
		s.track();
	}

	public static void trackViewOnMap(Context context) {
		Log.d("Tracking \"CKO.CP.ViewInMaps\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.ViewInMaps");
	}

	public static void trackNewSearch(Context context) {
		Log.d("Tracking \"new search\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.StartNewSearch");
	}

	public static void trackOpenExpediaCom(Context context) {
		Log.d("Tracking \"open on expedia com\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, null, "App.Link.View.Hotel.Fullsite");
	}
}
