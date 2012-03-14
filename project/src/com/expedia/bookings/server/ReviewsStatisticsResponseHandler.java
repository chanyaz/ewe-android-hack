package com.expedia.bookings.server;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.mobiata.android.Log;
import com.mobiata.android.net.JsonResponseHandler;

public class ReviewsStatisticsResponseHandler extends JsonResponseHandler<ReviewsStatisticsResponse> {

	public ReviewsStatisticsResponseHandler(Context context) {
	}

	@Override
	public ReviewsStatisticsResponse handleJson(JSONObject response) {
		ReviewsStatisticsResponse reviewsStatisticsResponse = new ReviewsStatisticsResponse();

		try {
			if (response.getBoolean("HasErrors")) {
				return reviewsStatisticsResponse;
			}

			JSONObject products = response.getJSONObject("Includes").getJSONObject("Products");

			String key = (String) products.keys().next();

			JSONObject stats = products.getJSONObject(key).getJSONObject("ReviewStatistics");

			reviewsStatisticsResponse.setRecommendedCount(stats.getInt("RecommendedCount"));
			reviewsStatisticsResponse.setTotalReviewCount(stats.getInt("TotalReviewCount"));

		}
		catch (JSONException e) {
			Log.d("Could not parse JSON reviews statistics response.", e);
		}

		return reviewsStatisticsResponse;
	}

}
