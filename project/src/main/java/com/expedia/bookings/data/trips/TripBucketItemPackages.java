package com.expedia.bookings.data.trips;

import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;

public class TripBucketItemPackages extends TripBucketItem {

	public final PackageCreateTripResponse mPackageTripResponse;

	public TripBucketItemPackages(PackageCreateTripResponse packageTripResponse) {
		mPackageTripResponse = packageTripResponse;
		if (mPackageTripResponse.getValidFormsOfPayment() != null) {
			addValidPaymentsV2(mPackageTripResponse.getValidFormsOfPayment());
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.PACKAGES;
	}
}
