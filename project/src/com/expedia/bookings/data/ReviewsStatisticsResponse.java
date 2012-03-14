package com.expedia.bookings.data;

public class ReviewsStatisticsResponse extends Response {

	private int totalReviewCount = - 1;
	private int recommendedCount = - 1;

	public ReviewsStatisticsResponse() {

	}

	public void setTotalReviewCount(int total) {
		totalReviewCount = total;
	}

	public void setRecommendedCount(int recommended) {
		recommendedCount = recommended;
	}

	public int getTotalReviewCount() {
		return totalReviewCount;
	}

	public int getRecommendedCount() {
		return recommendedCount;
	}

}
