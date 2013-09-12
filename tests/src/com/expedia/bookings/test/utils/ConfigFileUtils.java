package com.expedia.bookings.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import android.os.Environment;

import com.mobiata.android.Log;

/*
 * Test configurations should be stored on test devices in
 * a file named 'config.data'. Config data stored in this file
 * should include things like mock proxy IP and port
 */

public class ConfigFileUtils {
	private static final String TAG = "ConfigFileUtils";
	private static final String CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static final String CONFIG_FILE_NAME = "config.data";
	private File mConfigDataFile;
	private HashMap<String, String> mConfigDataHashMap = new HashMap<String, String>();

	public ConfigFileUtils() throws IOException {
		String fullFilePath = CONFIG_FILE_PATH + "/" + CONFIG_FILE_NAME;
		mConfigDataFile = new File(fullFilePath);
		createVariableHashFromConfigFile(mConfigDataFile);
	}

	public static void getFilePath() {
		Log.d("TAG", CONFIG_FILE_PATH + '/' + CONFIG_FILE_NAME);
	}

	private void createVariableHashFromConfigFile(File file) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(mConfigDataFile));
			String line;
			String keyword;
			String value;
			int tabIndex;
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty() && (line.indexOf('\t') != -1)) {
					tabIndex = line.indexOf('\t');
					keyword = line.substring(0, tabIndex);
					value = line.substring(tabIndex + 1, line.length());
					Log.v(TAG, "Keyword: " + keyword + " -> value: " + value);
					mConfigDataHashMap.put(keyword, value);
				}
			}
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			br.close();
		}
	}

	public String getConfigValue(String keyword) {
		return mConfigDataHashMap.get(keyword);
	}
}
