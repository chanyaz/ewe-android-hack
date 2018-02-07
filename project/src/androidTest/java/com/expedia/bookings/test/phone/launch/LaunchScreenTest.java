package com.expedia.bookings.test.phone.launch;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.mobiata.android.Log;

import static android.support.test.espresso.action.ViewActions.click;

public class LaunchScreenTest extends PhoneTestCase {

	public LaunchScreenTest() {
		super(PhoneLaunchActivity.class);
	}

	private static final String TAG = LaunchScreenTest.class.getName();

	@Test
	public void testGeneralUIElements() throws Throwable {
		LaunchScreen.tripsButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.account_sign_in_container);
		Log.v(TAG, "Trips button on Launch screen is displayed and works");

		LaunchScreen.shopButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.launch_list_widget);
		Log.v(TAG, "Shop button on Launch screen is displayed ");

		LaunchScreen.accountButton().perform(click());
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
		HotelResultsScreen.waitForResultsLoaded(10);
	}

}
