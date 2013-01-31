package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.trips.TripComponent.Type;

/**
 * Factory for converting TripComponent objects to (Multiple) ItinCardData Objects
 * 
 * This is where we break TripComponents into multiple cards, and may need
 * to do some magic to determine what gets broken up and what stays together 
 * and under what conditions is it like this
 * 
 * e.g. a past hotel booking should be represented in one card, but a future hotel booking
 * may be represented by checkin and checkout.
 *
 */
public class ItinCardDataFactory {

	private ItinCardDataFactory() {
	}

	public static List<ItinCardData> generateCardData(TripComponent tc) {
		ArrayList<ItinCardData> retList = new ArrayList<ItinCardData>();

		if (tc.getType().equals(Type.FLIGHT)) {
			retList = generateFlightCardData((TripFlight) tc);
		}
		else if (tc.getType().equals(Type.HOTEL)) {
			retList = generateHotelCardData((TripHotel) tc);
		}
		else if (tc.getType().equals(Type.ACTIVITY)) {
			retList = generateActivityCardData((TripActivity) tc);
		}
		else if (tc.getType().equals(Type.CAR)) {
			retList = generateCarCardData((TripCar) tc);
		}
		else {
			retList = generateGenericCardData(tc);
		}

		return retList;
	}

	private static ArrayList<ItinCardData> generateFlightCardData(TripFlight tc) {
		ArrayList<ItinCardData> retData = new ArrayList<ItinCardData>();

		if (tc.getFlightTrip() != null) {
			for (int i = 0; i < tc.getFlightTrip().getLegCount(); i++) {
				retData.add(new ItinCardDataFlight(tc, i));
			}
		}

		return retData;
	}

	private static ArrayList<ItinCardData> generateHotelCardData(TripHotel tc) {
		ArrayList<ItinCardData> retData = new ArrayList<ItinCardData>();

		retData.add(new ItinCardDataHotel(tc));

		return retData;
	}

	private static ArrayList<ItinCardData> generateActivityCardData(TripActivity tc) {
		return null;
	}

	private static ArrayList<ItinCardData> generateCarCardData(TripCar tc) {
		return null;
	}

	private static ArrayList<ItinCardData> generateGenericCardData(TripComponent tc) {
		ArrayList<ItinCardData> retData = new ArrayList<ItinCardData>();
		retData.add(new ItinCardData(tc));
		return retData;
	}

}
