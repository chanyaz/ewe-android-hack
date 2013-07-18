package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewRating;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;
import com.mobiata.android.text.format.Time;
import com.mobiata.android.util.AndroidUtils;

public class ReviewsResponseHandler extends JsonResponseHandler<ReviewsResponse> {

	private static final int SDK_VERSION = AndroidUtils.getSdkVersion();

	public ReviewsResponseHandler(Context context) {
	}

	@Override
	public ReviewsResponse handleJson(JSONObject response) {
		ReviewsResponse reviewsResponse = new ReviewsResponse();

		try {
			if (response.getBoolean("HasErrors")) {
				ServerError error = new ServerError(ApiMethod.BAZAAR_REVIEWS);
				reviewsResponse.addError(error);
				return reviewsResponse;
			}

			reviewsResponse.setTotalCount(response.getInt("TotalResults"));

			JSONArray reviews = response.getJSONArray("Results");
			int len = reviews.length();
			for (int a = 0; a < len; a++) {
				JSONObject reviewJson = reviews.getJSONObject(a);
				Review review = new Review();

				review.setReviewId(reviewJson.optString("Id", null));

				if (!reviewJson.isNull("Title")) {
					review.setTitle(JSONUtils.optNormalizedString(reviewJson, "Title", null));
				}
				else {
					review.setTitle("");
				}

				if (!reviewJson.isNull("ReviewText")) {
					review.setBody(JSONUtils.optNormalizedString(reviewJson, "ReviewText", null));
				}
				else {
					review.setBody("");
				}

				// do not add review to list if it has no content
				if (review.getBody().equals("") && review.getTitle().equals("")) {
					continue;
				}

				review.setRecommended(reviewJson.optBoolean("IsRecommended"));

				Time submissionDate = new Time();
				String submissionDateStr = reviewJson.getString("SubmissionTime");
				if (SDK_VERSION <= 7 && submissionDateStr.length() == 25) {
					// #11403: Need to massage the data here to get Android to properly parse it
					submissionDateStr = submissionDateStr.substring(0, 19) + ".000" + submissionDateStr.substring(19);
				}
				submissionDate.parse3339(submissionDateStr);
				review.setSubmissionDate(submissionDate);

				if (!reviewJson.isNull("UserNickname")) {
					review.setReviewerName(JSONUtils.getNormalizedString(reviewJson, "UserNickname"));
				}

				if (!reviewJson.isNull("UserLocation")) {
					review.setReviewerLocation(JSONUtils.getNormalizedString(reviewJson, "UserLocation"));
				}

				Object ratingObj = reviewJson.opt("SecondaryRatings");
				if (ratingObj instanceof JSONObject) {
					JSONObject ratingJson = (JSONObject) ratingObj;
					ReviewRating rating = new ReviewRating();
					review.setRating(rating);
					ratingJson.getJSONObject("Service").optInt("Value");
					rating.setOverallSatisfaction(reviewJson.optInt("Rating"));
					rating.setQualityOfService(ratingJson.getJSONObject("Service").optInt("Value"));
					rating.setRoomComfort(ratingJson.getJSONObject("RoomComfort").optInt("Value"));
					rating.setRoomCleanliness(ratingJson.getJSONObject("RoomCleanliness").optInt("Value"));
					rating.setHotelCondition(ratingJson.getJSONObject("HotelCondition").optInt("Value"));
					// no more convenience of location or neighborhood satisfaction rating
				}

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
