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

import com.expedia.bookings.data.DeprecatedHotelSearchParams;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.HotelCrossSellUtils;
import com.expedia.bookings.utils.Strings;
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
					if (includeSharedItins || !trip.isShared()) {
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

	public static List<TripComponent> getTripsComponentsInStartTimeAscendingOrder(List<TripComponent> trips) {
		if (trips.size() > 1) {
			List<TripComponent> sortedList = new ArrayList<>(trips);
			Collections.sort(sortedList, SORT_ASCENDING_ORDER_COMPARATOR_COMPONENT);
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

	private static final Comparator<TripComponent> SORT_ASCENDING_ORDER_COMPARATOR_COMPONENT = new Comparator<TripComponent>() {
		@Override
		public int compare(TripComponent trip1, TripComponent trip2) {

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
		if (trip != null && trip.getFlightTrip() != null && !trip.getFlightTrip().getLegs().isEmpty()) {
			FlightLeg firstFlightLeg = trip.getFlightTrip().getLeg(0);
			String cityName = firstFlightLeg.getSegment(firstFlightLeg.getSegmentCount() - 1).getDestinationWaypoint()
				.getAirport().mCity;
			return (cityName == null) ? "" : cityName;
		}
		return "";
	}

	public static DeprecatedHotelSearchParams getHotelSearchParamsForRecentFlightAirAttach(TripFlight tripFlight) {
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

	//For prop 75 the order of the LOBs is important for tracking data

	public static String createUsersProp75String(Collection<Trip> trips) {
		String usersProp75String = " ";
		ArrayList<String> usersProp75TripSet = new ArrayList<>();
		HashSet<TripComponent.Type> usersTripComponentTypes = getTripComponentTypesInUsersTrips(trips);
		List<TripComponent.Type> tripComponentOrder = tripComponentOrder();
		for (TripComponent.Type type : tripComponentOrder) {
			if (usersTripComponentTypes.contains(type)) {
				String activeTrip = getUsersActiveTrip(trips, type);
				addActiveTrip(usersProp75TripSet, activeTrip);
			}
		}
		usersProp75String = TextUtils.join("|", usersProp75TripSet);

		return usersProp75String;
	}

	private static List<TripComponent.Type> tripComponentOrder() {
		List tripComponentOrder = new ArrayList<>();
		tripComponentOrder.add(TripComponent.Type.HOTEL);
		tripComponentOrder.add(TripComponent.Type.FLIGHT);
		tripComponentOrder.add(TripComponent.Type.CAR);
		tripComponentOrder.add(TripComponent.Type.ACTIVITY);
		tripComponentOrder.add(TripComponent.Type.RAILS);
		return tripComponentOrder;
	}

	private static void addActiveTrip(ArrayList<String> usersProp75TripSet, String activeTrip) {
		if (!Strings.isEmpty(activeTrip)) {
			usersProp75TripSet.add(activeTrip);
		}
	}

	private static HashSet<TripComponent.Type> getTripComponentTypesInUsersTrips(Collection<Trip> trips) {
		HashSet<TripComponent.Type> usersTripComponentTypeHashSet = new HashSet<>();
		for (Trip trip : trips) {
			if (!trip.isShared()) {
				for (TripComponent component : trip.getTripComponents()) {
					if (component != null) {
						if (component.getType() == TripComponent.Type.PACKAGE) {
							for (TripComponent packageComponents : trip.getTripComponents(true)) {
								usersTripComponentTypeHashSet.add(packageComponents.getType());
							}
						}
						else {
							usersTripComponentTypeHashSet.add(component.getType());
						}
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
		case "RAILS":
			return "RAIL";
		case "CAR":
			return "CAR";
		default:
			return tripComponentType;
		}
	}

	public static String getUsersActiveTrip(Collection<Trip> trips, TripComponent.Type tripType) {
		List<TripComponent> usersTripComponents = new ArrayList<>();
		String activeTripComponentString = "";
		for (Trip trip : trips) {
			if (trip.getEndDate() != null && trip.getEndDate().plusDays(1).isAfterNow() && !trip.getTripComponents()
				.isEmpty()) {
				for (TripComponent tripComp : trip.getTripComponents()) {
					if (tripComp.getType() == TripComponent.Type.PACKAGE) {
						TripPackage pack = (TripPackage) tripComp;
						for (TripComponent packComp : pack.getTripComponents()) {
							if (packComp.getType().equals(tripType)) {
								usersTripComponents.add(packComp);
							}

						}
					}
					else if (tripComp.getType().equals(tripType)) {
						usersTripComponents.add(tripComp);
					}
				}

				if (!usersTripComponents.isEmpty()) {
					List<TripComponent> sortedTrips = getTripsComponentsInStartTimeAscendingOrder(usersTripComponents);
					activeTripComponentString = calculateActiveTripDatesFromNow(sortedTrips.get(0));
				}
			}
		}
		return activeTripComponentString;
	}

	@VisibleForTesting
	private static String calculateActiveTripDatesFromNow(TripComponent trip) {
		DateTime now = DateTime.now();
		StringBuilder tripDataStringBuilder = new StringBuilder();
		DateTime tripStartDate = trip.getStartDate().withTimeAtStartOfDay();
		DateTime tripEndDate = trip.getEndDate().withTimeAtStartOfDay().plusDays(1); //plus 1 day to account for today

		int startDateDaysBetweenNow = Days.daysBetween(now, tripStartDate).getDays();
		if (startDateDaysBetweenNow >= 1) {
			startDateDaysBetweenNow += 1; //this accounts for today
		}
		int endDateDaysBetweenNow = Days.daysBetween(now, tripEndDate).getDays();

		String tripType = setTripComponentTypeCode(trip);

		String tripDataString = tripDataStringBuilder.append(tripType)
			.append(":")
			.append(startDateDaysBetweenNow)
			.append(":")
			.append(endDateDaysBetweenNow)
			.toString();

		return tripDataString;
	}

}
