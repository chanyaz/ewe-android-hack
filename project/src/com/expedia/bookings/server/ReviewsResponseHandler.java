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
import com.mobiata.android.util.AndroidUtils;

public class ReviewsResponseHandler extends JsonResponseHandler<ReviewsResponse> {

	private Context mContext;

	private static final int SDK_VERSION = AndroidUtils.getSdkVersion();

	public ReviewsResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public ReviewsResponse handleJson(JSONObject response) {
		ReviewsResponse reviewsResponse = new ReviewsResponse();
		try {
			if (ParserUtils.parseServerErrors(mContext, null, response, reviewsResponse)) {
				return reviewsResponse;
			}

			JSONObject body = response.optJSONObject("body");

			reviewsResponse.setIndex(body.getInt("index"));

			JSONArray hotels = body.getJSONArray("hotels");
			if (hotels.length() == 0) {
				return reviewsResponse;
			}

			JSONArray reviews = hotels.getJSONObject(0).getJSONArray("reviews");
			int len = reviews.length();
			for (int a = 0; a < len; a++) {
				JSONObject reviewJson = reviews.getJSONObject(a);
				Review review = new Review();

				review.setReviewId(reviewJson.optString("ReviewID", null));
				review.setTitle(reviewJson.optString("Title", null));
				review.setBody(reviewJson.optString("Body", null));
				review.setRecommended(reviewJson.optBoolean("Recommended"));

				Time submissionDate = new Time();
				String submissionDateStr = reviewJson.getString("SubmissionDate");
				if (SDK_VERSION <= 7 && submissionDateStr.length() == 25) {
					// #11403: Need to massage the data here to get Android to properly parse it
					submissionDateStr = submissionDateStr.substring(0, 19) + ".000" + submissionDateStr.substring(19);
				}
				submissionDate.parse3339(submissionDateStr);
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
