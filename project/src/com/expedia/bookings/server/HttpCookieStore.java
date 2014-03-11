package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

public class HttpCookieStore implements CookieStore {
	private static final String TAG = "EBCookie";
	private HashMap<URI, List<CookieHolder>> mMap = new HashMap<URI, List<CookieHolder>>();
	private boolean mShouldLog = false;

	public void updateSettings(Context context) {
		mShouldLog = !AndroidUtils.isRelease(context) && SettingUtils.get(context, context.getString(R.string.preference_cookie_logging), false);
	}

	@Override
	public void add(URI uri, HttpCookie cookie) {
		if (mShouldLog) {
			log("add:", uri, cookie);
		}

		List<CookieHolder> holders;
		if (mMap.containsKey(uri)) {
			holders = mMap.get(uri);
		}
		else {
			holders = new ArrayList<CookieHolder>();
			mMap.put(uri, holders);
		}

		add(uri, holders, cookie);
	}

	private void add(URI uri, List<CookieHolder> holders, HttpCookie cookie) {
		remove(holders, cookie);
		CookieHolder newHolder = new CookieHolder(uri, cookie);
		holders.add(newHolder);
	}

	@Override
	public List<HttpCookie> get(URI uri) {
		List<HttpCookie> results = new ArrayList<HttpCookie>();

		List<CookieHolder> holders = mMap.get(uri);
		if (holders != null) {
			for (CookieHolder holder : holders) {
				if (!holder.isExpired()) {
					results.add(holder.getCookie());
				}
			}
		}

		if (mShouldLog) {
			log("get:", uri, results);
		}
		return results;
	}

	@Override
	public List<HttpCookie> getCookies() {
		List<HttpCookie> allCookies = new ArrayList<HttpCookie>();
		for (URI uri : getURIs()) {
			List<HttpCookie> cookies = get(uri);
			allCookies.addAll(cookies);
		}

		return allCookies;
	}

	@Override
	public List<URI> getURIs() {
		ArrayList<URI> uris = new ArrayList<URI>(mMap.keySet().size());
		for (URI uri: mMap.keySet()) {
			uris.add(uri);
		}
		return uris;
	}

	@Override
	public boolean remove(URI uri, HttpCookie cookie) {
		if (mMap.containsKey(uri)) {
			List<CookieHolder> holders = mMap.get(uri);
			return remove(holders, cookie);
		}

		return false;
	}

	private boolean remove(List<CookieHolder> holders, HttpCookie cookie) {
		List<CookieHolder> deads = new ArrayList<CookieHolder>();

		for (CookieHolder holder : holders) {
			if (holder.getCookie().equals(cookie)) {
				deads.add(holder);
			}
		}

		holders.removeAll(deads);
		return deads.size() > 0;
	}

	@Override
	public boolean removeAll() {
		boolean ret = mMap.size() > 0;
		mMap.clear();
		return ret;
	}

	private static class CookieHolder {
		private URI mURI;
		private HttpCookie mCookie;
		private long mCreated;

		public CookieHolder(URI uri, HttpCookie cookie) {
			mURI = uri;
			mCookie = cookie;
			mCreated = currentTimeSeconds();
		}

		public HttpCookie getCookie() {
			return mCookie;
		}

		public boolean isExpired() {
			return mCookie.getMaxAge() != -1 && mCreated + mCookie.getMaxAge() <= currentTimeSeconds();
		}

		private long currentTimeSeconds() {
			return System.currentTimeMillis() / 1000;
		}
	}

	private void log(String prefix, URI uri, HttpCookie cookie) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(" ");
		sb.append(uri.toString());
		sb.append(": ");
		addCookieToStringBuilder(sb, cookie);

		Log.v(TAG, sb.toString());
	}

	private void log(String prefix, URI uri, List<HttpCookie> cookies) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(" ");
		sb.append(uri.toString());
		sb.append(":\n");
		for (HttpCookie cookie : cookies) {
			addCookieToStringBuilder(sb, cookie);
			sb.append("\n");
		}

		Log.v(TAG, sb.toString());
	}

	private void addCookieToStringBuilder(StringBuilder sb, HttpCookie cookie) {
		sb.append("name:" + cookie.getName());
		sb.append(", comment:" + cookie.getComment());
		sb.append(", commentUrl:" + cookie.getCommentURL());
		sb.append(", domain:" + cookie.getDomain());
		sb.append(", path:" + cookie.getPath());
		sb.append(", value:" + cookie.getValue());
		sb.append(", version:" + cookie.getVersion());
		sb.append(", maxAge:" + cookie.getMaxAge());
	}
}
