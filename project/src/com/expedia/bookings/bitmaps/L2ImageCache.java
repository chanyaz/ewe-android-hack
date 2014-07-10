package com.expedia.bookings.bitmaps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;
import com.mobiata.android.util.AndroidUtils;

/**
 * A customizable two-level (memory and disk) bitmap cache class.
 *
 * TODO provide synchronous loading methods such that bg threads can load w/o relying on callback
 * TODO image resizing/downsampling based on View dimensions
 */
public class L2ImageCache {

	// Required for blurring
	private Context mContext;

	private String mLogTag;

	// Blur constants
	private static final int BLURRED_IMAGE_SIZE_REDUCTION_FACTOR = 4;
	private static final String BLUR_KEY_SUFFIX = "blurred";

	private float mDarkenMultiplier;
	private int mBlurRadius;

	public L2ImageCache(Context context, String logTag, EvictionPolicy evictionPolicy) {
		mContext = context;
		mLogTag = logTag;

		mMemoryCache = evictionPolicy.generateMemCache();
		mDiskCache = evictionPolicy.generateDiskCache();

		Resources res = context.getResources();

		// Compute the darken multiplier
		mDarkenMultiplier = res.getFraction(R.fraction.stack_blur_darken_multiplier, 1, 1);

		// Compute the blur radius
		mBlurRadius = res.getDimensionPixelSize(R.dimen.stack_blur_radius);
	}

	//////////////////////////////////////////////////////////////////////////
	// Static cache instances

	/**
	 * A general-purpose cache appropriate for most things. This cache may be cleared aggressively.
	 */
	public static L2ImageCache sGeneralPurpose;

	/**
	 * A cache intended to store large images (usually about full-screen) of the destination.
	 */
	public static L2ImageCache sDestination;

	public static void initAllCacheInstances(Context applicationContext) {
		initGeneralPurposeImageCache(applicationContext);
		initDestinationImageCache(applicationContext);
	}

	private static void initGeneralPurposeImageCache(Context context) {
		final String logTag = "GeneralPurposeImageCache";

		// Inspect on device memory class
		long maxMemory = Runtime.getRuntime().maxMemory();
		int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		Log.i(logTag, "Init - Device MaxMemory=" + maxMemory + " bytes (" + (maxMemory / 1048576) + "mb) MemoryClass=" + memoryClass + "mb");

		// Here's what we're aiming for, in terms of memory cache size:
		// 1. At least minCacheSize
		// 2. No greater than 1/5th the memory available
		final int minCacheSize = (1024 * 1024 * 6); // 6 MB
		int memCacheSize = (1024 * 1024 * memoryClass) / 5;
		if (memCacheSize < minCacheSize) {
			memCacheSize = minCacheSize;
		}

		final int diskCacheSize = 1024 * 1024 * 20; // 20 mb

		// Construct cache
		EvictionPolicy policy = new SizeEvictionPolicy(context, memCacheSize, diskCacheSize, logTag);
		sGeneralPurpose = new L2ImageCache(context, logTag, policy);
		sGeneralPurpose.setVerboseDebugLoggingEnabled(true);
	}

