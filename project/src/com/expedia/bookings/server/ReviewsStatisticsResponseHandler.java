package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.data.ServerError;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class ReviewsStatisticsResponseHandler extends JsonResponseHandler<ReviewsStatisticsResponse> {

	@Override
	public ReviewsStatisticsResponse handleJson(JSONObject response) {
		ReviewsStatisticsResponse statisticsResponse = new ReviewsStatisticsResponse();

		try {
			JSONObject collectionJson = response.getJSONObject("reviewSummaryCollection");
			JSONArray summaryJsonArray = collectionJson.getJSONArray("reviewSummary");
			JSONObject summaryJson = summaryJsonArray.getJSONObject(0);

			statisticsResponse.setTotalReviewCount(summaryJson.getInt("totalReviewCnt"));
			statisticsResponse.setAverageOverallRating(summaryJson.getDouble("avgOverallRating"));
			statisticsResponse.setPercentRecommended(summaryJson.getDouble("recommendedPercent"));
		}
		catch (JSONException e) {
			Log.e("Unable to parse reviews statistics", e);
			ServerError error = new ServerError(ServerError.ApiMethod.USER_REVIEWS);
			statisticsResponse.addError(error);
		}
		return statisticsResponse;
	}

}
