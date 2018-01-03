package com.mobiata.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingUtils {
	public static boolean contains(Context context, int keyResId) {
		return contains(context, context.getString(keyResId));
	}

	public static boolean contains(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.contains(key);
	}

	public static void save(Context context, int keyResId, boolean value) {
		save(context, context.getString(keyResId), value);
	}

	public static void save(Context context, String key, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public static void saveSynchronously(Context context, String key, boolean value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}


	public static void save(Context context, int keyResId, float value) {
		save(context, context.getString(keyResId), value);
	}

	public static void save(Context context, String key, float value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.apply();
	}

	public static void save(Context context, int keyResId, int value) {
		save(context, context.getString(keyResId), value);
	}

	public static void save(Context context, String key, int value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}

	public static void save(Context context, int keyResId, long value) {
		save(context, context.getString(keyResId), value);
	}

	public static void save(Context context, String key, long value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key, value);
		editor.apply();
	}

	public static void save(Context context, int keyResId, String value) {
		save(context, context.getString(keyResId), value);
	}

	public static void save(Context context, String key, String value) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public static boolean get(Context context, int keyResId, boolean defValue) {
		return get(context, context.getString(keyResId), defValue);
	}

	public static boolean get(Context context, String key, boolean defValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(key, defValue);
	}

	public static float get(Context context, int keyResId, float defValue) {
		return get(context, context.getString(keyResId), defValue);
	}

	public static float get(Context context, String key, float defValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getFloat(key, defValue);
	}

	public static int get(Context context, int keyResId, int defValue) {
		return get(context, context.getString(keyResId), defValue);
	}

	public static int get(Context context, String key, int defValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try {
			return prefs.getInt(key, defValue);
		}
		catch (ClassCastException e) {
			String prefString = prefs.getString(key, "");
			int radix = 10;
			if (prefString.startsWith("@")) {
				prefString = prefString.substring(1);
			}
			else if (prefString.startsWith("0x")) {
				prefString = prefString.substring(2);
				radix = 16;
			}

			return Integer.valueOf(prefString, radix);
		}
	}

	public static long get(Context context, int keyResId, long defValue) {
		return get(context, context.getString(keyResId), defValue);
	}

	public static long get(Context context, String key, long defValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getLong(key, defValue);
	}

	public static String get(Context context, int keyResId, String defValue) {
		return get(context, context.getString(keyResId), defValue);
	}

	public static String get(Context context, String key, String defValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, defValue);
	}

	public static void remove(Context context, int keyResId) {
		remove(context, context.getString(keyResId));
	}

	public static void remove(Context context, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.apply();
	}
}
