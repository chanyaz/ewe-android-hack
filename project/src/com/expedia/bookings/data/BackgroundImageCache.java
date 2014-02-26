package com.expedia.bookings.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.expedia.bookings.R;
import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.DiskLruCache.Editor;
import com.jakewharton.disklrucache.DiskLruCache.Snapshot;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.BitmapUtils;
import com.mobiata.android.util.AndroidUtils;

public class BackgroundImageCache {
	private LruCache<String, Bitmap> mMemoryCache;
	private DiskLruCache mDiskCache;
	private boolean mCancelAddBitmap = false;
	private Semaphore mAddingBitmapSem = new Semaphore(1);
	private Semaphore mAddingDefaultsSem = new Semaphore(1);

	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; //10MB
	private static final int DISK_WRITE_BUFFER_SIZE = 4096;
	private static final float DARKEN_MULTIPLIER = 0.65f;
	private static final String DISK_CACHE_SUBDIR = "LruImageChacher";
	private static final String TAG = "BG_IMAGE_CACHE";
	private static final String BLUR_KEY_SUFFIX = "blurkeysuffix";
	private static final String DEFAULT_KEY = "defaultkey";
	private static final int DECODE_IN_SAMPLE_SIZE = 1;//1 is lossless, but it takes lots of memory
	private static final int DECODE_IN_SAMPLE_SIZE_BLURRED = 1;//1 is lossless, but it takes lots of memory
	private static final int BLURRED_IMAGE_SIZE_REDUCTION_FACTOR = 4;
	private static final int STACK_BLUR_RADIUS = 28;//We divide here, so our radius reflects the size of our scaled down blurred image

	public BackgroundImageCache(Context context) {
		initMemCache();
		initDiskCache(context);
	}

	public void loadDefaultsInThread(final Context context) {
		Runnable loadDefaults = new Runnable() {
			@Override
			public void run() {
				addDefaultBgToCache(context);
			}
		};
		Thread loadDefaultsThread = new Thread(loadDefaults);
		loadDefaultsThread.setPriority(Thread.MIN_PRIORITY);
		loadDefaultsThread.start();
	}

	///////////////////////////
	// Public

	public boolean hasKeyAndBlurredKey(String bmapkey) {
		Log.d(TAG, "hasKeyAndBlurredKey:" + bmapkey);

		return hasKey(bmapkey) && hasKey(getBlurredKey(bmapkey));
	}

	public boolean isDefaultInCache() {
		return hasKeyAndBlurredKey(DEFAULT_KEY);
	}

	public Bitmap getBitmap(String bmapkey, Context context) {
		String key = bmapkey.toLowerCase(Locale.ENGLISH);
		Log.i(TAG, "getBitmap key:" + key);
		Bitmap ret = null;
		ret = getBitmapFromMemoryCache(key);
		if (ret != null) {
			Log.d(TAG, "MemCache hit!");
		}
		else {
			Log.d(TAG, "MemCache miss!");
			ret = getBitmapFromDiskCache(key);
			if (ret != null) {
				Log.d(TAG, "DiskCache hit!");
				addBitmapToMemoryCache(key, ret);
			}
			else {
				Log.d(TAG, "DiskCache miss! (revert to default)");

				//We put the default in the cache
				if (DEFAULT_KEY.equals(key) || getBlurredKey(DEFAULT_KEY).equals(key)) {
					addDefaultBgToCache(context);
				}
				if (this.keyIsBlurredKey(key)) {
					ret = getBitmap(getBlurredKey(DEFAULT_KEY), context);
				}
				else {
					ret = getBitmap(DEFAULT_KEY, context);
				}
			}
		}
		return ret;
	}

	public Bitmap getBlurredBitmap(String bmapkey, Context context) {
		return getBitmap(getBlurredKey(bmapkey), context);
	}

