package com.expedia.bookings.widget;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

public class CarSearchPresenter extends Presenter
	implements EditText.OnEditorActionListener, CarDateTimeWidget.ICarDateTimeListener {

	private static final String TOOLTIP_DATE_PATTERN = "MMM dd";
	private DateTimeFormatter df = DateTimeFormat.forPattern(TOOLTIP_DATE_PATTERN);
	private ArrayList<Suggestion> mRecentCarsLocationsSearches;
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
	AutoCompleteTextView pickUpLocation;

	@InjectView(R.id.search_container)
	ViewGroup searchContainer;

	@InjectView(R.id.dropoff_location)
	TextView dropOffLocation;

	@InjectView(R.id.select_date)
	ToggleButton selectDateButton;

	@InjectView(R.id.calendar_container)
	CarDateTimeWidget calendarContainer;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

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

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp);
		navIcon.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setTitle(getResources().getString(R.string.dates_and_location));
		toolbar.setTitleTextColor(getResources().getColor(R.color.cars_actionbar_text_color));
		toolbar.inflateMenu(R.menu.cars_search_menu);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.menu_check:
					if (isSearchFormFilled()) {
						Ui.hideKeyboard(CarSearchPresenter.this);
						Events.post(new Events.CarsNewSearchParams(carSearchParams));
					}
					break;
				}
				return false;
			}
		});
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});
		MenuItem item = toolbar.getMenu().findItem(R.id.menu_check);

		Drawable drawableAction = getResources().getDrawable(R.drawable.ic_check_white_24dp);
		drawableAction
			.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		item.setIcon(drawableAction);

		pickUpLocation.setVisibility(View.VISIBLE);
		dropOffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		setCalendarVisibility(View.INVISIBLE);

		suggestionAdapter = new CarSuggestionAdapter(getContext(), R.layout.cars_dropdown_item);
		Ui.getApplication(getContext()).carComponent().inject(suggestionAdapter);

		pickUpLocation.setAdapter(suggestionAdapter);
		pickUpLocation.setOnItemClickListener(mPickupListListener);
		pickUpLocation.setOnFocusChangeListener(mPickupClickListener);
		pickUpLocation.setOnEditorActionListener(this);
		pickUpLocation.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.location);
		drawableEnabled.setColorFilter(getResources().getColor(R.color.cars_secondary_color), PorterDuff.Mode.SRC_IN);
		pickUpLocation.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);

		Drawable drawableDisabled = getResources().getDrawable(R.drawable.location).mutate();
		drawableDisabled
			.setColorFilter(getResources().getColor(R.color.cars_dropdown_disabled_stroke), PorterDuff.Mode.SRC_IN);
		dropOffLocation.setCompoundDrawablesWithIntrinsicBounds(drawableDisabled, null, null, null);

		loadHistory();

		addTransition(defaultToCal);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = getContext().getResources().getColor(R.color.cars_primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color));
		}

		show(new CarParamsDefault());

	}

	private void setPickUpLocation(final Suggestion suggestion) {
		pickUpLocation.setText(StrUtils.formatCityName(suggestion.fullName));
		searchParamsBuilder.origin(suggestion.airportCode);
		searchParamsBuilder.originDescription(StrUtils.formatAirportName(suggestion.fullName));
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

		selectDateButton.setChecked(true);
		show(new CarParamsCalendar());
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
			Ui.hideKeyboard(CarSearchPresenter.this);
			clearFocus();
			Suggestion suggestion = suggestionAdapter.getItem(position);
			setPickUpLocation(suggestion);
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
				pickUpLocation.setText("");
				searchParamsBuilder.origin("");
				searchParamsBuilder.originDescription("");
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
					setPickUpLocation(topSuggestion);
				}
				else if (Strings.isEmpty(carSearchParams.origin)) {
					setPickUpLocation(topSuggestion);
				}
			}
			clearFocus();
		}
		return false;
	}

	public void clearFocus() {
		pickUpLocation.clearFocus();
		InputMethodManager imm = (InputMethodManager) pickUpLocation.getContext()
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(pickUpLocation.getWindowToken(), 0);
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
				pickUpLocation.showDropDown();
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
				Ui.hideKeyboard(CarSearchPresenter.this);
				pickUpLocation.clearFocus();
			}
			setCalendarVisibility(View.VISIBLE);
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
}
