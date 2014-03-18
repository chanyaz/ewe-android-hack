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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.enums.WaypointChooserState;
import com.expedia.bookings.fragment.SuggestionsFragment.SuggestionsFragmentListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
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

	private static final String STATE_WAYPOINT_CHOOSER_STATE = "STATE_WAYPOINT_CHOOSER_STATE";
	private static final String STATE_ERROR_MESSAGE = "STATE_ERROR_MESSAGE";

	public static interface ITabletWaypointFragmentListener {
		public Rect getAnimOrigin();

		public void onWaypointSearchComplete(TabletWaypointFragment caller, SuggestionV2 suggest);
	}

	private SuggestionsFragment mSuggestionsFragment;
	private ITabletWaypointFragmentListener mListener;
	private CurrentLocationFragment mLocationFragment;
	private String mErrorMessage;

	private ViewGroup mRootC;
	private FrameLayoutTouchController mBg;
	private ViewGroup mSearchBarC;
	private ViewGroup mSuggestionsC;
	private EditText mWaypointEditText;
	private TextView mErrorTv;
	private ProgressBar mLocationProgressBar;

	private StateManager<WaypointChooserState> mStateManager = new StateManager<WaypointChooserState>(
		WaypointChooserState.HIDDEN, this);

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

		//The background wont let any touches pass through it...
		mBg = Ui.findView(view, R.id.bg);
		mBg.setConsumeTouch(true);

		mWaypointEditText = Ui.findView(view, R.id.waypoint_edit_text);
		mSearchBarC = Ui.findView(view, R.id.search_bar_conatiner);
		mSuggestionsC = Ui.findView(view, R.id.suggestions_container);
		mLocationProgressBar = Ui.findView(view, R.id.location_loading_progress);
		mErrorTv = Ui.findView(view, R.id.error_text_view);

		//State
		if (savedInstanceState != null) {
			mErrorMessage = savedInstanceState.getString(STATE_ERROR_MESSAGE);
			mStateManager.setDefaultState(
				WaypointChooserState.valueOf(savedInstanceState.getString(STATE_WAYPOINT_CHOOSER_STATE)));
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
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().getActionBar().show();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_WAYPOINT_CHOOSER_STATE, mStateManager.getState().name());
		outState.putString(STATE_ERROR_MESSAGE, mErrorMessage == null ? "" : mErrorMessage);
	}

	//////////////////////////////////////////////////////////////////////////
	// General

	public void unsetLoadingAndError() {
		setErrorMessage(null);
		if (mStateManager.getState() == WaypointChooserState.LOADING_LOCATION) {
			mStateManager.setState(WaypointChooserState.VISIBLE, false);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Error messaging

	public void setErrorMessage(String message) {
		mErrorMessage = message;
		if (TextUtils.isEmpty(message)) {
			mErrorTv.setVisibility(View.GONE);
			mErrorTv.setText("");
		}
		else {
			mErrorTv.setText(message);
			mErrorTv.setVisibility(View.VISIBLE);
		}
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
			//We were loading the location, but the user started typing, so lets not look like we are waiting on location
			unsetLoadingAndError();
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
						setErrorMessage(getString(R.string.waypoint_suggestion_fail));
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
			//mWaitingForLocation = true;
			//This will fire the listener when the location is found
			setErrorMessage(null);
			setState(WaypointChooserState.LOADING_LOCATION, false);
			mLocationFragment.getCurrentLocation();
		}
		else {
			unsetLoadingAndError();
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
			if (transitionIsTopToBottom(stateOne, stateTwo)) {
				mAnimFrom = mListener.getAnimOrigin();
				mMultX = (mAnimFrom.width() / (float) mSearchBarC.getWidth());
				mMultY = (mAnimFrom.height() / (float) mSearchBarC.getHeight());

				mSuggestionsC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				mBg.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				mSearchBarC.setLayerType(View.LAYER_TYPE_HARDWARE, null);

				mSearchBarC.setPivotX(0);
				mSearchBarC.setPivotY(0);
			}
		}

		@Override
		public void onStateTransitionUpdate(WaypointChooserState stateOne, WaypointChooserState stateTwo,
			float percentage) {
			if (transitionIsTopToBottom(stateOne, stateTwo)) {
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
		}

		@Override
		public void onStateTransitionEnd(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
			if (transitionIsTopToBottom(stateOne, stateTwo)) {
				mSuggestionsC.setLayerType(View.LAYER_TYPE_NONE, null);
				mBg.setLayerType(View.LAYER_TYPE_NONE, null);
				mSearchBarC.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		}

		@Override
		public void onStateFinalized(WaypointChooserState state) {
			if (mRootC != null) {
				if (state == WaypointChooserState.HIDDEN) {
					mBg.setAlpha(0f);
					mWaypointEditText.setText("");
					mLocationProgressBar.setVisibility(View.GONE);
					clearEditTextFocus(mWaypointEditText);
					setErrorMessage(null);
					if (mSuggestionsFragment != null && mSuggestionsFragment.getListAdapter() != null
						&& mSuggestionsFragment.getListAdapter().getCount() > 0) {
						//Reset the scroll position of the suggestions frag
						mSuggestionsFragment.setSelection(0);
					}
				}
				else {
					mSearchBarC.setTranslationX(0);
					mSearchBarC.setTranslationY(0);
					mSearchBarC.setScaleX(1f);
					mSearchBarC.setScaleY(1f);
					mSuggestionsC.setTranslationY(0);
					mSearchBarC.setAlpha(1f);
					mBg.setAlpha(1f);

					if (state == WaypointChooserState.LOADING_LOCATION) {
						mLocationProgressBar.setVisibility(View.VISIBLE);
						mLocationFragment.getCurrentLocation();
					}
					else {
						mLocationProgressBar.setVisibility(View.GONE);
					}
					setErrorMessage(mErrorMessage);
					requestEditTextFocus(mWaypointEditText);
				}
			}
		}

		private boolean transitionIsTopToBottom(WaypointChooserState stateOne, WaypointChooserState stateTwo) {
			return (stateOne == WaypointChooserState.HIDDEN && (stateTwo == WaypointChooserState.VISIBLE
				|| stateTwo == WaypointChooserState.LOADING_LOCATION)) || (
				(stateOne == WaypointChooserState.VISIBLE || stateOne == WaypointChooserState.LOADING_LOCATION)
					&& stateTwo == WaypointChooserState.HIDDEN);
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
		if (mStateManager.getState() == WaypointChooserState.LOADING_LOCATION) {
			unsetLoadingAndError();
			mListener.onWaypointSearchComplete(this, suggestion);
		}
	}

	@Override
	public void onCurrentLocationError(int errorCode) {
		switch (errorCode) {
		case CurrentLocationFragment.ERROR_LOCATION_DATA:
		case CurrentLocationFragment.ERROR_LOCATION_SERVICE:
			if (mStateManager.getState() == WaypointChooserState.LOADING_LOCATION) {
				setErrorMessage(getString(R.string.geolocation_failed));
				setState(WaypointChooserState.VISIBLE, false);
			}
			break;
		case CurrentLocationFragment.ERROR_SUGGEST_DATA:
		case CurrentLocationFragment.ERROR_SUGGEST_SERVICE:
			setErrorMessage(getString(R.string.waypoint_suggestion_fail));
			break;
		}
	}

}
