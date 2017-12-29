package com.expedia.bookings.notification;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.notification.Notification.ImageType;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PushRegistrationResponseHandler;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;
import com.squareup.phrase.Phrase;

public class PushNotificationUtils {

	public static final String SENDER_ID = "895052546820";
	public static final String REGISTRATION_URL_TEST = "http://ewetest.flightalerts.mobiata.com/register_for_flight_alerts";
	public static final String REGISTRATION_URL_PRODUCTION = "https://ewe.flightalerts.mobiata.com/register_for_flight_alerts";
	private static final String LOGGING_TAG = "PushNotificationUtils";
	private static HashMap<String, Integer> sLocStringMap;
	private static HashMap<String, Integer> sNewLocStringMap;

	//We can cache (hashes of) the payloads we have sent to our api, and use them to prevent ourselves from sending
	//the same payload multiple times. Usually our flights dont change so this will save the api some work.
	private static HashMap<String, String> sPayloadMap = new HashMap<String, String>();

	//The server will splode if we make simultanious calls using the same regId,
	//so we create objects to synchronize with (usually this will have one entry)
	private static final HashMap<String, Object> sPushLocks = new HashMap<String, Object>();

	/**
	 * This provides a unique object for a given registration id that can be locked on to ensure
	 * we dont hit the server with multiple registration at the same time for the same regId
	 *
	 * The api does not handle more than one request for a given regId at the same time and will splode
	 * if we try.
	 *
	 * @param regId
	 * @return
	 */
	public static Object getLockObject(String regId) {
		if (!sPushLocks.containsKey(regId)) {
			sPushLocks.put(regId, new Object());
		}
		return sPushLocks.get(regId);
	}

	/**
	 * Before we send a payload to the PushNotification API, we can call this method to
	 * see if the payload provided has already been sent to the API, and thus save ourselves a network call
	 * (and save the api a bunch of traffic, as our flight list will typically not change)
	 * We dont persist these payloads on disk because the api doesnt care if we send the same thing twice
	 * we just want to reduce our network usage and reduce server load
	 *
	 * @param regId
	 * @param payload
	 * @return true if we should send this payload, false if the payload is invalid or already sent
	 */
	public static boolean sendPayloadCheck(String regId, JSONObject payload) {
		Log.d(LOGGING_TAG, "PushNotificationUtils.sendPayloadCheck() regId:" + regId);
		if (TextUtils.isEmpty(regId)) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.sendPayloadCheck() returning false - regId empty");
			return false;
		}

