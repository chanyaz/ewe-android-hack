package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

public class ReviewsResponse extends Response {
	private int mIndex;

	private List<Review> mReviews;
	
	private int mTotalCount;

	public ReviewsResponse() {
		mReviews = new ArrayList<Review>();
	}

	public void setIndex(int index) {
		mIndex = index;
	}
	
	public void setTotalCount(int totalCount) {
		mTotalCount = totalCount;
	}
	
	public int getTotalCount() {
		return mTotalCount;
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
