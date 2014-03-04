package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
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
import com.expedia.bookings.fragment.SuggestionsFragment.SuggestionsFragmentListener;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.util.AndroidUtils;

/**
 * A large search fragment only suitable for tablet sizes.
 */
@TargetApi(14)
public class ResultsWaypointFragment extends Fragment
	implements SuggestionsFragmentListener, FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final String FTAG_SUGGESTIONS = "FTAG_SUGGESTIONS";

	public static interface IResultsWaypointFragmentListener {
		public void onWaypointSearchComplete(ResultsWaypointFragment caller, SuggestionV2 suggest);
	}

	private SuggestionV2 mSuggest;
	private GridManager mGrid = new GridManager();
	private SuggestionsFragment mSuggestionsFragment;
	private IResultsWaypointFragmentListener mListener;

	private FrameLayoutTouchController mRootC;
	private ViewGroup mSearchBarC;
	private ViewGroup mSuggestionsC;
	private View mSearchBtn;
	private View mCancelBtn;
	private EditText mWaypointEditText;


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

		mWaypointEditText = Ui.findView(view, R.id.waypoint_edit_text);
		mSearchBarC = Ui.findView(view, R.id.search_bar_conatiner);
		mSuggestionsC = Ui.findView(view, R.id.suggestions_container);
		mSearchBtn = Ui.findView(view, R.id.search_button);
		mCancelBtn = Ui.findView(view, R.id.cancel_button);

		mSearchBtn.setOnClickListener(mSearchClick);
		mCancelBtn.setOnClickListener(mCancelClick);

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

		return view;
	}


	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
		getActivity().getActionBar().hide();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMeasurementHelper.unregisterWithProvider(this);
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

		if (AndroidUtils.isRelease(getActivity())) {
			Ui.showKeyboard(editText, null);
		}
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

	//////////////////////////////////////////////////////////////////////////
	// Clicks

	private View.OnClickListener mSearchClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (mSuggest == null) {
				//TODO: Remove
				Ui.showToast(getActivity(), "FAIL FAIL FAIL, must select waypoing");
			}
			else {
				mListener.onWaypointSearchComplete(ResultsWaypointFragment.this, mSuggest);
			}
		}
	};

	private View.OnClickListener mCancelClick = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			mListener.onWaypointSearchComplete(ResultsWaypointFragment.this, null);
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
	// SuggestionsFragmentListener

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		mSuggest = suggestion;
		mWaypointEditText.setText(getSuggestionText(suggestion));
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
	// Measurement listener

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);
			mGrid.setNumRows(2);
			mGrid.setNumCols(3);

			mGrid.setRowSize(0, getActivity().getActionBar().getHeight());

			mGrid.setColumnPercentage(0, 0.2f);
			mGrid.setColumnPercentage(1, 0.6f);
			mGrid.setColumnPercentage(2, 0.2f);

			mGrid.setContainerToRow(mSearchBarC, 0);
			mGrid.setContainerToColumn(mSearchBarC, 1);

			mGrid.setContainerToRow(mSuggestionsC, 1);
			mGrid.setContainerToColumnSpan(mSuggestionsC, 0, 2);

		}
	};

}
