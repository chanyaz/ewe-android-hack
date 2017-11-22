package com.expedia.bookings.data.packages;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.utils.Strings;

public class PackageCreateTripParams {

	private String productKey;
	private String destinationId;
	/*private int numOfAdults;
	private boolean infantsInLap;
	private List<Integer> childAges;*/
	public boolean flexEnabled;
	private List<PackageCreateTripRoomParam> packageCreateTripRoomParams;


	public PackageCreateTripParams(String productKey, String destinationId, List<PackageCreateTripRoomParam> packageCreateTripRoomParams
		/*int numOfAdults, boolean infantsInLap, List<Integer> childAges*/) {
		this.productKey = productKey;
		this.destinationId = destinationId;
		this.packageCreateTripRoomParams = packageCreateTripRoomParams;
		/*this.numOfAdults = numOfAdults;
		this.infantsInLap = infantsInLap;
		this.childAges = childAges;*/
	}

	public static PackageCreateTripParams fromPackageSearchParams(PackageSearchParams searchParams) {
		List<PackageCreateTripRoomParam> packageCreateTripRoomParams = new ArrayList<>();
		for (Integer i = 0; i < searchParams.getAdultsList().size(); i++) {
			int numberOfAdults = searchParams.getAdultsList().get(i);
			if (numberOfAdults > 0) {
				PackageCreateTripRoomParam param = new PackageCreateTripRoomParam(searchParams.getAdultsList().get(i),
					searchParams.getInfantSeatingInLap(), searchParams.getChildrenList().get(i));
				packageCreateTripRoomParams.add(param);
			}
		}
		return new PackageCreateTripParams(searchParams.getPackagePIID(),
			getGaiaIdOrMultiCityString(searchParams.getDestination()), packageCreateTripRoomParams);
		/*return new PackageCreateTripParams(searchParams.getPackagePIID(),
			getGaiaIdOrMultiCityString(searchParams.getDestination()),
			searchParams.getAdults(), searchParams.getInfantSeatingInLap(), searchParams.getChildren());
		*/
	}

	public boolean isValid() {
		return !Strings.isEmpty(productKey) && !Strings.isEmpty(destinationId) && packageCreateTripRoomParams.size() > 0;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public String getProductKey() {
		return productKey;
	}

	public List<PackageCreateTripRoomParam> occupants() {
		return packageCreateTripRoomParams;
	}

	public static String getGaiaIdOrMultiCityString(SuggestionV4 destination) {
		String destinationId;
		if (destination.type != null && destination.type.equals("POI") && destination.hierarchyInfo != null && destination.hierarchyInfo.airport != null) {
			destinationId = destination.hierarchyInfo.airport.multicity;
		}
		else {
			destinationId = destination.gaiaId != null ? destination.gaiaId : destination.hierarchyInfo.airport.regionId;
		}
		return destinationId;
	}
}
