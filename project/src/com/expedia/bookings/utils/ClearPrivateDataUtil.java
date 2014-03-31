package com.expedia.bookings.utils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.util.SettingUtils;

public class ClearPrivateDataUtil {
	public static void clear(Context context) {
		Log.i("Clearing all private data!");

		BillingInfo info = new BillingInfo();
		info.delete(context);

		boolean signedIn = User.isLoggedIn(context);
		if (signedIn) {
			User.signOut(context);
		}

		ItineraryManager.getInstance().clear();

		Db.deleteCachedFlightData(context);
		Db.deleteTravelers(context);

		Db.deleteHotelSearchData(context);

		Db.deleteTripBucket(context);

		WorkingBillingInfoManager biManager = new WorkingBillingInfoManager();
		biManager.deleteWorkingBillingInfoFile(context);

		WorkingTravelerManager travManager = new WorkingTravelerManager();
		travManager.deleteWorkingTravelerFile(context);

		try {
			//If the data has already been populated in memory, we should clear that....
			if (Db.getWorkingBillingInfoManager() != null) {
				Db.getWorkingBillingInfoManager().clearWorkingBillingInfo(context);
			}

			if (Db.getWorkingTravelerManager() != null) {
				Db.getWorkingTravelerManager().clearWorkingTraveler(context);
			}

			Db.getBillingInfo().delete(context);
			Db.getTravelers().clear();
		}
		catch (Exception ex) {
			//Don't care
		}

		// Cookies
		ExpediaServices services = new ExpediaServices(context);
		services.clearCookies();

		CookieSyncManager.createInstance(context);
		CookieManager.getInstance().removeAllCookie();

		// Clear itin button dismissals
		SettingUtils.remove(context, R.string.setting_hide_hotel_attach);
		SettingUtils.remove(context, R.string.setting_hide_local_expert);
		DismissedItinButton.clear();

		// Clear image cache, why not
		TwoLevelImageCache.clearMemoryCache();

		L2ImageCache.sDestination.clearDiskCache();
		L2ImageCache.sDestination.clearMemoryCache();

		// AirAsia Flight routes
		Db.deleteCachedFlightRoutes(context);

		// Clear airport dropdown suggestions
		AirportDropDownAdapter.clearRecentAirports(context);
		FlightSearchParamsFragment.clearRecentAirAsiaAirports(context);

		// Clear suggestions from tablet search
		SuggestionProvider.clearRecents(context);

		// Clear previous hotel searches
		Search.deleteAll();

		// Clear anything else out that might remain
		Db.clear();
	}
}
