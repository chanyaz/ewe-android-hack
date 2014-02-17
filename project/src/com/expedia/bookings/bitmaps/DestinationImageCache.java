package com.expedia.bookings.bitmaps;

import android.content.Context;

import com.mobiata.android.bitmaps.L2ImageCache;

public class DestinationImageCache {

	private static final String LOGGING_TAG = "DestinationImageCache";

	private static final int MAX_NUM_MEM_CACHE_ENTRIES = 2;
	private static final int SIZE_DISK_CACHE_IN_BYTES = 1024 * 1024 * 20; // 20 mb

	private DestinationImageCache() {
		// This class is a singleton
	}

	private static L2ImageCache sInstance;

	public static L2ImageCache getInstance() {
		if (sInstance == null) {
			throw new RuntimeException("Attempted to retrieve DestinationImageCache before init!");
		}
		return sInstance;
	}

	public static void init(Context context) {
		L2ImageCache.EvictionPolicy policy = new L2ImageCache.NumberEvictionPolicy(context, MAX_NUM_MEM_CACHE_ENTRIES,
			SIZE_DISK_CACHE_IN_BYTES, LOGGING_TAG);
		sInstance = new L2ImageCache(LOGGING_TAG, policy);
	}

}
