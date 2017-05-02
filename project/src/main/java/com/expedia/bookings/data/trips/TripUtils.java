package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.HotelCrossSellUtils;
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
				boolean hasTripStarted = DateTime.now().isAfter(trip.getStartDate().plusDays(1));
				boolean startDateBefore = trip.getStartDate().isBefore(dateTime);
				boolean startDateToday = trip.getStartDate().isEqual(dateTime);

				if (!hasTripStarted && startDateBefore || startDateToday) {
					if (!includeSharedItins && trip.isShared()) {
						continue; // don't add shared itins to the list
					}
					else {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static List<Trip> getTripsInStartTimeAscendingOrder(List<Trip> trips) {
		List<Trip> sortedList = new ArrayList<>(trips);
		Collections.sort(sortedList, SORT_ASCENDING_ORDER_COMPARATOR);
		return sortedList;
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
		public int compare(Trip trip1, Trip trip2) {

			if (trip1.getStartDate() == null) {
				if (trip1.getStartDate() == trip2.getStartDate()) {
					return 0;
				}
				else {
					return 1;
				}
			}

			if (trip1.getStartDate().isBefore(trip2.getStartDate())) {
				return -1;
			}
			else if (trip1.getStartDate().isEqual(trip2.getStartDate())) {
				return 0;
			}
			else {
				return 1;
			}
		}
	};

	public static List<Trip> getUpcomingAirAttachQualifiedFlightTrips(@NotNull Collection<Trip> trips) {
		List<Trip> flightTrips = new ArrayList<Trip>();
		for (Trip trip : trips) {
			boolean hasOneTripComponent = trip.getTripComponents().size() == 1;
			boolean isNotShared = !trip.isShared();
			if (hasOneTripComponent && isNotShared) {
				boolean isFlightTrip = trip.getTripComponents().get(0) instanceof TripFlight;
				if (isFlightTrip && trip.getAirAttach() != null && trip.getAirAttach().isAirAttachQualified()) {
					flightTrips.add(trip);
				}
			}
		}
		return flightTrips;
	}

	@Nullable
	public static Trip getUpcomingAirAttachQualifiedFlightTrip(Collection<Trip> trips) {
		List<Trip> allFlightTripswithAAQualified = getUpcomingAirAttachQualifiedFlightTrips(trips);
		if (allFlightTripswithAAQualified.size() > 0) {
			List<Trip> sortedRecentFlightTrips = getTripsInStartTimeAscendingOrder(allFlightTripswithAAQualified);
			return sortedRecentFlightTrips.get(0);
		}
		return null;
	}

	public static String getFlightTripDestinationCity(TripFlight trip) {
		if (trip != null) {
			FlightLeg firstFlightLeg = trip.getFlightTrip().getLeg(0);
			String cityName = firstFlightLeg.getSegment(firstFlightLeg.getSegmentCount() - 1).getDestinationWaypoint()
				.getAirport().mCity;
			return cityName;
		}
		return null;
	}

	public static HotelSearchParams getHotelSearchParamsForRecentFlightAirAttach(TripFlight tripFlight) {
		if (tripFlight != null) {
			FlightLeg firstFlightLeg = tripFlight.getFlightTrip().getLeg(0);
			FlightLeg secondFlightLeg = null;

			if (tripFlight.getFlightTrip().getLegCount() > 1) {
				secondFlightLeg = tripFlight.getFlightTrip().getLeg(1);
			}
			return HotelCrossSellUtils
				.generateHotelSearchParamsFromItinData(tripFlight, firstFlightLeg, secondFlightLeg);
		}
		return null;
	}

	//Below are methods used to populate Omniture data based on a User's Trip data

	public static String createUsersTripTypeEventString(Collection<Trip> trips) {
		HashSet<String> usersEventNumberSet = new HashSet<>();
		String usersEventNumberString;
		if (trips != null && !trips.isEmpty()) {
			HashSet<TripComponent.Type> tripTypesAvailable = getTripTypesInUsersTrips(trips);
			for (TripComponent.Type type : tripTypesAvailable) {
				String eventNumber = createTripTypeEventHashMap().get(type);
				if (eventNumber != null) {
					usersEventNumberSet.add(eventNumber);
				}
			}
		}
		usersEventNumberString = TextUtils.join(",", usersEventNumberSet);
		return usersEventNumberString;
	}

	private static HashSet<TripComponent.Type> getTripTypesInUsersTrips(Collection<Trip> trips) {
		HashSet<TripComponent.Type> usersTripTypeHashSet = new HashSet<>();
		for (Trip trip : trips) {
			if (!trip.isShared()) {
				for (TripComponent component : trip.getTripComponents()) {
					usersTripTypeHashSet.add(component.getType());
				}
			}
		}
		return usersTripTypeHashSet;
	}

	private static HashMap<TripComponent.Type, String> createTripTypeEventHashMap() {
		HashMap<TripComponent.Type, String> tripTypeEventHashMap = new HashMap<>();

		tripTypeEventHashMap.put(TripComponent.Type.HOTEL, "event250");
		tripTypeEventHashMap.put(TripComponent.Type.FLIGHT, "event251");
		tripTypeEventHashMap.put(TripComponent.Type.CAR, "event252");
		tripTypeEventHashMap.put(TripComponent.Type.ACTIVITY, "event253");
		tripTypeEventHashMap.put(TripComponent.Type.RAILS, "event254");
		tripTypeEventHashMap.put(TripComponent.Type.PACKAGE, "event255");

		return tripTypeEventHashMap;
	}

}
