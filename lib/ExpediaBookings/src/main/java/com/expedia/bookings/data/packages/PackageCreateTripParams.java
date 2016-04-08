package com.expedia.bookings.data.packages;

import java.util.List;

import com.expedia.bookings.utils.Strings;

public class PackageCreateTripParams {

	private String productKey;
	private String destinationId;
	private int numOfAdults;
	private int numOfInfantsInSeat;
	private List<Integer> childAges;

	public PackageCreateTripParams(String productKey, String destinationId,
		int numOfAdults, int numOfInfantsInSeat, List<Integer> childAges) {
		this.productKey = productKey;
		this.destinationId = destinationId;
		this.numOfAdults = numOfAdults;
		this.numOfInfantsInSeat = numOfInfantsInSeat;
		this.childAges = childAges;
	}

	public static PackageCreateTripParams fromPackageSearchParams(PackageSearchParams searchParams) {
		return new PackageCreateTripParams(searchParams.getPackagePIID(), searchParams.getDestination().gaiaId,
			searchParams.getAdults(), searchParams.getNumberOfSeatedChildren(), searchParams.getChildren());
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

	public boolean anyInfantsInSeat() {
		return numOfInfantsInSeat > 0;
	}

	public List<Integer> getChildAges() {
		return childAges;
	}
}
