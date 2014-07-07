package com.expedia.bookings.fragment;

import java.util.List;

import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsSearchControllerFragment: designed for tablet results 2014
 * This controls all the fragments relating to searching on the results screen
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsSearchControllerFragment extends Fragment implements IBackManageable,
	IStateProvider<ResultsSearchState>, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	DatesFragment.DatesFragmentListener, ResultsGuestPicker.GuestPickerFragmentListener,
	TabletWaypointFragment.ITabletWaypointFragmentListener,
	CurrentLocationFragment.ICurrentLocationListener, ResultsGdeFlightsFragment.IGdeFlightsListener {

	private static final String INSTANCE_RESULTS_SEARCH_STATE = "INSTANCE_RESULTS_SEARCH_STATE";
	private static final String INSTANCE_ANIM_FROM_ORIGIN = "INSTANCE_ANIM_FROM_ORIGIN";
	private static final String INSTANCE_LOCAL_PARAMS = "INSTANCE_LOCAL_PARAMS";

	private static final String FTAG_CALENDAR = "FTAG_CALENDAR";
	private static final String FTAG_TRAV_PICKER = "FTAG_TRAV_PICKER";
	private static final String FTAG_WAYPOINT = "FTAG_WAYPOINT";
	private static final String FTAG_ORIGIN_LOCATION = "FTAG_ORIGIN_LOCATION";
	private static final String FTAG_FLIGHTS_GDE = "FTAG_FLIGHTS_GDE";


	private GridManager mGrid = new GridManager();
	private StateManager<ResultsSearchState> mSearchStateManager = new StateManager<ResultsSearchState>(
		ResultsSearchState.CALENDAR, this);
	private ResultsSearchState mLastDownState = ResultsSearchState.CALENDAR;
	private boolean mWaypointAnimFromOrigin = true;
	private boolean mIgnoreDateChanges = false;
	private boolean mIgnoreGuestChanges = false;
	private Interpolator mCenterColumnUpDownInterpolator = new AccelerateInterpolator(1.2f);

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mSearchBarC;
	private ViewGroup mRightButtonsC;
	private ViewGroup mSearchActionsC;
	private FrameLayoutTouchController mBottomRightC;
	private FrameLayoutTouchController mBottomCenterC;

	//Fragment Containers
	private FrameLayoutTouchController mCalC;
	private FrameLayoutTouchController mTravC;
	private FrameLayoutTouchController mWaypointC;
	private FrameLayoutTouchController mGdeC;
	private View mTravPickWhiteSpace;

	//Search action buttons
	private TextView mDestBtn;
	private TextView mOrigBtn;
	private TextView mCalBtn;
	private TextView mTravBtn;

	//Search state buttons
	private TextView mClearDatesBtn;
	private TextView mCancelBtn;
	private TextView mSearchNowBtn;

	//Uncommited data
	private SearchParams mLocalParams;

	//Frags
	private ResultsWaypointFragment mWaypointFragment;
	private ResultsDatesFragment mDatesFragment;
	private ResultsGuestPicker mGuestsFragment;
	private CurrentLocationFragment mCurrentLocationFragment;
	private ResultsGdeFlightsFragment mGdeFragment;

	public TabletResultsSearchControllerFragment() {
		importParams();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		importParams();
		if (savedInstanceState != null) {
			mWaypointAnimFromOrigin = savedInstanceState.getBoolean(INSTANCE_ANIM_FROM_ORIGIN, mWaypointAnimFromOrigin);
			mSearchStateManager.setDefaultState(ResultsSearchState.valueOf(savedInstanceState.getString(
				INSTANCE_RESULTS_SEARCH_STATE,
				ResultsSearchState.DEFAULT.name())));

			if (savedInstanceState.containsKey(INSTANCE_LOCAL_PARAMS)) {
				mLocalParams = savedInstanceState.getParcelable(INSTANCE_LOCAL_PARAMS);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_search, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mSearchBarC = Ui.findView(view, R.id.search_bar_container);
		mRightButtonsC = Ui.findView(view, R.id.right_buttons_container);
		mBottomRightC = Ui.findView(view, R.id.bottom_right_container);
		mBottomCenterC = Ui.findView(view, R.id.bottom_center_container);
		mWaypointC = Ui.findView(view, R.id.waypoint_container);
		mTravC = Ui.findView(view, R.id.traveler_container);
		mCalC = Ui.findView(view, R.id.calendar_container);
		mGdeC = Ui.findView(view, R.id.gde_container);
		mTravPickWhiteSpace = Ui.findView(view, R.id.traveler_picker_port_white_space);

		//Fake AB form buttons
		mDestBtn = Ui.findView(view, R.id.dest_btn);
		mOrigBtn = Ui.findView(view, R.id.origin_btn);
		mCalBtn = Ui.findView(view, R.id.calendar_btn);
		mTravBtn = Ui.findView(view, R.id.traveler_btn);

		//Actions Container
		mSearchActionsC = Ui.findView(view, R.id.search_actions_container);
		mClearDatesBtn = Ui.findView(view, R.id.clear_dates_btn);
		mCancelBtn = Ui.findView(view, R.id.cancel_btn);
		mSearchNowBtn = Ui.findView(view, R.id.search_now_btn);

		//We dont want our clicks to pass through this container
		mGdeC.setConsumeTouch(true);

		//Fake AB actions
		mDestBtn.setOnClickListener(mDestClick);
		mOrigBtn.setOnClickListener(mOrigClick);
		mCalBtn.setOnClickListener(mCalClick);
		mTravBtn.setOnClickListener(mTravClick);

		//Search actions actions
		mClearDatesBtn.setOnClickListener(mClearDatesClick);
		mCancelBtn.setOnClickListener(mCancelClick);
		mSearchNowBtn.setOnClickListener(mSearchNowClick);

		registerStateListener(mSearchStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsSearchState>(), false);

		if (!PointOfSale.getPointOfSale().supportsFlights()) {
			mOrigBtn.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mResultsStateHelper.registerWithProvider(this, false);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
		Sp.getBus().register(this);
		Events.register(this);
		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, true);
		}
		bindSearchBtns();
	}

	@Override
	public void onPause() {
		super.onPause();
		mResultsStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
		Sp.getBus().unregister(this);
		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, false);
		}
		Events.unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_ANIM_FROM_ORIGIN, mWaypointAnimFromOrigin);
		outState.putString(INSTANCE_RESULTS_SEARCH_STATE, mSearchStateManager.getState().name());
		if (hasChanges()) {
			outState.putParcelable(INSTANCE_LOCAL_PARAMS, mLocalParams);
		}
	}


	/**
	 * BINDING STUFF
	 */

	public void bindSearchBtns() {
		//TODO: Improve string formats

		//Origin/Destination - Note that these come strait from Params
		if (mLocalParams.hasDestination() && mLocalParams.getDestination().getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION) {
			mDestBtn.setText(R.string.current_location);

		}
		else if (mLocalParams.hasDestination()) {
			mDestBtn.setText(Html.fromHtml(mLocalParams.getDestination().getDisplayName()).toString());
		}
		else {
			mDestBtn.setText("");
		}

		if (mLocalParams.hasOrigin()) {
			mOrigBtn.setText(getString(R.string.fly_from_TEMPLATE, mLocalParams.getOrigin().getAirportCode()));
		}
		else {
			mOrigBtn.setText("");
		}

		//Calendar button stuff
		bindCalBtn();

		//Traveler number stuff
		bindTravBtn();

	}

	private void bindCalBtn() {
		if (mLocalParams != null && mLocalParams.getStartDate() != null) {
			String dateStr;
			int flags = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;
			if (mLocalParams.getEndDate() != null) {
				dateStr = JodaUtils
					.formatDateRange(getActivity(), mLocalParams.getStartDate(), mLocalParams.getEndDate(), flags);
			}
			else {
				dateStr = JodaUtils.formatLocalDate(getActivity(), mLocalParams.getStartDate(), flags);
			}
			mCalBtn.setText(dateStr);
		}
		else {
			if (PointOfSale.getPointOfSale().supportsFlights()) {
				mCalBtn.setText(R.string.choose_flight_dates);
			}
			else {
				mCalBtn.setText(R.string.choose_dates);
			}
		}
	}

	private void bindTravBtn() {
		int numTravelers = mLocalParams.getNumAdults() + mLocalParams.getNumChildren();
		String travStr = getResources()
			.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers);
		mTravBtn.setText(travStr);
	}

	public void hideSearchBtns() {
		mTravBtn.setVisibility(View.INVISIBLE);
		mCalBtn.setVisibility(View.INVISIBLE);
	}

	public void showSearchBtns() {
		mTravBtn.setVisibility(View.VISIBLE);
		mCalBtn.setVisibility(View.VISIBLE);
	}

	protected void doSpUpdate() {
		if (getActivity() != null && isAdded() && isResumed()) {
			Sp.reportSpUpdate();
		}
	}

	protected boolean copyTempValuesToParams() {
		if (hasChanges()) {
			Sp.setParams(mLocalParams, false);
			return true;
		}
		return false;
	}

	protected boolean hasChanges() {
		return !mLocalParams.equals(Sp.getParams());
	}

	protected void clearChanges() {
		if (hasChanges()) {
			importParams();
			bindSearchBtns();
			guestsChangeHelper(mLocalParams.getNumAdults(), mLocalParams.getChildTravelers());
			dateChangeHelper(mLocalParams.getStartDate(), mLocalParams.getEndDate());
		}
	}

	protected void importParams() {
		mLocalParams = new SearchParams(Sp.getParams());
	}

	/**
	 * FRAG LISTENERS
	 */

	@Override
	public void onGuestsChanged(int numAdults, List<ChildTraveler> children, boolean infantsInLaps) {
		guestsChangeHelper(numAdults, children, infantsInLaps);
	}

	@Override
	public void onGuestsChanged(int numAdults, List<ChildTraveler> children) {
		guestsChangeHelper(numAdults, children);
	}

	private void guestsChangeHelper(int numAdults, List<ChildTraveler> children) {
		guestsChangeHelper(numAdults, children, !GuestsPickerUtils.moreInfantsThanAvailableLaps(numAdults, children));
	}

	private void guestsChangeHelper(int numAdults, List<ChildTraveler> children, boolean infantsInLaps) {
		if (!mIgnoreGuestChanges) {
			mIgnoreGuestChanges = true;

			if (mGuestsFragment != null) {
				mGuestsFragment.initializeGuests(numAdults, children);
				mGuestsFragment.bind();
			}

			mLocalParams.setNumAdults(numAdults);
			mLocalParams.setChildTravelers(children);
			mLocalParams.setInfantsInLaps(infantsInLaps);
			bindTravBtn();

			mIgnoreGuestChanges = false;
		}
	}

	private void dateChangeHelper(LocalDate startDate, LocalDate endDate) {
		if (!mIgnoreDateChanges) {
			mIgnoreDateChanges = true;

			if (mDatesFragment != null) {
				mDatesFragment.setDates(startDate, endDate);
			}

			if (mGdeFragment != null) {
				mGdeFragment
					.setGdeInfo(mLocalParams.getOriginLocation(true), mLocalParams.getDestinationLocation(true),
						startDate);
			}

			mLocalParams.setStartDate(startDate);
			mLocalParams.setEndDate(endDate);
			bindCalBtn();

			mIgnoreDateChanges = false;
		}
	}


	@Override
	public void onDatesChanged(LocalDate startDate, LocalDate endDate) {
		dateChangeHelper(startDate, endDate);
	}

	@Override
	public void onGdeFirstDateSelected(LocalDate date) {
		dateChangeHelper(date, null);
	}

	@Override
	public void onGdeOneWayTrip(LocalDate date) {
		dateChangeHelper(date, null);
		if (copyTempValuesToParams()) {
			doSpUpdate();
		}
		setState(ResultsSearchState.DEFAULT, true);
	}

	@Override
	public void onGdeTwoWayTrip(LocalDate depDate, LocalDate retDate) {
		dateChangeHelper(depDate, retDate);
		if (copyTempValuesToParams()) {
			doSpUpdate();
		}
		setState(ResultsSearchState.DEFAULT, true);
	}


	/*
	 * SEARCH BAR BUTTON STUFF
	 */

	private final boolean mAnimateButtonClicks = true;

	private View.OnClickListener mDestClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			setState(ResultsSearchState.DESTINATION, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mOrigClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			setState(ResultsSearchState.FLIGHT_ORIGIN, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mCalClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (mSearchStateManager.getState() != ResultsSearchState.CALENDAR) {
				setState(ResultsSearchState.CALENDAR, mAnimateButtonClicks);
			}
			else {
				setState(ResultsSearchState.DEFAULT, mAnimateButtonClicks);
			}
		}
	};

	private View.OnClickListener mTravClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (mSearchStateManager.getState() != ResultsSearchState.TRAVELER_PICKER) {
				setState(ResultsSearchState.TRAVELER_PICKER, mAnimateButtonClicks);
			}
			else {
				setState(ResultsSearchState.DEFAULT, mAnimateButtonClicks);
			}
		}
	};

	private View.OnClickListener mClearDatesClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			ResultsSearchState state = getState();
			if (state == ResultsSearchState.TRAVELER_PICKER || state == ResultsSearchState.CALENDAR) {
				dateChangeHelper(null, null);
			}
		}
	};

	private View.OnClickListener mCancelClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (getState() == ResultsSearchState.CALENDAR || getState() == ResultsSearchState.TRAVELER_PICKER) {
				clearChanges();
				setState(ResultsSearchState.DEFAULT, true);
			}
		}
	};

	private View.OnClickListener mSearchNowClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (getState() == ResultsSearchState.CALENDAR || getState() == ResultsSearchState.TRAVELER_PICKER) {
				if (copyTempValuesToParams()) {
					doSpUpdate();
				}
				setState(ResultsSearchState.DEFAULT, true);
			}
		}
	};


	/*
	 * SEARCH STATE LISTENER
	 */

	public ResultsSearchState getState() {
		return mSearchStateManager.getState();
	}

	public void setState(ResultsSearchState state, boolean animate) {
		ResultsSearchState curState = mSearchStateManager.getState();
		if (animate && stateShowsWidget(curState) && stateShowsWaypoint(state)) {
			mSearchStateManager.animateThroughStates(200, false, ResultsSearchState.DEFAULT, state);
		}
		else {
			mSearchStateManager.setState(state, animate);
		}
	}

	public boolean stateShowsWaypoint(ResultsSearchState state) {
		return state == ResultsSearchState.FLIGHT_ORIGIN || state == ResultsSearchState.DESTINATION;
	}

	private boolean stateShowsWidget(ResultsSearchState state) {
		return state == ResultsSearchState.TRAVELER_PICKER || state == ResultsSearchState.CALENDAR;
	}

	private StateListenerHelper<ResultsSearchState> mSearchStateHelper = new StateListenerHelper<ResultsSearchState>() {

		@Override
		public void onStateTransitionStart(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			if (performingSlideUpOrDownTransition(stateOne, stateTwo)) {
				setSlideUpAnimationHardwareLayers(true);
				if (isHotelsUpTransition(stateOne, stateTwo)) {
					//For hotels we also fade
					setSlideUpHotelsOnlyHardwareLayers(true);
				}

			}
			else {
				if (stateShowsWaypoint(stateOne) || stateShowsWaypoint(stateTwo)) {
					mWaypointC.setVisibility(View.VISIBLE);

					//Here we set up where the search bar animation will originate from
					if (stateOne == ResultsSearchState.FLIGHT_ORIGIN || stateTwo == ResultsSearchState.FLIGHT_ORIGIN) {
						mWaypointAnimFromOrigin = true;
					}
					else {
						mWaypointAnimFromOrigin = false;
					}
				}
			}

			if (stateOne == ResultsSearchState.CALENDAR || stateTwo == ResultsSearchState.CALENDAR) {
				mBottomRightC.setVisibility(View.VISIBLE);
				mBottomCenterC.setVisibility(View.VISIBLE);
				mCalC.setVisibility(View.VISIBLE);
				mGdeC.setVisibility(View.VISIBLE);
				mSearchActionsC.setVisibility(View.VISIBLE);

				if ((mDatesFragment == null || mDatesFragment.isDetached()) || (mGdeFragment == null || mGdeFragment
					.isDetached())) {
					FragmentManager manager = getChildFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					mDatesFragment = FragmentAvailabilityUtils.setFragmentAvailability(true, FTAG_CALENDAR, manager,
						transaction, TabletResultsSearchControllerFragment.this, R.id.calendar_container, true);
					mGdeFragment = FragmentAvailabilityUtils.setFragmentAvailability(PointOfSale.getPointOfSale().supportsFlights(), FTAG_FLIGHTS_GDE, manager,
						transaction, TabletResultsSearchControllerFragment.this, R.id.gde_container, true);
					transaction.commit();
				}
			}

			if (stateOne == ResultsSearchState.TRAVELER_PICKER || stateTwo == ResultsSearchState.TRAVELER_PICKER) {
				mBottomRightC.setVisibility(View.VISIBLE);
				mTravC.setVisibility(View.VISIBLE);
				if (!mGrid.isLandscape()) {
					mBottomCenterC.setVisibility(View.VISIBLE);
					mTravPickWhiteSpace.setVisibility(View.VISIBLE);
				}
				mSearchActionsC.setVisibility(View.VISIBLE);

				if (mGuestsFragment == null || mGuestsFragment.isDetached()) {
					FragmentManager manager = getChildFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					mGuestsFragment = FragmentAvailabilityUtils.setFragmentAvailability(true, FTAG_TRAV_PICKER, manager,
						transaction, TabletResultsSearchControllerFragment.this, R.id.traveler_container, false);
					transaction.commit();
				}
			}

			if (stateTwo == ResultsSearchState.FLIGHT_ORIGIN) {
				mWaypointFragment.updateViewsForOrigin();
			}
			else if (stateTwo == ResultsSearchState.DESTINATION) {
				mWaypointFragment.updateViewsForDestination();
			}

			setActionbarShowingState(stateTwo);
		}

		@Override
		public void onStateTransitionUpdate(ResultsSearchState stateOne, ResultsSearchState stateTwo,
			float percentage) {

			if (performingSlideUpOrDownTransition(stateOne, stateTwo)) {
				float perc = goingUp(stateOne, stateTwo) ? percentage : (1f - percentage);
				setSlideUpAnimationPercentage(perc);
				if (isHotelsUpTransition(stateOne, stateTwo)) {
					//For hotels we also fade
					setSlideUpHotelsOnlyAnimationPercentage(perc);
				}
			}
			else {
				int dist = mGrid.isLandscape() ? 1 : 2;
				if (stateOne == ResultsSearchState.DEFAULT && stateTwo == ResultsSearchState.CALENDAR) {
					mBottomRightC.setTranslationY((1f - percentage) * dist * mBottomRightC.getHeight());
					mBottomCenterC.setTranslationY((1f - percentage) * dist * mBottomCenterC.getHeight());
					mSearchActionsC.setTranslationX((1f - percentage) * mSearchActionsC.getWidth());
				}
				else if (stateOne == ResultsSearchState.CALENDAR && stateTwo == ResultsSearchState.DEFAULT) {
					mBottomRightC.setTranslationY(percentage * dist * mBottomRightC.getHeight());
					mBottomCenterC.setTranslationY(percentage * dist * mBottomCenterC.getHeight());
					mSearchActionsC.setTranslationX(percentage * mSearchActionsC.getWidth());
				}
				else if (stateOne == ResultsSearchState.DEFAULT && stateTwo == ResultsSearchState.TRAVELER_PICKER) {
					mBottomRightC.setTranslationY((1f - percentage) * dist * mBottomRightC.getHeight());
					mBottomCenterC.setTranslationY((1f - percentage) * dist * mBottomCenterC.getHeight());
					mSearchActionsC.setTranslationX((1f - percentage) * mSearchActionsC.getWidth());
				}
				else if (stateOne == ResultsSearchState.TRAVELER_PICKER && stateTwo == ResultsSearchState.DEFAULT) {
					mBottomRightC.setTranslationY(percentage * dist * mBottomRightC.getHeight());
					mBottomCenterC.setTranslationY(percentage * dist * mBottomCenterC.getHeight());
					mSearchActionsC.setTranslationX(percentage * mSearchActionsC.getWidth());
				}
				else if (stateOne == ResultsSearchState.TRAVELER_PICKER && stateTwo == ResultsSearchState.CALENDAR) {
					mTravC.setTranslationX(percentage * mTravC.getWidth());
					mCalC.setTranslationX((1f - percentage) * -mCalC.getWidth());
					if (mGrid.isLandscape()) {
						mGdeC.setTranslationY((1f - percentage) * mBottomCenterC.getHeight());
					}
					else {
						mGdeC.setTranslationX((1f - percentage) * -mBottomCenterC.getWidth());
						mTravPickWhiteSpace.setTranslationX(percentage * mBottomCenterC.getWidth());
					}
				}
				else if (stateOne == ResultsSearchState.CALENDAR && stateTwo == ResultsSearchState.TRAVELER_PICKER) {
					mTravC.setTranslationX((1f - percentage) * mTravC.getWidth());
					mCalC.setTranslationX(percentage * -mCalC.getWidth());
					if (mGrid.isLandscape()) {
						mGdeC.setTranslationY(percentage * mBottomCenterC.getHeight());
					}
					else {
						mGdeC.setTranslationX(percentage * -mBottomCenterC.getWidth());
						mTravPickWhiteSpace.setTranslationX((1f - percentage) * mBottomCenterC.getWidth());
					}
				}
				else if (stateOne == ResultsSearchState.DEFAULT && stateShowsWaypoint(stateTwo)
					|| stateShowsWaypoint(stateOne) && stateTwo == ResultsSearchState.DEFAULT) {
					float p = stateOne == ResultsSearchState.DEFAULT ? 1f - percentage : percentage;

					mBottomRightC.setAlpha(p);
					mBottomCenterC.setAlpha(p);
					mSearchActionsC.setAlpha(p);
					mSearchBarC.setAlpha(p);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			if (performingSlideUpOrDownTransition(stateOne, stateTwo)) {
				setSlideUpAnimationHardwareLayers(false);
				if (isHotelsUpTransition(stateOne, stateTwo)) {
					//For hotels we also fade
					setSlideUpHotelsOnlyHardwareLayers(false);
				}
			}
		}

		@Override
		public void onStateFinalized(ResultsSearchState state) {
			setActionbarShowingState(state);
			setFragmentState(state);
			setVisibilitiesForState(state);
			resetWidgetTranslations();

			switch (state) {
			case HOTELS_UP: {
				setSlideUpAnimationPercentage(1f);
				setSlideUpHotelsOnlyAnimationPercentage(1f);
				break;
			}
			case FLIGHTS_UP: {
				setSlideUpAnimationPercentage(1f);
				setSlideUpHotelsOnlyAnimationPercentage(0f);
				break;
			}
			default: {
				setSlideUpAnimationPercentage(0f);
				setSlideUpHotelsOnlyAnimationPercentage(0f);
			}
			}

			if (state == ResultsSearchState.CALENDAR) {
				if (mGdeFragment != null) {
					mGdeFragment
						.setGdeInfo(mLocalParams.getOriginLocation(true), mLocalParams.getDestinationLocation(true),
							mLocalParams.getStartDate());
				}
				mClearDatesBtn.setVisibility(View.VISIBLE);
			}
			else {
				mClearDatesBtn.setVisibility(View.GONE);
			}

			if (state != ResultsSearchState.CALENDAR && state != ResultsSearchState.TRAVELER_PICKER) {
				clearChanges();
			}

			if (!isUpState(state)) {
				mLastDownState = state;
			}
		}

		private boolean isUpState(ResultsSearchState state) {
			return state == ResultsSearchState.FLIGHTS_UP || state == ResultsSearchState.HOTELS_UP;
		}

		private boolean performingSlideUpOrDownTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			boolean goingUp = goingUp(stateOne, stateTwo);
			boolean goingDown = goingDown(stateOne, stateTwo);

			return goingUp || goingDown;
		}

		private boolean goingUp(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			return !isUpState(stateOne) && isUpState(stateTwo);
		}

		private boolean goingDown(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			return isUpState(stateOne) && !isUpState(stateTwo);
		}

		private boolean isHotelsUpTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			return stateOne == ResultsSearchState.HOTELS_UP || stateTwo == ResultsSearchState.HOTELS_UP;
		}

		private void setSlideUpAnimationHardwareLayers(boolean enabled) {
			int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

			mBottomRightC.setLayerType(layerType, null);
			mBottomCenterC.setLayerType(layerType, null);
			mDestBtn.setLayerType(layerType, null);
			mSearchActionsC.setLayerType(layerType, null);
		}

		private void setSlideUpAnimationPercentage(float percentage) {
			//Grid manager dimensions work before onMeasure
			int searchBarHeight = mGrid.getRowHeight(3);
			int widgetHeight = mGrid.getRowSpanHeight(0, 4);
			int barTransDistance = mGrid.getRowSpanHeight(0, 3);

			mSearchBarC.setTranslationY(percentage * -barTransDistance);
			mSearchBarC.getBackground().setAlpha((int)(255f * (1f - percentage)));
			mSearchActionsC.setTranslationY(percentage * -barTransDistance);
			if (mGrid.isLandscape()) {
				mBottomRightC.setTranslationY(percentage * widgetHeight);
				mBottomCenterC.setTranslationY(mCenterColumnUpDownInterpolator.getInterpolation(percentage) * widgetHeight);
			}
			else {
				mBottomRightC.setTranslationY(percentage * 2 * mBottomRightC.getHeight());
				mBottomCenterC.setTranslationY(percentage * 2 * mBottomCenterC.getHeight());
			}
			mDestBtn.setTranslationY(percentage * searchBarHeight);
			mDestBtn.setAlpha(1f - percentage);

			if (mGrid.isLandscape()) {
				// This is only to ensure the search controls shift left of the overflow menu. This
				// overflow menu is only present for HockeyApp builds.
				if (!AndroidUtils.isRelease(getActivity())) {
					mRightButtonsC.setTranslationX(percentage * -searchBarHeight);
				}
			}
			else {
				mRightButtonsC.setTranslationY(percentage * searchBarHeight);
			}
		}

		private void setSlideUpHotelsOnlyHardwareLayers(boolean enabled) {
			int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mWaypointC.setLayerType(layerType, null);
		}

		private void setSlideUpHotelsOnlyAnimationPercentage(float percentage) {
			mOrigBtn.setAlpha(1f - percentage);
		}

		private void setActionbarShowingState(ResultsSearchState state) {
			if (stateShowsWaypoint(state)) {
				getActivity().getActionBar().hide();
			}
			else {
				getActivity().getActionBar().show();
			}
		}

		private void setVisibilitiesForState(ResultsSearchState state) {
			mWaypointC.setVisibility(stateShowsWaypoint(state) ? View.VISIBLE : View.INVISIBLE);
			mTravC.setVisibility(state == ResultsSearchState.TRAVELER_PICKER ? View.VISIBLE : View.INVISIBLE);
			mCalC.setVisibility(state == ResultsSearchState.CALENDAR ? View.VISIBLE : View.INVISIBLE);
			mBottomRightC.setVisibility(
				mCalC.getVisibility() == View.VISIBLE || mTravC.getVisibility() == View.VISIBLE ? View.VISIBLE
					: View.INVISIBLE
			);

			mBottomCenterC.setVisibility(mCalC.getVisibility());
			mGdeC.setVisibility(mCalC.getVisibility());

			if (!mGrid.isLandscape()) {
				if (state == ResultsSearchState.TRAVELER_PICKER) {
					mBottomCenterC.setVisibility(View.VISIBLE);
					mTravPickWhiteSpace.setVisibility(View.VISIBLE);
				}
			}
			mSearchActionsC.setVisibility(
				state == ResultsSearchState.CALENDAR || state == ResultsSearchState.TRAVELER_PICKER ? View.VISIBLE
					: View.INVISIBLE
			);

			mCalBtn.setVisibility(
				(state == ResultsSearchState.HOTELS_UP && mLocalParams.getStartDate() == null) ? View.GONE
					: View.VISIBLE
			);
		}

		private void resetWidgetTranslations() {
			//These are only altered for animations, and we dont want things to get into odd places.
			mCalC.setTranslationX(0f);
			mCalC.setTranslationY(0f);
			mGdeC.setTranslationX(0f);
			mGdeC.setTranslationY(0f);
			mTravC.setTranslationX(0f);
			mTravC.setTranslationY(0f);
			mBottomRightC.setTranslationX(0f);
			mBottomRightC.setTranslationY(0f);
			mBottomCenterC.setTranslationY(0f);
			mBottomCenterC.setTranslationX(0f);
			mSearchActionsC.setTranslationX(0f);
			mSearchActionsC.setTranslationY(0f);
		}
	};


	/**
	 * FRAGMENT PROVIDER
	 */

	private void setFragmentState(ResultsSearchState state) {
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = manager.beginTransaction();

		//We want most of our frags available so they can animate in real nice like
		boolean mParamFragsAvailable = state != ResultsSearchState.HOTELS_UP && state != ResultsSearchState.FLIGHTS_UP;

		boolean mCalAvail = mParamFragsAvailable;
		boolean mGdeAvail = PointOfSale.getPointOfSale().supportsFlights(); //Always follows the POS
		boolean mTravAvail = mParamFragsAvailable;
		boolean mWaypointAvailable = mParamFragsAvailable;


		mDatesFragment = FragmentAvailabilityUtils.setFragmentAvailability(mCalAvail, FTAG_CALENDAR, manager,
			transaction, this, R.id.calendar_container, true);

		mGdeFragment = FragmentAvailabilityUtils.setFragmentAvailability(mGdeAvail, FTAG_FLIGHTS_GDE, manager,
			transaction, this, R.id.gde_container, true);

		mGuestsFragment = FragmentAvailabilityUtils.setFragmentAvailability(mTravAvail, FTAG_TRAV_PICKER, manager,
			transaction, this, R.id.traveler_container, false);

		mWaypointFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mWaypointAvailable, FTAG_WAYPOINT, manager,
				transaction, this, R.id.waypoint_container, false);

		mCurrentLocationFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(!mLocalParams.hasOrigin() && mGdeAvail, FTAG_ORIGIN_LOCATION, manager, transaction, this, 0,
				true);


		transaction.commit();
	}

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_CALENDAR) {
			return mDatesFragment;
		}
		else if (tag == FTAG_TRAV_PICKER) {
			return mGuestsFragment;
		}
		else if (tag == FTAG_WAYPOINT) {
			return mWaypointFragment;
		}
		else if (tag == FTAG_ORIGIN_LOCATION) {
			return mCurrentLocationFragment;
		}
		else if (tag == FTAG_FLIGHTS_GDE) {
			return mGdeFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_CALENDAR) {
			return new ResultsDatesFragment();
		}
		else if (tag == FTAG_TRAV_PICKER) {
			return ResultsGuestPicker.newInstance(mLocalParams.getNumAdults(), mLocalParams.getChildTravelers());
		}
		else if (tag == FTAG_WAYPOINT) {
			return new ResultsWaypointFragment();
		}
		else if (tag == FTAG_ORIGIN_LOCATION) {
			return new CurrentLocationFragment();
		}
		else if (tag == FTAG_FLIGHTS_GDE) {
			return ResultsGdeFlightsFragment.newInstance();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_CALENDAR) {
			((ResultsDatesFragment) frag).setDates(mLocalParams.getStartDate(), mLocalParams.getEndDate());
		}
		else if (tag == FTAG_ORIGIN_LOCATION) {
			if (!mLocalParams.hasOrigin()) {
				//Will notify listener
				((CurrentLocationFragment) frag).getCurrentLocation();
			}
		}
		else if (tag == FTAG_FLIGHTS_GDE) {
			((ResultsGdeFlightsFragment) frag).setGdeInfo(mLocalParams.getOriginLocation(true),
				mLocalParams.getDestinationLocation(true), mLocalParams.getStartDate());
		}
	}


	/*
	 * RESULTS STATE LISTENER
	 */

	public StateListenerHelper<ResultsState> getResultsListener() {
		return mResultsStateHelper;
	}

	private StateListenerHelper<ResultsState> mResultsStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			if (reactToTransition(stateOne, stateTwo)) {
				startStateTransition(translateState(stateOne), translateState(stateTwo));
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (reactToTransition(stateOne, stateTwo)) {
				updateStateTransition(translateState(stateOne), translateState(stateTwo), percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			if (reactToTransition(stateOne, stateTwo)) {
				endStateTransition(translateState(stateOne), translateState(stateTwo));
			}
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			ResultsSearchState lastState = mSearchStateManager.getState();
			ResultsSearchState newState = translateState(state);
			boolean lastStateUp = (lastState == ResultsSearchState.FLIGHTS_UP
				|| lastState == ResultsSearchState.HOTELS_UP);
			boolean newStateUp = (newState == ResultsSearchState.FLIGHTS_UP
				|| newState == ResultsSearchState.HOTELS_UP);

			//If we have not yet set a state, or if the Results state is moving between modes, we update our state, otherwise
			//results state doesnt matter to us.
			if (lastStateUp != newStateUp) {
				setState(newState, false);
			}
			else if (!mSearchStateManager.hasState()) {
				setState(mSearchStateManager.getState(), false);
			}
		}

		public boolean reactToTransition(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && (stateTwo == ResultsState.FLIGHTS
				|| stateTwo == ResultsState.HOTELS)) {
				return true;
			}
			else if ((stateOne == ResultsState.FLIGHTS || stateOne == ResultsState.HOTELS)
				&& stateTwo == ResultsState.OVERVIEW) {
				return true;
			}
			return false;
		}

		public ResultsSearchState translateState(ResultsState state) {
			switch (state) {
			case FLIGHTS: {
				return ResultsSearchState.FLIGHTS_UP;
			}
			case HOTELS: {
				return ResultsSearchState.HOTELS_UP;
			}
			default: {
				if (Db.getTripBucket().size() == 0 && mLastDownState != null) {
					return mLastDownState;
				}
				else {
					return ResultsSearchState.DEFAULT;
				}
			}
			}
		}
	};


	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {
		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			if (isLandscape) {
				mGrid.setDimensions(totalWidth, totalHeight);
				mGrid.setNumRows(5); // 0 - 3 = top half, 4 = bottom half, 0 = AB, 1 = space, 2 = AB height above 3, 3 = AB (down)
				mGrid.setNumCols(5); // 3 columns, 2 spacers

				int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
				mGrid.setColumnSize(1, spacerSize);
				mGrid.setColumnSize(3, spacerSize);

				mGrid.setRowSize(0, getActivity().getActionBar().getHeight());
				mGrid.setRowSize(2, getActivity().getActionBar().getHeight());
				mGrid.setRowSize(3, getActivity().getActionBar().getHeight());
				mGrid.setRowPercentage(4, getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1));

				mGrid.setContainerToRow(mSearchBarC, 3);
				mGrid.setContainerToRow(mSearchActionsC, 2);
				mGrid.setContainerToRowSpan(mWaypointC, 0, 4);
				mGrid.setContainerToRow(mBottomRightC, 4);
				mGrid.setContainerToColumn(mBottomRightC, 4);
				mGrid.setContainerToRow(mBottomCenterC, 4);
				mGrid.setContainerToColumn(mBottomCenterC, 2);
			}
			else {
				mGrid.setDimensions(totalWidth, totalHeight);

				mGrid.setNumRows(6);
				mGrid.setNumCols(3); // 2 columns, 1 spacer

				int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
				mGrid.setColumnSize(1, spacerSize);

				mGrid.setRowSize(0, getActivity().getActionBar().getHeight());
				mGrid.setRowSize(2, getActivity().getActionBar().getHeight());
				mGrid.setRowSize(3, 2 * getActivity().getActionBar().getHeight());
				mGrid.setRowPercentage(4, getResources().getFraction(R.fraction.results_grid_half_bottom_half, 1, 1));
				mGrid.setRowPercentage(5, getResources().getFraction(R.fraction.results_grid_half_bottom_half, 1, 1));

				mGrid.setContainerToRow(mSearchActionsC, 2);
				mGrid.setContainerToRow(mSearchBarC, 3);
				mGrid.setContainerToRowSpan(mWaypointC, 0, 5);
				mGrid.setContainerToRow(mBottomRightC, 4);
				mGrid.setContainerToColumn(mBottomRightC, 2);

				mGrid.setContainerToRow(mBottomCenterC, 5);
				mGrid.setContainerToColumn(mBottomCenterC, 2);
			}
		}
	};

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			ResultsSearchState state = mSearchStateManager.getState();
			if (state == ResultsSearchState.FLIGHTS_UP || state == ResultsSearchState.HOTELS_UP) {
				return false;
			}
			else if (state != ResultsSearchState.DEFAULT) {
				setState(ResultsSearchState.DEFAULT, true);
				return true;
			}
			return false;
		}

	};


	/*
	 * RESULTS SEARCH STATE PROVIDER
	 */

	private StateListenerCollection<ResultsSearchState> mLis = new StateListenerCollection<ResultsSearchState>();

	@Override
	public void startStateTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
		mLis.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo, float percentage) {
		mLis.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
		mLis.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsSearchState state) {
		mLis.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsSearchState> listener, boolean fireFinalizeState) {
		mLis.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsSearchState> listener) {
		mLis.unRegisterStateListener(listener);
	}

	/**
	 * ITabletWaypointFragmentListener
	 */

	@Override
	public Rect getAnimOrigin() {
		if (mWaypointAnimFromOrigin || mSearchStateManager.getState() == ResultsSearchState.FLIGHT_ORIGIN) {
			return ScreenPositionUtils.getGlobalScreenPosition(mOrigBtn);
		}
		else {
			return ScreenPositionUtils.getGlobalScreenPosition(mDestBtn);
		}
	}

	@Override
	public void onCurrentLocation(Location location, SuggestionV2 suggestion) {
		// Let's not update the origin when destination is CURRENT_LOCATION
		if (!mLocalParams.hasOrigin() && mLocalParams.getDestination().getResultType() != SuggestionV2.ResultType.CURRENT_LOCATION) {
			mLocalParams.setOrigin(suggestion);
			if (copyTempValuesToParams()) {
				doSpUpdate();
			}
		}
	}

	@Override
	public void onCurrentLocationError(int errorCode) {
		//TODO: HANDLE ERRORS?
	}

	/*
	 * Otto events
	 */

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		importParams();
		bindSearchBtns();
	}

	@Subscribe
	public void onSearchSuggestionSelected(Events.SearchSuggestionSelected event) {
		if (event.suggestion != null && (mSearchStateManager.getState() == ResultsSearchState.FLIGHT_ORIGIN
			|| mSearchStateManager.getState() == ResultsSearchState.DESTINATION)) {
			boolean usingOrigin = mSearchStateManager.getState() == ResultsSearchState.FLIGHT_ORIGIN;
			if (usingOrigin) {
				mLocalParams.setOrigin(event.suggestion);
			}
			else {
				mLocalParams.setDestination(event.suggestion);
				if (!TextUtils.isEmpty(event.queryText)) {
					mLocalParams.setCustomDestinationQryText(event.queryText);
				}
				else {
					mLocalParams.setDefaultCustomDestinationQryText();
				}
			}
			if (copyTempValuesToParams()) {
				doSpUpdate();
			}
		}
		setState(ResultsSearchState.DEFAULT, true);
	}

}
