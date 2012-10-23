package com.expedia.bookings.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import com.expedia.bookings.R;
import com.jakewharton.DiskLruCache;
import com.jakewharton.DiskLruCache.Editor;
import com.jakewharton.DiskLruCache.Snapshot;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.util.LruCache;

public class BackgroundImageCache {
	private LruCache<String, Bitmap> mMemoryCache;
	private DiskLruCache mDiskCache;
	private boolean mCancelAddBitmap = false;
	private Semaphore mAddingBitmapSem = new Semaphore(1);
	private Semaphore mAddingDefaultsSem = new Semaphore(1);

	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; //10MB
	private static final int DISK_WRITE_BUFFER_SIZE = 4096;
	private static final String DISK_CACHE_SUBDIR = "LruImageChacher";
	private static final String TAG = "BG_IMAGE_CACHE";
	private static final String BLUR_KEY_SUFFIX = "blurkeysuffix";
	private static final String DEFAULT_KEY = "defaultkey";
	private static final int DECODE_IN_SAMPLE_SIZE = 1;//1 is lossless, but it takes lots of memory
	private static final int DECODE_IN_SAMPLE_SIZE_BLURRED = 1;//1 is lossless, but it takes lots of memory
	private static final int BLURRED_IMAGE_SIZE_REDUCTION_FACTORY = 4;
	private static final int STACK_BLUR_RADIUS = Math.round(10 / BLURRED_IMAGE_SIZE_REDUCTION_FACTORY); //10 is what we want, but because we are shrinking the image dimensions

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

	public Bitmap getBitmap(String bmapkey, Context context) {
		String key = bmapkey.toLowerCase();
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

	public void putBitmap(final String bmapkey, final Bitmap bitmap, final boolean blur) {
		Runnable putBitmapRunner = new Runnable() {

			@Override
			public void run() {

				Log.i(TAG, "putBitmap key:" + bmapkey + " bmapSize w:" + bitmap.getWidth() + " h:" + bitmap.getHeight()
						+ " blur:" + blur);

				Editor bgImageEditor = null;
				Editor blurredEditor = null;
				try {
					mAddingBitmapSem.acquire();
					mCancelAddBitmap = false;
					bgImageEditor = mDiskCache.edit(bmapkey);
					if (blur) {
						blurredEditor = mDiskCache.edit(getBlurredKey(bmapkey));
					}

					Bitmap blurred = null;
					if (blur) {
						blurred = blur(bitmap);
						if (mCancelAddBitmap) {
							throw new Exception("Canceled after blur");
						}
						addBitmapToDiskCacheEditor(blurredEditor, blurred);
						if (mCancelAddBitmap) {
							throw new Exception("Canceled after blurred added to cache");
						}
					}

					addBitmapToDiskCacheEditor(bgImageEditor, bitmap);

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
					mAddingBitmapSem.release();
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
		boolean isAdding = !mAddingBitmapSem.tryAcquire();
		if (isAdding) {
			mAddingBitmapSem.release();
		}
		return isAdding;
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
					putBitmap(DEFAULT_KEY, bg, false);
					waitForAddingBitmap(50);
					bg.recycle();
				}
				if (!this.hasKey(getBlurredKey(DEFAULT_KEY))) {
					options.inSampleSize = 4;
					Bitmap bg = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.default_flights_background_blurred,
							options);
					putBitmap(getBlurredKey(DEFAULT_KEY), bg, false);
					waitForAddingBitmap(50);
					bg.recycle();
				}
			}
			finally
			{
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

	private Bitmap blur(Bitmap bmapToBlur) {
		//Shrink it, we will have a lot fewer pixels, and they are going to get blurred so nobody should care...
		int w = bmapToBlur.getWidth() / BLURRED_IMAGE_SIZE_REDUCTION_FACTORY;
		int h = bmapToBlur.getHeight() / BLURRED_IMAGE_SIZE_REDUCTION_FACTORY;
		Bitmap shrunk = Bitmap.createScaledBitmap(bmapToBlur, w, h, false);

		//Blur and darken it
		return stackBlurAndDarken(shrunk);
	}

	//This does require some memory...
	private Bitmap stackBlurAndDarken(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		final int radius = STACK_BLUR_RADIUS;
		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		final int wm = w - 1;
		final int hm = h - 1;
		final int wh = w * h;
		final int div = radius + radius + 1;

		int[] pix = new int[wh];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		long tic = System.currentTimeMillis();

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				}
				else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				}
				else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		//Darken each pixel. (this should be the equivalent of adding a black .35 opacity mask)
		double maskValue = 0.65;
		for (int d = 0; d < pix.length; d++) {
			pix[d] = Color.rgb((int) (Color.red(pix[d]) * maskValue), (int) (Color.green(pix[d]) * maskValue),
					(int) (Color.blue(pix[d]) * maskValue));
		}

		long toc = System.currentTimeMillis();

		Bitmap newbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		newbitmap.setPixels(pix, 0, w, 0, 0, w, h);

		Log.d("StackBlurAndDarken", (toc - tic) + "ms");
		return newbitmap;
	}

	/////////////////////////////////
	// Mem Cacher

	private void initMemCache() {
		Log.d(TAG, "initMemCache");
		//One place for regular, and one place for blurred.
		mMemoryCache = new LruCache<String, Bitmap>(2);
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
