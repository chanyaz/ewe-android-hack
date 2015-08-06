package com.expedia.bookings.presenter.lx;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.SuggestionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.expedia.bookings.widget.LxSuggestionAdapter;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.time.widget.DaysOfWeekView;
import com.mobiata.android.time.widget.MonthView;
import com.squareup.otto.Subscribe;

import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class LXSearchParamsPresenter extends Presenter
	implements EditText.OnEditorActionListener, CalendarPicker.DateSelectionChangedListener, DaysOfWeekView.DayOfWeekRenderer {

	private static final int RECENT_MAX_SIZE = 3;

	@InjectView(R.id.search_calendar)
	CalendarPicker calendarPicker;

	@InjectView(R.id.search_location)
	AlwaysFilterAutoCompleteTextView location;

	@InjectView(R.id.select_dates)
	ToggleButton selectDates;

	@InjectView(R.id.calendar_container)
	View calendarContainer;

	@InjectView(R.id.search_container)
	ViewGroup searchContainer;

	@InjectView(R.id.search_params_container)
	ViewGroup searchParamsContainer;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.days_of_week)
	DaysOfWeekView daysOfWeekView;

	@InjectView(R.id.month)
	MonthView monthView;

	@InjectView(R.id.toolbar_search_text)
	android.widget.TextView toolBarSearchText;

	@InjectView(R.id.toolbar_detail_text)
	android.widget.TextView toolBarDetailText;

	@InjectView(R.id.toolbar_subtitle_text)
	android.widget.TextView toolBarSubtitleText;

	@InjectView(R.id.toolbar_two)
	LinearLayout toolbarTwo;

	Button searchButton;

	private View statusBar;
	private int searchParamsContainerHeight;
	private int searchTop;

	LXSearchParams searchParams = new LXSearchParams();
	private LxSuggestionAdapter suggestionAdapter;

	private ArrayList<Suggestion> mRecentLXLocationsSearches;
	private ArrowXDrawable navIcon;

	public LXSearchParamsPresenter(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.lx_search_params_presenter, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		setupCalendar();
		suggestionAdapter = new LxSuggestionAdapter();
		Ui.getApplication(getContext()).lxComponent().inject(suggestionAdapter);

		setupToolbar();
		setUpSearchButton();
		location.setAdapter(suggestionAdapter);
		location.setOnItemClickListener(mLocationListListener);
		location.setOnEditorActionListener(this);
		location.setOnFocusChangeListener(mLocationFocusListener);
		location.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		selectDates.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		addTransition(defaultToCal);
		show(new LXParamsDefault());

		loadHistory();

		searchParamsContainer.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					searchParamsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					searchParamsContainerHeight = searchParamsContainer.getMeasuredHeight();

					// Set the dropdown height to size of 3 suggestions.
					location.setDropDownHeight(3 * (int) getResources().getDimension(R.dimen.suggestion_list_height));
				}
			});

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
				if (getCurrentState() != null && !getCurrentState().equals(LXParamsDefault.class.getName())) {
					back();
				}
				selectDates.setChecked(false);
				selectDates.setEnabled(true);
				location.setText("");
				searchParams.location("");
				searchParamsChanged();
			}
		}
	};

	private void setSearchLocation(final Suggestion suggestion) {
		location.setText(StrUtils.formatCityName(suggestion.fullName));
		searchParams.location(suggestion.fullName);
		searchParamsChanged();

		selectDates.setChecked(true);
		show(new LXParamsCalendar());

		Suggestion suggest = suggestion.clone();
		suggest.iconType = Suggestion.IconType.HISTORY_ICON;
		// Remove duplicates
		Iterator<Suggestion> it = mRecentLXLocationsSearches.iterator();
		while (it.hasNext()) {
			Suggestion s = it.next();
			if (s.fullName.equalsIgnoreCase(suggest.fullName)) {
				it.remove();
			}
		}

		if (mRecentLXLocationsSearches.size() >= RECENT_MAX_SIZE) {
			mRecentLXLocationsSearches.remove(RECENT_MAX_SIZE - 1);
		}

		mRecentLXLocationsSearches.add(0, suggest);
		//Have to remove the bold tag in display name so text for last search is normal
		suggest.displayName = Html.fromHtml(suggest.displayName).toString();
		// Save
		SuggestionUtils.saveSuggestionHistory(getContext(), mRecentLXLocationsSearches, SuggestionUtils.RECENT_ROUTES_LX_LOCATION_FILE);
		suggestionAdapter.updateRecentHistory(mRecentLXLocationsSearches);

	}

	private void loadHistory() {
		mRecentLXLocationsSearches = SuggestionUtils.loadSuggestionHistory(getContext(), SuggestionUtils.RECENT_ROUTES_LX_LOCATION_FILE);
		suggestionAdapter.addNearbyAndRecents(mRecentLXLocationsSearches, getContext());
	}

	@OnCheckedChanged(R.id.select_dates)
	public void onDateCheckedChanged(boolean isChecked) {
		Drawable drawableEnabled = getResources().getDrawable(R.drawable.date).mutate();
		drawableEnabled.setColorFilter(isChecked ? Color.WHITE
			: getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_lxUncheckedToggleTextColor)),
			PorterDuff.Mode.SRC_IN);
		selectDates.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
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
		if (!searchParams.hasLocation()) {
			AnimUtils.doTheHarlemShake(location);
			selectDates.setChecked(false);
			return;
		}
		selectDates.setChecked(true);
		show(new LXParamsCalendar());
	}

	private boolean validateSearchInput() {
		if (!searchParams.hasLocation()) {
			AnimUtils.doTheHarlemShake(location);
			return false;
		}
		else if (!searchParams.hasStartDate()) {
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
		searchParams.startDate(start);
		searchParams.endDate(start.plusDays(getResources().getInteger(R.integer.lx_default_search_range)));
		searchParamsChanged();
		setUpSearchButton();
	}

	private void searchParamsChanged() {
		searchParams.searchType(SearchType.EXPLICIT_SEARCH);
		if (searchParams.hasStartDate()) {
			String dateText = DateUtils.localDateToMMMMd(searchParams.startDate);

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
		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.inflateMenu(R.menu.lx_search_menu);
		MenuItem item = toolbar.getMenu().findItem(R.id.menu_search);
		setupToolBarCheckmark(item);

		toolBarSearchText.setText(getResources().getString(R.string.lx_search_title));
		toolbar.setTitleTextColor(Color.WHITE);
		toolbar
			.setBackgroundColor(getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_lxPrimaryColor)));
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getCurrentState() != null && !getCurrentState().equals(LXParamsDefault.class.getName())) {
					clearBackStack();
				}
				((Activity) getContext()).onBackPressed();
			}
		});

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int toolbarColor = getContext().getResources()
				.getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_lxPrimaryColor));
			statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, toolbarColor);
			addView(statusBar);
		}
	}

	private void setupToolBarCheckmark(final MenuItem menuItem) {
		searchButton = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		searchButton.setTextColor(Color.WHITE);
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
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		searchButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(searchButton);
	}

	private void setupCalendar() {
		calendarContainer.setVisibility(View.INVISIBLE);
		calendarPicker.setSelectableDateRange(LocalDate.now(),
			LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_days_lx_search)));
		calendarPicker.setDateChangedListener(this);
		daysOfWeekView.setDayOfWeekRenderer(this);
		daysOfWeekView.setMaxTextSize(getResources().getDimension(R.dimen.lx_calendar_month_view_max_text_size));
		monthView.setMaxTextSize(getResources().getDimension(R.dimen.lx_calendar_month_view_max_text_size));
		monthView.setTextEqualDatesColor(Color.WHITE);
		calendarPicker.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		daysOfWeekView.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT));
		monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM));
		// End date selection is disabled.
		calendarPicker.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_selection_date_range_lx));
	}

	// States and transitions
	public static class LXParamsDefault {
	}

	public static class LXParamsCalendar {
	}

	@Subscribe
	public void onShowSearchWidget(Events.LXShowSearchWidget event) {
		// Search Box Omniture Tracking on load of search param screen.
		OmnitureTracking.trackAppLXSearchBox();

		show(new LXParamsDefault());
		if (!Strings.isEmpty(location.getText())) {
			show(new LXParamsCalendar());
		}
	}

	@Subscribe
	public void onNewSearch(Events.LXNewSearch event) {
		location.setText(event.locationName);
		calendarPicker.setSelectedDates(event.startDate, null);
		searchParams.location(event.locationName);
		searchParams.startDate(event.startDate);
		searchParams.endDate(event.endDate);
		show(new LXParamsCalendar());
		searchParamsChanged();
		setUpSearchButton();
	}

	private Presenter.Transition defaultToCal = new Presenter.Transition(LXParamsDefault.class, LXParamsCalendar.class) {
		private int calendarHeight;

		@Override
		public void startTransition(boolean forward) {
			int parentHeight = getHeight();
			calendarHeight = calendarContainer.getHeight();
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
		if (searchParams.hasLocation() && searchParams.hasStartDate()) {
			searchButton.setAlpha(1f);
		}
		else {
			searchButton.setAlpha(.15f);
		}
	}

	public void animationStart(boolean forward) {
		calendarContainer.setTranslationY(forward ? searchContainer.getHeight() : 0);
		searchContainer.setBackgroundColor(Color.TRANSPARENT);
		toolBarSearchText.setAlpha(forward ? 0 : 1);
		searchButton.setAlpha(forward ? 0 : 1);
		searchTop = toolbarTwo.getTop() -  searchContainer.getTop();
		searchParamsContainer.setAlpha(1f);
		if (statusBar != null) {
			statusBar.setAlpha(1f);
		}
	}

	public void animationUpdate(float f, boolean forward, float alpha) {
		float translation = forward ? searchContainer.getHeight() * (1 - f) : searchContainer.getHeight() * f;
		float yTrans = forward ?  - (searchTop * (1 - f)) : - (searchTop * f);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) searchParamsContainer.getLayoutParams();
		layoutParams.height = forward ? (int) (f * (searchParamsContainerHeight)) : (int) (Math.abs(f - 1) * (searchParamsContainerHeight));
		searchParamsContainer.setLayoutParams(layoutParams);

		searchParamsContainer.setAlpha(forward ? alpha + ((1f - alpha) * f) : Math.abs(1 - f) + alpha);
		if (statusBar != null) {
			statusBar.setAlpha(forward ? alpha + ((1f - alpha) * f) : Math.abs(1 - f) + alpha);
		}

		calendarContainer.setTranslationY(translation);
		toolBarSearchText.setTranslationY(yTrans);
		toolBarSearchText.setAlpha(forward ? f : Math.abs(1 - f));
		searchButton.setAlpha(forward ? f : Math.abs(1 - f));
		toolbar.setAlpha(forward ? f : Math.abs(1 - f));
		navIcon.setParameter(forward ? f : Math.abs(1 - f));
	}

	public void animationFinalize(boolean forward) {
		calendarContainer.setTranslationY(0);
		searchContainer.setBackgroundColor(Color.WHITE);
		toolBarSearchText.setTranslationY(0);
		if (forward && Strings.isEmpty(location.getText())) {
			postDelayed(new Runnable() {
				public void run() {
					location.requestFocus();
					Ui.showKeyboard(location, null);
				}
			}, 300);
		}
		else {
			Ui.hideKeyboard(LXSearchParamsPresenter.this);
		}
		navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.CLOSE.getType());
	}
}
