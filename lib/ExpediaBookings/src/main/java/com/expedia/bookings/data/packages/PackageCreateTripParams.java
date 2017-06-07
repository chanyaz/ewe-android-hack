package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.utils.Strings;

public class PackageCreateTripParams {

	private String productKey;
	private String destinationId;
	private int numOfAdults;
	private boolean infantsInLap;
	private List<Integer> childAges;
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
		return new PackageCreateTripParams(searchParams.getPackagePIID(), searchParams.getDestination().gaiaId,
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
}
