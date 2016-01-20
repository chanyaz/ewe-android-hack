package com.expedia.bookings.presenter.car;

import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.DateTimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.expedia.account.graphics.ArrowXDrawable;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.presenter.Presenter;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.ArrowXDrawableUtil;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.SuggestionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.expedia.bookings.widget.CarDateTimeWidget;
import com.expedia.bookings.widget.CarSuggestionAdapter;
import com.expedia.bookings.widget.SuggestionBaseAdapter;
import com.mobiata.android.time.widget.CalendarPicker;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CarSearchPresenter extends Presenter
	implements EditText.OnEditorActionListener, CarDateTimeWidget.ICarDateTimeListener {

	private ArrayList<SuggestionV4> mRecentCarsLocationsSearches = new ArrayList<>();
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

	@InjectView(R.id.search_params_container)
	LinearLayout searchParamsContainer;

	@InjectView(R.id.search_container)
	ViewGroup searchContainer;

	@InjectView(R.id.location_image_view)
	ImageView locationImageView;

	@InjectView(R.id.dropoff_location)
	AlwaysFilterAutoCompleteTextView dropOffLocation;

	@InjectView(R.id.select_date)
	ToggleButton selectDateButton;

	@InjectView(R.id.calendar_container)
	CarDateTimeWidget calendarContainer;

	@InjectView(R.id.calendar)
	CalendarPicker calendar;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.toolbar_search_text)
	TextView toolBarSearchText;

	@InjectView(R.id.toolbar_detail_text)
	TextView toolBarDetailText;

	@InjectView(R.id.toolbar_subtitle_text)
	TextView toolBarSubtitleText;

	@InjectView(R.id.toolbar_two)
	LinearLayout toolbarTwo;

	Button searchButton;

	View statusBar;

	private int searchParamsContainerHeight;
	private int searchTop;
	private ArrowXDrawable navIcon;

	public void reset() {
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) searchParamsContainer.getLayoutParams();
		layoutParams.height = searchParamsContainerHeight;
		searchParamsContainer.setLayoutParams(layoutParams);
		toolBarSearchText.setAlpha(1f);
		searchParamsContainer.setAlpha(1f);
		toolbar.setAlpha(1f);
		setUpSearchButton();
	}

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
		OmnitureTracking.trackAppCarSearchBox();
		calendarContainer.setCarDateTimeListener(this);

		navIcon = ArrowXDrawableUtil
			.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE);
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolBarSearchText.setText(getResources().getString(R.string.toolbar_search_cars));
		toolbar.setBackgroundColor(
			getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_carsPrimaryColor)));
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
		onDateCheckedChanged(false);
		dropOffLocation.setVisibility(View.VISIBLE);
		selectDateButton.setVisibility(View.VISIBLE);
		setCalendarVisibility(View.INVISIBLE);

		suggestionAdapter = new CarSuggestionAdapter(getContext());
		Ui.getApplication(getContext()).carComponent().inject(suggestionAdapter);

		pickUpLocation.setAdapter(suggestionAdapter);
		pickUpLocation.setOnItemClickListener(mPickupListListener);
		pickUpLocation.setOnFocusChangeListener(mPickupFocusListener);
		pickUpLocation.setOnClickListener(mPickupClickListener);
		pickUpLocation.setOnEditorActionListener(this);
		pickUpLocation.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
		selectDateButton.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));

		addTransition(defaultToCal);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = getContext().getResources()
				.getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_carsPrimaryColor));
			statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color);
			addView(statusBar);
		}

		show(new CarParamsDefault());

		CarSearchParams carSearchParams = null;
		Intent intent = ((Activity) getContext()).getIntent();
		String carSearchParamsString = intent.getStringExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS);

		if (intent.getBooleanExtra(Codes.FROM_DEEPLINK, false)) {
			carSearchParams = CarDataUtils.getCarSearchParamsFromJSON(intent.getStringExtra("carSearchParams"));
			if (carSearchParams != null && carSearchParams.startDateTime != null && carSearchParams.endDateTime != null) {
				carSearchParams.startDateTime = carSearchParams.startDateTime.withZone(DateTimeZone.getDefault());
				carSearchParams.endDateTime = carSearchParams.endDateTime.withZone(DateTimeZone.getDefault());
			}
		}
		else if (Strings.isNotEmpty(carSearchParamsString)) {
			carSearchParams = CarDataUtils.getCarSearchParamsFromJSON(carSearchParamsString);
		}

		if (carSearchParams != null) {
			CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder()
				.startDate(carSearchParams.startDateTime.toLocalDate())
				.endDate(carSearchParams.endDateTime.toLocalDate());
			searchParamsBuilder.dateTimeBuilder(dateTimeBuilder);
			searchParamsBuilder.pickupLocationLatLng(carSearchParams.pickupLocationLatLng);

			SuggestionV4 suggestion = new SuggestionV4();
			suggestion.gaiaId = "";
			if (Strings.isNotEmpty(carSearchParams.origin)) {
				suggestion.hierarchyInfo.airport.airportCode = carSearchParams.origin;
			}
			suggestion.regionNames.fullName = suggestion.regionNames.shortName = suggestion.regionNames.displayName = carSearchParams.originDescription;
			if (carSearchParams.pickupLocationLatLng != null) {
				SuggestionV4.LatLng coordinates = new SuggestionV4.LatLng();
				coordinates.lat = carSearchParams.pickupLocationLatLng.lat;
				coordinates.lng = carSearchParams.pickupLocationLatLng.lng;
				suggestion.coordinates = coordinates;
			}

			setPickUpLocation(suggestion, false);

			if (carSearchParams.startDateTime.compareTo(carSearchParams.endDateTime) <= 0) {
				calendar.setSelectedDates(carSearchParams.startDateTime.toLocalDate(),
					carSearchParams.endDateTime.toLocalDate());
			}
			calendarContainer.setPickupTime(carSearchParams.startDateTime);
			calendarContainer.setDropoffTime(carSearchParams.endDateTime);
		}
		else {
			loadHistory();
		}

		searchParamsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				searchParamsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				searchParamsContainerHeight = searchParamsContainer.getMeasuredHeight();

				// Set the dropdown height to size of 3 suggestions.
				pickUpLocation.setDropDownHeight(3 * (int) getResources().getDimension(R.dimen.location_suggestion_row_height));
			}
		});
	}

	public Button setupToolBarCheckmark(final MenuItem menuItem) {
		Button tv = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		tv.setTextColor(Color.WHITE);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSearchFormFilled()) {
					Ui.hideKeyboard(CarSearchPresenter.this);
					calendarContainer.hideToolTip();
					Events.post(new Events.CarsNewSearchParams(carSearchParams));
				}
			}
		});
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
		tv.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(tv);
		return tv;
	}

	private void setPickUpLocation(final SuggestionV4 suggestion) {
		setPickUpLocation(suggestion, true);
	}

	private void setPickUpLocation(final SuggestionV4 suggestion, final boolean filter) {
		SuggestionV4 suggest = suggestion.copy();

		pickUpLocation.setText(suggest.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON ? getContext()
			.getString(R.string.current_location)
			: StrUtils.formatCityName(suggest.regionNames.fullName), filter);

		if (!suggest.isMajorAirport() && suggest.coordinates != null) {
			searchParamsBuilder.origin(null);
			searchParamsBuilder.pickupLocationLatLng(new LatLong(suggest.coordinates.lat, suggest.coordinates.lng));
			searchParamsBuilder.originDescription(StrUtils.formatCityName(suggest.regionNames.fullName));
		}
		else {
			searchParamsBuilder.pickupLocationLatLng(null);
			searchParamsBuilder.origin(suggest.hierarchyInfo.airport.airportCode);
			searchParamsBuilder.originDescription(StrUtils.formatAirport(suggest));
		}
		paramsChanged();
		suggest.iconType = SuggestionV4.IconType.HISTORY_ICON;

		// Remove duplicates
		Iterator<SuggestionV4> it = mRecentCarsLocationsSearches.iterator();
		while (it.hasNext()) {
			SuggestionV4 s = it.next();
			if (Strings.isNotEmpty(s.gaiaId) && s.gaiaId.equalsIgnoreCase(suggest.gaiaId)) {
				it.remove();
			}
		}

		if (mRecentCarsLocationsSearches.size() >= RECENT_MAX_SIZE) {
			mRecentCarsLocationsSearches.remove(RECENT_MAX_SIZE - 1);
		}

		mRecentCarsLocationsSearches.add(0, suggest);
		//Have to remove the bold tag in display name so text for last search is normal
		suggest.regionNames.displayName = Html.fromHtml(suggest.regionNames.displayName).toString();
		// Save
		saveHistory();
		suggestionAdapter.updateRecentHistory(mRecentCarsLocationsSearches);

		selectDateButton.setChecked(true);
		show(new CarParamsCalendar(), FLAG_CLEAR_BACKSTACK);

		setUpSearchButton();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (suggestionAdapter != null) {
			suggestionAdapter.cleanup();
		}
		super.onDetachedFromWindow();
	}

	@Override
	public boolean back() {
		calendarContainer.hideToolTip();
		return super.back();
	}

	@OnClick(R.id.select_date)
	public void onPickupDateTimeClicked() {
		if (!searchParamsBuilder.hasOrigin() && selectDateButton.isChecked()) {
			AnimUtils.doTheHarlemShake(pickUpLocation);
			selectDateButton.setChecked(false);
			return;
		}
		selectDateButton.setChecked(true);
		show(new CarParamsCalendar());
	}

	@OnCheckedChanged(R.id.select_date)
	public void onDateCheckedChanged(boolean isChecked) {
		Drawable drawableEnabled = getResources().getDrawable(R.drawable.date).mutate();
		drawableEnabled.setColorFilter(isChecked ? Color.WHITE
			: getResources().getColor(Ui.obtainThemeResID(getContext(), R.attr.skin_carsUncheckedToggleTextColor)),
			PorterDuff.Mode.SRC_IN);
		selectDateButton.setCompoundDrawablesWithIntrinsicBounds(drawableEnabled, null, null, null);
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
			SuggestionV4 suggestion = suggestionAdapter.getItem(position);
			setPickUpLocation(suggestion);
		}
	};

	private OnClickListener mPickupClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			pickUpLocation.showDropDown();
		}
	};

	private OnFocusChangeListener mPickupFocusListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				if (getCurrentState() != null && !getCurrentState().equals(CarParamsDefault.class.getName())) {
					back();
				}
				selectDateButton.setChecked(false);
				pickUpLocation.setText("");
				searchParamsBuilder.origin("");
				searchParamsBuilder.originDescription("");
				searchParamsBuilder.pickupLocationLatLng(null);
				paramsChanged();
				setUpSearchButton();
			}
		}
	};

	/**
	 * Interfaces
	 */

	private void paramsChanged() {
		if (searchParamsBuilder.hasOrigin()) {
			carSearchParams = searchParamsBuilder.build();
			if (carSearchParams.startDateTime != null) {
				String dateTimeRange = DateFormatUtils
					.formatCarDateTimeRange(getContext(), carSearchParams.startDateTime,
						carSearchParams.endDateTime);
				selectDateButton.setText(dateTimeRange);
				selectDateButton.setTextOff(dateTimeRange);
				selectDateButton.setTextOn(dateTimeRange);
			}
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
			SuggestionV4 topSuggestion = giveTopSuggestion();
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

	public SuggestionV4 giveTopSuggestion() {
		for (int position = 0; position < suggestionAdapter.getCount() - 1; position++) {
			SuggestionV4 suggestion = suggestionAdapter.getItem(position);
			if (!(SuggestionBaseAdapter.DEFAULT_AUTOFILL_ITEM_ID).equalsIgnoreCase(suggestion.gaiaId)) {
				return suggestion;
			}
		}
		return null;
	}

	private void hidePickupDropdown() {
		pickUpLocation.clearFocus();
		Ui.hideKeyboard(pickUpLocation);
	}

	private void saveHistory() {
		SuggestionUtils
			.saveSuggestionHistory(getContext(), mRecentCarsLocationsSearches, SuggestionUtils.RECENT_ROUTES_CARS_LOCATION_FILE);
	}

	private void loadHistory() {
		mRecentCarsLocationsSearches = SuggestionUtils.loadSuggestionHistory(getContext(),
			SuggestionUtils.RECENT_ROUTES_CARS_LOCATION_FILE);
		suggestionAdapter.addNearbyAndRecents(mRecentCarsLocationsSearches, getContext());
		postDelayed(new Runnable() {
			public void run() {
				if (ExpediaBookingApp.isAutomation()) {
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
		CarParamsCalendar.class, new DecelerateInterpolator(), Transition.DEFAULT_ANIMATION_DURATION) {
		private int calendarHeight;

		@Override
		public void startTransition(boolean forward) {
			int parentHeight = getHeight();
			calendarHeight = calendarContainer.getHeight();
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

	public void animationStart(boolean forward, float alpha) {
		calendarContainer.setTranslationY(forward ? searchContainer.getHeight() : 0);
		searchContainer.setBackgroundColor(Color.TRANSPARENT);
		toolBarSearchText.setAlpha(forward ? 0 : 1);
		searchButton.setAlpha(forward ? 0 : 1);
		searchTop = toolbarTwo.getTop() -  searchContainer.getTop();
		searchParamsContainer.setAlpha(forward ? alpha : 1f);
		if (statusBar != null) {
			statusBar.setAlpha(forward ? alpha : 1f);
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
		navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.CLOSE.getType());
	}

	public void setUpSearchButton() {
		if (searchParamsBuilder.areRequiredParamsFilled()) {
			searchButton.setAlpha(1f);
		}
		else {
			searchButton.setAlpha(.15f);
		}
	}
}
