package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class FlightSearchParamsFragment extends Fragment implements OnDateChangedListener {

	public static final String TAG = FlightSearchParamsFragment.class.toString();

	private static final String ARG_INITIAL_PARAMS = "ARG_INITIAL_PARAMS";
	private static final String ARG_DIM_BACKGROUND = "ARG_DIM_BACKGROUND";

	private static final String INSTANCE_SHOW_CALENDAR = "INSTANCE_SHOW_CALENDAR";

	// Controls the ratio of how large a selected EditText should take up
	// 1 == takes up the full size, 0 == takes up 50%.
	private static final float EDITTEXT_EXPANSION_RATIO = .25f;

	// TODO: Localize this date format
	private static final String DATE_FORMAT = "MMM d";

	private View mFocusStealer;
	private View mDimmerView;
	private LinearLayout mAirportsContainer;
	private AutoCompleteTextView mDepartureAirportEditText;
	private AutoCompleteTextView mArrivalAirportEditText;
	private TextView mDatesTextView;
	private View mPassengersButton;
	private CalendarDatePicker mCalendarDatePicker;

	private FlightSearchParams mSearchParams;

	private AirportDropDownAdapter mAirportAdapter;

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

		mSearchParams = JSONUtils.getJSONable(getArguments(), ARG_INITIAL_PARAMS, FlightSearchParams.class);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mAirportAdapter = new AirportDropDownAdapter(activity);
		mAirportAdapter.openDb();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_search_params, container, false);

		// Cache views
		mFocusStealer = Ui.findView(v, R.id.focus_stealer);
		mDimmerView = Ui.findView(v, R.id.dimmer_view);
		mAirportsContainer = Ui.findView(v, R.id.airports_container);
		mDepartureAirportEditText = Ui.findView(v, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(v, R.id.arrival_airport_edit_text);
		mDatesTextView = Ui.findView(v, R.id.dates_button);
		mPassengersButton = Ui.findView(v, R.id.passengers_button);
		mCalendarDatePicker = Ui.findView(v, R.id.calendar_date_picker);

		// Configure views
		if (getArguments().getBoolean(ARG_DIM_BACKGROUND)) {
			mDimmerView.setVisibility(View.VISIBLE);
		}

		mDepartureAirportEditText.setAdapter(mAirportAdapter);
		mArrivalAirportEditText.setAdapter(mAirportAdapter);

		mDepartureAirportEditText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mArrivalAirportEditText.requestFocus();
			}
		});

		mArrivalAirportEditText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
			updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureAirportCode());
			updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalAirportCode());
		}

		mDatesTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				toggleCalendarDatePicker(true);
			}
		});

		mPassengersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				toggleCalendarDatePicker(false);

				Toast.makeText(getActivity(), "TODO: Design & implement passenger picker", Toast.LENGTH_SHORT).show();
			}
		});

		// Initial calendar date picker variables
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID);
		mCalendarDatePicker.setOnDateChangedListener(this);

		if (savedInstanceState != null) {
			toggleCalendarDatePicker(savedInstanceState.getBoolean(INSTANCE_SHOW_CALENDAR));
		}

		updateCalendarText();
		updateAirportTextColors();
		updateCalendarInstructionText();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Don't set the focus change listener until now, so that we can properly
		// restore the state of the views
		mDepartureAirportEditText.setOnFocusChangeListener(mAirportFocusChangeListener);
		mArrivalAirportEditText.setOnFocusChangeListener(mAirportFocusChangeListener);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_SHOW_CALENDAR, mCalendarDatePicker.getVisibility() == View.VISIBLE);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mAirportAdapter.closeDb();
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
			else {
				if (v == mDepartureAirportEditText) {
					String airportCode = mDepartureAirportEditText.getText().toString().toUpperCase();
					if (!TextUtils.isEmpty(airportCode)) {
						mSearchParams.setDepartureAirportCode(airportCode);
					}
					updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureAirportCode());
				}
				else {
					String airportCode = mArrivalAirportEditText.getText().toString().toUpperCase();
					if (!TextUtils.isEmpty(airportCode)) {
						mSearchParams.setArrivalAirportCode(airportCode);
					}
					updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalAirportCode());
				}
			}
		}
	};

	private void updateAirportText(TextView textView, String airportCode) {
		Airport airport = FlightStatsDbUtils.getAirport(airportCode);
		if (airport == null) {
			textView.setText(null);
		}
		else {
			String city = airport.mCity;
			if (TextUtils.isEmpty(city)) {
				city = getString(R.string.custom_code);
			}
			String str = getString(R.string.search_airport_TEMPLATE, airport.mAirportCode, city);
			textView.setText(Html.fromHtml(str));
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

		if (textWithFocus != null) {
			mFocusStealer.requestFocus();

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textWithFocus.getWindowToken(), 0);
		}

		resetAirportEditTexts();
	}

	private void updateAirportTextColors() {
		Resources res = getResources();
		int defColor = res.getColor(R.color.flight_airport_text);
		mDepartureAirportEditText.setTextColor(defColor);
		mArrivalAirportEditText.setTextColor(defColor);

		// If the calendar is open, highlight whichever leg we're selecting a date for
		if (mCalendarDatePicker.getVisibility() == View.VISIBLE) {
			int selectedColor = res.getColor(R.color.flight_airport_text_selected);
			if (mCalendarDatePicker.getStartTime() == null) {
				mDepartureAirportEditText.setTextColor(selectedColor);
			}
			else if (mCalendarDatePicker.getEndTime() == null) {
				mArrivalAirportEditText.setTextColor(selectedColor);
			}
		}
	}

	private void updateCalendarInstructionText() {
		if (mCalendarDatePicker != null) {
			Calendar dateStart = mSearchParams.getDepartureDate() == null ? null : mSearchParams.getDepartureDate()
					.getCalendar();
			Calendar dateEnd = mSearchParams.getReturnDate() == null ? null : mSearchParams.getReturnDate()
					.getCalendar();

			if (dateStart != null && dateEnd != null) {
				int nightCount = Time.getJulianDay(dateEnd.getTimeInMillis(), dateEnd.getTimeZone().getRawOffset())
						- Time.getJulianDay(dateStart.getTimeInMillis(), dateStart.getTimeZone().getRawOffset());
				String nightsStr = String.format(getString(R.string.calendar_instructions_range_selected_TEMPLATE),
						nightCount);
				mCalendarDatePicker.setHeaderInstructionText(nightsStr);
			}
			else if (dateStart != null) {
				mCalendarDatePicker.setHeaderInstructionText(getString(R.string.calendar_instructions_start_selected));
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
		}

		mCalendarDatePicker.setVisibility(enabled ? View.VISIBLE : View.GONE);
		updateAirportTextColors();
		updateCalendarInstructionText();
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

	//////////////////////////////////////////////////////////////////////////
	// OnDateChangedListener

	@Override
	public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
		mSearchParams.setDepartureDate(new Date(mCalendarDatePicker.getStartYear(),
				mCalendarDatePicker.getStartMonth() + 1, mCalendarDatePicker.getStartDayOfMonth()));

		if (mCalendarDatePicker.getEndTime() != null) {
			mSearchParams.setReturnDate(new Date(mCalendarDatePicker.getEndYear(),
					mCalendarDatePicker.getEndMonth() + 1, mCalendarDatePicker.getEndDayOfMonth()));
		}
		else {
			mSearchParams.setReturnDate(null);
		}

		updateCalendarText();
		updateAirportTextColors();
		updateCalendarInstructionText();
	}
}