		String payloadHash = hashJsonPayload(payload);
		if (payloadHash == null) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.sendPayloadCheck() returning false - payloadHash empty");
			return false;
		}
		else {
			Log.d(LOGGING_TAG, "PushNotificationUtils.sendPayloadCheck() payloadHash: " + Strings.formatHexString(payloadHash));
		}

		if (!sPayloadMap.containsKey(regId)) {
			sPayloadMap.put(regId, payloadHash);
			return true;
		}
		else {
			String oldPayloadHash = sPayloadMap.get(regId);

			//We should send this to the api if the payloads DON'T match
			boolean shouldSend = !oldPayloadHash.equals(payloadHash);

			if (!shouldSend) {
				Log.d(LOGGING_TAG, "PushNotificationUtils.sendPayloadCheck() returning false because the payloads match. regId:"
						+ regId + " Hash:" + payloadHash + " payload:" + payload.toString());
			}

			return shouldSend;
		}
	}

	public static void clearPayloadMap() {
		sPayloadMap.clear();
	}

	public static void removePayloadFromMap(String regId) {
		if (sPayloadMap.containsKey(regId)) {
			sPayloadMap.remove(regId);
		}
	}

	/**
	 * Build and display a notification for the provided arguments
	 * @param fhid - The FlightHistoryId as provided by flightstats
	 * @param locKey - The display message provided by the push
	 * @param locKeyArgs - The arguments to be formatted into the displayMessage
	 * @param context
	 */
	public static void generateNotification(Context context, int fhid, String locKey,
		String[] locKeyArgs, String typeIntStr, String titleArg, String nID) {
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
					generateDesktopBookingNotification(context, fhid, locKey, locKeyArgs, typeIntStr);
				}
				else {
					Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification couldnt find ItinCardData for fhid:" + fhid);
				}
			}
			else {
				FlightLeg leg = data.getFlightLeg();

				if (locKey.equals("S_Push_baggage_BAGGAGE") && leg.getSegmentCount() > 0
						&& leg.getSegment(leg.getSegmentCount() - 1).mFlightHistoryId == fhid) {
					//We only care about baggage claim info for the terminal segment of our flight
					Log.d(LOGGING_TAG, "PushNotificationUtils.generateNotification we got a baggage claim push message for"
							+ " a NON-TERMINAL segment of our flight. Because this is a layover flight, we do not"
							+ " display the baggage claim notification. fhid:" + fhid);
				}
				else {
					String itinId = data.getId();
					long triggerTimeMillis = System.currentTimeMillis();

					String formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);

					if (formattedMessage == null) {
						Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification Formatted message was null for locKey:"
								+ locKey);
					}
					else {
						String uniqueId = sanitizeUniqueId(fhid + "_" + formattedMessage);

						NotificationManager notificationManager = Ui.getApplication(context).appComponent().notificationManager();
						Notification notification = new Notification(uniqueId, itinId, triggerTimeMillis);
						notification.setItinId(itinId);
						notification.setNotificationType(pushApiTypeToNotificationType(typeIntStr));
						notification.setFlags(Notification.FLAG_PUSH);

						notification.setIconResId(R.drawable.ic_stat_flight);
						notification.setImageType(ImageType.NONE);

						String destination = getDestinationStringFromLocArgs(locKey, locKeyArgs);
						if (TextUtils.isEmpty(destination)) {
							destination = StrUtils.getWaypointCityOrCode(leg.getLastWaypoint());
						}

						String airline = leg.getPrimaryAirlineNamesFormatted();
						String title;

						if (!TextUtils.isEmpty(airline)) {
							title = context.getString(R.string.x_flight_to_x_TEMPLATE, airline, destination);
						}
						else {
							title = context.getString(R.string.your_flight_to_x_TEMPLATE, destination);
						}

						notification.setTitle(title);
						notification.setBody(formattedMessage);
						notification.setTicker(formattedMessage);

						notification.save();
						notificationManager.scheduleNotification(notification);
					}
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
		if (Strings.isNotEmpty(nID)) {
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

			NotificationManager notificationManager = Ui.getApplication(context).appComponent().notificationManager();
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

			notification.save();
			notificationManager.scheduleNotification(notification);
		}
	}

	public static String getLocNewString(Context context, NotificationType notificationType, String locKey, String[] locKeyArgs) {
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
		if (sLocStringMap == null || sNewLocStringMap == null) {
			initLocStrMap();
		}
		return sNewLocStringMap.containsKey(locKey);
	}

	private static boolean locKeyForDesktopBooking(String locKey) {
		return locKey.equals("S_Push_Hey_VALUE_your_booking_is_confirmed") ||
			locKey.equals("S_Push_Your_booking_is_confirmed_View_it_in_app");
	}

	private static void generateDesktopBookingNotification(Context context, int fhid, String locKey,
														   String[] locKeyArgs, String typeIntStr) {
		String formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);

		if (formattedMessage == null) {
			Log.e(LOGGING_TAG, "PushNotificationUtils.generateNotification Formatted message was null for locKey:"
				+ locKey);
		}
		else {
			String uniqueId = sanitizeUniqueId(fhid + "_" + formattedMessage);
			String itinId = "";
			long triggerTimeMillis = System.currentTimeMillis();

			NotificationManager notificationManager = Ui.getApplication(context).appComponent().notificationManager();
			Notification notification = new Notification(uniqueId, itinId, triggerTimeMillis);
			notification.setItinId(itinId);
			notification.setNotificationType(pushApiTypeToNotificationType(typeIntStr));
			notification.setFlags(Notification.FLAG_PUSH);

			notification.setIconResId(R.drawable.ic_itin_ready);
			notification.setImageType(ImageType.NONE);
			notification.setTitle(context.getString(R.string.Itinerary_ready));
			notification.setBody(formattedMessage);
			notification.setTicker(formattedMessage);

			notification.save();
			notificationManager.scheduleNotification(notification);
		}
	}

	/**
	 * Given the locKey and the args, we try to get the destination string.
	 *
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
		if (locKey.equals("S_Push_flight_from_CITY_to_CITY_cancelled")) {
			//This one is like Fright from <arg1> to <arg2> is cancelled, we want destination so we go index 1
			return 1;
		}
		else {
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
	 * Build the JSONObject that is to be posted to the api for registering push notifications
	 * @param token - This is api terminology, in our case it is the Registration Id provided by GCM
	 * @param tuid - The current user's tuid
	 * @param flightList - A list of flight objects we want to register for that come from our regular itins.
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static JSONObject buildPushRegistrationPayload(Context context, String token, int siteId, long tuid, List<Flight> flightList) {
		JSONObject retObj = new JSONObject();
		JSONObject courier = new JSONObject();
		JSONArray flights = new JSONArray();
		JSONObject user = new JSONObject();
		try {
			retObj.putOpt("__type__", "RegisterForAlertsRequest");
			retObj.putOpt("version", "4");

			user.putOpt("__type__", "ExpediaUser");
			user.put("site_id", siteId);
			user.put("tuid", tuid);

			courier.put("__type__", "Courier");
			courier.put("group", "gcm");
			courier.put("token", token);

			if (flightList != null) {
				for (Flight f : flightList) {
					JSONObject flightJson = buildFlightJSON(context, f, false);
					if (flightJson != null) {
						flights.put(flightJson);
					}
				}
			}

			retObj.putOpt("courier", courier);
			retObj.putOpt("flights", flights);
			retObj.putOpt("user", user);
		}
		catch (Exception ex) {
			Log.d(LOGGING_TAG, "Exception in buildPushRegistrationPayload", ex);
		}

		return retObj;
	}

	private static JSONObject buildFlightJSON(Context context, Flight flight, boolean shared) {
		try {
			DateTime departureDate = flight.getSegmentDepartureTime();
			DateTime arrivalDate = flight.getSegmentArrivalTime();

			JSONObject flightJson = new JSONObject();
			flightJson.put("__type__", "Flight");
			flightJson.put("departure_date", JodaUtils.format(departureDate, "yyyy-MM-dd HH:mm:ss"));
			flightJson.put("arrival_date", JodaUtils.format(arrivalDate, "yyyy-MM-dd HH:mm:ss"));
			flightJson.put("destination", flight.getDestinationWaypoint().mAirportCode);
			flightJson.put("origin", flight.getOriginWaypoint().mAirportCode);
			if (flight.getPrimaryFlightCode() != null) {
				flightJson.put("airline", flight.getPrimaryFlightCode().mAirlineCode);
				flightJson.put("flight_no", flight.getPrimaryFlightCode().mNumber);
			}
			flightJson.put("shared", shared);
			//values for new TNS system
			flightJson.put("departureDateWithTZ", JodaUtils.format(departureDate, "yyyy-MM-dd HH:mm:ss"));
			flightJson.put("arrivalDateWithTZ", JodaUtils.format(arrivalDate, "yyyy-MM-dd HH:mm:ss"));
			if (flight.getPrimaryFlightCode() != null) {
				flightJson.put("airlineCode", flight.getPrimaryFlightCode().mAirlineCode);
				flightJson.put("flightNumber", flight.getPrimaryFlightCode().mNumber);
			}
			return flightJson;
		}
		catch (Exception ex) {
			Log.e(LOGGING_TAG, "Exception in buildFlightJSON", ex);
		}
		return null;
	}

	/**
	 * Provided the locKey and the arguments, return a formatted localized string for a notification
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
		if (sLocStringMap == null || sNewLocStringMap == null) {
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
		sLocStringMap = new HashMap<String, Integer>();
		sNewLocStringMap = new HashMap();

		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR", R.string.S_Push_flight_CITY_delayed_HOUR);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS", R.string.S_Push_flight_CITY_delayed_HOURS);
		sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE", R.string.S_Push_flight_CITY_delayed_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES", R.string.S_Push_flight_CITY_delayed_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTE", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTES", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_early_MINUTE", R.string.S_Push_flight_CITY_early_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_early_MINUTES", R.string.S_Push_flight_CITY_early_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR", R.string.S_Push_flight_CITY_early_HOUR);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS", R.string.S_Push_flight_CITY_early_HOURS);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE", R.string.S_Push_flight_CITY_early_HOUR_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE", R.string.S_Push_flight_CITY_early_HOURS_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES", R.string.S_Push_flight_CITY_early_HOUR_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTES", R.string.S_Push_flight_CITY_early_HOURS_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE_gate_GATE", R.string.S_Push_flight_CITY_delayed_MINUTE_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES_gate_GATE", R.string.S_Push_flight_CITY_delayed_MINUTES_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_gate_GATE", R.string.S_Push_flight_CITY_delayed_HOUR_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_gate_GATE", R.string.S_Push_flight_CITY_delayed_HOURS_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTE_gate_GATE", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE_gate_GATE", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES_gate_GATE", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTES_gate_GATE", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_MINUTE_gate_GATE", R.string.S_Push_flight_CITY_early_MINUTE_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_MINUTES_gate_GATE", R.string.S_Push_flight_CITY_early_MINUTES_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_gate_GATE", R.string.S_Push_flight_CITY_early_HOUR_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_gate_GATE", R.string.S_Push_flight_CITY_early_HOURS_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE_gate_GATE", R.string.S_Push_flight_CITY_early_HOUR_MINUTE_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE_gate_GATE", R.string.S_Push_flight_CITY_early_HOURS_MINUTE_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES_gate_GATE", R.string.S_Push_flight_CITY_early_HOUR_MINUTES_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTES_gate_GATE", R.string.S_Push_flight_CITY_early_HOURS_MINUTES_gate_GATE);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR", R.string.S_Push_flight_CITY_departs_in_HOUR);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS", R.string.S_Push_flight_CITY_departs_in_HOURS);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS_MINUTE", R.string.S_Push_flight_CITY_departs_in_HOURS_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS_MINUTES", R.string.S_Push_flight_CITY_departs_in_HOURS_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR_MINUTE", R.string.S_Push_flight_CITY_departs_in_HOUR_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR_MINUTES", R.string.S_Push_flight_CITY_departs_in_HOUR_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_departs_in_MINUTES", R.string.S_Push_flight_CITY_departs_in_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_gate_GATE", R.string.S_Push_flight_CITY_gate_GATE);
		sLocStringMap.put("S_Push_flight_from_CITY_to_CITY_cancelled", R.string.S_Push_flight_from_CITY_to_CITY_cancelled);
		sLocStringMap.put("S_Push_baggage_BAGGAGE", R.string.S_Push_baggage_BAGGAGE);
		sLocStringMap.put("S_Push_Hey_VALUE_your_booking_is_confirmed", R.string.S_Push_Hey_VALUE_your_booking_is_confirmed);
		sLocStringMap.put("S_Push_Your_booking_is_confirmed_View_it_in_app", R.string.S_Push_Your_booking_is_confirmed_View_it_in_app);


		//Add the FlightTrack push keys and strings for shared itins.
		addFTPushKeysToMap();
		addFlightAlertArgs();
	}

	private static void addFlightAlertArgs() {
		sNewLocStringMap.put("S_Push_Flight_delayed_with_new_departure_time", R.string.flight_notification_delayed_flight_TEMPLATE);
		sNewLocStringMap.put("S_Push_Flight_delayed_title", R.string.flight_notification_delayed_title);
		sLocStringMap.putAll(sNewLocStringMap);
	}

	/**
	 * This adds the FlightTrack push keys and accompanying strings to the key map.
	 * We keep these separate because we are likely to customize them eventually.
	 * Also this makes porting over the keys easy. Since both push systems derive from the same code
	 * we can just go into the GcmBroadcastReceiver and copy the contents of initLocStringMap() to this
	 * function (not including the part that initializes the map).
	 */
	private static void addFTPushKeysToMap() {
		sLocStringMap.put("S_Push_check_in_C_from_D_to_A_HS", R.string.S_Push_check_in_C_from_D_to_A_HS);
		sLocStringMap.put("S_Push_two_hour_C_D_to_A_from_G", R.string.S_Push_two_hour_C_D_to_A_from_G);
		sLocStringMap.put("S_Push_two_hour_C_D_to_A", R.string.S_Push_two_hour_C_D_to_A);
		sLocStringMap.put("S_Push_one_hour_C_D_to_A_from_G", R.string.S_Push_one_hour_C_D_to_A_from_G);
		sLocStringMap.put("S_Push_one_hour_C_D_to_A", R.string.S_Push_one_hour_C_D_to_A);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_MS_late_from_G",
				R.string.S_Push_C_D_to_A_depart_T_HS_MS_late_from_G);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_MS_late", R.string.S_Push_C_D_to_A_depart_T_HS_MS_late);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_late_from_G", R.string.S_Push_C_D_to_A_depart_T_HS_late_from_G);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_late", R.string.S_Push_C_D_to_A_depart_T_HS_late);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_MS_late_from_G", R.string.S_Push_C_D_to_A_depart_T_MS_late_from_G);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_MS_late", R.string.S_Push_C_D_to_A_depart_T_MS_late);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_MS_early_from_G",
				R.string.S_Push_C_D_to_A_depart_T_HS_MS_early_from_G);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_MS_early", R.string.S_Push_C_D_to_A_depart_T_HS_MS_early);
		sLocStringMap
				.put("S_Push_C_D_to_A_depart_T_HS_early_from_G", R.string.S_Push_C_D_to_A_depart_T_HS_early_from_G);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_HS_early", R.string.S_Push_C_D_to_A_depart_T_HS_early);
		sLocStringMap
				.put("S_Push_C_D_to_A_depart_T_MS_early_from_G", R.string.S_Push_C_D_to_A_depart_T_MS_early_from_G);
		sLocStringMap.put("S_Push_C_D_to_A_depart_T_MS_early", R.string.S_Push_C_D_to_A_depart_T_MS_early);
		sLocStringMap.put("S_Push_Gate_change_C_D_A_from_G_instead_of_G",
				R.string.S_Push_Gate_change_C_D_A_from_G_instead_of_G);
		sLocStringMap.put("S_Push_Gate_change_C_D_A_from_G", R.string.S_Push_Gate_change_C_D_A_from_G);
		sLocStringMap.put("S_Push_On_time_departure_C_D_A_from_G_at_T",
				R.string.S_Push_On_time_departure_C_D_A_from_G_at_T);
		sLocStringMap.put("S_Push_On_time_departure_C_D_A_at_T", R.string.S_Push_On_time_departure_C_D_A_at_T);
		sLocStringMap.put("S_Push_Early_departure_C_D_A_from_G_at_T_HS_MS",
				R.string.S_Push_Early_departure_C_D_A_from_G_at_T_HS_MS);
		sLocStringMap.put("S_Push_Early_departure_C_D_A_from_G_at_T_HS",
				R.string.S_Push_Early_departure_C_D_A_from_G_at_T_HS);
		sLocStringMap.put("S_Push_Early_departure_C_D_A_from_G_at_T_MS",
				R.string.S_Push_Early_departure_C_D_A_from_G_at_T_MS);
		sLocStringMap.put("S_Push_Early_departure_C_D_A_at_T_HS_MS", R.string.S_Push_Early_departure_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_Early_departure_C_D_A_at_T_HS", R.string.S_Push_Early_departure_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_Early_departure_C_D_A_at_T_MS", R.string.S_Push_Early_departure_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_Late_departure_C_D_A_from_G_at_T_HS_MS",
				R.string.S_Push_Late_departure_C_D_A_from_G_at_T_HS_MS);
		sLocStringMap.put("S_Push_Late_departure_C_D_A_from_G_at_T_HS",
				R.string.S_Push_Late_departure_C_D_A_from_G_at_T_HS);
		sLocStringMap.put("S_Push_Late_departure_C_D_A_from_G_at_T_MS",
				R.string.S_Push_Late_departure_C_D_A_from_G_at_T_MS);
		sLocStringMap.put("S_Push_Late_departure_C_D_A_at_T_HS_MS", R.string.S_Push_Late_departure_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_Late_departure_C_D_A_at_T_HS", R.string.S_Push_Late_departure_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_Late_departure_C_D_A_at_T_MS", R.string.S_Push_Late_departure_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_on_time_takeoff_C_D_A_at_T", R.string.S_Push_on_time_takeoff_C_D_A_at_T);
		sLocStringMap.put("S_Push_late_takeoff_C_D_A_at_T_HS_MS", R.string.S_Push_late_takeoff_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_late_takeoff_C_D_A_at_T_HS", R.string.S_Push_late_takeoff_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_late_takeoff_C_D_A_at_T_MS", R.string.S_Push_late_takeoff_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_early_takeoff_C_D_A_at_T_HS_MS", R.string.S_Push_early_takeoff_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_early_takeoff_C_D_A_at_T_HS", R.string.S_Push_early_takeoff_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_early_takeoff_C_D_A_at_T_MS", R.string.S_Push_early_takeoff_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_HS_MS", R.string.S_Push_C_D_A_arrives_in_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_HS_M", R.string.S_Push_C_D_A_arrives_in_HS_M);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_H_MS", R.string.S_Push_C_D_A_arrives_in_H_MS);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_H_M", R.string.S_Push_C_D_A_arrives_in_H_M);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_HS", R.string.S_Push_C_D_A_arrives_in_HS);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_H", R.string.S_Push_C_D_A_arrives_in_H);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_MS", R.string.S_Push_C_D_A_arrives_in_MS);
		sLocStringMap.put("S_Push_C_D_A_arrives_in_M", R.string.S_Push_C_D_A_arrives_in_M);
		sLocStringMap.put("S_Push_C_D_A_will_land_on_time_at_T", R.string.S_Push_C_D_A_will_land_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_will_land_late_at_T_HS_MS", R.string.S_Push_C_D_A_will_land_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_will_land_late_at_T_HS", R.string.S_Push_C_D_A_will_land_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_will_land_late_at_T_MS", R.string.S_Push_C_D_A_will_land_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_will_land_early_at_T_HS_MS", R.string.S_Push_C_D_A_will_land_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_will_land_early_at_T_HS", R.string.S_Push_C_D_A_will_land_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_will_land_early_at_T_MS", R.string.S_Push_C_D_A_will_land_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_on_time_at_T",
				R.string.S_Push_C_D_A_will_arrive_at_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_will_arrive_at_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_late_at_T_HS",
				R.string.S_Push_C_D_A_will_arrive_at_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_late_at_T_MS",
				R.string.S_Push_C_D_A_will_arrive_at_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_will_arrive_at_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_early_at_T_HS",
				R.string.S_Push_C_D_A_will_arrive_at_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_will_arrive_at_G_early_at_T_MS",
				R.string.S_Push_C_D_A_will_arrive_at_G_early_at_T_MS);
		sLocStringMap.put("S_Push_Gate_change_C_D_A_at_G_instead_of_G",
				R.string.S_Push_Gate_change_C_D_A_at_G_instead_of_G);
		sLocStringMap.put("S_Push_Gate_change_C_D_A_at_G", R.string.S_Push_Gate_change_C_D_A_at_G);
		sLocStringMap.put("S_Push_late_landed_C_D_A_at_T_HS_MS", R.string.S_Push_late_landed_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_late_landed_C_D_A_at_T_HS", R.string.S_Push_late_landed_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_late_landed_C_D_A_at_T_MS", R.string.S_Push_late_landed_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_early_landed_C_D_A_at_T_HS_MS", R.string.S_Push_early_landed_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_early_landed_C_D_A_at_T_HS", R.string.S_Push_early_landed_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_early_landed_C_D_A_at_T_MS", R.string.S_Push_early_landed_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_on_time_landed_C_D_A_at_T", R.string.S_Push_on_time_landed_C_D_A_at_T);
		sLocStringMap.put("S_Push_early_arrival_C_D_A_at_G_at_T_HS_MS",
				R.string.S_Push_early_arrival_C_D_A_at_G_at_T_HS_MS);
		sLocStringMap.put("S_Push_early_arrival_C_D_A_at_G_at_T_HS", R.string.S_Push_early_arrival_C_D_A_at_G_at_T_HS);
		sLocStringMap.put("S_Push_early_arrival_C_D_A_at_G_at_T_MS", R.string.S_Push_early_arrival_C_D_A_at_G_at_T_MS);
		sLocStringMap.put("S_Push_early_arrival_C_D_A_at_T_HS_MS", R.string.S_Push_early_arrival_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_early_arrival_C_D_A_at_T_HS", R.string.S_Push_early_arrival_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_early_arrival_C_D_A_at_T_MS", R.string.S_Push_early_arrival_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_late_arrival_C_D_A_at_G_at_T_HS_MS",
				R.string.S_Push_late_arrival_C_D_A_at_G_at_T_HS_MS);
		sLocStringMap.put("S_Push_late_arrival_C_D_A_at_G_at_T_HS", R.string.S_Push_late_arrival_C_D_A_at_G_at_T_HS);
		sLocStringMap.put("S_Push_late_arrival_C_D_A_at_G_at_T_MS", R.string.S_Push_late_arrival_C_D_A_at_G_at_T_MS);
		sLocStringMap.put("S_Push_late_arrival_C_D_A_at_T_HS_MS", R.string.S_Push_late_arrival_C_D_A_at_T_HS_MS);
		sLocStringMap.put("S_Push_late_arrival_C_D_A_at_T_HS", R.string.S_Push_late_arrival_C_D_A_at_T_HS);
		sLocStringMap.put("S_Push_late_arrival_C_D_A_at_T_MS", R.string.S_Push_late_arrival_C_D_A_at_T_MS);
		sLocStringMap.put("S_Push_on_time_arrival_C_D_A_at_G_at_T", R.string.S_Push_on_time_arrival_C_D_A_at_G_at_T);
		sLocStringMap.put("S_Push_on_time_arrival_C_D_A_at_T", R.string.S_Push_on_time_arrival_C_D_A_at_T);
		sLocStringMap.put("S_Push_cancelled_C_D_A", R.string.S_Push_cancelled_C_D_A);
		sLocStringMap.put("S_Push_diverted_C_D_A_AIRPORT", R.string.S_Push_diverted_C_D_A_AIRPORT);
		sLocStringMap.put("S_Push_diverted_C_D_A", R.string.S_Push_diverted_C_D_A);
		sLocStringMap.put("S_Push_redirected_C_D_A", R.string.S_Push_redirected_C_D_A);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_on_time_at_T",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_on_time_at_T);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_on_time_at_T_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_on_time_at_T_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_on_time_at_T_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_early_at_T_HS_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_early_at_T_HS_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_late_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_late_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_late_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_late_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_late_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_late_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_early_at_T_HS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_early_at_T_HS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_G_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_MS_arrival_early_at_T_HS_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_MS_arrival_early_at_T_HS_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_G_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_G_late_at_T_HS_MS_arrival_early_at_T_MS);
		sLocStringMap.put("S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_early_at_T_MS",
				R.string.S_Push_C_D_A_left_gate_late_at_T_HS_MS_arrival_early_at_T_MS);
	}

	/**
	 * Get the NotificationType from the "type" value returned from the api
	 * @param typeIntStr - The "type" string returned from the api
	 * @return - NotificationType, default to NotificationType.FLIGHT_GATE_TIME_CHANGE;
	 */
	public static NotificationType pushApiTypeToNotificationType(String typeIntStr) {

		// From @shawn's push notifiaction type test:
		//    self.assertEqual(Notification("SCHEDULED_DEPARTURE").id, 1)
		//    self.assertEqual(Notification("SCHEDULED_ARRIVAL").id, 2)
		//    self.assertEqual(Notification("STATUS_CANCELLED").id, 3)
		//    self.assertEqual(Notification("STATUS_DEPARTED").id, 4)
		//    self.assertEqual(Notification("STATUS_WILL_LEAVE_GATE").id, 5)
		//    self.assertEqual(Notification("STATUS_LEFT_GATE").id, 6)
		//    self.assertEqual(Notification("STATUS_WILL_TAKE_OFF").id, 7)
		//    self.assertEqual(Notification("STATUS_TOOK_OFF").id, 8)
		//    self.assertEqual(Notification("STATUS_WILL_LAND").id, 9)
		//    self.assertEqual(Notification("STATUS_LANDED").id, 10)
		//    self.assertEqual(Notification("STATUS_WILL_REACH_GATE").id, 11)
		//    self.assertEqual(Notification("STATUS_REACHED_GATE").id, 12)
		//    self.assertEqual(Notification("STATUS_DEPARTURE_GATE_CHANGE").id, 13)
		//    self.assertEqual(Notification("STATUS_ARRIVAL_GATE_CHANGE").id, 14)
		//    self.assertEqual(Notification("STATUS_ARRIVED").id, 15)
		//    self.assertEqual(Notification("STATUS_DIVERTED").id, 16)
		//    self.assertEqual(Notification("STATUS_REDIRECTED").id, 17)
		//    self.assertEqual(Notification("STATUS_BAGGAGE_CLAIM").id, 18)

		int iType = 0;
		try {
			//We attempt to parse the typeIntStr to an int, it may however contain multiple values such as "6,16"
			//in that case we just use the default as we currently don't handle multiple types
			iType = Integer.parseInt(typeIntStr);
		}
		catch (NumberFormatException ex) {
			Log.w(LOGGING_TAG, "Failure to parse typeIntStr:" + typeIntStr + " to an int. Using default type:" + iType, ex);
		}

		switch (iType) {
		case 1:
			return NotificationType.FLIGHT_DEPARTURE_REMINDER;
		case 3:
			return NotificationType.FLIGHT_CANCELLED;
		case 5:
			return NotificationType.FLIGHT_GATE_TIME_CHANGE;
		case 13:
			return NotificationType.FLIGHT_GATE_NUMBER_CHANGE;
		case 18:
			return NotificationType.FLIGHT_BAGGAGE_CLAIM;
		case 100:
			return NotificationType.DESKTOP_BOOKING;

		default:
			Log.e(LOGGING_TAG, "Type couldn't be converted from type:" + typeIntStr + " to valid NotificationType enum");
			//Default as this is largely used for tracking only
			return NotificationType.FLIGHT_GATE_TIME_CHANGE;
		}
	}

	/**
	 * We allow concurrent un-registrations (which should never really happen, but better safe than sorry)
	 * thus we build unique download keys based on registrationId
	 *
	 * @param regId
	 * @return
	 */
	public static String buildUnregisterDownloadKey(String regId) {
		return "UNREGISTER_PUSH_NOTIFICATIONS_" + regId;
	}

	/**
	 * This is a helper method that essentially just sends an empty flight list to the api
	 * (in the background) and thereby unregisters all of the current push notifications for
	 * the supplied regId
	 *
	 * @param context
	 * @param serverUrl
	 * @param regId
	 * @param unregistrationCompleteHandler
	 */
	public static void unRegister(final Context context, final String serverUrl, final String regId,
			OnDownloadComplete<PushNotificationRegistrationResponse> unregistrationCompleteHandler) {
		Log.d(LOGGING_TAG, "PushNotificationUtils.unRegister regId " + regId);
		String downloadKey = buildUnregisterDownloadKey(regId);
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(downloadKey)) {
			bd.cancelDownload(downloadKey);
		}
		bd.startDownload(downloadKey, new Download<PushNotificationRegistrationResponse>() {
			@Override
			public PushNotificationRegistrationResponse doDownload() {
				long userTuid = 0;
				int siteId = PointOfSale.getPointOfSale().getSiteId();
				UserStateManager userStateManager = Ui.getApplication(context).appComponent().userStateManager();
				if (userStateManager.isUserAuthenticated()) {
					User user = userStateManager.getUserSource().getUser();

					if (user != null && user.getPrimaryTraveler() != null) {
						userTuid = user.getPrimaryTraveler().getTuid();
					}
				}
				ExpediaServices services = new ExpediaServices(context);
				return services.registerForPushNotifications(serverUrl, new PushRegistrationResponseHandler(context),
						buildPushRegistrationPayload(context, regId, siteId, userTuid, null), regId);
			}
		}, unregistrationCompleteHandler);
	}

	/**
	 * This is a helper method that essentially just sends an empty flight list to the api
	 * (in the background) and thereby unregisters all of the current push notifications for
	 * the supplied regId to the default push server
	 *
	 * @param context
	 * @param regId
	 * @param unregistrationCompleteHandler
	 */
	public static void unRegister(final Context context, final String regId,
			OnDownloadComplete<PushNotificationRegistrationResponse> unregistrationCompleteHandler) {
		unRegister(context, getRegistrationUrl(), regId, unregistrationCompleteHandler);
	}

	/**
	 * Helper for unRegistering flights for a particular regId.
	 * This does the unRegistration and only logs result. Basically useful if you just want to clear
	 * the current registrations, but the registration id has not changed.
	 *
	 * @param context
	 * @param regId
	 */
	public static void unRegister(final Context context, final String regId) {
		unRegister(context, getRegistrationUrl(), regId,
				new OnDownloadComplete<PushNotificationRegistrationResponse>() {
					@Override
					public void onDownload(PushNotificationRegistrationResponse result) {
						Log.d(LOGGING_TAG, "PushNotificationUtils.unRegister regId " + regId + " complete! result:"
								+ (result == null ? "null" : "success:" + result.getSuccess()));
					}
				});
	}

	/**
	 * Generate a checksum of our JSONpayload.
	 *
	 * This was written to build a hash string of our payload which we can keep in memory to
	 * prevent us from sending the same payload to the push server twice.
	 *
	 * @param payload - The payload
	 * @return A hash of the argument
	 */
	private static String hashJsonPayload(JSONObject payload) {
		if (payload == null) {
			return null;
		}

		return hashString(payload.toString());
	}

	/**
	 * Hash a string
	 *
	 * @param strToHash
	 * @return hashed String or null if the input was bad.
	 */
	private static String hashString(String strToHash) {
		if (TextUtils.isEmpty(strToHash)) {
			return null;
		}

		try {
			InputStream payloadStream = new ByteArrayInputStream(strToHash.getBytes("UTF-8"));
			MessageDigest digester = MessageDigest.getInstance("SHA-256");
			byte[] bytes = new byte[2048];
			int byteCount;
			while ((byteCount = payloadStream.read(bytes)) > 0) {
				digester.update(bytes, 0, byteCount);
			}
			byte[] digest = digester.digest();
			return new String(digest);
		}
		catch (Exception ex) {
			Log.e(LOGGING_TAG, "Exception generating hash of string:" + strToHash);
			return null;
		}
	}

	public static Boolean isFlightAlertsNotification(Notification notification) {
		List<NotificationType> notificationTypes = Arrays.asList(NotificationType.FLIGHT_DELAYED);
		return notificationTypes.contains(notification.getNotificationType());
	}

	public static String getRegistrationUrl() {
			return REGISTRATION_URL_PRODUCTION;
	}
}
