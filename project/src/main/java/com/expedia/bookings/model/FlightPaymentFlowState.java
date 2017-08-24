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
import com.expedia.bookings.utils.Ui;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class FlightPaymentFlowState {
	final SectionLocation mBillingAddress;
	final SectionBillingInfo mCardInfo;

	private FlightPaymentFlowState(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		mBillingAddress = Ui.inflate(context, R.layout.old_flights_section_edit_address, null);
		mCardInfo = Ui.inflate(context, R.layout.section_flight_edit_creditcard, null);
		mBillingAddress.setLineOfBusiness(LineOfBusiness.FLIGHTS);
		mCardInfo.setLineOfBusiness(LineOfBusiness.FLIGHTS);
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

	public boolean hasValidBillingAddress(BillingInfo billingInfo) {
		bind(billingInfo);
		return !PointOfSale.getPointOfSale().requiresBillingAddressFlights() || mBillingAddress.performValidation();
	}

	public boolean hasValidCardInfo(BillingInfo billingInfo) {
		bind(billingInfo);
		return mCardInfo.performValidation();
	}

}
