package com.expedia.bookings.data;

import com.expedia.bookings.R;

public enum ReviewSort {
	NEWEST_REVIEW_FIRST,
	HIGHEST_RATING_FIRST,
	LOWEST_RATING_FIRST;

	public String getSortByApiParam() {
		switch (this) {
		case NEWEST_REVIEW_FIRST:
			return "DATEDESCWITHLANGBUCKETS";
		case HIGHEST_RATING_FIRST:
			return "RATINGDESC";
		case LOWEST_RATING_FIRST:
		default:
			return "RATINGASC";
		}
	}

	public boolean reviewPassesFilter(Review review) {
		switch (this) {
		case NEWEST_REVIEW_FIRST:
			return true;
		case HIGHEST_RATING_FIRST:
			return review.getOverallSatisfaction() > 2;
		case LOWEST_RATING_FIRST:
			return review.getOverallSatisfaction() < 3;
		}
		// should never get here, but this makes the method happy
		return true;
	}

	public int getNoReviewsMessageResId() {
		switch (this) {
		case NEWEST_REVIEW_FIRST:
			return R.string.user_review_no_recent_reviews;
		case HIGHEST_RATING_FIRST:
			return R.string.user_review_no_favorable_reviews;
		case LOWEST_RATING_FIRST:
		default:
			return R.string.user_review_no_critical_reviews;
		}
	}

}
