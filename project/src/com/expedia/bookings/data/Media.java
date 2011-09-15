package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Media implements JSONable {
	public static final int TYPE_STILL_IMAGE = 1;
	public static final int TYPE_VIRTUAL_TOUR = 2;
	public static final int TYPE_VIDEO_TOUR = 3;

	private int mMediaType;
	private String mUrl;
	private int mHeight;
	private int mWidth;

	public Media() {
		// Default constructor
	}

	public Media(int mediaType, String url) {
		mMediaType = mediaType;
		mUrl = url;
	}

	public int getMediaType() {
		return mMediaType;
	}

	public void setMediaType(int mediaType) {
		this.mMediaType = mediaType;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int height) {
		this.mHeight = height;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int width) {
		this.mWidth = width;
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("url", mUrl);
			obj.putOpt("height", mHeight);
			obj.putOpt("width", mWidth);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Media object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mUrl = obj.optString("url", null);
		mHeight = obj.optInt("height");
		mWidth = obj.optInt("width");
		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}
}
