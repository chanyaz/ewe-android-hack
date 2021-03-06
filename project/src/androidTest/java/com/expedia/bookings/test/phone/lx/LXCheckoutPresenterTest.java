package com.expedia.bookings.test.phone.lx;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.utils.LXDataUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.not;

public class LXCheckoutPresenterTest extends LxTestCase {
	private void goToCheckout() throws Throwable {
		LXScreen.location().perform(typeText("San"));
		LXScreen.selectLocation("San Francisco, CA");
		LXScreen.selectDateButton().perform(click());
		LXScreen.selectDates(LocalDate.now(), null);
		LXScreen.searchButton().perform(click());

		LXScreen.waitForSearchListDisplayed();
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		LXScreen.waitForLoadingDetailsNotDisplayed();

		final String ticketName = "2-Day";
		LXInfositeScreen.selectOffer("2-Day New York Pass").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(waitFor(isDisplayed(), 2L,
			TimeUnit.SECONDS), scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Adult").perform(scrollTo(), click());
		LXInfositeScreen.ticketAddButton(ticketName, "Child").perform(scrollTo(), click());
		LXInfositeScreen.bookNowButton(ticketName).perform(scrollTo(), click());
	}

	public void testVisibilitiesAndOfferDetailsOnCheckout() throws Throwable {
		goToCheckout();

		LXScreen.checkoutWidget().check(matches(isDisplayed()));
		LXScreen.checkoutActivityTitle().check(matches(withText("happy")));
		LXScreen.checkoutOfferTitle().check(matches(withText("2-Day New York Pass")));
		LXScreen.checkoutGroupText().check(matches(withText("3 Adults, 1 Child")));
		LXScreen.checkoutOfferLocation().check(matches(withText("New York, United States")));
		LXScreen.checkoutGrandTotalText().check(matches(withText("Total with Tax")));
		LXScreen.checkoutPriceText().check(matches(withText("$500")));
		LXScreen.checkoutFreeCancellationText().check(matches(withText("Free Cancellation")));

		LXScreen.checkoutSignInCard().check(matches(isDisplayed()));
		LXScreen.checkoutContactInfoCard().check(matches(isDisplayed()));
		LXScreen.checkoutPaymentInfoCard().check(matches(isDisplayed()));
		LXScreen.checkoutSlideToPurchase().check(matches(not(isDisplayed())));
	}

	public void testSliderVisibility() throws Throwable {
		goToCheckout();

		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		LXScreen.checkoutSlideToPurchase().check(matches(not(isDisplayed())));
		CheckoutViewModel.enterTravelerInfo();
		LXScreen.checkoutSlideToPurchase().check(matches(not(isDisplayed())));
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		LXScreen.checkoutSlideToPurchase().check(matches(isDisplayed()));
	}

	public void testRulesWidgetOnFreeCancellationInfoClick() throws Throwable {
		goToCheckout();

		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		LXScreen.checkoutGrandTotalText().perform(scrollTo());
		LXScreen.checkoutFreeCancellationText().check(matches(withText("Free Cancellation")));
		LXScreen.checkoutFreeCancellationText().perform(click());
		LXScreen.rulesWidget().check(matches(isDisplayed()));
		LXScreen.rulesWidgetCancellationPolicyHeader().check(matches(isDisplayed()));
		String cancellationPolicyContent = LXDataUtils
			.getCancelationPolicyDisplayText(getActivity(), 72);
		LXScreen.rulesWidgetCancellationPolicyContent(cancellationPolicyContent).check(matches(isDisplayed()));
		LXScreen.rulesWidgetRulesRestrictions().check(matches(isDisplayed()));
		LXScreen.rulesWidgetTermsConditions().check(matches(isDisplayed()));
		LXScreen.rulesWidgetPrivacyPolicy().check(matches(isDisplayed()));
		LXScreen.rulesWidgetToolbar().check(matches(isDisplayed()));
		LXScreen.rulesWidgetToolbar().check(matches(hasDescendant(withText(R.string.legal_information))));
	}

	public void testAcceptTermsVisibilityHappyPathGuestUser() throws Throwable {
		Common.setPOS(PointOfSaleId.FRANCE);
		goToCheckout();
		screenshot("Checkout_Payment_Card");
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		LXScreen.acceptTermsWidget().check(matches(isDisplayed()));
	}

	public void testAcceptTermsVisibilityOnBack() throws Throwable {
		Common.setPOS(PointOfSaleId.FRANCE);
		goToCheckout();
		screenshot("Checkout_Payment_Card");
		CheckoutViewModel.enterTravelerInfo();
		CheckoutViewModel.enterPaymentInfo();
		CheckoutViewModel.clickDone();
		Common.pressBack();
		LXInfositeScreen.bookNowButton("2-Day").perform(scrollTo(), click());
		CheckoutViewModel.driverInfo().perform(waitForViewToDisplay());
		LXScreen.acceptTermsWidget().check(matches(not(isDisplayed())));

	}
}