	private static void initDestinationImageCache(Context context) {
		// Cache params
		final String logTag = "DestinationImageCache";
		// NOTE: The phone flights flow requires both the regular and blurred versions of the destination
		// image in memory at once to properly show the regular to blur transition on FlightSearchResults.
		// Tablet search results requires a fade from non-blurry to blurry versions of the destination image.
		final int numMemCacheEntries = 2;
		final int diskCacheSize = 1024 * 1024 * 20; // 20 mb

		// Construct cache
		EvictionPolicy policy = new NumberEvictionPolicy(context, numMemCacheEntries, diskCacheSize, logTag);
		sDestination = new L2ImageCache(context, logTag, policy);
		sDestination.setVerboseDebugLoggingEnabled(true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Cache implementation

	private LruCache<String, Bitmap> mMemoryCache;

	private DiskLruCache mDiskCache;

	public Bitmap getImage(String url, boolean checkDisk) {
		return getImage(url, false, checkDisk);
	}

	/**
	 * Returns a blurred Bitmap from the cache.
	 * @param url - the base URL of the image (unblurred)
	 * @param checkDisk - whether or not to try to retrieve from disk
	 * @return
	 */
	public Bitmap getBlurredImage(String url, boolean checkDisk) {
		return getImage(url, true, checkDisk);
	}

	/**
	 * Returns a Bitmap for the given URL from the cache.
	 *
	 * @param url the url to retrieve
	 * @param blur whether or not to grab the blurred version of the image
	 * @param checkDisk whether or not to try to retrieve from disk.  This will cause file IO, which should
	 * 		be avoided if possible.  As such, this should only be true on non-UI threads.
	 * @return the Bitmap if cached, null if not
	 */
	public Bitmap getImage(String url, final boolean blur, final boolean checkDisk) {
		// Try to retrieve from memory cache
		String memKey = blur ? url + BLUR_KEY_SUFFIX : url;
		Bitmap bitmap = mMemoryCache.get(memKey);
		if (bitmap != null) {
			if (mVerboseDebugLoggingEnabled) {
				Log.d(mLogTag, "Mem cache hit url=" + memKey + " blur=" + blur);
			}
			return bitmap;
		}
		else {
			if (mVerboseDebugLoggingEnabled) {
				Log.d(mLogTag, "Mem cache miss url=" + memKey + " blur=" + blur);
			}
		}

		// Try to retrieve from disk cache (and load into memory cache if we get a hit).
		if (checkDisk) {
			try {
				Snapshot snapshot = mDiskCache.get(blur ? getDiskKeyForBlurred(url) : getDiskKey(url));
				if (snapshot != null) {
					if (mVerboseDebugLoggingEnabled) {
						Log.d(mLogTag, "Disk cache hit url=" + url + " blur=" + blur);
					}
					bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0));
					if (bitmap != null) {
						mMemoryCache.put(memKey, bitmap);
						return bitmap;
					}
				}
				else {
					if (mVerboseDebugLoggingEnabled) {
						Log.d(mLogTag, "Disk cache miss url=" + memKey + " blur=" + blur);
					}
				}
			}
			catch (IOException e) {
				Log.w(mLogTag, "Could not retrieve url from disk cache: " + url, e);
			}
			catch (OutOfMemoryError e) {
				Log.w(mLogTag, "Tried to decode Bitmap from disk cache but ran out of memory: " + url, e);
			}
		}

		return null;
	}

	public boolean hasImageInDiskCache(String url) {
		try {
			return mDiskCache.get(getDiskKey(url)) != null;
		}
		catch (IOException e) {
			Log.w(mLogTag, "Could check on status of url from disk cache: " + url, e);
			return false;
		}
	}

	/**
	 * Ensures the erasure of the image from both the memory and disk cache.
	 *
	 * Also cancels the download of the image if currently in progress.
	 */
	public void removeImage(String url) {
		// Cancel the download of this image if in progress
		if (mDownloadsByUrl.containsKey(url)) {
			ImageTask task = mDownloadsByUrl.get(url);
			task.cancel(true);
		}

		Bitmap bitmap = mMemoryCache.remove(url);
		if (bitmap != null) {
			bitmap.recycle();
		}

		try {
			mDiskCache.remove(getDiskKey(url));
		}
		catch (IOException e) {
			Log.w("Could not remove URL from disk cache: " + url, e);
		}
	}

	// Used in getDiskKey() for converting from bytes to a hex string
	private static final char[] HEX_DIGITS = {
		'0',
		'1',
		'2',
		'3',
		'4',
		'5',
		'6',
		'7',
		'8',
		'9',
		'a',
		'b',
		'c',
		'd',
		'e',
		'f',
	};

