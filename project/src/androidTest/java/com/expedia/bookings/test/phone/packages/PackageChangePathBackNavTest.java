package com.expedia.bookings.test.phone.packages;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.PackageTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

public class PackageChangePathBackNavTest extends PackageTestCase {

	public void testPackageChangePathBackNavTest() throws Throwable {
		PackageScreen.doPackageSearch();

		//change hotel, cancel
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change hotel")).check(matches(isEnabled()));
		onView(withText("Change hotel")).perform(click());
		Common.delay(1);
		Common.pressBack();
		Common.delay(1);
		assertAfterChange();

		//change hotel room, cancel
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change hotel room")).perform(click());
		Common.delay(1);
		Common.pressBack();
		Common.delay(1);
		assertAfterChange();

		//change flight, before change outbound flight, cancel
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change flights")).perform(click());
		Common.delay(1);
		Common.pressBack();
		Common.delay(1);
		assertAfterChange();

		//change flight, after change outbound flight, cancel
		openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
		onView(withText("Change flights")).perform(click());
		PackageScreen.selectFlight(-2);
		PackageScreen.selectThisFlight().perform(click());
		Common.delay(1);

		Common.pressBack();
		Common.delay(1);
		Common.pressBack();
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Flight to Detroit, MI")))));

		Common.pressBack();
		Common.delay(1);
		PackageScreen.flightsToolbar().check(matches(hasDescendant(allOf(isDisplayed(), withText("Select flight to Detroit, MI")))));

		Common.pressBack();
		Common.delay(1);
		assertAfterChange();
	}

	private void assertAfterChange() {
		onView(withId(R.id.checkout_button)).check(matches(isEnabled()));
	}
}

