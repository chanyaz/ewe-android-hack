package com.expedia.bookings.bitmaps;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
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

	private PicassoTarget mTarget;
	private Callback mCallback;

	private String mTag;

	private boolean mRetrieving;
	private boolean mHasLoadedPlaceholder;

	private static Picasso mPicasso;

	public static void init(Context context) {
		mPicasso = Picasso.with(context);
	}

	private PicassoHelper(Context context) {
		mContext = context;
	}

	public void load(List<String> urls) {
		mUrls = urls;
		retrieveImage(false);
	}

	public void load(String url) {
		mUrl = url;
		retrieveImage(false);
	}

	public void load(int resId) {
		mResId = resId;
		retrieveImage(false);
	}

	private void loadImage(RequestCreator requestCreator) {
		if (mDefaultResId != 0) {
			requestCreator = requestCreator.placeholder(mDefaultResId);
			mHasLoadedPlaceholder = true;
		}

		if (mErrorResId != 0) {
			requestCreator = requestCreator.error(mErrorResId);
		}

		if (mBlur) {
			requestCreator = requestCreator.transform(new BlurTransform(mContext));
		}

		if (mPalette) {
			requestCreator = requestCreator.transform(PaletteTransformation.instance());
		}

		if (mTag != null) {
			requestCreator = requestCreator.tag(mTag);
		}

		if (mTarget != null) {
			//Download bitmap and pass to a callback
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

	protected void retrieveImage(boolean forceRetrieve) {
		if (!mRetrieving || forceRetrieve) {

			if (mResId != 0) {
				loadImage(mResId);
				return;
			}
			String url = getUrl();

			if (FailedUrlCache.getInstance().contains(url)) {
				// If there is a placeholder and it hasn't been loaded
				// load it anyways to set the placeholder
				if (mDefaultResId != 0 && !mHasLoadedPlaceholder) {
					loadImage(url);
					return;
				}
				if (mTarget == null) {
					onError();
					return;
				}
			}

			if (!TextUtils.isEmpty(url)) {
				mRetrieving = true;
				loadImage(url);
				return;
			}

		}
	}

	/**
	 * Returns the URL to try to load.
	 */
	protected String getUrl() {
		if (mUrls == null) {
			return mUrl;
		}
		if (mIndex < mUrls.size()) {
			return mUrls.get(mIndex);
		}

		return null;
	}

	private void retry() {
		if (mUrls == null) {
			return;
		}

		if (mIndex + 1 < mUrls.size()) {
			mIndex++;
			retrieveImage(true);
		}
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

		if (mCallback != null) {
			mCallback.onError();
		}

		retry();
	}

	/**
	 * Target callbacks*
	 */
	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
	}

	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		retry();
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

		private String mTag;

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

		public PicassoHelper build() {
			PicassoHelper picassoHelper = new PicassoHelper(mContext);
			picassoHelper.setPlaceholder(mDefaultResId);
			picassoHelper.setError(mErrorResId);
			picassoHelper.setBlurTransformation(mBlur);
			picassoHelper.setPaletteTransformation(mPalette);
			if (mTarget != null) {
				mTarget.setCallBack(picassoHelper);
				picassoHelper.setTarget(mTarget);
			}
			picassoHelper.setCallback(mCallback);
			picassoHelper.setImageView(mView);
			picassoHelper.setFade(mFade);
			picassoHelper.setTag(mTag);
			return picassoHelper;
		}
	}
}
