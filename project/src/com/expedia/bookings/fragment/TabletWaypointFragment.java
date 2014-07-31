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
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.fragment.SuggestionsFragment.SuggestionsFragmentListener;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;

/**
 * A large search fragment only suitable for tablet sizes.
 */
@TargetApi(14)
public class TabletWaypointFragment extends Fragment
	implements SuggestionsFragmentListener, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	CurrentLocationFragment.ICurrentLocationListener {

	private static final String FTAG_SUGGESTIONS = "FTAG_SUGGESTIONS";
	private static final String FTAG_LOCATION = "FTAG_LOCATION";

	private static final String STATE_WAYPOINT_CHOOSER_STATE = "STATE_WAYPOINT_CHOOSER_STATE";
	private static final String STATE_ERROR_MESSAGE = "STATE_ERROR_MESSAGE";
	private static final String STATE_HAS_BACKGROUND = "STATE_HAS_BACKGROUND";
	private static final String STATE_WAYPOINT_EDIT_TEXT = "STATE_WAYPOINT_EDIT_TEXT";

	public static interface ITabletWaypointFragmentListener {
		public Rect getAnimOrigin();
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

	private boolean mHasBackground = false;
	private String mWayPointString;

	public static TabletWaypointFragment newInstance(boolean hasBackground) {
		TabletWaypointFragment f = new TabletWaypointFragment();
		f.mHasBackground = hasBackground;
		return f;
	}

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

		//State
		if (savedInstanceState != null) {
			mErrorMessage = savedInstanceState.getString(STATE_ERROR_MESSAGE);
			mHasBackground = savedInstanceState.getBoolean(STATE_HAS_BACKGROUND);
			mWayPointString = savedInstanceState.getString(STATE_WAYPOINT_EDIT_TEXT);
		}
		
		//The background wont let any touches pass through it...
		mBg = Ui.findView(view, R.id.bg);
		mBg.setConsumeTouch(true);
		if (!mHasBackground) {
			mBg.setBackgroundDrawable(null);
		}

		mWaypointEditText = Ui.findView(view, R.id.waypoint_edit_text);
		mSearchBarC = Ui.findView(view, R.id.search_bar_container);
		mSuggestionsC = Ui.findView(view, R.id.suggestions_container);
		mLocationProgressBar = Ui.findView(view, R.id.location_loading_progress);
		mErrorTv = Ui.findView(view, R.id.error_text_view);

		// Fix waypoint EditText font
		FontCache.setTypeface(mWaypointEditText, FontCache.Font.ROBOTO_LIGHT);

		// Set up clear text button
		View clearWaypointTextView = Ui.findView(view, R.id.clear_waypoint_text);
		clearWaypointTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mWaypointEditText.setText("");
			}
		});

		// Setup the suggestions fragment
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

		//Add a click listener incase they want to re-request focus
		mWaypointEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				requestEditTextFocus(mWaypointEditText);
			}
		});

		if (!TextUtils.isEmpty(mWayPointString)) {
			mWaypointEditText.setFocusableInTouchMode(true);
			mWaypointEditText.setText(mWayPointString);
		}
		setupStateTransitions();

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
		outState.putString(STATE_ERROR_MESSAGE, mErrorMessage == null ? "" : mErrorMessage);
		outState.putBoolean(STATE_HAS_BACKGROUND, mHasBackground);
		outState.putString(STATE_WAYPOINT_EDIT_TEXT, mWaypointEditText.getText().toString());
	}

	//////////////////////////////////////////////////////////////////////////
	// General

	private boolean mLoadingLocation = false;

	public void unsetLoadingAndError() {
		mLoadingLocation = false;
		setErrorMessage(null);
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
			mWaypointEditText.setHint(R.string.search_all_hint);
		}
	}

	private TextView.OnEditorActionListener mSearchActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				if (mSuggestionsFragment != null && mLoadingLocation == false) {
					SuggestionV2 suggest = mSuggestionsFragment.getBestChoiceForFilter();
					if (suggest != null) {
						handleSuggestion(suggest, mWaypointEditText.getText().toString());
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

	protected void handleSuggestion(SuggestionV2 suggestion, String qryText) {
		Location fakeLocation = ExpediaDebugUtil.getFakeLocation(getActivity());
		if (suggestion.getResultType() == ResultType.CURRENT_LOCATION && fakeLocation != null) {
			com.expedia.bookings.data.Location location = new com.expedia.bookings.data.Location();
			location.setLatitude(fakeLocation.getLatitude());
			location.setLongitude(fakeLocation.getLongitude());
			location.setDestinationId("SFO");
			suggestion.setLocation(location);
			suggestion.setAirportCode("SFO");
		}

		if (needsLocation(suggestion)) {
			//This will fire the listener when the location is found
			setErrorMessage(null);
			mLoadingLocation = true;
			mLocationFragment.getCurrentLocation();
		}
		else {
			unsetLoadingAndError();
			Events.post(new Events.SearchSuggestionSelected(suggestion, qryText));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SuggestionsFragmentListener

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		handleSuggestion(suggestion, null);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment availability provider

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
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
	// State Transition Handlers

	private void setupStateTransitions() {
		Fragment parent = getParentFragment();
		if (parent instanceof TabletLaunchControllerFragment) {
			WaypointStateListener wsl = new WaypointStateListener(false);
			SingleStateListener waypoint = new SingleStateListener<>(
				LaunchState.DEFAULT, LaunchState.WAYPOINT, true, wsl);
			TabletLaunchControllerFragment controller = (TabletLaunchControllerFragment) parent;
			controller.registerStateListener(waypoint, false);
		}
		else if (parent instanceof TabletResultsSearchControllerFragment) {
			WaypointStateListener wsl = new WaypointStateListener(true);
			SingleStateListener flightOrigin = new SingleStateListener<>(
				ResultsSearchState.DEFAULT, ResultsSearchState.FLIGHT_ORIGIN, true, wsl);
			SingleStateListener destination = new SingleStateListener<>(
				ResultsSearchState.DEFAULT, ResultsSearchState.DESTINATION, true, wsl);

			TabletResultsSearchControllerFragment controller = (TabletResultsSearchControllerFragment) parent;
			controller.registerStateListener(flightOrigin, false);
			controller.registerStateListener(destination, false);
		}
	}

	private class WaypointStateListener implements ISingleStateListener {
		private Rect mAnimFrom;
		private float mMultX;
		private float mMultY;
		private boolean mDoFadeSearchBar;

		public WaypointStateListener(boolean doFadeSearchBar) {
			mDoFadeSearchBar = doFadeSearchBar;
		}

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mAnimFrom = ScreenPositionUtils.translateGlobalPositionToLocalPosition(mListener.getAnimOrigin(), mRootC);
			mMultX = (mAnimFrom.width() / (float) mSearchBarC.getWidth());
			mMultY = (mAnimFrom.height() / (float) mSearchBarC.getHeight());

			mSuggestionsC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			mBg.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			mSearchBarC.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			mSearchBarC.setPivotX(0);
			mSearchBarC.setPivotY(0);
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			float transX = (1f - percentage) * (mAnimFrom.left - mSearchBarC.getLeft());
			float transY = (1f - percentage) * (mAnimFrom.bottom - (mSearchBarC.getBottom()));
			float scaleX = mMultX + percentage * (1f - mMultX);
			float scaleY = mMultY + percentage * (1f - mMultY);

			mSearchBarC.setTranslationX(transX);
			mSearchBarC.setTranslationY(transY);
			mSearchBarC.setScaleX(scaleX);
			mSearchBarC.setScaleY(scaleY);
			mSuggestionsC.setTranslationY((1f - percentage) * mSuggestionsC.getHeight());
			if (mDoFadeSearchBar) {
				mSearchBarC.setAlpha(percentage);
			}
			mBg.setAlpha(percentage);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			mSuggestionsC.setLayerType(View.LAYER_TYPE_NONE, null);
			mBg.setLayerType(View.LAYER_TYPE_NONE, null);
			mSearchBarC.setLayerType(View.LAYER_TYPE_NONE, null);
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			if (mRootC == null) {
				return;
			}

			if (isReversed) {
				mBg.setAlpha(0f);
				mWaypointEditText.setText("");
				mLocationProgressBar.setVisibility(View.GONE);
				clearEditTextFocus(mWaypointEditText);
				setErrorMessage(null);
				if (mSuggestionsFragment != null
					&& mSuggestionsFragment.isAdded()
					&& mSuggestionsFragment.getListAdapter() != null
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

				if (mLoadingLocation) {
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

	//////////////////////////////////////////////////////////////////////////
	// ICurrentLocationListener

	@Override
	public void onCurrentLocation(Location location, SuggestionV2 suggestion) {
		if (mLoadingLocation) {
			unsetLoadingAndError();
			suggestion.getLocation().setLatitude(location.getLatitude());
			suggestion.getLocation().setLongitude(location.getLongitude());
			suggestion.setResultType(ResultType.CURRENT_LOCATION);
			Events.post(new Events.SearchSuggestionSelected(suggestion, null));
		}
	}

	@Override
	public void onCurrentLocationError(int errorCode) {
		switch (errorCode) {
		case CurrentLocationFragment.ERROR_LOCATION_DATA:
		case CurrentLocationFragment.ERROR_LOCATION_SERVICE:
			if (mLoadingLocation) {
				setErrorMessage(getString(R.string.geolocation_failed));
			}
			break;
		case CurrentLocationFragment.ERROR_SUGGEST_DATA:
		case CurrentLocationFragment.ERROR_SUGGEST_SERVICE:
			setErrorMessage(getString(R.string.waypoint_suggestion_fail));
			break;
		}
	}

}
