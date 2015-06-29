package com.expedia.bookings.test.component.lx;

import android.location.Location;

import com.expedia.bookings.R;
import com.expedia.bookings.dagger.DaggerLXTestComponent;
import com.expedia.bookings.dagger.LXFakeCurrentLocationSuggestionModule;
import com.expedia.bookings.dagger.LXTestComponent;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.LxTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class LXCurrentLocationErrorTests extends LxTestCase {

	@Override
	public void runTest() throws Throwable {
		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();
		LXFakeCurrentLocationSuggestionModule module;
		ApiError apiError;
		if (testMethodName.contains("testCurrentLocationNoSuggestionsError")) {
			apiError = new ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS);
			ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
			errorInfo.cause = "No results from api.";
			apiError.errorInfo = errorInfo;
			module = new LXFakeCurrentLocationSuggestionModule(apiError);
		}
		else if (testMethodName.contains("testCurrentLocationSuggestionWithNoActivitiesError")) {
			// This fake location returns a suggestion that causes an lxSearch failure
			Location location = new Location("Jalandhar");
			location.setLatitude(31.32);
			location.setLongitude(75.57);
			module = new LXFakeCurrentLocationSuggestionModule(location);
		}
		else {
			// testNoCurrentLocationError
			apiError = new ApiError(ApiError.Code.CURRENT_LOCATION_ERROR);
			ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
			errorInfo.cause = "Could not determine users current location.";
			apiError.errorInfo = errorInfo;
			module = new LXFakeCurrentLocationSuggestionModule(apiError);
		}

		//Setup Lx Test Component
		LXTestComponent lxTestComponent = DaggerLXTestComponent.builder()
			.appComponent(getApplication().appComponent())
			.lXFakeCurrentLocationSuggestionModule(module)
			.build();

		getApplication().setLXTestComponent(lxTestComponent);
		super.runTest();
	}

	public void testNoCurrentLocationError() throws Throwable {
		ScreenActions.delay(1);
		LXViewModel.searchErrorScreen().check(matches(isDisplayed()));
		LXViewModel.searchErrorText().check(matches(withText(R.string.error_lx_current_location_search_message)));
		LXViewModel.srpErrorToolbar().check(matches(isDisplayed()));
		LXViewModel.srpErrorToolbar().check(matches(hasDescendant(withText(R.string.lx_error_current_location_toolbar_text))));

		screenshot("No current location");
		LXViewModel.searchErrorButton().perform(click());
	}

	public void testCurrentLocationNoSuggestionsError() throws Throwable {
		ScreenActions.delay(1);
		LXViewModel.searchErrorScreen().check(matches(isDisplayed()));
		LXViewModel.searchErrorText().check(matches(withText(R.string.lx_error_current_location_no_results)));
		LXViewModel.srpErrorToolbar().check(matches(isDisplayed()));
		LXViewModel.srpErrorToolbar().check(
			matches(hasDescendant(withText(R.string.lx_error_current_location_toolbar_text))));

		screenshot("No suggestions");
		LXViewModel.searchErrorButton().perform(click());
	}

	public void testCurrentLocationSuggestionWithNoActivitiesError() throws Throwable {
		ScreenActions.delay(1);
		LXViewModel.searchErrorScreen().check(matches(isDisplayed()));
		LXViewModel.searchErrorText().check(matches(withText(R.string.lx_error_current_location_no_results)));
		LXViewModel.srpErrorToolbar().check(matches(isDisplayed()));
		LXViewModel.srpErrorToolbar().check(matches(
			hasDescendant(withText("Jalandhar, India"))));

		screenshot("No activities");
		LXViewModel.searchErrorButton().perform(click());
	}
}
