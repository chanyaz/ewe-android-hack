package com.expedia.bookings.utils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

import com.expedia.bookings.data.AppDatabase;
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

		final AppDatabase appDB = Ui.getApplication(context).appComponent().provideAppDatabase();

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
		Db.sharedInstance.deleteCachedFlightRoutes(context);

		// Clear anything else out that might remain
		Db.sharedInstance.clear();

		// Clear new hotels suggestions history
		SuggestionV4Utils.deleteCachedSuggestions(context);

		// Clear search params history
		SearchParamsHistoryUtil.deleteCachedSearchParams(context);

		//Clear Webpage JS Dom Storage
		WebStorage.getInstance().deleteAllData();

		//Clear Flight recent search suggestion
		new Thread() {
			@Override
			public void run() {
				appDB.recentSearchDAO().clear();
			}
		}.start();
	}
}
