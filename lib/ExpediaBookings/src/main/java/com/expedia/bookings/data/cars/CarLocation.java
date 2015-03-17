package com.expedia.bookings.data.cars;

/* Example:
	locationType: "AIRPORT_SHUTTLE_TO_CAR_AND_COUNTER",
	locationDescription: "Boston (Logan Intl.)",
	airportInstructions: "Shuttle to counter and car",
	locationCode: "BOS"
*/

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

}
