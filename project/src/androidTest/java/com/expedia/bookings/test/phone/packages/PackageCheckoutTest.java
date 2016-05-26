package com.expedia.bookings.test.phone.packages;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PackageTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

public class PackageCheckoutTest extends PackageTestCase {

	public void testSlideToPayWidget() throws Throwable {
		PackageScreen.doPackageSearch();
		assertChangePackageVisibility(true);
		PackageScreen.checkout().perform(click());
		assertCheckoutBottomContainerBeforeComplete();

		PackageScreen.enterTravelerInfo();
		PackageScreen.enterPaymentInfo();
		assertCheckoutBottomContainerAfterComplete();

		pressBack();
		assertOverviewBottomContainer();
		assertChangePackageVisibility(true);
		PackageScreen.checkout().perform(click());
		assertChangePackageVisibility(false);
		assertCheckoutBottomContainerAfterComplete();
		pressBack();

	}

	private void assertOverviewBottomContainer() {
		onView(withId(R.id.total_price_widget)).check(matches(isDisplayed()));
		onView(withId(R.id.slide_to_purchase_widget)).check(matches(not(isCompletelyDisplayed())));
	}

	private void assertCheckoutBottomContainerAfterComplete() {
		onView(withId(R.id.total_price_widget)).check(matches(isDisplayed()));
		onView(withId(R.id.slide_to_purchase_widget)).check(matches(isCompletelyDisplayed()));
	}

	private void assertCheckoutBottomContainerBeforeComplete() {
		onView(withId(R.id.total_price_widget)).check(matches(isDisplayed()));
		onView(withId(R.id.slide_to_purchase_widget)).check(matches(not(isCompletelyDisplayed())));
	}

	private void assertChangePackageVisibility(boolean isVisible) {
		if (isVisible) {
			onView(allOf(isDisplayed(), withContentDescription("More options"))).check(matches(isDisplayed()));
		}
		else {
			onView(allOf(isDisplayed(), withContentDescription("More options"))).check(doesNotExist());
		}
	}
}

