package com.expedia.bookings.notification;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.google.android.gcm.GCMRegistrar;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;

/**
 * If a client receives a new RegistrationId from GCM
 * we must tell our api to unregister all flights for the old RegistrationId.
 * Failure to do so will result in multiple push notifications for the same
 * event, and that makes us look bad. Thus we have a persistant list of registration ids
 * and we make sure to keep them around until we have SUCCESSFULLY told the server
 * to remove our old registration ids.
 * 
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
		if (sInstance == null) {
			sInstance = new GCMRegistrationKeeper(context);
		}
		return sInstance;
	}

	/**
	 * Private constructor for our singleton.
	 * @param context
	 */
	private GCMRegistrationKeeper(Context context) {
		//We load our old values from disk
		loadFromDisk(context);

		//We check with GCM, which will potentially give us a new id
		loadFromGCM(context);
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

		//Unregister old regIds with api
		unRegisterAllExpiredIds(context);
	}

	/**
	 * Tell the api about regids that should no longer get push notifications
	 * @param context
	 */
	private void unRegisterAllExpiredIds(final Context context) {
		for (final String oldRegId : mExpiredRegistrationIds) {
			PushNotificationUtils.unRegister(context, oldRegId,
					new OnDownloadComplete<PushNotificationRegistrationResponse>() {
						@Override
						public void onDownload(PushNotificationRegistrationResponse results) {
							if (results != null && results.getSuccess()) {
								onRegistrationIdSuccessfullyUnregistered(context, oldRegId);
							}
						}
					});
		}
	}

	/**
	 * Set the registrationId to be used for GCM.
	 * @param context
	 * @return
	 */
	public String getRegistrationId(Context context) {
		return mActiveRegistrationId;
	}

	/**
	 * This is the standard GCM workflow for getting a registrationId
	 * @param context
	 */
	private synchronized void loadFromGCM(Context context) {
		Log.d("GCM GCMRegistrar.checkDevice(this);");
		GCMRegistrar.checkDevice(context);
		Log.d("GCM GCMRegistrar.checkManifest(this);");
		GCMRegistrar.checkManifest(context);
		final String regId = GCMRegistrar.getRegistrationId(context);
		Log.d("GCM GCMRegistrar regId:" + regId);
		if (regId.equals("")) {
			GCMRegistrar.register(context, PushNotificationUtils.SENDER_ID);
		}
		else {
			setRegistrationId(context, regId);
			Log.v("GCM Already registered");
		}
	}

	/**
	 * Load instance from disk
	 * @param context
	 */
	private synchronized void loadFromDisk(Context context) {
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

	/**
	 * This is used as a callback for when we get a success message back from the api
	 * after attempting to unregister. This indicates that we have successfully unregistered
	 * the provided regId
	 * 
	 * @param context
	 * @param regId
	 */
	public void onRegistrationIdSuccessfullyUnregistered(Context context, String regId) {
		mExpiredRegistrationIds.remove(regId);
		PushNotificationUtils.removePayloadFromMap(regId);
		writeToDisk(context);
	}

	@Override
	public JSONObject toJson() {
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