	public void putBitmap(final String bmapkey, final Bitmap bitmap, final boolean blur, final Context context) {
		Runnable putBitmapRunner = new Runnable() {

			@Override
			public void run() {

				Log.i(TAG, "putBitmap key:" + bmapkey + " bmapSize w:" + bitmap.getWidth() + " h:" + bitmap.getHeight()
						+ " blur:" + blur);

				Editor bgImageEditor = null;
				Editor blurredEditor = null;
				boolean semGot = false;
				try {
					mAddingBitmapSem.acquire();
					semGot = true;
					mCancelAddBitmap = false;
					bgImageEditor = mDiskCache.edit(bmapkey);
					if (blur) {
						blurredEditor = mDiskCache.edit(getBlurredKey(bmapkey));
					}

					Bitmap blurred = null;
					if (blur) {
						blurred = BitmapUtils.stackBlurAndDarken(bitmap, context, BLURRED_IMAGE_SIZE_REDUCTION_FACTOR,
							STACK_BLUR_RADIUS, DARKEN_MULTIPLIER);
						if (mCancelAddBitmap) {
							throw new Exception("Canceled after blur");
						}
						if (!addBitmapToDiskCacheEditor(blurredEditor, blurred)) {
							throw new Exception("Error adding blurred bitmap to editor");
						}
						if (mCancelAddBitmap) {
							throw new Exception("Canceled after blurred added to cache");
						}
					}

					if (!addBitmapToDiskCacheEditor(bgImageEditor, bitmap)) {
						throw new Exception("Error adding bitmap to editor");
					}

					if (mCancelAddBitmap) {
						throw new Exception("Canceled after bg added to disk cache");
					}

					bgImageEditor.commit();
					if (blur) {
						blurredEditor.commit();
					}
					mDiskCache.flush();

					if (blurred != null) {
						blurred.recycle();
					}
				}
				catch (Exception ex) {
					Log.e("Exception adding bitmap:", ex);
					try {
						if (bgImageEditor != null) {
							bgImageEditor.abort();
						}
						if (blur && blurredEditor != null) {
							blurredEditor.abort();
						}
					}
					catch (IOException e) {
						Log.e("Exception aborting commit", e);
					}
				}
				finally {
					mCancelAddBitmap = false;
					if (semGot) {
						mAddingBitmapSem.release();
					}
				}

			}
		};
		Thread updateThread = new Thread(putBitmapRunner);
		updateThread.start();
	}

	public void clearMemCache() {
		mMemoryCache.evictAll();
		initMemCache();
	}

	public void clearDiskCache(Context context) {
		try {
			mDiskCache.delete();
			initDiskCache(context);
		}
		catch (IOException e) {
			Log.e(TAG, "Exception deleting the disk cache", e);
		}
	}

	public boolean isAddingBitmap() {
		boolean gotSem = mAddingBitmapSem.tryAcquire();
		if (gotSem) {
			mAddingBitmapSem.release();
		}
		return !gotSem;
	}

	public void waitForAddingBitmap(long delay) {
		boolean aquired = false;
		try {
			Thread.sleep(delay);
			if (!isAddingBitmap()) {
				return;
			}
			mAddingBitmapSem.acquire();
			aquired = true;
		}
		catch (Exception ex) {
			//Don't care.
		}
		finally {
			if (aquired) {
				mAddingBitmapSem.release();
			}
		}

	}

	public void cancelPutBitmap() {
		mCancelAddBitmap = true;
	}

	//////////////////////////////////////////
	// Helpers

	private boolean hasKey(String bmapkey) {
		return memCacheHasKey(bmapkey) || diskCacheHasKey(bmapkey);
	}

