package com.expedia.bookings.data;

import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AirlineCheckInIntervals {

	private static final String ASSET_FILENAME = "ExpediaSharedData/AirlineCheckInIntervals.json";

	private static Map<String, Integer> sIntervals;

	/**
	 * Returns the number of seconds prior to flight time that checkin is available for the passed
	 * airline. For instance, an airline with a 24h checkin window will return 86400 here.
	 * Note: this may be relatively slow the first time it's loaded, it reads an asset from disk.
	 * @param context
	 * @param airline The two-character airline code, such as "WN"
	 * @return the integer number of seconds, with default 24 hours.
	 */
	public static int get(Context context, String airline) {
		if (sIntervals == null) {
			init(context);
		}

		if (sIntervals.containsKey(airline)) {
			return sIntervals.get(airline);
		}

		return (int) TimeUnit.DAYS.toSeconds(1);
	}

	private static void init(Context context) {
		long start = System.nanoTime();

		try {
			if (sIntervals == null) {
				sIntervals = new HashMap<>();
			}
			else {
				sIntervals.clear();
			}

			InputStream is = context.getAssets().open(ASSET_FILENAME);
			String data = IoUtils.convertStreamToString(is);
			JSONObject checkinData = new JSONObject(data);

			@SuppressWarnings("unchecked")
			Iterator<String> keys = checkinData.keys();

			while (keys.hasNext()) {
				String airline = keys.next();
				int interval = checkinData.getInt(airline);
				sIntervals.put(airline, interval);
			}
		}
		catch (Exception e) {
			// If the POSes fail to load, then we should fail horribly
			throw new RuntimeException(e);
		}

		Log.i("Loaded " + ASSET_FILENAME + " data in " + (System.nanoTime() - start) / 1000000 + " ms");
	}
}
