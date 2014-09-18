package com.expedia.bookings.bitmaps;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;

import com.mobiata.android.Log;

/**
 * This is a version of BitmapDrawable that's associated with a particular URL.
 * It automatically caches and reloads itself as its Bitmap is flushed from memory.
 * 
 * When using this with an ImageView, use  loadImageView() or configureImageView(),
 * as there is some special handling needed when using ImageViews.
 */
public class UrlBitmapDrawable extends BitmapDrawable implements L2ImageCache.OnBitmapLoaded {

	private String mKey = ((Object) this).toString();

	// We allow you to define a series of URLs to try (in order, from 0 and up).
	private List<String> mUrls;
	private boolean mBlur;
	private int mIndex; // Keeps track of which URL we're going to try next

	// Used for reloading the default drawable resource id
	private WeakReference<Resources> mResources;
	private int mDefaultResId;

	private boolean mRetrieving;
	private boolean mFailed;

	private WeakReference<ImageView> mImageView;

	private L2ImageCache.OnBitmapLoaded mCallback;

	/**
	 * This class must be associated with an instance of L2ImageCache in order to reload Bitmaps in
	 * the event the Bitmap has been flushed from the cache and/or recycled. It defaults to using
	 * the general-purpose cache. You can configure which cache if you'd like, though.
	 */
	private static final L2ImageCache DEFAULT_IMAGE_CACHE_IMPL = L2ImageCache.sGeneralPurpose;
	private static L2ImageCache sL2ImageCache;

	/**
	 * Create a drawable that loads the requested URL, not blurred.
	 *
	 * @param resources resources for setting target density
	 * @param url the url of the image to load
	 */
	public UrlBitmapDrawable(Resources resources, String url) {
		this(resources, Arrays.asList(url), false);
	}

	/**
	 * Create a drawable that loads the requested URL
	 *
	 * @param resources resources for setting target density
	 * @param url the url of the image to load
	 * @param blur whether or not the image should be blurred
	 */
	public UrlBitmapDrawable(Resources resources, String url, boolean blur, L2ImageCache cacheImpl) {
		this(resources, Arrays.asList(url), blur, 0, cacheImpl);
	}

	/**
	 * Create a drawable that loads one of a list of URLs, in order
	 * from index 0...n. Not blurred.
	 * 
	 * @param resources resources for setting target density
	 * @param urls the urls to try for this image
	 */

	public UrlBitmapDrawable(Resources resources, List<String> urls) {
		this(resources, urls, false, 0, DEFAULT_IMAGE_CACHE_IMPL);
	}

	/**
	 * Create a drawable that loads one of a list of URLs, in order
	 * from index 0...n
	 *
	 * @param resources resources for setting target density
	 * @param urls the urls to try for this image
	 * @param blur whether or not the image should be blurred
	 */
	public UrlBitmapDrawable(Resources resources, List<String> urls, boolean blur) {
		this(resources, urls, blur, 0, DEFAULT_IMAGE_CACHE_IMPL);
	}

	/**
	 * Creates a drawable that loads the requested URL, and has a default image set
	 * while it's loading.
	 * 
	 * @param resources resources for setting target density
	 * @param url the url of the image to load
	 * @param defaultResId the default image when not loading
	 */
	public UrlBitmapDrawable(Resources resources, String url, int defaultResId) {
		this(resources, Arrays.asList(url), false, defaultResId, DEFAULT_IMAGE_CACHE_IMPL);
	}

	public UrlBitmapDrawable(Resources resources, List<String> urls, int defaultResId) {
		this(resources, urls, false, defaultResId, DEFAULT_IMAGE_CACHE_IMPL);
	}

	/**
	 * Create a drawable that loads one of a list of URLs, in order
	 * from index 0...n; it also has a default image set while it's loading
	 * 
	 * @param resources resources for setting target density
	 * @param urls the urls to try for this image
	 * @param defaultResId the default image when not loading
	 */
	public UrlBitmapDrawable(Resources resources, List<String> urls, boolean blur, int defaultResId,
		L2ImageCache cache) {
		super(resources, loadBitmap(cache, resources, defaultResId));

		mResources = new WeakReference<Resources>(resources);
		mUrls = urls;
		mBlur = blur;
		mIndex = 0;
		mDefaultResId = defaultResId;
		sL2ImageCache = cache;

		// Kick off an initial load for the URL
		retrieveImage(false);
	}

