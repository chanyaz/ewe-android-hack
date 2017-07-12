package com.expedia.bookings.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class PersistentCookieManager implements PersistentCookiesCookieJar {
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
				HttpUrl url = new HttpUrl.Builder().scheme("http").host("www.expedia.com").build();
				return Cookie.parse(url, "fakeCookie=v.1,1; Domain=.expedia.com; Path=/");
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
	public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
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
	public List<Cookie> loadForRequest(@NotNull HttpUrl url) {
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

	@Override
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

	@Override
	public void removeNamedCookies(String endpointURL, String[] names) {
		removeNamedCookies(names);
	}

	/**
	 * This method is used to set the specified POS URL
	 * Entry's MC1 Cookie to the input GUID
	 * @param guid Must take the format: "GUID=1234abcd..."
	 * @param posUrl Must take the format: "expedia.com"
	 */
	@Override
	public void setMC1Cookie(String guid, String posUrl) {
		String urlKey = posUrl;
		if (cookieStore.containsKey(urlKey)) {
			HashMap<String, Cookie> cookies = cookieStore.get(urlKey);
			cookies.put("MC1", generateMC1Cookie(guid, posUrl));
			cookieStore.put(urlKey, cookies);
			save();
		}
		else {
			createNewEntryWithMC1Cookie(guid, posUrl);
		}
	}

	private void load() {
		BufferedReader reader = null;
		try {
			if (!storage.exists()) {
				return;
			}

			TypeToken token = new TypeToken<HashMap<String, HashMap<String, Cookie>>>() {
			};
			reader = new BufferedReader(new FileReader(storage));
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
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadAndDelete(File file) {
		BufferedReader reader = null;
		try {
			if (file == null || !file.exists()) {
				return;
			}

			TypeToken token = new TypeToken<List<UriCookiePair>>() {
			};
			reader = new BufferedReader(new FileReader(file));
			List<UriCookiePair> pairs = gson.fromJson(reader, token.getType());
			reader.close();

			if (pairs == null) {
				return;
			}

			putLoadedCookiesIntoStore(pairs);
		}
		catch (Exception e) {
			file.delete();
			throw new RuntimeException(e);
		}
		finally {
			try {
				if (file != null && file.exists()) {
					file.delete();
				}
				if (reader != null) {
					reader.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void putLoadedCookiesIntoStore(List<UriCookiePair> pairs) {
		for (UriCookiePair pair : pairs) {
			HashMap<String, Cookie> cookies = cookieStore.get(pair.uri.getHost());
			if (cookies == null) {
				cookies = new HashMap<>();
			}

			HttpUrl url = HttpUrl.get(pair.uri);

			if (url == null) {
				continue;
			}

			Cookie cookie = Cookie.parse(url, pair.cookie.toString());

			if (cookie == null) {
				continue;
			}

			cookies.put(cookie.name(), cookie);

			cookieStore.put(pair.uri.getHost(), cookies);
		}
	}

	private void save() {
		// Generate json
		String json = gson.toJson(cookieStore);
		FileWriter writer = null;
		try {
			storage.getParentFile().mkdirs();
			storage.createNewFile();
			writer = new FileWriter(storage);
			writer.write(json, 0, json.length());
			writer.close();
		}
		catch (Exception e) {
			storage.delete();
			throw new RuntimeException(e);
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
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

	private long fiveYearsFromNowInMilliseconds() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 5);
		calendar.getTime();
		return calendar.getTimeInMillis();
	}

	private Cookie generateMC1Cookie(String guid, String posUrl) {
		Cookie.Builder cookieBuilder = new Cookie.Builder();
		posUrl = posUrl.replace("www.", "");
		cookieBuilder.domain(posUrl);
		cookieBuilder.expiresAt(fiveYearsFromNowInMilliseconds());
		cookieBuilder.name("MC1");
		cookieBuilder.value(guid);
		return cookieBuilder.build();
	}

	private void createNewEntryWithMC1Cookie(String guid, String posUrl) {
		HashMap<String, Cookie> cookies = new HashMap<>();
		cookies.put("MC1", generateMC1Cookie(guid, posUrl));
		cookieStore.put(posUrl, cookies);
		save();
	}

}
