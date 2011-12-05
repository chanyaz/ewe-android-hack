package com.expedia.bookings.tracking;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Application;
import android.content.Context;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.SearchResponse;
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
}
