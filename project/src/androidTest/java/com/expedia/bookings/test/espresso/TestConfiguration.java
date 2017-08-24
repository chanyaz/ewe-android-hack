package com.expedia.bookings.test.espresso;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.os.Environment;

import com.google.gson.Gson;

/*
 * Test configurations should be stored on test devices in
 * a file named 'config.json'. Config data stored in this file
 * should include api server, language and country
 */
public class TestConfiguration {

	private static final String CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String CONFIG_FILE_NAME = "config.json";

	public static class Config {
		public final String server;
		public final String language;
		public final String country;
	}

	public static boolean exists() {
		return new File(CONFIG_FILE_PATH, CONFIG_FILE_NAME).exists();
	}

	public Config getConfiguration() throws IOException {
		Gson gson = new Gson();
		Config config = gson.fromJson(new FileReader(new File(CONFIG_FILE_PATH, CONFIG_FILE_NAME)), Config.class);
		return config;
	}
}
