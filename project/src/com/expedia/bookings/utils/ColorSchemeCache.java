package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.expedia.bookings.bitmaps.ColorScheme;
import com.expedia.bookings.bitmaps.DominantColorCalculator;
import com.mobiata.android.Log;
import com.mobiata.android.util.TimingLogger;

/**
 * This class encapsulates the long-ish running task of calculating the color scheme and
 * dominant color for a particular bitmap. It also caches the results since we know they'll be used
 * a few times per hotel.
 * <p/>
 * Created by dmelton on 4/18/14.
 */
public class ColorSchemeCache {

	private static Map<String, ColorScheme> sCachedSchemes = new HashMap<String, ColorScheme>();

	public interface Callback {
		public void callback(ColorScheme colorScheme);
	}

	/**
	 * Note: result will not be cached using this call.
	 * Consider getScheme(String url, Callback callback) instead.
	 *
	 * @param bitmap
	 * @param callback
	 */
	public static void getScheme(Bitmap bitmap, Callback callback) {
		new ColorSchemeTask(null, callback).execute(bitmap);
	}

	public static void getScheme(String url, Bitmap bitmap, Callback callback) {
		new ColorSchemeTask(url, callback).execute(bitmap);
	}

	private static class ColorSchemeTask extends AsyncTask<Bitmap, Void, ColorScheme> {
		private String mUrl;
		private Callback mCallback;

		protected ColorSchemeTask(String url, Callback callback) {
			mUrl = url;
			mCallback = callback;
		}

		@Override
		protected ColorScheme doInBackground(Bitmap... bitmaps) {
			if (sCachedSchemes.containsKey(mUrl)) {
				// Cache hit!
				return sCachedSchemes.get(mUrl);
			}

			TimingLogger startupTimer = new TimingLogger("ExpediaBookings", "BitmapColorAveraging");
			DominantColorCalculator dominantColorCalculator = new DominantColorCalculator(bitmaps[0]);
			startupTimer.addSplit("Bitmap average color scheme obtained.");
			startupTimer.dumpToLog();

			return dominantColorCalculator.getColorScheme();
		}

		@Override
		protected void onPostExecute(ColorScheme colorScheme) {
			if (mUrl != null) {
				sCachedSchemes.put(mUrl, colorScheme);
			}
			mCallback.callback(colorScheme);
		}
	}

	;


}
