package com.expedia.bookings.widget;

import org.joda.time.YearMonth;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

/**
 * TODO: Setup style that can change things in CalendarPicker
 * TODO: Scale all views based on size of CalendarPicker itself
 *
 */
public class CalendarPicker extends LinearLayout {

	private static final String INSTANCE_SUPER_STATE = "INSTANCE_SUPER_STATE";
	private static final String INSTANCE_DISPLAY_YEAR_MONTH = "INSTANCE_DISPLAY_YEAR_MONTH";

	private YearMonth mDisplayYearMonth;

	private TextView mPreviousMonthTextView;
	private TextView mCurrentMonthTextView;
	private TextView mNextMonthTextView;

	public CalendarPicker(Context context) {
		super(context);
		init(context);
	}

	public CalendarPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// Configure some layout params (so you don't have to do it in XML)
		setOrientation(LinearLayout.VERTICAL);

		// Inflate the widget
		inflate(context, R.layout.widget_calendar_picker, this);

		// Default to showing current year/month
		mDisplayYearMonth = YearMonth.now();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mPreviousMonthTextView = Ui.findView(this, R.id.previous_month);
		mCurrentMonthTextView = Ui.findView(this, R.id.current_month);
		mNextMonthTextView = Ui.findView(this, R.id.next_month);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_SUPER_STATE));
		mDisplayYearMonth = YearMonth.parse(bundle.getString(INSTANCE_DISPLAY_YEAR_MONTH));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Wait until here to start manipulating sub-Views; that way we can
		// restore the instance state properly first.
		updateHeader();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(INSTANCE_SUPER_STATE, super.onSaveInstanceState());
		bundle.putString(INSTANCE_DISPLAY_YEAR_MONTH, mDisplayYearMonth.toString());
		return bundle;
	}

	//////////////////////////////////////////////////////////////////////////
	// Display

	private void updateHeader() {
		mPreviousMonthTextView.setText(mDisplayYearMonth.minusMonths(1).monthOfYear().getAsText());
		mCurrentMonthTextView.setText(mDisplayYearMonth.monthOfYear().getAsText());
		mNextMonthTextView.setText(mDisplayYearMonth.plusMonths(1).monthOfYear().getAsText());
	}

}
