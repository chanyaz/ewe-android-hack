package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.time.widget.CalendarPicker;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarSearchParamsWidget extends FrameLayout {

	public CarSearchParamsWidget(Context context) {
		super(context);
	}

	public CarSearchParamsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CarSearchParamsWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@InjectView(R.id.pickup_datetime) Button pickupDateTime;
	@InjectView(R.id.calendar) CalendarPicker calendar;
	@InjectView(R.id.change_time) TextView changeTime;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		calendar.setVisibility(View.INVISIBLE);
		changeTime.setVisibility(View.INVISIBLE);

		calendar.setDateChangedListener(new CalendarPicker.DateSelectionChangedListener() {
			@Override
			public void onDateSelectionChanged(LocalDate start, LocalDate end) {
				onDateSelected(start);
			}
		});
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		pickupDateTime.setText("Pick-up time");
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@OnClick(R.id.pickup_datetime)
	public void onPickupDateTimeClicked() {
		calendar.setVisibility(View.VISIBLE);
	}

	public void onDateSelected(LocalDate date) {
		changeTime.setText("CHANGE PICKUP TIME - 9:00AM PST");
		changeTime.setVisibility(View.VISIBLE);
	}
}