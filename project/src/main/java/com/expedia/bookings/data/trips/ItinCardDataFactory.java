package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.expedia.bookings.data.trips.Trip.LevelOfDetail;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.itin.data.ItinCardDataHotel;

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
		LevelOfDetail lod = tc.getParentTrip().getLevelOfDetail();
		if (lod == LevelOfDetail.SUMMARY || lod == LevelOfDetail.SUMMARY_FALLBACK) {
			return generateFallbackCardData(tc);
		}
		else {
			Type type = tc.getType();
			switch (type) {
			case FLIGHT: {
				return generateFlightCardData((TripFlight) tc);
			}
			case HOTEL: {
				return generateHotelCardData((TripHotel) tc);
			}
			case CAR: {
				return generateCarCardData((TripCar) tc);
			}
			case ACTIVITY: {
				return generateActivityCardData((TripActivity) tc);
			}
			case PACKAGE: {
				return generatePackageCardData((TripPackage) tc);
			}
			case RAILS: {
				return generateRailsCardData((TripRails) tc);
			}
			default: {
				return generateGenericCardData(tc);
			}
			}
		}
	}

	private static List<ItinCardData> generateFlightCardData(TripFlight tc) {
		List<ItinCardData> retData = new ArrayList<ItinCardData>();

		if (tc.getFlightTrip() != null) {
			for (int i = 0; i < tc.getFlightTrip().getLegCount(); i++) {
				retData.add(new ItinCardDataFlight(tc, i));
			}
		}

		return retData;
	}

	private static List<ItinCardData> generateHotelCardData(TripHotel tc) {
		return Arrays.asList((ItinCardData) new ItinCardDataHotel(tc));
	}

	private static List<ItinCardData> generateActivityCardData(TripActivity tc) {
		return Arrays.asList((ItinCardData) new ItinCardDataActivity(tc));
	}

	private static List<ItinCardData> generateCarCardData(TripCar tc) {
		return Arrays.asList((ItinCardData) new ItinCardDataCar(tc));
	}

	// Dev note: this code probably isn't called right now because the loop that
	// calls generateCardData() unrolls the packages ahead of time.  This is kept
	// just in case we ever do need to unroll packages more manually.
	private static List<ItinCardData> generatePackageCardData(TripPackage tc) {
		List<ItinCardData> data = new ArrayList<ItinCardData>();
		for (TripComponent subComponent : tc.getTripComponents()) {
			List<ItinCardData> subData = generateCardData(subComponent);
			if (subData != null) {
				data.addAll(subData);
			}
		}
		return data;
	}

	private static List<ItinCardData> generateRailsCardData(TripRails tc) {
		return Arrays.asList((ItinCardData) new ItinCardDataRails(tc));
	}

	private static List<ItinCardData> generateGenericCardData(TripComponent tc) {
		return Arrays.asList((ItinCardData) new ItinCardData(tc));
	}

	private static List<ItinCardData> generateFallbackCardData(TripComponent tc) {
		return Arrays.asList((ItinCardData) new ItinCardDataFallback(tc));
	}
}