	/**
	 * DiskLruCache is limited to keys of the form [a-z0-9_-]{1,64}, so we need a way to
	 * convert URLs into something we can actually use as a key.
	 *
	 * @param url the url of the image
	 * @return the key for that image
	 */
	private static String getDiskKey(String url) {
		try {
			// Generate the hash bytes
			MessageDigest sha2 = MessageDigest.getInstance("SHA-256");
			sha2.update(url.getBytes("UTF-8"));
			byte[] hash = sha2.digest();

			// Convert the bytes into a hex string (optimized for this specific use-case)
			char[] hexChars = new char[64];
			for (int i = 0, j = 0; i < 32; i++) {
				hexChars[j++] = HEX_DIGITS[(0xF0 & hash[i]) >>> 4];
				hexChars[j++] = HEX_DIGITS[0x0F & hash[i]];
			}

			return new String(hexChars);
		}
		catch (Exception e) {
			// I can't imagine us actually running into an exception here
			throw new RuntimeException(e);
		}
	}

	private static String getDiskKeyForBlurred(String url) {
		return getDiskKey(url + BLUR_KEY_SUFFIX);
	}

	/**
	 * Clears the memory cache.  Sometimes you want a bit more
	 * direct control over when we need an image sitting in
	 * memory or not.
	 */
	public void clearMemoryCache() {
		mMemoryCache.evictAll();
	}

