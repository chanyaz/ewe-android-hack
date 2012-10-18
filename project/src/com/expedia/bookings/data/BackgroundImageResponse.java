package com.expedia.bookings.data;

import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class BackgroundImageResponse extends Response implements JSONable {

	private static final String JSON_KEY_IMAGEURL = "imageUrl";
	private static final String JSON_KEY_BLURREDIMAGEURL = "blurredImageUrl";
	private static final String JSON_KEY_CACHEKEY = "cacheKey";

	private String mCacheKey;
	private String mImageUrl;
	private String mBlurredImageUrl;

	public String getmCacheKey() {
		return mCacheKey;
	}

	public void setmCacheKey(String mCacheKey) {
		this.mCacheKey = mCacheKey;
	}

	public String getmImageUrl() {
		return mImageUrl;
	}

	public void setmImageUrl(String mImageUrl) {
		this.mImageUrl = mImageUrl;
	}

	public String getmBlurredImageUrl() {
		return mBlurredImageUrl;
	}

	public void setmBlurredImageUrl(String mBlurredImageUrl) {
		this.mBlurredImageUrl = mBlurredImageUrl;
	}

	public BackgroundImageResponse() {

	}

	@Override
	public JSONObject toJson() {
		JSONObject out = new JSONObject();
		try {
			out.putOpt(JSON_KEY_CACHEKEY, mCacheKey);
			out.putOpt(JSON_KEY_IMAGEURL, mImageUrl);
			out.putOpt(JSON_KEY_BLURREDIMAGEURL, mBlurredImageUrl);
		}
		catch (Exception ex) {
			Log.e("Exception in toJson", ex);
		}
		return out;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mImageUrl = obj.optString(JSON_KEY_IMAGEURL);
		mBlurredImageUrl = obj.optString(JSON_KEY_BLURREDIMAGEURL);
		mCacheKey = obj.optString(JSON_KEY_CACHEKEY);
		return true;
	}
}
