package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class ReviewsResponse extends Response {
	private int mIndex;

	private List<Review> mReviews;

	public ReviewsResponse() {
		mReviews = new ArrayList<Review>();
	}

	public void setIndex(int index) {
		mIndex = index;
	}

	public int getIndex() {
		return mIndex;
	}

	public void addReview(Review review) {
		mReviews.add(review);
	}

	public List<Review> getReviews() {
		return mReviews;
	}

	public int getReviewCount() {
		return mReviews.size();
	}
}
