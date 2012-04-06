package com.expedia.bookings.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.expedia.bookings.fragment.UserReviewsFragment.ReviewWrapper;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.mobiata.android.Log;

public class UserReviewsUtils {

	private ConcurrentHashMap<String, List<ReviewWrapper>> mReviewsMap;

	private UserReviewsUtils() {
		mReviewsMap = new ConcurrentHashMap<String, List<ReviewWrapper>>();
	}

	private static class UserReviewsUtilsHolder {
		private static final UserReviewsUtils INSTANCE = new UserReviewsUtils();
	}

	public static UserReviewsUtils getInstance() {
		return UserReviewsUtilsHolder.INSTANCE;
	}

	public void addReviews(String propertyId, ReviewSort sort, List<ReviewWrapper> reviews) {
		String key = propertyId + "_" + sort.toString();

		Log.d("bradley", "add: " + key + ", " + reviews.size() + " reviews");

		mReviewsMap.put(key, reviews);
	}

	public List<ReviewWrapper> getReviews(String propertyId, ReviewSort sort) {
		String key = propertyId + "_" + sort.toString();

		return mReviewsMap.get(key);
	}

	public void clearCache() {
		mReviewsMap.clear();
	}

}
