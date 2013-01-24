package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

public class HotelPaymentFlowState {
	SectionLocation mBillingAddress;
	SectionBillingInfo mCardInfo;

	private HotelPaymentFlowState(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBillingAddress = (SectionLocation) inflater.inflate(R.layout.section_hotel_edit_address, null);
		mCardInfo = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard, null);
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
		mBillingAddress.bind(billingInfo.getLocation());
		mCardInfo.bind(billingInfo);
	}

	public boolean hasValidBillingAddress(BillingInfo billingInfo) {
		bind(billingInfo);
		return mBillingAddress.hasValidInput();
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mCardInfo.hasValidInput();
	}

	public boolean allBillingInfoIsValid(BillingInfo billingInfo) {
		bind(billingInfo);
		return mBillingAddress.hasValidInput()
				&& mCardInfo.hasValidInput();
	}

}
