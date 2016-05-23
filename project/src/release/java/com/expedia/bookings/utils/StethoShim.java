package com.expedia.bookings.utils;

import android.app.Application;

import okhttp3.OkHttpClient;

public class StethoShim {

	public static void install(Application application) {
		// Do nothing for release builds
	}

	public static void install(OkHttpClient.Builder client) {
		// Do nothing for release builds
	}
}
