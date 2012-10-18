package com.expedia.bookings.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import com.jakewharton.DiskLruCache;
import com.jakewharton.DiskLruCache.Editor;
import com.jakewharton.DiskLruCache.Snapshot;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

public class BackgroundImageCache {
	private LruCache<String, Bitmap> mMemoryCache;
	private DiskLruCache mDiskCache;

	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; //10MB
	private static final String DISK_CACHE_SUBDIR = "LruImageChacher";

	public BackgroundImageCache(Context context) {
		initMemCache();
		initDiskCache(context);
	}

	///////////////////////////
	// Public

	public Bitmap getBitmap(String key) {
		Bitmap ret = null;
		ret = getBitmapFromMemoryCache(key);
		if (ret == null) {
			ret = getBitmapFromDiskCache(key);
			if (ret != null) {
				Log.i("DiskCache hit!");
				addBitmapToMemoryCache(key, ret);
			}
		}else{
			Log.i("MemCache hit!");
		}
		return ret;
	}

	public void putBitmap(String key, Bitmap bitmap) {
		addBitmapToMemoryCache(key, bitmap);
		addBitmapToDiskCache(key, bitmap);
	}
	
	public void clearMemCache(){
		mMemoryCache.evictAll();
	}
	
	public void clearDiskCache(){
		try {
			mDiskCache.delete();
		}
		catch (IOException e) {
			Log.e("Exception deleting the disk cache",e);
		}
	}

	///////////////////////////////////
	// Disk Cache

	private void initDiskCache(Context context) {
		File cacheDir = context.getCacheDir();
		File subCacheDir = new File(cacheDir,DISK_CACHE_SUBDIR);

		if (cacheDir.isDirectory() && cacheDir.exists()) {
			if (!subCacheDir.exists()) {
				if (!subCacheDir.mkdir()) {
					Log.e("Error creating cache directory!");
				}
			}
		}

		try {
			mDiskCache = DiskLruCache.open(subCacheDir, AndroidUtils.getAppCode(context), 1, DISK_CACHE_SIZE);
		}
		catch (Exception ex) {
			Log.e("Error initiailizing disk cache", ex);
		}
	}

	private boolean addBitmapToDiskCache(String key, Bitmap bitmap) {
		try {
			if (mDiskCache.get(key) == null) {
				Editor editor = mDiskCache.edit(key);
				while (editor == null) {
					editor = mDiskCache.edit(key);
					Thread.sleep(500);
				}
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				editor.set(0, stream.toString());
				editor.commit();
				return true;
			}
		}
		catch (Exception ex) {
			Log.e("Exception loading from disk cache:", ex);
		}
		return false;
	}

	private Bitmap getBitmapFromDiskCache(String key) {
		try {
			if (mDiskCache.get(key) != null) {
				Snapshot snapshot = mDiskCache.get(key);
				return BitmapFactory.decodeStream(snapshot.getInputStream(0));
			}
		}
		catch (Exception ex) {
			Log.e("Exception getting bitmap from disk cache:", ex);
		}
		return null;
	}

	/////////////////////////////////
	// Mem Cacher

	private void initMemCache() {
		//because we are using this for bg images, we only store one image in memory
		mMemoryCache = new LruCache<String, Bitmap>(1);
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

}
