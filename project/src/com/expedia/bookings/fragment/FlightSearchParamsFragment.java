package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class FlightSearchParamsFragment extends Fragment implements OnDateChangedListener {

	public static final String TAG = FlightSearchParamsFragment.class.toString();

	private static final String ARG_INITIAL_PARAMS = "ARG_INITIAL_PARAMS";
	private static final String ARG_DIM_BACKGROUND = "ARG_DIM_BACKGROUND";

	private static final String INSTANCE_SHOW_CALENDAR = "INSTANCE_SHOW_CALENDAR";
	private static final String INSTANCE_PARAMS = "INSTANCE_PARAMS";
	private static final String INSTANCE_FIRST_LOCATION = "INSTANCE_FIRST_LOCATION";

	// Controls the ratio of how large a selected EditText should take up
	// 1 == takes up the full size, 0 == takes up 50%.
	private static final float EDITTEXT_EXPANSION_RATIO = .25f;

	// TODO: Localize this date format
	private static final String DATE_FORMAT = "MMM d";

	private FlightSearchParamsFragmentListener mListener;

	private View mFocusStealer;
	private View mDimmerView;
	private ViewGroup mHeaderGroup;
	private LinearLayout mAirportsContainer;
	private AutoCompleteTextView mDepartureAirportEditText;
	private AutoCompleteTextView mArrivalAirportEditText;
	private TextView mDatesTextView;
	private View mClearDatesButton;
	private CalendarDatePicker mCalendarDatePicker;

	private FlightSearchParams mSearchParams;

	private AirportDropDownAdapter mAirportAdapter;

	// We have to store this due to the way the airport adapter works.
	// If you've rotated the screen, we prevent the adapter from
	// firing another autocomplete request (as it's not shown anyways)
	// but we still need to know what the first "match" was in case
	// the user clicks away from the edit text without selecting a location.
	private Location mFirstAdapterLocation;

	public static FlightSearchParamsFragment newInstance(FlightSearchParams initialParams, boolean dimBackground) {
		FlightSearchParamsFragment fragment = new FlightSearchParamsFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_INITIAL_PARAMS, initialParams);
		args.putBoolean(ARG_DIM_BACKGROUND, dimBackground);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			mSearchParams = JSONUtils.getJSONable(getArguments(), ARG_INITIAL_PARAMS, FlightSearchParams.class);
		}
		else {
			mSearchParams = JSONUtils.getJSONable(savedInstanceState, INSTANCE_PARAMS, FlightSearchParams.class);
			mFirstAdapterLocation = JSONUtils.getJSONable(savedInstanceState, INSTANCE_FIRST_LOCATION, Location.class);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof FlightSearchParamsFragmentListener) {
			mListener = (FlightSearchParamsFragmentListener) activity;
		}
		else {
			throw new RuntimeException("FlightSearchParamsFragment Activity requires a listener!");
		}
	}

	private boolean mItemClicked = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_search_params, container, false);

		// Cache views
		mFocusStealer = Ui.findView(v, R.id.focus_stealer);
		mDimmerView = Ui.findView(v, R.id.dimmer_view);
		mHeaderGroup = Ui.findView(v, R.id.header);
		mAirportsContainer = Ui.findView(v, R.id.airports_container);
		mDepartureAirportEditText = Ui.findView(v, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(v, R.id.arrival_airport_edit_text);
		mDatesTextView = Ui.findView(v, R.id.dates_button);
		mCalendarDatePicker = Ui.findView(v, R.id.calendar_date_picker);
		mClearDatesButton = Ui.findView(v, R.id.clear_dates_btn);

		// Configure views
		if (getArguments().getBoolean(ARG_DIM_BACKGROUND)) {
			mDimmerView.setVisibility(View.VISIBLE);
		}

		mAirportAdapter = new AirportDropDownAdapter(getActivity());

		mDepartureAirportEditText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mItemClicked = true;

				Location location = mAirportAdapter.getLocation(position);
				if (location != null) {
					mSearchParams.setDepartureLocation(location);
					mAirportAdapter.onAirportSelected(location);
				}
				updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureLocation());

				mArrivalAirportEditText.requestFocus();
			}
		});

		mArrivalAirportEditText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mItemClicked = true;

				Location location = mAirportAdapter.getLocation(position);
				if (location != null) {
					mSearchParams.setArrivalLocation(location);
					mAirportAdapter.onAirportSelected(location);
				}
				updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalLocation());

				mDatesTextView.performClick();
			}
		});

		mArrivalAirportEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					mDatesTextView.performClick();
					return true;
				}

				return false;
			}
		});

		if (savedInstanceState == null) {
			// Fill in the initial departure/arrival airports if we are just launching
			updateAirportText();
		}

		mDatesTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				toggleCalendarDatePicker(true);
			}
		});

		mDatesTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.isEmpty(s)) {
					mClearDatesButton.setVisibility(View.GONE);
				}
				else {
					mClearDatesButton.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		// Initial calendar date picker variables
		CalendarUtils.configureCalendarDatePickerForFlights(mCalendarDatePicker,
				CalendarDatePicker.SelectionMode.HYBRID);
		mCalendarDatePicker.setOnDateChangedListener(this);

		if (savedInstanceState != null) {
			toggleCalendarDatePicker(savedInstanceState.getBoolean(INSTANCE_SHOW_CALENDAR));
		}

		mClearDatesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Set out of range dates...
				mCalendarDatePicker.reset();

				mSearchParams.setDepartureDate(null);
				mSearchParams.setReturnDate(null);

				//Refresh things
				updateCalendarText();
				updateCalendarInstructionText();

				updateListener();
			}
		});

		updateCalendarText();
		updateCalendarInstructionText();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Don't set the focus change listener until now, so that we can properly
		// restore the state of the views.  Same with the adapter (so it doesn't
		// constantly fire queries when nothing has changed).
		mDepartureAirportEditText.setOnFocusChangeListener(mAirportFocusChangeListener);
		mArrivalAirportEditText.setOnFocusChangeListener(mAirportFocusChangeListener);

		mDepartureAirportEditText.setAdapter(mAirportAdapter);
		mArrivalAirportEditText.setAdapter(mAirportAdapter);
	}

	@Override
	public void onPause() {
		super.onPause();

		// Clear adapter so we don't fire off unnecessary requests to it
		// during a configuration change
		mFirstAdapterLocation = mAirportAdapter.getLocation(0);
		mDepartureAirportEditText.setAdapter((AirportDropDownAdapter) null);
		mArrivalAirportEditText.setAdapter((AirportDropDownAdapter) null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_SHOW_CALENDAR, mCalendarDatePicker.getVisibility() == View.VISIBLE);
		JSONUtils.putJSONable(outState, INSTANCE_PARAMS, mSearchParams);
		JSONUtils.putJSONable(outState, INSTANCE_FIRST_LOCATION, mFirstAdapterLocation);
	}

	//////////////////////////////////////////////////////////////////////////
	// View control

	private OnFocusChangeListener mAirportFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				toggleCalendarDatePicker(false);

				// Clear out previous data
				TextView tv = (TextView) v;
				tv.setText(null);

				expandAirportEditText(v);
			}
			else if (mItemClicked) {
				mItemClicked = false;
			}
			else {
				// We lost focus, but the user did not select a new airport.  Here's the logic:
				// 
				// 1. If the autocomplete has suggestions, use the first one.
				// 2. If there are no suggestions but there was text, just use that
				// 3. If the textview was empty, revert back to the original search param
				TextView tv = (TextView) v;
				Location location = null;
				if (!TextUtils.isEmpty(tv.getText())) {
					location = mAirportAdapter.getLocation(0);
					if (location == null) {
						location = mFirstAdapterLocation;
						if (location == null) {
							location = new Location();
							location.setDestinationId(tv.getText().toString());
						}
					}
				}

				if (v == mDepartureAirportEditText) {
					if (location != null) {
						mSearchParams.setDepartureLocation(location);
						mAirportAdapter.onAirportSelected(location);
					}

					updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureLocation());
				}
				else {
					if (location != null) {
						mSearchParams.setArrivalLocation(location);
						mAirportAdapter.onAirportSelected(location);
					}

					updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalLocation());
				}
			}

			// Regardless anything else, we don't care about the first adapter location at this point
			mFirstAdapterLocation = null;

			if (!hasFocus) {
				updateListener();
			}
		}
	};

	private void updateAirportText() {
		updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureLocation());
		updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalLocation());
	}

	private void updateAirportText(TextView textView, Location location) {
		if (location == null) {
			textView.setText(null);
		}
		else if (!TextUtils.isEmpty(location.getDescription())) {
			textView.setText(location.getDescription());
		}
		else {
			textView.setText(location.getDestinationId());
		}
	}

	private void expandAirportEditText(View focusView) {
		final View expandingView;
		final View shrinkingView;

		if (focusView == mDepartureAirportEditText) {
			expandingView = mDepartureAirportEditText;
			shrinkingView = mArrivalAirportEditText;
		}
		else {
			expandingView = mArrivalAirportEditText;
			shrinkingView = mDepartureAirportEditText;
		}

		// Show an animation to expand/collapse one or the other.
		final LinearLayout.LayoutParams expandingLayoutParams = (LinearLayout.LayoutParams) expandingView
				.getLayoutParams();
		final LinearLayout.LayoutParams shrinkingLayoutParams = (LinearLayout.LayoutParams) shrinkingView
				.getLayoutParams();
		final float halfWeight = mAirportsContainer.getWeightSum() / 2;
		final float startVal = expandingLayoutParams.weight - halfWeight;
		final float endVal = halfWeight * EDITTEXT_EXPANSION_RATIO;

		ValueAnimator anim = ValueAnimator.ofFloat(startVal, endVal);
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				float val = (Float) animator.getAnimatedValue();
				expandingLayoutParams.weight = halfWeight + val;
				shrinkingLayoutParams.weight = halfWeight - val;
				mAirportsContainer.requestLayout();
			}
		});
		anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
		anim.start();
	}

	private void resetAirportEditTexts() {
		if (!isAdded()) {
			return;
		}

		// Animate both views back to 50/50
		final float halfWeight = mAirportsContainer.getWeightSum() / 2;

		final LinearLayout.LayoutParams expandingLayoutParams;
		final LinearLayout.LayoutParams shrinkingLayoutParams;

		if (((LinearLayout.LayoutParams) mDepartureAirportEditText.getLayoutParams()).weight > halfWeight) {
			expandingLayoutParams = (LinearLayout.LayoutParams) mArrivalAirportEditText.getLayoutParams();
			shrinkingLayoutParams = (LinearLayout.LayoutParams) mDepartureAirportEditText.getLayoutParams();
		}
		else {
			expandingLayoutParams = (LinearLayout.LayoutParams) mDepartureAirportEditText.getLayoutParams();
			shrinkingLayoutParams = (LinearLayout.LayoutParams) mArrivalAirportEditText.getLayoutParams();
		}

		float startVal = expandingLayoutParams.weight - halfWeight;

		// If they weren't already focused, don't bother with this animation
		if (Math.abs(startVal) < .001) {
			return;
		}

		ValueAnimator anim = ValueAnimator.ofFloat(expandingLayoutParams.weight - halfWeight, 0);
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				float val = (Float) animator.getAnimatedValue();
				expandingLayoutParams.weight = halfWeight + val;
				shrinkingLayoutParams.weight = halfWeight - val;
				mAirportsContainer.requestLayout();
			}
		});
		anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
		anim.start();
	}

	private void clearEditTextFocus() {
		EditText textWithFocus = null;

		if (mDepartureAirportEditText.hasFocus()) {
			textWithFocus = mDepartureAirportEditText;
		}
		else if (mArrivalAirportEditText.hasFocus()) {
			textWithFocus = mArrivalAirportEditText;
		}

		if (textWithFocus != null && isAdded()) {
			mFocusStealer.requestFocus();

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textWithFocus.getWindowToken(), 0);
		}

		resetAirportEditTexts();
	}

	private void updateCalendarInstructionText() {
		if (mCalendarDatePicker != null) {
			Calendar dateStart = mSearchParams.getDepartureDate() == null ? null : mSearchParams.getDepartureDate()
					.getCalendar();
			Calendar dateEnd = mSearchParams.getReturnDate() == null ? null : mSearchParams.getReturnDate()
					.getCalendar();
			boolean researchMode = mCalendarDatePicker.getOneWayResearchMode();

			if (dateStart != null && dateEnd != null) {
				int nightCount = Time.getJulianDay(dateEnd.getTimeInMillis(), dateEnd.getTimeZone().getRawOffset())
						- Time.getJulianDay(dateStart.getTimeInMillis(), dateStart.getTimeZone().getRawOffset());
				String nightsStr;
				if (nightCount == 0) {
					nightsStr = getString(R.string.calendar_instructions_range_selected_same_day);
				}
				else {
					nightsStr = String.format(getString(R.string.calendar_instructions_range_selected_TEMPLATE),
							nightCount);
				}
				mCalendarDatePicker.setHeaderInstructionText(nightsStr);
			}
			else if (dateStart != null && researchMode) {
				mCalendarDatePicker
						.setHeaderInstructionText(getString(R.string.calendar_instructions_nothing_selected));
			}
			else if (dateStart != null) {
				mCalendarDatePicker.setHeaderInstructionText(getString(R.string.calendar_instructions_start_selected));
			}
			else {
				mCalendarDatePicker
						.setHeaderInstructionText(getString(R.string.calendar_instructions_nothing_selected));
			}
		}
	}

	private void toggleCalendarDatePicker(boolean enabled) {
		if (enabled == (mCalendarDatePicker.getVisibility() == View.VISIBLE)) {
			return;
		}

		if (enabled) {
			Date departureDate = mSearchParams.getDepartureDate();
			Date returnDate = mSearchParams.getReturnDate();

			if (departureDate != null) {
				mCalendarDatePicker.updateStartDate(departureDate.getYear(), departureDate.getMonth() - 1,
						departureDate.getDayOfMonth());
			}
			if (returnDate != null) {
				mCalendarDatePicker.updateEndDate(returnDate.getYear(), returnDate.getMonth() - 1,
						returnDate.getDayOfMonth());
			}

			if (returnDate == null) {
				mCalendarDatePicker.setOneWayResearchMode(true);
			}

			// f826 Make sure calendar redraws after crash
			if (departureDate == null && returnDate == null) {
				mCalendarDatePicker.reset();
			}
		}

		if (enabled) {
			// If all the data is available now, fix height - otherwise we have to wait for a layout
			// pass to fix the height.
			if (getView() != null && getView().getHeight() != 0) {
				fixCalendarHeight();
			}
			else {
				mCalendarDatePicker.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						fixCalendarHeight();
						mCalendarDatePicker.requestLayout();
						mCalendarDatePicker.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				});
			}
		}

		mCalendarDatePicker.setVisibility(enabled ? View.VISIBLE : View.GONE);
		updateCalendarInstructionText();
	}

	private void fixCalendarHeight() {
		// Depends on the calendar date picker having a preset height
		int totalHeight = getView().getHeight();
		int headerHeight = mHeaderGroup.getHeight();
		int maxHeight = totalHeight - headerHeight;
		LayoutParams params = mCalendarDatePicker.getLayoutParams();
		if (params.height > maxHeight) {
			params.height = maxHeight;

			int minHeight = getResources().getDimensionPixelSize(R.dimen.flight_calendar_min_height);
			if (params.height < minHeight) {
				params.height = minHeight;
			}
		}
	}

	private void updateCalendarText() {
		Calendar dateStart = mSearchParams.getDepartureDate() == null ? null : mSearchParams.getDepartureDate()
				.getCalendar();
		Calendar dateEnd = mSearchParams.getReturnDate() == null ? null : mSearchParams.getReturnDate().getCalendar();

		if (dateStart == null && dateEnd == null) {
			mDatesTextView.setText(null);
		}
		else if (dateStart != null && dateEnd == null) {
			CharSequence start = DateFormat.format(DATE_FORMAT, dateStart);
			mDatesTextView.setText(Html.fromHtml(getString(R.string.one_way_TEMPLATE, start)));
		}
		else if (dateStart != null && dateEnd != null) {
			CharSequence start = DateFormat.format(DATE_FORMAT, dateStart);
			CharSequence end = DateFormat.format(DATE_FORMAT, dateEnd);
			mDatesTextView.setText(Html.fromHtml(getString(R.string.round_trip_TEMPLATE, start, end)));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Access

	/**
	 * Returns the SearchParams represented by this screen.
	 * 
	 * Warning: doing this deselects all current fields (as a matter of
	 * making sure we have current data).  You are only expected to call
	 * this method when you're closing the fragment/starting a new search.
	 */
	public FlightSearchParams getSearchParams() {
		// Sync all current fields by deselection		
		clearEditTextFocus();

		return mSearchParams;
	}

	public void setSearchParams(FlightSearchParams params) {
		// Reset view state
		clearEditTextFocus();
		toggleCalendarDatePicker(false);

		mSearchParams = params;

		updateAirportText();
		updateCalendarText();
		updateCalendarInstructionText();
	}

	//////////////////////////////////////////////////////////////////////////
	// OnDateChangedListener

	@Override
	public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
		if (mCalendarDatePicker.getStartTime() != null) {
			mSearchParams.setDepartureDate(new Date(mCalendarDatePicker.getStartYear(),
					mCalendarDatePicker.getStartMonth() + 1, mCalendarDatePicker.getStartDayOfMonth()));
		}
		else {
			mSearchParams.setDepartureDate(null);
		}

		if (mCalendarDatePicker.getEndTime() != null) {
			mSearchParams.setReturnDate(new Date(mCalendarDatePicker.getEndYear(),
					mCalendarDatePicker.getEndMonth() + 1, mCalendarDatePicker.getEndDayOfMonth()));
		}
		else {
			mSearchParams.setReturnDate(null);
		}

		updateCalendarText();
		updateCalendarInstructionText();

		updateListener();
	}

	//////////////////////////////////////////////////////////////////////////
	// Interface

	private void updateListener() {
		mListener.onParamsChanged();
	}

	public interface FlightSearchParamsFragmentListener {
		public void onParamsChanged();
	}
}
