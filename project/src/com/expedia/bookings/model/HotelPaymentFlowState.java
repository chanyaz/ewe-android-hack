package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

public class HotelPaymentFlowState {
	SectionLocation mSectionLocation;
	SectionBillingInfo mSectionBillingInfo;

	private HotelPaymentFlowState(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);

		mSectionLocation = (SectionLocation) inflater.inflate(R.layout.section_hotel_edit_address, null);
		mSectionBillingInfo = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard, null);
		mSectionLocation.setLineOfBusiness(LineOfBusiness.HOTELS);
		mSectionBillingInfo.setLineOfBusiness(LineOfBusiness.HOTELS);
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
		return mSectionLocation.hasValidInput();
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mSectionBillingInfo.hasValidInput();
	}

	public boolean hasAValidCardSelected(BillingInfo bi) {
		return bi.hasStoredCard() || hasValidCardInfo(bi) && hasValidBillingAddress(bi);
	}

}
