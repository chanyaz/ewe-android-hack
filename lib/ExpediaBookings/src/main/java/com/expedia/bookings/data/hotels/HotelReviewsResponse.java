package com.expedia.bookings.data.hotels;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class HotelReviewsResponse {

	public ReviewDetails reviewDetails;

	public class ReviewDetails {
		public int startIndex;
		public int numberOfReviewsInThisPage;
		public ReviewSummaryCollection reviewSummaryCollection;
		public ReviewCollection reviewCollection;
	}

	public class ReviewSummaryCollection {
		public List<ReviewSummary> reviewSummary = new ArrayList<ReviewSummary>();
	}

	public class ReviewSummary {
		public String id;
		public String hotelId;
		public int totalReviewCnt;
		public float avgOverallRating;
		public float cleanliness;
		public float serviceAndStaff;
		public float hotelCondition;
		public float convenienceOfLocation;
		public float neighborhoodSatisfaction;
	}

	public class ReviewCollection {
		public List<Review> review = new ArrayList<Review>();
	}

	public class Review {
		public String hotelId;
		public String reviewId;
		public int ratingOverall;
		public String userDisplayName;
		public String title;
		public String reviewText;
		public boolean recommended;
		public String userLocation;
		public DateTime reviewSubmissionTime;
	}
}
