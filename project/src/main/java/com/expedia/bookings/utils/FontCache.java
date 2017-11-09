package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

/**
 * Stores fonts so we don't have to keep reloading them from assets
 * over and over again.
 *
 * ONLY initialize with the Application Context (so we don't leak
 * memory from any of the shorter-lived Contexts).
 */
public class FontCache {

	public enum Font {
		OCRA_STD("fonts/OCRAStd.otf"),
		ROBOTO_LIGHT("fonts/Roboto-Light.ttf"),
		ROBOTO_MEDIUM("fonts/Roboto-Medium.ttf"),
		ROBOTO_BOLD("fonts/Roboto-Bold.ttf"),
		ROBOTO_REGULAR("fonts/Roboto-Regular.ttf"),
		SIGNERICA_FAT("fonts/Signerica_Fat.ttf"),
		EXPEDIASANS_REGULAR("fonts/ExpediaSans-Regular.ttf"),
		;

		private String mPath;

		Font(String path) {
			mPath = path;
		}

		private String getPath() {
			return mPath;
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
	}

	public static Typeface getTypeface(Font font) {
		// Lazy-load fonts as necessary
		if (!sCachedFonts.containsKey(font)) {
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
