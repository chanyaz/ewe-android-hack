package com.expedia.bookings.notification;

import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.notification.Notification.ImageType;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class PushNotificationUtils {
	private static final String LOGGING_TAG = "PushNotificationUtils";
	private static HashMap<String, Integer> sLocStringMap;

	static boolean locKeyForDesktopBooking(String locKey) {
		return locKey.equals("S_Push_Hey_VALUE_your_booking_is_confirmed") ||
			locKey.equals("S_Push_Your_booking_is_confirmed_View_it_in_app");
	}

	static void generateDesktopBookingNotification(Context context, int fhid, String locKey,
		String[] locKeyArgs) {
		String formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);

		if (formattedMessage == null) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification Formatted message was null for locKey:"
				+ locKey);
		}
		else {
			String uniqueId = PushNotificationUtilsV2.sanitizeUniqueId(fhid + "_" + formattedMessage);
			String itinId = "";
			long triggerTimeMillis = System.currentTimeMillis();

			INotificationManager notificationManager = Ui.getApplication(context).appComponent().notificationManager();
			Notification notification = new Notification(uniqueId, itinId, triggerTimeMillis);
			notification.setItinId(itinId);
			notification.setNotificationType(NotificationType.DESKTOP_BOOKING);
			notification.setFlags(Notification.FLAG_PUSH);

			notification.setIconResId(R.drawable.ic_itin_ready);
			notification.setImageType(ImageType.NONE);
			notification.setTitle(context.getString(R.string.Itinerary_ready));
			notification.setBody(formattedMessage);
			notification.setTicker(formattedMessage);
			notification.setTemplateName(locKey);

			notification.save();
			notificationManager.scheduleNotification(notification);
		}
	}

	/**
	 * Provided the locKey and the arguments, return a formatted localized string for a notification
	 *
	 * @param context
	 * @param locKey
	 * @param args
	 * @return
	 */
	static String getFormattedLocString(Context context, String locKey, Object[] args) {
		Log.d(LOGGING_TAG, "PushNotificationUtils.getFormattedLocString locKey:" + locKey);
		String locStr = getLocStringForKey(context, locKey);
		if (TextUtils.isEmpty(locStr)) {
			return null;
		}
		else {
			return String.format(locStr, args);
		}
	}

	/**
	 * Given the key provided by the push notification, we return
	 * the localized string it represents
	 *
	 * @param locKey
	 * @return - the loc string or null
	 */
	static String getLocStringForKey(Context context, String locKey) {
		if (sLocStringMap == null) {
			initLocStrMap();
		}
		if (sLocStringMap.containsKey(locKey)) {
			return context.getString(sLocStringMap.get(locKey));
		}
		return null;
	}

	/**
	 * Set up the hash map for loc strings.
	 * Basically the api returns to us a key, and we need to produce a localized string from it.
	 */
	private static void initLocStrMap() {
		sLocStringMap = new HashMap<>();
		sLocStringMap
			.put("S_Push_Hey_VALUE_your_booking_is_confirmed", R.string.S_Push_Hey_VALUE_your_booking_is_confirmed);
		sLocStringMap.put("S_Push_Your_booking_is_confirmed_View_it_in_app",
			R.string.S_Push_Your_booking_is_confirmed_View_it_in_app);
	}
}
