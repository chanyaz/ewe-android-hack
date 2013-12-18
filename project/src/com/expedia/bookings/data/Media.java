package com.expedia.bookings.data;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.ImageView;

import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache.OnImageLoaded;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;
import com.mobiata.android.json.JSONable;

public class Media implements JSONable {

	/*
	 * The suffixes for different image sizes is documented here:
	 * https://team.mobiata.com/wiki/EAN_Servers#Expedia_Hotels_Image_Derivatives
	 */
	public static final String IMAGE_LARGE_SUFFIX = "y.jpg"; // 500x500 sized image
	public static final String IMAGE_BIG_SUFFIX = "b.jpg"; // 350x350 sized image

	private static final int SUFFIX_LENGTH = 5;

	private String mBaseUrl;
	private String mOriginalSuffix;

	public Media() {
		// Default constructor
	}

	public Media(String url) {
		setUrl(url);
	}

	public String getUrl() {
		return mBaseUrl + mOriginalSuffix;
	}

	public String getUrl(String suffix) {
		return mBaseUrl + suffix;
	}

	public void setUrl(String url) {
		if (url == null) {
			throw new InvalidParameterException("url cannot be null");
		}
		int split = url.length() - SUFFIX_LENGTH;
		mBaseUrl = url.substring(0, split);
		mOriginalSuffix = url.substring(split);
	}

	/**
	 * Loads a high-res image automatically into an ImageView.
	 *
	 * If you need more fine-grained control
	 *
	 * @param imageView
	 * @param callback
	 */
	public void loadHighResImage(ImageView imageView, OnImageLoaded callback) {
		UrlBitmapDrawable drawable = UrlBitmapDrawable.loadImageView(getHighResUrls(), imageView);
		drawable.setOnImageLoadedCallback(callback);
	}

	public void preloadHighResImage(Context context, OnImageLoaded callback) {
		// It may make sense to someday rewrite this not to abuse UrlBitmapDrawable (e.g. go straight to the cache)
		UrlBitmapDrawable drawable = new UrlBitmapDrawable(context.getResources(), getHighResUrls());
		drawable.setOnImageLoadedCallback(callback);
	}

	public List<String> getHighResUrls() {
		return Arrays.asList(getUrl(IMAGE_LARGE_SUFFIX), getUrl(IMAGE_BIG_SUFFIX), getUrl());
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("url", mBaseUrl);
			obj.putOpt("suffix", mOriginalSuffix);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Media object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mBaseUrl = obj.optString("url");
		mOriginalSuffix = obj.optString("suffix");
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Media)) {
			return false;
		}

		// Equals compares the base URL, not the full URL (which could vary but ultimately means the same image)

		Media other = (Media) o;
		return mBaseUrl.equals(other.mBaseUrl);
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
