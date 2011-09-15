package com.expedia.bookings.data;

import com.mobiata.android.text.format.Time;

public class Review {
	private String mReviewId;
	private String mTitle;
	private String mBody;
	private boolean mRecommended;
	private Time mSubmissionDate;
	private String mReviewerName;
	private String mReviewerLocation;
	private ReviewRating mRating;

	public String getReviewId() {
		return mReviewId;
	}

	public void setReviewId(String reviewId) {
		this.mReviewId = reviewId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getBody() {
		return mBody;
	}

	public void setBody(String body) {
		this.mBody = body;
	}

	public boolean isRecommended() {
		return mRecommended;
	}

	public void setRecommended(boolean recommended) {
		this.mRecommended = recommended;
	}

	public Time getSubmissionDate() {
		return mSubmissionDate;
	}

	public void setSubmissionDate(Time submissionDate) {
		this.mSubmissionDate = submissionDate;
	}

	public String getReviewerName() {
		return mReviewerName;
	}

	public void setReviewerName(String reviewerName) {
		this.mReviewerName = reviewerName;
	}

	public String getReviewerLocation() {
		return mReviewerLocation;
	}

	public void setReviewerLocation(String reviewerLocation) {
		this.mReviewerLocation = reviewerLocation;
	}

	public ReviewRating getRating() {
		return mRating;
	}

	public void setRating(ReviewRating rating) {
		this.mRating = rating;
	}
}
