package com.expedia.bookings.fragment;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
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
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

/**
 * TabletResultsSearchControllerFragment: designed for tablet results 2014
 * This controls all the fragments relating to searching on the results screen
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsSearchControllerFragment extends Fragment implements IBackManageable,
	IStateProvider<ResultsSearchState>, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	ResultsDatesFragment.DatesFragmentListener, ResultsGuestPicker.GuestPickerFragmentListener,
	TabletWaypointFragment.ITabletWaypointFragmentListener,
	CurrentLocationFragment.ICurrentLocationListener {

	private static final String INSTANCE_RESULTS_SEARCH_STATE = "INSTANCE_RESULTS_SEARCH_STATE";
	private static final String INSTANCE_ANIM_FROM_ORIGIN = "INSTANCE_ANIM_FROM_ORIGIN";
	private static final String INSTANCE_LOCAL_PARAMS = "INSTANCE_LOCAL_PARAMS";

	private static final String FTAG_CALENDAR = "FTAG_CALENDAR";
	private static final String FTAG_TRAV_PICKER = "FTAG_TRAV_PICKER";
	private static final String FTAG_WAYPOINT = "FTAG_WAYPOINT";
	private static final String FTAG_ORIGIN_LOCATION = "FTAG_ORIGIN_LOCATION";
	private static final String FTAG_FLIGHTS_GDE = "FTAG_FLIGHTS_GDE";
	private static final String FTAG_REDEYE_ITEMS_DIALOG = "FTAG_REDEYE_ITEMS_DIALOG";
	private static final String FTAG_MISMATCHED_ITEMS_DIALOG = "FTAG_MISMATCHED_ITEMS_DIALOG";

	private GridManager mGrid = new GridManager();

	// Note: default state gets reset in onCreate using getDefaultBaseState()
	private StateManager<ResultsSearchState> mSearchStateManager = new StateManager<ResultsSearchState>(
		ResultsSearchState.CALENDAR, this);

	private boolean mWaypointAnimFromOrigin = true;
	private boolean mIgnoreDateChanges = false;
	private boolean mIgnoreGuestChanges = false;
	private Interpolator mCenterColumnUpDownInterpolator = new AccelerateInterpolator(1.2f);

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mSearchBarC;
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

	//Search popup buttons
	private ViewGroup mPopupC;
	private ViewGroup mPopupContentC;
	private ViewGroup mPopupLeftC;
	private ViewGroup mCalPopupC;
	private TextView mTravPopupC;
	private TextView mPopupDoneTv;
	private TextView mCalPopupStartTv;
	private TextView mCalPopupEndTv;

	private ImageView mPopupStartClearBtn;
	private ImageView mPopupEndClearBtn;

	//Uncommited data
	private SearchParams mLocalParams;

	//Frags
	private TabletWaypointFragment mWaypointFragment;
	private ResultsDatesFragment mDatesFragment;
	private ResultsGuestPicker mGuestsFragment;
	private CurrentLocationFragment mCurrentLocationFragment;
	private ResultsGdeFlightsFragment mGdeFragment;
	private SimpleCallbackDialogFragment mRedeyeDialogFrag;
	private SimpleCallbackDialogFragment mMismatchedDialogFrag;

	public TabletResultsSearchControllerFragment() {
		importParams();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		importParams();

		ResultsSearchState defaultState = getDefaultBaseState(savedInstanceState);
		mSearchStateManager.setDefaultState(defaultState);

		if (savedInstanceState != null) {
			mWaypointAnimFromOrigin = savedInstanceState.getBoolean(INSTANCE_ANIM_FROM_ORIGIN, mWaypointAnimFromOrigin);

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

		//Search popup
		mPopupC = Ui.findView(view, R.id.search_popup_container);
		mPopupContentC = Ui.findView(view, R.id.search_popup_content_container);
		mPopupLeftC = Ui.findView(view, R.id.search_popup_left_content_container);
		mCalPopupC = Ui.findView(view, R.id.calendar_popup_content_container);
		mTravPopupC = Ui.findView(view, R.id.traveler_popup_num_guests_label);
		mPopupDoneTv = Ui.findView(view, R.id.search_popup_done);
		mCalPopupStartTv = Ui.findView(view, R.id.popup_start_date);
		mCalPopupEndTv = Ui.findView(view, R.id.popup_end_date);
		mPopupStartClearBtn = Ui.findView(view, R.id.popup_start_date_clear_btn);
		mPopupEndClearBtn = Ui.findView(view, R.id.popup_end_date_clear_btn);

		mPopupStartClearBtn.setOnClickListener(mStartDateClearClick);
		mPopupEndClearBtn.setOnClickListener(mEndDateClearClick);
		mPopupDoneTv.setOnClickListener(mSearchNowClick);

		//Fake AB actions
		mDestBtn.setOnClickListener(mDestClick);
		mOrigBtn.setOnClickListener(mOrigClick);
		mCalBtn.setOnClickListener(mCalClick);
		mTravBtn.setOnClickListener(mTravClick);

		registerStateListener(mSearchStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsSearchState>(), false);

		if (!PointOfSale.getPointOfSale().isFlightSearchEnabledTablet()) {
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

	public void setStateToBaseState(boolean animated) {
		setState(getDefaultBaseState(null), animated);
	}

	private ResultsSearchState getDefaultBaseState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			return ResultsSearchState.valueOf(savedInstanceState.getString(INSTANCE_RESULTS_SEARCH_STATE));
		}
		else if (Db.getTripBucket() != null && !Db.getTripBucket().isEmpty()) {
			return ResultsSearchState.DEFAULT;
		}
		else if (Sp.getParams().getStartDate() != null || Sp.getParams().getEndDate() != null) {
			return ResultsSearchState.DEFAULT;
		}
		else {
			return ResultsSearchState.CALENDAR;
		}
	}

	/**
	 * BINDING STUFF
	 */

	public void bindSearchBtns() {
		// Destination Button - Note that this comes straight from Params
		if (mLocalParams.hasDestination() && mLocalParams.getDestination().getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION) {
			mDestBtn.setText(R.string.current_location);
		}
		else if (mLocalParams.hasDestination()) {
			mDestBtn.setText(StrUtils.formatCity(mLocalParams.getDestination()));
		}
		else {
			mDestBtn.setText("");
		}

		// Origin Button - Note that this comes straight from Params
		if (mLocalParams.hasOrigin()) {
			mOrigBtn.setText(getString(R.string.fly_from_TEMPLATE, StrUtils.formatCity(mLocalParams.getOrigin())));
		}
		else {
			mOrigBtn.setText(getString(R.string.Fly_from_dot_dot_dot));
		}

		//Calendar button stuff
		bindCalBtn();

		//Traveler number stuff
		bindTravBtn();
	}

	private void bindCalBtn() {
		// Search bar
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
			mCalBtn.setText(R.string.select_dates_proper_case);
		}

		// Popup
		if (mLocalParams != null) {
			// Text
			int flags = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;
			String startStr = null, endStr = null;
			if (mLocalParams.getStartDate() != null) {
				startStr = JodaUtils.formatLocalDate(getActivity(), mLocalParams.getStartDate(), flags);
			}
			if (mLocalParams.getEndDate() != null) {
				endStr = JodaUtils.formatLocalDate(getActivity(), mLocalParams.getEndDate(), flags);
			}
			mCalPopupStartTv.setText(startStr);
			mCalPopupEndTv.setText(endStr);

			// X
			mPopupStartClearBtn.setVisibility(mLocalParams.getStartDate() == null ? View.GONE : View.VISIBLE);
			mPopupEndClearBtn.setVisibility(mLocalParams.getEndDate() == null ? View.GONE : View.VISIBLE);

			// Highlight cursor
			if (mLocalParams.getStartDate() == null && mLocalParams.getEndDate() == null) {
				mCalPopupStartTv.setBackgroundResource(R.drawable.textfield_activated_tablet_date_picker);
				mCalPopupEndTv.setBackgroundResource(R.drawable.textfield_default_tablet_date_picker);
			}
			else if (mLocalParams.getStartDate() != null && mLocalParams.getEndDate() == null) {
				mCalPopupStartTv.setBackgroundResource(R.drawable.textfield_default_tablet_date_picker);
				mCalPopupEndTv.setBackgroundResource(R.drawable.textfield_activated_tablet_date_picker);
			}
			else {
				mCalPopupStartTv.setBackgroundResource(R.drawable.textfield_default_tablet_date_picker);
				mCalPopupEndTv.setBackgroundResource(R.drawable.textfield_default_tablet_date_picker);
			}
		}
	}

	private void bindTravBtn() {
		int numTravelers = mLocalParams.getNumAdults() + mLocalParams.getNumChildren();
		String travStr = getResources()
			.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers);
		mTravBtn.setText(travStr);


		if (mGuestsFragment != null) {
			mTravPopupC.setText(mGuestsFragment.getHeaderString());
		}
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
			OmnitureTracking.trackTabletSearchResultsPageLoad(getActivity(), Sp.getParams());
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
				mGuestsFragment.bind(numAdults, children);
			}

			mLocalParams.setNumAdults(numAdults);
			mLocalParams.setChildTravelers(children);
			mLocalParams.setInfantsInLaps(infantsInLaps);
			bindTravBtn();

			mIgnoreGuestChanges = false;
		}
	}

	private void dateChangeHelper(LocalDate startDate, LocalDate endDate) {
		boolean showPopup = false;
		if (!mIgnoreDateChanges) {
			mIgnoreDateChanges = true;

			if (mDatesFragment != null) {
				mDatesFragment.setDates(startDate, endDate);
			}

			if (mGdeFragment != null) {
				mGdeFragment.setGdeInfo(
					mLocalParams.getOriginLocation(true),
					mLocalParams.getDestinationLocation(true),
					startDate);
			}

			if (mLocalParams.getStartDate() == null && startDate != null) {
				showPopup = true;
			}
			mLocalParams.setStartDate(startDate);
			mLocalParams.setEndDate(endDate);

			bindCalBtn();

			mIgnoreDateChanges = false;
		}

		if (showPopup) {
			setState(ResultsSearchState.CALENDAR_WITH_POPUP, true);
		}
	}

	/*
	 * ResultDatesFragment.DatesFragmentListener
	 */

	@Override
	public void onDatesChanged(LocalDate startDate, LocalDate endDate) {
		dateChangeHelper(startDate, endDate);
	}

	@Override
	public void onYearMonthDisplayedChanged(YearMonth yearMonth) {
		// Perhaps GDE is not supported in this POS
		if (mGdeFragment != null) {
			mGdeFragment.scrollToMonth(yearMonth);
		}
	}

	/*
	 * SEARCH BAR BUTTON STUFF
	 */

	private final boolean mAnimateButtonClicks = true;

	private View.OnClickListener mDestClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			OmnitureTracking.trackChooseDestinationLinkClick(getActivity());
			setState(ResultsSearchState.DESTINATION, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mOrigClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			OmnitureTracking.trackChooseOriginLinkClick(getActivity());
			setState(ResultsSearchState.FLIGHT_ORIGIN, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mCalClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			OmnitureTracking.trackChooseDatesLinkClick(getActivity());
			setState(ResultsSearchState.CALENDAR_WITH_POPUP, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mTravClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			setState(ResultsSearchState.TRAVELER_PICKER, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mStartDateClearClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			dateChangeHelper(null, null);
		}
	};

	private View.OnClickListener mEndDateClearClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			dateChangeHelper(mLocalParams.getStartDate(), null);
		}
	};

	private View.OnClickListener mSearchNowClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (getState().showsSearchControls() || getState().showsSearchPopup()) {
				if (copyTempValuesToParams()) {
					doSpUpdate();
				}
				setStateToBaseState(true);
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
		if (animate && curState.showsSearchControls() && state.showsWaypoint()) {
			mSearchStateManager.animateThroughStates(200, false, getDefaultBaseState(null), state);
		}
		else {
			mSearchStateManager.setState(state, animate);
		}
	}

	private StateListenerHelper<ResultsSearchState> mSearchStateHelper = new StateListenerHelper<ResultsSearchState>() {

		@Override
		public void onStateTransitionStart(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			if (stateOne.isUpState() != stateTwo.isUpState()) {
				setSlideUpAnimationHardwareLayers(true);
			}

			if (stateOne.showsWaypoint() || stateTwo.showsWaypoint()) {
				mWaypointC.setVisibility(View.VISIBLE);

				// Here we set up where the search bar animation will originate from
				mWaypointAnimFromOrigin = stateOne == ResultsSearchState.FLIGHT_ORIGIN
					|| stateTwo == ResultsSearchState.FLIGHT_ORIGIN;
			}

			if (stateOne.showsSearchPopup() != stateTwo.showsSearchPopup()) {
				setPopupAnimationHardwareLayers(true);
			}

			if (stateOne.showsCalendar() || stateTwo.showsCalendar()) {
				mBottomRightC.setVisibility(View.VISIBLE);
				mBottomCenterC.setVisibility(View.VISIBLE);
				mCalC.setVisibility(View.VISIBLE);
				mGdeC.setVisibility(View.VISIBLE);

				if (mDatesFragment == null || mDatesFragment.isDetached()
					|| mGdeFragment == null || mGdeFragment.isDetached()) {
					FragmentManager manager = getChildFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					mDatesFragment = FragmentAvailabilityUtils.setFragmentAvailability(true, FTAG_CALENDAR, manager,
						transaction, TabletResultsSearchControllerFragment.this, R.id.calendar_container, true);
					mGdeFragment = FragmentAvailabilityUtils.setFragmentAvailability(true, FTAG_FLIGHTS_GDE, manager,
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

				if (mGuestsFragment == null || mGuestsFragment.isDetached()) {
					FragmentManager manager = getChildFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					mGuestsFragment = FragmentAvailabilityUtils.setFragmentAvailability(true, FTAG_TRAV_PICKER, manager,
						transaction, TabletResultsSearchControllerFragment.this, R.id.traveler_container, true);
					transaction.commit();
				}
			}

			// Popup stuff
			if (stateTwo.showsSearchPopup()) {
				mPopupC.setVisibility(View.VISIBLE);
			}

			if (stateTwo.showsCalendar()) {
				mCalPopupC.setVisibility(View.VISIBLE);
				mTravPopupC.setVisibility(View.GONE);
			}

			if (stateTwo == ResultsSearchState.TRAVELER_PICKER) {
				mTravPopupC.setVisibility(View.VISIBLE);
				mCalPopupC.setVisibility(View.GONE);
				bindTravBtn();
			}

			if (stateTwo.showsSearchControls()) {
				mSearchBarC.setVisibility(View.VISIBLE);
			}

			if (stateTwo == ResultsSearchState.FLIGHT_ORIGIN) {
				mWaypointFragment.updateViewsForOrigin();
			}
			else if (stateTwo == ResultsSearchState.DESTINATION) {
				mWaypointFragment.updateViewsForDestination();
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsSearchState stateOne, ResultsSearchState stateTwo,
											float percentage) {

			if (stateOne.isUpState() != stateTwo.isUpState()) {
				float perc = stateTwo.isUpState() ? percentage : (1f - percentage);
				setSlideUpAnimationPercentage(perc);
			}

			// There are 2 ways to hide the bottom containers
			// Way 1: fade them out
			if (stateOne.showsWaypoint() != stateTwo.showsWaypoint()) {
				float p = stateTwo.showsWaypoint() ? 1f - percentage : percentage;
				mBottomRightC.setAlpha(p);
				mBottomCenterC.setAlpha(p);
				mSearchBarC.setAlpha(p);
			}
			// Way 2: slide them down
			else if ((stateOne.showsSearchControls() || stateOne.showsSearchPopup())
				!= (stateTwo.showsSearchControls() || stateTwo.showsSearchPopup())) {
				float p = stateTwo.showsSearchControls() || stateTwo.showsSearchPopup() ? 1f - percentage : percentage;
				int dist = mGrid.isLandscape() ? 1 : 2;
				mBottomRightC.setTranslationY(p * dist * mBottomRightC.getHeight());
				mBottomCenterC.setTranslationY(p * dist * mBottomCenterC.getHeight());

			}

			if (stateOne.showsSearchPopup() != stateTwo.showsSearchPopup()) {
				setPopupAnimationPercentage(percentage, stateTwo.showsSearchPopup());
			}

			// Special animation for switching between CALENDAR and TRAVEL_PICKER
			if (stateOne == ResultsSearchState.CALENDAR && stateTwo == ResultsSearchState.TRAVELER_PICKER
				|| stateOne == ResultsSearchState.TRAVELER_PICKER && stateTwo == ResultsSearchState.CALENDAR) {

				float p = stateTwo == ResultsSearchState.TRAVELER_PICKER ? percentage : 1f - percentage;

				mTravC.setTranslationX((1f - p) * mTravC.getWidth());
				mCalC.setTranslationX(p * -mCalC.getWidth());
				if (mGrid.isLandscape()) {
					mGdeC.setTranslationY(p * mBottomCenterC.getHeight());
				}
				else {
					mGdeC.setTranslationX(p * -mBottomCenterC.getWidth());
					mTravPickWhiteSpace.setTranslationX((1f - p) * mBottomCenterC.getWidth());
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			if (stateOne.isUpState() != stateTwo.isUpState()) {
				setSlideUpAnimationHardwareLayers(false);
			}

			if (stateOne.showsSearchPopup() != stateTwo.showsSearchPopup()) {
				setPopupAnimationHardwareLayers(false);
			}
		}

		@Override
		public void onStateFinalized(ResultsSearchState state) {
			setFragmentState(state);
			setVisibilitiesForState(state);
			resetWidgetTranslations();

			setSlideUpAnimationPercentage(state.isUpState() ? 1f : 0f);

			if (state == ResultsSearchState.TRAVELER_PICKER) {
				bindTravBtn();
			}

			if (state.showsCalendar()) {
				if (mGdeFragment != null) {
					mGdeFragment.setGdeInfo(mLocalParams.getOriginLocation(true),
						mLocalParams.getDestinationLocation(true),
						mLocalParams.getStartDate());
				}
			}

			if (!state.showsSearchControls() && !state.showsSearchPopup()) {
				clearChanges();
			}
		}

		private void setSlideUpAnimationHardwareLayers(boolean enabled) {
			int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

			mBottomRightC.setLayerType(layerType, null);
			mBottomCenterC.setLayerType(layerType, null);
			mDestBtn.setLayerType(layerType, null);
		}

		private void setSlideUpAnimationPercentage(float percentage) {
			// Grid manager dimensions work before onMeasure
			int searchBarHeight = mGrid.getRowHeight(3);
			int barTransDistance = mGrid.getRowSpanHeight(0, 3);

			mSearchBarC.setTranslationY(percentage * -barTransDistance);
			if (mGrid.isLandscape()) {
				mBottomRightC.setTranslationY(percentage * mBottomRightC.getHeight());
				mBottomCenterC.setTranslationY(mCenterColumnUpDownInterpolator.getInterpolation(percentage) * mBottomCenterC.getHeight());
			}
			else {
				mBottomRightC.setTranslationY(percentage * 2 * mBottomRightC.getHeight());
				mBottomCenterC.setTranslationY(percentage * 2 * mBottomCenterC.getHeight());
			}
			mDestBtn.setTranslationY(percentage * searchBarHeight);
			mDestBtn.setAlpha(1f - percentage);
			mOrigBtn.setAlpha(1f - percentage);
			mCalBtn.setAlpha(1f - percentage);
			mTravBtn.setAlpha(1f - percentage);

			if (mGrid.isLandscape()) {
				// This is only to ensure the search controls shift left of the overflow menu. This
				// overflow menu is only present for HockeyApp builds.
				if (!AndroidUtils.isRelease(getActivity())) {
					float transX = percentage * -searchBarHeight;
					mOrigBtn.setTranslationX(transX);
					mCalBtn.setTranslationX(transX);
					mTravBtn.setTranslationX(transX);
				}
			}
			else {
				float transY = percentage * searchBarHeight;
				mOrigBtn.setTranslationY(transY);
				mCalBtn.setTranslationY(transY);
				mTravBtn.setTranslationY(transY);
			}
		}

		// Popup anim helper methods

		private void setPopupAnimationHardwareLayers(boolean enabled) {
			int layerType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
			mPopupC.setLayerType(layerType, null);
		}

		private void setPopupAnimationPercentage(float percentage, boolean active) {
			percentage = active ? percentage : 1f - percentage;

			// Note: these wiggle values are dependent upon the layout and more specifically
			// the background assets that are used the popup
			float wiggleX = 32 * getResources().getDisplayMetrics().density;
			float wiggleY = 12 * getResources().getDisplayMetrics().density;
			float pivotX = mPopupLeftC.getWidth() - wiggleX;
			float pivotY = mPopupContentC.getHeight() - wiggleY;

			mPopupContentC.setPivotX(pivotX);
			mPopupContentC.setPivotY(pivotY);
			mPopupContentC.setScaleX(percentage);
			mPopupContentC.setScaleY(percentage);

			mSearchBarC.setAlpha(1f - percentage);
		}

		private void setVisibilitiesForState(ResultsSearchState state) {
			mWaypointC.setVisibility(
				state.showsWaypoint()
					? View.VISIBLE
					: View.INVISIBLE
			);

			mTravC.setVisibility(
				state == ResultsSearchState.TRAVELER_PICKER
					? View.VISIBLE
					: View.INVISIBLE
			);

			mCalC.setVisibility(
				state.showsCalendar()
					? View.VISIBLE
					: View.INVISIBLE
			);

			mCalPopupC.setVisibility(mCalC.getVisibility());

			// GDE visibility should always follow the calendar visibility
			mGdeC.setVisibility(mCalC.getVisibility());

			mBottomRightC.setVisibility(
				mCalC.getVisibility() == View.VISIBLE || mTravC.getVisibility() == View.VISIBLE
					? View.VISIBLE
					: View.INVISIBLE
			);

			mTravPopupC.setVisibility(
				state == ResultsSearchState.TRAVELER_PICKER
					? View.VISIBLE
					: View.GONE
			);

			mPopupC.setVisibility(
				state.showsSearchPopup()
					? View.VISIBLE
					: View.INVISIBLE
			);

			mSearchBarC.setVisibility(
				state.showsSearchControls()
					? View.VISIBLE
					: View.INVISIBLE
			);

			mTravPickWhiteSpace.setVisibility(
				!mGrid.isLandscape() && state == ResultsSearchState.TRAVELER_PICKER
					? View.VISIBLE
					: View.INVISIBLE
			);

			// BottomCenterContainer houses GDE and traveler picker whitespace. So if either one of
			// those is visible, make this one visible too.
			mBottomCenterC.setVisibility(
				mTravPickWhiteSpace.getVisibility() == View.VISIBLE || mGdeC.getVisibility() == View.VISIBLE
					? View.VISIBLE
					: View.INVISIBLE
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
		}
	};


	/**
	 * FRAGMENT PROVIDER
	 */

	private void setFragmentState(ResultsSearchState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		// We want most of our frags available so they can animate in real nice like
		boolean mParamFragsAvailable = !state.isUpState();

		boolean mCalAvail = mParamFragsAvailable;
		boolean mGdeAvail = mCalAvail; // GDE will display an error message if POS isn't supported
		boolean mTravAvail = mParamFragsAvailable;
		boolean mWaypointAvail = mParamFragsAvailable;
		boolean mLocAvail = mGdeAvail && !mLocalParams.hasOrigin();

		mDatesFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mCalAvail, FTAG_CALENDAR, manager,
				transaction, this, R.id.calendar_container, true);

		mGdeFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mGdeAvail, FTAG_FLIGHTS_GDE, manager,
				transaction, this, R.id.gde_container, true);

		mGuestsFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mTravAvail, FTAG_TRAV_PICKER, manager,
				transaction, this, R.id.traveler_container, true);

		mWaypointFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mWaypointAvail, FTAG_WAYPOINT, manager,
				transaction, this, R.id.waypoint_container, false);

		mCurrentLocationFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(mLocAvail, FTAG_ORIGIN_LOCATION, manager,
				transaction, this, 0, true);

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
		switch (tag) {
		case FTAG_CALENDAR: {
			return new ResultsDatesFragment();
		}
		case FTAG_TRAV_PICKER: {
			return new ResultsGuestPicker();
		}
		case FTAG_WAYPOINT: {
			return TabletWaypointFragment.newInstance(false);
		}
		case FTAG_ORIGIN_LOCATION: {
			return new CurrentLocationFragment();
		}
		case FTAG_FLIGHTS_GDE: {
			return ResultsGdeFlightsFragment.newInstance();
		}
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		switch (tag) {
		case FTAG_CALENDAR: {
			((ResultsDatesFragment) frag).setDates(mLocalParams.getStartDate(), mLocalParams.getEndDate());
			break;
		}
		case FTAG_TRAV_PICKER: {
			((ResultsGuestPicker) frag).bind(mLocalParams.getNumAdults(), mLocalParams.getChildTravelers());
			break;
		}
		case FTAG_ORIGIN_LOCATION: {
			if (!mLocalParams.hasOrigin()) {
				//Will notify listener
				((CurrentLocationFragment) frag).getCurrentLocation();
			}
			break;
		}
		case FTAG_FLIGHTS_GDE: {
			((ResultsGdeFlightsFragment) frag).setGdeInfo(mLocalParams.getOriginLocation(true),
				mLocalParams.getDestinationLocation(true), mLocalParams.getStartDate());
			break;
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
			boolean lastStateUp = lastState.isUpState();
			boolean newStateUp = newState.isUpState();

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
				// When returning to Results Overview, show the trip bucket if it has items
				if (Db.getTripBucket().isEmpty()) {
					return getDefaultBaseState(null);
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

				mGrid.setContainerToRowSpan(mPopupC, 0, 3);
				mGrid.setContainerToRow(mSearchBarC, 3);
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

				mGrid.setContainerToRowSpan(mPopupC, 0, 3);
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
			if (state.isUpState()) {
				return false;
			}

			ResultsSearchState base = getDefaultBaseState(null);
			if (state != base) {
				setState(base, true);
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
				// When the user selects current location as destination, let's clear origin IF it was previously current location. #2996
				if (mLocalParams.getOrigin() != null && mLocalParams.getOrigin().getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION
					&& mLocalParams.getDestination() != null && mLocalParams.getDestination().getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION) {
					mLocalParams.setOrigin(null);
				}
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
		setStateToBaseState(true);
	}

	@Subscribe
	public void onTripBucketHasRedeyeItems(Events.TripBucketHasRedeyeItems event) {
		mRedeyeDialogFrag = Ui.findSupportFragment(this, FTAG_REDEYE_ITEMS_DIALOG);
		if (mRedeyeDialogFrag == null) {
			mRedeyeDialogFrag = SimpleCallbackDialogFragment.newInstance(
				"" /*title*/, //
				getString(R.string.tablet_redeye_products_message), //
				getString(R.string.yes) /*button*/, //
				SimpleCallbackDialogFragment.CODE_TABLET_MISMATCHED_ITEMS, //
				getString(R.string.cancel) /*negativeButton*/);
		}
		if (!mRedeyeDialogFrag.isAdded()) {
			OmnitureTracking.trackRedeyeAlert(getActivity());
			mRedeyeDialogFrag.show(getFragmentManager(), FTAG_REDEYE_ITEMS_DIALOG);
		}
	}

	@Subscribe
	public void onTripBucketHasMismatchedItems(Events.TripBucketHasMismatchedItems event) {
		mMismatchedDialogFrag = Ui.findSupportFragment(this, FTAG_MISMATCHED_ITEMS_DIALOG);
		if (mMismatchedDialogFrag == null) {
			mMismatchedDialogFrag = SimpleCallbackDialogFragment.newInstance(
				"" /*title*/, //
				getString(R.string.tablet_mismatched_products_message), //
				getString(R.string.yes) /*button*/, //
				SimpleCallbackDialogFragment.CODE_TABLET_MISMATCHED_ITEMS, //
				getString(R.string.cancel) /*negativeButton*/);
		}
		if (!mMismatchedDialogFrag.isAdded()) {
			OmnitureTracking.trackDateMismatchAlert(getActivity());
			mMismatchedDialogFrag.show(getFragmentManager(), FTAG_REDEYE_ITEMS_DIALOG);
		}
	}

	@Subscribe
	public void onShowSearchFragment(Events.ShowSearchFragment event) {
		OmnitureTracking.trackChooseDestinationLinkClick(getActivity());
		setState(event.searchState, mAnimateButtonClicks);
	}
}