	public void clearDiskCache() {
		File[] files = getDiskCacheDir(mContext, mLogTag).listFiles();
		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Resource cached images
	//
	// Uses same cache as TwoLevelImageCache, so we don't use more memory
	// than necessary if we expect to reload the same asset multiple times.

	// For things we get cached from the APK, rather than the network
	private static final String RESOURCE_CACHE_PREFIX = "res://";

	/**
	 * Retrieves an image from Resources.  Uses the memory cache if possible,
	 * meaning that images may not have to be reloaded from Resources.
	 *
	 * This is synchronous, so if you're loading a huge image, you may want to throw
	 * this in another thread. ESPECIALLY if you are blurring.
	 */
	public Bitmap getImage(Resources res, int resId, boolean blur) {
		// Don't ever try to load invalid resources
		if (resId == 0) {
			return null;
		}

		String key = getMemCacheKeyForResDrawable(resId, blur);
		Bitmap bitmap = mMemoryCache.get(key);
		if (bitmap == null) {
			try {
				if (blur) {
					// Is the regular in memory?
					Bitmap regBitmap = mMemoryCache.get(getMemCacheKeyForResDrawable(resId, false));
					if (regBitmap == null) {
						regBitmap = BitmapFactory.decodeResource(res, resId);
					}

					bitmap = BitmapUtils.stackBlurAndDarken(regBitmap, mContext,
						BLURRED_IMAGE_SIZE_REDUCTION_FACTOR, mBlurRadius, mDarkenMultiplier);
				}
				else {
					bitmap = BitmapFactory.decodeResource(res, resId);
				}
			}
			catch (OutOfMemoryError e) {
				Log.w("Could not load native resource " + resId + ", ran out of memory.", e);
				return null;
			}

			mMemoryCache.put(key, bitmap);
		}

		return bitmap;
	}

	private String getMemCacheKeyForResDrawable(int resId, boolean blur) {
		String key = RESOURCE_CACHE_PREFIX + resId;
		if (blur) {
			key += BLUR_KEY_SUFFIX;
		}
		return key;
	}

	/**
	 * Whether or not the given drawable from resources is in the memory cache
	 * @param resId
	 * @return
	 */
	public boolean hasInMemCache(int resId, boolean blur) {
		String key = getMemCacheKeyForResDrawable(resId, blur);
		return mMemoryCache.get(key) != null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Image loading methods

	/**
	 * A list of known bad URLs (aka throw FileNotFound exceptions when we try to download them).
	 * The ImageCache will not attempt to reload images that were previously unsuccessful in this
	 * manner.
	 */
	private Set<String> mIgnore = new HashSet<String>();

	/**
	 * Keeps track of current downloads, from key --> task.
	 */
	private ConcurrentHashMap<String, ImageTask> mDownloadsByKey = new ConcurrentHashMap<String, L2ImageCache.ImageTask>();

	/**
	 * Keeps track of current downloads, from url --> task
	 */
	private ConcurrentHashMap<String, ImageTask> mDownloadsByUrl = new ConcurrentHashMap<String, L2ImageCache.ImageTask>();

	/**
	 * Callback which is called when the image is loaded.
	 */
	public interface OnBitmapLoaded {
		/**
		 * Called when a bitmap is successfully loaded.
		 *
		 * @param url the url of the Bitmap
		 * @param bitmap the Bitmap that was just loaded
		 */
		public void onBitmapLoaded(String url, Bitmap bitmap);

		/**
		 * Called when a bitmap fails to load for any reason.  A few of the possible reasons:
		 * - The URL is bad.
		 * - The URL is known bad (from a previous call), so we fail it again
		 * - Internet connectivity is down.
		 *
		 * This is NOT called when the bitmap download is explicitly cancelled.  It is assumed in
		 * this case that the caller is handling any repercussions of cancelling the download.
		 *
		 * @param url the url that failed to load
		 */
		public void onBitmapLoadFailed(String url);
	}

	public static class OnBitmapLoadedAdapter implements OnBitmapLoaded {
		@Override
		public void onBitmapLoaded(String url, Bitmap bitmap) {
			// ignore
		}

		@Override
		public void onBitmapLoadFailed(String url) {
			// ignore
		}
	}

	/**
	 * Loads an image, then uses the callback to do something with that image.
	 * @param url the url of the image
	 * @param callback the method to call after loading the image
	 * @return true if image was already in cache, and callback was immediately called
	 */
	public boolean loadImage(String url, OnBitmapLoaded callback) {
		return loadImage(url, url, false, callback);
	}

	public boolean loadImage(String url, boolean blur, OnBitmapLoaded callback) {
		return loadImage(url, url, blur, callback);
	}

	/**
	 * Convenience class which takes the common callback case - loading an image
	 * into an ImageView - and wraps it.
	 * @param url the url to load into the ImageView
	 * @param imageView the ImageView to load the results into
	 * @return true if image was already in cache, and ImageView set instantly
	 */
	public boolean loadImage(String url, final ImageView imageView) {
		String key = imageView.toString();
		Log.v(mLogTag, "Loading ImageView " + key + " with " + url);

		// Begin a load on the ImageView
		OnBitmapLoaded callback = new OnBitmapLoaded() {
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				BitmapDrawable drawable = new BitmapDrawable(imageView.getContext().getResources(), bitmap);
				imageView.setImageDrawable(drawable);
			}

			public void onBitmapLoadFailed(String url) {
				// Do nothing
			}
		};

		return loadImage(key, url, false, callback);
	}

	/**
	 *
	 * @param callbackKey the key for the download callback
	 * @param url the url of the image
	 * @param callback the method to call after loading the image
	 * @return true if image was already in cache, and callback was immediately called
	 */
	public boolean loadImage(String callbackKey, String url, boolean blur, OnBitmapLoaded callback) {
		// If this key was originally associated with another download, disassociate it
		clearCallbacks(callbackKey);

		// First check if we already have the image in memory; if we do, just do the callback
		Bitmap image = getImage(url, blur, false);
		if (image != null) {
			callback.onBitmapLoaded(url, image);
			return true;
		}
		else if (mIgnore.contains(url)) {
			Log.v(mLogTag, "Url has been ignored because it previously could not be found: " + url);
			callback.onBitmapLoadFailed(url);
			return false;
		}

		// If we don't have the image cached in memory, either latch onto an existing download or start a new one
		ImageTask task;
		if (!mDownloadsByUrl.containsKey(url)) {
			task = createImageTask(url, blur);
			mDownloadsByUrl.put(url, task);
			sExecutor.execute(task);
		}
		else {
			task = mDownloadsByUrl.get(url);
		}

		task.addCallback(callbackKey, callback);
		mDownloadsByKey.put(callbackKey, task);

		return false;
	}

	// This clears all callbacks associated with a key.  This is useful when a key switches
	// from being a download to loading an already cached image (common with ImageViews)
	public void clearCallbacks(String key) {
		if (mDownloadsByKey.containsKey(key)) {
			mDownloadsByKey.get(key).removeCallback(key);
			mDownloadsByKey.remove(key);
		}
	}

	/**
	 * Clears all callbacks for a particular URL.  Useful when you can't call clearAllCallbacks() because
	 * someone else may be using the ImageCache at that moment.
	 * @param url the url to disassociate all callbacks
	 */
	public void clearCallbacksByUrl(String url) {
		if (mDownloadsByUrl.containsKey(url)) {
			ImageTask task = mDownloadsByUrl.get(url);
			for (String key : task.mCallbacks.keySet()) {
				mDownloadsByKey.remove(key);
			}
			task.mCallbacks.clear();
		}
	}

	/**
	 * Clears all callbacks in the ImageCache.  This is useful when you don't want to retain any accidental
	 * ImageViews during a rotation.
	 */
	public void clearAllCallbacks() {
		for (ImageTask task : mDownloadsByUrl.values()) {
			for (String key : task.mCallbacks.keySet()) {
				mDownloadsByKey.remove(key);
			}
			task.mCallbacks.clear();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Eviction policies

	/**
	 * Subclass to provide your own mem cache and disk cache implementations
	 */
	public static abstract class EvictionPolicy {

		public abstract LruCache<String, Bitmap> generateMemCache();

		public abstract DiskLruCache generateDiskCache();
	}

	/**
	 * Mem cache evictions work based on the total number of entries rather than total size. Disk cache
	 * evictions are on a total size policy.
	 */
	public static class NumberEvictionPolicy extends EvictionPolicy {

		private Context mContext;

		private String mLogTag;

		private int mNumMemCacheEntries;
		private int mDiskCacheSize;

		public NumberEvictionPolicy(Context context, int numMemCacheEntries, int numDiskCacheEntries, String logTag) {
			mContext = context;
			mLogTag = logTag;
			mNumMemCacheEntries = numMemCacheEntries;
			mDiskCacheSize = numDiskCacheEntries;
		}

		public LruCache<String, Bitmap> generateMemCache() {
			Log.i(mLogTag, "Creating mem cache of " + mNumMemCacheEntries + " max entries.");
			return new LruCache<String, Bitmap>(mNumMemCacheEntries) {
				@Override
				protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
					super.entryRemoved(evicted, key, oldValue, newValue);
					Log.i(mLogTag, "Entry removed key=" + key + " evicted=" + evicted);
					handleMemCacheEviction(evicted, oldValue, newValue);
				}
			};
		}

		public DiskLruCache generateDiskCache() {
			return L2ImageCache.generateDiskCache(mContext, mLogTag, mDiskCacheSize);
		}

	}
	/**
	 * Mem cache and disk cache evictions occur based on the sizes (in bytes) you provide for each cache.
	 */
	public static class SizeEvictionPolicy extends EvictionPolicy {

		private Context mContext;

		private int mMemCacheSize;

		private int mDiskCacheSize;

		private String mLogTag;

		public SizeEvictionPolicy(Context context, int memCacheSize, int diskCacheSize, String logTag) {
			mContext = context;
			mMemCacheSize = memCacheSize;
			mDiskCacheSize = diskCacheSize;
			mLogTag = logTag;
		}

		public LruCache<String, Bitmap> generateMemCache() {
			Log.i(mLogTag, "Creating mem cache of size " + (mMemCacheSize / (1024 * 1024)) + " mb");
			return new LruCache<String, Bitmap>(mMemCacheSize) {

				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					// The cache size will be measured in bytes rather than number of items.
					if (Build.VERSION.SDK_INT >= 12) {
						return bitmap.getByteCount();
					}
					else {
						// Approximation of byte count pre-getByteCount()
						return bitmap.getRowBytes() * bitmap.getHeight();
					}
				}

				@Override
				protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
					super.entryRemoved(evicted, key, oldValue, newValue);
					handleMemCacheEviction(evicted, oldValue, newValue);
				}

			};
		}

		public DiskLruCache generateDiskCache() {
			return L2ImageCache.generateDiskCache(mContext, mLogTag, mDiskCacheSize);
		}
	}

