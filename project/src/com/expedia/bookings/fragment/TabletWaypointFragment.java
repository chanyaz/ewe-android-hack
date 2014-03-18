package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.enums.WaypointChooserState;
import com.expedia.bookings.fragment.SuggestionsFragment.SuggestionsFragmentListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;

/**
 * A large search fragment only suitable for tablet sizes.
 */
@TargetApi(14)
public class TabletWaypointFragment extends Fragment
	implements SuggestionsFragmentListener, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	IStateProvider<WaypointChooserState>, CurrentLocationFragment.ICurrentLocationListener {

	private static final String FTAG_SUGGESTIONS = "FTAG_SUGGESTIONS";
	private static final String FTAG_LOCATION = "FTAG_LOCATION";

	private static final String STATE_WAITING_FOR_LOCATION = "STATE_WAITING_FOR_LOCATION";

	public static interface ITabletWaypointFragmentListener {
		public Rect getAnimOrigin();

		public void onWaypointSearchComplete(TabletWaypointFragment caller, SuggestionV2 suggest);
	}

	private GridManager mGrid = new GridManager();
	private SuggestionsFragment mSuggestionsFragment;
	private ITabletWaypointFragmentListener mListener;
	private CurrentLocationFragment mLocationFragment;

	private FrameLayoutTouchController mRootC;
	private View mBg;
	private ViewGroup mSearchBarC;
	private ViewGroup mSuggestionsC;
	private EditText mWaypointEditText;

	private StateManager<WaypointChooserState> mStateManager = new StateManager<WaypointChooserState>(
		WaypointChooserState.HIDDEN, this);

	private boolean mWaitingForLocation;


	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, ITabletWaypointFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_waypoint_search, container, false);
		mRootC = Ui.findView(view, R.id.root_layout);
		mRootC.setConsumeTouch(true);

		mBg = Ui.findView(view, R.id.bg);
		mWaypointEditText = Ui.findView(view, R.id.waypoint_edit_text);
		mSearchBarC = Ui.findView(view, R.id.search_bar_conatiner);
		mSuggestionsC = Ui.findView(view, R.id.suggestions_container);

		//State
		if (savedInstanceState != null) {
			mWaitingForLocation = savedInstanceState.getBoolean(STATE_WAITING_FOR_LOCATION);
		}

		//Setup the suggestions fragment
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mSuggestionsFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			true, FTAG_SUGGESTIONS, manager,
			transaction, this, R.id.suggestions_container, false);
		mLocationFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			true, FTAG_LOCATION, manager,
			transaction, this, 0, false);


		transaction.commit();

		// Add a search edit text watcher.  We purposefully add it before
		// we restore the edit text's state so that it will fire and update
		// the UI properly to restore its state.
		mWaypointEditText.addTextChangedListener(new MyWatcher(mWaypointEditText));

		//Add the search action listener to the edit text
		mWaypointEditText.setOnEditorActionListener(mSearchActionListener);

		registerStateListener(mStateHelper, false);

		return view;
	}


	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMeasurementHelper.unregisterWithProvider(this);
		getActivity().getActionBar().show();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_WAITING_FOR_LOCATION, mWaitingForLocation);
	}

	//////////////////////////////////////////////////////////////////////////
	// Edit Text

	private void clearEditTextFocus(EditText editText) {
		editText.setFocusableInTouchMode(false);
		editText.clearFocus();
		Ui.hideKeyboard(editText, 0);
	}

	private void requestEditTextFocus(EditText editText) {
		editText.setFocusableInTouchMode(true);
		editText.requestFocus();
		Ui.showKeyboard(editText, null);
	}

	private class MyWatcher extends AfterChangeTextWatcher {

		private EditText mEditText;

		public MyWatcher(EditText editText) {
			mEditText = editText;
		}

		@Override
		public void afterTextChanged(Editable s) {
			doAfterTextChanged(mEditText);
		}
	}

	// We check if the edit text is focusable; if it's not, we want it to be the default when the fragment
	// would be re-visible (aka, empty).  That way we don't have to wait for autocomplete results to load.
	void doAfterTextChanged(EditText editText) {
		if (editText == mWaypointEditText) {
			updateFilter(mSuggestionsFragment, editText.isFocusableInTouchMode() ? mWaypointEditText.getText()
				: null);
		}

	}

	private void updateFilter(SuggestionsFragment fragment, CharSequence text) {
		if (fragment != null) {
			fragment.filter(text);
		}
	}

	public void updateViewsForOrigin() {
		if (mWaypointEditText != null) {
			mWaypointEditText.setHint(R.string.origins_hint);
		}
	}

	public void updateViewsForDestination() {
		if (mWaypointEditText != null) {
			mWaypointEditText.setHint(R.string.destinations_hint);
		}
	}

	private TextView.OnEditorActionListener mSearchActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				if (mSuggestionsFragment != null && getState() == WaypointChooserState.VISIBLE) {
					SuggestionV2 suggest = mSuggestionsFragment.getBestChoiceForFilter();
					if (suggest != null) {
						onSuggestionClicked(mSuggestionsFragment, suggest);
						return true;
					}
					else {
						Ui.showToast(getActivity(), R.string.waypoint_suggestion_fail);
						requestEditTextFocus(mWaypointEditText);
					}
				}
			}
			return false;
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Formatting

	private String getSuggestionText(SuggestionV2 suggestion) {
		String text = null;

		if (suggestion.getResultType() == ResultType.CURRENT_LOCATION) {
			text = getString(R.string.current_location);
		}

		if (TextUtils.isEmpty(text)) {
			text = suggestion.getDisplayName();

			if (!TextUtils.isEmpty(text)) {
				// Strip HTML from display
				text = Html.fromHtml(text).toString();
			}
		}

		if (TextUtils.isEmpty(text) && suggestion.getLocation() != null) {
			text = suggestion.getLocation().getCity();
		}

		if (TextUtils.isEmpty(text)) {
			text = suggestion.getAirportCode();
		}

		if (TextUtils.isEmpty(text)) {
			text = getString(R.string.great_unknown);
		}

		return text;
	}

	//////////////////////////////////////////////////////////////////////////
	// Utils

	protected boolean needsLocation(SuggestionV2 suggestion) {
		return suggestion.getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION && (
			suggestion.getLocation() == null || (suggestion.getLocation().getLatitude() == 0
				&& suggestion.getLocation().getLongitude() == 0));
	}

	//////////////////////////////////////////////////////////////////////////
	// SuggestionsFragmentListener

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		if (needsLocation(suggestion)) {
			mWaitingForLocation = true;
			//This will fire the listener when the location is found
			mLocationFragment.getCurrentLocation();
		}
		else {
			mWaitingForLocation = false;
			mListener.onWaypointSearchComplete(this, suggestion);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment availability provider

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_SUGGESTIONS) {
			return mSuggestionsFragment;
		}
		else if (tag == FTAG_LOCATION) {
			return mLocationFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_SUGGESTIONS) {
			return new SuggestionsFragment();
		}
		else if (tag == FTAG_LOCATION) {
			return new CurrentLocationFragment();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
	}


	//////////////////////////////////////////////////////////////////////////
	// State Listener

	public void setState(WaypointChooserState state, boolean animate) {
		mStateManager.setState(state, animate);
	}

	public WaypointChooserState getState() {
		return mStateManager.getState();
	}

	private StateListenerHelper<WaypointChooserState> mStateHelper = new StateListenerHelper<WaypointChooserState>() {

		private Rect mAnimFrom;
		private float mMultX;
		private float mMultY;

		@Override
		public void onStateTransitionStart(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
			mAnimFrom = mListener.getAnimOrigin();
			mMultX = (mAnimFrom.width() / (float) mSearchBarC.getWidth());
			mMultY = (mAnimFrom.height() / (float) mSearchBarC.getHeight());

			mSuggestionsC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			mBg.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			mSearchBarC.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			mSearchBarC.setPivotX(0);
			mSearchBarC.setPivotY(0);
		}

		@Override
		public void onStateTransitionUpdate(WaypointChooserState stateOne, WaypointChooserState stateTwo,
			float percentage) {
			float perc = stateTwo == WaypointChooserState.HIDDEN ? 1f - percentage : percentage;

			float transX = (1f - perc) * (mAnimFrom.left - mSearchBarC.getLeft());
			float transY = (1f - perc) * (mAnimFrom.bottom - (mSearchBarC.getBottom()));
			float scaleX = mMultX + perc * (1f - mMultX);
			float scaleY = mMultY + perc * (1f - mMultY);

			mSearchBarC.setTranslationX(transX);
			mSearchBarC.setTranslationY(transY);
			mSearchBarC.setScaleX(scaleX);
			mSearchBarC.setScaleY(scaleY);
			mSuggestionsC.setTranslationY((1f - perc) * mSuggestionsC.getHeight());
			mSearchBarC.setAlpha(perc);
			mBg.setAlpha(perc);
		}

		@Override
		public void onStateTransitionEnd(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
			mSuggestionsC.setLayerType(View.LAYER_TYPE_NONE, null);
			mBg.setLayerType(View.LAYER_TYPE_NONE, null);
			mSearchBarC.setLayerType(View.LAYER_TYPE_NONE, null);
		}

		@Override
		public void onStateFinalized(WaypointChooserState state) {
			if (mRootC != null) {
				if (state == WaypointChooserState.VISIBLE) {
					mSearchBarC.setTranslationX(0);
					mSearchBarC.setTranslationY(0);
					mSearchBarC.setScaleX(1f);
					mSearchBarC.setScaleY(1f);
					mSuggestionsC.setTranslationY(0);
					mSearchBarC.setAlpha(1f);
					mBg.setAlpha(1f);
					requestEditTextFocus(mWaypointEditText);
				}
				else {
					mBg.setAlpha(0f);
					mWaypointEditText.setText("");
					clearEditTextFocus(mWaypointEditText);
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Measurement listener

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);
			mGrid.setNumRows(4);//top space - Search bar - spacer - suggestion results
			mGrid.setNumCols(3);//Left padding - content - right padding

			mGrid.setRowPercentage(0, 0.05f);
			mGrid.setRowSize(1, getResources().getDimensionPixelSize(R.dimen.tablet_search_header_height));
			mGrid.setRowPercentage(2, 0.05f);

			mGrid.setColumnSize(1, getResources().getDimensionPixelSize(R.dimen.tablet_search_width));

			mGrid.setContainerToRow(mSearchBarC, 1);
			mGrid.setContainerToColumn(mSearchBarC, 1);

			mGrid.setContainerToRow(mSuggestionsC, 3);
			mGrid.setContainerToColumn(mSuggestionsC, 1);

		}
	};

	//////////////////////////////////////////////////////////////////////////
	// IStateProvider

	StateListenerCollection<WaypointChooserState> mL = new StateListenerCollection<WaypointChooserState>(
		mStateManager.getState());

	@Override
	public void startStateTransition(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
		mL.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(WaypointChooserState stateOne, WaypointChooserState stateTwo, float percentage) {
		mL.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
		mL.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(WaypointChooserState state) {
		mL.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<WaypointChooserState> listener, boolean fireFinalizeState) {
		mL.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<WaypointChooserState> listener) {
		mL.unRegisterStateListener(listener);
	}

	//////////////////////////////////////////////////////////////////////////
	// ICurrentLocationListener

	@Override
	public void onCurrentLocation(Location location, SuggestionV2 suggestion) {
		if (mWaitingForLocation) {
			mWaitingForLocation = false;
			mListener.onWaypointSearchComplete(this, suggestion);
		}
	}

}
