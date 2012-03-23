package com.expedia.bookings.data;

public class ReviewsStatisticsResponse extends Response {

	private int totalReviewCount = -1;
	private int recommendedCount = -1;
	private float averageOverallRating = -1;

	public ReviewsStatisticsResponse() {
	}

	public void setTotalReviewCount(int total) {
		totalReviewCount = total;
	}

	public void setRecommendedCount(int recommended) {
		recommendedCount = recommended;
	}

	public void setAverageOverallRating(float avg) {
		averageOverallRating = avg;
	}

	public int getTotalReviewCount() {
		return totalReviewCount;
	}

	public int getRecommendedCount() {
		return recommendedCount;
	}

	public float getAverageOverallRating() {
		return averageOverallRating;
	}
}
