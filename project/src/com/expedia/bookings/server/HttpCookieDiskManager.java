package com.expedia.bookings.server;

import java.io.File;
import java.net.HttpCookie;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;

public class HttpCookieDiskManager {
	private static final String COOKIE_FILE_V1 = "cookies.dat";
	private static final String COOKIE_FILE_V2 = "cookies-2.dat";

	public static void save(Context context, List<HttpCookie> cookies, HashMap<HttpCookie, Long> times) {
		try {
			JSONArray arr = new JSONArray();
			for (HttpCookie cookie : cookies) {
				JSONObject obj = new JSONObject();

				obj.putOpt("comment", cookie.getComment());
				obj.putOpt("commentUrl", cookie.getCommentURL());
				obj.putOpt("domain", cookie.getDomain());
				obj.putOpt("name", cookie.getName());
				obj.putOpt("path", cookie.getPath());
				obj.putOpt("value", cookie.getValue());
				obj.putOpt("version", cookie.getVersion());
				obj.putOpt("maxAge", cookie.getMaxAge());
				obj.putOpt("portlist", cookie.getPortlist());

				arr.put(obj);
			}
			IoUtils.writeStringToFile(COOKIE_FILE_V2, arr.toString(), context);
			Log.v("Saved " + cookies.size() + " cookies");
		}
		catch (Exception e) {
			Log.e("Could not save cookies", e);
		}
	}

	public static void load(Context context, List<HttpCookie> cookies, HashMap<HttpCookie, Long> times) {
		// If V1 cookies exist we want to parse them and forget about them forever
		File v1File = context.getFileStreamPath(COOKIE_FILE_V1);
		File v2File = context.getFileStreamPath(COOKIE_FILE_V2);

		if (v1File.exists() && v2File.exists()) {
			// We do not need both
			v1File.delete();
		}

		if (v1File.exists()) {
			loadCookies(COOKIE_FILE_V1, context, cookies, times);
			Log.v("Loaded v1 cookies");
			return;
		}

		if (v2File.exists()) {
			loadCookies(COOKIE_FILE_V2, context, cookies, times);
			Log.v("Loaded v2 cookies");
			return;
		}

		Log.v("No cookies to load");
		return;
	}

	private static void loadCookies(final String filename, Context context, List<HttpCookie> cookies, HashMap<HttpCookie, Long> times) {
		try {
			String data = IoUtils.readStringFromFile(filename, context);

			if (data == null) {
				Log.w("Could not load cookie file, thread interrupted during read.");
				return;
			}

			JSONArray arr = new JSONArray(data);
			int len = arr.length();
			for (int a = 0; a < len; a++) {
				JSONObject cookieObj = arr.getJSONObject(a);
				HttpCookie cookie;
				if (COOKIE_FILE_V1.equals(filename)) {
					cookie = fromJsonV1Cookie(cookieObj);
					times.put(cookie, System.currentTimeMillis() / 1000);
				}
				else {
					cookie = fromJsonV2Cookie(cookieObj);
					try {
						long time = cookieObj.optLong("firstSeenTime", System.currentTimeMillis() / 1000);
						times.put(cookie, time);
					}
					catch (Exception e) {
						times.put(cookie, 0L);
					}
				}
				cookies.add(cookie);
			}

			Log.v("Loaded " + cookies.size() + " cookies");
		}
		catch (Exception e) {
			Log.e("Could not load cookies", e);
		}
	}

	private static HttpCookie fromJsonV1Cookie(JSONObject obj) {
		String name = obj.optString("name", null);
		String value = obj.optString("value", null);

		HttpCookie cookie = new HttpCookie(name, value);
		cookie.setComment(obj.optString("comment", null));
		cookie.setCommentURL(obj.optString("commentUrl", null));
		cookie.setDomain(obj.optString("domain", null));
		cookie.setPath(obj.optString("path", null));
		cookie.setVersion(obj.optInt("version", 0));

		try {
			if (obj.has("expiryDate")) {
				Date expiry = new Date(obj.optLong("expiryDate"));
				long maxAge = (expiry.getTime() - System.currentTimeMillis()) / 1000;
				cookie.setMaxAge(maxAge);
			}

			if (obj.has("ports")) {
				JSONArray portsArr = obj.getJSONArray("ports");
				int portsLen = portsArr.length();
				Integer[] ports = new Integer[portsLen];
				for (int b = 0; b < portsLen; b++) {
					ports[b] = portsArr.getInt(b);
				}
				cookie.setPortlist(TextUtils.join(",", ports));
			}
		}
		catch (Exception e) {
			// ignore
		}

		return cookie;
	}

	private static HttpCookie fromJsonV2Cookie(JSONObject obj) {
		String name = obj.optString("name", null);
		String value = obj.optString("value", null);

		HttpCookie cookie = new HttpCookie(name, value);
		cookie.setComment(obj.optString("comment", null));
		cookie.setCommentURL(obj.optString("commentUrl", null));
		cookie.setDomain(obj.optString("domain", null));
		cookie.setPath(obj.optString("path", null));
		cookie.setVersion(obj.optInt("version", 0));
		cookie.setMaxAge(obj.optLong("maxAge", 0));
		cookie.setPortlist(obj.optString("portlist", null));

		return cookie;
	}
}
