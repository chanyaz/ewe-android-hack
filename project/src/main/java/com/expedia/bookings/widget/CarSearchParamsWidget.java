package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
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
	CalendarPicker.DateSelectionChangedListener, SeekBar.OnSeekBarChangeListener {

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

	@InjectView(R.id.pickup_time_seek_bar)
	SeekBar pickupTimeSeekBar;

	@InjectView(R.id.dropoff_time_seek_bar)
	SeekBar dropoffTimeSeekBar;

	private CarSearchParamsBuilder searchParamsBuilder;

	Subscription carSearchSubscription;

	@OnClick(R.id.search_btn)
	public void startWidgetDownload() {
		startDownload();
		Ui.showToast(getContext(), "Loading results, please wait");
	}

	public void startDownload() {
		CarSearchParams params = searchParamsBuilder.build();
		params.origin = pickupLocation.getText().toString();

		CarDb.setSearchParams(params);

		carSearchSubscription = CarDb.getCarServices()
			.carSearch(params, carSearchSubscriber);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		searchParamsBuilder = new CarSearchParamsBuilder();
		Events.register(getContext());

		pickupLocation.setVisibility(View.VISIBLE);
		dropoffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		calendarContainer.setVisibility(View.INVISIBLE);
		pickupTimeSeekBar.setVisibility(View.VISIBLE);
		pickupTimeSeekBar.setOnSeekBarChangeListener(this);
		dropoffTimeSeekBar.setVisibility(View.VISIBLE);
		dropoffTimeSeekBar.setOnSeekBarChangeListener(this);

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
		searchParamsBuilder.startDate(start);
		searchParamsBuilder.endDate(end);
		paramsChanged();
	}

	// Seek bar

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar.getId() == R.id.pickup_time_seek_bar) {
			searchParamsBuilder.startMillis(convertProgressToMillis(progress));
		}
		else if (seekBar.getId() == R.id.dropoff_time_seek_bar) {
			searchParamsBuilder.endMillis(convertProgressToMillis(progress));
		}
		else {
			throw new RuntimeException("You're using our seekbar listener on an unknown view.");
		}

		paramsChanged();
	}

	private void paramsChanged() {
		CarSearchParams params = searchParamsBuilder.build();
		if (params.startDateTime != null) {
			String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
			selectDateButton.setText(dateTimeRange);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// ignore
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// ignore
	}

	private static int convertProgressToMillis(int progress) {
		return progress * (30 * 60 * 1000);
	}
}
