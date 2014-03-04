package com.expedia.bookings.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.Hours;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2.ResultType;
import com.expedia.bookings.fragment.SuggestionsFragment.SuggestionsFragmentListener;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;

/**
 * A large search fragment only suitable for tablet sizes.
 */
@TargetApi(14)
public class TabletSearchFragment extends MeasurableFragment implements OnClickListener, SuggestionsFragmentListener {

	// This is a debug option - disable if you want to avoid the keyboard IME on expand, thus
	// helping for testing other performance issues.
	private static final boolean DEBUG_SHOW_KEYBOARD_ON_EXPAND = true;

	// This is a debug option - enable if you want to save search params whenever "search"
	// is clicked, and then reload whenever this fragment starts (from scratch)
	private static final boolean DEBUG_SAVE_SEARCH_PARAMS = false;
	private static final String DEBUG_SAVE_SEARCH_PARAMS_PATH = "tablet-search-params-debug.dat";

	private static final float HEADER_BG_SCALE_Y = 2.0f;

	private static final String INSTANCE_SEARCH_PARAMS = "INSTANCE_SEARCH_PARAMS";

	private static final String TAG_DESTINATIONS = "fragment.destinations";

	private SearchParams mSearchParams;

	// Cached views (general)
	private FrameLayoutTouchController mBlockEventFrameLayout;
	private View mBackground;

	// Cached views (header)
	private ViewGroup mHeader;
	private View mHeaderBackground;
	private TextView mSearchStatusTextView;
	private View mCancelButton;
	private ViewGroup mHeaderTopContainer;
	private EditText mDestinationEditText;
	private View mSearchButton;

	// Cached views (content)
	private ViewGroup mContentContainer;

	// Child fragments, shown in the content container
	private SuggestionsFragment mDestinationsFragment;

	// Special positioning of Views
	private float mInitialTranslationY;

	// Init variables
	private boolean mStartExpanded;

	// Animation
	private int mDuration;
	private boolean mIsExpanding;
	private Animator mCurrentAnimation;

	private final List<View> mHiddenWhenCollapsedViews = new ArrayList<View>();

	// Views that should use HW layers during expand/collapse animation
	private final List<View> mExpandCollapseHwLayerViews = new ArrayList<View>();

	private SearchFragmentListener mListener;

	// Cached data that shouldn't change
	private int mActionBarHeight;

	// We hold onto the "current location" which we pass along to child fragments; this
	// is to avoid duplicate work and so that they're all in sync with each other
	private android.location.Location mCurrentLocation;
	private static final int CURRENT_LOCATION_CUTOFF = Hours.ONE.toPeriod().getMillis();

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, SearchFragmentListener.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
		mActionBarHeight = getActivity().getActionBar().getHeight();

