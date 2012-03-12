package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewRating;
import com.expedia.bookings.data.ReviewsResponse;
import com.mobiata.android.Log;
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
				return reviewsResponse;
			}
			
			reviewsResponse.setTotalCount(response.getInt("TotalResults"));

			JSONArray reviews = response.getJSONArray("Results");
			int len = reviews.length();
			for (int a = 0; a < len; a++) {
				JSONObject reviewJson = reviews.getJSONObject(a);
				Review review = new Review();

				review.setReviewId(reviewJson.optString("Id", null));
				review.setTitle(reviewJson.optString("Title", null));
				review.setBody(reviewJson.optString("ReviewText", null));
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
					review.setReviewerName(reviewJson.getString("UserNickname"));
				}

				if (!reviewJson.isNull("UserLocation")) {
					review.setReviewerLocation(reviewJson.getString("UserLocation"));
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
		}

		return reviewsResponse;
	}

}
