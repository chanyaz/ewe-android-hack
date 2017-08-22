package com.expedia.bookings.data.cars;

/* Example:
	locationType: "AIRPORT_SHUTTLE_TO_CAR_AND_COUNTER",
	locationDescription: "Boston (Logan Intl.)",
	airportInstructions: "Shuttle to counter and car",
	locationCode: "BOS"
*/

import com.expedia.bookings.utils.Strings;

public class CarLocation {
	public enum LocationType {
		COUNTER_AND_CAR_IN_AIRPORT_TERMINAL,
		COUNTER_IN_AIRPORT_TERMINAL_AND_SHUTTLE_TO_CAR,
		AIRPORT_SHUTTLE_TO_CAR_AND_COUNTER,
		AIRPORT_NO_COUNTER_OR_SHUTTLE_INFORMATION,
		NON_AIRPORT_ADDRESS,
		UNKNOWN,
	}

	public LocationType locationType;
	public String locationDescription;
	public String airportInstructions;
	public String locationCode;
	public Double latitude;
	public Double longitude;
	public String addressLine1;
	public String cityName;
	public String provinceStateName;
	public String countryCode;
	public String regionId;

	public String toAddress() {
		return addressLine1 + " " + cityName + " " + provinceStateName + " " + countryCode;
	}

	public String getAddressLine1() {
		return Strings.isNotEmpty(addressLine1) ? addressLine1 : locationDescription;
	}

	public String getAddressLine2() {
		return isAddressLine2Available() ? cityName + ", " + provinceStateName : "";
	}

	public boolean isAddressLine2Available() {
		return Strings.isNotEmpty(cityName) && Strings.isNotEmpty(provinceStateName);
	}
}
