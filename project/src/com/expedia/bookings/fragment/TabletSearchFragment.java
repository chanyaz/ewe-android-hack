package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.debug.ButtonFragment;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.mobiata.android.util.AndroidUtils;

/**
 * A large search fragment only suitable for tablet sizes.
 */
public class TabletSearchFragment extends MeasurableFragment implements OnClickListener {

	// This is a debug option - disable if you want to avoid the keyboard IME on expand, thus
	// helping for testing other performance issues.
	private static final boolean DEBUG_SHOW_KEYBOARD_ON_EXPAND = true;

	private static final float HEADER_BG_SCALE_Y = 2.0f;

	private static final String TAG_DESTINATIONS = "fragment.destinations";
	private static final String TAG_ORIGINS = "fragment.origins";
	private static final String TAG_DATES = "fragment.dates";
	private static final String TAG_GUESTS = "fragment.guests";

	// Cached views (general)
	private BlockEventFrameLayout mBlockEventFrameLayout;
	private View mBackground;

	// Cached views (header)
	private ViewGroup mHeader;
	private View mHeaderBackground;
	private View mCancelButton;
	private ViewGroup mHeaderTopContainer;
	private EditText mSearchEditText;
	private View mSearchDivider;
	private TextView mSearchDatesTextView;
	private View mSearchButton;
	private ViewGroup mHeaderBottomContainer;
	private View mOriginTextView;
	private View mGuestsTextView;

	// Cached views (content)
	private ViewGroup mContentContainer;

	// Child fragments, shown in the content container
	private Fragment mDestinationsFragment;
	private Fragment mOriginsFragment;
	private Fragment mDatesFragment;
	private Fragment mGuestsFragment;

	// Special positioning of Views
	private float mEditTextTranslationX;
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

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
		mActionBarHeight = getActivity().getActionBar().getHeight();

		mListener = Ui.findFragmentListener(this, SearchFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_search, container, false);

		mBlockEventFrameLayout = Ui.findView(view, R.id.block_event_frame_layout);
		mBackground = Ui.findView(view, R.id.background);
		mHeader = Ui.findView(view, R.id.search_header);
		mHeaderBackground = Ui.findView(view, R.id.header_background);
		mCancelButton = Ui.findView(view, R.id.cancel_button);
		mHeaderTopContainer = Ui.findView(view, R.id.search_header_top_container);
		mSearchEditText = Ui.findView(view, R.id.search_edit_text);
		mSearchDivider = Ui.findView(view, R.id.search_divider_view);
		mSearchDatesTextView = Ui.findView(view, R.id.search_dates_text_view);
		mSearchButton = Ui.findView(view, R.id.search_button);
		mHeaderBottomContainer = Ui.findView(view, R.id.search_header_bottom_container);
		mOriginTextView = Ui.findView(view, R.id.origin_text_view);
		mGuestsTextView = Ui.findView(view, R.id.guests_text_view);
		mContentContainer = Ui.findView(view, R.id.content_container);

		// Setup child fragments
		mDestinationsFragment = Ui.findChildSupportFragment(this, TAG_DESTINATIONS);
		mOriginsFragment = Ui.findChildSupportFragment(this, TAG_ORIGINS);
		mDatesFragment = Ui.findChildSupportFragment(this, TAG_DATES);
		mGuestsFragment = Ui.findChildSupportFragment(this, TAG_GUESTS);

		if (savedInstanceState == null) {
			// Always start with the destinations fragment visible
			constructDestinationsFragment();
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.add(R.id.content_container, mDestinationsFragment, TAG_DESTINATIONS);
			ft.commit();
		}

		// Setup on click listeners
		mHeaderTopContainer.setOnClickListener(this);
		mSearchEditText.setOnClickListener(this);
		mSearchDivider.setOnClickListener(this);
		mSearchDatesTextView.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		mOriginTextView.setOnClickListener(this);
		mGuestsTextView.setOnClickListener(this);

