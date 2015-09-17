package com.expedia.bookings.utils;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

public class StethoShim {

	public static void install(Application app) {
		Stetho.initialize(
			Stetho.newInitializerBuilder(app)
				.enableDumpapp(
					Stetho.defaultDumperPluginsProvider(app))
				.enableWebKitInspector(
					Stetho.defaultInspectorModulesProvider(app))
				.build());
	}

	public static void install(OkHttpClient client) {
		client.networkInterceptors().add(new StethoInterceptor());
	}

}
