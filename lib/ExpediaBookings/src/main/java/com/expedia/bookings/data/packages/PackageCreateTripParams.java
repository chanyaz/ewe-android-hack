package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.utils.Strings;

public class PackageCreateTripParams {

	private final String productKey;
	private final String destinationId;
	private final int numOfAdults;
	private final boolean infantsInLap;
	private final List<Integer> childAges;
	public boolean flexEnabled;

	public PackageCreateTripParams(String productKey, String destinationId,
		int numOfAdults, boolean infantsInLap, List<Integer> childAges) {
		this.productKey = productKey;
		this.destinationId = destinationId;
		this.numOfAdults = numOfAdults;
		this.infantsInLap = infantsInLap;
		this.childAges = childAges;
	}

	public static PackageCreateTripParams fromPackageSearchParams(PackageSearchParams searchParams) {
		return new PackageCreateTripParams(searchParams.getPackagePIID(), getGaiaIdOrMultiCityString(searchParams.getDestination()),
			searchParams.getAdults(), searchParams.getInfantSeatingInLap(), searchParams.getChildren());
	}

	public boolean isValid() {
		return !Strings.isEmpty(productKey) && !Strings.isEmpty(destinationId) && numOfAdults > 0;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public String getProductKey() {
		return productKey;
	}

	public int getNumOfAdults() {
		return numOfAdults;
	}

	public boolean isInfantsInLap() {
		return infantsInLap;
	}

	public boolean isInfantsInSeat() {
		return !infantsInLap;
	}
	public List<Integer> getChildAges() {
		return childAges;
	}

	public static String getGaiaIdOrMultiCityString(SuggestionV4 destination) {
		String destinationId;
		if (destination.type.equals("POI") && destination.hierarchyInfo != null && destination.hierarchyInfo.airport != null) {
			destinationId = destination.hierarchyInfo.airport.multicity;
		}
		else {
			destinationId = destination.gaiaId;
		}
		return destinationId;
	}
}