	/**
	 * Convenience method; creates a UrlBitmapDrawable with the assigned URL and adds it
	 * to the ImageView.
	 */
	public static UrlBitmapDrawable loadImageView(String url, ImageView imageView) {
		return loadImageView(Arrays.asList(url), imageView);
	}

	/**
	 * Convenience method; creates a UrlBitmapDrawable with the assigned URLs and adds it
	 * to the ImageView.
	 */
	public static UrlBitmapDrawable loadImageView(List<String> urls, ImageView imageView) {
		UrlBitmapDrawable drawable = new UrlBitmapDrawable(imageView.getContext().getResources(), urls);
		drawable.configureImageView(imageView);
		return drawable;
	}

	/**
	 * Convenience method; creates a UrlBitmapDrawable with the assigned URL and adds it
	 * to the ImageView.  Also includes a default loading icon.
	 */
	public static UrlBitmapDrawable loadImageView(String url, ImageView imageView, int defaultResId) {
		return loadImageView(Arrays.asList(url), imageView, defaultResId);
	}

	/**
	 * Convenience method; creates a UrlBitmapDrawable with the assigned URLs and adds it
	 * to the ImageView.  Also includes a default loading icon.
	 */
	public static UrlBitmapDrawable loadImageView(List<String> urls, ImageView imageView, int defaultResId) {
		UrlBitmapDrawable drawable = new UrlBitmapDrawable(imageView.getContext().getResources(), urls,
			false, defaultResId, DEFAULT_IMAGE_CACHE_IMPL);
		drawable.configureImageView(imageView);
		return drawable;
	}

	/**
	 * Configures an ImageView to use this UrlBitmapDrawable.
	 * 
	 * ImageViews have a bit of weird logic to them, and so it's best to let the
	 * UrlBitmapDrawable handle the situation for you.  Thus, you should either
	 * use this method or one of the static convenience methods to set a
	 * UrlBitmapDrawable with an ImageView.
	 * 
	 * @param imageView the ImageView to add this drawable to.
	 */
	public void configureImageView(ImageView imageView) {
		// If there's already a UrlBitmapDrawable retrieving for this image, disassociate it (so we don't have
		// two UrlBitmapDrawables trying to operate on the same ImageView).
		Drawable currDrawable = imageView.getDrawable();
		if (currDrawable instanceof UrlBitmapDrawable) {
			UrlBitmapDrawable oldDrawable = (UrlBitmapDrawable) currDrawable;
			oldDrawable.mCallback = null;
			oldDrawable.mImageView = null;
		}

		imageView.setImageDrawable(this);
		mImageView = new WeakReference<ImageView>(imageView);
	}

