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
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.HotelCrossSellUtils;
import com.mobiata.android.json.JSONUtils;

public class TripUtils {

	public static boolean customerHasTripsInNextTwoWeeks(@NotNull Collection<Trip> customerTrips,
		boolean includeSharedItins) {
		DateTime twoWeeksFromNow = DateTime.now().plusDays(14);
		return hasTripStartDateBeforeDateTime(customerTrips, twoWeeksFromNow, includeSharedItins);
	}

	public static boolean hasTripStartDateBeforeDateTime(Collection<Trip> trips, DateTime dateTime,
		boolean includeSharedItins) {
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
		if (trips.size() > 1) {
			List<Trip> sortedList = new ArrayList<>(trips);
			Collections.sort(sortedList, SORT_ASCENDING_ORDER_COMPARATOR);
			return sortedList;
		}
		else {
			return trips;
		}
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
		List<Trip> flightTrips = new ArrayList<>();
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

	public static String createUsersTripComponentTypeEventString(Collection<Trip> trips) {
		HashSet<String> usersEventNumberSet = new HashSet<>();
		String usersEventNumberString;
		if (trips != null && !trips.isEmpty()) {
			HashSet<TripComponent.Type> tripComponentTypesAvailable = getTripComponentTypesInUsersTrips(trips);
			for (TripComponent.Type type : tripComponentTypesAvailable) {
				String eventNumber = createTripComponentTypeEventHashMap().get(type);
				if (eventNumber != null) {
					usersEventNumberSet.add(eventNumber);
				}
			}
		}
		usersEventNumberString = TextUtils.join(",", usersEventNumberSet);
		return usersEventNumberString;
	}

	public static String createUsersProp75String(Collection<Trip> trips) {
		String usersProp75String = " ";
		ArrayList<String> usersProp75TripSet = new ArrayList<>();
		HashSet<TripComponent.Type> usersTripComponentTypes = getTripComponentTypesInUsersTrips(trips);
		if (usersTripComponentTypes.contains(TripComponent.Type.HOTEL)) {
			String activeTrip = getUsersActiveTrip(trips, TripComponent.Type.HOTEL);
			usersProp75TripSet.add(activeTrip);
		}
		if (usersTripComponentTypes.contains(TripComponent.Type.FLIGHT)) {
			String activeTrip = getUsersActiveTrip(trips, TripComponent.Type.FLIGHT);
			usersProp75TripSet.add(activeTrip);
		}
		if (usersTripComponentTypes.contains(TripComponent.Type.CAR)) {
			String activeTrip = getUsersActiveTrip(trips, TripComponent.Type.CAR);
			usersProp75TripSet.add(activeTrip);
		}
		if (usersTripComponentTypes.contains(TripComponent.Type.ACTIVITY)) {
			String activeTrip = getUsersActiveTrip(trips, TripComponent.Type.ACTIVITY);
			usersProp75TripSet.add(activeTrip);
		}
		if (usersTripComponentTypes.contains(TripComponent.Type.RAILS)) {
			String activeTrip = getUsersActiveTrip(trips, TripComponent.Type.RAILS);
			usersProp75TripSet.add(activeTrip);
		}
		if (usersTripComponentTypes.contains(TripComponent.Type.PACKAGE)) {
			String activeTrip = getUsersActiveTrip(trips, TripComponent.Type.PACKAGE);
			usersProp75TripSet.add(activeTrip);
		}

		usersProp75String = TextUtils.join("|", usersProp75TripSet);

		return usersProp75String;
	}

	private static HashSet<TripComponent.Type> getTripComponentTypesInUsersTrips(Collection<Trip> trips) {
		HashSet<TripComponent.Type> usersTripComponentTypeHashSet = new HashSet<>();
		for (Trip trip : trips) {
			if (!trip.isShared()) {
				for (TripComponent component : trip.getTripComponents()) {
					if (component != null) {
						usersTripComponentTypeHashSet.add(component.getType());
					}
				}
			}
		}
		return usersTripComponentTypeHashSet;
	}

	private static HashMap<TripComponent.Type, String> createTripComponentTypeEventHashMap() {
		HashMap<TripComponent.Type, String> tripComponentTypeEventHashMap = new HashMap<>();

		tripComponentTypeEventHashMap.put(TripComponent.Type.HOTEL, "event250");
		tripComponentTypeEventHashMap.put(TripComponent.Type.FLIGHT, "event251");
		tripComponentTypeEventHashMap.put(TripComponent.Type.CAR, "event252");
		tripComponentTypeEventHashMap.put(TripComponent.Type.ACTIVITY, "event253");
		tripComponentTypeEventHashMap.put(TripComponent.Type.RAILS, "event254");
		tripComponentTypeEventHashMap.put(TripComponent.Type.PACKAGE, "event255");

		return tripComponentTypeEventHashMap;
	}

	private static String setTripComponentTypeCode(TripComponent tripComponent) {
		String tripComponentType = tripComponent.getType().toString();

		switch (tripComponentType) {
		case "HOTEL":
			return "HOT";
		case "FLIGHT":
			return "AIR";
		case "ACTIVITY":
			return "LX";
		case "PACKAGE":
			return "PGK";
		case "RAILS":
			return "RAIL";
		case "CAR":
			return "CAR";
		default:
			return tripComponentType;
		}
	}

	public static String getUsersActiveTrip(Collection<Trip> trips, TripComponent.Type tripType) {
		List<Trip> usersTrips = new ArrayList<>();
		String activeTripString = "";
		for (Trip trip : trips) {
			if (trip.getEndDate().isAfterNow() && !trip.getTripComponents().isEmpty()) {
				TripComponent tripComponent = trip.getTripComponents().get(0);
				if (tripComponent.getType().equals(tripType)) {
					usersTrips.add(trip);
				}
			}
		}
		if (!usersTrips.isEmpty()) {
			List<Trip> sortedTrips = getTripsInStartTimeAscendingOrder(usersTrips);
			activeTripString = calculateActiveTripDatesFromNow(sortedTrips.get(0));
			return activeTripString;
		}
		else {
			return null;
		}
	}

	@VisibleForTesting
	private static String calculateActiveTripDatesFromNow(Trip trip) {
		DateTime now = DateTime.now();
		StringBuilder tripDataStringBuilder = new StringBuilder();
		DateTime tripStartDate = trip.getStartDate().withTimeAtStartOfDay();
		DateTime tripEndDate = trip.getEndDate().withTimeAtStartOfDay().plusDays(1); //plus 1 day to account for today

		int startDateDaysBetweenNow = Days.daysBetween(now, tripStartDate).getDays();
		if (startDateDaysBetweenNow >= 0) {
			startDateDaysBetweenNow += 1; //this accounts for today
		}
		int endDateDaysBetweenNow = Days.daysBetween(now, tripEndDate).getDays();

		String tripType = setTripComponentTypeCode(trip.getTripComponents().get(0));

		String tripDataString = tripDataStringBuilder.append(tripType)
			.append(":")
			.append(startDateDaysBetweenNow)
			.append(":")
			.append(endDateDaysBetweenNow)
			.toString();

		return tripDataString;
	}
	
}
