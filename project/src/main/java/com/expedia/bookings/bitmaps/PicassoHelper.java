package com.expedia.bookings.bitmaps;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

public class PicassoHelper implements Target, Callback {
	// We allow you to define a series of URLs to try (in order, from 0 and up).
	private List<String> mUrls;
	private String mUrl;

	// Keeps track of which URL we're going to try next
	private int mIndex;

	private Context mContext;
	private ImageView mView;

	private int mResId;
	private int mDefaultResId;
	private int mErrorResId;

	private boolean mBlur;
	private boolean mPalette;
	private boolean mFade;
	private boolean mFit;
	private boolean mCenterCrop;
	private boolean mCacheEnabled;

	private PicassoTarget mTarget;
	private Callback mCallback;

	private String mTag;

	private static Picasso mPicasso;

	public static void init(Context context, OkHttpClient client) {
		OkHttpDownloader okHttpDownloader = new OkHttpDownloader(client);

		mPicasso = new Picasso.Builder(context)
			.downloader(okHttpDownloader)
			.build();

		boolean isLoggingEnabled = SettingUtils.get(context, context.getString(R.string.preference_enable_picasso_logging), false);
		mPicasso.setLoggingEnabled(isLoggingEnabled);
	}

	private PicassoHelper(Context context) {
		mContext = context;
	}

	public void load(List<String> urls) {
		mUrls = urls;
		retrieveImage();
	}

	public void load(String url) {
		mUrl = url;
		retrieveImage();
	}

	public void load(int resId) {
		mResId = resId;
		retrieveImage();
	}

	private void loadImage(RequestCreator requestCreator) {
		if (mDefaultResId != 0) {
			requestCreator = requestCreator.placeholder(mDefaultResId);
		}

		if (mBlur) {
			requestCreator = requestCreator.transform(new BlurTransform(mContext));
		}

		if (mPalette) {
			requestCreator = requestCreator.transform(PaletteTransformation.instance());
		}

		if (mFit) {
			requestCreator = requestCreator.fit();
		}

		if (mCenterCrop) {
			requestCreator = requestCreator.centerCrop();
		}

		if (mTag != null) {
			requestCreator = requestCreator.tag(mTag);
		}

		if (!mCacheEnabled) {
			requestCreator = requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE);
		}

