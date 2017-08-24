package com.expedia.bookings.server;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.FlightRoutes;
import com.expedia.bookings.data.RoutesResponse;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.flightlib.data.Airport;

public class RoutesResponseHandler extends JsonResponseHandler<RoutesResponse> {

	private final Context mContext;

	public RoutesResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public RoutesResponse handleJson(JSONObject response) {
		RoutesResponse routesResponse = new RoutesResponse();

		ParserUtils.logActivityId(response);

		try {
			// Check for errors, return if found
			routesResponse.addErrors(ParserUtils.parseErrors(ApiMethod.FLIGHT_ROUTES, response));
			if (!routesResponse.isSuccess()) {
				return routesResponse;
			}

			FlightRoutes routes = new FlightRoutes();

			// Parse airports
			JSONArray airportsJson = response.optJSONArray("airports");
			if (airportsJson != null) {
				int len = airportsJson.length();
				for (int a = 0; a < len; a++) {
					JSONObject airportJson = airportsJson.optJSONObject(a);
					Airport airport = new Airport();
					airport.mAirportCode = airportJson.optString("airportCode");
					airport.mName = airportJson.optString("name");
					airport.mCountryCode = airportJson
						.optString("country"); // Note: this is using a somewhat overloaded var name
					routes.addAirport(airport);
				}
			}

			// Parse routes
			JSONArray routesJson = response.optJSONArray("routes");
			if (routesJson != null) {
				int len = routesJson.length();
				for (int a = 0; a < len; a++) {
					JSONObject routeJson = routesJson.optJSONObject(a);
					String origin = routeJson.optString("origin");
					List<String> destinations = JSONUtils.getStringList(routeJson, "destinations");
					routes.addRoutes(origin, destinations);
				}
			}

			routes.markCreationTime();

			routesResponse.setFlightRoutes(routes);
		}
		catch (JSONException e) {
			Log.e("Could not parse routes response", e);
			return null;
		}

		return routesResponse;
	}
}
