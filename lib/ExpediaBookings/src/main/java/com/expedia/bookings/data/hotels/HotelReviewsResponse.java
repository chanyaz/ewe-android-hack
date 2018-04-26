package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class HotelReviewsResponse {

	public ReviewDetails reviewDetails;

	public static class ReviewDetails {
		public ReviewSummaryCollection reviewSummaryCollection;
		public ReviewCollection reviewCollection;
	}

	public static class ReviewCollection {
		public List<Review> review = new ArrayList<>();
	}

	public static class Review {
		public String reviewId;
		public String hotelId;
		public int ratingOverall;
		public String userDisplayName;
		public String title;
		public String reviewText;
		public boolean recommended;
		public IsRecommended isRecommended;
		public String userLocation;
		public DateTime reviewSubmissionTime;
		public String contentLocale;

		public enum IsRecommended {
			YES,
			NO,
			NONE
		}
	}
}
