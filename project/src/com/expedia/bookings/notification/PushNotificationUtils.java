package com.expedia.bookings.notification;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.Notification.ImageType;
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PushRegistrationResponseHandler;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class PushNotificationUtils {

	public static final String SENDER_ID = "895052546820";
	public static final String REGISTRATION_URL = "http://ewetest.flightalerts.mobiata.com/register_for_flight_alerts";

	private static HashMap<String, Integer> sLocStringMap;

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
		Log.d("PushNotificationUtils.sendPayloadCheck() regId:" + regId);
		if (TextUtils.isEmpty(regId)) {
			Log.e("PushNotificationUtils.sendPayloadCheck() returning false - regId empty");
			return false;
		}

		String payloadHash = hashJsonPayload(payload);
		if (payloadHash == null) {
			Log.e("PushNotificationUtils.sendPayloadCheck() returning false - payloadHash empty");
			return false;
		}
		else {
			Log.d("PushNotificationUtils.sendPayloadCheck() payloadHash:" + payloadHash);
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
				Log.d("PushNotificationUtils.sendPayloadCheck() returning false because the payloads match. regId:"
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
			String[] locKeyArgs, String typeIntStr) {
		if (fhid < 0) {
			Log.e("PushNotificationUtils.generateNotification FlightHistoryId must be >= 0");
		}
		else {
			ItinCardDataFlight data = (ItinCardDataFlight) ItineraryManager.getInstance()
					.getItinCardDataFromFlightHistoryId(fhid);
			if (data == null) {
				Log.e("PushNotificationUtils.generateNotification couldnt find ItinCardData for fhid:" + fhid);
			}
			else {

				FlightLeg leg = data.getFlightLeg();

				String itinId = data.getId();
				long triggerTimeMillis = System.currentTimeMillis();

				String formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);

				if (formattedMessage == null) {
					Log.e("PushNotificationUtils.generateNotification Formatted message was null for locKey:" + locKey);
				}
				else {
					String uniqueId = sanitizeUniqueId(fhid + "_" + formattedMessage);

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

					String airline = leg.getAirlinesFormatted();
					String title;
					if (AndroidUtils.getSdkVersion() >= 14) {
						title = context.getString(R.string.x_flight_to_x_TEMPLATE, airline, destination);
					}
					else {
						title = context.getString(R.string.your_flight_to_x_TEMPLATE, destination);
					}
					notification.setTitle(title);
					notification.setBody(formattedMessage);
					notification.setTicker(formattedMessage);

					notification.save();
					notification.scheduleNotification(context);
				}
			}
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
		if (locKey.equals("S_Push_baggage_BAGGAGE")) {
			return -1;
		}
		else if (locKey.equals("S_Push_flight_from_CITY_to_CITY_cancelled")) {
			return 1;
		}
		else {
			return 0;
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
		Log.d("PushNotificationUtils.sanitizeUniqueId input:" + uniqueId + " output:" + retStr);
		return retStr;
	}

	/**
	 * Build the JSONObject that is to be posted to the api for registering push notifications
	 * @param token - This is api terminology, in our case it is the Registration Id provided by GCM
	 * @param tuid - The current user's tuid
	 * @param flightList - A list of flight objects we want to register for
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static JSONObject buildPushRegistrationPayload(String token, long tuid, List<Flight> flightList) {
		JSONObject retObj = new JSONObject();
		JSONObject courier = new JSONObject();
		JSONArray flights = new JSONArray();
		JSONObject user = new JSONObject();
		try {
			retObj.putOpt("__type__", "RegisterForAlertsRequest");
			retObj.putOpt("version", "4");

			user.putOpt("__type__", "ExpediaUser");
			user.put("site_id", 1);
			user.put("tuid", tuid);

			courier.put("__type__", "Courier");
			courier.put("group", "gcm");
			courier.put("token", token);

			if (flightList != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				for (Flight f : flightList) {
					Date departureDate = DateTimeUtils.getTimeInLocalTimeZone(f.mOrigin.getBestSearchDateTime());
					Date arrivalDate = DateTimeUtils.getTimeInLocalTimeZone(f.mDestination.getBestSearchDateTime());

					JSONObject flightJson = new JSONObject();
					flightJson.put("__type__", "Flight");
					flightJson.put("departure_date", sdf.format(departureDate));
					flightJson.put("arrival_date", sdf.format(arrivalDate));
					flightJson.put("destination", f.mDestination.mAirportCode);
					flightJson.put("origin", f.mOrigin.mAirportCode);
					flightJson.put("airline", f.getPrimaryFlightCode().mAirlineCode);
					flightJson.put("flight_no", f.getPrimaryFlightCode().mNumber);
					flights.put(flightJson);
				}
			}

			retObj.putOpt("courier", courier);
			retObj.putOpt("flights", flights);
			retObj.putOpt("user", user);
		}
		catch (Exception ex) {
			Log.d("Exception in buildPushRegistrationPayload", ex);
		}

		return retObj;
	}

	/**
	 * Provided the locKey and the arguments, return a formatted localized string for a notification
	 * @param context
	 * @param locKey
	 * @param args
	 * @return
	 */
	public static String getFormattedLocString(Context context, String locKey, Object[] args) {
		Log.d("PushNotificationUtils.getFormattedLocString locKey:" + locKey);
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
		sLocStringMap = new HashMap<String, Integer>();
		if (AndroidUtils.getSdkVersion() >= 14) {
			//Newer phones allow us to wrap/expand notification text, so we can use longer string versions
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR", R.string.S_Push_flight_CITY_delayed_HOUR);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS", R.string.S_Push_flight_CITY_delayed_HOURS);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE", R.string.S_Push_flight_CITY_delayed_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES", R.string.S_Push_flight_CITY_delayed_MINUTES);
			sLocStringMap
					.put("S_Push_flight_CITY_delayed_HOUR_MINUTE", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES",
					R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE",
					R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE);
			sLocStringMap
					.put("S_Push_flight_CITY_delayed_HOURS_MINUTES", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTE", R.string.S_Push_flight_CITY_early_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTES", R.string.S_Push_flight_CITY_early_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR", R.string.S_Push_flight_CITY_early_HOUR);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS", R.string.S_Push_flight_CITY_early_HOURS);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE", R.string.S_Push_flight_CITY_early_HOUR_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE", R.string.S_Push_flight_CITY_early_HOURS_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES", R.string.S_Push_flight_CITY_early_HOUR_MINUTES);
			sLocStringMap
					.put("S_Push_flight_CITY_early_HOURS_MINUTES", R.string.S_Push_flight_CITY_early_HOURS_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_MINUTE_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_MINUTES_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOUR_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOURS_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_early_MINUTE_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_early_MINUTES_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOUR_gate_GATE);
			sLocStringMap
					.put("S_Push_flight_CITY_early_HOURS_gate_GATE", R.string.S_Push_flight_CITY_early_HOURS_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOUR_MINUTE_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOURS_MINUTE_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOUR_MINUTES_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOURS_MINUTES_gate_GATE);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR", R.string.S_Push_flight_CITY_departs_in_HOUR);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS", R.string.S_Push_flight_CITY_departs_in_HOURS);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS_MINUTE",
					R.string.S_Push_flight_CITY_departs_in_HOURS_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS_MINUTES",
					R.string.S_Push_flight_CITY_departs_in_HOURS_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR_MINUTE",
					R.string.S_Push_flight_CITY_departs_in_HOUR_MINUTE);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR_MINUTES",
					R.string.S_Push_flight_CITY_departs_in_HOUR_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_MINUTES", R.string.S_Push_flight_CITY_departs_in_MINUTES);
			sLocStringMap.put("S_Push_flight_CITY_gate_GATE", R.string.S_Push_flight_CITY_gate_GATE);
		}
		else {
			//Older phones cant wrap notifications so we use shorter strings.
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR", R.string.S_Push_flight_CITY_delayed_HOUR_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS", R.string.S_Push_flight_CITY_delayed_HOURS_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE", R.string.S_Push_flight_CITY_delayed_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES", R.string.S_Push_flight_CITY_delayed_MINUTES_short);
			sLocStringMap
					.put("S_Push_flight_CITY_delayed_HOUR_MINUTE",
							R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES",
					R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE",
					R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE_short);
			sLocStringMap
					.put("S_Push_flight_CITY_delayed_HOURS_MINUTES",
							R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTE", R.string.S_Push_flight_CITY_early_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTES", R.string.S_Push_flight_CITY_early_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR", R.string.S_Push_flight_CITY_early_HOUR_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS", R.string.S_Push_flight_CITY_early_HOURS_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE",
					R.string.S_Push_flight_CITY_early_HOUR_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE",
					R.string.S_Push_flight_CITY_early_HOURS_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES",
					R.string.S_Push_flight_CITY_early_HOUR_MINUTES_short);
			sLocStringMap
					.put("S_Push_flight_CITY_early_HOURS_MINUTES",
							R.string.S_Push_flight_CITY_early_HOURS_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_MINUTE_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_MINUTES_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOUR_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOURS_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_early_MINUTE_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_early_MINUTES_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOUR_gate_GATE_short);
			sLocStringMap
					.put("S_Push_flight_CITY_early_HOURS_gate_GATE",
							R.string.S_Push_flight_CITY_early_HOURS_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOUR_MINUTE_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOURS_MINUTE_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOUR_MINUTES_gate_GATE_short);
			sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTES_gate_GATE",
					R.string.S_Push_flight_CITY_early_HOURS_MINUTES_gate_GATE_short);

			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR", R.string.S_Push_flight_CITY_departs_in_HOUR_short);
			sLocStringMap
					.put("S_Push_flight_CITY_departs_in_HOURS", R.string.S_Push_flight_CITY_departs_in_HOURS_short);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS_MINUTE",
					R.string.S_Push_flight_CITY_departs_in_HOURS_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOURS_MINUTES",
					R.string.S_Push_flight_CITY_departs_in_HOURS_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR_MINUTE",
					R.string.S_Push_flight_CITY_departs_in_HOUR_MINUTE_short);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_HOUR_MINUTES",
					R.string.S_Push_flight_CITY_departs_in_HOUR_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_departs_in_MINUTES",
					R.string.S_Push_flight_CITY_departs_in_MINUTES_short);
			sLocStringMap.put("S_Push_flight_CITY_gate_GATE", R.string.S_Push_flight_CITY_gate_GATE_short);
		}

		//These are the same for both versions...
		sLocStringMap.put("S_Push_flight_from_CITY_to_CITY_cancelled",
				R.string.S_Push_flight_from_CITY_to_CITY_cancelled);
		sLocStringMap.put("S_Push_baggage_BAGGAGE", R.string.S_Push_baggage_BAGGAGE);
	}

	/**
	 * Get the NotificationType from the "type" value returned from the api
	 * @param typeIntStr - The "type" string returned from the api
	 * @return - NotificationType, default to NotificationType.FLIGHT_GATE_TIME_CHANGE;
	 */
	public static NotificationType pushApiTypeToNotificationType(String typeIntStr) {
		try {
			int iType = Integer.parseInt(typeIntStr);
			if (NotificationType.values().length > iType) {
				return NotificationType.values()[iType];
			}
		}
		catch (Exception ex) {
			Log.e("Type couldn't be converted from type:" + typeIntStr + " to valid NotificationType enum", ex);
		}
		//Default as this is largely used for tracking only
		return NotificationType.FLIGHT_GATE_TIME_CHANGE;
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
	 * @param regId
	 */
	public static void unRegister(final Context context, final String regId,
			OnDownloadComplete<PushNotificationRegistrationResponse> unregistrationCompleteHandler) {
		Log.d("PushNotificationUtils.unRegister regId " + regId);
		String downloadKey = buildUnregisterDownloadKey(regId);
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(downloadKey)) {
			bd.cancelDownload(downloadKey);
		}
		bd.startDownload(downloadKey, new Download<PushNotificationRegistrationResponse>() {
			@Override
			public PushNotificationRegistrationResponse doDownload() {
				long userTuid = 0;
				if (User.isLoggedIn(context)) {
					if (Db.getUser() == null) {
						Db.loadUser(context);
					}
					if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null) {
						userTuid = Db.getUser().getPrimaryTraveler().getTuid();
					}
				}
				ExpediaServices services = new ExpediaServices(context);
				return services.registerForPushNotifications(new PushRegistrationResponseHandler(context),
						buildPushRegistrationPayload(regId, userTuid, null), regId);
			}
		}, unregistrationCompleteHandler);
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
		unRegister(context, regId, new OnDownloadComplete<PushNotificationRegistrationResponse>() {
			@Override
			public void onDownload(PushNotificationRegistrationResponse result) {
				Log.d("PushNotificationUtils.unRegister regId " + regId + " complete! result:"
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
			Log.e("Exception generating hash of string:" + strToHash);
			return null;
		}
	}
}
