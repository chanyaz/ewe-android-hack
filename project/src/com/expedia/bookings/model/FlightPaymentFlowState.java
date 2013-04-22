package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class FlightPaymentFlowState {
	SectionLocation mBillingAddress;
	SectionBillingInfo mCardInfo;

	private FlightPaymentFlowState(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBillingAddress = (SectionLocation) inflater.inflate(R.layout.section_edit_address, null);
		mCardInfo = (SectionBillingInfo) inflater.inflate(R.layout.section_edit_creditcard, null);
	}

	public static FlightPaymentFlowState getInstance(Context context) {
		if (context == null) {
			return null;
		}
		return new FlightPaymentFlowState(context);
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
		return !PointOfSale.getPointOfSale().requiresBillingAddressFlights() || mBillingAddress.hasValidInput();
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mCardInfo.hasValidInput();
	}

	public boolean allBillingInfoIsValid(BillingInfo billingInfo) {
		bind(billingInfo);
		return mBillingAddress.hasValidInput() && mCardInfo.hasValidInput();
	}

}
