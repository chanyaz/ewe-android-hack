package com.expedia.bookings.utils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.fragment.FlightSearchParamsFragment;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.mobiata.android.Log;

public class ClearPrivateDataUtil {
	public static void clear(Context context) {
		Log.i("Clearing all private data!");

		boolean signedIn = User.isLoggedIn(context);
		if (signedIn) {
			User.signOut(context);
		}

		ItineraryManager.getInstance().clear();

		Db.deleteTripBucket(context);

		// Cookies
		ExpediaServices services = new ExpediaServices(context);
		services.clearCookies();

		CookieSyncManager.createInstance(context);
		CookieManager.getInstance().removeAllCookie();

		// Clear itin button dismissals
		DismissedItinButton.clear();

		// AirAsia Flight routes
		Db.deleteCachedFlightRoutes(context);

		// Clear airport dropdown suggestions
		AirportDropDownAdapter.clearRecentAirports(context);
		FlightSearchParamsFragment.clearRecentAirAsiaAirports(context);

		// Clear suggestions from tablet search
		SuggestionProvider.clearRecents(context);

		// Tablet launch tile stuff
		Sp.clear(context);
		LaunchDb.clear();

		// Clear anything else out that might remain
		Db.clear();
		Db.clearFlightSearchParamsFromDisk(context);

		// Clear LX and cars suggestions history
		SuggestionUtils.deleteCachedSuggestions(context);

	}
}
