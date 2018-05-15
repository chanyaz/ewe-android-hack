package com.expedia.bookings.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.Notification.ImageType;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.phrase.Phrase;

public class PushNotificationUtils {

	public static final String SENDER_ID = "895052546820";
	private static final String LOGGING_TAG = "PushNotificationUtils";
	private static HashMap<String, Integer> sLocStringMap;

	//We can cache (hashes of) the payloads we have sent to our api, and use them to prevent ourselves from sending
	//the same payload multiple times. Usually our flights dont change so this will save the api some work.
	private static HashMap<String, String> sPayloadMap = new HashMap<>();

	//The server will splode if we make simultanious calls using the same regId,
	//so we create objects to synchronize with (usually this will have one entry)
	private static final HashMap<String, Object> sPushLocks = new HashMap<>();

	public static void removePayloadFromMap(String regId) {
		if (sPayloadMap.containsKey(regId)) {
			sPayloadMap.remove(regId);
		}
	}

	/**
	 * Build and display a notification for the provided arguments
	 *
	 * @param fhid       - The FlightHistoryId as provided by flightstats
	 * @param locKey     - The display message provided by the push
	 * @param locKeyArgs - The arguments to be formatted into the displayMessage
	 * @param context
	 */
	public static void generateNotification(Context context, int fhid, String locKey,
		String[] locKeyArgs, String titleArg, String nID) {
		if (fhid < 0) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification FlightHistoryId must be >= 0");
		}
		else {
			ItinCardDataFlight data = (ItinCardDataFlight) ItineraryManager.getInstance()
				.getItinCardDataFromFlightHistoryId(fhid);
			if (hasLocKeyForNewFlightAlerts(locKey)) {
				generateFlightAlertNotification(context, fhid, locKey,
					locKeyArgs, titleArg, nID, data);
			}
			else if (data == null) {
				// There is not any data from a desktop booking notification,
				// so we check the locKey to see if it indicates that the message
				// is related to a desktop booking. If so, we generate it.
				if (locKeyForDesktopBooking(locKey)) {
					generateDesktopBookingNotification(context, fhid, locKey, locKeyArgs);
				}
			}
		}
	}

	static void generateFlightAlertNotification(Context context, int fhid, String locKey,
		String[] locKeyArgs, String titleArg, String nID, ItinCardDataFlight dataFlight) {
		String itinId;
		if (dataFlight != null) {
			itinId = dataFlight.getId();
		}
		else {
			itinId = "-1";
			Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification couldnt find ItinCardData for fhid:" + fhid);
		}

		long triggerTimeMillis = System.currentTimeMillis();

		String formattedMessage;
		String uniqueId;
		NotificationType notificationType = getNotificationTypeFromLocKey(locKey);
		if (Strings.isNotEmpty(nID) && notificationType != null) {
			uniqueId = sanitizeUniqueId("Push_" + nID);
			formattedMessage = getLocNewString(context, notificationType, locKey, locKeyArgs);
		}
		else {
			formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);
			uniqueId = sanitizeUniqueId(fhid + "_" + formattedMessage);
		}
		if (formattedMessage == null) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification Formatted message was null for locKey:"
				+ locKey);
		}
		else {

			INotificationManager notificationManager = Ui.getApplication(context).appComponent().notificationManager();
			Notification notification = new Notification(uniqueId, itinId, triggerTimeMillis);
			notification.setNotificationType(notificationType);
			notification.setFlags(Notification.FLAG_PUSH);

			notification.setIconResId(R.drawable.ic_stat_flight);
			notification.setImageType(ImageType.NONE);

			String destination = getDestinationStringFromLocArgs(locKey, locKeyArgs);

			String title = getLocStringForKey(context, titleArg);


			if (Strings.isEmpty(title)) {
				title = context.getString(R.string.your_flight_to_x_TEMPLATE, destination);
			}

			notification.setTitle(title);
			notification.setBody(formattedMessage);
			notification.setTicker(formattedMessage);
			notification.setTemplateName(locKey);

			notification.save();
			notificationManager.scheduleNotification(notification);
		}
	}

	public static String getLocNewString(Context context, NotificationType notificationType, String locKey,
		String[] locKeyArgs) {
		String locString = getLocStringForKey(context, locKey);
		switch (notificationType) {
		case FLIGHT_DELAYED:
			if (locString != null) {
				return Phrase.from(locString)
					.put("arrival_airport", locKeyArgs[0])
					.put("departure_time", locKeyArgs[1]).format().toString();
			}
		default:
			return null;
		}
	}

	private static NotificationType getNotificationTypeFromLocKey(String locKey) {
		switch (locKey) {
		case "S_Push_Flight_delayed_with_new_departure_time":
			return NotificationType.FLIGHT_DELAYED;
		default:
			return null;
		}
	}

	private static boolean hasLocKeyForNewFlightAlerts(String locKey) {
		if (sLocStringMap == null) {
			initLocStrMap();
		}
		return sLocStringMap.containsKey(locKey);
	}

	private static boolean locKeyForDesktopBooking(String locKey) {
		return locKey.equals("S_Push_Hey_VALUE_your_booking_is_confirmed") ||
			locKey.equals("S_Push_Your_booking_is_confirmed_View_it_in_app");
	}

	private static void generateDesktopBookingNotification(Context context, int fhid, String locKey,
		String[] locKeyArgs) {
		String formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);

		if (formattedMessage == null) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification Formatted message was null for locKey:"
				+ locKey);
		}
		else {
			String uniqueId = sanitizeUniqueId(fhid + "_" + formattedMessage);
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
	 * Given the locKey and the args, we try to get the destination string.
	 * <p>
	 * Basically most push notifications have a destination name in them, however
	 * not all, and nothing is keyed, so we try do best effort. Return null if
	 * not available.
	 *
	 * @param locKey
	 * @param locKeyArgs
	 * @return
	 */
	private static String getDestinationStringFromLocArgs(String locKey, String[] locKeyArgs) {
		int argNum = getDestinationArgNum(locKey);
		if (argNum >= 0 && argNum < locKeyArgs.length) {
			return locKeyArgs[argNum];
		}
		else {
			return null;
		}
	}

	/**
	 * Given the lockey, which argument index do we expect to contain a destination string.
	 * A negative return value indicates that we dont expect a destination string in the args
	 *
	 * @param locKey
	 * @return loc argument index of destination string, or negative if no destination argument
	 */
	private static int getDestinationArgNum(String locKey) {
		//At this point we analyze the push key to determine the index of the destination argument.
		//THIS IS NOT GREAT. FT does a network lookup based on the fhid that comes with the push message,
		//but because ALL BUT ONE of our push messages contain the destination as an argument it seems
		//better to just find the destination (in a shady manner) than to do an extra network call.
		int retVal = -1;
		String str = locKey.replaceFirst("S_Push", ""); //Remove S_PUSH
		String[] splits = str.split("_");
		int argNum = 0;
		for (String split : splits) {
			//The argument portions are always A-Z all caps
			if (split.matches("[A-Z]+")) {
				//"A" denotes destination (arrival) for FT strings, and "CITY" does so for the EB strings
				if (split.equals("A") || split.equals("CITY")) {
					retVal = argNum;
					break;
				}
				argNum++;
			}
		}

		return retVal;
	}

	/**
	 * We want to build the uniqueId such that it includes only word characters and isn't too long.
	 * This function strips out all of the non word characters and chops it down to length == 1024 if needed.
	 *
	 * @param uniqueId
	 * @return
	 */
	private static String sanitizeUniqueId(String uniqueId) {
		String retStr = uniqueId.replaceAll("\\W", "");
		if (retStr.length() > 1024) {
			retStr = retStr.substring(0, 1024);
		}
		Log.d(LOGGING_TAG, "PushNotificationUtils.sanitizeUniqueId input:" + uniqueId + " output:" + retStr);
		return retStr;
	}

	/**
	 * Provided the locKey and the arguments, return a formatted localized string for a notification
	 *
	 * @param context
	 * @param locKey
	 * @param args
	 * @return
	 */
	public static String getFormattedLocString(Context context, String locKey, Object[] args) {
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
	public static String getLocStringForKey(Context context, String locKey) {
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
		sLocStringMap
			.put("S_Push_Flight_delayed_with_new_departure_time", R.string.flight_notification_delayed_flight_TEMPLATE);
		sLocStringMap.put("S_Push_Flight_delayed_title", R.string.flight_notification_delayed_title);
	}

	public static Boolean isFlightAlertsNotification(Notification notification) {
		List<NotificationType> notificationTypes = Arrays.asList(NotificationType.FLIGHT_DELAYED);
		return notificationTypes.contains(notification.getNotificationType());
	}
}
