package com.expedia.bookings.widget;

import java.util.List;

import org.joda.time.LocalDate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.time.widget.CalendarPicker;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;

public class CarSearchParamsWidget extends FrameLayout implements
	CalendarPicker.DateSelectionChangedListener,
	SeekBar.OnSeekBarChangeListener,
	EditText.OnEditorActionListener {

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
	AutoCompleteTextView pickupLocation;

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

	private CarSearchParams carSearchParams;

	private CarSuggestionAdapter suggestionAdapter;
	private Subscription suggestionSubscription;

	@OnClick(R.id.search_btn)
	public void startWidgetDownload() {
		if (isSearchFormFilled()) {
			Ui.hideKeyboard(this);
			Events.post(new Events.CarsNewSearchParams(carSearchParams));
			Events.post(new Events.CarsShowListLoading());
		}
	}

	@OnClick(R.id.dropoff_location)
	public void displayAlertForDropOffLocacationClick() {
		showAlertMessage(R.string.drop_off_same_as_pick_up, R.string.ok);
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

		suggestionAdapter = new CarSuggestionAdapter(getContext(), android.R.layout.simple_dropdown_item_1line);
		pickupLocation.setAdapter(suggestionAdapter);
		pickupLocation.setOnItemClickListener(mPickupListListener);
		pickupLocation.setOnClickListener(mPickupClickListener);
		pickupLocation.setOnDismissListener(mDismissListener);
		pickupLocation.setOnEditorActionListener(this);
		pickupLocation.addTextChangedListener(mPickupLocationTextWatcher);
	}

	private void setPickupLocation(Suggestion suggestion) {
		pickupLocation.setText(suggestion.shortName);
		searchParamsBuilder.origin(suggestion.airportCode);
		paramsChanged();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Events.unregister(getContext());
		if (suggestionSubscription != null) {
			suggestionSubscription.unsubscribe();
		}
		calendar.setDateChangedListener(null);
	}

	@OnClick(R.id.select_date)
	public void onPickupDateTimeClicked() {
		Ui.hideKeyboard(this);
		calendarContainer.setVisibility(View.VISIBLE);
	}

	/*
	 * Error handling
	 */

	public void showAlertMessage(int messageResourceId, int confirmButtonResourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(messageResourceId)
			.setNeutralButton(confirmButtonResourceId, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}

	private boolean isSearchFormFilled() {
		boolean areRequiredParamsFilled = searchParamsBuilder.areRequiredParamsFilled();
		if (!areRequiredParamsFilled) {
			showParamMissingToast();
		}
		return areRequiredParamsFilled;
	}

	private void showParamMissingToast() {
		int messageResourceId;
		if (carSearchParams == null || Strings.isEmpty(carSearchParams.origin)) {
			messageResourceId = R.string.error_missing_origin_param;
		}
		else if (carSearchParams.startDateTime == null) {
			messageResourceId = R.string.error_missing_start_date_param;
		}
		else {
			messageResourceId = R.string.error_missing_end_date_param;
		}
		showAlertMessage(messageResourceId, R.string.ok);
	}

	/*
	 * Pickup edit text helpers
	 */

	private AdapterView.OnItemClickListener mPickupListListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Suggestion suggestion = suggestionAdapter.getItem(position);
			setPickupLocation(suggestion);
		}
	};

	private OnClickListener mPickupClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			pickupLocation.setText("");
			searchParamsBuilder.origin("");
			paramsChanged();
		}
	};

	private AutoCompleteTextView.OnDismissListener mDismissListener = new AutoCompleteTextView.OnDismissListener() {
		@Override
		public void onDismiss() {
			Suggestion topSuggestion = suggestionAdapter.getItem(0);
			if (topSuggestion != null) {
				if (carSearchParams == null) {
					setPickupLocation(topSuggestion);
				}
				else if (Strings.isEmpty(carSearchParams.origin)) {
					setPickupLocation(topSuggestion);
				}
			}
		}
	};

	private TextWatcher mPickupLocationTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// ignore
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() >= 3) {
				suggestionSubscription = CarDb.getSuggestionServices().getAirportSuggestions(s.toString(), mSuggestionsRequestObs);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			// ignore
		}
	};

	Observer<List<Suggestion>> mSuggestionsRequestObs = new Observer<List<Suggestion>>() {
		List<Suggestion> list;

		@Override
		public void onCompleted() {
			suggestionAdapter.setSuggestionList(list);
		}

		@Override
		public void onError(Throwable e) {
			Log.d("ERROR", e);
		}

		@Override
		public void onNext(List<Suggestion> suggestions) {
			list = suggestions;
		}
	};


	/*
	 * Interfaces
	 */

	// Calendar

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
		carSearchParams = searchParamsBuilder.build();
		if (carSearchParams.startDateTime != null) {
			String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), carSearchParams,
				DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
			selectDateButton.setText(dateTimeRange);
		}
	}

	/*
	 * SeekBar
	 */

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

	/*
	 * OnEditorActionListener
	 */

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE && !Strings.isEmpty(v.getText())) {
			calendarContainer.setVisibility(View.VISIBLE);
		}
		return false;
	}
}
