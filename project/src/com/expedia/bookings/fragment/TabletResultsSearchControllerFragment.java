package com.expedia.bookings.fragment;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
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
	SuggestionsFragment.SuggestionsFragmentListener {

	private GridManager mGrid = new GridManager();
	private StateManager<ResultsSearchState> mSearchStateManager = new StateManager<ResultsSearchState>(ResultsSearchState.DEFAULT, this);

	//Containers
	private ViewGroup mRootC;
	private FrameLayoutTouchController mTopHalfC;
	private FrameLayoutTouchController mSearchBarC;
	private ViewGroup mRightButtonsC;
	private FrameLayoutTouchController mWidgetC;
	//Fragment Containers
	private FrameLayoutTouchController mCalC;
	private FrameLayoutTouchController mTravC;
	private FrameLayoutTouchController mOrigC;

	//Search action buttons
	private TextView mDestBtn;
	private TextView mOrigBtn;
	private TextView mCalBtn;
	private TextView mTravBtn;

	private static final String FTAG_CALENDAR = "FTAG_CALENDAR";
	private static final String FTAG_TRAV_PICKER = "FTAG_TRAV_PICKER";
	private static final String FTAG_ORIG_CHOOSER = "FTAG_ORIG_CHOOSER";

	private SuggestionsFragment mOriginsFragment;
	private ResultsDatesFragment mDatesFragment;
	private GuestsDialogFragment mGuestsFragment;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_search, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTopHalfC = Ui.findView(view, R.id.top_half_container);
		mSearchBarC = Ui.findView(view, R.id.search_bar_conatiner);
		mRightButtonsC = Ui.findView(view, R.id.right_buttons_container);
		mWidgetC = Ui.findView(view, R.id.widget_container);

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
		mResultsStateHelper.registerWithProvider(this);
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

	/**
	 * BINDING STUFF
	 */

	public void bind() {
		SearchParams params = Sp.getParams();

		//TODO: Improve string formats

		if (params.hasDestination()) {
			mDestBtn.setText(params.getDestination().getAirportCode());
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
		String travStr = getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers);
		mTravBtn.setText(travStr);
	}

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		bind();
	}


	/**
	 * FRAG LISTENERS
	 */

	@Override
	public void onDatesChanged(LocalDate startDate, LocalDate endDate) {
		Sp.getParams().setStartDate(startDate);
		Sp.getParams().setEndDate(endDate);
		Sp.reportSpUpdate();
	}

	@Override
	public void onGuestsChanged(int numAdults, ArrayList<Integer> numChildren) {
		Sp.getParams().setNumAdults(numAdults);
		Sp.getParams().setChildAges(numChildren);
		Sp.reportSpUpdate();
	}

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		if (fragment == mOriginsFragment) {
			Sp.getParams().setOrigin(suggestion);
			Sp.reportSpUpdate();
		}
	}

	/*
	 * SEARCH BAR BUTTON STUFF
	 */

	private final boolean mAnimateButtonClicks = false;

	private View.OnClickListener mDestClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {

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
			setState(ResultsSearchState.CALENDAR, mAnimateButtonClicks);
		}
	};

	private View.OnClickListener mTravClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			setState(ResultsSearchState.TRAVELER_PICKER, mAnimateButtonClicks);
		}
	};


	/*
	 * SEARCH STATE LISTENER
	 */

	public void setState(ResultsSearchState state, boolean animate) {
		mSearchStateManager.setState(state, animate);
	}

	private StateListenerHelper<ResultsSearchState> mSearchStateHelper = new StateListenerHelper<ResultsSearchState>() {
		@Override
		public void onStateTransitionStart(ResultsSearchState stateOne, ResultsSearchState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsSearchState stateOne, ResultsSearchState stateTwo, float percentage) {
			boolean goingUp = (stateOne == ResultsSearchState.DEFAULT && (stateTwo == ResultsSearchState.FLIGHTS_UP || stateTwo == ResultsSearchState.HOTELS_UP));
			boolean goingDown = ((stateOne == ResultsSearchState.FLIGHTS_UP || stateOne == ResultsSearchState.HOTELS_UP) && stateTwo == ResultsSearchState.DEFAULT);

			if (goingUp || goingDown) {
				float perc = goingUp ? percentage : (1f - percentage);
				setSlideUpAnimationPercentage(perc);
				if (stateOne == ResultsSearchState.HOTELS_UP || stateTwo == ResultsSearchState.HOTELS_UP) {
					//For hotels we also fade
					setSlideUpHotelsOnlyAnimationPercentage(perc);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsSearchState stateOne, ResultsSearchState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsSearchState state) {
			setFragmentState(state);

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

		private void setSlideUpAnimationPercentage(float percentage) {
			int barTransDistance = mTopHalfC.getHeight() - mSearchBarC.getHeight();
			mSearchBarC.setTranslationY(percentage * -barTransDistance);
			mWidgetC.setTranslationY(percentage * mWidgetC.getHeight());
			mDestBtn.setTranslationY(percentage * mSearchBarC.getHeight());
			mDestBtn.setAlpha(1f - percentage);
			//TODO: Use better number (this is to move to the left of the action bar buttons)
			mRightButtonsC.setTranslationX(percentage * -(getActivity().getActionBar().getHeight()));
		}

		private void setSlideUpHotelsOnlyAnimationPercentage(float percentage) {
			mOrigBtn.setAlpha(1f - percentage);
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

		boolean mCalAvail = state == ResultsSearchState.CALENDAR;
		boolean mTravAvail = state == ResultsSearchState.TRAVELER_PICKER;
		boolean mOrigAvail = state == ResultsSearchState.FLIGHT_ORIGIN;

		mDatesFragment = (ResultsDatesFragment) FragmentAvailabilityUtils.setFragmentAvailability(mCalAvail, FTAG_CALENDAR, manager,
			transaction, this, R.id.calendar_container, true);

		mGuestsFragment = (GuestsDialogFragment) FragmentAvailabilityUtils.setFragmentAvailability(mTravAvail, FTAG_TRAV_PICKER, manager,
			transaction, this, R.id.traveler_container, false);

		mOriginsFragment = (SuggestionsFragment) FragmentAvailabilityUtils.setFragmentAvailability(mOrigAvail, FTAG_ORIG_CHOOSER, manager,
			transaction, this, R.id.origin_container, false);

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
		else if (tag == FTAG_ORIG_CHOOSER) {
			return mOriginsFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_CALENDAR) {
			return new ResultsDatesFragment();
		}
		else if (tag == FTAG_TRAV_PICKER) {
			return GuestsDialogFragment.newInstance(Sp.getParams().getNumAdults(), Sp.getParams().getChildAges());
		}
		else if (tag == FTAG_ORIG_CHOOSER) {
			return new SuggestionsFragment();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_CALENDAR) {
			((ResultsDatesFragment) frag).setDatesFromParams(Sp.getParams());
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
			startStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			updateStateTransition(translateState(stateOne), translateState(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			endStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsState state) {

			boolean currentlyDown = mSearchStateManager.getState() != ResultsSearchState.FLIGHTS_UP && mSearchStateManager.getState() != ResultsSearchState.HOTELS_UP;
			if (!currentlyDown || translateState(state) != ResultsSearchState.DEFAULT) {
				//We respond to things where we move from the up to the down state, but we dont listen to the parent
				finalizeState(translateState(state));
			}
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
			mGrid.setNumRows(2);
			mGrid.setNumCols(3);

			mGrid.setContainerToRow(mTopHalfC, 0);
			mGrid.setContainerToRow(mWidgetC, 1);
			mGrid.setContainerToColumn(mWidgetC, 2);
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
			return false;
		}

	};


	/*
	 * RESULTS SEARCH STATE PROVIDER
	 */

	private StateListenerCollection<ResultsSearchState> mLis = new StateListenerCollection<ResultsSearchState>(mSearchStateManager.getState());

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
}