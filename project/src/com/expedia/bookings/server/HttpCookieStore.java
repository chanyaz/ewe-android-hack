package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

public class HttpCookieStore implements CookieStore {
	private static final String TAG = "EBCookie";
	private List<HttpCookie> mCookies = new ArrayList<HttpCookie>();
	private HashMap<HttpCookie, Long> mCreatedTimes = new HashMap<HttpCookie, Long>();
	private boolean mShouldLog = false;

	public void updateSettings(Context context) {
		mShouldLog = !AndroidUtils.isRelease(context) && SettingUtils.get(context, context.getString(R.string.preference_cookie_logging), false);
	}

	@Override
	public void add(URI uri, HttpCookie cookie) {
		if (mShouldLog) {
			log("add:", uri, cookie);
		}

		for (int i = 0; i < mCookies.size(); i++) {
			HttpCookie stored = mCookies.get(i);
			if (sameCookie(stored, cookie)) {
				mCookies.set(i, cookie);
				updateCookieCreatedTime(stored, cookie);
				return;
			}
		}

		// Didn't find it in the list, just add to the end
		mCookies.add(cookie);
		mCreatedTimes.put(cookie, currentTimeSeconds());
	}

	@Override
	public List<HttpCookie> get(URI uri) {
		List<HttpCookie> results = getCookies();
		if (mShouldLog) {
			log("get:", uri, results);
		}
		return results;
	}

	@Override
	public List<HttpCookie> getCookies() {
		List<HttpCookie> results = new ArrayList<HttpCookie>();
		List<HttpCookie> deads = new ArrayList<HttpCookie>();
		for (HttpCookie cookie : mCookies) {
			if (!isExpired(cookie)) {
				results.add(cookie);
			}
			else {
				deads.add(cookie);
			}
		}
		cleanup(deads);
		return results;
	}

	@Override
	public List<URI> getURIs() {
		return new ArrayList<URI>();
	}

	@Override
	public boolean remove(URI uri, HttpCookie cookie) {
		List<HttpCookie> deads = new ArrayList<HttpCookie>();
		for (HttpCookie stored : mCookies) {
			if (sameCookie(stored, cookie)) {
				deads.add(stored);
			}
		}
		cleanup(deads);

		return deads.size() > 0;
	}

	@Override
	public boolean removeAll() {
		boolean ret = mCookies.size() > 0;
		mCookies.clear();
		mCreatedTimes.clear();
		return ret;
	}

	private void cleanup(List<HttpCookie> deads) {
		if (deads != null && deads.size() > 0) {
			for (HttpCookie dead : deads) {
				mCreatedTimes.remove(dead);
			}
			mCookies.removeAll(deads);
		}
	}

	private void updateCookieCreatedTime(HttpCookie old, HttpCookie fresh) {
		long time = mCreatedTimes.remove(old);
		mCreatedTimes.put(fresh, time);
	}

	private boolean isExpired(HttpCookie cookie) {
		return cookie.getMaxAge() != -1 && mCreatedTimes.get(cookie) + cookie.getMaxAge() <= currentTimeSeconds();
	}

	private long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	private boolean sameCookie(HttpCookie a, HttpCookie b) {
		return TextUtils.equals(a.getDomain(), b.getDomain()) && TextUtils.equals(a.getName(), b.getName());
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
