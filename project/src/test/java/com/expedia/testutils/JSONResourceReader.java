package com.expedia.testutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobiata.android.Log;

// http://stackoverflow.com/questions/6349759/using-json-file-in-android-app-resources
public class JSONResourceReader {
	private String jsonString;
	private static final String LOGTAG = JSONResourceReader.class.getSimpleName();

	public JSONResourceReader(String filePath) throws FileNotFoundException {
		File jsonFile = new File(filePath);
		InputStream resourceReader = new FileInputStream(jsonFile);

		Writer writer = new StringWriter();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(resourceReader, "UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				writer.write(line);
				line = reader.readLine();
			}
		}
		catch (Exception e) {
			Log.e(LOGTAG, "Unhandled exception while using JSONResourceReader", e);
		}
		finally {
			try {
				resourceReader.close();
			}
			catch (Exception e) {
				Log.e(LOGTAG, "Unhandled exception while using JSONResourceReader", e);
			}
		}

		jsonString = writer.toString();
	}

	public <T> T constructUsingGson(Class<T> type) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(jsonString, type);
	}
}
