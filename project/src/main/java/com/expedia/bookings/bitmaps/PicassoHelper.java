package com.expedia.bookings.bitmaps;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.utils.Constants;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.pollexor.Thumbor;
import com.squareup.pollexor.ThumborUrlBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

	private boolean mPalette;
	private boolean mFade;
	private boolean mFit;
	private boolean mCenterCrop;
	private boolean mCacheEnabled;

	private PicassoTarget mTarget;
	private Callback mCallback;

	private String mTag;

	private Picasso mPicasso;
	static final String THUMBOR_ENDPOINT = "https://thumbnails.expedia.com/g";
	private static Thumbor thumbor = Thumbor.create(THUMBOR_ENDPOINT);
	private static long imageContentLength;
	private static int numberOfImagesDownloaded;
	private static boolean recordImageLength;

	public static void init(final Context context, OkHttpClient client) {
		if (!ExpediaBookingApp.isAutomation()) {
			client = client.newBuilder().addInterceptor(new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request request = chain.request();
					Response response = chain.proceed(request);
					boolean bucketed = AbacusFeatureConfigManager
						.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppHotelMediaHubSmartImagingService);
					boolean shouldTrackSize = request.url().toString().contains(
						bucketed ? Constants.THUMBOR_URL_PARAM_FOR_TRACKING_BUCKETED
							: Constants.THUMBOR_URL_PARAM_FOR_TRACKING_CONTROL);
					if (shouldTrackSize && response.cacheResponse() == null) {
						addImageContentLength(response.body().contentLength());
					}
					return response;
				}
			}).build();
			
			OkHttp3Downloader okHttpDownloader = new OkHttp3Downloader(client);
			boolean isLoggingEnabled = SettingUtils
				.get(context, context.getString(R.string.preference_enable_picasso_logging), false);

			Picasso picasso = new Picasso.Builder(context)
				.downloader(okHttpDownloader)
				.build();
			picasso.setLoggingEnabled(isLoggingEnabled);
			Picasso.setSingletonInstance(picasso);

		}
	}

	private static void addImageContentLength(long contentLength) {
		if (recordImageLength) {
			imageContentLength += contentLength;
			numberOfImagesDownloaded++;
		}
	}

	protected PicassoHelper(Context context) {
		mContext = context;
		mPicasso = Picasso.with(context);
	}

	public static String generateSizedSmartCroppedUrl(String originalUrl, int width, int height) {
		ThumborUrlBuilder thumborBuilder = thumbor.buildImage(originalUrl.replace("https://", "").trim());
		thumborBuilder.resize(width, height);
		thumborBuilder.smart();
		thumborBuilder.filter(ThumborUrlBuilder.format(ThumborUrlBuilder.ImageFormat.WEBP));
		return thumborBuilder.toUrl().replace("/unsafe", "");
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

	public static Pair<Long, Integer> stopRecordingImageLength() {
		recordImageLength = false;
		Pair<Long, Integer> returnPair = new Pair(imageContentLength, numberOfImagesDownloaded);
		// Reset
		imageContentLength = 0;
		numberOfImagesDownloaded = 0;
		return returnPair;
	}

	public static void startRecordingImageLength() {
		recordImageLength = true;
	}

	private void loadImage(RequestCreator requestCreator) {
		if (mDefaultResId != 0) {
			requestCreator = requestCreator.placeholder(mDefaultResId);
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
			requestCreator = requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE);
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
			if (!FailedUrlCache.getInstance().contains(getUrl())) {
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
