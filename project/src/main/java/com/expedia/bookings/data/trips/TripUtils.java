package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.trips.TripComponent.Type;
import com.mobiata.android.json.JSONUtils;

public class TripUtils {

	public static boolean customerHasTripsInNextTwoWeeks(@NotNull Collection<Trip> customerTrips, boolean includeSharedItins) {
		DateTime twoWeeksFromNow = DateTime.now().plusDays(14);
		return hasTripStartDateBeforeDateTime(customerTrips, twoWeeksFromNow, includeSharedItins);
	}

	public static boolean hasTripStartDateBeforeDateTime(Collection<Trip> trips, DateTime dateTime, boolean includeSharedItins) {
		List<Trip> tripsSortedDateTimeAscending = new ArrayList<>(trips);

		Collections.sort(tripsSortedDateTimeAscending, SORT_ASCENDING_ORDER_COMPARATOR);

		for (Trip trip : tripsSortedDateTimeAscending) {
			if (trip.getStartDate() != null) {
				boolean hasTripExpired = trip.hasExpired(0);
				boolean startDateBefore = trip.getStartDate().isBefore(dateTime);
				boolean startDateToday = trip.getStartDate().isEqual(dateTime);

				if (!hasTripExpired && startDateBefore || startDateToday) {
					if (!includeSharedItins && trip.isShared()) {
						continue; // don't add shared itins to the list
					}
					else {
						return true;
					}
				}
				else { // we don't have any more trips within the time range. Break out of loop
					break;
				}
			}
		}
		return false;
	}

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

	private static final Comparator<Trip> SORT_ASCENDING_ORDER_COMPARATOR = new Comparator<Trip>() {
		@Override
		public int compare(Trip o1, Trip o2) {

			if (o1.getStartDate() == null) {
				if (o1.getStartDate() == o2.getStartDate()) {
					return 0;
				}
				else {
					return 1;
				}
			}

			if (o1.getStartDate().isBefore(o2.getStartDate())) {
				return -1;
			}
			else if (o1.getStartDate().isEqual(o2.getStartDate())) {
				return 0;
			}
			else {
				return 1;
			}
		}
	};
}