	/**
	 * Allows one to set a callback for when the image is loaded (or fails to load)
	 * 
	 * Note that while it returns a Bitmap and Url, it probably should not be used;
	 * the UrlBitmapDrawable should be handling the display itself.  If you want
	 * to manipulate the Bitmap directly, you should probably be using
	 * L2ImageCache directly instead of going through a UrlBitmapDrawable.
	 */
	public void setOnBitmapLoadedCallback(L2ImageCache.OnBitmapLoaded callback) {
		if (callback != mCallback) {
			if (callback != null) {
				if (!mFailed && !mRetrieving && getBitmap() != null) {
					callback.onBitmapLoaded(getUrl(), getBitmap());
				}
				else if (mFailed) {
					callback.onBitmapLoadFailed(getUrl());
				}
			}

			mCallback = callback;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Meat of image loading setup

	protected void retrieveImage(boolean forceRetrieve) {
		if (!mRetrieving || forceRetrieve) {
			String url = getUrl();
			if (!TextUtils.isEmpty(url)) {
				mRetrieving = true;
				sL2ImageCache.loadImage(mKey, getUrl(), mBlur, UrlBitmapDrawable.this);
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		// If URL not set, or we could not retrieve the image for this drawable, don't draw (or draw default)
		if (mFailed) {
			if (mDefaultResId != 0) {
				super.draw(canvas);
			}
			return;
		}

		// If URL not loaded (either being null or recycled), load it now
		if (!hasLoadedBitmap()) {
			if (mDefaultResId != 0) {
				Resources res = mResources.get();
				if (res != null) {
					setBitmap(loadBitmap(sL2ImageCache, res, mDefaultResId));
				}
			}
			retrieveImage(false);
		}
		else {
			super.draw(canvas);
		}
	}

	/**
	 * Returns the URL to try to load.  Can be overridden to try loading different URLs.
	 */
	protected String getUrl() {
		if (mIndex < mUrls.size()) {
			return mUrls.get(mIndex);
		}

		return null;
	}

	public boolean hasLoadedBitmap() {
		Bitmap bitmap = getBitmap();
		return bitmap != null && !bitmap.isRecycled();
	}

	//////////////////////////////////////////////////////////////////////////
	// L2ImageCache.OnBitmapLoaded

	@Override
	public void onBitmapLoaded(String url, Bitmap bitmap) {
		setBitmap(bitmap);

		// This is a hack to get the ImageView to re-measure the
		// width/height of this Drawable without having to completely
		// re-implement ImageView.  Without a re-measure this drawable
		// can end up skewed in the ImageView.
		if (mImageView != null) {
			final ImageView imageView = mImageView.get();
			if (imageView != null) {
				imageView.setImageDrawable(null);
				imageView.setImageDrawable(UrlBitmapDrawable.this);
			}
		}

		mRetrieving = false;

		if (mCallback != null) {
			mCallback.onBitmapLoaded(url, bitmap);
		}

		if (sReportingEnabled) {
			report();
		}
	}

	@Override
	public void onBitmapLoadFailed(String url) {
		if (mIndex + 1 < mUrls.size()) {
			mIndex++;
			retrieveImage(true);
		}
		else {
			mRetrieving = false;
			mFailed = true;

			if (mCallback != null) {
				mCallback.onBitmapLoadFailed(url);
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Default Bitmaps
	//
	// We cache them in L2ImageCache because often times the same
	// default bitmap is used over and over again (and there's no reason to
	// keep reloading it).

	private static final Object mLock = new Object();

	// Conditionally loads a bitmap if passed a real resId
	private static Bitmap loadBitmap(L2ImageCache cache, Resources res, int resId) {
		if (resId == 0) {
			return null;
		}

		// Synchronized so that we don't try to load the same default image
		// multiple times at once.
		synchronized (mLock) {
			return cache.getImage(res, resId, false);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Excess usage reporting
	//
	// This is not an exact science; it is mostly used to find big offenders.
	//
	// It will tend to flood your logs when used, so only enable it in debug.

	private static boolean sReportingEnabled = false;

	public static void enableReporting() {
		sReportingEnabled = true;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		if (sReportingEnabled) {
			report();
		}
	}

	@TargetApi(12)
	private void report() {
		String url = getUrl();
		if (hasLoadedBitmap() && !TextUtils.isEmpty(url)) {
			Bitmap bitmap = getBitmap();

			// Calculate how many bytes this *would* take up if it were exactly the right size
			Rect bounds = getBounds();
			long pixels = bounds.width() * bounds.height();
			if (pixels == 0) {
				// Can't measure, go back
				return;
			}
			long neededBytes = 0;
			switch (bitmap.getConfig()) {
			case ALPHA_8:
				neededBytes = pixels;
				break;
			case RGB_565:
			case ARGB_4444:
				neededBytes = pixels * 2;
				break;
			case ARGB_8888:
				neededBytes = pixels * 4;
				break;
			}

			// Calculate how many bytes are being used
			long usedBytes = 0;
			if (Build.VERSION.SDK_INT >= 12) {
				usedBytes = bitmap.getByteCount();
			}
			else {
				// Approximation of byte count pre-getByteCount()
				usedBytes = bitmap.getRowBytes() * bitmap.getHeight();
			}

			// difference in usage
			long diff = usedBytes - neededBytes;
			if (diff > 0) {
				NumberFormat nf = NumberFormat.getInstance();
				Log.i("BitmapWaste", "Over=" + nf.format(diff / 1024) + "KB - " + url);
			}
		}
	}
}
