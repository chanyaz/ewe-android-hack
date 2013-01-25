package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

public class HotelPaymentFlowState {
	SectionLocation mSectionLocation;
	SectionBillingInfo mSectionBillingInfo;

	private HotelPaymentFlowState(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSectionLocation = (SectionLocation) inflater.inflate(R.layout.section_hotel_edit_address, null);
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
		mSectionLocation.bind(billingInfo.getLocation());
		mSectionBillingInfo.bind(billingInfo);
	}

	/**
	 * For hotels flow we do not need billing address information for all POS that are not US 
	 */
	public boolean hasValidBillingAddress(BillingInfo billingInfo) {
		bind(billingInfo);
		if (PointOfSale.getPointOfSale().getPointOfSaleId().equals(PointOfSaleId.UNITED_STATES)) {
			return mSectionLocation.hasValidInput();
		}
		else {
			return true;
		}
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mSectionBillingInfo.hasValidInput();
	}
}
