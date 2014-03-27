package com.expedia.bookings.data;

import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class ExpediaImageResponse extends Response implements JSONable {

	private static final String JSON_KEY_IMAGEURL = "imageUrl";
	private static final String JSON_KEY_CACHEKEY = "cacheKey";

	private String mCacheKey;
	private String mImageUrl;

	public String getCacheKey() {
		return mCacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.mCacheKey = cacheKey;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.mImageUrl = imageUrl;
	}

	@Override
	public JSONObject toJson() {
		JSONObject out = new JSONObject();
		try {
			out.putOpt(JSON_KEY_CACHEKEY, mCacheKey);
			out.putOpt(JSON_KEY_IMAGEURL, mImageUrl);
		}
		catch (Exception ex) {
			Log.e("Exception in ExpediaImageResponse.toJson", ex);
		}
		return out;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mImageUrl = obj.optString(JSON_KEY_IMAGEURL);
		mCacheKey = obj.optString(JSON_KEY_CACHEKEY);
		return true;
	}
}
