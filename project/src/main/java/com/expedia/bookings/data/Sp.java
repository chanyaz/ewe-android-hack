package com.expedia.bookings.data;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.BuildConfig;
import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;
import com.squareup.otto.Bus;

/**
 * Created by jdrotos on 2/27/14.
 */
public class Sp {

	public static final String BUS_NAME = "SP_BUS";

	private static SearchParams sSearchParams;
	private static Bus sBus;

	public static Bus getBus() {
		if (sBus == null) {
			sBus = new Bus(BUS_NAME);
		}
		return sBus;
	}

	public static SearchParams getParams() {
		if (sSearchParams == null) {
			sSearchParams = new SearchParams();
		}
		return sSearchParams;
	}

	public static boolean isEmpty() {
		return !getParams().hasDestination();
	}

	public static void setParams(SearchParams params, boolean report) {
		sSearchParams = params;
		if (report) {
			reportSpUpdate();
		}
	}

	public static void clear(Context context) {
		sSearchParams = new SearchParams();
		saveSearchParamsToDisk(context);
	}

	public static void reportSpUpdate() {
		getBus().post(new SpUpdateEvent());
	}

	// Otto Event Objects
	public static class SpUpdateEvent {
	}

	private Sp() {
		// ignore
	}

	/**
	 * SAVING AND RESTORING LOGIC. THIS LARGELY MIMICS Db.saveOrLoadDbForTesting.
	 */
	private static final String SP_TESTING_FILE_NAME = "sp_testing.json";

	public static void saveOrLoadForTesting(android.app.Activity activity) {
		if (BuildConfig.RELEASE) {
			throw new RuntimeException("You may not call saveOrLoadForTesting on release builds");
		}

		Intent intent = activity.getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			loadFromDisk(activity, SP_TESTING_FILE_NAME);
		}
		else {
			saveToDisk(activity, SP_TESTING_FILE_NAME);
		}
	}

	private static void loadFromDisk(Context context, String fileName) {
		try {
			if (context.getFileStreamPath(fileName).exists()) {
				SearchParams params = new SearchParams();
				params.fromJson(new JSONObject(IoUtils.readStringFromFile(fileName, context)));
				setParams(params, false);
			}
		}
		catch (Exception ex) {
			Log.e("Exception reading Sp testing data from disk", ex);
		}
	}

	private static void saveToDisk(Context context, String fileName) {
		try {
			IoUtils.writeStringToFile(fileName, getParams().toJson().toString(), context);
		}
		catch (Exception ex) {
			Log.e("Exception writing Sp testing data to disk.", ex);
		}
	}

	private static final String SP_FILE_NAME = "search_params.json";

	public static void saveSearchParamsToDisk(Context context) {
		saveToDisk(context, SP_FILE_NAME);
	}

	public static void loadSearchParamsFromDisk(Context context) {
		loadFromDisk(context, SP_FILE_NAME);
	}

}
