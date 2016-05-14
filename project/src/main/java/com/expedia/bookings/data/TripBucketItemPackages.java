package com.expedia.bookings.data;

import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.utils.CurrencyUtils;

public class TripBucketItemPackages extends TripBucketItem {

	public PackageCreateTripResponse mPackageTripResponse;

	public TripBucketItemPackages(PackageCreateTripResponse packageTripResponse) {
		mPackageTripResponse = packageTripResponse;
		if (mPackageTripResponse.getValidFormsOfPayment() != null) {
			for (ValidPayment payment : mPackageTripResponse.getValidFormsOfPayment()) {
				payment.setPaymentType(CurrencyUtils.parsePaymentType(payment.name));
			}
			addValidPayments(mPackageTripResponse.getValidFormsOfPayment());
		}
	}

	@Override
	public LineOfBusiness getLineOfBusiness() {
		return LineOfBusiness.PACKAGES;
	}
}
