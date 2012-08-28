package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class PaymentFlowState {
	Context mContext;

	SectionLocation mBillingAddress;
	SectionBillingInfo mCardInfo;

	private PaymentFlowState(Context context) {
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBillingAddress = (SectionLocation) inflater.inflate(R.layout.section_edit_address, null);
		mCardInfo = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard, null);
	}

	public static PaymentFlowState getInstance(Context context) {
		return new PaymentFlowState(context);
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
