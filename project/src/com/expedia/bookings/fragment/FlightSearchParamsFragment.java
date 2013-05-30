package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.animation.AnimationListenerAdapter;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.expedia.bookings.widget.NumTravelersPopupDropdown;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class FlightSearchParamsFragment extends Fragment implements OnDateChangedListener, InputFilter {

	public static final String TAG = FlightSearchParamsFragment.class.toString();

	private static final String ARG_INITIAL_PARAMS = "ARG_INITIAL_PARAMS";
	private static final String ARG_DIM_BACKGROUND = "ARG_DIM_BACKGROUND";

	private static final String INSTANCE_SHOW_CALENDAR = "INSTANCE_SHOW_CALENDAR";
	private static final String INSTANCE_PARAMS = "INSTANCE_PARAMS";
	private static final String INSTANCE_FIRST_LOCATION = "INSTANCE_FIRST_LOCATION";

	// Controls the ratio of how large a selected EditText should take up
	// 1 == takes up the full size, 0 == takes up 50%.
	private static final float EDITTEXT_EXPANSION_RATIO = .25f;

	private static final int DATE_FORMAT_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH
			| DateUtils.FORMAT_NO_YEAR;

	private FlightSearchParamsFragmentListener mListener;

	private View mFocusStealer;
	private View mDimmerView;
	private ViewGroup mHeaderGroup;
	private ViewGroup mAirportsContainer;
	private AutoCompleteTextView mDepartureAirportEditText;
	private AutoCompleteTextView mArrivalAirportEditText;
	private TextView mDatesTextView;
	private View mClearDatesButton;
	private ViewGroup mCalendarContainer;
	private View mCalendarShadow;
	private CalendarDatePicker mCalendarDatePicker;
	private ImageButton mNumTravelersButton;
	private TextView mNumTravelersTextView;
	private PopupWindow mNumTravelersPopup;

	private FlightSearchParams mSearchParams;

	private AirportDropDownAdapter mAirportAdapter;

	// Animator for calendar
	private Animation mCalendarAnimation;

	// We have to store this due to the way the airport adapter works.
	// If you've rotated the screen, we prevent the adapter from
	// firing another autocomplete request (as it's not shown anyways)
	// but we still need to know what the first "match" was in case
	// the user clicks away from the edit text without selecting a location.
	private Location mFirstAdapterLocation;

	// Special code just for landscape
	private boolean mIsLandscape;
	private boolean mIsTablet;

	// So we can tell if we are running this fragment for the very first time
	private boolean mFirstRun;

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

		mIsLandscape = getResources().getBoolean(R.bool.landscape);
		mIsTablet = ExpediaBookingApp.useTabletInterface(getActivity());
		mFirstRun = savedInstanceState == null;
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
		mCalendarContainer = Ui.findView(v, R.id.calendar_container);
		mCalendarShadow = Ui.findView(v, R.id.calendar_shadow);
		mCalendarDatePicker = Ui.findView(v, R.id.calendar_date_picker);
		mClearDatesButton = Ui.findView(v, R.id.clear_dates_btn);
		mNumTravelersButton = Ui.findView(v, R.id.num_travelers_button);
		mNumTravelersTextView = Ui.findView(v, R.id.num_travelers_text_view);

		// Configure views
		if (getArguments().getBoolean(ARG_DIM_BACKGROUND)) {
			mDimmerView.setVisibility(View.VISIBLE);
		}

		// In landscape, make the calendar date picker fill the screen
		if (mIsLandscape && !mIsTablet) {
			mCalendarShadow.setVisibility(View.GONE);
			mCalendarContainer.getLayoutParams().height = LayoutParams.MATCH_PARENT;
		}

		// If it is set to MATCH_PARENT on tablet though we want
		// to clamp it to the bottom of the other search params
		if (mIsLandscape
				&& mIsTablet
				&& getResources().getDimensionPixelSize(R.dimen.flight_search_calendar_height) == LayoutParams.MATCH_PARENT) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCalendarContainer.getLayoutParams();
			lp.addRule(RelativeLayout.BELOW, R.id.header);
		}

		mAirportAdapter = new AirportDropDownAdapter(getActivity());

		InputFilter[] filters = new InputFilter[] { this };
		mDepartureAirportEditText.setFilters(filters);
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

				onDepartureInputComplete();
			}
		});

		mDepartureAirportEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					onDepartureInputComplete();
					return true;
				}

				return false;
			}
		});

		mArrivalAirportEditText.setFilters(filters);
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

				onArrivalInputComplete();
			}
		});

		mArrivalAirportEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
					onArrivalInputComplete();
					return true;
				}

				return false;
			}
		});

		mArrivalAirportEditText.setImeOptions(mIsLandscape ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);

		// Always initially reset the airport texts (otherwise they start overlapping)
		resetAirportEditTexts(false);

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
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID);
		mCalendarDatePicker.setOnDateChangedListener(this);

		if (savedInstanceState != null) {
			toggleCalendarDatePicker(savedInstanceState.getBoolean(INSTANCE_SHOW_CALENDAR), false);
		}

		mClearDatesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearDates();
			}
		});

		updateCalendarText();
		updateCalendarInstructionText();

		// Num travelers select
		mNumTravelersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleNumAdultsDropdown();
			}
		});

		mNumTravelersPopup = NumTravelersPopupDropdown.newInstance(getActivity());
		ListView lv = Ui.findView(mNumTravelersPopup.getContentView(), R.id.nav_dropdown_list);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onNumTravelersSelected(position + 1);
			}
		});

		updateNumTravelersText();

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

		if (mFirstRun && Db.getFlightSearch().getSearchParams().getDepartureLocation() == null) {
			mDepartureAirportEditText.requestFocus();

			// Dumb hack to get IME to show.  Without delaying this doesn't work (for some dumb reason)
			mDepartureAirportEditText.postDelayed(new Runnable() {
				@Override
				public void run() {
					// With weird timing issues, we can end up running this when we're not attached
					Context context = getActivity();
					if (context == null) {
						return;
					}

					InputMethodManager imm = (InputMethodManager) context
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(mDepartureAirportEditText, 0);
				}
			}, 250);
		}
		else if (mDepartureAirportEditText.isFocused()) {
			expandAirportEditText(mDepartureAirportEditText, false);
		}
		else if (mArrivalAirportEditText.isFocused()) {
			expandAirportEditText(mArrivalAirportEditText, false);
		}
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

		outState.putBoolean(INSTANCE_SHOW_CALENDAR, mCalendarContainer.getVisibility() == View.VISIBLE);
		JSONUtils.putJSONable(outState, INSTANCE_PARAMS, mSearchParams);
		JSONUtils.putJSONable(outState, INSTANCE_FIRST_LOCATION, mFirstAdapterLocation);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		// EB93: There is a crazy bug in earlier versions of Android that retain the Fragment
		// if the user opens an EditText in some circumstances.  This was causing serious memory
		// problems; below we remove the largest issue when retaining the Fragment.  It's a
		// serious workaround, but better than nothing.
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
			mCalendarContainer.removeAllViewsInLayout();
			mCalendarDatePicker = null;
		}
	}

	public boolean onBackPressed() {
		if (mCalendarAnimation != null && !mCalendarAnimation.hasEnded()) {
			// Block back during animation
			return true;
		}
		else if (mCalendarContainer.getVisibility() == View.VISIBLE) {
			toggleCalendarDatePicker(false);
			return true;
		}
		else {
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// View control

	private OnFocusChangeListener mAirportFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v;
			if (hasFocus) {
				mAirportAdapter.setShowNearbyAirports(v == mDepartureAirportEditText);
				autoCompleteTextView.setAdapter(mAirportAdapter);
			}
			else {
				autoCompleteTextView.setAdapter((AirportDropDownAdapter) null);
			}

			if (hasFocus) {
				toggleCalendarDatePicker(false);

				// Clear out previous data
				TextView tv = (TextView) v;
				tv.setText(null);

				expandAirportEditText(v, true);
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

	private void expandAirportEditText(final View focusView, final boolean animate) {
		adjustAirportEditTexts(focusView, EDITTEXT_EXPANSION_RATIO, animate);
	}

	private void resetAirportEditTexts(final boolean animate) {
		if (!isAdded()) {
			return;
		}

		adjustAirportEditTexts(mDepartureAirportEditText, .5f, animate);
	}

	private void adjustAirportEditTexts(final View focusView, final float ratio, final boolean animate) {
		// There are two possible setups here - one where we use LinearLayout (but never animate)
		// and one where we use StableFrameLayout (and do animate, but only on newer devices).
		if (mAirportsContainer instanceof LinearLayout) {
			LinearLayout airportsContainer = (LinearLayout) mAirportsContainer;
			View expandingView = focusView == mDepartureAirportEditText ? mDepartureAirportEditText
					: mArrivalAirportEditText;
			View shrinkingView = focusView == mDepartureAirportEditText ? mArrivalAirportEditText
					: mDepartureAirportEditText;

			final LinearLayout.LayoutParams expandingLayoutParams = (LinearLayout.LayoutParams) expandingView
					.getLayoutParams();
			final LinearLayout.LayoutParams shrinkingLayoutParams = (LinearLayout.LayoutParams) shrinkingView
					.getLayoutParams();
			final float totalWeight = airportsContainer.getWeightSum();
			expandingLayoutParams.weight = totalWeight * (1 - ratio);
			shrinkingLayoutParams.weight = totalWeight * ratio;
			mAirportsContainer.requestLayout();
		}
		else {
			final int totalWidth = mAirportsContainer.getWidth();
			if (totalWidth == 0) {
				// Defer reset until we have layout
				mAirportsContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						mAirportsContainer.getViewTreeObserver().removeOnPreDrawListener(this);
						adjustAirportEditTexts(focusView, ratio, animate);
						return true;
					}
				});
			}
			else {
				float mod = focusView == mDepartureAirportEditText ? 1 - ratio : ratio;
				int midPoint = (int) Math.round(totalWidth * mod);
				final int startDepRight = mDepartureAirportEditText.getRight();
				final int changeRight = startDepRight - midPoint;
				final int startArrLeft = mArrivalAirportEditText.getLeft();
				final int changeLeft = startArrLeft - midPoint;

				if (!animate) {
					mDepartureAirportEditText.setRight(startDepRight - changeRight);
					mArrivalAirportEditText.setLeft(startArrLeft - changeLeft);
				}
				else {
					final boolean isExpandingDeparture = changeRight < 0;
					final CharSequence departureHint = mDepartureAirportEditText.getHint();
					ValueAnimator depAnim = ValueAnimator.ofInt(0, changeRight);
					depAnim.addUpdateListener(new AnimatorUpdateListener() {
						public void onAnimationUpdate(ValueAnimator animator) {
							int val = (Integer) animator.getAnimatedValue();
							mDepartureAirportEditText.setRight(startDepRight - val);

							// Trick into re-laying out the hint (otherwise end up with ellipsis)
							if (isExpandingDeparture) {
								mDepartureAirportEditText.setHint(departureHint);
							}
						}
					});

					final boolean isExpandingArrival = changeLeft > 0;
					final CharSequence arrivalHint = mArrivalAirportEditText.getHint();
					ValueAnimator arrAnim = ValueAnimator.ofInt(0, changeLeft);
					arrAnim.addUpdateListener(new AnimatorUpdateListener() {
						public void onAnimationUpdate(ValueAnimator animator) {
							int val = (Integer) animator.getAnimatedValue();
							mArrivalAirportEditText.setLeft(startArrLeft - val);

							// Trick into re-laying out the hint (otherwise end up with ellipsis)
							if (isExpandingArrival) {
								mArrivalAirportEditText.setHint(arrivalHint);
							}
						}
					});

					AnimatorSet set = new AnimatorSet();
					set.playTogether(depAnim, arrAnim);

					// We want to hide the dropdown durin the animation so it doesn't end up
					// in a funny state.
					set.addListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationStart(Animator animation) {
							mDepartureAirportEditText.dismissDropDown();
							mArrivalAirportEditText.dismissDropDown();
						}

						@Override
						public void onAnimationEnd(Animator animation) {
							if (getActivity() != null) {
								mDepartureAirportEditText.showDropDown();
								mArrivalAirportEditText.showDropDown();
							}
						}
					});

					set.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

					set.start();
				}
			}
		}
	}

	private void onDepartureInputComplete() {
		if (mSearchParams.getArrivalLocation() == null) {
			mArrivalAirportEditText.requestFocus();
		}
		else if (mSearchParams.getDepartureDate() == null && !mIsLandscape) {
			mDatesTextView.performClick();
		}
		else {
			clearEditTextFocus();
		}
	}

	private void onArrivalInputComplete() {
		if (mSearchParams.getDepartureDate() == null && !mIsLandscape) {
			mDatesTextView.performClick();
		}
		else {
			clearEditTextFocus();
		}
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

			hideKeyboard();
		}

		resetAirportEditTexts(true);
	}

	private void hideKeyboard() {
		Activity activity = getActivity();
		if (activity != null) {
			View focusView = activity.getCurrentFocus();
			if (focusView != null) {
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
			}
		}
	}

	private void clearDates() {
		//Set out of range dates...
		mCalendarDatePicker.reset();

		mSearchParams.setDepartureDate(null);
		mSearchParams.setReturnDate(null);

		//Refresh things
		updateCalendarText();
		updateCalendarInstructionText();

		updateListener();
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
		toggleCalendarDatePicker(enabled, true);
	}

	private void toggleCalendarDatePicker(boolean enabled, boolean animate) {
		if (enabled == (mCalendarContainer.getVisibility() == View.VISIBLE)) {
			return;
		}

		if (enabled) {
			hideKeyboard();

			Date departureDate = mSearchParams.getDepartureDate();
			Date returnDate = mSearchParams.getReturnDate();

			CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID);

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

			// F1213 - Show action bar because landscape takes up the entire screen otherwise
			if (mIsLandscape && !mIsTablet) {
				((SherlockFragmentActivity) getActivity()).startActionMode(mCalendarActionMode);
				mDepartureAirportEditText.setFocusable(false);
				mArrivalAirportEditText.setFocusable(false);
			}

			mDatesTextView.setBackgroundResource(R.drawable.textfield_activated_holo_light);
		}
		else {
			mDatesTextView.setBackgroundResource(R.drawable.textfield_default_holo_light);
			mDepartureAirportEditText.setFocusableInTouchMode(true);
			mDepartureAirportEditText.setFocusable(true);
			mArrivalAirportEditText.setFocusableInTouchMode(true);
			mArrivalAirportEditText.setFocusable(true);
		}

		if (enabled && !mIsLandscape) {
			// If all the data is available now, fix height - otherwise we have to wait for a layout
			// pass to fix the height.
			if (getView() != null && getView().getHeight() != 0) {
				fixCalendarHeight();
			}
			else {
				mCalendarContainer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						fixCalendarHeight();
						mCalendarContainer.requestLayout();
						mCalendarContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				});
			}
		}

		updateCalendarInstructionText();

		if (!animate) {
			mCalendarContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
		}
		else {
			if (enabled) {
				mCalendarContainer.setVisibility(View.VISIBLE);
				mCalendarAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
			}
			else {
				mCalendarAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
				mCalendarAnimation.setAnimationListener(mCalAnimationListener);
			}

			mCalendarContainer.startAnimation(mCalendarAnimation);
		}
	}

	private AnimationListener mCalAnimationListener = new AnimationListenerAdapter() {
		@Override
		public void onAnimationEnd(Animation animation) {
			mCalendarContainer.setVisibility(View.GONE);
		}
	};

	private void fixCalendarHeight() {
		// Depends on the calendar date picker having a preset height
		int totalHeight = getView().getHeight();
		int headerHeight = mHeaderGroup.getHeight();
		int maxHeight = totalHeight - headerHeight;
		LayoutParams params = mCalendarContainer.getLayoutParams();
		if (params.height > maxHeight) {
			params.height = maxHeight;

			int minHeight = getResources().getDimensionPixelSize(R.dimen.flight_calendar_min_height);
			if (params.height < minHeight) {
				params.height = minHeight;
			}
		}
	}

	private void updateCalendarText() {
		CharSequence start = null;
		if (mSearchParams.getDepartureDate() != null) {
			start = DateUtils.formatDateTime(getActivity(), mSearchParams.getDepartureDate().getCalendar()
					.getTimeInMillis(), DATE_FORMAT_FLAGS);
		}

		CharSequence end = null;
		if (mSearchParams.getReturnDate() != null) {
			end = DateUtils.formatDateTime(getActivity(),
					mSearchParams.getReturnDate().getCalendar().getTimeInMillis(), DATE_FORMAT_FLAGS);
		}

		if (start == null && end == null) {
			mDatesTextView.setText(null);
		}
		else if (end == null) {
			mDatesTextView.setText(Html.fromHtml(getString(R.string.one_way_TEMPLATE, start)));
		}
		else {
			mDatesTextView.setText(Html.fromHtml(getString(R.string.round_trip_TEMPLATE, start, end)));
		}
	}

	// Traveler dropdown methods

	private void toggleNumAdultsDropdown() {
		if (mNumTravelersPopup.isShowing()) {
			mNumTravelersPopup.dismiss();
		}
		else {
			toggleCalendarDatePicker(false);
			clearEditTextFocus();
			mNumTravelersPopup.showAsDropDown(mNumTravelersButton);

		}
	}

	private void onNumTravelersSelected(int num) {
		mSearchParams.setNumAdults(num);
		mNumTravelersPopup.dismiss();
		updateNumTravelersText();
	}

	private void updateNumTravelersText() {
		if (mNumTravelersTextView != null) {
			mNumTravelersTextView.setText(Integer.toString(mSearchParams.getNumAdults()));
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
	public FlightSearchParams getSearchParams(boolean syncFields) {
		if (syncFields) {
			// Sync all current fields by deselection
			clearEditTextFocus();
		}

		return mSearchParams;
	}

	public void setSearchParams(FlightSearchParams params) {
		// Reset view state
		clearEditTextFocus();
		toggleCalendarDatePicker(false, false);

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
	// Calendar action mode
	//
	// Only invoked in landscape with calendar taking up the entire screen

	private ActionMode.Callback mCalendarActionMode = new ActionMode.Callback() {

		private boolean mSaveOnClose;

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			mode.getMenuInflater().inflate(R.menu.action_mode_done, menu);
			mode.setTitle(R.string.select_dates);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mSaveOnClose = false;
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_done:
				mSaveOnClose = true;
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			toggleCalendarDatePicker(false);

			if (!mSaveOnClose) {
				clearDates();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// InputFilter

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		if (end > start && start == 0 && dstart == 0 && source.charAt(0) == ' ') {
			return "";
		}

		return null;
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
