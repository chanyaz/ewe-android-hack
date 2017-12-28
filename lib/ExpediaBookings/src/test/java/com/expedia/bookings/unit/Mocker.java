package com.expedia.bookings.unit;

import com.expedia.bookings.services.DateTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobiata.mocke3.FileSystemOpener;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Map;

import static com.mobiata.mocke3.DispatcherUtilitiesKt.loadMockResponseAndReplaceTemplateParams;

public class Mocker {
	public static <T> T object(Class<T> clazz, String mockName, Map<String, String> params) {
		try {
			String root = new File("../mocked/templates").getCanonicalPath();
			FileSystemOpener opener = new FileSystemOpener(root);
			String jsonString = loadMockResponseAndReplaceTemplateParams(mockName, opener, params);
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
					.create();
			return gson.fromJson(jsonString, clazz);
		}
		catch (Exception e) {
			return null;
		}
	}
}
