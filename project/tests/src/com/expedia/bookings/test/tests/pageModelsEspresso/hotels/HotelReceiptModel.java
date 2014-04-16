package com.expedia.bookings.test.tests.pageModelsEspresso.hotels;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelReceiptModel extends ScreenActions {
	private static final int HEADER_IMAGE_VIEW_ID = R.id.header_image_view;
	private static final int ROOM_TYPE_DESCRIPTION_TEXT_VIEW_ID = R.id.room_type_description_text_view;
	private static final int BED_TYPE_NAME_TEXT_VIEW_ID = R.id.bed_type_name_text_view;
	private static final int ROOM_LONG_DESCRIPTION_TEXT_VIEW_ID = R.id.room_long_description_text_view;

	private static final int NIGHTS_TEXT_VIEW_ID = R.id.nights_text;
	private static final int DATE_RANGE_TEXT_VIEW_ID = R.id.date_range_text;
	private static final int GUESTS_TEXT_VIEW_ID = R.id.guests_text;
	private static final int PRICE_TEXT_VIEW_ID = R.id.price_text;
	private static final int GRAND_TOTAL_TEXT_VIEW_ID = R.id.grand_total_text;
	private static final int WALLET_PROMO_APPLIED_TEXT_VIEW_ID = R.id.wallet_promo_applied_text_view;
	private static final int COST_SUMMARY_STRING_ID = R.string.cost_summary;

	public static ViewInteraction headerImageView() {
		return onView(withId(HEADER_IMAGE_VIEW_ID));
	}

	public static ViewInteraction roomTypeDescriptionTextView() {
		return onView(withId(ROOM_TYPE_DESCRIPTION_TEXT_VIEW_ID));
	}

	public static ViewInteraction bedTypeTextView() {
		return onView(withId(BED_TYPE_NAME_TEXT_VIEW_ID));
	}

	public static ViewInteraction roomDescriptionTextView() {
		return onView(withId(ROOM_LONG_DESCRIPTION_TEXT_VIEW_ID));
	}

	public static ViewInteraction nightsTextView() {
		return onView(withId(NIGHTS_TEXT_VIEW_ID));
	}

	public static ViewInteraction dateRangeTextView() {
		return onView(withId(DATE_RANGE_TEXT_VIEW_ID));
	}

	public static ViewInteraction guestsTextView() {
		return onView(withId(GUESTS_TEXT_VIEW_ID));
	}

	public static ViewInteraction priceTextView() {
		return onView(withId(PRICE_TEXT_VIEW_ID));
	}

	public static ViewInteraction grandTotalTextView() {
		return onView(withId(GRAND_TOTAL_TEXT_VIEW_ID));
	}

	public static ViewInteraction walletPromoAppliedTextView() {
		return onView(withId(WALLET_PROMO_APPLIED_TEXT_VIEW_ID));
	}

	public static ViewInteraction costSummaryString() {
		return onView(withText(COST_SUMMARY_STRING_ID));
	}

	// Object interaction

	public static void clickGrandTotalTextView() {
		grandTotalTextView().perform(click());
	}
}
