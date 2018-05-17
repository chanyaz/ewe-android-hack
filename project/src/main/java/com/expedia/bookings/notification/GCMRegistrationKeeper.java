package com.expedia.bookings.notification;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.iid.InstanceID;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;

import static com.google.android.gms.gcm.GoogleCloudMessaging.INSTANCE_ID_SCOPE;

/**
 * If a client receives a new RegistrationId from GCM
 * we must tell our api to unregister all flights for the old RegistrationId.
 * Failure to do so will result in multiple push notifications for the same
 * event, and that makes us look bad. Thus we have a persistant list of registration ids
 * and we make sure to keep them around until we have SUCCESSFULLY told the server
 * to remove our old registration ids.
 */
public class GCMRegistrationKeeper implements JSONable {

	private static final String FILE_NAME = "gcm_registration_keeper.dat";
	private static final String JSON_ACTIVE_REGISTRATION_ID = "JSON_ACTIVE_REGISTRATION_ID";
	private static final String JSON_EXPIRED_REGISTRATION_LIST = "JSON_EXPIRED_REGISTRATION_LIST";

	private static GCMRegistrationKeeper sInstance;

	private String mActiveRegistrationId = "";
	private Set<String> mExpiredRegistrationIds = new HashSet<String>();

	/**
	 * Get the instance of our GCMRegistrationKeeper, initializing it
	 * if it didn't previously exist.
	 *
	 * @param context
	 * @return
	 */
	public static GCMRegistrationKeeper getInstance(Context context) {
		Log.d("GCMRegistrationKeeper.getInstance called");
		if (sInstance == null) {
			Log.d("GCMRegistrationKeeper.getInstance called - instance was null, creating new instance.");
			sInstance = new GCMRegistrationKeeper(context);
		}
		return sInstance;
	}

	/**
	 * Private constructor for our singleton.
	 *
	 * @param context
	 */
	private GCMRegistrationKeeper(Context context) {
		Log.d("GCMRegistrationKeeper constructor.");
		//We load our old values from disk
		loadFromDisk(context);
		Log.d("GCMRegistrationKeeper constructor - loadFromDisk complete");

		//We check with GCM, which will potentially give us a new id
		loadFromGCM(context);
		Log.d("GCMRegistrationKeeper constructor - loadFromGCM complete");
	}

	/**
	 * Set the GCM registrationId to be used throughout the app.
	 * Do the appropriate clean up of the old registrationId if need be.
	 *
	 * @param context
	 * @param regId
	 */
	public void setRegistrationId(final Context context, final String regId) {
		Log.d("GCMRegistrationKeeper setRegistrationId - oldId:" + mActiveRegistrationId + " newId:" + regId);

		//If this doesn't change then we don't care
		if (!mActiveRegistrationId.equals(regId)) {
			if (!TextUtils.isEmpty(mActiveRegistrationId)) {
				//Unregister our old id
				final String oldRegId = mActiveRegistrationId;
				mExpiredRegistrationIds.add(oldRegId);
			}

			//set the new id
			mActiveRegistrationId = regId;
			writeToDisk(context);

		}

	}

	/**
	 * Set the registrationId to be used for GCM.
	 *
	 * @param context
	 * @return
	 */
	public String getRegistrationId(Context context) {
		Log.d("GCMRegistrationKeeper.getRegistrationId returning: \"" + mActiveRegistrationId + "\"");
		return mActiveRegistrationId;
	}

	/**
	 * This is the standard GCM workflow for getting a registrationId
	 *
	 * @param context
	 */
	private synchronized void loadFromGCM(Context context) {
		Log.d("GCMRegistrationKeeper.loadFromGCM");
		final String regId = getRegistrationId(context);
		Log.d("GCM GCMRegistrar regId:" + regId);
		if (TextUtils.isEmpty(regId)) {
			registerWithGCMInBackground(context);
		}
		else {
			setRegistrationId(context, regId);
			Log.v("GCM Already registered");
		}
	}

	private void registerWithGCMInBackground(final Context context) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					String regId = InstanceID.getInstance(context).getToken(PushNotificationUtils.SENDER_ID, INSTANCE_ID_SCOPE);
					setRegistrationId(context, regId);
				}
				catch (IOException e) {
					Log.d("GCMRegistrationKeeper: Unable to register with GCM.");
				}
				return null;
			}
		}.execute(null, null, null);
	}

	/**
	 * Load instance from disk
	 *
	 * @param context
	 */
	private synchronized void loadFromDisk(Context context) {
		Log.d("GCMRegistrationKeeper.loadFromDisk");
		File file = context.getFileStreamPath(FILE_NAME);
		if (file.exists()) {
			try {
				JSONObject obj = new JSONObject(IoUtils.readStringFromFile(FILE_NAME, context));
				fromJson(obj);
			}
			catch (Exception ex) {
				Log.e("Exception loading GCMRegistrationKeeper file.", ex);
			}
		}
	}

	/**
	 * Write the contents of our instance to disk, so if the GCM regId changes
	 * we can unregister our old registrationId even if the app has been restarted.
	 *
	 * @param context
	 */
	private synchronized void writeToDisk(Context context) {
		Log.d("GCMRegistrationKeeper.writeToDisk");
		try {
			JSONObject fileContent = toJson();
			if (fileContent != null) {
				IoUtils.writeStringToFile(FILE_NAME, fileContent.toString(), context);
			}
			else {
				File file = context.getFileStreamPath(FILE_NAME);
				if (file.exists()) {
					file.delete();
				}
			}
		}
		catch (Exception ex) {
			Log.e("Exception writing GCMRegistrationKeeper file.", ex);
		}
	}

	@Override
	public JSONObject toJson() {
		Log.d("GCMRegistrationKeeper.toJson");
		try {
			JSONObject obj = new JSONObject();
			obj.put(JSON_ACTIVE_REGISTRATION_ID, mActiveRegistrationId);
			JSONArray arr = new JSONArray();
			for (String oldRegId : mExpiredRegistrationIds) {
				arr.put(oldRegId);
			}
			obj.put(JSON_EXPIRED_REGISTRATION_LIST, arr);
			return obj;
		}
		catch (Exception ex) {
			Log.e("Exception in toJson()", ex);
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		Log.d("GCMRegistrationKeeper.fromJson");
		try {
			mActiveRegistrationId = obj.optString(JSON_ACTIVE_REGISTRATION_ID, mActiveRegistrationId);
			if (obj.has(JSON_EXPIRED_REGISTRATION_LIST)) {
				mExpiredRegistrationIds.clear();
				JSONArray arr = obj.getJSONArray(JSON_EXPIRED_REGISTRATION_LIST);
				for (int i = 0; i < arr.length(); i++) {
					String oldRegId = arr.getString(i);
					if (!TextUtils.isEmpty(oldRegId) && !mExpiredRegistrationIds.contains(oldRegId)) {
						mExpiredRegistrationIds.add(oldRegId);
					}
				}
			}
			return true;
		}
		catch (Exception ex) {
			Log.e("Exception in fromJson()", ex);
		}
		return false;
	}

}
