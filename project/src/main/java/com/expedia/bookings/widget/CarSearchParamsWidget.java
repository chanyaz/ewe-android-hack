package com.expedia.bookings.widget;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.Log;
import com.mobiata.android.time.widget.CalendarPicker;
import com.mobiata.android.time.widget.DaysOfWeekView;
import com.mobiata.android.time.widget.MonthView;
import com.mobiata.android.util.IoUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarSearchParamsWidget extends RelativeLayout implements
	CalendarPicker.DateSelectionChangedListener, CalendarPicker.YearMonthDisplayedChangedListener,
	SeekBar.OnSeekBarChangeListener, DaysOfWeekView.DayOfWeekRenderer,
	EditText.OnEditorActionListener {

	private static final String TOOLTIP_PATTERN = "hh:mm aa";
	private static final String TOOLTIP_DATE_PATTERN = "MMM dd";
	private DateTimeFormatter df = DateTimeFormat.forPattern(TOOLTIP_DATE_PATTERN);
	private int height = Ui.getScreenSize(getContext()).y;
	private ArrayList<Suggestion> mRecentCarsLocationsSearches;
	// We keep a separate (but equal) recents list for routes-based searches
	// because it's slightly different (e.g., no description)
	private static final String RECENT_ROUTES_CARS_LOCATION_FILE = "recent-cars-airport-routes-list.dat";
	private static final int RECENT_MAX_SIZE = 3;

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

	@InjectView(R.id.title)
	TextView txtTitle;

	@InjectView(R.id.select_date)
	ToggleButton selectDateButton;

	@InjectView(R.id.backout_btn)
	ImageButton backButton;

	@InjectView(R.id.search_btn)
	ImageButton searchButton;

	@InjectView(R.id.calendar_shadow)
	View calendarShadow;

	@InjectView(R.id.slider_shadow)
	View sliderShadow;

	@InjectView(R.id.calendar)
	CalendarPicker calendar;

	@InjectView(R.id.days_of_week)
	DaysOfWeekView daysOfWeekView;

	@InjectView(R.id.month)
	MonthView monthView;

	@InjectView(R.id.pickup_time_seek_bar)
	CarsTimeSlider pickupTimeSeekBar;

	@InjectView(R.id.dropoff_time_seek_bar)
	CarsTimeSlider dropoffTimeSeekBar;

	@InjectView(R.id.pickup_time_popup)
	TextView pickupTimePopup;

	@InjectView(R.id.pop_up_label)
	TextView popupLabel;

	@InjectView(R.id.pickup_time_popup_container)
	LinearLayout pickupTimePopupContainer;

	@InjectView(R.id.pickup_time_popup_text_container)
	LinearLayout pickupTimePopupContainerText;

	@InjectView(R.id.pickup_time_popup_tail)
	ImageView pickupTimePopupTail;

	@InjectView(R.id.slider_container)
	RelativeLayout sliderContainer;


	private CarSearchParamsBuilder searchParamsBuilder = new CarSearchParamsBuilder();
	private CarSearchParams carSearchParams;

	private CarSuggestionAdapter suggestionAdapter;

	private LocalDate lastStart;
	private LocalDate lastEnd;

	@OnClick(R.id.search_btn)
	public void startWidgetDownload() {
		if (isSearchFormFilled()) {
			Ui.hideKeyboard(this);
			Events.post(new Events.CarsNewSearchParams(carSearchParams));
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

		pickupLocation.setVisibility(View.VISIBLE);
		dropoffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		setCalendarVisibility(View.GONE);

		calendar.setSelectableDateRange(LocalDate.now(),
			LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)));
		calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_flight_search));
		calendar.setDateChangedListener(this);
		calendar.setYearMonthDisplayedChangedListener(this);

		daysOfWeekView.setDayOfWeekRenderer(this);
		daysOfWeekView.setTextColor(getContext().getResources().getColor(R.color.cars_calendar_week_color));
		daysOfWeekView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size));
		monthView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size));
		monthView.setTextEqualDatesColor(Color.WHITE);

		suggestionAdapter = new CarSuggestionAdapter(getContext(), R.layout.cars_dropdown_item);
		pickupLocation.setAdapter(suggestionAdapter);
		pickupLocation.setOnItemClickListener(mPickupListListener);
		pickupLocation.setOnFocusChangeListener(mPickupClickListener);
		pickupLocation.setOnEditorActionListener(this);

		Drawable drawableEnabled = getResources().getDrawable(R.drawable.location);
		drawableEnabled.setColorFilter(getResources().getColor(R.color.cars_secondary_color), PorterDuff.Mode.SRC_IN);
		pickupLocation.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);

		Drawable drawableDisabled = getResources().getDrawable(R.drawable.location).mutate();
		drawableDisabled
			.setColorFilter(getResources().getColor(R.color.cars_dropdown_disabled_stroke), PorterDuff.Mode.SRC_IN);
		dropoffLocation.setCompoundDrawablesWithIntrinsicBounds(drawableDisabled, null, null, null);

		NinePatchDrawable drawablePopUp = (NinePatchDrawable) getResources().getDrawable(R.drawable.toolbar_bg);
		drawablePopUp
			.setColorFilter(getResources().getColor(R.color.cars_tooltip_color), PorterDuff.Mode.SRC_IN);
		pickupTimePopupContainerText.setBackground(drawablePopUp);

		pickupTimeSeekBar.setProgress(new DateTime().plusHours(2).getHourOfDay() * 2);
		pickupTimeSeekBar.addOnSeekBarChangeListener(this);
		dropoffTimeSeekBar.setProgress(new DateTime().plusHours(2).getHourOfDay() * 2);
		dropoffTimeSeekBar.addOnSeekBarChangeListener(this);
		setupTextFont();
		loadHistory();
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
		calendar.setDateChangedListener(null);
		super.onDetachedFromWindow();
	}

	@OnClick(R.id.select_date)
	public void onPickupDateTimeClicked() {
		Ui.hideKeyboard(this);
		setCalendarVisibility(View.VISIBLE);
		selectDateButton.setEnabled(false);
		pickupLocation.clearFocus();
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
				selectDateButton.setChecked(false);
				selectDateButton.setEnabled(true);
				setCalendarVisibility(View.GONE);
				pickupTimePopupContainer.setVisibility(View.GONE);
				pickupLocation.setText("");
				searchParamsBuilder.origin("");
				paramsChanged();

			}
		}
	};

	/**
	 * Interfaces
	 */

	// Calendar
	@Override
	public void onYearMonthDisplayed(YearMonth yearMonth) {
		pickupTimePopupContainer.setVisibility(View.GONE);
	}

	@Override
	public void onDateSelectionChanged(final LocalDate start, final LocalDate end) {
		searchParamsBuilder.startDate(start);
		searchParamsBuilder.endDate(end);
		searchParamsBuilder.startMillis(convertProgressToMillis(pickupTimeSeekBar.getProgress()));
		if (end != null) {
			searchParamsBuilder.endMillis(convertProgressToMillis(dropoffTimeSeekBar.getProgress()));
		}

		paramsChanged();

		new Handler().postDelayed(new Runnable() {
			public void run() {
				drawCalendarTooltip(start, end);
			}
		}, 50);
	}

	// SeekBar

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		drawSliderTooltip(seekBar);
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

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		drawSliderTooltip(seekBar);
		animateToolTip(pickupTimePopupContainer);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		pickupTimePopupContainer.setVisibility(View.GONE);
	}

	public void drawSliderTooltip(SeekBar seekBar) {

		String title = ((CarsTimeSlider) seekBar).calculateProgress(seekBar.getProgress(), TOOLTIP_PATTERN);
		String subtitle = seekBar.getId() == R.id.pickup_time_seek_bar ? getContext().getResources()
			.getString(R.string.cars_time_slider_pick_up_label)
			: getContext().getResources().getString(R.string.cars_time_slider_drop_off_label);
		pickupTimePopup.setText(title);
		popupLabel.setText(subtitle);

		Rect thumbRect = seekBar.getThumb().getBounds();
		int[] location = new int[2];
		seekBar.getLocationOnScreen(location);
		final int x = thumbRect.centerX() + seekBar.getLeft();
		final int y = location[1] - ((CarsTimeSlider) seekBar).getTooltipOffset();

		pickupTimePopupContainer.setVisibility(View.VISIBLE);
		ViewTreeObserver vto = pickupTimePopupContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				pickupTimePopupContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

				p.setMargins(x - pickupTimePopupContainer.getMeasuredWidth() / 2,
					y - pickupTimePopupContainer.getMeasuredHeight(), 0, 0);
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pickupTimePopupTail.getLayoutParams();
				lp.gravity = Gravity.CENTER;
				pickupTimePopupTail.setLayoutParams(lp);
				pickupTimePopupContainer.setLayoutParams(p);
			}
		});
	}

	public void drawCalendarTooltip(final LocalDate start, final LocalDate end) {
		String title = end == null ? df.print(start) : df.print(start) + " - " + df.print(end);
		String subtitle = end == null ? getContext().getResources().getString(R.string.cars_calendar_start_date_label)
			: getContext().getResources().getString(R.string.cars_calendar_end_date_label);
		pickupTimePopup.setText(title);
		popupLabel.setText(subtitle);

		final boolean animate = pickupTimePopupContainer.getVisibility() == GONE;

		pickupTimePopupContainer.setVisibility(View.INVISIBLE);
		ViewTreeObserver vto = pickupTimePopupContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				pickupTimePopupContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);

				Point point = lastStart != start ? monthView.getStartDayCoordinates()
					: lastEnd != end ? monthView.getEndDayCoordinates() : monthView.getStartDayCoordinates();
				lastStart = start;
				lastEnd = end;

				int min = calendar.getRight() - pickupTimePopupContainer.getMeasuredWidth();
				int max = point.x - (pickupTimePopupContainer.getMeasuredWidth() / 2);

				int x = Math.min(min, Math.max(0, max));
				int y = point.y + calendar.getTop() - (int) monthView.getRadius() - 10;

				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pickupTimePopupTail.getLayoutParams();
				lp.gravity = x == 0 ? Gravity.LEFT : x == min ? Gravity.RIGHT : Gravity.CENTER;
				pickupTimePopupTail.setLayoutParams(lp);

				p.setMargins(x, y, 0, 0);
				pickupTimePopupContainer.setLayoutParams(p);
				pickupTimePopupContainer.setVisibility(View.VISIBLE);
				if (animate) {
					animateToolTip(pickupTimePopupContainer);
				}
			}
		});

	}

	private void animateToolTip(View v) {
		ScaleAnimation animation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
			Animation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		v.startAnimation(animation);
	}

	private void translateViewY(View v, float y) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationY", y);
		animator.setDuration(400);
		animator.setInterpolator(new DecelerateInterpolator());
		animator.start();
	}

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

	private static int convertProgressToMillis(int progress) {
		return progress * (30 * 60 * 1000);
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
		int y = visibility == View.GONE ? height : 0;
		translateViewY(calendarShadow, y);
		translateViewY(calendar, y);
		translateViewY(sliderShadow, y);
		translateViewY(sliderContainer, y);
	}

	private void setupTextFont() {
		Typeface robotoRegular = Typeface.create("sans-serif", Typeface.NORMAL);
		Typeface robotoMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
		Typeface robotoLight = Typeface.create("sans-serif-light", Typeface.NORMAL);
		txtTitle.setTypeface(robotoMedium);
		pickupLocation.setTypeface(robotoRegular);
		dropoffLocation.setTypeface(robotoRegular);
		calendar.setMonthHeaderTypeface(robotoRegular);
		daysOfWeekView.setTypeface(robotoLight);
		monthView.setDaysTypeface(robotoLight);
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
}
