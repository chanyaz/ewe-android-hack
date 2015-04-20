package com.expedia.bookings.presenter;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.SuggestionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.expedia.bookings.widget.CarDateTimeWidget;
import com.expedia.bookings.widget.CarSuggestionAdapter;
import com.google.gson.Gson;
import com.mobiata.android.time.widget.CalendarPicker;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarSearchPresenter extends Presenter
	implements EditText.OnEditorActionListener, CarDateTimeWidget.ICarDateTimeListener {

	private ArrayList<Suggestion> mRecentCarsLocationsSearches = new ArrayList<>();
	private static final String RECENT_ROUTES_CARS_LOCATION_FILE = "recent-cars-airport-routes-list.dat";
	private static final int RECENT_MAX_SIZE = 3;

	public CarSearchPresenter(Context context) {
		this(context, null);
	}

	public CarSearchPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private CarSearchParamsBuilder searchParamsBuilder = new CarSearchParamsBuilder();
	private CarSearchParams carSearchParams;

	private CarSuggestionAdapter suggestionAdapter;

	@InjectView(R.id.pickup_location)
	AlwaysFilterAutoCompleteTextView pickUpLocation;

	@InjectView(R.id.search_container)
	ViewGroup searchContainer;

	@InjectView(R.id.dropoff_location)
	TextView dropOffLocation;

	@InjectView(R.id.select_date)
	ToggleButton selectDateButton;

	@InjectView(R.id.calendar_container)
	CarDateTimeWidget calendarContainer;

	@InjectView(R.id.calendar)
	CalendarPicker calendar;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	Button searchButton;

	@OnClick(R.id.pickup_location)
	public void onPickupEditClicked() {
		if (getCurrentState() != null && !getCurrentState().equals(CarParamsDefault.class.getName())) {
			back();
		}
	}

	@OnClick(R.id.dropoff_location)
	public void displayAlertForDropOffLocationClick() {
		showAlertMessage(R.string.drop_off_same_as_pick_up, R.string.ok);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		OmnitureTracking.trackAppCarSearchBox(getContext());
		calendarContainer.setCarDateTimeListener(this);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp);
		navIcon.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setTitle(getResources().getString(R.string.dates_and_location));
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setTitleTextColor(getResources().getColor(R.color.cars_actionbar_text_color));
		toolbar.inflateMenu(R.menu.cars_search_menu);

		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearBackStack();
				((Activity) getContext()).onBackPressed();
			}
		});
		MenuItem item = toolbar.getMenu().findItem(R.id.menu_check);
		searchButton = setupToolBarCheckmark(item);
		setUpSearchButton();

		pickUpLocation.setVisibility(View.VISIBLE);
		dropOffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		setCalendarVisibility(View.INVISIBLE);

		suggestionAdapter = new CarSuggestionAdapter();
		Ui.getApplication(getContext()).carComponent().inject(suggestionAdapter);

		pickUpLocation.setAdapter(suggestionAdapter);
		pickUpLocation.setOnItemClickListener(mPickupListListener);
		pickUpLocation.setOnFocusChangeListener(mPickupClickListener);
		pickUpLocation.setOnEditorActionListener(this);
		pickUpLocation.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.location).mutate();
		drawableEnabled.setColorFilter(getResources().getColor(R.color.cars_secondary_color), PorterDuff.Mode.SRC_IN);
		pickUpLocation.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);

		Drawable drawableDisabled = getResources().getDrawable(R.drawable.location).mutate();
		drawableDisabled
			.setColorFilter(getResources().getColor(R.color.search_dropdown_disabled_stroke), PorterDuff.Mode.SRC_IN);
		dropOffLocation.setCompoundDrawablesWithIntrinsicBounds(drawableDisabled, null, null, null);

		addTransition(defaultToCal);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = getContext().getResources().getColor(R.color.cars_status_bar_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color));
		}

		show(new CarParamsDefault());

		Gson gson = CarServices.generateGson();
		String carSearchParamsString = ((Activity) getContext()).getIntent()
			.getStringExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS);
		CarSearchParams carSearchParams = gson.fromJson(carSearchParamsString, CarSearchParams.class);

		if (carSearchParams != null) {
			CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder()
				.startDate(carSearchParams.startDateTime.toLocalDate())
				.endDate(carSearchParams.endDateTime.toLocalDate());
			searchParamsBuilder.dateTimeBuilder(dateTimeBuilder);
			Suggestion suggestion = new Suggestion();
			suggestion.airportCode = carSearchParams.origin;
			suggestion.fullName = suggestion.shortName = suggestion.displayName = carSearchParams.originDescription;
			setPickUpLocation(suggestion, false);
			calendar.setSelectedDates(carSearchParams.startDateTime.toLocalDate(),
				carSearchParams.endDateTime.toLocalDate());
		}
		else {
			loadHistory();
		}
	}

	public Button setupToolBarCheckmark(final MenuItem menuItem) {
		Button tv = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSearchFormFilled()) {
					Ui.hideKeyboard(CarSearchPresenter.this);
					Events.post(new Events.CarsNewSearchParams(carSearchParams));
				}
			}
		});
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.cars_primary_color), PorterDuff.Mode.SRC_IN);
		tv.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(tv);
		return tv;
	}

	private void setPickUpLocation(final Suggestion suggestion) {
		setPickUpLocation(suggestion, true);
	}

	private void setPickUpLocation(final Suggestion suggestion, final boolean filter) {
		Suggestion suggest = suggestion.clone();
		pickUpLocation.setText(StrUtils.formatCityName(suggest.fullName), filter);
		searchParamsBuilder.origin(suggest.airportCode);
		searchParamsBuilder.originDescription(StrUtils.formatAirport(suggest));
		paramsChanged();
		suggest.iconType = Suggestion.IconType.HISTORY_ICON;
		// Remove duplicates
		Iterator<Suggestion> it = mRecentCarsLocationsSearches.iterator();
		while (it.hasNext()) {
			Suggestion s = it.next();
			if (s.airportCode.equalsIgnoreCase(suggest.airportCode)) {
				it.remove();
			}
		}

		if (mRecentCarsLocationsSearches.size() >= RECENT_MAX_SIZE) {
			mRecentCarsLocationsSearches.remove(RECENT_MAX_SIZE - 1);
		}

		mRecentCarsLocationsSearches.add(0, suggest);
		//Have to remove the bold tag in display name so text for last search is normal
		suggest.displayName = Html.fromHtml(suggest.displayName).toString();
		// Save
		saveHistory();
		suggestionAdapter.updateRecentHistory(mRecentCarsLocationsSearches);

		selectDateButton.setChecked(true);
		show(new CarParamsCalendar());

		setUpSearchButton();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (suggestionAdapter != null) {
			suggestionAdapter.cleanup();
		}
		super.onDetachedFromWindow();
	}

	@OnClick(R.id.select_date)
	public void onPickupDateTimeClicked() {
		if (!searchParamsBuilder.hasOrigin()) {
			AnimUtils.doTheHarlemShake(pickUpLocation);
			selectDateButton.setChecked(false);
			return;
		}
		show(new CarParamsCalendar());
	}

	/*
	 * Error handling
	 */

	public void showAlertMessage(int messageResourceId, int confirmButtonResourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(getContext());
		b.setCancelable(false)
			.setMessage(messageResourceId)
			.setPositiveButton(confirmButtonResourceId, new DialogInterface.OnClickListener() {
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
			if (!searchParamsBuilder.hasOrigin()) {
				AnimUtils.doTheHarlemShake(pickUpLocation);
			}
			else if (!searchParamsBuilder.hasStartAndEndDates()) {
				AnimUtils.doTheHarlemShake(calendar);
			}
		}
		return areRequiredParamsFilled;
	}

	/*
	 * Pickup edit text helpers
	 */

	private AdapterView.OnItemClickListener mPickupListListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			hidePickupDropdown();
			Suggestion suggestion = suggestionAdapter.getItem(position);
			setPickUpLocation(suggestion);
		}
	};

	private OnFocusChangeListener mPickupClickListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (getCurrentState() != null && !getCurrentState().equals(CarParamsDefault.class.getName())) {
					back();
				}
				selectDateButton.setChecked(false);
				selectDateButton.setEnabled(true);
				pickUpLocation.setText("");
				searchParamsBuilder.origin("");
				searchParamsBuilder.originDescription("");
				paramsChanged();
				setUpSearchButton();
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
					setPickUpLocation(topSuggestion);
				}
				else if (Strings.isEmpty(carSearchParams.origin)) {
					setPickUpLocation(topSuggestion);
				}
			}
			hidePickupDropdown();
		}
		return false;
	}

	private void hidePickupDropdown() {
		pickUpLocation.clearFocus();
		Ui.hideKeyboard(pickUpLocation);
	}

	private void saveHistory() {
		SuggestionUtils
			.saveSuggestionHistory(getContext(), mRecentCarsLocationsSearches, RECENT_ROUTES_CARS_LOCATION_FILE);
	}

	private void loadHistory() {
		mRecentCarsLocationsSearches = SuggestionUtils.loadSuggestionHistory(getContext(),
			RECENT_ROUTES_CARS_LOCATION_FILE);
		suggestionAdapter.addNearbyAndRecents(mRecentCarsLocationsSearches, getContext());
		postDelayed(new Runnable() {
			public void run() {
				if (ExpediaBookingApp.sIsAutomation) {
					return;
				}
				pickUpLocation.requestFocus();
				Ui.showKeyboard(pickUpLocation, null);
			}
		}, 300);
	}

	public void setCalendarVisibility(int visibility) {
		calendarContainer.setVisibility(visibility);
	}

	@Override
	public void onDateTimeChanged(CarSearchParamsBuilder.DateTimeBuilder dtb) {
		searchParamsBuilder.dateTimeBuilder(dtb);
		paramsChanged();
		setUpSearchButton();
	}

	/*
	* States and stuff
	*/

	public static class CarParamsDefault {
	}

	public static class CarParamsCalendar {
	}

	private Presenter.Transition defaultToCal = new Presenter.Transition(CarParamsDefault.class,
		CarParamsCalendar.class) {
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
				hidePickupDropdown();
			}
			setCalendarVisibility(View.VISIBLE);
			if (forward && searchParamsBuilder.areRequiredParamsFilled()) {
				calendarContainer.onDateSelectionChanged(carSearchParams.startDateTime.toLocalDate(),
					carSearchParams.endDateTime.toLocalDate());
			}
		}
	};

	public void animationStart(boolean forward) {
		searchContainer.setTranslationY(forward ? searchContainer.getHeight() : 0);
		toolbar.setTranslationY(forward ? -toolbar.getHeight() : 0);
	}

	public void animationUpdate(float f, boolean forward) {
		float translation = forward ? searchContainer.getHeight() * (1 - f) : searchContainer.getHeight() * f;
		searchContainer.setTranslationY(translation);
		toolbar.setTranslationY(forward ? -toolbar.getHeight() * (1 - f) : -toolbar.getHeight() * f);
	}

	public void animationFinalize(boolean forward) {
		searchContainer.setTranslationY(0);
		toolbar.setTranslationY(forward ? 0 : -toolbar.getHeight());
	}

	public void setUpSearchButton() {
		if (searchParamsBuilder.areRequiredParamsFilled()) {
			searchButton.setAlpha(1f);
		}
		else {
			searchButton.setAlpha(.7f);
		}
	}
}
