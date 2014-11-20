package com.expedia.bookings.test.ui.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

import com.mobiata.android.Log;

/*
 * Test configurations should be stored on test devices in
 * a file named 'config.json'. Config data stored in this file
 * should include things like api server and screen orientation
 */

public class ConfigFileUtils {
	private static final String TAG = "ConfigFileUtils";
	private static final String CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String CONFIG_FILE_NAME = "config.json";
	private File mConfigDataFile;
	private JSONObject mJsonObject;

	public ConfigFileUtils() throws IOException, JSONException {
		mConfigDataFile = new File(CONFIG_FILE_PATH, CONFIG_FILE_NAME);
		createJSONObjectFromFile();
	}

	public String getFilePath() {
		return mConfigDataFile.toString();
	}

	private String readFileToString(File f) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					buffer.append(line);
				}
			}
			Log.v("TAG", "Returned JSON string: " + buffer.toString());
			return buffer.toString();
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			br.close();
		}
	}

	private void createJSONObjectFromFile() throws IOException, JSONException {
		String jsonString = readFileToString(mConfigDataFile);
		mJsonObject = new JSONObject(jsonString);
		Iterator<?> keys = mJsonObject.keys();
		String key;
		String val;
		while (keys.hasNext()) {
			key = (String) keys.next();
			val = mJsonObject.getString(key);
			Log.d(TAG, "Config file produced: key " + key + " -> value " + val);
		}
	}

	public String getConfigValue(String keyword) throws JSONException {
		return mJsonObject.getString(keyword);
	}

	public boolean getBooleanConfigValue(String keyword) throws JSONException {
		return mJsonObject.getBoolean(keyword);
	}

	public int getIntegerConfigValue(String keyword) throws JSONException {
		return mJsonObject.getInt(keyword);
	}

	public static boolean doesConfigFileExist() {
		return new File(CONFIG_FILE_PATH, CONFIG_FILE_NAME).exists();
	}
}
