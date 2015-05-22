package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonCheckoutScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsCheckoutScreen extends CommonCheckoutScreen {
	private static final int TITLE_VIEW_ID = R.id.title;
	private static final int RATING_VIEW_ID = R.id.rating_stars;
	private static final int COUPON_TEXT_ID = R.id.coupon_button;

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

	public static ViewInteraction guestCountView() {
		return onView(withId(R.id.guests_text));
	}

	public static ViewInteraction couponButton() {
		return onView(withId(COUPON_TEXT_ID));
	}
}
