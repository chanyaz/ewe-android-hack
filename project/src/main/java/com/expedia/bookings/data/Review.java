package com.expedia.bookings.data;

import org.joda.time.DateTime;

public class Review {
	private String mReviewId;
	private String mTitle;
	private String mBody;
	private boolean mRecommended;
	private DateTime mSubmissionDate;
	private String mReviewerName;
	private String mReviewerLocation;
	private int mOverallSatisfaction;

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

	public DateTime getSubmissionDate() {
		return mSubmissionDate;
	}

	public void setSubmissionDate(DateTime submissionDate) {
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

	public int getOverallSatisfaction() {
		return mOverallSatisfaction;
	}

	public void setOverrallSatisfaction(int overallSatisfaction) {
		mOverallSatisfaction = overallSatisfaction;
	}

}
