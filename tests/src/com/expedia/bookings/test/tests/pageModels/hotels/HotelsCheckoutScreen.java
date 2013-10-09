package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;

import com.expedia.bookings.test.tests.pageModels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsCheckoutScreen extends CommonCheckoutScreen {

	public HotelsCheckoutScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public HotelReceiptModel hotelReceiptModel() {
		return new HotelReceiptModel(mInstrumentation, getCurrentActivity(), mRes,
				mPreferences);
	}

}
