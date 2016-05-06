package com.expedia.bookings.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentCookieManager implements CookieJar {
	private HashMap<String, HashMap<String, Cookie>> cookieStore = new HashMap<>();
	private File storage;
	private Gson gson;

	public PersistentCookieManager(File storage) {
		this(storage, null);
	}

	public PersistentCookieManager(File storage, File oldStorage) {
		this.storage = storage;
		//Gson doesn't use class constructors by default so it may not call vital init code.
		InstanceCreator httpCookieTypeAdapter = new InstanceCreator<HttpCookie>() {
			@Override
			public HttpCookie createInstance(Type type) {
				return new HttpCookie("fakeName", "");
			}
		};
		InstanceCreator cookieTypeAdapter = new InstanceCreator<Cookie>() {
			@Override
			public Cookie createInstance(Type type) {
				return Cookie
					.parse(HttpUrl.parse("http://www.expedia.com"), "fakeCookie=v.1,1; Domain=.expedia.com; Path=/");
			}
		};
		gson = new GsonBuilder()
			.registerTypeAdapter(Cookie.class, cookieTypeAdapter)
			.registerTypeAdapter(HttpCookie.class, httpCookieTypeAdapter)
			.create();
		loadAndDelete(oldStorage);
		load();
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		HashMap<String, Cookie> cookieMap = cookieStore.get(url.host());
		if (cookieMap == null) {
			cookieMap = new HashMap<>();
		}
		for (Cookie cookie : cookies) {
			if (cookie.expiresAt() < System.currentTimeMillis()) {
				continue;
			}
			cookieMap.put(cookie.name(), cookie);
		}
		cookieStore.put(url.host(), cookieMap);
		save();
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> cookies = new ArrayList<>();
		HashMap<String, Cookie> cookieMap = cookieStore.get(url.host());
		if (cookieMap != null) {
			cookies = new ArrayList<>(cookieMap.values());
		}
		return cookies;
	}

	public HashMap<String, HashMap<String, Cookie>> getCookieStore() {
		return cookieStore;
	}

	public void clear() {
		cookieStore.clear();
		storage.delete();
	}

	public void removeNamedCookies(String[] names) {
		for (Map.Entry<String, HashMap<String, Cookie>> entry : cookieStore.entrySet()) {
			HashMap<String, Cookie> cookies = entry.getValue();
			for (String name : names) {
				cookies.remove(name);
			}
			cookieStore.put(entry.getKey(), cookies);
		}
		save();
	}

	private void load() {
		try {
			if (!storage.exists()) {
				return;
			}

			TypeToken token = new TypeToken<HashMap<String, HashMap<String, Cookie>>>() {
			};
			BufferedReader reader = new BufferedReader(new FileReader(storage));
			HashMap<String, HashMap<String, Cookie>> savedCookies = gson.fromJson(reader, token.getType());
			reader.close();

			if (savedCookies == null) {
				return;
			}

			cookieStore.putAll(savedCookies);
		}
		catch (Exception e) {
			storage.delete();
			throw new RuntimeException(e);
		}
	}

	private void loadAndDelete(File file) {
		try {
			if (file == null || !file.exists()) {
				return;
			}

			TypeToken token = new TypeToken<List<UriCookiePair>>() {
			};
			BufferedReader reader = new BufferedReader(new FileReader(file));
			List<UriCookiePair> pairs = gson.fromJson(reader, token.getType());
			reader.close();

			if (pairs == null) {
				return;
			}

			for (UriCookiePair pair : pairs) {
				HashMap<String, Cookie> cookies = cookieStore.get(pair.uri.getHost());
				if (cookies == null) {
					cookies = new HashMap<>();
				}
				Cookie cookie = Cookie.parse(HttpUrl.get(pair.uri), pair.cookie.toString());
				cookies.put(cookie.name(), cookie);

				cookieStore.put(pair.uri.getHost(), cookies);
			}
		}
		catch (Exception e) {
			file.delete();
			throw new RuntimeException(e);
		}
		finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	private void save() {
		// Generate json
		String json = gson.toJson(cookieStore);

		try {
			storage.getParentFile().mkdirs();
			storage.createNewFile();
			FileWriter writer = new FileWriter(storage);
			writer.write(json, 0, json.length());
			writer.close();
		}
		catch (Exception e) {
			storage.delete();
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

}
