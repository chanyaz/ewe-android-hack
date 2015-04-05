package com.expedia.bookings.presenter.lx;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchParamsBuilder;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.LxSuggestionAdapter;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.time.widget.DaysOfWeekView;
import com.mobiata.android.time.widget.MonthView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXSearchParamsPresenter extends Presenter
	implements EditText.OnEditorActionListener, CalendarPicker.DateSelectionChangedListener, DaysOfWeekView.DayOfWeekRenderer {

	@InjectView(R.id.search_calendar)
	CalendarPicker calendarPicker;

	@InjectView(R.id.search_location)
	AutoCompleteTextView location;

	@InjectView(R.id.select_dates)
	ToggleButton selectDates;

	@InjectView(R.id.calendar_container)
	View calendarContainer;

	@InjectView(R.id.search_params_container)
	ViewGroup searchParamContainer;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.days_of_week)
	DaysOfWeekView daysOfWeekView;

	@InjectView(R.id.month)
	MonthView monthView;

	Button searchButton;

	LXSearchParams searchParams;
	private LxSuggestionAdapter suggestionAdapter;

	private LXSearchParamsBuilder searchParamsBuilder = new LXSearchParamsBuilder();

	public LXSearchParamsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.lx_search_params_presenter, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// Search Box Omniture Tracking on load of search param screen.
		OmnitureTracking.trackAppLXSearchBox(getContext());
		ButterKnife.inject(this);

		setupCalendar();
		suggestionAdapter = new LxSuggestionAdapter();
		Ui.getApplication(getContext()).lxComponent().inject(suggestionAdapter);

		setupToolbar();
		setUpSearchButton();
		location.setAdapter(suggestionAdapter);
		location.setOnItemClickListener(mLocationListListener);
		location.setOnEditorActionListener(this);
		location.setOnFocusChangeListener(mLocationFocusListener);
		Drawable locationDrawable = getResources().getDrawable(R.drawable.location);
		locationDrawable.setColorFilter(getResources().getColor(R.color.lx_secondary_color), PorterDuff.Mode.SRC_IN);
		location.setCompoundDrawablesWithIntrinsicBounds(locationDrawable, null, null, null);
		addTransition(defaultToCal);
		show(new LXParamsDefault());
	}

	private AdapterView.OnItemClickListener mLocationListListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Ui.hideKeyboard(LXSearchParamsPresenter.this);
			clearFocus();
			Suggestion suggestion = suggestionAdapter.getItem(position);
			setSearchLocation(suggestion);
			setUpSearchButton();
		}
	};

	private OnFocusChangeListener mLocationFocusListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (!getCurrentState().equals(LXParamsDefault.class.getName())) {
					back();
				}
				selectDates.setChecked(false);
				selectDates.setEnabled(true);
				location.setText("");
				searchParamsBuilder.location("");
				searchParamsChanged();
			}
		}
	};

	private void setSearchLocation(final Suggestion suggestion) {
		location.setText(StrUtils.formatCityName(suggestion.fullName));
		searchParamsBuilder.location(suggestion.fullName);
		searchParamsChanged();

		selectDates.setChecked(true);
		show(new LXParamsCalendar());
	}

	@Override
	protected void onDetachedFromWindow() {
		if (suggestionAdapter != null) {
			suggestionAdapter.cleanup();
		}
		super.onDetachedFromWindow();
	}

	// Click events

	@OnClick(R.id.select_dates)
	public void showCalendar() {
		if (!searchParamsBuilder.hasLocation()) {
			AnimUtils.doTheHarlemShake(location);
			selectDates.setChecked(false);
			return;
		}
		show(new LXParamsCalendar());
		//calendarContainer.setVisibility(View.VISIBLE);
	}

	private boolean validateSearchInput() {
		if (!searchParamsBuilder.hasLocation()) {
			AnimUtils.doTheHarlemShake(location);
			return false;
		}
		else if (!searchParamsBuilder.hasStartDate()) {
			AnimUtils.doTheHarlemShake(calendarContainer);
			return false;
		}
		return true;
	}

	/*
	 * OnEditorActionListener
	 */

	@Override
	public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE && !Strings.isEmpty(v.getText())) {
			calendarContainer.setVisibility(View.VISIBLE);
			clearLocationFocus();
		}
		return false;
	}

	public void clearLocationFocus() {
		location.clearFocus();
		InputMethodManager imm = (InputMethodManager) location.getContext()
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(location.getWindowToken(), 0);
	}
	// Calendar

	@Override
	public void onDateSelectionChanged(LocalDate start, LocalDate end) {
		searchParamsBuilder.startDate(start);
		searchParamsBuilder.endDate(start.plusDays(getResources().getInteger(R.integer.lx_default_search_range)));
		searchParamsChanged();
		setUpSearchButton();
	}

	private void searchParamsChanged() {
		searchParams = searchParamsBuilder.build();
		if (searchParamsBuilder.startDate != null) {
			String dateText = DateUtils.localDateToMMMdd(searchParamsBuilder.startDate);

			selectDates.setText(dateText);
			selectDates.setTextOff(dateText);
			selectDates.setTextOn(dateText);
		}
	}

	@Override
	public String renderDayOfWeek(LocalDate.Property dayOfWeek) {
		if (Build.VERSION.SDK_INT >= 18) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEEEE", Locale.getDefault());
			return sdf.format(dayOfWeek.getLocalDate().toDate());
		}
		else if (Locale.getDefault().getLanguage().equals("en")) {
			return dayOfWeek.getAsShortText().toUpperCase(Locale.getDefault()).substring(0, 1);
		}
		return DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek);
	}

	public LXSearchParams getCurrentParams() {
		return searchParams;
	}

	private void setupToolbar() {
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp);
		navIcon.setColorFilter(getResources().getColor(R.color.lx_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.inflateMenu(R.menu.lx_search_menu);
		MenuItem item = toolbar.getMenu().findItem(R.id.menu_search);
		setupToolBarCheckmark(item);

		toolbar.setTitle(getResources().getString(R.string.search_widget_heading));
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int toolbarColor = getContext().getResources().getColor(R.color.lx_primary_color);
			addView(Ui.setUpStatusBar(getContext(), toolbar, searchParamContainer, toolbarColor));
		}
	}

	private void setupToolBarCheckmark(final MenuItem menuItem) {
		searchButton = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (validateSearchInput()) {
					Events.post(new Events.LXNewSearchParamsAvailable(searchParams));
					Ui.hideKeyboard(LXSearchParamsPresenter.this);
				}
			}
		});
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.lx_primary_color), PorterDuff.Mode.SRC_IN);
		searchButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(searchButton);
	}

	private void setupCalendar() {
		calendarContainer.setVisibility(View.INVISIBLE);
		calendarPicker.setSelectableDateRange(LocalDate.now(),
			LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_days_lx_search)));
		calendarPicker.setDateChangedListener(this);
		daysOfWeekView.setDayOfWeekRenderer(this);
		daysOfWeekView.setTextColor(getContext().getResources().getColor(R.color.lx_calendar_week_color));
		daysOfWeekView.setMaxTextSize(getResources().getDimension(R.dimen.lx_calendar_month_view_max_text_size));
		monthView.setMaxTextSize(getResources().getDimension(R.dimen.lx_calendar_month_view_max_text_size));
		monthView.setTextEqualDatesColor(Color.WHITE);
		calendarPicker.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		daysOfWeekView.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));
		monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));
	}

	// States and transitions
	public static class LXParamsDefault {
	}

	public static class LXParamsCalendar {
	}

	private Presenter.Transition defaultToCal = new Presenter.Transition(LXParamsDefault.class,
		LXParamsCalendar.class) {
		private int calendarHeight;

		@Override
		public void startTransition(boolean forward) {
			int parentHeight = getHeight();
			calendarHeight = calendarContainer.getHeight();
			selectDates.setEnabled(!forward);
			selectDates.setChecked(forward);
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
				Ui.hideKeyboard(LXSearchParamsPresenter.this);
				location.clearFocus();
			}
			calendarContainer.setVisibility(View.VISIBLE);
		}
	};

	public void setUpSearchButton() {
		if (searchParamsBuilder.hasLocation() && searchParamsBuilder.hasStartDate()) {
			searchButton.setAlpha(1f);
		}
		else {
			searchButton.setAlpha(.7f);
		}
	}
}
