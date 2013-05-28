package com.expedia.bookings.notification;

import java.text.SimpleDateFormat;
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
import com.expedia.bookings.notification.Notification.NotificationType;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PushRegistrationResponseHandler;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.flightlib.data.Flight;

public class PushNotificationUtils {

	public static final String SENDER_ID = "895052546820";
	public static final String REGISTRATION_URL = "http://ewetest.flightalerts.mobiata.com/register_for_flight_alerts";

	public static HashMap<String, Integer> sLocStringMap;

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

				String uniqueId = data.getId();
				long triggerTimeMillis = System.currentTimeMillis();

				String formattedMessage = getFormattedLocString(context, locKey, locKeyArgs);

				if (formattedMessage == null) {
					Log.e("PushNotificationUtils.generateNotification Formatted message was null for locKey:" + locKey);
				}
				else {
					Notification notification = new Notification(uniqueId, triggerTimeMillis);
					notification.setNotificationType(pushApiTypeToNotificationType(typeIntStr));
					notification.setFlags(Notification.FLAG_PUSH);
					notification.setIconResId(R.drawable.ic_stat_flight);

					String airline = leg.getAirlinesFormatted();
					String destination = StrUtils.getWaypointCityOrCode(leg.getLastWaypoint());
					String title = context.getString(R.string.x_flight_to_x_TEMPLATE, airline, destination);
					notification.setTitle(title);
					notification.setBody(formattedMessage);
					notification.setTicker(formattedMessage);

					String destinationCode = leg.getLastWaypoint().mAirportCode;
					notification.setImage(Notification.ImageType.DESTINATION, R.drawable.bg_itin_placeholder_flight,
							destinationCode);

					notification.save();
					notification.scheduleNotification(context);
				}
			}
		}
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
					JSONObject flightJson = new JSONObject();
					flightJson.put("__type__", "Flight");
					flightJson.put("arrival_date", sdf.format(f.mDestination.getBestSearchDateTime().getTime()));
					flightJson.put("departure_date", sdf.format(f.mOrigin.getBestSearchDateTime().getTime()));
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
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR", R.string.S_Push_flight_CITY_delayed_HOUR);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS", R.string.S_Push_flight_CITY_delayed_HOURS);
		sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTE", R.string.S_Push_flight_CITY_delayed_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_MINUTES", R.string.S_Push_flight_CITY_delayed_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTE", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOUR_MINUTES", R.string.S_Push_flight_CITY_delayed_HOUR_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_delayed_HOURS_MINUTE", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTE);
		sLocStringMap
				.put("S_Push_flight_CITY_delayed_HOURS_MINUTES", R.string.S_Push_flight_CITY_delayed_HOURS_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_early_MINUTE", R.string.S_Push_flight_CITY_early_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_early_MINUTES", R.string.S_Push_flight_CITY_early_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR", R.string.S_Push_flight_CITY_early_HOUR);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS", R.string.S_Push_flight_CITY_early_HOURS);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTE", R.string.S_Push_flight_CITY_early_HOUR_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTE", R.string.S_Push_flight_CITY_early_HOURS_MINUTE);
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_MINUTES", R.string.S_Push_flight_CITY_early_HOUR_MINUTES);
		sLocStringMap.put("S_Push_flight_CITY_early_HOURS_MINUTES", R.string.S_Push_flight_CITY_early_HOURS_MINUTES);
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
		sLocStringMap.put("S_Push_flight_CITY_early_HOUR_gate_GATE", R.string.S_Push_flight_CITY_early_HOUR_gate_GATE);
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
		sLocStringMap.put("S_Push_flight_from_CITY_to_CITY_cancelled",
				R.string.S_Push_flight_from_CITY_to_CITY_cancelled);
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
}
