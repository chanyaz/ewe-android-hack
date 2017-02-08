package com.expedia.bookings.interfaces;

/**
 * Created by shasgupta on 2/8/17.
 */

public interface CheckoutInformationListener {
	void checkoutInformationIsValid();

	void checkoutInformationIsNotValid();

	void onBillingInfoChange();

	void onLogout();
}
