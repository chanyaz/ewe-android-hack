package com.expedia.bookings.test.tests.pageModels.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CommonCheckoutScreen extends ScreenActions {

	private static final int CHECKOUT_BUTTON_ID = R.id.menu_checkout;
	private static final int LOG_IN_BUTTON_ID = R.id.login_text_view;
	private static final int LOG_OUT_BUTTON_ID = R.id.account_logout_logout_button;
	private static final int BUY_WITH_GOOGLE_BUTTON_ID = R.id.wallet_button;
	private static final int ADD_TRAVELER_BUTTON_ID = R.id.traveler_info_btn;
	private static final int EMPTY_TRAVELER_BUTTON_ID = R.id.traveler_empty_text_view;
	private static final int SELECT_PAYMENT_BUTTON_ID = R.id.payment_info_btn;
	private static final int SLIDE_TO_PURCHASE_START_ID = R.id.slider_image;
	private static final int SLIDE_TO_PURCHASE_END_ID = R.id.destination_image;
	private static final int CALCULATING_TAXES_AND_FEES_ID = R.string.calculating_taxes_and_fees;
	private static final int I_ACCEPT_STRING_ID = R.string.I_Accept;
	private static final int ADD_TRAVELER_STRING_ID = R.string.add_traveler;
	private static final int ENTER_COUPON_TEXT_VIEW_ID = R.id.coupon_button;
	private static final int ENTER_COUPON_CODE_STRING_ID = R.string.enter_coupon_code;
	private static final int COUPON_EDIT_TEXT_ID = R.id.coupon_edit_text;
	private static final int RULES_RESTRICTIONS_TEXT_VIEW_ID = R.id.legal_information_text_view;

	private static final String TAG = "Common Checkout Screen";

	public CommonCheckoutScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View checkoutButton() {
		return getView(CHECKOUT_BUTTON_ID);
	}

	public View logInButton() {
		return getView(LOG_IN_BUTTON_ID);
	}

	public View logOutButton() {
		return getView(LOG_OUT_BUTTON_ID);
	}

	public View googleWalletButton() {
		return getView(BUY_WITH_GOOGLE_BUTTON_ID);
	}

	public View addTravelerButton() {
		return getView(ADD_TRAVELER_BUTTON_ID);
	}

	public View selectPaymentButton() {
		return getView(SELECT_PAYMENT_BUTTON_ID);
	}

	public View slideToPurchaseStartView() {
		return getView(SLIDE_TO_PURCHASE_START_ID);
	}

	public View slideToPurchaseEndView() {
		return getView(SLIDE_TO_PURCHASE_END_ID);
	}

	public String calculatingTaxesAndFees() {
		return getString(CALCULATING_TAXES_AND_FEES_ID);
	}

	public String acceptString() {
		return getString(I_ACCEPT_STRING_ID);
	}

	public String addTravelerString() {
		return getString(ADD_TRAVELER_STRING_ID);
	}

	public TextView enterCouponButton() {
		return (TextView) getView(ENTER_COUPON_TEXT_VIEW_ID);
	}

	public String enterCouponCode() {
		return getString(ENTER_COUPON_CODE_STRING_ID);
	}

	public EditText couponCodeEditText() {
		return (EditText) getView(COUPON_EDIT_TEXT_ID);
	}

	public TextView legalInfoTextView() {
		return (TextView) getView(RULES_RESTRICTIONS_TEXT_VIEW_ID);
	}

	public TextView flightsLegalTextView() {
		return (TextView) getView(R.id.legal_blurb);
	}

	public View emptyTravelerButton() {
		return getView(EMPTY_TRAVELER_BUTTON_ID);
	}

	// Object interaction

	public void clickCheckoutButton() {
		clickOnView(checkoutButton());
	}

	public void clickLogInButton() {
		clickOnView(logInButton());
	}

	public void clickLogOutButton() {
		clickOnView(logOutButton());
	}

	public void clickGoogleWalletButton() {
		clickOnView(googleWalletButton());
	}

	public void clickAddTravelerButton() {
		clickOnView(addTravelerButton());
	}

	public void clickTravelerDetailsButton() {
		clickOnView(emptyTravelerButton());
	}

	public void clickSelectPaymentButton() {
		clickOnView(selectPaymentButton());
	}

	public void clickOnAcceptString() {
		clickOnText(acceptString());
	}

	public void clickOnEnterCouponButton() {
		clickOnView(enterCouponButton());
	}

	public void clickOnLegalInfoButton() {
		clickOnView(legalInfoTextView());
	}

	public void clickAddTravelerString() {
		clickOnText(addTravelerString());
	}

	public void typeTextCouponEditText(String couponCode) {
		enterText(couponCodeEditText(), couponCode);
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
