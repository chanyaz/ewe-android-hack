package com.expedia.bookings.widget;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarSearchParamsWidget extends Presenter implements EditText.OnEditorActionListener, CarDateTimeWidget.ICarDateTimeListener {

	private static final String TOOLTIP_PATTERN = "hh:mm aa";
	private static final String TOOLTIP_DATE_PATTERN = "MMM dd";
	private DateTimeFormatter df = DateTimeFormat.forPattern(TOOLTIP_DATE_PATTERN);
	private ArrayList<Suggestion> mRecentCarsLocationsSearches;
	// We keep a separate (but equal) recents list for routes-based searches
	// because it's slightly different (e.g., no description)
	private static final String RECENT_ROUTES_CARS_LOCATION_FILE = "recent-cars-airport-routes-list.dat";
	private static final int RECENT_MAX_SIZE = 3;

	public CarSearchParamsWidget(Context context) {
		this(context, null);
	}

	public CarSearchParamsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private CarSearchParamsBuilder searchParamsBuilder = new CarSearchParamsBuilder();
	private CarSearchParams carSearchParams;

	private CarSuggestionAdapter suggestionAdapter;

	@InjectView(R.id.pickup_location)
	AutoCompleteTextView pickupLocation;

	@InjectView(R.id.dropoff_location)
	TextView dropoffLocation;

	@InjectView(R.id.title)
	TextView txtTitle;

	@InjectView(R.id.select_date)
	ToggleButton selectDateButton;

	@InjectView(R.id.backout_btn)
	ImageButton backButton;

	@InjectView(R.id.search_btn)
	ImageButton searchButton;

	@InjectView(R.id.calendar_container)
	CarDateTimeWidget calendarContainer;

	@OnClick(R.id.search_btn)
	public void startWidgetDownload() {
		if (isSearchFormFilled()) {
			Ui.hideKeyboard(this);
			Events.post(new Events.CarsNewSearchParams(carSearchParams));
		}
	}

	@OnClick(R.id.pickup_location)
	public void onPickupEditClicked() {
		if (!getCurrentState().equals(CarParamsDefault.class.getName())) {
			back();
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
		calendarContainer.setCarDateTimeListener(this);
		pickupLocation.setVisibility(View.VISIBLE);
		dropoffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		setCalendarVisibility(View.INVISIBLE);

		suggestionAdapter = new CarSuggestionAdapter(getContext(), R.layout.cars_dropdown_item);
		pickupLocation.setAdapter(suggestionAdapter);
		pickupLocation.setOnItemClickListener(mPickupListListener);
		pickupLocation.setOnFocusChangeListener(mPickupClickListener);
		pickupLocation.setOnEditorActionListener(this);
		pickupLocation.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.location);
		drawableEnabled.setColorFilter(getResources().getColor(R.color.cars_secondary_color), PorterDuff.Mode.SRC_IN);
		pickupLocation.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);

		Drawable drawableDisabled = getResources().getDrawable(R.drawable.location).mutate();
		drawableDisabled
				.setColorFilter(getResources().getColor(R.color.cars_dropdown_disabled_stroke), PorterDuff.Mode.SRC_IN);
		dropoffLocation.setCompoundDrawablesWithIntrinsicBounds(drawableDisabled, null, null, null);

		loadHistory();

		addTransition(mOneToTwo);

		show(new CarParamsDefault());
	}

	private void setPickupLocation(final Suggestion suggestion) {
		pickupLocation.setText(StrUtils.formatCityName(suggestion.fullName));
		searchParamsBuilder.origin(suggestion.airportCode);
		paramsChanged();
		suggestion.isHistory = true;

		// Remove duplicates
		Iterator<Suggestion> it = mRecentCarsLocationsSearches.iterator();
		while (it.hasNext()) {
			Suggestion suggest = it.next();
			if (suggest.airportCode.equalsIgnoreCase(suggestion.airportCode)) {
				it.remove();
			}
		}

		if (mRecentCarsLocationsSearches.size() >= RECENT_MAX_SIZE) {
			mRecentCarsLocationsSearches.remove(RECENT_MAX_SIZE - 1);
		}

		mRecentCarsLocationsSearches.add(0, suggestion);
		//Have to remove the bold tag in display name so text for last search is normal
		suggestion.displayName = Html.fromHtml(suggestion.displayName).toString();
		// Save
		saveHistory();

	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@OnClick(R.id.select_date)
	public void onPickupDateTimeClicked() {
		show(new CarParamsCalendar());
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
			Ui.hideKeyboard(CarSearchParamsWidget.this);
			clearFocus();
			Suggestion suggestion = suggestionAdapter.getItem(position);
			setPickupLocation(suggestion);
		}
	};

	private OnFocusChangeListener mPickupClickListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (!getCurrentState().equals(CarParamsDefault.class.getName())) {
					back();
				}
				selectDateButton.setChecked(false);
				selectDateButton.setEnabled(true);
				pickupLocation.setText("");
				searchParamsBuilder.origin("");
				paramsChanged();
			}
		}
	};

	/**
	 * Interfaces
	 */

	private void paramsChanged() {
		carSearchParams = searchParamsBuilder.build();
		if (carSearchParams.startDateTime != null) {
			String dateTimeRange = DateFormatUtils.formatCarSearchDateRange(getContext(), carSearchParams,
					DateFormatUtils.FLAGS_DATE_ABBREV_MONTH | DateFormatUtils.FLAGS_TIME_FORMAT);
			selectDateButton.setText(dateTimeRange);
			selectDateButton.setTextOff(dateTimeRange);
			selectDateButton.setTextOn(dateTimeRange);
		}
	}

	public CarSearchParams getCurrentParams() {
		return searchParamsBuilder.build();
	}

	/*
	 * OnEditorActionListener
	 */

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE && !Strings.isEmpty(v.getText())) {
			setCalendarVisibility(View.VISIBLE);
			Suggestion topSuggestion = suggestionAdapter.getItem(0);
			if (topSuggestion != null) {
				if (carSearchParams == null) {
					setPickupLocation(topSuggestion);
				}
				else if (Strings.isEmpty(carSearchParams.origin)) {
					setPickupLocation(topSuggestion);
				}
			}
			clearFocus();
		}
		return false;
	}

	public void clearFocus() {
		pickupLocation.clearFocus();
		InputMethodManager imm = (InputMethodManager) pickupLocation.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(pickupLocation.getWindowToken(), 0);
	}

	public void saveHistory() {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				Type type = new TypeToken<ArrayList<Suggestion>>() {
				}.getType();
				String suggestionJson = new Gson().toJson(mRecentCarsLocationsSearches, type);
				try {
					IoUtils.writeStringToFile(RECENT_ROUTES_CARS_LOCATION_FILE, suggestionJson, getContext());
				}
				catch (IOException e) {
					Log.e("Save History Error: ", e);
				}
			}
		})).start();
	}

	private void loadHistory() {

		mRecentCarsLocationsSearches = new ArrayList<Suggestion>();
		try {
			String str = IoUtils.readStringFromFile(RECENT_ROUTES_CARS_LOCATION_FILE, getContext());
			Type type = new TypeToken<ArrayList<Suggestion>>() {
			}.getType();
			mRecentCarsLocationsSearches = new Gson().fromJson(str, type);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		suggestionAdapter.addAll(mRecentCarsLocationsSearches);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				pickupLocation.showDropDown();
			}
		}, 300);
	}

	public void setCalendarVisibility(int visibility) {
		calendarContainer.setVisibility(visibility);
	}

	// ICarDateTimeListener

	@Override
	public void onDateTimeChanged(CarSearchParamsBuilder.DateTimeBuilder dtb) {
		searchParamsBuilder.dateTimeBuilder(dtb);
		paramsChanged();
	}

	/*
	* States and stuff
	*/

	public static class CarParamsDefault {
	}

	public static class CarParamsCalendar {
	}

	private Presenter.Transition mOneToTwo = new Presenter.Transition(CarParamsDefault.class.getName(), CarParamsCalendar.class.getName()) {
		private int calendarHeight;

		@Override
		public void startTransition(boolean forward) {
			int parentHeight = getHeight();
			calendarHeight = calendarContainer.getHeight();
			selectDateButton.setEnabled(!forward);
			selectDateButton.setChecked(forward);
			float pos = forward ? parentHeight + calendarHeight : calendarHeight;
			calendarContainer.setTranslationY(pos);
			calendarContainer.setVisibility(View.VISIBLE);
		}

		@Override
		public void updateTransition(float f, boolean forward) {
			float pos = forward ? calendarHeight + (-f * calendarHeight) : (f * calendarHeight);
			calendarContainer.setTranslationY(pos);
		}

		@Override
		public void endTransition(boolean forward) {
			calendarContainer.setTranslationY(forward ? 0 : calendarHeight);
		}

		@Override
		public void finalizeTransition(boolean forward) {
			calendarContainer.setTranslationY(forward ? 0 : calendarHeight);
			if (forward) {
				Ui.hideKeyboard(CarSearchParamsWidget.this);
				pickupLocation.clearFocus();
			}
			setCalendarVisibility(View.VISIBLE);
		}
	};
}
