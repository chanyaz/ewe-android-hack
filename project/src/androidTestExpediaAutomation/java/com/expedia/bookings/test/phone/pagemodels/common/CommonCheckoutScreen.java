package com.expedia.bookings.test.phone.pagemodels.common;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.swipeRight;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
public class CommonCheckoutScreen extends ScreenActions {
	private static final int CHECKOUT_BUTTON_ID = R.id.menu_checkout;
	private static final int LOG_IN_BUTTON_ID = R.id.login_text_view;
	private static final int LOG_OUT_BUTTON_ID = R.id.account_logout_logout_button;
	private static final int BUY_WITH_GOOGLE_BUTTON_ID = R.id.wallet_button;
	private static final int ADD_TRAVELER_BUTTON_ID = R.id.traveler_info_btn;
	private static final int EMPTY_TRAVELER_BUTTON_ID = R.id.traveler_empty_text_view;
	private static final int SELECT_PAYMENT_BUTTON_ID = R.id.payment_info_btn;
	private static final int SLIDE_TO_PURCHASE_START_ID = R.id.slide_to_purchase_widget;
	private static final int SLIDE_TO_PURCHASE_END_ID = R.id.destination_image;
	private static final int CALCULATING_TAXES_AND_FEES_ID = R.string.calculating_taxes_and_fees;
	private static final int I_ACCEPT_STRING_ID = R.string.I_Accept;
	private static final int ADD_TRAVELER_STRING_ID = R.string.Add_Traveler;
	private static final int ENTER_COUPON_TEXT_VIEW_ID = R.id.coupon_button;
	private static final int ENTER_COUPON_CODE_STRING_ID = R.string.enter_coupon_code;
	private static final int COUPON_EDIT_TEXT_ID = R.id.coupon_edit_text;
	private static final int RULES_RESTRICTIONS_TEXT_VIEW_ID = R.id.legal_information_text_view;
	private static final String TAG = "Common Checkout Screen";

	// Object access

	public static ViewInteraction checkoutButton() {
		return onView(withId(CHECKOUT_BUTTON_ID));
	}

	public static ViewInteraction logInButton() {
		return onView(withId(LOG_IN_BUTTON_ID));
	}

	public static ViewInteraction logOutButton() {
		return onView(withId(LOG_OUT_BUTTON_ID));
	}

	public static ViewInteraction googleWalletButton() {
		return onView(withId(BUY_WITH_GOOGLE_BUTTON_ID));
	}

	public static ViewInteraction addTravelerButton() {
		return onView(withText("Enter a new traveler"));
	}

	public static ViewInteraction selectPaymentButton() {
		return onView(withId(SELECT_PAYMENT_BUTTON_ID));
	}

	public static ViewInteraction slideToPurchaseStartView() {
		return onView(withId(R.id.slide_to_purchase_widget));
	}

	public static ViewInteraction slideToPurchaseEndView() {
		return onView(withId(SLIDE_TO_PURCHASE_END_ID));
	}

	public static ViewInteraction calculatingTaxesAndFees() {
		return onView(withText(CALCULATING_TAXES_AND_FEES_ID));
	}

	public static ViewInteraction acceptString() {
		return onView(withText(I_ACCEPT_STRING_ID));
	}

	public static ViewInteraction addTravelerString() {
		return onView(withText(ADD_TRAVELER_STRING_ID));
	}

	public static ViewInteraction enterCouponButton() {
		return onView(withId(ENTER_COUPON_TEXT_VIEW_ID));
	}

	public static ViewInteraction enterCouponCode() {
		return onView(withText(ENTER_COUPON_CODE_STRING_ID));
	}

	public static ViewInteraction couponCodeEditText() {
		return onView(withId(COUPON_EDIT_TEXT_ID));
	}

	public static ViewInteraction legalInfoTextView() {
		return onView(withId(RULES_RESTRICTIONS_TEXT_VIEW_ID));
	}

	public static ViewInteraction flightsLegalTextView() {
		return onView(withId(R.id.legal_blurb));
	}

	public static ViewInteraction emptyTravelerButton() {
		return onView(withId(EMPTY_TRAVELER_BUTTON_ID));
	}

	// Object interaction

	public static void clickCheckoutButton() {
		checkoutButton().perform(click());
	}

	public static void clickLogInButton() {
		logInButton().perform(click());
	}

	public static void clickLogOutButton() {
		logOutButton().perform(click());
	}

	public static void clickGoogleWalletButton() {
		googleWalletButton().perform(click());
	}

	public static void clickAddTravelerButton() {
		addTravelerButton().perform(click());
	}

	public static void clickTravelerDetailsButton() {
		emptyTravelerButton().perform(click());
	}

	public static void clickSelectPaymentButton() {
		selectPaymentButton().perform(scrollTo());
		selectPaymentButton().perform(click());
	}

	public static void clickOnAcceptString() {
		acceptString().perform(click());
	}

	public static void clickOnEnterCouponButton() {
		enterCouponButton().perform(click());
	}

	public static void clickOnLegalInfoButton() {
		legalInfoTextView().perform(click());
	}

	public static void clickAddTravelerString() {
		addTravelerString().perform(click());
	}

	public static void typeTextCouponEditText(String couponCode) {
		couponCodeEditText().perform(typeText(couponCode), closeSoftKeyboard());
	}

	public static void slideToCheckout() {
		slideToPurchaseStartView().perform(swipeRight());
	}

	public static void clickNewPaymentCard() {
		onView(withId(R.id.new_payment_new_card)).perform(click());
	}

	public static void clickIAcceptButton() {
		onView(withId(R.id.layout_i_accept)).perform(click());
	}

	public static void clickEnterInfoButton() {
		onView(withId(R.id.enter_info_manually_button)).perform(click());
	}

	public static void clickTravelerDetails() {
		try {
			onView(withId(R.id.traveler_empty_text_view)).perform(click());
		}
		catch (Exception e) {
			onView(withText("Traveler details")).perform(scrollTo());
			onView(withText("Traveler details")).perform(click());
		}
	}

	public static void clickGuestDetails() {
		try {
			onView(withId(R.id.traveler_info_btn)).perform(click());
		}
		catch (Exception e) {
			onView(withText("Guest details")).perform(scrollTo());
			onView(withText("Guest details")).perform(click());
		}
	}
}
