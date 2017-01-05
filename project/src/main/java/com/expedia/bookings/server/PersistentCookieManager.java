package com.expedia.bookings.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.utils.EncryptionUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class PersistentCookieManager implements CookieJar {
	private HashMap<String, HashMap<String, Cookie>> cookieStore = new HashMap<>();
	private File storage;
	private Gson gson;
	private EncryptionUtil encryptionUtil;

	public PersistentCookieManager(@NotNull File storage, @NotNull File oldStorage, @NotNull EncryptionUtil encryptionUtil) {
		this.storage = storage;
		this.encryptionUtil = encryptionUtil;
		//Gson doesn't use class constructors by default so it may not call vital init code.
		InstanceCreator cookieTypeAdapter = new InstanceCreator<Cookie>() {
			@Override
			public Cookie createInstance(Type type) {
				return Cookie
					.parse(HttpUrl.parse("http://www.expedia.com"), "fakeCookie=v.1,1; Domain=.expedia.com; Path=/");
			}
		};
		gson = new GsonBuilder()
			.registerTypeAdapter(Cookie.class, cookieTypeAdapter)
			.create();
		loadEncryptAndDelete(oldStorage);
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
		encryptionUtil.clear();
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

			BufferedSource source = Okio.buffer(Okio.source(storage));
			String encryptedText = source.readUtf8();

			String json = encryptionUtil.decryptStringFromBase64CipherText(encryptedText);
			HashMap<String, HashMap<String, Cookie>> savedCookies = gson.fromJson(json, token.getType());

			if (savedCookies == null) {
				return;
			}

			cookieStore.putAll(savedCookies);
		}
		catch (Exception e) {
			clear();
			throw new RuntimeException(e);
		}
	}

	private void loadEncryptAndDelete(@NotNull File file) {
		try {
			if (!file.exists()) {
				return;
			}

			TypeToken token = new TypeToken<HashMap<String, HashMap<String, Cookie>>>() {
			};
			BufferedReader reader = new BufferedReader(new FileReader(file));
			HashMap<String, HashMap<String, Cookie>> savedCookies = gson.fromJson(reader, token.getType());
			reader.close();

			if (savedCookies == null) {
				return;
			}

			cookieStore.putAll(savedCookies);
			save();
		}
		catch (Exception e) {
			file.delete();
			throw new RuntimeException(e);
		}
		finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	private void save() {
		String json = gson.toJson(cookieStore);
		try {
			storage.getParentFile().mkdirs();
			storage.createNewFile();
			String encryptedJson = encryptionUtil.encryptStringToBase64CipherText(json);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedJson.getBytes());
			Source from = Okio.source(inputStream);
			BufferedSink to = Okio.buffer(Okio.sink(storage));
			to.writeAll(from);
			to.close();
		}
		catch (Exception e) {
			clear();
			throw new RuntimeException(e);
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
