package com.expedia.bookings.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.expedia.bookings.data.ReviewSort;
import com.expedia.bookings.fragment.UserReviewsFragment.ReviewWrapper;

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

	public void putReviews(String propertyId, ReviewSort sort, List<ReviewWrapper> reviews) {
		mReviewsMap.put(getKey(propertyId, sort), reviews);
	}

	public List<ReviewWrapper> getReviews(String propertyId, ReviewSort sort) {
		return mReviewsMap.get(getKey(propertyId, sort));
	}

	public void clearCache() {
		mReviewsMap.clear();
	}

	private static String getKey(String propertyId, ReviewSort sort) {
		return propertyId + "_" + sort.name();
	}

}
