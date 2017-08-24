package com.expedia.bookings.model;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.utils.Ui;

public class HotelPaymentFlowState {
	final SectionLocation mSectionLocation;
	final SectionBillingInfo mSectionBillingInfo;

	private HotelPaymentFlowState(Context context) {
		mSectionLocation = Ui.inflate(context, R.layout.section_hotel_edit_address, null);
		mSectionBillingInfo = Ui.inflate(context, R.layout.section_hotel_edit_creditcard, null);
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
	 * Looks at the billingInfo and determines whether or not it contains a valid, selected card of any type. A valid,
	 * selected card means that the card in the billingInfo (either stored or manual) has a valid credit card and a
	 * valid billing address
	 * @param billingInfo
	 * @return
	 */
	public boolean hasAValidCardSelected(BillingInfo billingInfo) {
		if (billingInfo == null) {
			return false;
		}
		if (billingInfo.hasStoredCard()) {
			return true;
		}
		return hasValidBillingAddress(billingInfo) && hasValidCardInfo(billingInfo);
	}

	/**
	 * For hotels flow we do not need billing address information for all POS that are not US
	 */
	public boolean hasValidBillingAddress(BillingInfo billingInfo) {
		bind(billingInfo);
		return mSectionLocation.performValidation();
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mSectionBillingInfo.performValidation();
	}
}
