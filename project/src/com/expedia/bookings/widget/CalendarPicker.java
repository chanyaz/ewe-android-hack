package com.expedia.bookings.widget;

import android.content.Context;
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
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mPreviousMonthTextView = Ui.findView(this, R.id.previous_month);
		mCurrentMonthTextView = Ui.findView(this, R.id.current_month);
		mNextMonthTextView = Ui.findView(this, R.id.next_month);

		updateHeader();
	}

	//////////////////////////////////////////////////////////////////////////
	// Display

	private void updateHeader() {
		mPreviousMonthTextView.setText("PREVIOUS");
		mCurrentMonthTextView.setText("CURRENT");
		mNextMonthTextView.setText("NEXT");
	}
}
