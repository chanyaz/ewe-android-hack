package com.expedia.bookings.test.phone.packages;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.espresso.ViewActions;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isFocusable;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

public class PackageCheckoutTest extends PackageTestCase {

	public void testSlideToPayWidget() throws Throwable {
		PackageScreen.doPackageSearch();
		assertChangePackageVisibility(true);
		assertBundleWidgetOnOverview();
		PackageScreen.checkout().perform(click());
		assertCheckoutBottomContainerBeforeComplete();
		assertBundleWidgetOnCheckout();
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
		onView(withId(R.id.total_price_widget))
			.perform(ViewActions.waitForViewToDisplay())
			.check(matches(isDisplayed()));
		onView(withId(R.id.slide_to_purchase_widget)).check(matches(isCompletelyDisplayed()));
		onView(withId(R.id.slide_to_purchase_layout)).check(matches(isFocusable()));
	}

	private void assertCheckoutBottomContainerBeforeComplete() {
		onView(withId(R.id.total_price_widget)).check(matches(isDisplayed()));
		onView(withId(R.id.slide_to_purchase_widget)).check(matches(not(isCompletelyDisplayed())));
		onView(withId(R.id.slide_to_purchase_layout)).check(matches(not(isFocusable())));
	}

	private void assertBundleWidgetOnOverview() {
		onView(withId(R.id.nested_scrollview)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
	}

	private void assertBundleWidgetOnCheckout() {
		onView(withId(R.id.nested_scrollview)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
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

