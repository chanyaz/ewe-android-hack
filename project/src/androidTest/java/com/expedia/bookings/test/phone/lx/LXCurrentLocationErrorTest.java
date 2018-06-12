package com.expedia.bookings.test.phone.lx;

import org.junit.Test;

import android.location.Location;

import com.expedia.bookings.dagger.DaggerLXTestComponent;
import com.expedia.bookings.dagger.LXFakeCurrentLocationSuggestionModule;
import com.expedia.bookings.dagger.LXTestComponent;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.LxTestCase;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

public class LXCurrentLocationErrorTest extends LxTestCase {

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
			.appComponent(Common.getApplication().appComponent())
			.lXFakeCurrentLocationSuggestionModule(module)
			.build();

		Common.getApplication().setLXTestComponent(lxTestComponent);
		super.runTest();
	}

	@Test
	public void testNoCurrentLocationError() throws Throwable {
		Common.delay(1);
		LXScreen.didNotGoToResults();
		LXScreen.location().check(matches(isDisplayed()));

	}

	@Test
	public void testCurrentLocationNoSuggestionsError() throws Throwable {
		Common.delay(1);
		LXScreen.location().check(matches(isDisplayed()));
	}

//	 Since the experience is changed hence, need to update the test Card#14974
//	@Test
//	public void testCurrentLocationSuggestionWithNoActivitiesError() throws Throwable {
//		Common.delay(1);
//		LXScreen.searchFailed().check(matches(isDisplayed()));
//	}
}
