package com.expedia.bookings.test.phone.launch;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.NewLaunchScreen;
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity;
import com.mobiata.android.Log;

import static android.support.test.espresso.action.ViewActions.click;

public class NewLaunchScreenTest extends PhoneTestCase {

	public NewLaunchScreenTest() {
		super(NewPhoneLaunchActivity.class);
	}

	private static final String TAG = NewLaunchScreenTest.class.getName();

	@Test
	public void testGeneralUIElements() throws Throwable {
		NewLaunchScreen.tripsButton().perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.status_refresh_button, R.string.sign_in_for_your_trips);
		Log.v(TAG, "Trips button on Launch screen is displayed and works");

		NewLaunchScreen.shopButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.launch_list_widget);
		Log.v(TAG, "Shop button on Launch screen is displayed ");

		NewLaunchScreen.accountButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.sign_in_button);
		Log.v(TAG, "Account button on Launch screen is displayed ");

	}

	@Test
	public void testSeeMore() throws Throwable {
		CollectionLocation collectionLocation = new CollectionLocation();
		CollectionLocation.Location suggestion = new CollectionLocation.Location();
		suggestion.type = HotelSearchParams.SearchType.MY_LOCATION.toString();

		CollectionLocation.LatLng coordinates = new CollectionLocation.LatLng();
		coordinates.lat = 32.71444d;
		coordinates.lng = -117.16237d;
		suggestion.latLong = coordinates;

		collectionLocation.location = suggestion;
		Events.post(new Events.LaunchCollectionItemSelected(collectionLocation, null, ""));
		// Assert that the results screen is displayed
		HotelScreen.waitForResultsLoaded();
	}

}
