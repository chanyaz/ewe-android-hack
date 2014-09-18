package com.expedia.bookings.test.phone.pagemodels.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsCheckoutScreen extends CommonCheckoutScreen {
	private static final int TITLE_VIEW_ID = R.id.title;
	private static final int RATING_VIEW_ID = R.id.rating;
	private HotelReceiptModel mHotelReceiptModel;

	public HotelReceiptModel hotelReceiptModel() {
		if (mHotelReceiptModel == null) {
			mHotelReceiptModel = new HotelReceiptModel();
		}
		return mHotelReceiptModel;
	}

	// Object access

	public static ViewInteraction hotelNameView() {
		return (onView(withId(TITLE_VIEW_ID)));
	}

	public static ViewInteraction ratingBar() {
		return (onView(withId(RATING_VIEW_ID)));
	}
}
