package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

/**
 * Stores fonts so we don't have to keep reloading them from assets
 * over and over again.
 *
 * ONLY initialize with the Application Context (so we don't leak
 * memory from any of the shorter-lived Contexts).
 */
public class FontCache {

	public enum Font {
		OCRA_STD("fonts/OCRAStd.otf", ""),
		ROBOTO_LIGHT("fonts/Roboto-Light.ttf", "name=Roboto&weight=300"),
		ROBOTO_MEDIUM("fonts/Roboto-Medium.ttf", "name=Roboto&weight=500"),
		ROBOTO_BOLD("fonts/Roboto-Bold.ttf", "name=Roboto&weight=700"),
		ROBOTO_REGULAR("fonts/Roboto-Regular.ttf", "name=Roboto"),
		SIGNERICA_FAT("fonts/Signerica_Fat.ttf", ""),
		EXPEDIASANS_REGULAR("fonts/ExpediaSans-Regular.ttf", "");

		private String mPath;
		private String mFontName;

		Font(String path, String fontName) {
			mPath = path;
			mFontName= fontName;
		}

		private String getPath() {
			return mPath;
		}
		private String getFontName() {
			return mFontName;
		}
	}

	private static Context sContext;

	private static Map<Font, Typeface> sCachedFonts = new HashMap<Font, Typeface>();
	private static Map<Font, TypefaceSpan> sCachedSpans = new HashMap<Font, TypefaceSpan>();

	private FontCache() {
		// No constructor
	}

	public static void initialize(Application app) {
		sContext = app;
		getTypeface(Font.ROBOTO_REGULAR);
	}

	private static Handler mHandler = null;

	public static Typeface getTypeface(final Font font) {
		return getTypeface(null, font);
	}

	public static Typeface getTypeface(final TextView tv, final Font font) {
		// Lazy-load fonts as necessary
		if (!sCachedFonts.containsKey(font)) {
			if (true && Strings.isNotEmpty(font.getFontName())) {
				FontRequest fontRequest = new FontRequest(
					"com.google.android.gms.fonts",
					"com.google.android.gms",
					font.getFontName(),
					R.array.com_google_android_gms_fonts_certs);
				FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
					.FontRequestCallback() {
					@Override
					public void onTypefaceRetrieved(Typeface typeface) {
						sCachedFonts.put(font, typeface);
						if(tv != null) {
							tv.setTypeface(typeface);
						}
					}

					@Override
					public void onTypefaceRequestFailed(int reason) {
						Log.d("Testinggg", "Failed to download font " + reason);
					}
				};
				FontsContractCompat
					.requestFont(sContext, fontRequest, callback,
						getHandlerThreadHandler());
			}
			else {
				sCachedFonts.put(font, Typeface.createFromAsset(sContext.getAssets(), font.getPath()));
			}
		}

		return sCachedFonts.get(font);
	}

	private static Handler getHandlerThreadHandler() {
		if (mHandler == null) {
			HandlerThread handlerThread = new HandlerThread("fonts");
			handlerThread.start();
			mHandler = new Handler(handlerThread.getLooper());
		}
		return mHandler;
	}

	public static TypefaceSpan getSpan(Font font) {
		if (!sCachedSpans.containsKey(font)) {
			sCachedSpans.put(font, new TypefaceSpan(getTypeface(font)));
		}
		return sCachedSpans.get(font);
	}

	public static void setTypeface(TextView tv, Font font) {
		if (!tv.isInEditMode()) {
			tv.setTypeface(getTypeface(tv, font));
		}
	}

	public static void setTypeface(View view, int resId, Font font) {
		TextView text = Ui.findView(view, resId);
		if (text != null) {
			setTypeface(text, font);
		}
	}
}