package com.expedia.bookings.test.tests.pageModels.hotels;

import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;

public class RoomsAndRatesRow {

	private static final int SALE_TEXT_VIEW_ID = R.id.sale_text_view;
	private static final int ROOM_DESCRIPTION_TEXT_VIEW_ID = R.id.room_description_text_view;
	private static final int PRICE_EXPLANATION_TEXT_VIEW_ID = R.id.price_explanation_text_view;
	private static final int PRICE_TEXT_VIEW_ID = R.id.price_text_view;
	private static final int BEDS_TEXT_VIEW_ID = R.id.beds_text_view;
	private static final int VALUE_ADDS_TEXT_VIEW_ID = R.id.value_adds_text_view;
	private static final int VALUE_ADDS_BEDS_TEXT_VIEW_ID = R.id.value_adds_beds_text_view;

	private TextView mSaleTextView;
	private TextView mRoomDescriptionTextView;
	private TextView mPriceExplanationTextView;
	private TextView mPriceTextView;
	private TextView mBedsTextView;
	private TextView mValueAddsTextView;
	private TextView mValueAddsBedsTextView;

	public RoomsAndRatesRow(View rowView) {
		mSaleTextView = (TextView) rowView.findViewById(SALE_TEXT_VIEW_ID);
		mRoomDescriptionTextView = (TextView) rowView.findViewById(ROOM_DESCRIPTION_TEXT_VIEW_ID);
		mPriceExplanationTextView = (TextView) rowView.findViewById(PRICE_EXPLANATION_TEXT_VIEW_ID);
		mPriceTextView = (TextView) rowView.findViewById(PRICE_TEXT_VIEW_ID);
		mBedsTextView = (TextView) rowView.findViewById(BEDS_TEXT_VIEW_ID);
		mValueAddsTextView = (TextView) rowView.findViewById(VALUE_ADDS_TEXT_VIEW_ID);
		mValueAddsBedsTextView = (TextView) rowView.findViewById(VALUE_ADDS_BEDS_TEXT_VIEW_ID);
	}

	public TextView saleTextView() {
		return mSaleTextView;
	}

	public TextView roomDescriptionTextView() {
		return mRoomDescriptionTextView;
	}

	public TextView priceExplanationTextView() {
		return mPriceExplanationTextView;
	}

	public TextView priceTextView() {
		return mPriceTextView;
	}

	public TextView bedsTextView() {
		return mBedsTextView;
	}

	public TextView valueAddsTextView() {
		return mValueAddsTextView;
	}

	public TextView valueAddsBedsTextView() {
		return mValueAddsBedsTextView;
	}

}
