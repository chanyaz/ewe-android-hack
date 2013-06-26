package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageCache;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.model.DismissedItinButton;
import com.expedia.bookings.model.WorkingBillingInfoManager;
import com.expedia.bookings.model.WorkingTravelerManager;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;
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

			BackgroundImageCache cache = Db.getBackgroundImageCache(context);
			if (cache != null) {
				cache.clearDiskCache(context);
				cache.clearMemCache();
			}

		}
		catch (Exception ex) {
			//Don't care
		}

		ExpediaServices services = new ExpediaServices(context);
		services.clearCookies();

		// Clear itin button dismissals
		SettingUtils.remove(context, R.string.setting_hide_hotel_attach);
		SettingUtils.remove(context, R.string.setting_hide_local_expert);
		DismissedItinButton.clear();

		// Clear anything else out that might remain
		Db.clear();
	}
}
