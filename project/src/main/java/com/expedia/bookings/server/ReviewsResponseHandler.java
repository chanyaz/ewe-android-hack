package com.expedia.bookings.server;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class ReviewsResponseHandler extends JsonResponseHandler<ReviewsResponse> {

	@Override
	public ReviewsResponse handleJson(JSONObject response) {
		ReviewsResponse reviewsResponse = new ReviewsResponse();

		try {
			JSONObject reviewDetailsJson = response.getJSONObject("reviewDetails");

			int numReviews = reviewDetailsJson.getInt("numberOfReviewsInThisPage");
			reviewsResponse.setNumReviewsInResponse(numReviews);

			JSONArray reviewsJsonArray = reviewDetailsJson.getJSONObject("reviewCollection").getJSONArray("review");
			int len = reviewsJsonArray.length();
			for (int a = 0; a < len; a++) {
				JSONObject reviewJson = reviewsJsonArray.getJSONObject(a);
				Review review = new Review();

				review.setReviewId(reviewJson.optString("externalLinkId"));
				review.setTitle(JSONUtils.optNormalizedString(reviewJson, "title", null));
				review.setBody(JSONUtils.optNormalizedString(reviewJson, "reviewText", null));

				// Do not add review to list if it has no content
				if (TextUtils.isEmpty(review.getBody()) && TextUtils.isEmpty(review.getTitle())) {
					continue;
				}

				String isRecommendedValue = reviewJson.optString("isRecommended", Review.IsRecommended.NONE.name());
				review.setIsRecommended(Review.IsRecommended.valueOf(isRecommendedValue));

				review.setOverrallSatisfaction(reviewJson.optInt("ratingOverall"));

				String submissionDateStr = reviewJson.getString("reviewSubmissionTime");
				review.setSubmissionDate(DateTime.parse(submissionDateStr));

				review.setReviewerName(JSONUtils.optNormalizedString(reviewJson, "userDisplayName", ""));
				review.setReviewerLocation(JSONUtils.optNormalizedString(reviewJson, "userLocation", ""));

				reviewsResponse.addReview(review);
			}
		}
		catch (JSONException e) {
			Log.d("Could not parse JSON reviews response.", e);
			ServerError error = new ServerError(ApiMethod.USER_REVIEWS);
			reviewsResponse.addError(error);
			return reviewsResponse;
		}
		return reviewsResponse;
	}

}
