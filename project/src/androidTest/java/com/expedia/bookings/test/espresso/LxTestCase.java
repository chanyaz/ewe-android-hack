package com.expedia.bookings.test.espresso;

import com.expedia.bookings.dagger.DaggerLXTestComponent;
import com.expedia.bookings.dagger.LXFakeCurrentLocationSuggestionModule;
import com.expedia.bookings.dagger.LXTestComponent;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.test.espresso.IdlingResources.LxIdlingResource;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;

public class LxTestCase extends PhoneTestCase {

	private LxIdlingResource mLxIdlingResource;

	public LxIdlingResource getLxIdlingResource() {
		return mLxIdlingResource;
	}

	public LxTestCase() {
		super(LXBaseActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		mLxIdlingResource = new LxIdlingResource();
		mLxIdlingResource.register();

		if (Common.getApplication().lxTestComponent() == null) {
			ApiError apiError = new ApiError(ApiError.Code.CURRENT_LOCATION_ERROR);
			ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
			errorInfo.cause = "Could not determine users current location.";
			apiError.errorInfo = errorInfo;
			LXFakeCurrentLocationSuggestionModule module = new LXFakeCurrentLocationSuggestionModule(apiError);

			LXTestComponent lxTestComponent = DaggerLXTestComponent.builder()
				.appComponent(Common.getApplication().appComponent())
				.lXFakeCurrentLocationSuggestionModule(module)
				.build();
			Common.getApplication().setLXTestComponent(lxTestComponent);
		}
		super.runTest();
	}

	@Override
	public void tearDown() throws Exception {
		mLxIdlingResource.unregister();
		mLxIdlingResource = null;

		Common.getApplication().setLXTestComponent(null);
		super.tearDown();
	}
}
