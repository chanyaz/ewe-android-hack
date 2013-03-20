package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

public class HotelPaymentFlowState {
	SectionLocation mSectionLocation;
	SectionBillingInfo mSectionBillingInfo;

	private HotelPaymentFlowState(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Important note: Hotels checkout flow only requires a Location (only for postal code) for a set of POS. If we
		// require the postal code field, then we need to inflate and validate against its value.
		PointOfSale.RequiredPaymentFieldsHotels field = PointOfSale.getPointOfSale().getRequiredPaymentFieldsHotels();
		if (field == PointOfSale.RequiredPaymentFieldsHotels.POSTAL_CODE) {
			mSectionLocation = (SectionLocation) inflater.inflate(R.layout.section_hotel_edit_address, null);
			mSectionLocation.setLineOfBusiness(LineOfBusiness.HOTELS);
		}

		mSectionBillingInfo = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard, null);
	}

	public static HotelPaymentFlowState getInstance(Context context) {
		if (context == null) {
			return null;
		}
		return new HotelPaymentFlowState(context);
	}

	private void bind(BillingInfo billingInfo) {
		if (billingInfo.getLocation() == null) {
			billingInfo.setLocation(new Location());
		}
		if (mSectionLocation != null) {
			mSectionLocation.bind(billingInfo.getLocation());
		}
		mSectionBillingInfo.bind(billingInfo);
	}

	/**
	 * For hotels flow we do not need billing address information for all POS that are not US 
	 */
	public boolean hasValidBillingAddress(BillingInfo billingInfo) {
		bind(billingInfo);

		if (mSectionLocation == null) {
			// If sectionLocation is null that means we don't have a location to validate against because it is not
			// something that is needed and is thus valid.
			return true;
		}
		else {
			return mSectionLocation.hasValidInput();
		}
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mSectionBillingInfo.hasValidInput();
	}
}
