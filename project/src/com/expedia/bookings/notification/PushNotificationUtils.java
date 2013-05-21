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

	private static String sGCMRegistrationId = "";

	//The server will splode if we make simultanious calls using the same regId,
	//so we create objects to synchronize with (usually this will have one entry) 
	private static final HashMap<String, Object> sPushLocks = new HashMap<String, Object>();

	/**
	 * Set the registrationId to use for push notifications and ensure that old
	 * registration ids get cleaned up.
	 * @param context
	 * @param regId
	 */
	public static void setRegistrationId(Context context, String regId) {
		//TODO: We need to persist a list of registration ids on disk, and ensure that when we get
		//a new id (which shouldn't happen often but can happen) that we SUCCESSFULLY remove all registrations
		//for the old id, otherwise we are likely to get double notifications and clutter the apis data
		//as the registrations will just persist indefinitely if we don't remove them.
		
		Log.d("PushNotificationUtils  setRegistrationId  - old:" + sGCMRegistrationId + " new:" + regId);
		
		//If this doesn't change then we don't care
		if (!sGCMRegistrationId.equals(regId)) {
			if (!TextUtils.isEmpty(sGCMRegistrationId)) {
				//Send empty list to server for old regId
				unRegister(context, sGCMRegistrationId);
			}
			
			//set the id
			sGCMRegistrationId = regId;

			//We should now tell the server about our current flights
			ItineraryManager.getInstance().startSync(true);
		}
	}

	/**
	 * The registration id to use for push notification registration
	 * @return
	 */
	public static String getRegistrationId() {
		return sGCMRegistrationId;
	}

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
	 * @param displayMessage - The display message provided by the push
	 * @param displayMessageArgs - The arguments to be formatted into the displayMessage
	 * @param context
	 */
	public static void generateNotification(Context context, int fhid, String displayMessage, String[] displayMessageArgs) {
		if (fhid >= 0) {
			ItinCardDataFlight data = (ItinCardDataFlight) ItineraryManager.getInstance()
					.getItinCardDataFromFlightHistoryId(fhid);
			if (data != null) {

				FlightLeg leg = data.getFlightLeg();

				String uniqueId = data.getId();
				long triggerTimeMillis = System.currentTimeMillis();

				Notification notification = new Notification(uniqueId, triggerTimeMillis);
				//TODO: we should set this to the appropriate type, or just make a new generic type titled push
				notification.setNotificationType(NotificationType.FLIGHT_CHECK_IN);
				notification.setFlags(Notification.FLAG_PUSH | Notification.FLAG_DIRECTIONS | Notification.FLAG_SHARE);
				notification.setIconResId(R.drawable.ic_stat_flight);

				//String formattedMessage = String.format(displayMessage, displayMessageArgs);

				notification.setTicker(displayMessage);
				notification.setTitle(displayMessage);

				String airline = leg.getAirlinesFormatted();
				String destination = StrUtils.getWaypointCityOrCode(leg.getLastWaypoint());
				String body = context.getString(R.string.x_flight_to_x_TEMPLATE, airline, destination);
				notification.setBody(body);

				String destinationCode = leg.getLastWaypoint().mAirportCode;
				notification.setImage(Notification.ImageType.DESTINATION, R.drawable.bg_itin_placeholder_flight,
						destinationCode);

				notification.save();
				notification.scheduleNotification(context);
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
	 * Given the key provided by the push notification, we return
	 * the localized string it represents
	 * 
	 * @param locKey
	 * @return
	 */
	public static String getLocStringForKey(String locKey) {
		//TODO: GET STRINGS FROM IOS
		return "FAKE_LOC_STRING";
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
	 * (in the background) and thereby unregisters all of our current push notifications
	 * 
	 * @param context
	 * @param regId
	 */
	public static void unRegister(final Context context, final String regId) {
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
					userTuid = Db.getUser().getPrimaryTraveler().getTuid();
				}
				ExpediaServices services = new ExpediaServices(context);
				return services.registerForPushNotifications(new PushRegistrationResponseHandler(context),
						buildPushRegistrationPayload(regId, userTuid, null), regId);
			}
		}, new OnDownloadComplete<PushNotificationRegistrationResponse>() {
			@Override
			public void onDownload(PushNotificationRegistrationResponse result) {
				Log.d("PushNotificationUtils.unRegister regId " + regId + " complete! result:"
						+ (result == null ? "null" : "success:" + result.getSuccess()));
			}
		});
	}
}
