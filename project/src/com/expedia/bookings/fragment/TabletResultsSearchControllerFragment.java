package com.expedia.bookings.fragment;

import java.util.ArrayList;

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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsSearchControllerFragment: designed for tablet results 2014
 * This controls all the fragments relating to searching on the results screen
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsSearchControllerFragment extends Fragment implements IBackManageable,
	IStateProvider<ResultsSearchState>, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	DatesFragment.DatesFragmentListener, GuestsDialogFragment.GuestsDialogFragmentListener,
	TabletWaypointFragment.ITabletWaypointFragmentListener,
	CurrentLocationFragment.ICurrentLocationListener {

	private static final String STATE_RESULTS_SEARCH_STATE = "STATE_RESULTS_SEARCH_STATE";
	private static final String STATE_ANIM_FROM_ORIGIN = "STATE_ANIM_FROM_ORIGIN";

	private static final String FTAG_CALENDAR = "FTAG_CALENDAR";
	private static final String FTAG_TRAV_PICKER = "FTAG_TRAV_PICKER";
	private static final String FTAG_WAYPOINT = "FTAG_WAYPOINT";
	private static final String FTAG_ORIGIN_LOCATION = "FTAG_ORIGIN_LOCATION";


	private GridManager mGrid = new GridManager();
	private StateManager<ResultsSearchState> mSearchStateManager = new StateManager<ResultsSearchState>(
		ResultsSearchState.DEFAULT, this);
	private boolean mWaypointAnimFromOrigin = true;

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mSearchBarC;
	private ViewGroup mRightButtonsC;
	private FrameLayoutTouchController mBottomRightC;
	private FrameLayoutTouchController mBottomCenterC;
	private View mBottomRightBg;
	private View mBottomCenterBg;
	//Fragment Containers
	private FrameLayoutTouchController mCalC;
	private FrameLayoutTouchController mTravC;
	private FrameLayoutTouchController mWaypointC;
	private FrameLayoutTouchController mGdeC;


	//Search action buttons
	private TextView mDestBtn;
	private TextView mOrigBtn;
	private TextView mCalBtn;
	private TextView mTravBtn;

	//Frags
	private ResultsWaypointFragment mWaypointFragment;
	private ResultsDatesFragment mDatesFragment;
	private ResultsGuestPicker mGuestsFragment;
	private CurrentLocationFragment mCurrentLocationFragment;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mWaypointAnimFromOrigin = savedInstanceState.getBoolean(STATE_ANIM_FROM_ORIGIN, mWaypointAnimFromOrigin);
			mSearchStateManager.setDefaultState(ResultsSearchState.valueOf(savedInstanceState.getString(
				STATE_RESULTS_SEARCH_STATE,
				ResultsSearchState.DEFAULT.name())));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_search, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mSearchBarC = Ui.findView(view, R.id.search_bar_conatiner);
		mRightButtonsC = Ui.findView(view, R.id.right_buttons_container);
		mBottomRightC = Ui.findView(view, R.id.bottom_right_container);
		mBottomCenterC = Ui.findView(view, R.id.bottom_center_container);
		mWaypointC = Ui.findView(view, R.id.waypoint_container);
		mTravC = Ui.findView(view, R.id.traveler_container);
		mCalC = Ui.findView(view, R.id.calendar_container);
		mGdeC = Ui.findView(view, R.id.gde_container);
		mBottomRightBg = Ui.findView(view, R.id.bottom_right_bg);
		mBottomCenterBg = Ui.findView(view, R.id.bottom_center_bg);

		mDestBtn = Ui.findView(view, R.id.dest_btn);
		mOrigBtn = Ui.findView(view, R.id.origin_btn);
		mCalBtn = Ui.findView(view, R.id.calendar_btn);
		mTravBtn = Ui.findView(view, R.id.traveler_btn);

		mDestBtn.setOnClickListener(mDestClick);
		mOrigBtn.setOnClickListener(mOrigClick);
		mCalBtn.setOnClickListener(mCalClick);
		mTravBtn.setOnClickListener(mTravClick);

		registerStateListener(mSearchStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsSearchState>(), false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mResultsStateHelper.registerWithProvider(this, false);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
		Sp.getBus().register(this);
		bind();
	}

	@Override
	public void onPause() {
		super.onPause();
		mResultsStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
		Sp.getBus().unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_ANIM_FROM_ORIGIN, mWaypointAnimFromOrigin);
		outState.putString(STATE_RESULTS_SEARCH_STATE, mSearchStateManager.getState().name());
	}


	/**
	 * BINDING STUFF
	 */

	public void bind() {
		SearchParams params = Sp.getParams();

		//TODO: Improve string formats

		if (params.hasDestination()) {
			mDestBtn.setText(Html.fromHtml(params.getDestination().getDisplayName()));
		}
		else {
			mDestBtn.setText("");
		}

		if (params.hasOrigin()) {
			mOrigBtn.setText(getString(R.string.fly_from_TEMPLATE, params.getOrigin().getAirportCode()));
		}
		else {
			mOrigBtn.setText("");
		}

		if (params.getStartDate() != null) {
			String dateStr;
			int flags = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;
			LocalDate startDate = params.getStartDate();
			if (params.getEndDate() != null) {
				LocalDate endDate = params.getEndDate();
				dateStr = JodaUtils.formatDateRange(getActivity(), startDate, endDate, flags);
			}
			else {
				dateStr = JodaUtils.formatLocalDate(getActivity(), startDate, flags);
			}
			mCalBtn.setText(dateStr);
		}
		else {
			mCalBtn.setText("");
		}

		int numTravelers = params.getNumAdults() + params.getNumChildren();
		String travStr = getResources()
			.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers);
		mTravBtn.setText(travStr);
	}

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		bind();
	}

	protected void doSpUpdate() {
		Db.getTripBucket().clear();
		if (getActivity() != null && isAdded() && isResumed()) {
			Sp.reportSpUpdate();
		}
	}


	/**
	 * FRAG LISTENERS
	 */

	@Override
	public void onDatesChanged(LocalDate startDate, LocalDate endDate) {
		Sp.getParams().setStartDate(startDate);
		Sp.getParams().setEndDate(endDate);
		doSpUpdate();
	}

	@Override
	public void onGuestsChanged(int numAdults, ArrayList<ChildTraveler> numChildren) {
		Sp.getParams().setNumAdults(numAdults);
		Sp.getParams().setChildTravelers(numChildren);
		doSpUpdate();
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


	/*
	 * SEARCH STATE LISTENER
	 */

	public void setState(ResultsSearchState state, boolean animate) {
		ResultsSearchState curState = mSearchStateManager.getState();
		if (animate && stateShowsWidget(curState) && stateShowsWaypoint(state)) {
			mSearchStateManager.animateThroughStates(200, false, ResultsSearchState.DEFAULT, state);
		}
		else {
			mSearchStateManager.setState(state, animate);
		}
	}

	private boolean stateShowsWaypoint(ResultsSearchState state) {
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

				if (stateOne == ResultsSearchState.CALENDAR || stateTwo == ResultsSearchState.CALENDAR) {
					mBottomRightC.setVisibility(View.VISIBLE);
					mBottomCenterC.setVisibility(View.VISIBLE);
					mCalC.setVisibility(View.VISIBLE);
					mGdeC.setVisibility(View.VISIBLE);
				}

				if (stateOne == ResultsSearchState.TRAVELER_PICKER || stateTwo == ResultsSearchState.TRAVELER_PICKER) {
					mBottomRightC.setVisibility(View.VISIBLE);
					mTravC.setVisibility(View.VISIBLE);
				}
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
				if (stateOne == ResultsSearchState.DEFAULT && stateTwo == ResultsSearchState.CALENDAR) {
					mCalC.setTranslationY((1f - percentage) * -mBottomRightC.getHeight());
					mGdeC.setTranslationY((1f - percentage) * -mBottomCenterC.getHeight());
					mBottomRightBg.setTranslationY((1f - percentage) * -mBottomRightBg.getHeight());
					mBottomCenterBg.setTranslationY((1f - percentage) * -mBottomCenterBg.getHeight());
				}
				else if (stateOne == ResultsSearchState.CALENDAR && stateTwo == ResultsSearchState.DEFAULT) {
					mCalC.setTranslationY(percentage * -mBottomRightC.getHeight());
					mGdeC.setTranslationY(percentage * -mBottomCenterC.getHeight());
					mBottomRightBg.setTranslationY(percentage * -mBottomRightBg.getHeight());
					mBottomCenterBg.setTranslationY(percentage * -mBottomCenterBg.getHeight());
				}
				else if (stateOne == ResultsSearchState.DEFAULT && stateTwo == ResultsSearchState.TRAVELER_PICKER) {
					mTravC.setTranslationY((1f - percentage) * -mBottomRightC.getHeight());
					mBottomRightBg.setTranslationY((1f - percentage) * -mBottomRightBg.getHeight());
				}
				else if (stateOne == ResultsSearchState.TRAVELER_PICKER && stateTwo == ResultsSearchState.DEFAULT) {
					mTravC.setTranslationY(percentage * -mBottomRightC.getHeight());
					mBottomRightBg.setTranslationY(percentage * -mBottomRightBg.getHeight());
				}
				else if (stateOne == ResultsSearchState.TRAVELER_PICKER && stateTwo == ResultsSearchState.CALENDAR) {
					mTravC.setTranslationX(percentage * mTravC.getWidth());
					mCalC.setTranslationX((1f - percentage) * -mCalC.getWidth());
					mGdeC.setTranslationY((1f - percentage) * -mBottomCenterC.getHeight());
					mBottomCenterBg.setTranslationY((1f - percentage) * -mBottomCenterBg.getHeight());
				}
				else if (stateOne == ResultsSearchState.CALENDAR && stateTwo == ResultsSearchState.TRAVELER_PICKER) {
					mTravC.setTranslationX((1f - percentage) * mTravC.getWidth());
					mCalC.setTranslationX(percentage * -mCalC.getWidth());
					mGdeC.setTranslationY(percentage * -mBottomCenterC.getHeight());
					mBottomCenterBg.setTranslationY(percentage * -mBottomCenterBg.getHeight());
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
		}

		private boolean performingSlideUpOrDownTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			boolean goingUp = goingUp(stateOne, stateTwo);
			boolean goingDown = goingDown(stateOne, stateTwo);

			return goingUp || goingDown;
		}

		private boolean goingUp(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			return (stateOne == ResultsSearchState.DEFAULT && (stateTwo == ResultsSearchState.FLIGHTS_UP
				|| stateTwo == ResultsSearchState.HOTELS_UP));
		}

		private boolean goingDown(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			return ((stateOne == ResultsSearchState.FLIGHTS_UP || stateOne == ResultsSearchState.HOTELS_UP)
				&& stateTwo == ResultsSearchState.DEFAULT);
		}

		private boolean isHotelsUpTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			return stateOne == ResultsSearchState.HOTELS_UP || stateTwo == ResultsSearchState.HOTELS_UP;
		}

		private void setSlideUpAnimationHardwareLayers(boolean enabled) {
			int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

			mBottomRightC.setLayerType(layerType, null);
			mBottomCenterC.setLayerType(layerType, null);
			mDestBtn.setLayerType(layerType, null);
		}

		private void setSlideUpAnimationPercentage(float percentage) {
			//Grid manager dimensions work before onMeasure
			int searchBarHeight = mGrid.getRowHeight(2);
			int widgetHeight = mGrid.getRowSpanHeight(0, 3);
			int barTransDistance = mGrid.getRowSpanHeight(0, 2);

			mSearchBarC.setTranslationY(percentage * -barTransDistance);
			mBottomRightC.setTranslationY(percentage * widgetHeight);
			mBottomCenterC.setTranslationY(percentage * widgetHeight);
			mDestBtn.setTranslationY(percentage * searchBarHeight);
			mDestBtn.setAlpha(1f - percentage);
			//TODO: Use better number than searchBarHeight (this is to move to the left of the action bar buttons)
			mRightButtonsC.setTranslationX(percentage * -searchBarHeight);
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
			mBottomRightBg.setTranslationX(0f);
			mBottomRightBg.setTranslationY(0f);
			mBottomCenterBg.setTranslationX(0f);
			mBottomCenterBg.setTranslationY(0f);
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
		boolean mTravAvail = mParamFragsAvailable;
		boolean mWaypointAvailable = mParamFragsAvailable;

		mDatesFragment = FragmentAvailabilityUtils.setFragmentAvailability(mCalAvail, FTAG_CALENDAR, manager,
			transaction, this, R.id.calendar_container, true);

		mGuestsFragment = FragmentAvailabilityUtils.setFragmentAvailability(mTravAvail, FTAG_TRAV_PICKER, manager,
			transaction, this, R.id.traveler_container, false);

		mWaypointFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mWaypointAvailable, FTAG_WAYPOINT, manager,
				transaction, this, R.id.waypoint_container, false);

		mCurrentLocationFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(!Sp.getParams().hasOrigin(), FTAG_ORIGIN_LOCATION, manager, transaction, this, 0,
				true);

		transaction.commit();
	}

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
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
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_CALENDAR) {
			return new ResultsDatesFragment();
		}
		else if (tag == FTAG_TRAV_PICKER) {
			return ResultsGuestPicker.newInstance(Sp.getParams().getNumAdults(), Sp.getParams().getChildTravelers());
		}
		else if (tag == FTAG_WAYPOINT) {
			return new ResultsWaypointFragment();
		}
		else if (tag == FTAG_ORIGIN_LOCATION) {
			return new CurrentLocationFragment();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_CALENDAR) {
			((ResultsDatesFragment) frag).setDatesFromParams(Sp.getParams());
		}
		else if (tag == FTAG_ORIGIN_LOCATION) {
			if (!Sp.getParams().hasOrigin()) {
				//Will notify listener
				((CurrentLocationFragment) frag).getCurrentLocation();
			}
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
				return ResultsSearchState.DEFAULT;
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
			mGrid.setDimensions(totalWidth, totalHeight);
			mGrid.setNumRows(4);// 1 - 3 = top half, 4 = bottom half, 1 = AB, 2 = space, 3 = AB (down)
			mGrid.setNumCols(5);//3 columns, 2 spacers

			int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
			mGrid.setColumnSize(1, spacerSize);
			mGrid.setColumnSize(3, spacerSize);

			mGrid.setRowSize(0, getActivity().getActionBar().getHeight());
			mGrid.setRowSize(2, getActivity().getActionBar().getHeight());
			mGrid.setRowPercentage(3, 0.5f);

			mGrid.setContainerToRow(mSearchBarC, 2);
			mGrid.setContainerToRowSpan(mWaypointC, 0, 3);
			mGrid.setContainerToRow(mBottomRightC, 3);
			mGrid.setContainerToColumn(mBottomRightC, 4);
			mGrid.setContainerToRow(mBottomCenterC, 3);
			mGrid.setContainerToColumn(mBottomCenterC, 2);
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

	private StateListenerCollection<ResultsSearchState> mLis = new StateListenerCollection<ResultsSearchState>(
		mSearchStateManager.getState());

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
	public void onWaypointSearchComplete(TabletWaypointFragment caller, SuggestionV2 suggest, String qryText) {
		if (suggest != null && (mSearchStateManager.getState() == ResultsSearchState.FLIGHT_ORIGIN
			|| mSearchStateManager.getState() == ResultsSearchState.DESTINATION)) {
			boolean usingOrigin = mSearchStateManager.getState() == ResultsSearchState.FLIGHT_ORIGIN;
			if (usingOrigin) {
				Sp.getParams().setOrigin(suggest);
			}
			else {
				Sp.getParams().setDestination(suggest);
				if (!TextUtils.isEmpty(qryText)) {
					Sp.getParams().setCustomDestinationQryText(qryText);
				}
				else {
					Sp.getParams().setDefaultCustomDestinationQryText();
				}
			}
			doSpUpdate();
		}
		setState(ResultsSearchState.DEFAULT, true);
	}

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
		if (!Sp.getParams().hasOrigin()) {
			Sp.getParams().setOrigin(suggestion);
			doSpUpdate();
		}
	}

	@Override
	public void onCurrentLocationError(int errorCode) {
		//TODO: HANDLE ERRORS?
	}
}
