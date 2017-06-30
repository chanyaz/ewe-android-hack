package com.expedia.bookings.server;

import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.bookings.utils.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class PersistentCookieManagerV2 extends CookieManager implements PersistentCookiesCookieJar {
	private android.webkit.CookieManager webkitCookieManager;
	private File storage;
	private Gson gson;

	public PersistentCookieManagerV2(File storage) {
		this(storage, null);
	}

	public PersistentCookieManagerV2(File storage, File oldStorage) {
		this.storage = storage;
		this.webkitCookieManager = android.webkit.CookieManager.getInstance();
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
		loadAndDelete();
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		super.put(uri, responseHeaders);
		if ((uri == null) || (responseHeaders == null)) {
			return;
		}
		String url = uri.toString();
		for (String headerKey : responseHeaders.keySet()) {
			if ((headerKey == null) || !(headerKey.equalsIgnoreCase("Set-Cookie"))) {
				continue;
			}
			for (String headerValue : responseHeaders.get(headerKey)) {
				if (Strings.isNotEmpty(headerValue)) {
					webkitCookieManager.setCookie(url, headerValue);
				}
			}
		}
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
		if ((uri == null) || (requestHeaders == null)) {
			throw new IllegalArgumentException("Argument is null");
		}
		String url = uri.toString();
		String cookie = webkitCookieManager.getCookie(url);
		if (cookie != null) {
			requestHeaders.put("Cookie", Arrays.asList(cookie));
		}
		return requestHeaders;
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		HashMap<String, List<String>> generatedResponseHeaders = new HashMap<>();
		ArrayList<String> cookiesList = new ArrayList<>();
		for (Cookie cookie : cookies) {
			// toString correctly generates a normal cookie string
			cookiesList.add(cookie.toString());
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


	public String getCookieValue(HttpUrl url, String cookieKey) {
		List<Cookie> cookieList = this.loadForRequest(url);
		for (Cookie cookie : cookieList) {
			if (cookie.name().equalsIgnoreCase(cookieKey)) {
				return cookie.value();
			}
		}
		return "";
	}

	@Override
	public void clear() {
		webkitCookieManager.removeAllCookie();
		storage.delete();
	}

	@Override
	public void removeNamedCookies(String endpointUrl, String[] names) {
		for (String cookieName : names) {
			clearCookies(endpointUrl, cookieName);
		}
	}

	private void clearCookies(String domain, String name) {
		String cookieString = webkitCookieManager.getCookie(domain);
		HttpUrl url = HttpUrl.parse(domain);
		if (Strings.isNotEmpty(cookieString)) {
			String[] cookies = cookieString.split(";");
			for (int i = 0; i < cookies.length; i++) {
				String[] cookieParts = cookies[i].split("=");
				final String nameOfCookie = cookieParts[0].trim();
				if (nameOfCookie.equals(name)) {
					String urlHost = url.host();
					webkitCookieManager
						.setCookie(domain, nameOfCookie + "=; domain=" + (urlHost.contains("www.") ? urlHost.split("www.")[1] : urlHost) + ";");
				}
			}
		}
	}

	/**
	 * This method is used to set the specified POS URL
	 * Entry's MC1 Cookie to the input GUID
	 *
	 * @param guid   Must take the format: "GUID=1234abcd..."
	 * @param posUrl Must take the format: "expedia.com"
	 */
	@Override
	public void setMC1Cookie(String guid, String posUrl) {
		Map<String, List<String>> value = new HashMap<>();
		List<String> headers = new ArrayList<>();
		headers.add(generateMC1Cookie(guid, posUrl));
		value.put("Set-Cookie", headers);
		try {
			put(URI.create(posUrl), value);
		}
		catch (IOException e) {
			Log.e("Error adding cookies through okhttp" + e.toString());
		}
	}

	private void loadAndDelete() {
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
			Set<String> savedCookieIterator = savedCookies.keySet();
			for (String cookieKey : savedCookieIterator) {
				HashMap<String, Cookie> cookieMap = savedCookies.get(cookieKey);
				Set<String> iterator = cookieMap.keySet();
				List<String> headers = new ArrayList<>();
				Map<String, List<String>> value = new HashMap<>();

				for (String key : iterator) {
					Cookie cookie = cookieMap.get(key);
					headers.add(cookie.toString());
				}
				value.put("Set-Cookie", headers);
				try {
					put(URI.create(cookieKey), value);
				}
				catch (IOException e) {
					Log.e("Error adding cookies through okhttp" + e.toString());
				}
			}
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

	private long fiveYearsFromNowInMilliseconds() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 5);
		calendar.getTime();
		return calendar.getTimeInMillis();
	}

	private String generateMC1Cookie(String guid, String posUrl) {
		Cookie.Builder cookieBuilder = new Cookie.Builder();
		posUrl = posUrl.replace("www.", "");
		cookieBuilder.domain(posUrl);
		cookieBuilder.expiresAt(fiveYearsFromNowInMilliseconds());
		cookieBuilder.name("MC1");
		cookieBuilder.value(guid);
		return cookieBuilder.build().toString();
	}

}
