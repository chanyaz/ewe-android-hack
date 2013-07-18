package com.expedia.bookings.data;

public class ReviewsStatisticsResponse extends Response {

	private int mTotalReviewCount;
	private float mPercentRecommended;
	private float mAverageOverallRating;

	public int getTotalReviewCount() {
		return mTotalReviewCount;
	}

	public void setTotalReviewCount(int totalReviewCount) {
		mTotalReviewCount = totalReviewCount;
	}

	public float getPercentRecommended() {
		return mPercentRecommended;
	}

	public int getTotalRecommended() {
		return Math.round(mPercentRecommended / 100.0f * mTotalReviewCount);
	}

	public void setPercentRecommended(double percentRecommended) {
		mPercentRecommended = (float) percentRecommended;
	}

	public float getAverageOverallRating() {
		return mAverageOverallRating;
	}

	public void setAverageOverallRating(double averageOverallRating) {
		mAverageOverallRating = (float) averageOverallRating;
	}
}