		if (mTarget != null) {
			requestCreator.into(mTarget);
		}
		else if (mView != null) {
			//Download bitmap and inject into an ImageView
			if (!mFade) {
				requestCreator = requestCreator.noFade();
			}
			requestCreator.into(mView, this);
		}
		else {
			//default just fetch the image
			requestCreator.into(this);
		}
	}

	private void loadImage(int resourceId) {
		RequestCreator requestCreator;
		requestCreator = mPicasso.load(resourceId);
		loadImage(requestCreator);
	}

	private void loadImage(String url) {
		RequestCreator requestCreator;
		requestCreator = mPicasso.load(url);
		loadImage(requestCreator);
	}

	public void getDebugInfo() {
		mPicasso.getSnapshot().dump();
	}

	protected void retrieveImage() {
		if (mResId != 0) {
			loadImage(mResId);
		}
		else {
			String url = getUrl();
			if (!TextUtils.isEmpty(url)) {
				loadImage(url);
			}
		}
	}

	/**
	 * Returns the URL to try to load.
	 */
	protected String getUrl() {
		if (mUrls != null && mIndex < mUrls.size()) {
			return mUrls.get(mIndex);
		}

		return mUrl;
	}

	private boolean retry() {
		while (mUrls != null && mIndex + 1 < mUrls.size()) {
			mIndex++;
			if (FailedUrlCache.getInstance().contains(getUrl())) {
				continue;
			}
			else {
				return true;
			}
		}

		return false;
	}

	/**
	 * Callbacks*
	 */
	@Override
	public void onSuccess() {
		if (mCallback != null) {
			mCallback.onSuccess();
		}
	}

	@Override
	public void onError() {
		FailedUrlCache.getInstance().add(getUrl());
		boolean didRetry = retry();
		if (!didRetry) {
			if (mErrorResId != 0) {
				if (mTarget != null) {
					mTarget.mIsFallbackImage = true;
				}
				load(mErrorResId);
			}
		}
		else {
			retrieveImage();
		}
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		FailedUrlCache.getInstance().add(getUrl());
		boolean didRetry = retry();
		if (!didRetry) {
			if (mErrorResId != 0) {
				load(mErrorResId);
			}
		}
		else {
			retrieveImage();
		}
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {

	}

	public void pause(String tag) {
		mPicasso.pauseTag(tag);
	}

	public void resume(String tag) {
		mPicasso.resumeTag(tag);
	}

	private void setPlaceholder(int defaultResId) {
		mDefaultResId = defaultResId;
	}

	private void setError(int errorResId) {
		mErrorResId = errorResId;
	}

	private void setBlurTransformation(boolean value) {
		mBlur = value;
	}

	private void setFit(boolean fit) {
		mFit = fit;
	}

	private void setCacheEnabled(boolean enabled) {
		mCacheEnabled = enabled;
	}

	private void setCenterCrop(boolean centerCrop) {
		mCenterCrop = centerCrop;
	}

	private void setPaletteTransformation(boolean value) {
		mPalette = value;
	}

	private void setFade(boolean value) {
		mFade = value;
	}

	private void setTarget(PicassoTarget target) {
		mTarget = target;
	}

	private void setCallback(Callback callback) {
		mCallback = callback;
	}

	private void setImageView(ImageView view) {
		mView = view;
	}

	private void setTag(String tag) {
		mTag = tag;
	}

	public void setLoggingEnabled(boolean value) {
		mPicasso.setLoggingEnabled(value);
	}

	public boolean isLoggingEnabled() {
		return mPicasso.isLoggingEnabled();
	}

	public void setDisableFallback(boolean disableFallback) {

	}

	public static class Builder {

		// Required attributes
		private Context mContext;
		private int mDefaultResId;
		private int mErrorResId;
		private ImageView mView;

		private Callback mCallback;
		private PicassoTarget mTarget;

		private boolean mBlur;
		private boolean mPalette;
		private boolean mFade;
		private boolean mFit;
		private boolean mCenterCrop;
		private boolean mCacheEnabled;

		private String mTag;
		private boolean mDisableFallback = false;

		public Builder(Context context) {
			this.mContext = context;
		}

		public Builder(ImageView view) {
			this(view.getContext());
			this.mView = view;
		}

		public Builder setPlaceholder(int defaultResId) {
			mDefaultResId = defaultResId;
			return this;
		}

		public Builder setError(int errorResId) {
			mErrorResId = errorResId;
			return this;
		}

		public Builder fade() {
			mFade = true;
			return this;
		}

		public Builder fit() {
			mFit = true;
			return this;
		}

		public Builder centerCrop() {
			mCenterCrop = true;
			return this;
		}

		public Builder applyBlurTransformation(boolean value) {
			mBlur = value;
			return this;
		}

		public Builder applyPaletteTransformation(PaletteCallback callback) {
			if (callback != null) {
				mPalette = true;
				mCallback = callback;
			}
			return this;
		}

		public Builder setTarget(PicassoTarget target) {
			mTarget = target;
			return this;
		}

		public Builder setCallback(Callback callback) {
			mCallback = callback;
			return this;
		}

		public Builder setTag(String tag) {
			mTag = tag;
			return this;
		}

		public Builder setCacheEnabled(boolean enabled) {
			mCacheEnabled = enabled;
			return this;
		}

		public PicassoHelper build() {
			PicassoHelper picassoHelper = new PicassoHelper(mContext);
			picassoHelper.setPlaceholder(mDefaultResId);
			picassoHelper.setError(mErrorResId);
			picassoHelper.setBlurTransformation(mBlur);
			picassoHelper.setFit(mFit);
			picassoHelper.setCenterCrop(mCenterCrop);
			picassoHelper.setPaletteTransformation(mPalette);
			if (mTarget != null) {
				mTarget.mIsFallbackImage = false;
				mTarget.setCallBack(picassoHelper);
				picassoHelper.setTarget(mTarget);
			}
			picassoHelper.setCallback(mCallback);
			picassoHelper.setImageView(mView);
			picassoHelper.setFade(mFade);
			picassoHelper.setTag(mTag);
			picassoHelper.setCacheEnabled(mCacheEnabled);
			picassoHelper.setDisableFallback(mDisableFallback);
			return picassoHelper;
		}

		public Builder disableFallback() {
			mDisableFallback = true;
			return this;
		}
	}
}
