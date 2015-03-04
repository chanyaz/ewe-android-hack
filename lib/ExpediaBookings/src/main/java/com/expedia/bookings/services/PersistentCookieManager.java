package com.expedia.bookings.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PersistentCookieManager extends CookieManager {

	private File storage;
	private Gson gson;

	public PersistentCookieManager(File storage) {
		super(null /*default*/, CookiePolicy.ACCEPT_ORIGINAL_SERVER);

		this.storage = storage;
		gson = new Gson();
		load();
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		super.put(uri, responseHeaders);
		save();
	}

	private void load() {
		try {
			if (!storage.exists()) {
				return;
			}

			TypeToken token = new TypeToken<List<UriCookiePair>>() { };
			BufferedReader reader = new BufferedReader(new FileReader(storage));
			List<UriCookiePair> pairs = gson.fromJson(reader, token.getType());
			reader.close();

			if (pairs == null) {
				return;
			}

			for (UriCookiePair pair : pairs) {
				getCookieStore().add(pair.uri, pair.cookie);
			}
		}
		catch (Exception e) {
			// ignore, we don't care about cookies that throw parsing exceptions
		}
	}

	private void save() {
		// Generate json
		List<UriCookiePair> pairs = new ArrayList<>();
		CookieStore store = getCookieStore();
		for (URI uri : store.getURIs()) {
			for (HttpCookie cookie : store.get(uri)) {
				pairs.add(new UriCookiePair(uri, cookie));
			}
		}
		String json = gson.toJson(pairs);

		try {
			storage.createNewFile();
			FileWriter writer = new FileWriter(storage);
			writer.write(json, 0, json.length());
			writer.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class UriCookiePair {
		public URI uri;
		public HttpCookie cookie;

		public UriCookiePair(URI uri, HttpCookie cookie) {
			this.uri = uri;
			this.cookie = cookie;
		}
	}

	// Utility

	private static List<HttpCookie> parseV2Cookies(File oldCookieStorage) {
		try {
			TypeToken token = new TypeToken<List<HttpCookie>>() { };
			BufferedReader reader = new BufferedReader(new FileReader(oldCookieStorage));
			List<HttpCookie> cookies = new Gson().fromJson(reader, token.getType());
			reader.close();
			return cookies;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static void fillWithOldCookies(CookieManager manager, File storage) {
		if (storage.exists()) {
			List<HttpCookie> oldCookies = PersistentCookieManager.parseV2Cookies(storage);
			if (oldCookies != null) {
				for (HttpCookie cookie : oldCookies) {
					cookie.setMaxAge(-1);
					manager.getCookieStore().add(null, cookie);
				}
			}

			// We don't need the old cookie file anymore
			storage.delete();
		}
	}
};
