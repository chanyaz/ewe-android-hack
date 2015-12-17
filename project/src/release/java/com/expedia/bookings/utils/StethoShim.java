package com.expedia.bookings.utils;

import android.app.Application;

import com.squareup.okhttp.OkHttpClient;

public class StethoShim {

	public static void install(Application application) {
		// Do nothing for release builds
	}

	public static void install(OkHttpClient client) {
		// Do nothing for release builds
	}
}
