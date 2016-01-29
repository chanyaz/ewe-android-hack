package com.expedia.bookings.data;

import org.joda.time.DateTime;

public class Review {
	private String mReviewId;
	private String mTitle;
	private String mBody;
	private IsRecommended mIsRecommended;
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

	public IsRecommended isRecommended() {
		return mIsRecommended;
	}

	public void setIsRecommended(IsRecommended mIsRecommended) {
		this.mIsRecommended = mIsRecommended;
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

	public enum IsRecommended {
		YES,
		NO,
		NONE
	}
}
