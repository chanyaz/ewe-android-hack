package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsCheckoutScreen extends CommonCheckoutScreen {

	private static final int TITLE_VIEW_ID = R.id.title;
	private static final int RATING_VIEW_ID = R.id.rating;
	private HotelReceiptModel mHotelReceiptModel;

	public HotelsCheckoutScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public HotelReceiptModel hotelReceiptModel() {
		if (mHotelReceiptModel == null) {
			mHotelReceiptModel = new HotelReceiptModel(mInstrumentation, getCurrentActivity(), mRes,
					mPreferences);
		}
		return mHotelReceiptModel;
	}

	// Object access

	public TextView hotelNameView() {
		return (TextView) getView(TITLE_VIEW_ID);
	}

	public RatingBar ratingBar() {
		return (RatingBar) getView(RATING_VIEW_ID);
	}

}
