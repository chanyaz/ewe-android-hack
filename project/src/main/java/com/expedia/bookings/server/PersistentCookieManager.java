package com.expedia.bookings.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.Log;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentCookieManager extends CookieManager implements CookieJar {
	private android.webkit.CookieManager webkitCookieManager;
	private HashMap<String, HashMap<String, Cookie>> cookieStore = new HashMap<>();
	private File storage;
	private Gson gson;

	public PersistentCookieManager(File storage) {
		this(storage, null);
	}

	public PersistentCookieManager(File storage, File oldStorage) {
		this.storage = storage;
		this.webkitCookieManager = android.webkit.CookieManager.getInstance();
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
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		super.put(uri, responseHeaders);
		if ((uri == null) || (responseHeaders == null)) {
			return;
		}

		// save our url once
		String url = uri.toString();

		// go over the headers
		for (String headerKey : responseHeaders.keySet()) {
			// ignore headers which aren't cookie related
			if ((headerKey == null)
				|| !(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey
				.equalsIgnoreCase("Set-Cookie"))) {
				continue;
			}

			// process each of the headers
			for (String headerValue : responseHeaders.get(headerKey)) {
				webkitCookieManager.setCookie(url, headerValue);
			}
		}
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
		// make sure our args are valid
		if ((uri == null) || (requestHeaders == null)) {
			throw new IllegalArgumentException("Argument is null");
		}

		// save our url once
		String url = uri.toString();

		// prepare our response
		Map<String, List<String>> res = new java.util.HashMap<String, List<String>>();

		// get the cookie
		String cookie = webkitCookieManager.getCookie(url);

		// return it
		if (cookie != null) {
			res.put("Cookie", Arrays.asList(cookie));
		}

		return res;
		// return super.get(uri, requestHeaders);
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		HashMap<String, List<String>> generatedResponseHeaders = new HashMap<>();
		ArrayList<String> cookiesList = new ArrayList<>();
		for (Cookie c : cookies) {
			// toString correctly generates a normal cookie string
			cookiesList.add(c.toString());
		}

		generatedResponseHeaders.put("Set-Cookie", cookiesList);
		try {
			put(url.uri(), generatedResponseHeaders);
		}
		catch (IOException e) {
			Log.e("Error adding cookies through okhttp" + e.toString());
		}
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		ArrayList<Cookie> cookieArrayList = new ArrayList<>();
		try {
			Map<String, List<String>> cookieList = get(url.uri(), new HashMap<String, List<String>>());
			// Format here looks like: "Cookie":["cookie1=val1;cookie2=val2;"]
			for (List<String> ls : cookieList.values()) {
				for (String s : ls) {
					String[] cookies = s.split(";");
					for (String cookie : cookies) {
						Cookie c = Cookie.parse(url, cookie);
						cookieArrayList.add(c);
					}
				}
			}
		}
		catch (IOException e) {
			Log.e("error making cookie!", e.toString());
		}
		return cookieArrayList;
	}

	public HashMap<String, HashMap<String, Cookie>> getAppCookieStore() {
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

	/**
	 * This method is used to set the specified POS URL
	 * Entry's MC1 Cookie to the input GUID
	 * @param guid Must take the format: "GUID=1234abcd..."
	 * @param posUrl Must take the format: "expedia.com"
	 */
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

	private long fiveYearsFromNowInMilliseconds() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 5);
		calendar.getTime();
		return calendar.getTimeInMillis();
	}

	private Cookie generateMC1Cookie(String guid, String posUrl) {
		Cookie.Builder cookieBuilder = new Cookie.Builder();
		posUrl = posUrl.replace("www.","");
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