	private static DiskLruCache generateDiskCache(Context context, String logTag, int size) {
		DiskLruCache diskCache = null;
		try {
			Log.i(logTag, "Creating DiskLruCache of size " + (size / (1024 * 1024)) + " mb");

			File diskCacheDir = getDiskCacheDir(context, logTag);
			diskCache = DiskLruCache.open(diskCacheDir, AndroidUtils.getAppCode(context), 1, size);
			return diskCache;
		}
		catch (IOException e) {
			// In the case that we can't open the disk cache, blow up catastrophically (as not having
			// the disk cache will really screw things up down the line).
			throw new RuntimeException("Failed to create DiskLruCache. We require it.");
		}
	}

	private static void handleMemCacheEviction(boolean evicted, Bitmap oldValue, Bitmap newValue) {
		// Explicitly recycle the bitmap if it's been evicted or replaced;
		// older Android phones need the push.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if (evicted || newValue != null) {
				oldValue.recycle();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Handler

	private final InternalHandler sHandler = new InternalHandler();

	private static final int MESSAGE_FINISHED = 1;
	private static final int MESSAGE_CANCELLED = 2;

	private class InternalHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			ImageTask task = (ImageTask) msg.obj;
			String url = task.getUrl();

			switch (msg.what) {
			case MESSAGE_FINISHED:
				if (task.mBitmap != null) {
					for (OnBitmapLoaded callback : task.mCallbacks.values()) {
						callback.onBitmapLoaded(url, task.mBitmap);
					}
				}
				else {
					for (OnBitmapLoaded callback : task.mCallbacks.values()) {
						callback.onBitmapLoadFailed(url);
					}
				}
				break;
			case MESSAGE_CANCELLED:
				// Do nothing special (for now)
				break;
			}

			// Either way, clean the download after it is done
			for (String key : task.mCallbacks.keySet()) {
				mDownloadsByKey.remove(key);
			}
			mDownloadsByUrl.remove(url);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Download execution

	private static final int POOL_SIZE = 1;

	private ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 10, TimeUnit.SECONDS,
			new PriorityBlockingQueue<Runnable>());