		// Configure hiding views (that change alpha during expand/collapse)
		mHiddenWhenCollapsedViews.add(mBackground);
		mHiddenWhenCollapsedViews.add(mHeaderBackground);
		mHiddenWhenCollapsedViews.add(mCancelButton);
		mHiddenWhenCollapsedViews.add(mSearchDivider);
		mHiddenWhenCollapsedViews.add(mSearchDatesTextView);
		mHiddenWhenCollapsedViews.add(mSearchButton);
		mHiddenWhenCollapsedViews.add(mHeaderBottomContainer);
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

			mSearchEditText.setFocusableInTouchMode(true);
		}

		// Configure views which use HW layers on expand/collapse animation
		//
		// We don't just set the containers to be HW layers because some parts
		// move interdependently inside of them.
		mExpandCollapseHwLayerViews.add(mHeaderBackground);
		mExpandCollapseHwLayerViews.add(mCancelButton);
		mExpandCollapseHwLayerViews.add(mSearchEditText);
		mExpandCollapseHwLayerViews.add(mSearchDatesTextView);
		mExpandCollapseHwLayerViews.add(mSearchButton);
		mExpandCollapseHwLayerViews.add(mHeaderBottomContainer);
		mExpandCollapseHwLayerViews.add(mContentContainer);

		if (Build.VERSION.SDK_INT < 16) {
			// On 16+, we use hasOverlappingRendering() == false to draw
			// super quick.  In that case, HW layers just get in the way
			// (apparently).  But on <16, hasOverlappingRendering() does
			// not exist, so HW layers are better than nothing.
			mExpandCollapseHwLayerViews.add(mBackground);
			mExpandCollapseHwLayerViews.add(mSearchDivider);
		}

		// We need to measure where to place the header (horizontally)
		mHeaderTopContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				mHeaderTopContainer.getViewTreeObserver().removeOnPreDrawListener(this);

				mEditTextTranslationX = (mHeaderTopContainer.getWidth() - mSearchEditText.getWidth()) / 2.0f;

				if (!mStartExpanded) {
					mSearchEditText.setTranslationX(mEditTextTranslationX);
				}

				return true;
			}
		});

		return view;
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
		mInitialTranslationY = translationY - (mHeaderTopContainer.getHeight() / 2.0f) - mHeader.getPaddingTop();
	}

	//////////////////////////////////////////////////////////////////////////
	// Child fragments

	public void constructDestinationsFragment() {
		if (mDestinationsFragment == null) {
			mDestinationsFragment = ButtonFragment.newInstance("Destinations", R.dimen.tablet_search_width);
		}
	}

	public void constructOriginsFragment() {
		if (mOriginsFragment == null) {
			mOriginsFragment = ButtonFragment.newInstance("Origins", R.dimen.tablet_search_width);
		}
	}

	public void constructDatesFragment() {
		if (mDatesFragment == null) {
			mDatesFragment = ButtonFragment.newInstance("Dates", R.dimen.tablet_search_width);
		}

	}

	public void constructGuestsFragment() {
		if (mGuestsFragment == null) {
			mGuestsFragment = ButtonFragment.newInstance("Guests", R.dimen.tablet_search_width);
		}
	}

	public int getFragmentPosition(String tag) {
		if (TAG_DESTINATIONS.equals(tag)) {
			return 0;
		}
		else if (TAG_DATES.equals(tag)) {
			return 1;
		}
		else if (TAG_ORIGINS.equals(tag)) {
			return 2;
		}
		else if (TAG_GUESTS.equals(tag)) {
			return 3;
		}

		throw new RuntimeException("Unrecognized tag: " + tag);
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
				PropertyValuesHolder translateXPvh = PropertyValuesHolder.ofFloat("translationX", 0);
				PropertyValuesHolder translateYPvh = PropertyValuesHolder.ofFloat("translationY", 0);
				PropertyValuesHolder scaleYPvh = PropertyValuesHolder.ofFloat("scaleY", 1);

				anims.add(ObjectAnimator.ofPropertyValuesHolder(mHeader, translateYPvh));
				anims.add(ObjectAnimator.ofPropertyValuesHolder(mSearchEditText, translateXPvh));

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

				PropertyValuesHolder fadeOutPvh = PropertyValuesHolder.ofFloat("alpha", 0);
				PropertyValuesHolder translateXPvh = PropertyValuesHolder
						.ofFloat("translationX", mEditTextTranslationX);
				PropertyValuesHolder translateYPvh = PropertyValuesHolder
						.ofFloat("translationY", mInitialTranslationY);
				PropertyValuesHolder scaleYPvh = PropertyValuesHolder.ofFloat("scaleY", HEADER_BG_SCALE_Y);

				anims.add(ObjectAnimator.ofPropertyValuesHolder(mHeader, translateYPvh));
				anims.add(ObjectAnimator.ofPropertyValuesHolder(mSearchEditText, translateXPvh));

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
				mSearchEditText.setFocusableInTouchMode(false);
				mSearchEditText.clearFocus();
				Ui.hideKeyboard(mSearchEditText, 0);
			}
			else {
				for (View view : mHiddenWhenCollapsedViews) {
					view.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mCurrentAnimation = null;

			mBlockEventFrameLayout.setBlockNewEventsEnabled(false);

			if (mIsExpanding) {
				mListener.onFinishExpand();

				mSearchEditText.setFocusableInTouchMode(true);
				mSearchEditText.requestFocus();

				if (AndroidUtils.isRelease(getActivity()) || DEBUG_SHOW_KEYBOARD_ON_EXPAND) {
					Ui.showKeyboard(mSearchEditText, null);
				}
			}
			else {
				// Once we're collapsed, replace the content container with destinations fragment (if it's
				// not already visible).  That way when this gets expanded, it's visible, since we always
				// want to start with this fragment.
				if (mDestinationsFragment == null || !mDestinationsFragment.isVisible()) {
					constructDestinationsFragment();
					getChildFragmentManager()
							.beginTransaction()
							.replace(R.id.content_container, mDestinationsFragment, TAG_DESTINATIONS)
							.commit();
				}

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
	 * 
	 * When expanding, hide at any time.
	 * 
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
		else if (!isExpanded() && (v == mHeaderTopContainer || v == mSearchEditText || v == mSearchDivider
				|| v == mSearchDatesTextView)) {
			// Expand the UI if found 
			expand();
		}
		else if (isExpanded()) {
			// Switching between different child fragments
			Fragment fragmentToShow;
			String fragmentTag;
			if (v == mSearchEditText) {
				constructDestinationsFragment();
				fragmentToShow = mDestinationsFragment;
				fragmentTag = TAG_DESTINATIONS;
			}
			else if (v == mOriginTextView) {
				constructOriginsFragment();
				fragmentToShow = mOriginsFragment;
				fragmentTag = TAG_ORIGINS;
			}
			else if (v == mSearchDatesTextView) {
				constructDatesFragment();
				fragmentToShow = mDatesFragment;
				fragmentTag = TAG_DATES;
			}
			else if (v == mGuestsTextView) {
				constructGuestsFragment();
				fragmentToShow = mGuestsFragment;
				fragmentTag = TAG_GUESTS;
			}
			else {
				// What was clicked was not a fragment tag; don't react
				return;
			}

			if (!fragmentToShow.isVisible()) {
				String currentFragmentTag = getChildFragmentManager().findFragmentById(R.id.content_container).getTag();
				int currPosition = getFragmentPosition(currentFragmentTag);
				int newPosition = getFragmentPosition(fragmentTag);

				boolean animRight = currPosition < newPosition;
				int inAnim = animRight ? R.anim.tablet_search_fragment_in_right : R.anim.tablet_search_fragment_in_left;
				int outAnim = animRight ? R.anim.tablet_search_fragment_out_right
						: R.anim.tablet_search_fragment_out_left;

				getChildFragmentManager()
						.beginTransaction()
						.setCustomAnimations(inAnim, outAnim)
						.replace(R.id.content_container, fragmentToShow, fragmentTag)
						.commit();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface SearchFragmentListener {

		public void onFinishExpand();

	}

}
