package com.expedia.bookings.data;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class Media implements JSONable {
	public static final int TYPE_STILL_IMAGE = 1;
	public static final int TYPE_VIRTUAL_TOUR = 2;
	public static final int TYPE_VIDEO_TOUR = 3;

	/*
	 * The suffixes for different image sizes is documented here:
	 * https://team.mobiata.com/wiki/EAN_Servers#Expedia_Hotels_Image_Derivatives
	 */
	public static final String IMAGE_XLARGE_SUFFIX = "z.jpg"; //1000x1000 sized image
	public static final String IMAGE_LARGE_SUFFIX = "y.jpg"; // 500x500 sized image
	public static final String IMAGE_BIG_SUFFIX = "b.jpg"; // 350x350 sized image

	private HashMap<String, String> mImageUrls;

	private int mMediaType;
	private String mUrl;
	private int mHeight;
	private int mWidth;

	public Media() {
		// Default constructor
	}

	public Media(int mediaType, String url) {
		mMediaType = mediaType;
		setUrl(url);
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

	public String getHighResUrl() {
		return mImageUrls.get(IMAGE_XLARGE_SUFFIX);
	}

	public void setUrl(String url) {
		this.mUrl = url;
		constructUrlsForDifferentImageSizes();
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

	/**
	 * This method provides a callback that implements the 
	 * fallback mechanism for images, trying to get the
	 * highest resolution image first, and then working
	 * its way down the list to see what sized image is available
	 */
	public OnImageLoaded getImageLoadedCallback(final ImageView imageView, final OnImageLoaded additionCallback) {
		return new OnImageLoaded() {

			@Override
			public void onImageLoaded(String url, Bitmap bitmap) {
				if (bitmap != null) {
					Log.d("** Loading image with url = " + url);
					imageView.setImageBitmap(bitmap);

					if (additionCallback != null) {
						additionCallback.onImageLoaded(url, bitmap);
					}
				}
			}

			@Override
			public void onImageLoadFailed(String url) {
				if (url.equals(mImageUrls.get(IMAGE_XLARGE_SUFFIX))) {
					Log.d("** Falling back from " + IMAGE_XLARGE_SUFFIX + " to " + IMAGE_LARGE_SUFFIX);
					ImageCache.loadImage(mImageUrls.get(IMAGE_LARGE_SUFFIX),
							getImageLoadedCallback(imageView, additionCallback));
				}
				else if (url.equals(mImageUrls.get(IMAGE_LARGE_SUFFIX))) {
					Log.d("** Falling back from " + IMAGE_LARGE_SUFFIX + " to " + IMAGE_BIG_SUFFIX);
					ImageCache.loadImage(mImageUrls.get(IMAGE_BIG_SUFFIX),
							getImageLoadedCallback(imageView, additionCallback));
				}
				else {
					Log.d("** No sizes available");
					if (additionCallback != null) {
						additionCallback.onImageLoadFailed(url);
					}
				}
			}

		};
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

	/*
	 * This method takes the last 4 characters in the 
	 * url string to replace it with the desired resolution
	 * for the image.
	 */
	private void constructUrlsForDifferentImageSizes() {
		mImageUrls = new HashMap<String, String>();
		mImageUrls.put(IMAGE_XLARGE_SUFFIX, mUrl.substring(0, mUrl.length() - 5) + IMAGE_XLARGE_SUFFIX);
		mImageUrls.put(IMAGE_LARGE_SUFFIX, mUrl.substring(0, mUrl.length() - 5) + IMAGE_LARGE_SUFFIX);
		mImageUrls.put(IMAGE_BIG_SUFFIX, mUrl.substring(0, mUrl.length() - 5) + IMAGE_BIG_SUFFIX);
	}
}