	private class ImageTask extends FutureTask<Bitmap> implements Comparable<ImageTask> {

		// For execution
		public final BitmapCallable mBitmapCallable;

		// Post execution
		public ConcurrentHashMap<String, OnBitmapLoaded> mCallbacks;
		public Bitmap mBitmap;

		// Solely for comparing which download should go first
		public final long mCreateTime;

		private ImageTask(BitmapCallable bitmapCallable) {
			super(bitmapCallable);

			mBitmapCallable = bitmapCallable;

			mCallbacks = new ConcurrentHashMap<String, OnBitmapLoaded>();

			mCreateTime = System.currentTimeMillis();
		}

		@Override
		protected void done() {
			try {
				mBitmap = get();
			}
			catch (InterruptedException e) {
				Log.w(mLogTag, "Image download interrupted.", e);
				return;
			}
			catch (ExecutionException e) {
				throw new RuntimeException("An error occurred while downloading an image.", e.getCause());
			}
			catch (CancellationException e) {
				mBitmapCallable.interruptDownload();
				Message message = sHandler.obtainMessage(MESSAGE_CANCELLED, this);
				message.sendToTarget();
				return;
			}
			catch (Throwable t) {
				throw new RuntimeException("An error occurred while downloading an image.", t);
			}

			Message message = sHandler.obtainMessage(MESSAGE_FINISHED, this);
			message.sendToTarget();
		}

		public void addCallback(String key, OnBitmapLoaded callback) {
			if (isDone()) {
				String url = mBitmapCallable.getUrl();
				Bitmap bitmap = getImage(url, true);
				if (bitmap != null) {
					callback.onBitmapLoaded(url, getImage(url, true));
				}
				else {
					callback.onBitmapLoadFailed(url);
				}
			}
			else {
				mCallbacks.put(key, callback);
			}
		}

		public void removeCallback(String key) {
			mCallbacks.remove(key);
		}

		public String getUrl() {
			return mBitmapCallable.getUrl();
		}

		@Override
		public int compareTo(ImageTask another) {
			if (mCreateTime < another.mCreateTime) {
				return 1;
			}
			else if (mCreateTime > another.mCreateTime) {
				return -1;
			}
			return 0;
		}
	}

