package com.expedia.bookings.test.component.lx;

import com.expedia.bookings.R;
import com.expedia.bookings.dagger.DaggerLXTestComponent;
import com.expedia.bookings.dagger.LXFakeCurrentLocationSuggestionModule;
import com.expedia.bookings.dagger.LXTestComponent;
import com.expedia.bookings.enums.LxCurrentLocationSearchErrorTestMode;
import com.expedia.bookings.test.ui.utils.LxTestCase;
import com.expedia.bookings.utils.Ui;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LXCurrentLocationErrorTests extends LxTestCase {

	@Override
	public void runTest() throws Throwable {

		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();

		LxCurrentLocationSearchErrorTestMode sCurrentLocationSearchErrorTestMode = LxCurrentLocationSearchErrorTestMode.NO_CURRENT_LOCATION;
		if (testMethodName.contains("testNoCurrentLocationError")) {
			sCurrentLocationSearchErrorTestMode = LxCurrentLocationSearchErrorTestMode.NO_CURRENT_LOCATION;
		}
		else if (testMethodName.contains("testCurrentLocationNoSuggestionsError")) {
			sCurrentLocationSearchErrorTestMode = LxCurrentLocationSearchErrorTestMode.NO_SUGGESTIONS;
		}
		else if (testMethodName.contains("testCurrentLocationSuggestionWithNoActivitiesError")) {
			sCurrentLocationSearchErrorTestMode = LxCurrentLocationSearchErrorTestMode.NO_LX_ACTIVITIES;
		}

		//Setup Lx Test Component
		LXTestComponent lxTestComponent = DaggerLXTestComponent.builder()
			.appComponent(Ui.getApplication(getInstrumentation().getTargetContext()).appComponent())
			.lXFakeCurrentLocationSuggestionModule(
				new LXFakeCurrentLocationSuggestionModule(sCurrentLocationSearchErrorTestMode))
			.build();
		Ui.getApplication(getInstrumentation().getTargetContext()).setLXComponent(lxTestComponent);

		super.runTest();
	}

	public void testNoCurrentLocationError() throws Throwable {
		LXViewModel.searchErrorScreen().check(matches(isDisplayed()));
		LXViewModel.searchErrorText().check(matches(withText(R.string.error_lx_current_location_search_message)));
		LXViewModel.srpErrorToolbar().check(matches(isDisplayed()));
		LXViewModel.srpErrorToolbar().check(matches(hasDescendant(withText(R.string.lx_error_current_location_toolbar_text))));
		LXViewModel.searchErrorButton().perform(click());
	}

	public void testCurrentLocationNoSuggestionsError() throws Throwable {
		LXViewModel.searchErrorScreen().check(matches(isDisplayed()));
		LXViewModel.searchErrorText().check(matches(withText(R.string.lx_error_current_location_no_results)));
		LXViewModel.srpErrorToolbar().check(matches(isDisplayed()));
		LXViewModel.srpErrorToolbar().check(
			matches(hasDescendant(withText(R.string.lx_error_current_location_toolbar_text))));
		LXViewModel.searchErrorButton().perform(click());
	}

	public void testCurrentLocationSuggestionWithNoActivitiesError() throws Throwable {
		LXViewModel.searchErrorScreen().check(matches(isDisplayed()));
		LXViewModel.searchErrorText().check(matches(withText(R.string.lx_error_current_location_no_results)));
		LXViewModel.srpErrorToolbar().check(matches(isDisplayed()));
		LXViewModel.srpErrorToolbar().check(matches(
			hasDescendant(withText("search_failure"))));
		LXViewModel.searchErrorButton().perform(click());
	}
}
