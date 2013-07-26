package com.expedia.bookings.server;

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
import com.mobiata.android.net.JsonResponseHandler;
import com.mobiata.android.text.format.Time;

public class ReviewsResponseHandler extends JsonResponseHandler<ReviewsResponse> {

	@Override
	public ReviewsResponse handleJson(JSONObject response) {
		ReviewsResponse reviewsResponse = new ReviewsResponse();

		try {
			JSONObject reviewDetailsJson = response.getJSONObject("reviewDetails");

			int totalNumReviews = reviewDetailsJson.getJSONObject("reviewSummaryCollection")
					.getJSONArray("reviewSummary").getJSONObject(0).getInt("totalReviewCnt");
			reviewsResponse.setTotalCount(totalNumReviews);

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

				review.setRecommended(reviewJson.optBoolean("recommended"));
				review.setOverrallSatisfaction(reviewJson.optInt("ratingOverall"));

				Time submissionDate = new Time();
				String submissionDateStr = reviewJson.getString("reviewSubmissionTime");
				submissionDate.parse3339(submissionDateStr);
				review.setSubmissionDate(submissionDate);

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
