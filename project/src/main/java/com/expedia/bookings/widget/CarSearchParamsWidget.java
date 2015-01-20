package com.expedia.bookings.widget;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.time.widget.CalendarPicker;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class CarSearchParamsWidget extends FrameLayout implements
	CalendarPicker.DateSelectionChangedListener {

	public CarSearchParamsWidget(Context context) {
		super(context);
	}

	public CarSearchParamsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CarSearchParamsWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@InjectView(R.id.pickup_location)
	EditText pickupLocation;

	@InjectView(R.id.dropoff_location)
	TextView dropoffLocation;

	@InjectView(R.id.select_date)
	Button selectDateButton;

	@InjectView(R.id.search_btn)
	ImageButton searchButton;

	@InjectView(R.id.calendar_container)
	ViewGroup calendarContainer;

	@InjectView(R.id.calendar)
	CalendarPicker calendar;

	@InjectView(R.id.change_time)
	TextView changeTime;

	@InjectView(R.id.time_container)
	ViewGroup timePickerContainer;

	private CarSearchParams searchParams;

	Subscription carSearchSubscription;

	@OnClick(R.id.search_btn)
	public void startWidgetDownload() {
		startDownload();
		Ui.showToast(getContext(), "Loading results, please wait");
	}

	public void startDownload() {
		searchParams.origin = pickupLocation.getText().toString();
		CarDb.setSearchParams(searchParams);

		carSearchSubscription = CarDb.getCarServices()
			.carSearch(CarDb.searchParams, carSearchSubscriber);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		searchParams = new CarSearchParams();
		Events.register(getContext());

		pickupLocation.setVisibility(View.VISIBLE);
		dropoffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		calendarContainer.setVisibility(View.INVISIBLE);
		timePickerContainer.setVisibility(View.INVISIBLE);
		changeTime.setVisibility(View.INVISIBLE);

		calendar.setSelectableDateRange(LocalDate.now(),
			LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)));
		calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_flight_search));
		calendar.setDateChangedListener(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Events.unregister(getContext());
		calendar.setDateChangedListener(null);
	}

	@OnClick(R.id.select_date)
	public void onPickupDateTimeClicked() {
		calendarContainer.setVisibility(View.VISIBLE);
	}

	@OnClick(R.id.change_time)
	public void onChangeTimeClicked() {
		calendarContainer.setVisibility(View.INVISIBLE);
		timePickerContainer.setVisibility(View.VISIBLE);
	}

	@OnClick(R.id.time_confirm_btn)
	public void onTimeConfirmClicked() {
		timePickerContainer.setVisibility(View.INVISIBLE);
		calendarContainer.setVisibility(View.VISIBLE);
	}

	private Observer<CarSearch> carSearchSubscriber = new Observer<CarSearch>() {
		@Override
		public void onCompleted() {
			Log.d("TestCarSearchWidget - onCompleted");
			Events.post(new Events.EnableCarsSearchResults());
		}

		@Override
		public void onError(Throwable e) {
			Log.d("TestCarSearchWidget - onError", e);
		}

		@Override
		public void onNext(CarSearch carSearch) {
			Log.d("TestCarSearchWidget - onNext");
			CarDb.carSearch = carSearch;
		}
	};

	// Interfaces

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		DateTime startDate = start.toDateTimeAtStartOfDay();
		DateTime endDate = null;
		if (end != null) {
			endDate = end.toDateTimeAtStartOfDay();
		}
		searchParams.startTime = startDate;
		searchParams.endTime = endDate;

		String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), searchParams, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
		selectDateButton.setText(dateTimeRange);
		changeTime.setText("CHANGE PICKUP TIME - 9:00AM PST");
		changeTime.setVisibility(View.VISIBLE);
	}
}
