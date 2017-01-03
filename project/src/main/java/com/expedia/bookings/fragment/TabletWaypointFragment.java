package com.expedia.bookings.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.dialog.NoLocationServicesDialog;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.fragment.SuggestionsFragment.SuggestionsFragmentListener;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.launch.fragment.TabletLaunchControllerFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.ExpediaNetUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TouchableFrameLayout;

/**
 * A large search fragment only suitable for tablet sizes.
 */
public class TabletWaypointFragment extends Fragment
	implements SuggestionsFragmentListener, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	CurrentLocationFragment.ICurrentLocationListener {

	private static final String FTAG_SUGGESTIONS = "FTAG_SUGGESTIONS";
	private static final String FTAG_LOCATION = "FTAG_LOCATION";

	private static final String STATE_HAS_BACKGROUND = "STATE_HAS_BACKGROUND";
	private static final String STATE_WAYPOINT_EDIT_TEXT = "STATE_WAYPOINT_EDIT_TEXT";

	public interface ITabletWaypointFragmentListener {
		Rect getAnimOrigin();
	}

	private SuggestionsFragment mSuggestionsFragment;
	private ITabletWaypointFragmentListener mListener;
	private CurrentLocationFragment mLocationFragment;

	private ViewGroup mRootC;
	private TouchableFrameLayout mBg;
	private View mCancelButton;
	private ViewGroup mSearchBarC;
	private ViewGroup mSuggestionsC;
	private EditText mWaypointEditText;
	private ProgressBar mLocationProgressBar;

	private boolean mHasBackground = false;
	private String mWayPointString;

	public static TabletWaypointFragment newInstance(boolean hasBackground) {
		TabletWaypointFragment waypointFragment = new TabletWaypointFragment();
		waypointFragment.mHasBackground = hasBackground;
		return waypointFragment;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, ITabletWaypointFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_waypoint, container, false);
		mRootC = Ui.findView(view, R.id.root_layout);

		//State
		if (savedInstanceState != null) {
			mHasBackground = savedInstanceState.getBoolean(STATE_HAS_BACKGROUND);
			mWayPointString = savedInstanceState.getString(STATE_WAYPOINT_EDIT_TEXT);
		}

		//The background wont let any touches pass through it...
		mBg = Ui.findView(view, R.id.bg);
		mBg.setConsumeTouch(true);
		if (!mHasBackground) {
			mBg.setBackgroundDrawable(null);
		}
		mBg.setTouchListener(mBgTouchListener);

		mWaypointEditText = Ui.findView(view, R.id.waypoint_edit_text);
		mCancelButton = Ui.findView(view, R.id.cancel_button);
		mSearchBarC = Ui.findView(view, R.id.search_bar_container);
		mSuggestionsC = Ui.findView(view, R.id.suggestions_container);
		mLocationProgressBar = Ui.findView(view, R.id.location_loading_progress);

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

		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismissWayPointFragment();
			}
		});

		if (!TextUtils.isEmpty(mWayPointString)) {
			mWaypointEditText.setFocusableInTouchMode(true);
			mWaypointEditText.setText(mWayPointString);
		}
		setupStateTransitions();

		return view;
	}

	private void dismissWayPointFragment() {
		Fragment parent = getParentFragment();
		if (parent instanceof TabletLaunchControllerFragment) {
			((TabletLaunchControllerFragment) parent).setLaunchState(LaunchState.OVERVIEW, true);
		}
		else if (parent instanceof TabletResultsSearchControllerFragment) {
			((TabletResultsSearchControllerFragment) parent).setStateToBaseState(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
		mLoadingLocation = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_HAS_BACKGROUND, mHasBackground);
		outState.putString(STATE_WAYPOINT_EDIT_TEXT, mWaypointEditText.getText().toString());
	}

	//////////////////////////////////////////////////////////////////////////
	// General

	private boolean mLoadingLocation = false;

	public void unsetLoading() {
		mLoadingLocation = false;
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
			unsetLoading();
		}
	}

	private void updateFilter(SuggestionsFragment fragment, CharSequence text) {
		if (fragment != null) {
			fragment.filter(text);
		}
	}

	public void updateViewsForOrigin() {
		if (mWaypointEditText != null) {
			mWaypointEditText.setHint(R.string.Fly_from_dot_dot_dot);
			mWaypointEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tablet_origin_arrow, 0, 0, 0);
		}
		SuggestionProvider.enableCurrentLocation(false);
		SuggestionProvider.setShowNearbyAiports(true);
		doAfterTextChanged(mWaypointEditText);
	}

	public void updateViewsForDestination() {
		if (mWaypointEditText != null) {
			mWaypointEditText.setHint(R.string.search_all_hint);
			mWaypointEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_searchbox, 0, 0, 0);
		}
		SuggestionProvider.enableCurrentLocation(true);
		SuggestionProvider.setShowNearbyAiports(false);
		doAfterTextChanged(mWaypointEditText);
	}

	private TextView.OnEditorActionListener mSearchActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				if (mSuggestionsFragment != null && !mLoadingLocation) {
					SuggestionV2 suggest = mSuggestionsFragment.getBestChoiceForFilter();
					if (suggest != null) {
						handleSuggestion(suggest);
						return true;
					}
					else {
						requestEditTextFocus(mWaypointEditText);
					}
				}
			}
			return false;
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Utils

	protected boolean needsLocation(SuggestionV2 suggestion) {
		boolean latLongZero = suggestion.getLocation().getLatitude() == 0
			&& suggestion.getLocation().getLongitude() == 0;

		return suggestion.getResultType() == SuggestionV2.ResultType.CURRENT_LOCATION
			&& (suggestion.getLocation() == null || (latLongZero));
	}

	protected void handleSuggestion(SuggestionV2 suggestion) {
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
			mLoadingLocation = true;
			mLocationFragment.getCurrentLocation();
		}
		else {
			unsetLoading();
			Events.post(new Events.SearchSuggestionSelected(suggestion));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SuggestionsFragmentListener

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		if (!ExpediaNetUtils.isOnline(getActivity())) {
			Events.post(
				new Events.ShowNoInternetDialog(SimpleCallbackDialogFragment.CODE_TABLET_NO_NET_CONNECTION_SEARCH));
		}
		else {
			handleSuggestion(suggestion);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment availability provider

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (FTAG_SUGGESTIONS.equals(tag)) {
			return mSuggestionsFragment;
		}
		else if (FTAG_LOCATION.equals(tag)) {
			return mLocationFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (FTAG_SUGGESTIONS.equals(tag)) {
			return new SuggestionsFragment();
		}
		else if (FTAG_LOCATION.equals(tag)) {
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
				LaunchState.OVERVIEW, LaunchState.WAYPOINT, true, wsl);
			TabletLaunchControllerFragment controller = (TabletLaunchControllerFragment) parent;
			controller.registerStateListener(waypoint, false);
		}
		else if (parent instanceof TabletResultsSearchControllerFragment) {
			WaypointStateListener wsl = new WaypointStateListener(true);
			SingleStateListener defaultFlightOrigin = new SingleStateListener<>(
				ResultsSearchState.DEFAULT, ResultsSearchState.FLIGHT_ORIGIN, true, wsl);
			SingleStateListener defaultDestination = new SingleStateListener<>(
				ResultsSearchState.DEFAULT, ResultsSearchState.DESTINATION, true, wsl);
			SingleStateListener calendarFlightOrigin = new SingleStateListener<>(
				ResultsSearchState.CALENDAR, ResultsSearchState.FLIGHT_ORIGIN, true, wsl);
			SingleStateListener calendarDestination = new SingleStateListener<>(
				ResultsSearchState.CALENDAR, ResultsSearchState.DESTINATION, true, wsl);

			TabletResultsSearchControllerFragment controller = (TabletResultsSearchControllerFragment) parent;
			controller.registerStateListener(defaultFlightOrigin, false);
			controller.registerStateListener(defaultDestination, false);
			controller.registerStateListener(calendarFlightOrigin, false);
			controller.registerStateListener(calendarDestination, false);
		}
	}

	// Show the cancel button if there's enough room for it.
	private boolean showCancelButton() {
		return isAdded() ? getResources().getBoolean(R.bool.show_waypoint_cancel_button) : false;
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
			if (showCancelButton()) {
				mCancelButton.setVisibility(View.VISIBLE);
				mCancelButton.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
			else {
				mCancelButton.setVisibility(View.GONE);
			}
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
			if (showCancelButton()) {
				mCancelButton.setAlpha(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			mSuggestionsC.setLayerType(View.LAYER_TYPE_NONE, null);
			mBg.setLayerType(View.LAYER_TYPE_NONE, null);
			mSearchBarC.setLayerType(View.LAYER_TYPE_NONE, null);
			if (showCancelButton()) {
				mCancelButton.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			if (mRootC == null || !isAdded()) {
				return;
			}

			if (showCancelButton()) {
				mCancelButton.setVisibility(View.VISIBLE);
				mCancelButton.setAlpha(isReversed ? 0f : 1f);
			}
			else {
				mCancelButton.setVisibility(View.GONE);
			}

			if (isReversed) {
				mBg.setAlpha(0f);
				mWaypointEditText.setText("");
				mLocationProgressBar.setVisibility(View.GONE);
				clearEditTextFocus(mWaypointEditText);
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
				requestEditTextFocus(mWaypointEditText);
			}
		}
	}

	/**
	 * This is a {@link TouchableFrameLayout.TouchListener} that listens to user touches/taps
	 * outside the autocomplete real estate. When user taps/touches outside the view
	 * let's close the search fragment.
	 */
	public TouchableFrameLayout.TouchListener mBgTouchListener = new TouchableFrameLayout.TouchListener() {
		@Override
		public void onInterceptTouch(MotionEvent ev) {
			// Nothing to do here
		}

		@Override
		public void onTouch(MotionEvent ev) {
			dismissWayPointFragment();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// ICurrentLocationListener

	@Override
	public void onCurrentLocation(Location location, SuggestionV2 suggestion) {
		if (mLoadingLocation) {
			unsetLoading();
			suggestion.getLocation().setLatitude(location.getLatitude());
			suggestion.getLocation().setLongitude(location.getLongitude());
			suggestion.setResultType(ResultType.CURRENT_LOCATION);
			Events.post(new Events.SearchSuggestionSelected(suggestion));
	}
	}

	@Override
	public void onCurrentLocationError(int errorCode) {
		switch (errorCode) {
		case CurrentLocationFragment.ERROR_LOCATION_DATA: // FALL THRU
		case CurrentLocationFragment.ERROR_LOCATION_SERVICE:
			if (mLoadingLocation) {
				NoLocationServicesDialog dialog = NoLocationServicesDialog.newInstance();
				dialog.show(getFragmentManager(), NoLocationServicesDialog.TAG);
			}
			break;
		case CurrentLocationFragment.ERROR_SUGGEST_DATA: // FALL THRU
		case CurrentLocationFragment.ERROR_SUGGEST_SERVICE:
			Ui.showToast(getActivity(), R.string.geolocation_failed);
			break;
		}
	}
}
