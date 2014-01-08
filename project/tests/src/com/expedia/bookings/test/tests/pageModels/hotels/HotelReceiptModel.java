package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

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

	public HotelReceiptModel(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	public ImageView headerImageView() {
		return (ImageView) getView(HEADER_IMAGE_VIEW_ID);
	}

	public TextView roomTypeDescriptionTextView() {
		return (TextView) getView(ROOM_TYPE_DESCRIPTION_TEXT_VIEW_ID);
	}

	public TextView bedTypeTextView() {
		return (TextView) getView(BED_TYPE_NAME_TEXT_VIEW_ID);
	}

	public TextView roomDescriptionTextView() {
		return (TextView) getView(ROOM_LONG_DESCRIPTION_TEXT_VIEW_ID);
	}

	public TextView nightsTextView() {
		return (TextView) getView(NIGHTS_TEXT_VIEW_ID);
	}

	public TextView dateRangeTextView() {
		return (TextView) getView(DATE_RANGE_TEXT_VIEW_ID);
	}

	public TextView guestsTextView() {
		return (TextView) getView(GUESTS_TEXT_VIEW_ID);
	}

	public TextView priceTextView() {
		return (TextView) getView(PRICE_TEXT_VIEW_ID);
	}

	public TextView grandTotalTextView() {
		return (TextView) getView(GRAND_TOTAL_TEXT_VIEW_ID);
	}

	public TextView walletPromoAppliedTextView() {
		return (TextView) getView(WALLET_PROMO_APPLIED_TEXT_VIEW_ID);
	}

	public String costSummaryString() {
		return getString(COST_SUMMARY_STRING_ID);
	}

	// Object interaction

	public void clickGrandTotalTextView() {
		clickOnView(grandTotalTextView());
	}
}
