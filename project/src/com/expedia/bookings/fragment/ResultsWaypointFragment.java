package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.enums.ResultsSearchState;
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
public class ResultsWaypointFragment extends Fragment
	implements SuggestionsFragmentListener, FragmentAvailabilityUtils.IFragmentAvailabilityProvider,
	IStateProvider<WaypointChooserState> {

	private static final String FTAG_SUGGESTIONS = "FTAG_SUGGESTIONS";

	public static interface IResultsWaypointFragmentListener {
		public Rect getAnimOrigin();

		public void onWaypointSearchComplete(ResultsWaypointFragment caller, SuggestionV2 suggest);
	}

	private SuggestionV2 mSuggest;
	private GridManager mGrid = new GridManager();
	private SuggestionsFragment mSuggestionsFragment;
	private IResultsWaypointFragmentListener mListener;

	private FrameLayoutTouchController mRootC;
	private View mBg;
	private ViewGroup mSearchBarC;
	private ViewGroup mSuggestionsC;
	private EditText mWaypointEditText;

	private StateManager<WaypointChooserState> mStateManager = new StateManager<WaypointChooserState>(
		WaypointChooserState.HIDDEN, this);


	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, IResultsWaypointFragmentListener.class);
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

		//Setup the suggestions fragment
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mSuggestionsFragment = FragmentAvailabilityUtils.setFragmentAvailability(
			true, FTAG_SUGGESTIONS, manager,
			transaction, this, R.id.suggestions_container, false);

		transaction.commit();

		// Add a search edit text watcher.  We purposefully add it before
		// we restore the edit text's state so that it will fire and update
		// the UI properly to restore its state.
		mWaypointEditText.addTextChangedListener(new MyWatcher(mWaypointEditText));

		registerStateListener(mStateHelper, false);

		return view;
	}


	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
		mSearchStateListener.registerWithProvider(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMeasurementHelper.unregisterWithProvider(this);
		mSearchStateListener.unregisterWithProvider(this);
		getActivity().getActionBar().show();
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


	///////////////////////////////////////////////////////////////////////////
	///// StateListenerHelper<ResultsSearchState>

	private StateListenerHelper<ResultsSearchState> mSearchStateListener = new StateListenerHelper<ResultsSearchState>() {

		@Override
		public void onStateTransitionStart(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			startStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsSearchState stateOne, ResultsSearchState stateTwo,
			float percentage) {
			updateStateTransition(translateState(stateOne), translateState(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			endStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsSearchState state) {
			if (state == ResultsSearchState.FLIGHT_ORIGIN) {
				updateViewsForOrigin();
			}
			else if (state == ResultsSearchState.DESTINATION) {
				updateViewsForDestination();
			}
			setState(translateState(state), false);
		}

		private WaypointChooserState translateState(ResultsSearchState state) {
			if (state == ResultsSearchState.FLIGHT_ORIGIN || state == ResultsSearchState.DESTINATION) {
				return WaypointChooserState.VISIBLE;
			}
			return WaypointChooserState.HIDDEN;
		}
	};

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
	// SuggestionsFragmentListener

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		mSuggest = suggestion;
		mListener.onWaypointSearchComplete(this, mSuggest);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment availability provider

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_SUGGESTIONS) {
			mSuggestionsFragment = new SuggestionsFragment();
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_SUGGESTIONS) {
			return mSuggestionsFragment;
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
}
