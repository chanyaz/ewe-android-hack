package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.utils.Ui;

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

	private ImageView mHeaderImageView;
	private TextView mRoomTypeDesciptionTextView;
	private TextView mBedTypeNameTextView;
	private TextView mRoomLongDescriptionTextView;

	private TextView mNightsTextView;
	private TextView mDateRangeTextView;
	private TextView mGuestsTextView;
	private TextView mPriceTextView;
	private TextView mGrandTotalTextView;
	private TextView mWalletPromoAppliedTextView;

	public HotelReceiptModel(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
		mHeaderImageView = (ImageView) getView(HEADER_IMAGE_VIEW_ID);
		mRoomLongDescriptionTextView = (TextView) getView(ROOM_TYPE_DESCRIPTION_TEXT_VIEW_ID);
		mBedTypeNameTextView = (TextView) getView(BED_TYPE_NAME_TEXT_VIEW_ID);
		mRoomLongDescriptionTextView = (TextView) getView(ROOM_LONG_DESCRIPTION_TEXT_VIEW_ID);

		mNightsTextView = (TextView) getView(NIGHTS_TEXT_VIEW_ID);
		mDateRangeTextView = (TextView) getView(DATE_RANGE_TEXT_VIEW_ID);
		mGuestsTextView = (TextView) getView(GUESTS_TEXT_VIEW_ID);
		mPriceTextView = (TextView) getView(PRICE_TEXT_VIEW_ID);
		mGrandTotalTextView = (TextView) getView(GRAND_TOTAL_TEXT_VIEW_ID);
		mWalletPromoAppliedTextView = (TextView) getView(WALLET_PROMO_APPLIED_TEXT_VIEW_ID);
	}

	public ImageView headerImageView() {
		return mHeaderImageView;
	}

	public TextView roomTypeDescriptionTextView() {
		return mRoomTypeDesciptionTextView;
	}

	public TextView bedTypeTextView() {
		return mBedTypeNameTextView;
	}

	public TextView roomDescriptionTextView() {
		return mRoomLongDescriptionTextView;
	}

	public TextView nightsTextView() {
		return mNightsTextView;
	}

	public TextView dateRangeTextView() {
		return mDateRangeTextView;
	}

	public TextView guestsTextView() {
		return mGuestsTextView;
	}

	public TextView priceTextView() {
		return mPriceTextView;
	}

	public TextView grandTotalTextView() {
		return mGrandTotalTextView;
	}

	public TextView walletPromoAppliedTextView() {
		return mWalletPromoAppliedTextView;
	}
}
