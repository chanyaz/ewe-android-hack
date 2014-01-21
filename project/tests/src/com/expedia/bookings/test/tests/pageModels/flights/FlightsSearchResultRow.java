package com.expedia.bookings.test.tests.pageModels.flights;

import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;

public class FlightsSearchResultRow {

	private static final int PRICE_TEXT_VIEW_ID = R.id.price_text_view;
	private static final int AIRLINE_TEXT_VIEW_ID = R.id.airline_text_view;
	private static final int DEPARTURE_TIME_TEXT_VIEW_ID = R.id.departure_time_text_view;
	private static final int ARRIVAL_TIME_TEXT_VIEW_ID = R.id.arrival_time_text_view;
	private static final int MULTI_DAY_TEXT_VIEW_ID = R.id.multi_day_text_view;

	private TextView mPriceTextView;
	private TextView mAirlineTextView;
	private TextView mDepartureTimeTextView;
	private TextView mArrivalTimeTextView;
	private TextView mMultiDayTextView;

	public FlightsSearchResultRow(View view) {
		mPriceTextView = (TextView) view.findViewById(PRICE_TEXT_VIEW_ID);
		mAirlineTextView = (TextView) view.findViewById(AIRLINE_TEXT_VIEW_ID);
		mDepartureTimeTextView = (TextView) view.findViewById(DEPARTURE_TIME_TEXT_VIEW_ID);
		mArrivalTimeTextView = (TextView) view.findViewById(ARRIVAL_TIME_TEXT_VIEW_ID);
		mMultiDayTextView = (TextView) view.findViewById(MULTI_DAY_TEXT_VIEW_ID);
	}

	// Object access

	public TextView getPriceTextView() {
		return mPriceTextView;
	}

	public TextView getAirlineTextView() {
		return mAirlineTextView;
	}

	public TextView getDepartureTimeTextView() {
		return mDepartureTimeTextView;
	}

	public TextView getArrivalTimeTextView() {
		return mArrivalTimeTextView;
	}

	public TextView getMultiDayTextView() {
		return mMultiDayTextView;
	}

}