		if (savedInstanceState != null) {
			mSearchParams = savedInstanceState.getParcelable(INSTANCE_SEARCH_PARAMS);
		}
		else {
			// TODO: For now, we will just use SearchParams internally; eventually this
			// should be coming from some outside source.
			mSearchParams = new SearchParams();

			if (DEBUG_SAVE_SEARCH_PARAMS) {
				try {
					String searchParamsJsonStr = IoUtils.readStringFromFile(DEBUG_SAVE_SEARCH_PARAMS_PATH,
						getActivity());
					if (!TextUtils.isEmpty(searchParamsJsonStr)) {
						JSONObject searchParamsJson = new JSONObject(searchParamsJsonStr);
						mSearchParams.fromJson(searchParamsJson);
					}
				}
				catch (Exception e) {
					Log.w("DEBUG: Could not reload saved search params", e);
				}
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_search, container, false);

		mBlockEventFrameLayout = Ui.findView(view, R.id.block_event_frame_layout);
		mBackground = Ui.findView(view, R.id.background);
		mHeader = Ui.findView(view, R.id.search_header);
		mHeaderBackground = Ui.findView(view, R.id.header_background);
		mSearchStatusTextView = Ui.findView(view, R.id.search_status_text_view);
		mCancelButton = Ui.findView(view, R.id.cancel_button);
		mHeaderTopContainer = Ui.findView(view, R.id.search_header_top_container);
		mDestinationEditText = Ui.findView(view, R.id.destination_edit_text);
		mSearchButton = Ui.findView(view, R.id.search_button);
		mContentContainer = Ui.findView(view, R.id.content_container);

		// Setup child fragments
		mDestinationsFragment = Ui.findChildSupportFragment(this, TAG_DESTINATIONS);

		// Setup on click listeners
		mSearchStatusTextView.setOnClickListener(this);
		mDestinationEditText.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		mSearchButton.setOnClickListener(this);

		// Configure hiding views (that change alpha during expand/collapse)
		mHiddenWhenCollapsedViews.add(mBackground);
		mHiddenWhenCollapsedViews.add(mHeaderBackground);
		mHiddenWhenCollapsedViews.add(mCancelButton);
		mHiddenWhenCollapsedViews.add(mDestinationEditText);
		mHiddenWhenCollapsedViews.add(mSearchButton);
		mHiddenWhenCollapsedViews.add(mContentContainer);

		if (!mStartExpanded) {
			for (View hiddenView : mHiddenWhenCollapsedViews) {
				hiddenView.setAlpha(0);
				hiddenView.setVisibility(View.INVISIBLE);
			}

			mHeaderBackground.setScaleY(HEADER_BG_SCALE_Y);
		}
		else {
			getActivity().getActionBar().hide();

			mDestinationEditText.setFocusableInTouchMode(true);

			mSearchStatusTextView.setAlpha(0);
			mSearchStatusTextView.setVisibility(View.INVISIBLE);
		}

		// Configure views which use HW layers on expand/collapse animation
		//
		// We don't just set the containers to be HW layers because some parts
		// move interdependently inside of them.
		mExpandCollapseHwLayerViews.add(mSearchStatusTextView);
		mExpandCollapseHwLayerViews.add(mHeaderBackground);
		mExpandCollapseHwLayerViews.add(mCancelButton);
		mExpandCollapseHwLayerViews.add(mDestinationEditText);
		mExpandCollapseHwLayerViews.add(mSearchButton);
		mExpandCollapseHwLayerViews.add(mContentContainer);

		if (Build.VERSION.SDK_INT < 16) {
			// On large screens, the extra memory taken up by HW layers
			// can slow down the execution.  The background is a huge layer,
			// so if we can avoid keeping it in a HW layer, the better.
			//
			// On 16+, we use hasOverlappingRendering() == false to draw
			// super quick without needing a HW layer.
			mExpandCollapseHwLayerViews.add(mBackground);
		}

		if (savedInstanceState == null) {
			// Always start with the destinations fragment visible
			switchToFragment(TAG_DESTINATIONS);
		}
		else {
			setupViewsForChildFragment(getCurrentChildFragmentTag());
		}

		// Always make sure that we at least have the destinations/origins fragments so we can start filtering
		// before it's shown; otherwise it may not cross-fade in a pretty manner the first time.
		createDestinationsFragment();


		// Add a search edit text watcher.  We purposefully add it before
		// we restore the edit text's state so that it will fire and update
		// the UI properly to restore its state.
		mDestinationEditText.addTextChangedListener(new MyWatcher(mDestinationEditText));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(INSTANCE_SEARCH_PARAMS, mSearchParams);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Cancel all animations if we're stopping the fragment
		if (mCurrentAnimation != null) {
			mCurrentAnimation.cancel();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Manipulation

	public void setInitialTranslationY(int translationY) {
		mInitialTranslationY = translationY - mHeaderTopContainer.getHeight() - mHeader.getPaddingTop();
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

		if (AndroidUtils.isRelease(getActivity()) || DEBUG_SHOW_KEYBOARD_ON_EXPAND) {
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

	;

	// We check if the edit text is focusable; if it's not, we want it to be the default when the fragment
	// would be re-visible (aka, empty).  That way we don't have to wait for autocomplete results to load.
	void doAfterTextChanged(EditText editText) {
		if (editText == mDestinationEditText) {
			updateFilter(mDestinationsFragment, editText.isFocusableInTouchMode() ? mDestinationEditText.getText()
				: null);
		}
	}

	private void updateFilter(SuggestionsFragment fragment, CharSequence text) {
		if (fragment != null) {
			fragment.filter(text);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Child fragments

	public String getCurrentChildFragmentTag() {
		Fragment fragment = getChildFragmentManager().findFragmentById(R.id.content_container);
		if (fragment == null) {
			return null;
		}
		return fragment.getTag();
	}

	public void switchToFragment(String tag) {
		String currentTag = getCurrentChildFragmentTag();

		if (tag.equals(currentTag)) {
			return;
		}

		// Switching between different child fragments
		Fragment fragmentToShow;
		if (tag.equals(TAG_DESTINATIONS)) {
			createDestinationsFragment();
			fragmentToShow = mDestinationsFragment;
		}
		else {
			throw new RuntimeException("I do not understand the tag \"" + tag + "\"");
		}

		setupViewsForChildFragment(tag);

		getChildFragmentManager()
			.beginTransaction()
			.setCustomAnimations(R.anim.fragment_tablet_search_in, R.anim.fragment_tablet_search_out)
			.replace(R.id.content_container, fragmentToShow, tag)
			.commit();
	}

	// This configures the views related to a child Fragment
	private void setupViewsForChildFragment(String tag) {
		// We want to make sure the origin/destination text is properly updated
		if (tag.equals(TAG_DESTINATIONS) && isExpanded()) {
			requestEditTextFocus(mDestinationEditText);
		}
		else {
			clearEditTextFocus(mDestinationEditText);
		}
		mDestinationEditText.setText(mSearchParams.getDestination() != null ? getString(R.string.to_TEMPLATE,
			getSuggestionText(mSearchParams.getDestination())) : null);
	}

	private void createDestinationsFragment() {
		if (mDestinationsFragment == null) {
			mDestinationsFragment = new SuggestionsFragment();
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
	// Animation

	public boolean isExpanded() {
		return mBackground.getAlpha() == 1;
	}

	public boolean isExpanding() {
		return mCurrentAnimation != null && mIsExpanding;
	}

	public boolean isCollapsed() {
		return mBackground.getAlpha() == 0;
	}

	public boolean isCollapsing() {
		return mCurrentAnimation != null && !mIsExpanding;
	}

	public void expand() {
		if (getView() == null) {
			mStartExpanded = true;
		}
		else if (isCollapsed() || isCollapsing()) {
			mIsExpanding = true;

			if (isCollapsed()) {
				prepHardwareLayersForAnimation();

				AnimatorSet set = new AnimatorSet();
				Collection<Animator> anims = new ArrayList<Animator>();

				PropertyValuesHolder fadeInPvh = PropertyValuesHolder.ofFloat("alpha", 1);
				PropertyValuesHolder fadeOutPvh = PropertyValuesHolder.ofFloat("alpha", 0);
				PropertyValuesHolder translateYPvh = PropertyValuesHolder.ofFloat("translationY", 0);
				PropertyValuesHolder scaleYPvh = PropertyValuesHolder.ofFloat("scaleY", 1);

				anims.add(ObjectAnimator.ofPropertyValuesHolder(mHeader, translateYPvh));
				anims.add(ObjectAnimator.ofPropertyValuesHolder(mSearchStatusTextView, fadeOutPvh));

				for (View view : mHiddenWhenCollapsedViews) {
					if (view == mContentContainer) {
						// Special handling for content container
						anims.add(ObjectAnimator.ofPropertyValuesHolder(view, fadeInPvh, translateYPvh));
					}
					else if (view == mHeaderBackground) {
						// Special handling for header bg
						anims.add(ObjectAnimator.ofPropertyValuesHolder(view, fadeInPvh, scaleYPvh));
					}
					else {
						anims.add(ObjectAnimator.ofPropertyValuesHolder(view, fadeInPvh));
					}
				}

				ValueAnimator actionBarAnim = ValueAnimator.ofPropertyValuesHolder(translateYPvh);
				actionBarAnim.addUpdateListener(mActionBarUpdateListener);
				anims.add(actionBarAnim);

				set.playTogether(anims);
				set.setInterpolator(mInterpolator);
				set.setDuration(mDuration);
				set.addListener(mAnimatorListener);

				set.start();
			}
			else {
				AnimUtils.reverseAnimator(mCurrentAnimation);
			}
		}
	}

	public void collapse() {
		// We animate based on the current state
		if (isExpanded() || isExpanding()) {
			// Animate back
			mIsExpanding = false;

			if (isExpanded()) {
				prepHardwareLayersForAnimation();

				AnimatorSet set = new AnimatorSet();
				Collection<Animator> anims = new ArrayList<Animator>();

				PropertyValuesHolder fadeInPvh = PropertyValuesHolder.ofFloat("alpha", 1);
				PropertyValuesHolder fadeOutPvh = PropertyValuesHolder.ofFloat("alpha", 0);
				PropertyValuesHolder translateYPvh = PropertyValuesHolder
					.ofFloat("translationY", mInitialTranslationY);
				PropertyValuesHolder scaleYPvh = PropertyValuesHolder.ofFloat("scaleY", HEADER_BG_SCALE_Y);

				anims.add(ObjectAnimator.ofPropertyValuesHolder(mHeader, translateYPvh));
				anims.add(ObjectAnimator.ofPropertyValuesHolder(mSearchStatusTextView, fadeInPvh));

				for (View view : mHiddenWhenCollapsedViews) {
					if (view == mContentContainer) {
						anims.add(ObjectAnimator.ofPropertyValuesHolder(view, fadeOutPvh, translateYPvh));
					}
					else if (view == mHeaderBackground) {
						anims.add(ObjectAnimator.ofPropertyValuesHolder(view, fadeOutPvh, scaleYPvh));
					}
					else {
						anims.add(ObjectAnimator.ofPropertyValuesHolder(view, fadeOutPvh));
					}
				}

				ValueAnimator actionBarAnim = ValueAnimator.ofPropertyValuesHolder(translateYPvh);
				actionBarAnim.addUpdateListener(mActionBarUpdateListener);
				anims.add(actionBarAnim);

				set.playTogether(anims);
				set.setInterpolator(mInterpolator);
				set.setDuration(mDuration);
				set.addListener(mAnimatorListener);

				set.start();
			}
			else {
				AnimUtils.reverseAnimator(mCurrentAnimation);
			}
		}
		else {
			// We're just starting, don't animate
			mHeader.setTranslationY(mInitialTranslationY);
			mContentContainer.setTranslationY(mInitialTranslationY);
		}
	}

	private void prepHardwareLayersForAnimation() {
		for (View view : mExpandCollapseHwLayerViews) {
			view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}
	}

	// Returns true if mode undone, returns false otherwise
	public boolean onBackPressed() {
		if (isExpanding()) {
			collapse();
			return true;
		}

		return false;
	}

	private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	private AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {

		@Override
		public void onAnimationStart(Animator animation) {
			mCurrentAnimation = animation;

			mBlockEventFrameLayout.setBlockNewEventsEnabled(true);

			if (!mIsExpanding) {
				mSearchStatusTextView.setVisibility(View.VISIBLE);

				clearEditTextFocus(mDestinationEditText);
			}
			else {
				for (View view : mHiddenWhenCollapsedViews) {
					view.setVisibility(View.VISIBLE);
				}

				// When we start expanding, we always start in the destination fields
				mDestinationEditText.setText(null);

				// When we expand, we know the destinations fragment will be shown first.
				// Hide the vertical scrollbar, otherwise it will fade when we animate
				// causing a performance hit.
				mDestinationsFragment.getListView().setVerticalScrollBarEnabled(false);
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mCurrentAnimation = null;

			mBlockEventFrameLayout.setBlockNewEventsEnabled(false);

			if (mIsExpanding) {
				mSearchStatusTextView.setVisibility(View.INVISIBLE);

				mListener.onFinishExpand();

				requestEditTextFocus(mDestinationEditText);

				mDestinationsFragment.getListView().setVerticalScrollBarEnabled(true);
			}
			else {
				// Once we're collapsed, replace the content container with destinations fragment (if it's
				// not already visible).  That way when this gets expanded, it's visible, since we always
				// want to start with this fragment.
				switchToFragment(TAG_DESTINATIONS);

				for (View view : mHiddenWhenCollapsedViews) {
					view.setVisibility(View.INVISIBLE);
				}
			}

			for (View view : mExpandCollapseHwLayerViews) {
				view.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		}

	};

	/**
	 * We don't get to control the duration of the action bar showing/hiding, so we instead show/hide
	 * based on how much through the animation we are.
	 * <p/>
	 * When expanding, hide at any time.
	 * <p/>
	 * When collapsing, hide once the top is past where the action bar shows up.
	 */
	private AnimatorUpdateListener mActionBarUpdateListener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			ActionBar actionBar = getActivity().getActionBar();
			boolean isActionBarShowing = actionBar.isShowing();

			if (isActionBarShowing && mIsExpanding) {
				actionBar.hide();
			}
			else if (!isActionBarShowing && !mIsExpanding
				&& (Float) animation.getAnimatedValue() > mActionBarHeight) {
				actionBar.show();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	@Override
	public void onClick(View v) {
		if (v == mCancelButton) {
			getActivity().onBackPressed();
		}
		else if (!isExpanded() && v == mSearchStatusTextView) {
			// Expand the UI if found
			expand();
		}
		else if (v == mSearchButton) {
			if (DEBUG_SAVE_SEARCH_PARAMS) {
				// Do this in a BG thread
				(new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							long start = System.nanoTime();
							IoUtils.writeStringToFile(DEBUG_SAVE_SEARCH_PARAMS_PATH, mSearchParams.toJson().toString(),
								getActivity());
							long end = System.nanoTime();
							Log.d("DEBUG: Saved search params in " + ((end - start) / 1000000) + " ms");
						}
						catch (IOException e) {
							Log.w("DEBUG: Could not save search params", e);
						}
					}
				})).start();
			}

			mListener.onSearch(mSearchParams);
		}
		else if (isExpanded()) {
			// Switching between different child fragments
			if (v == mDestinationEditText) {
				switchToFragment(TAG_DESTINATIONS);
			}
			else {
				// What was clicked was not a fragment tag; don't react
				return;
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SuggestionsFragmentListener

	@Override
	public void onSuggestionClicked(Fragment fragment, SuggestionV2 suggestion) {
		if (fragment.getTag().equals(TAG_DESTINATIONS)) {
			mSearchParams.setDestination(suggestion);
			setupViewsForChildFragment(TAG_DESTINATIONS);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface SearchFragmentListener {

		public void onFinishExpand();

		public void onSearch(SearchParams searchParams);

	}

}
