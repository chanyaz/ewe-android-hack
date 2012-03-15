package com.expedia.bookings.server;

import java.io.File;
import java.util.Date;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;

// TODO: Find some way to keep this easily in memory so we're not saving/loading after each request.
public class PersistantCookieStore extends BasicCookieStore {

	private boolean mDirty = true;

	@Override
	public synchronized void addCookie(Cookie cookie) {
		super.addCookie(cookie);

		mDirty = true;
	}

	@Override
	public synchronized void addCookies(Cookie[] cookies) {
		super.addCookies(cookies);

		mDirty = true;
	}

	@Override
	public synchronized void clear() {
		super.clear();

		mDirty = true;
	}

	@Override
	public synchronized boolean clearExpired(Date date) {
		boolean cleared = super.clearExpired(date);

		mDirty |= cleared;

		return cleared;
	}

	public boolean isDirty() {
		return mDirty;
	}

	public void load(Context context, String fileName) {
		File file = context.getFileStreamPath(fileName);
		if (!file.exists()) {
			Log.i("Tried to load cookie file \"" + fileName + "\" that does not exist");
			return;
		}

		try {
			String data = IoUtils.readStringFromFile(fileName, context);

			if (data == null) {
				Log.w("Could not load cookie file, thread interrupted during read.");
				return;
			}

			JSONArray arr = new JSONArray(data);
			int len = arr.length();
			for (int a = 0; a < len; a++) {
				JSONObject cookieObj = arr.getJSONObject(a);

				BasicClientCookie2 cookie = new BasicClientCookie2(cookieObj.optString("name", null),
						cookieObj.optString("value", null));
				cookie.setComment(cookieObj.optString("comment", null));
				cookie.setCommentURL(cookieObj.optString("commentUrl", null));
				cookie.setDomain(cookieObj.optString("domain", null));
				cookie.setPath(cookieObj.optString("path", null));
				cookie.setVersion(cookieObj.optInt("version", 0));

				if (cookieObj.has("expiryDate")) {
					cookie.setExpiryDate(new Date(cookieObj.getLong("expiryDate")));
				}

				if (cookieObj.has("ports")) {
					JSONArray portsArr = cookieObj.getJSONArray("ports");
					int portsLen = portsArr.length();
					int[] ports = new int[portsLen];
					for (int b = 0; b < portsLen; b++) {
						ports[b] = portsArr.getInt(b);
					}
					cookie.setPorts(ports);
				}

				addCookie(cookie);
			}

			Log.d("Loaded " + len + " cookies");
		}
		catch (Exception e) {
			Log.e("Could not load cookies.", e);
		}
	}

	public void save(Context context, String fileName) {
		try {
			JSONArray arr = new JSONArray();
			for (Cookie cookie : getCookies()) {
				JSONObject obj = new JSONObject();
				obj.putOpt("comment", cookie.getComment());
				obj.putOpt("commentUrl", cookie.getCommentURL());
				obj.putOpt("domain", cookie.getDomain());
				obj.putOpt("name", cookie.getName());
				obj.putOpt("path", cookie.getPath());
				obj.putOpt("value", cookie.getValue());
				obj.putOpt("version", cookie.getVersion());

				Date expiryDate = cookie.getExpiryDate();
				if (expiryDate != null) {
					obj.putOpt("expiryDate", expiryDate.getTime());
				}

				int[] ports = cookie.getPorts();
				if (ports != null) {
					JSONArray portsArr = new JSONArray();
					for (int port : ports) {
						portsArr.put(port);
					}
					obj.putOpt("ports", ports);
				}

				arr.put(obj);
			}

			IoUtils.writeStringToFile(fileName, arr.toString(), context);

			Log.d("Saved " + getCookies().size() + " cookies");
		}
		catch (Exception e) {
			Log.e("Could not save cookies.", e);
		}
	}
}
