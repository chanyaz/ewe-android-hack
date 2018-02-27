package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.provider.FontsContractCompat;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.fonts.FontProvider;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.tracking.DownloadableFontsFailureLogger;
import com.expedia.bookings.tracking.DownloadableFontsSuccessLogger;
import com.expedia.bookings.tracking.FontDownloadTimeClientLog;
import com.expedia.bookings.tracking.TimeSource;
import com.mobiata.android.Log;

import static com.expedia.bookings.utils.FeatureUtilKt.isDownloadableFontsEnabled;

/**
 * Stores fonts so we don't have to keep reloading them from assets
 * over and over again.
 * <p>
 * ONLY initialize with the Application Context (so we don't leak
 * memory from any of the shorter-lived Contexts).
 */
public class FontCache {

	public enum Font {
		ROBOTO_LIGHT("fonts/Roboto-Light.ttf", "name=Roboto&weight=300"),
		ROBOTO_MEDIUM("fonts/Roboto-Medium.ttf", "name=Roboto&weight=500"),
		ROBOTO_BOLD("fonts/Roboto-Bold.ttf", "name=Roboto&weight=700"),
		ROBOTO_REGULAR("fonts/Roboto-Regular.ttf", "name=Roboto");

		private String mPath;
		private String mQuery;
		private FontsContractCompat.FontRequestCallback requestCallback;
		private DownloadableFontsSuccessLogger successLogger;
		private DownloadableFontsFailureLogger failureLogger;

		Font(String path, String query) {
			mPath = path;
			mQuery = query;
		}

		public String getPath() {
			return mPath;
		}

		public String getQuery() {
			return mQuery;
		}

		public void setRequestCallback(FontsContractCompat.FontRequestCallback callback) {
			requestCallback = callback;
		}

		public FontsContractCompat.FontRequestCallback getRequestCallback() {
			return requestCallback;
		}

		public void setLoggers(TimeSource timeSource) {
			successLogger = new DownloadableFontsSuccessLogger(this.toString(), timeSource);
			failureLogger = new DownloadableFontsFailureLogger(this.toString(), timeSource);
		}

		public DownloadableFontsSuccessLogger getSuccessLogger() {
			return successLogger;
		}

		public DownloadableFontsFailureLogger getFailureLogger() {
			return failureLogger;
		}

		public void markDownloadSuccess(Map<Font, Typeface> cachedFonts, Typeface typeface,
			IClientLogServices clientLogServices) {
			successLogger.setEndTime();
			cachedFonts.put(this, typeface);
			Log.d(TAG, "Font download success : " + mQuery);
			FontDownloadTimeClientLog.trackDownloadTimeLogger(successLogger, clientLogServices);
		}

		public void markDownloadFailed(Context context, Map<Font, Typeface> cachedFonts, int reason,
			IClientLogServices clientLogServices) {
			failureLogger.setEndTime();
			cachedFonts.put(this, Typeface.createFromAsset(context.getAssets(), mPath));
			Log.d(TAG, "Font download failed : " + reason);
			FontDownloadTimeClientLog.trackDownloadTimeLogger(failureLogger, clientLogServices);
		}
	}

	private final static String TAG = "FontCache";

	private static Context sContext;
	private static Map<Font, Typeface> sCachedFonts = new HashMap<Font, Typeface>();
	private static Map<Font, TypefaceSpan> sCachedSpans = new HashMap<Font, TypefaceSpan>();

	private FontCache() {
		// No constructor
	}

	public static void initialize(Application app) {
		sContext = app;
	}

	public static Map<Font, Typeface> getCachedFonts() {
		return sCachedFonts;
	}

	public static void downloadFonts(final Context context, final FontProvider fontProvider,
		final IClientLogServices clientLogServices, final Map<Font, Typeface> cachedFonts,
		final TimeSource timeSource) {
		if (isDownloadableFontsEnabled(context)) {
			for (final Font font : Font.values()) {

				FontsContractCompat.FontRequestCallback callback = new FontsContractCompat.FontRequestCallback() {
					@Override
					public void onTypefaceRetrieved(Typeface typeface) {
						font.markDownloadSuccess(cachedFonts, typeface, clientLogServices);
					}

					@Override
					public void onTypefaceRequestFailed(int reason) {
						font.markDownloadFailed(context, cachedFonts, reason, clientLogServices);
					}
				};

				font.setLoggers(timeSource);
				font.setRequestCallback(callback);
				downloadFont(fontProvider, context, font);
			}
		}
	}

	private static void downloadFont(FontProvider fontProvider, Context context, Font font) {
		font.getSuccessLogger().setStartTime();
		font.getFailureLogger().setStartTime();
		fontProvider.downloadFont(context, font);
	}

	public static Typeface getTypeface(final Font font) {
		if (!sCachedFonts.containsKey(font)) {
			//if Downloadable font AB Test is enabled, we would only enter here when
			//either the font is downloading in the background already, or the download failed, this would still act as fallback
			//Put it in cache from assets, once the font is downloaded, it would be replaced in the cache
			if (isDownloadableFontsEnabled(sContext)) {
				Log.d(TAG, "Font created from assets and cached since download copy wasn't available yet");
			}
			sCachedFonts.put(font, Typeface.createFromAsset(sContext.getAssets(), font.getPath()));
		}
		return sCachedFonts.get(font);
	}

	public static TypefaceSpan getSpan(Font font) {
		if (!sCachedSpans.containsKey(font)) {
			sCachedSpans.put(font, new TypefaceSpan(getTypeface(font)));
		}
		return sCachedSpans.get(font);
	}

	public static void setTypeface(TextView tv, Font font) {
		if (!tv.isInEditMode()) {
			tv.setTypeface(getTypeface(font));
		}
	}

	public static void setTypeface(View view, int resId, Font font) {
		TextView text = Ui.findView(view, resId);
		if (text != null) {
			setTypeface(text, font);
		}
	}
}
