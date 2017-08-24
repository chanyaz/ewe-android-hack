package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class ReviewsResponse extends Response {

	private int mNumReviewsInResponse;
	private final List<Review> mReviews;

	public ReviewsResponse() {
		mReviews = new ArrayList<Review>();
	}

	public void setNumReviewsInResponse(int num) {
		mNumReviewsInResponse = num;
	}

	public int getNumReviewsInResponse() {
		return mNumReviewsInResponse;
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
