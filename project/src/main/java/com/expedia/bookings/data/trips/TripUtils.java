package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.trips.TripComponent.Type;
import com.mobiata.android.json.JSONUtils;

public class TripUtils {

	public static void putTripComponents(JSONObject obj, List<TripComponent> tripComponents) throws JSONException {
		JSONUtils.putJSONableList(obj, "tripComponents", tripComponents);
	}

	// We have to load trip components manually here; otherwise they are all loaded as
	// TripComponent instead of as the individual classes they are (TripFlight, TripHotel, etc)
	public static List<TripComponent> getTripComponents(JSONObject obj) {
		JSONArray tripComponents = obj.optJSONArray("tripComponents");
		List<TripComponent> components = new ArrayList<TripComponent>();
		if (tripComponents != null) {
			for (int a = 0; a < tripComponents.length(); a++) {
				JSONObject tripComponent = tripComponents.optJSONObject(a);
				Type type = JSONUtils.getEnum(tripComponent, "type", Type.class);
				Class<? extends TripComponent> clz;
				switch (type) {
				case ACTIVITY:
					clz = TripActivity.class;
					break;
				case CAR:
					clz = TripCar.class;
					break;
				case CRUISE:
					clz = TripCruise.class;
					break;
				case FLIGHT:
					clz = TripFlight.class;
					break;
				case HOTEL:
					clz = TripHotel.class;
					break;
				case PACKAGE:
					clz = TripPackage.class;
					break;
				case RAILS:
					clz = TripRails.class;
					break;
				default:
					clz = TripComponent.class;
					break;
				}

				components.add(JSONUtils.getJSONable(tripComponents, a, clz));
			}
		}

		return components;
	}
}
