package com.expedia.bookings.data.packages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.utils.Strings;

public class PackageCreateTripParams {
	private String productKey;
	private String destinationId;
	private int numOfAdults;
	private int numOfInfantsInLap;
	private List<Integer> childAges;

	public PackageCreateTripParams(String productKey, String destinationId,
								   int numOfAdults, int numOfInfantsInLap, List<Integer> childAges) {
		this.productKey = productKey;
		this.destinationId = destinationId;
		this.numOfAdults = numOfAdults;
		this.numOfInfantsInLap = numOfInfantsInLap;
		this.childAges = childAges;
	}

	public static PackageCreateTripParams fromPackageSearchParams(PackageSearchParams searchParams) {
		return new PackageCreateTripParams(searchParams.getPackagePIID(), searchParams.getDestination().gaiaId,
			searchParams.getAdults(), 0, searchParams.getChildren());
	}

	public boolean isValid() {
		return !Strings.isEmpty(productKey) && !Strings.isEmpty(destinationId) && numOfAdults > 0;
	}

	@NotNull
	public Map<String, Object> toQueryMap() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("productKey", productKey);
		params.put("destinationId", destinationId);
		params.put("roomOccupants[0].numberOfAdultGuests", numOfAdults);
		if (numOfInfantsInLap > 0) {
			params.put("roomOccupants[0].infantsInSeat", numOfInfantsInLap);
		}
		if (childAges != null && childAges.size() > 0) {
			params.put("roomOccupants[0].childGuestAge", buildChildAgesString());
		}
		return params;
	}

	//TODO verify this is the right format. The api doc makes no sense
	private String buildChildAgesString() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < childAges.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(childAges.get(i));
		}
		return sb.toString();
	}
}
