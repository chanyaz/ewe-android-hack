package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewRating;
import com.expedia.bookings.data.ReviewsResponse;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;
import com.mobiata.android.text.format.Time;

public class ReviewsResponseHandler extends JsonResponseHandler<ReviewsResponse> {

	private Context mContext;

	public ReviewsResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public ReviewsResponse handleJson(JSONObject response) {
		ReviewsResponse reviewsResponse = new ReviewsResponse();
		try {
			if (ParserUtils.parseServerErrors(mContext, response, reviewsResponse)) {
				return reviewsResponse;
			}

			reviewsResponse.setIndex(response.getInt("index"));

			JSONArray reviews = response.getJSONArray("hotels").getJSONObject(0).getJSONArray("reviews");
			int len = reviews.length();
			for (int a = 0; a < len; a++) {
				JSONObject reviewJson = reviews.getJSONObject(a);
				Review review = new Review();

				review.setReviewId(reviewJson.optString("ReviewID", null));
				review.setTitle(reviewJson.optString("Title", null));
				review.setBody(reviewJson.optString("Body", null));
				review.setRecommended(reviewJson.optBoolean("Recommended"));

				Time submissionDate = new Time();
				submissionDate.parse3339(reviewJson.getString("SubmissionDate"));
				review.setSubmissionDate(submissionDate);

				JSONObject reviewerJson = reviewJson.getJSONObject("ReviewerDetails");
				if (reviewerJson.optBoolean("DisplayName")) {
					String firstName = reviewerJson.optString("FirstName", null);
					String lastName = reviewerJson.optString("LastName", null);
					if (firstName != null && lastName != null) {
						review.setReviewerName(mContext.getString(R.string.name_template, firstName, lastName));
					}
					else if (firstName != null) {
						review.setReviewerName(firstName);
					}
					else if (lastName != null) {
						review.setReviewerName(lastName);
					}
				}

				review.setReviewerLocation(reviewerJson.optString("Location", null));

				Object ratingObj = reviewJson.opt("HotelReviewRatings");
				if (ratingObj instanceof JSONObject) {
					JSONObject ratingJson = (JSONObject) ratingObj;
					ReviewRating rating = new ReviewRating();
					review.setRating(rating);
					rating.setConvenienceOfLocation(ratingJson.optInt("ConvenienceOfLocation"));
					rating.setHotelCondition(ratingJson.optInt("HotelCondition"));
					rating.setQualityOfService(ratingJson.optInt("QualityOfService"));
					rating.setRoomCleanliness(ratingJson.optInt("RoomCleanliness"));
					rating.setRoomComfort(ratingJson.optInt("RoomComfort"));
					rating.setOverallSatisfaction(ratingJson.optInt("OverallSatisfaction"));
					rating.setNeighborhoodSatisfaction(ratingJson.optInt("NeighborhoodSatisfaction"));
				}

				reviewsResponse.addReview(review);
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON reviews response.", e);
			return null;
		}

		return reviewsResponse;
	}
}
