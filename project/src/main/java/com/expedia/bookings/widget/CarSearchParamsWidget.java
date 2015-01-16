package com.expedia.bookings.widget;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
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

	@InjectView(R.id.pickup_datetime)
	Button pickupDateTime;

	@InjectView(R.id.dropoff_datetime)
	Button dropoffDateTime;

	@InjectView(R.id.calendar_action_button)
	TextView calendarActionButton;

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

	private boolean isSelectingStartTime = true;

	@OnClick(R.id.calendar_action_button)
	public void startWidgetDownload() {
		startDownload();
		Ui.showToast(getContext(), "Loading results, please wait");
	}

	public void startDownload() {
		carSearchSubscription = CarServices
			.getInstance()
			.doBoringCarSearch(carSearchSubscriber);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		searchParams = new CarSearchParams();
		Events.register(getContext());

		dropoffDateTime.setVisibility(View.INVISIBLE);
		calendarContainer.setVisibility(View.INVISIBLE);
		timePickerContainer.setVisibility(View.INVISIBLE);
		changeTime.setVisibility(View.INVISIBLE);

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

	@OnClick(R.id.pickup_datetime)
	public void onPickupDateTimeClicked() {
		dropoffDateTime.setVisibility(View.VISIBLE);
		calendarContainer.setVisibility(View.VISIBLE);
		calendarActionButton.setText(R.string.next);
		isSelectingStartTime = true;
	}

	@OnClick(R.id.dropoff_datetime)
	public void onDropOffDateTimeClicked() {
		calendarActionButton.setText(R.string.search);
		isSelectingStartTime = false;
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

	@OnClick(R.id.calendar_action_button)
	public void onCalendarActionButtonClicked() {
		boolean actionButtonShowsSearch = !isSelectingStartTime;
		if (actionButtonShowsSearch) {
			CarDb.setSearchParams(searchParams);
		}
	}

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		DateTime dateSelected = start.toDateTimeAtStartOfDay();
		if (isSelectingStartTime) {
			searchParams.startTime = dateSelected;
		}
		else {
			searchParams.endTime = dateSelected;
		}
		changeTime.setText("CHANGE PICKUP TIME - 9:00AM PST");
		changeTime.setVisibility(View.VISIBLE);
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
}
