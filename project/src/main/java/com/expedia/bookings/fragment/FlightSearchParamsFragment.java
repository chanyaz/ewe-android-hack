package com.expedia.bookings.fragment;

import java.io.File;
import java.util.List;

import org.joda.time.LocalDate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.animation.AnimationListenerAdapter;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.RecentList;
import com.expedia.bookings.data.RoutesResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AirportDropDownAdapter;
import com.expedia.bookings.widget.FlightRouteAdapter;
import com.expedia.bookings.widget.FlightRouteAdapter.FlightRouteAdapterListener;
import com.expedia.bookings.widget.GuestPicker;
import com.expedia.bookings.widget.GuestPicker.GuestPickerListener;
import com.expedia.bookings.widget.NumTravelersPopupDropdown;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.app.SimpleProgressDialogFragment;
import com.mobiata.android.app.SimpleProgressDialogFragment.SimpleProgressDialogFragmentListener;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;
import com.mobiata.flightlib.data.Airport;
import com.squareup.otto.Subscribe;

public class FlightSearchParamsFragment extends Fragment implements OnDateChangedListener, InputFilter,
	SimpleProgressDialogFragmentListener, OnItemSelectedListener,
	FlightRouteAdapterListener, GuestPickerListener {

	public static final String TAG = FlightSearchParamsFragment.class.toString();
	private static final String TAG_PROGRESS = TAG + ".DIALOG_PROGRESS";

	private static final String ARG_INITIAL_PARAMS = "ARG_INITIAL_PARAMS";
	private static final String ARG_DIM_BACKGROUND = "ARG_DIM_BACKGROUND";

	private static final String INSTANCE_SHOW_CALENDAR = "INSTANCE_SHOW_CALENDAR";
	private static final String INSTANCE_PARAMS = "INSTANCE_PARAMS";
	private static final String INSTANCE_FIRST_LOCATION = "INSTANCE_FIRST_LOCATION";

	public static final int MAX_RECENTS = 3;

	// Controls the ratio of how large a selected EditText should take up
	// 1 == takes up the full size, 0 == takes up 50%.
	private static final float EDITTEXT_EXPANSION_RATIO = .25f;

	private static final int DATE_FORMAT_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH
		| DateUtils.FORMAT_NO_YEAR;

	private FlightSearchParamsFragmentListener mListener;

	private SimpleProgressDialogFragment mProgressDialog;

	private View mFocusStealer;
	private View mDimmerView;
	private ViewGroup mHeaderGroup;
	private ViewGroup mAirportsContainer;
	private AutoCompleteTextView mDepartureAirportEditText;
	private AutoCompleteTextView mArrivalAirportEditText;
	private Spinner mDepartureAirportSpinner;
	private Spinner mArrivalAirportSpinner;
	private TextView mDatesTextView;
	private View mClearDatesButton;
	private ViewGroup mCalendarContainer;
	private View mCalendarShadow;
	private CalendarDatePicker mCalendarDatePicker;
	private ImageButton mNumTravelersButton;
	private TextView mNumTravelersTextView;
	private PopupWindow mNumTravelersPopup;

	private ViewGroup mGuestsContainer;
	private View mGuestsLayout;
	private GuestPicker mGuestPicker;
	private TextView mInfantAlertTextView;
	private View mDoneButton;
	private View mButtonBarLayout;
	private TextView mRefinementInfoTextView;
	private RadioGroup mInfantPreferenceRadioGroup;
	private LinearLayout mInfantPreferenceLayout;

	private FlightSearchParams mSearchParams;

	private AirportDropDownAdapter mAirportAdapter;

	private FlightRouteAdapter mDepartureRouteAdapter;
	private FlightRouteAdapter mArrivalRouteAdapter;

	// Animator for calendar
	private Animation mCalendarAndGuestAnimation;

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

	// Workaround for spinner selection listener
	private boolean mJustSetSpinnerInCode;

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
		mIsTablet = ExpediaBookingApp.useTabletInterface();
		mFirstRun = savedInstanceState == null;

		mProgressDialog = (SimpleProgressDialogFragment) getChildFragmentManager().findFragmentByTag(TAG_PROGRESS);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (context instanceof FlightSearchParamsFragmentListener) {
			mListener = (FlightSearchParamsFragmentListener) context;
		}
		else {
			throw new RuntimeException("FlightSearchParamsFragment Activity requires a listener!");
		}
	}

	private boolean mItemClicked = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		boolean displayFlightDropDownRoutes = PointOfSale.getPointOfSale().displayFlightDropDownRoutes();

		// If we're using the airport dropdowns, we need a different theme for the dropdown listview style
		if (displayFlightDropDownRoutes) {
			Context newContext = new ContextThemeWrapper(getActivity(), R.style.FlightTheme_DropDownSearch);
			inflater = LayoutInflater.from(newContext);
		}

		View v = inflater.inflate(R.layout.fragment_flight_search_params, container, false);

		// We want to use one of two airport pickers depending on our POS; inflate the correct one
		int airportStubId = displayFlightDropDownRoutes ? R.id.stub_flight_search_airports_spinner
			: R.id.stub_flight_search_airports;
		Ui.inflateViewStub(v, airportStubId);

		// Cache views
		mFocusStealer = Ui.findView(v, R.id.focus_stealer);
		mDimmerView = Ui.findView(v, R.id.dimmer_view);
		mHeaderGroup = Ui.findView(v, R.id.header);
		mAirportsContainer = Ui.findView(v, R.id.airports_container);
		mDepartureAirportEditText = Ui.findView(v, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(v, R.id.arrival_airport_edit_text);
		mDepartureAirportSpinner = Ui.findView(v, R.id.departure_airport_spinner);
		mArrivalAirportSpinner = Ui.findView(v, R.id.arrival_airport_spinner);
		mDatesTextView = Ui.findView(v, R.id.dates_button);
		mCalendarContainer = Ui.findView(v, R.id.calendar_container);
		mCalendarShadow = Ui.findView(v, R.id.calendar_shadow);
		mCalendarDatePicker = Ui.findView(v, R.id.calendar_date_picker);
		mClearDatesButton = Ui.findView(v, R.id.clear_dates_btn);
		mNumTravelersButton = Ui.findView(v, R.id.num_travelers_button);
		mNumTravelersTextView = Ui.findView(v, R.id.num_travelers_text_view);

		mGuestsContainer = Ui.findView(v, R.id.guest_picker_container);
		mGuestsLayout = Ui.findView(v, R.id.guests_layout);
		mGuestPicker = Ui.findView(v, R.id.guest_picker);
		mGuestPicker.setListener(this);
		mInfantAlertTextView = Ui.findView(v, R.id.infant_alert_text_view);
		mButtonBarLayout = Ui.findView(v, R.id.button_bar_layout);
		mRefinementInfoTextView = Ui.findView(v, R.id.refinement_info_text_view);
		mInfantPreferenceRadioGroup = Ui.findView(v, R.id.infant_seating_preference_radio_group);
		mInfantPreferenceLayout = Ui.findView(v, R.id.infant_preference_seating_layout);
		mDoneButton = Ui.findView(v, R.id.guest_done_button);

		// Configure views
		if (getArguments().getBoolean(ARG_DIM_BACKGROUND)) {
			mDimmerView.setVisibility(View.VISIBLE);
		}

		// In landscape, make the calendar date picker fill the screen
		if (mIsLandscape && !mIsTablet) {
			mCalendarShadow.setVisibility(View.GONE);
			LayoutParams params = mCalendarContainer.getLayoutParams();
			params.height = LayoutParams.MATCH_PARENT;
		}

		// If it is set to MATCH_PARENT on tablet though we want
		// to clamp it to the bottom of the other search params
		if (mIsLandscape
			&& mIsTablet
			&& getResources().getDimensionPixelSize(R.dimen.flight_search_calendar_height) == LayoutParams.MATCH_PARENT) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCalendarContainer.getLayoutParams();
			lp.addRule(RelativeLayout.BELOW, R.id.header);
		}

		// Configuration for airport edit texts
		if (isUsingEditTexts()) {
			mAirportAdapter = new AirportDropDownAdapter(getActivity());

			InputFilter[] filters = new InputFilter[] {this};
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

			mArrivalAirportEditText.setImeOptions(mIsLandscape ? EditorInfo.IME_ACTION_DONE
				: EditorInfo.IME_ACTION_NEXT);

			// Always initially reset the airport texts (otherwise they start overlapping)
			resetAirportEditTexts(false);
		}

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
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID,
			LineOfBusiness.FLIGHTS);
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
				toggleGuestPicker(true, true);
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

		mDoneButton.setOnClickListener(mDoneButtonClickListener);

		mInfantPreferenceRadioGroup.setOnCheckedChangeListener(mInfantChangeListener);

		updateNumTravelersText();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);

		refreshTravelerDataToDB();

		if (isUsingEditTexts()) {
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
		else if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			if (Db.getFlightRoutes() != null) {
				onRoutesLoaded();
			}
			else {
				BackgroundDownloader bd = BackgroundDownloader.getInstance();

				if (!bd.isDownloading(CrossContextHelper.KEY_FLIGHT_ROUTES_DOWNLOAD)) {
					// Try to load the data one more time
					CrossContextHelper.updateFlightRoutesData(getActivity(), false);
				}

				if (mProgressDialog == null || !mProgressDialog.isAdded()) {
					mProgressDialog = SimpleProgressDialogFragment
						.newInstance(getString(R.string.loading_air_asia_routes));
					mProgressDialog.show(getChildFragmentManager(), TAG_PROGRESS);
				}

				// Attach a callback of our own
				bd.registerDownloadCallback(CrossContextHelper.KEY_FLIGHT_ROUTES_DOWNLOAD, mRoutesCallback);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);

		BackgroundDownloader.getInstance().unregisterDownloadCallback(CrossContextHelper.KEY_FLIGHT_ROUTES_DOWNLOAD,
			mRoutesCallback);

		if (isUsingEditTexts()) {
			// Clear adapter so we don't fire off unnecessary requests to it
			// during a configuration change
			mFirstAdapterLocation = mAirportAdapter.getLocation(0);
			mDepartureAirportEditText.setAdapter(null);
			mArrivalAirportEditText.setAdapter(null);
		}
		mFirstRun = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_SHOW_CALENDAR, mCalendarContainer.getVisibility() == View.VISIBLE);
		JSONUtils.putJSONable(outState, INSTANCE_PARAMS, mSearchParams);
		JSONUtils.putJSONable(outState, INSTANCE_FIRST_LOCATION, mFirstAdapterLocation);
	}

	public boolean onBackPressed() {
		if (mCalendarAndGuestAnimation != null && !mCalendarAndGuestAnimation.hasEnded()) {
			// Block back during animation
			return true;
		}
		else if (mCalendarContainer.getVisibility() == View.VISIBLE) {
			toggleCalendarDatePicker(false);
			return true;
		}
		else if (mGuestsContainer.getVisibility() == View.VISIBLE) {
			toggleGuestPicker(false, true);
			return true;
		}
		else {
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// View control

	private boolean isUsingEditTexts() {
		return mDepartureAirportEditText != null && mArrivalAirportEditText != null;
	}

	private OnFocusChangeListener mAirportFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v;
			if (hasFocus) {
				mAirportAdapter.setShowNearbyAirports(v == mDepartureAirportEditText);
				autoCompleteTextView.setAdapter(mAirportAdapter);
			}
			else {
				autoCompleteTextView.setAdapter(null);
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
		if (isUsingEditTexts()) {
			updateAirportText(mDepartureAirportEditText, mSearchParams.getDepartureLocation());
			updateAirportText(mArrivalAirportEditText, mSearchParams.getArrivalLocation());
		}
		else {
			updateAirportSpinner(mDepartureAirportSpinner, mSearchParams.getDepartureLocation());
			updateAirportSpinner(mArrivalAirportSpinner, mSearchParams.getArrivalLocation());
		}
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
		mAirportAdapter.setShowNearbyAirports(focusView == mDepartureAirportEditText);
		((AutoCompleteTextView)focusView).setAdapter(mAirportAdapter);
		adjustAirportEditTexts(focusView, EDITTEXT_EXPANSION_RATIO, animate);
	}

	private void resetAirportEditTexts(final boolean animate) {
		if (!isAdded()) {
			return;
		}

		adjustAirportEditTexts(mDepartureAirportEditText, .5f, animate);
	}

	@SuppressLint("NewApi")
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
		if (isUsingEditTexts()) {
			EditText textWithFocus = null;

			if (mDepartureAirportEditText.hasFocus()) {
				textWithFocus = mDepartureAirportEditText;
			}
			else if (mArrivalAirportEditText.hasFocus()) {
				textWithFocus = mArrivalAirportEditText;
			}

			if (textWithFocus != null && isAdded()) {
				mFocusStealer.requestFocus();

				Ui.hideKeyboard(getActivity());
			}

			resetAirportEditTexts(true);
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
			LocalDate dateStart = mSearchParams.getDepartureDate();
			LocalDate dateEnd = mSearchParams.getReturnDate();
			boolean researchMode = mCalendarDatePicker.getOneWayResearchMode();

			if (dateStart != null && dateEnd != null) {
				int nightCount = JodaUtils.daysBetween(dateStart, dateEnd);
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

		if (mGuestsLayout.getVisibility() == View.VISIBLE) {
			toggleGuestPicker(false, true);
		}

		if (enabled) {
			Ui.hideKeyboard(getActivity());

			LocalDate departureDate = mSearchParams.getDepartureDate();
			LocalDate returnDate = mSearchParams.getReturnDate();

			CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.HYBRID,
				LineOfBusiness.FLIGHTS);

			CalendarUtils.updateCalendarPickerStartDate(mCalendarDatePicker, departureDate);
			CalendarUtils.updateCalendarPickerEndDate(mCalendarDatePicker, returnDate);

			if (returnDate == null) {
				mCalendarDatePicker.setOneWayResearchMode(true);
			}

			// f826 Make sure calendar redraws after crash
			if (departureDate == null && returnDate == null) {
				mCalendarDatePicker.reset();
			}

			// F1213 - Show action bar because landscape takes up the entire screen otherwise
			if (mIsLandscape && !mIsTablet) {
				((FragmentActivity) getActivity()).startActionMode(mCalendarActionMode);
				if (isUsingEditTexts()) {
					mDepartureAirportEditText.setFocusable(false);
					mArrivalAirportEditText.setFocusable(false);
				}
			}

			mDatesTextView.setBackgroundResource(R.drawable.textfield_activated_holo_light);
		}
		else {
			mDatesTextView.setBackgroundResource(R.drawable.textfield_default_holo_light);

			if (isUsingEditTexts()) {
				mDepartureAirportEditText.setFocusableInTouchMode(true);
				mDepartureAirportEditText.setFocusable(true);
				mArrivalAirportEditText.setFocusableInTouchMode(true);
				mArrivalAirportEditText.setFocusable(true);
			}
		}

		if (enabled && !mIsLandscape) {
			// If all the data is available now, fix height - otherwise we have to wait for a layout
			// pass to fix the height.
			if (getView() != null && getView().getHeight() != 0) {
				fixCalendarHeight();
			}
			else {
				Ui.runOnNextLayout(mCalendarContainer, new Runnable() {
					public void run() {
						fixCalendarHeight();
						mCalendarContainer.requestLayout();
					}
				});
			}
		}

		updateCalendarInstructionText();

		mCalendarDatePicker.setTooltipSuppressed(!enabled);

		if (!animate) {
			mCalendarContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
		}
		else {
			if (enabled) {
				mCalendarContainer.setVisibility(View.VISIBLE);
				mCalendarAndGuestAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
			}
			else {
				mCalendarAndGuestAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
				mCalendarAndGuestAnimation.setAnimationListener(mCalAnimationListener);
			}

			mCalendarContainer.startAnimation(mCalendarAndGuestAnimation);
		}
	}

	private AnimationListener mGuestAnimationListener = new AnimationListenerAdapter() {
		@Override
		public void onAnimationEnd(Animation animation) {
			mGuestsContainer.setVisibility(View.GONE);
		}
	};

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
			start = JodaUtils.formatLocalDate(getActivity(), mSearchParams.getDepartureDate(), DATE_FORMAT_FLAGS);
		}

		CharSequence end = null;
		if (mSearchParams.getReturnDate() != null) {
			end = JodaUtils.formatLocalDate(getActivity(), mSearchParams.getReturnDate(), DATE_FORMAT_FLAGS);
		}
		int color = getResources().getColor(R.color.flight_departure_date_color);
		if (start == null && end == null) {
			mDatesTextView.setText(null);
		}
		else if (end == null) {
			Spannable stringToSpan = new SpannableString(getString(R.string.one_way_TEMPLATE, start));
			int endSpan = start.length();
			Ui.setTextStyleBoldText(stringToSpan, color, 0, endSpan);
			mDatesTextView.setText(stringToSpan);
		}
		else {
			Spannable stringToSpan = new SpannableString(getString(R.string.round_trip_TEMPLATE, start, end));
			int endSpan = start.length();
			Ui.setTextStyleBoldText(stringToSpan, color, 0, endSpan);
			endSpan = stringToSpan.length();
			int startSpan = endSpan - end.length();
			Ui.setTextStyleBoldText(stringToSpan, color, startSpan, endSpan);
			mDatesTextView.setText(stringToSpan);
		}
	}

	// Traveler selection methods

	private void toggleGuestPicker(boolean enabled, boolean animate) {
		if (enabled == (mGuestsContainer.getVisibility() == View.VISIBLE)) {
			return;
		}

		if (mCalendarContainer.getVisibility() == View.VISIBLE) {
			toggleCalendarDatePicker(false, false);
		}

		if (enabled) {
			clearEditTextFocus();
			Ui.hideKeyboard(getActivity());
			mGuestsLayout.setVisibility(View.VISIBLE);
			mButtonBarLayout.setVisibility(View.VISIBLE);
			displayRefinementInfo();
		}

		if (!animate) {
			mGuestsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
		}
		else {
			if (enabled) {
				mGuestsContainer.setVisibility(View.VISIBLE);
				mCalendarAndGuestAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
			}
			else {
				mCalendarAndGuestAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
				mCalendarAndGuestAnimation.setAnimationListener(mGuestAnimationListener);
			}

			mGuestsContainer.startAnimation(mCalendarAndGuestAnimation);
		}
	}

	public void displayRefinementInfo() {
		FlightSearchParams searchParams = getSearchParams(false);
		final int numAdults = searchParams.getNumAdults();
		final int numChildren = searchParams.getNumChildren();
		String text = StrUtils.formatGuests(getActivity(), numAdults, numChildren);
		mRefinementInfoTextView.setText(text);
	}

	private void onNumTravelersSelected(int num) {
		mSearchParams.setNumAdults(num);
		mNumTravelersPopup.dismiss();
		updateNumTravelersText();
	}

	private void updateNumTravelersText() {
		if (mNumTravelersTextView != null) {
			mNumTravelersTextView.setText(Integer.toString(mSearchParams.getNumAdults() + mSearchParams.getNumChildren()));
		}
	}

	private void refreshTravelerDataToDB() {
		FlightSearchParams searchParams = getSearchParams(false);
		mGuestPicker.bind(searchParams.getNumAdults(), searchParams.getChildren());

		displayRefinementInfo();
		updateNumTravelersText();
		showInfantSeatingPreferenceAsNecessary();
	}

	// Infants

	@Override
	public void onGuestsChanged(int numAdults, List<ChildTraveler> children) {
		FlightSearchParams searchParams = getSearchParams(false);
		searchParams.setNumAdults(numAdults);
		searchParams.setChildren(children);

		displayRefinementInfo();
		updateNumTravelersText();
		showInfantSeatingPreferenceAsNecessary();
		showNotEnoughLapsAsNecessary();
		updateInfantPreferenceAsNecessary();
	}

	private void updateInfantPreferenceAsNecessary() {
		if (mGuestPicker.moreInfantsThanAvailableLaps()) {
			mInfantPreferenceRadioGroup.check(R.id.infant_in_seat);
		}
	}

	private void showInfantSeatingPreferenceAsNecessary() {
		int vis = mSearchParams.hasInfants() ? View.VISIBLE : View.INVISIBLE;
		mInfantPreferenceLayout.setVisibility(vis);

		if (vis == View.VISIBLE) {
			mInfantPreferenceRadioGroup.setOnCheckedChangeListener(null);
			boolean inLap = getSearchParams(false).getInfantSeatingInLap();
			int idToCheck = inLap ? R.id.infant_in_lap : R.id.infant_in_seat;
			mInfantPreferenceRadioGroup.check(idToCheck);
			mInfantPreferenceRadioGroup.setOnCheckedChangeListener(mInfantChangeListener);
		}
	}

	private void showNotEnoughLapsAsNecessary() {
		if (mGuestPicker.moreInfantsThanAvailableLaps()) {
			mInfantAlertTextView.setVisibility(View.VISIBLE);
			mInfantPreferenceRadioGroup.getChildAt(0).setEnabled(false);
		}
		else {
			mInfantAlertTextView.setVisibility(View.INVISIBLE);
			mInfantPreferenceRadioGroup.getChildAt(0).setEnabled(true);
		}
	}

	private final RadioGroup.OnCheckedChangeListener mInfantChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			mSearchParams.setInfantSeatingInLap(checkedId == R.id.infant_in_lap);
			showNotEnoughLapsAsNecessary();
			updateListener();
		}
	};

	private final View.OnClickListener mDoneButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			toggleGuestPicker(false, true);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Access

	/**
	 * Returns the FlightSearchParams represented by this screen.
	 * <p/>
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
	// AirAsia routes stuff

	private OnDownloadComplete<RoutesResponse> mRoutesCallback = new OnDownloadComplete<RoutesResponse>() {
		@Override
		public void onDownload(RoutesResponse results) {
			if (results == null || results.hasErrors()) {
				// Throw up an error dialog that routes the user back to the start
				SimpleCallbackDialogFragment df = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.error_could_not_load_air_asia), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_FLIGHT_ROUTES_FAILURE);
				df.show(getChildFragmentManager(), "error");
			}
			else {
				// Show the results!
				onRoutesLoaded();
			}

			// Dismiss the dialog
			mProgressDialog.dismissAllowingStateLoss();
		}
	};

	private void updateAirportSpinner(Spinner spinner, Location location) {
		int position = 0;
		if (location != null) {
			FlightRouteAdapter adapter = (FlightRouteAdapter) spinner.getAdapter();
			if (adapter != null) {
				String airportCode = location.getDestinationId();
				if (!TextUtils.isEmpty(airportCode)) {
					position = adapter.getPosition(airportCode);
				}
			}
		}

		if (spinner.getSelectedItemPosition() != position) {
			spinner.setSelection(position);
			mJustSetSpinnerInCode = true;
		}
	}

	private void onRoutesLoaded() {
		mRecentRouteSearches = new RecentList<>(Location.class, getActivity(), RECENT_ROUTES_AIRPORTS_FILE, MAX_RECENTS);

		mDepartureRouteAdapter = new FlightRouteAdapter(getActivity(), Db.getFlightRoutes(), mRecentRouteSearches, true);
		mArrivalRouteAdapter = new FlightRouteAdapter(getActivity(), Db.getFlightRoutes(), mRecentRouteSearches, false);

		mDepartureAirportSpinner.setAdapter(mDepartureRouteAdapter);
		mArrivalAirportSpinner.setAdapter(mArrivalRouteAdapter);

		mDepartureAirportSpinner.setOnItemSelectedListener(this);
		mArrivalAirportSpinner.setOnItemSelectedListener(this);

		mDepartureRouteAdapter.setListener(this);
		mArrivalRouteAdapter.setListener(this);

		// #1626: Keep the arrival list properly filtered (if there's an origin selected)
		Location depLoc = mSearchParams.getDepartureLocation();
		if (mArrivalRouteAdapter != null && depLoc != null) {
			mArrivalRouteAdapter.setOrigin(depLoc.getDestinationId());
			mArrivalRouteAdapter.onDataSetChanged();
		}

		// Sync the current params with the spinners
		updateAirportText();
	}

	private void onRoutesLoadFailed() {
		getActivity().finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// AirAsia recents

	// We keep a separate (but equal) recents list for routes-based searches
	// because it's slightly different (e.g., no description)
	private static final String RECENT_ROUTES_AIRPORTS_FILE = "recent-airports-routes-list.dat";

	private RecentList<Location> mRecentRouteSearches;

	public void onAirportSelected(Location location) {
		// Don't save if it's a completely custom code and we don't have any info on it
		mRecentRouteSearches.addItem(location);

		// Update the datasets for the new recents
		mDepartureRouteAdapter.onDataSetChanged();
		mArrivalRouteAdapter.onDataSetChanged();

		// Save
		(new Thread(new Runnable() {
			@Override
			public void run() {
				mRecentRouteSearches.saveList(getActivity(), RECENT_ROUTES_AIRPORTS_FILE);
			}
		})).start();
	}

	/*
	 * This should only be called outside of when the Fragment is being shown, so we don't
	 * have to handle clearing the recents in memory.
	 */
	public static void clearRecentAirAsiaAirports(Context context) {
		File recents = context.getFileStreamPath(RECENT_ROUTES_AIRPORTS_FILE);
		if (recents != null && recents.exists()) {
			recents.delete();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnDateChangedListener

	@Override
	public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
		if (mCalendarDatePicker.getStartTime() != null) {
			mSearchParams.setDepartureDate(new LocalDate(mCalendarDatePicker.getStartYear(),
				mCalendarDatePicker.getStartMonth() + 1, mCalendarDatePicker.getStartDayOfMonth()));
		}
		else {
			mSearchParams.setDepartureDate(null);
		}

		if (mCalendarDatePicker.getEndTime() != null) {
			mSearchParams.setReturnDate(new LocalDate(mCalendarDatePicker.getEndYear(),
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
	// SimpleProgressDialogFragmentListener

	@Override
	public void onCancel() {
		onRoutesLoadFailed();
	}

	//////////////////////////////////////////////////////////////////////////
	// Otto event subscriptions

	@Subscribe
	public void onSimpleDialogClick(Events.SimpleCallBackDialogOnClick event) {
		onRoutesLoadFailed();
	}

	@Subscribe
	public void onSimpleDialogCancel(Events.SimpleCallBackDialogOnCancel event) {
		onRoutesLoadFailed();
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightRouteAdapterListener

	@Override
	public void onSpinnerClicked() {
		toggleCalendarDatePicker(false, true);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnItemSelectedListener
	//
	// For Spinners

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// Workaround: otherwise it's impossible to tell if the spinner was changed by me in code,
		// or by the user selecting a new airport.
		if (mJustSetSpinnerInCode) {
			mJustSetSpinnerInCode = false;
			return;
		}

		if (parent == mDepartureAirportSpinner) {
			Airport airport = mDepartureRouteAdapter.getAirport(position);

			if (airport != null) {
				Location location = airportToLocation(airport);
				mSearchParams.setDepartureLocation(location);
				mArrivalRouteAdapter.setOrigin(airport.mAirportCode);
				onAirportSelected(location);

				// If arrival route adapter already had a destination, remove it if no longer valid
				Location arrLocation = mSearchParams.getArrivalLocation();
				if (arrLocation != null && mArrivalRouteAdapter.getPosition(arrLocation.getDestinationId()) == 0) {
					mSearchParams.setArrivalLocation(null);
				}

				// Regardless of what happened, update the texts - the location of the destination
				// may have changed in the spinner
				updateAirportText();
			}
		}
		else {
			Airport airport = mArrivalRouteAdapter.getAirport(position);
			if (airport != null) {
				Location location = airportToLocation(airport);
				mSearchParams.setArrivalLocation(location);
				onAirportSelected(location);

				// Update the texts - the location of the departure airport may have changed in the spinner
				updateAirportText();
			}
		}

		updateListener();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// No need to do anything
	}

	private Location airportToLocation(Airport airport) {
		Location location = new Location();
		location.setDestinationId(airport.mAirportCode);
		location.setCity(airport.mCity);
		return location;
	}

	//////////////////////////////////////////////////////////////////////////
	// Interface

	private void updateListener() {
		mListener.onParamsChanged();
	}

	public interface FlightSearchParamsFragmentListener {
		void onParamsChanged();
	}
}