	public ImageTask createImageTask(String url, boolean blur) {
		BitmapCallable callable;
		if (blur) {
			callable = new BitmapBlur(url);
		}
		else {
			callable = new BitmapDownload(url);
		}
		return new ImageTask(callable);
	}

	private abstract class BitmapCallable implements Callable<Bitmap> {
		public abstract void interruptDownload();

		public abstract String getUrl();
	}

	/**
	 * This will load a bitmap either from the DiskLruCache or the
	 * network (at which point it adds the image to the DiskLruCache).
	 * It handles both these situations since they are both long(ish)
	 * running services.
	 */
	private class BitmapDownload extends BitmapCallable {
		private String mUrl;
		private HttpURLConnection mConn;
		private boolean mCancelled;

		public BitmapDownload(String url) {
			mUrl = url;
			mCancelled = false;
		}

		public Bitmap call() throws Exception {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

			// First we try getting the image from the disk cache
			if (hasImageInDiskCache(mUrl)) {
				return getImage(mUrl, true);
			}

			// Construct URL
			URL imageUrl;
			try {
				imageUrl = new URL(mUrl);
			}
			catch (MalformedURLException e) {
				Log.w(mLogTag, "Could not fetch image, bad url.", e);
				return null;
			}

			// Open HTTP network connection and store Bitmap in disk cache
			try {
				mConn = makeHttpBitmapConn(imageUrl);
				return downloadBitmapToDiskCacheFromNetwork(mUrl, mConn);
			}
			catch (IOException e) {
				if (!mCancelled && !ExpediaBookingApp.IS_AUTOMATION) {
					Log.w(mLogTag, "Could not fetch image, could not load.", e);
				}
				return null;
			}
		}

		@Override
		public void interruptDownload() {
			mCancelled = true;
			if (mConn != null) {
				mConn.disconnect();
			}
		}

		@Override
		public String getUrl() {
			return mUrl;
		}

	}

	private class BitmapBlur extends BitmapCallable {
		private String mOrigUrl;
		private HttpURLConnection mConn;
		private boolean mNetworkDownloadCancelled;

		public BitmapBlur(String url) {
			mOrigUrl = url;
		}

		@Override
		public Bitmap call() throws Exception {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

			if (mVerboseDebugLoggingEnabled) {
				Log.d(mLogTag, "generating blurred bitmap and adding to disk cache");
			}

			boolean wasWritten;
			Editor editor = null;
			Bitmap blurBitmap;
			OutputStream stream = null;

			try {
				// Grab the original Bitmap from the cache
				Bitmap origBitmap = getImage(mOrigUrl, true);
				if (origBitmap == null) {
					Log.i(mLogTag, "Attempting to load blurred Bitmap where original Bitmap is not in cache. Downloading original from network, url=" + mOrigUrl);

					// Download the regular bitmap and load into memory
					URL imageUrl = new URL(mOrigUrl);
					mConn = makeHttpBitmapConn(imageUrl);
					downloadBitmapToDiskCacheFromNetwork(mOrigUrl, mConn);
					origBitmap = getImage(mOrigUrl, true);
				}

				if (origBitmap == null) {
					Log.w(mLogTag, "Could not fetch original image url=" + mOrigUrl);
					return null;
				}

				// Allocate a new Bitmap for the blurred Bitmap
				blurBitmap = BitmapUtils.stackBlurAndDarken(origBitmap, mContext,
					BLURRED_IMAGE_SIZE_REDUCTION_FACTOR, mBlurRadius, mDarkenMultiplier);

				// Write the Blurred bitmap into the disk cache
				editor = mDiskCache.edit(getDiskKeyForBlurred(mOrigUrl));
				stream = new BufferedOutputStream(editor.newOutputStream(0), 4096);
				wasWritten = blurBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				editor.commit();

				mDiskCache.flush();
			}
			catch (MalformedURLException e) {
				Log.w(mLogTag, "Could not fetch image, bad url.", e);
				return null;
			}
			catch (IOException e) {
				if (!mNetworkDownloadCancelled && !ExpediaBookingApp.IS_AUTOMATION) {
					Log.w(mLogTag, "Could not fetch image, could not load.", e);
				}
				return null;
			}
			catch (OutOfMemoryError e) {
				Log.e(mLogTag, "Could not blur image, ran out of memory.", e);
				return null;
			}
			finally {
				if (editor != null) {
					editor.abortUnlessCommitted();
				}
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			}

			if (wasWritten) {
				return getBlurredImage(mOrigUrl, true);
			}
			else {
				Log.e(mLogTag, "Failed to write to disk!!");
				return null;
			}
		}

