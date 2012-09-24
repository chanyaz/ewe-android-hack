package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.StrUtils;

public class MiniReceiptWidget extends RelativeLayout {
	private TextView mRoomTypeTextView;
	private TextView mGuestsTextView;
	private TextView mDatesTextView;
	private TextView mTotalCostTextView;

	private boolean mViewsInflated = false;

	public MiniReceiptWidget(Context context) {
		this(context, null, 0);
	}

	public MiniReceiptWidget(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MiniReceiptWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void bind(Property selectedProperty, SearchParams searchParams, Rate selectedRate) {
		if (!mViewsInflated) {
			mRoomTypeTextView = (TextView) findViewById(R.id.room_type_text_view);
			mGuestsTextView = (TextView) findViewById(R.id.guests_text_view);
			mDatesTextView = (TextView) findViewById(R.id.dates_text_view);
			mTotalCostTextView = (TextView) findViewById(R.id.total_cost_text_view);

			mViewsInflated = true;
		}

		mRoomTypeTextView.setText(selectedRate.getRatePlanName());
		mGuestsTextView.setText(StrUtils.formatGuests(getContext(), searchParams));

		Money displayedTotal;
		if (LocaleUtils.shouldDisplayMandatoryFees(getContext())) {
			displayedTotal = selectedRate.getTotalPriceWithMandatoryFees();
		}
		else {
			displayedTotal = selectedRate.getTotalAmountAfterTax();
		}

		mTotalCostTextView.setText(displayedTotal.getFormattedMoney());
	}
}