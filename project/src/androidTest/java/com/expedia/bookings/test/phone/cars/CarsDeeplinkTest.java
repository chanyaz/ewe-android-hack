package com.expedia.bookings.test.phone.cars;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;

public class CarsDeeplinkTest extends PhoneTestCase {

	public void testCarDeeplinkDropoffTimeBeforePickup() throws Throwable {
		Intent intent = new Intent();
		Uri dropOffBeforePickup = Uri.parse("expda://carSearch?pickupLocationLat=32.71444&pickupLocationLng=-117.16237&pickupDateTime=2015-06-26T09:00:00&dropoffDateTime=2015-06-25T09:00:00&originDescription=SFO-San Francisco International Airport");
		intent.setData(dropOffBeforePickup);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
			"com.expedia.bookings.activity.DeepLinkRouterActivity"));
		getApplication().startActivity(intent);

		ScreenActions.delay(3);
		EspressoUtils.assertViewIsDisplayed(R.id.widget_car_params);
	}

	public void testCarDeeplinkURLWithNoParams() throws Throwable {
		Intent intent = new Intent();
		Uri emptyParams = Uri.parse("expda://carSearch");
		intent.setData(emptyParams);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
			"com.expedia.bookings.activity.DeepLinkRouterActivity"));
		getApplication().startActivity(intent);

		ScreenActions.delay(3);
		EspressoUtils.assertViewIsDisplayed(R.id.widget_car_params);
	}
}
