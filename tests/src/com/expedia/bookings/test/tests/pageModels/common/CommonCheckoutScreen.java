package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CommonCheckoutScreen extends ScreenActions {

	private static int sCheckoutButtonID = R.id.menu_checkout;
	private static int sLogInButtonID = R.id.login_text_view;
	private static int sBuyWithGoogleButtonID = R.id.wallet_button;
	private static int sAddTravelerButtonID = R.id.traveler_info_btn;
	private static int sSelectPaymentButtonID = R.id.payment_info_btn;
	private static int sSlideToPurchaseStartViewID = R.id.slider_image;
	private static int sSlideToPurchaseEndViewID = R.id.destination_image;

	private static final String TAG = "Common Checkout Screen";

	public CommonCheckoutScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View checkoutButton() {
		return getView(sCheckoutButtonID);
	}

	public View logInButton() {
		return getView(sLogInButtonID);
	}

	public View googleWalletButton() {
		return getView(sBuyWithGoogleButtonID);
	}

	public View addTravelerButton() {
		return getView(sAddTravelerButtonID);
	}

	public View selectPaymentButton() {
		return getView(sSelectPaymentButtonID);
	}

	public View slideToPurchaseStartView() {
		return getView(sSlideToPurchaseStartViewID);
	}

	public View slideToPurchaseEndView() {
		return getView(sSlideToPurchaseEndViewID);
	}

	// Object interaction

	public void clickCheckoutButton() {
		clickOnView(checkoutButton());
	}

	public void clickLogInButton() {
		clickOnView(logInButton());
	}

	public void clickGoogleWalletButton() {
		clickOnView(googleWalletButton());
	}

	public void clickAddTravelerButton() {
		clickOnView(addTravelerButton());
	}

	public void clickSelectPaymentButton() {
		clickOnView(selectPaymentButton());
	}

	public void slideToCheckout() {
		int[] startLocation = new int[2];
		slideToPurchaseStartView().getLocationOnScreen(startLocation);

		int[] endLocation = new int[2];
		slideToPurchaseEndView().getLocationOnScreen(endLocation);

		enterLog(TAG, "Booking: Slide X from: " + startLocation[0] + " to " + endLocation[0] + ".");
		enterLog(TAG, "Booking: Slide Y from: " + startLocation[1] + " to " + endLocation[1] + ".");
		delay();

		drag(startLocation[0], mRes.getDisplayMetrics().widthPixels - 5, startLocation[1] + 50, endLocation[1] + 50, 10);
	}

}