	private void addDefaultBgToCache(Context context) {
		if (mAddingDefaultsSem.tryAcquire()) {
			try {
				Log.d("Adding defaults to cache...");
				BitmapFactory.Options options = new BitmapFactory.Options();
				if (!this.hasKey(DEFAULT_KEY)) {
					options.inSampleSize = DECODE_IN_SAMPLE_SIZE;
					Bitmap bg = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.default_flights_background,
							options);
					putBitmap(DEFAULT_KEY, bg, false, context);
					waitForAddingBitmap(50);
					bg.recycle();
				}
				if (!this.hasKey(getBlurredKey(DEFAULT_KEY))) {
					options.inSampleSize = 4;
					Bitmap bg = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.default_flights_background_blurred,
							options);
					putBitmap(getBlurredKey(DEFAULT_KEY), bg, false, context);
					waitForAddingBitmap(50);
					bg.recycle();
				}
			}
			finally {
				mAddingDefaultsSem.release();
			}
		}
		else {
			//If we are already loading the defaults, we wait for them to finish...
			boolean semGot = false;
			try {
				mAddingDefaultsSem.acquire();
				semGot = true;
			}
			catch (Exception ex) {
				Log.e("Exception waiting for mAddingDefaultsSem");
			}
			finally {
				if (semGot) {
					mAddingDefaultsSem.release();
				}
			}
		}
	}

	private String getBlurredKey(String unblurredKey) {
		return unblurredKey + BLUR_KEY_SUFFIX;
	}

	private boolean keyIsBlurredKey(String key) {
		return key.endsWith(BLUR_KEY_SUFFIX);
	}

	/////////////////////////////////
	// Mem Cacher

	private void initMemCache() {
		Log.d(TAG, "initMemCache");
		//One place for regular, and one place for blurred.
		mMemoryCache = new LruCache<String, Bitmap>(2) {
			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
				super.entryRemoved(evicted, key, oldValue, newValue);

				// Explicitly recycle the bitmap if it's been evicted or replaced;
				// older Android phones need the push.
				if (evicted || newValue != null) {
					oldValue.recycle();
				}
			}
		};
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		Log.d(TAG, "addBitmapToMemoryCache key:" + key);
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemoryCache(String key) {
		Log.d(TAG, "getBitmapFromMemoryCache key:" + key);
		return mMemoryCache.get(key);
	}

	private boolean memCacheHasKey(String key) {
		return (mMemoryCache.get(key) != null);
	}

	///////////////////////////////////
	// Disk Cache

	private void initDiskCache(Context context) {
		Log.d(TAG, "initDiskCache");
		File cacheDir = context.getCacheDir();
		File subCacheDir = new File(cacheDir, DISK_CACHE_SUBDIR);

		Log.d(TAG, "CacheDir:" + subCacheDir.getAbsolutePath());

		if (cacheDir.isDirectory() && cacheDir.exists()) {
			if (!subCacheDir.exists()) {
				if (!subCacheDir.mkdir()) {
					Log.e(TAG, "Error creating cache directory!");
				}
			}
		}

		try {
			Log.d(TAG, "FILES IN CACHE DIRECTORY");
			File[] files = subCacheDir.listFiles();
			for (File file : files) {
				Log.d(TAG, "file:" + file.getName());
			}

			mDiskCache = DiskLruCache.open(subCacheDir, AndroidUtils.getAppCode(context), 1, DISK_CACHE_SIZE);
		}
		catch (Exception ex) {
			Log.e(TAG, "Error initiailizing disk cache", ex);
		}
	}

	private boolean addBitmapToDiskCacheEditor(Editor editor, Bitmap bitmap) {
		boolean retVal = false;
		try {
			OutputStream stream = null;
			boolean wrote = false;
			try {
				stream = new BufferedOutputStream(editor.newOutputStream(0), DISK_WRITE_BUFFER_SIZE);
				wrote = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			}
			finally {
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			}
			if (wrote) {
				Log.i(TAG, "Stream wrote successfully");
				retVal = true;

			}
			else {
				Log.i(TAG, "Stream write failed");
			}
		}
		catch (Exception ex) {
			Log.e("Exception in addBitmapToEditor", ex);
		}
		return retVal;
	}

	private Bitmap getBitmapFromDiskCache(String key) {
		Log.d(TAG, "getBitmapFromDiskCache key:" + key);
		try {
			if (!mDiskCache.isClosed()) {
				if (mDiskCache.get(key) != null) {
					Log.d(TAG, "mDiskCache.get(key) != null");
					Snapshot snapshot = mDiskCache.get(key);
					BitmapFactory.Options options = new BitmapFactory.Options();
					if (this.keyIsBlurredKey(key)) {
						options.inSampleSize = DECODE_IN_SAMPLE_SIZE_BLURRED;
					}
					else {
						options.inSampleSize = DECODE_IN_SAMPLE_SIZE;
					}
					return BitmapFactory.decodeStream(snapshot.getInputStream(0), null, options);
				}
			}
		}
		catch (Exception ex) {
			Log.e(TAG, "Exception getting bitmap from disk cache:", ex);
		}
		return null;
	}

	private boolean diskCacheHasKey(String key) {
		boolean retVal = false;
		if (!mDiskCache.isClosed()) {
			//mDiskCache.get(arg0)
			try {
				retVal = (mDiskCache.get(key) != null);
			}
			catch (Exception ex) {
				Log.e("DiskCache Exception", ex);
			}
		}
		Log.d(TAG, "diskCacheHasKey:" + key + " is " + retVal);
		return retVal;
	}
}