		@Override
		public void interruptDownload() {
			mNetworkDownloadCancelled = true;
			if (mConn != null) {
				mConn.disconnect();
			}
		}

		@Override
		public String getUrl() {
			return mOrigUrl;
		}
	}

	private static HttpURLConnection makeHttpBitmapConn(URL imageUrl) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
		conn.setDoInput(true);
		conn.connect();
		return conn;
	}

	// 4k of memory is alright for a permanent buffer (as long as
	// we are only doing one download at once).
	private final byte[] mBuffer = new byte[4096];

	/**
	 * This utility method will download a Bitmap from the network, writing it directly to the disk
	 * cache, and then return the in-memory instance of the Bitmap.
	 * @param url
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	private Bitmap downloadBitmapToDiskCacheFromNetwork(String url, HttpURLConnection conn) throws IOException {
		Log.i(mLogTag, "Downloading bitmap from network, url=" + url);

		long tic = System.currentTimeMillis();
		Editor editor = null;
		try {
			InputStream in = conn.getInputStream();

			// IMPORTANT DETAIL: Since our pool size is one, we can
			// safely edit one entry at a time.  If you ever want
			// concurrent downloads, you'll have to wait setup some
			// method for safely concurrently editing the disk cache.
			editor = mDiskCache.edit(getDiskKey(url));
			OutputStream stream = editor.newOutputStream(0);
			int available = 0;
			while ((available = in.read(mBuffer)) >= 0) {
				stream.write(mBuffer, 0, available);
			}
			editor.commit();

			// Use the cache to get the image now
			return getImage(url, true);
		}
		catch (FileNotFoundException e) {
			Log.w(mLogTag, "Ignoring url because it could not be found: " + url);
			mIgnore.add(url);
			return null;
		}
		catch (OutOfMemoryError e) {
			Log.e(mLogTag, "Could not fetch image, ran out of memory.", e);
			return null;
		}
		finally {
			if (editor != null) {
				editor.abortUnlessCommitted();
			}
			long toc = System.currentTimeMillis();
			Log.v(mLogTag, "Download took " + (toc - tic) + "ms url=" + url);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Static utilities

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 *
	 * @param context The context to use
	 * @param uniqueName A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use external cache dir
		// otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
				!isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
				context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * Check if external storage is built-in or removable.
	 *
	 * @return True if external storage is removable (like an SD card), false
	 *         otherwise.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static boolean isExternalStorageRemovable() {
		if (AndroidUtils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/**
	 * Get the external app cache directory.
	 *
	 * @param context The context to use
	 * @return The external cache dir
	 */
	@TargetApi(Build.VERSION_CODES.FROYO)
	public static File getExternalCacheDir(Context context) {
		if (AndroidUtils.hasFroyo()) {
			return context.getExternalCacheDir();
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	//////////////////////////////////////////////////////////////////////////
	// Debugging

	// TODO MOAR DEBUGGING LOGGING
	// cache hits, miss, cache logging of what is in cache

	private boolean mVerboseDebugLoggingEnabled = false;

	public void setVerboseDebugLoggingEnabled(boolean enabled) {
		mVerboseDebugLoggingEnabled = enabled;
	}

	public void debugInfo() {
		Log.i(mLogTag, "Memory cache maxSize=" + mMemoryCache.maxSize() + " size=" + mMemoryCache.size());
		Log.i(mLogTag, "Disk cache maxSize=" + mDiskCache.getMaxSize() + " size=" + mDiskCache.size());
	}
}
