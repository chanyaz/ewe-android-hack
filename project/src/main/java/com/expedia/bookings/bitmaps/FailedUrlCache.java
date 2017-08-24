package com.expedia.bookings.bitmaps;

import java.util.HashSet;
import java.util.Set;

public class FailedUrlCache {

	private static volatile FailedUrlCache singletonInstance = null;

	private static final Set<String> mIgnore = new HashSet<String>();

	private FailedUrlCache() {
	}

	public static FailedUrlCache getInstance() {
		if (singletonInstance == null) {
			synchronized (FailedUrlCache.class) {
				singletonInstance = new FailedUrlCache();
			}
		}
		return singletonInstance;
	}

	public boolean contains(String url) {
		return mIgnore.contains(url);
	}

	public void add(String url) {
		mIgnore.add(url);
	}

	public void clearCache() {
		mIgnore.clear();
	}

}
