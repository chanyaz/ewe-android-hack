package com.expedia.bookings.utils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;

public class ClearPrivateDataUtil {
	public static void clear(Context context) {
		Log.i("Clearing all private data!");

		UserStateManager userStateManager = Ui.getApplication(context).appComponent().userStateManager();

		boolean signedIn = userStateManager.isUserAuthenticated();
		if (signedIn) {
			userStateManager.signOut();
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

		// Clear suggestions from tablet search
		SuggestionProvider.clearRecents(context);

		// Clear anything else out that might remain
		Db.clear();

		// Clear LX and cars suggestions history
		SuggestionUtils.deleteCachedSuggestions(context);
		// Clear new hotels suggestions history
		SuggestionV4Utils.deleteCachedSuggestions(context);

		// Clear search params history
		SearchParamsHistoryUtil.deleteCachedSearchParams(context);
	}
}
