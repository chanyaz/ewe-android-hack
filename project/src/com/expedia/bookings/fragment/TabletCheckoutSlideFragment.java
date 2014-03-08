package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.widget.SlideToWidget;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutSlideFragment extends Fragment implements ICheckoutDataListener,
	CheckoutLoginButtonsFragment.ILoginStateChangedListener {

	private static final String HAS_ACCEPTED_TOS = "HAS_ACCEPTED_TOS";

	private ViewGroup mRootC;
	private ViewGroup mAcceptContainer;
	private ViewGroup mSlideContainer;
	private ViewGroup mBookContainer;
	private SlideToWidget mSlideToWidget;

	private boolean mHasAcceptedTOS;

	public static TabletCheckoutSlideFragment newInstance() {
		TabletCheckoutSlideFragment frag = new TabletCheckoutSlideFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		boolean isCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
		if (isCheckout) {
			((TabletCheckoutControllerFragment) getParentFragment()).registerStateListener(mStateHelper, true);
		}
	}

	private StateListenerHelper<CheckoutState> mStateHelper = new StateListenerHelper<CheckoutState>() {

		@Override
		public void onStateTransitionStart(CheckoutState stateOne, CheckoutState stateTwo) {
		}

		@Override
		public void onStateTransitionUpdate(CheckoutState stateOne, CheckoutState stateTwo, float percentage) {
		}

		@Override
		public void onStateTransitionEnd(CheckoutState stateOne, CheckoutState stateTwo) {
		}

		@Override
		public void onStateFinalized(CheckoutState state) {
			resetSlider();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_slide_to_purchase, container, false);

		if (savedInstanceState != null && savedInstanceState.containsKey(HAS_ACCEPTED_TOS)) {
			mHasAcceptedTOS = savedInstanceState.getBoolean(HAS_ACCEPTED_TOS);
		}
		else {
			mHasAcceptedTOS = !(PointOfSale.getPointOfSale().requiresRulesRestrictionsCheckbox());
		}

		mAcceptContainer = Ui.findView(mRootC, R.id.accept_tos_container);
		mSlideContainer = Ui.findView(mRootC, R.id.slide_container);
		mBookContainer = Ui.findView(mRootC, R.id.book_container);

		Ui.findView(mAcceptContainer, R.id.layout_i_accept).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHasAcceptedTOS = true;
				hideAcceptTOS(true);
			}
		});

		mSlideToWidget = Ui.findView(mRootC, R.id.slide_to_purchase_widget);
		mSlideToWidget.addSlideToListener(mSlideListener);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		bindAll();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (Db.getTravelersAreDirty()) {
			Db.kickOffBackgroundTravelerSave(getActivity());
		}

		if (Db.getBillingInfoIsDirty()) {
			Db.kickOffBackgroundBillingInfoSave(getActivity());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ACCEPTED_TOS, mHasAcceptedTOS);
	}

	/*
	 * BINDING
	 */

	public void bindAll() {
		if (mRootC == null) {
			return;
		}

		mSlideToWidget.resetSlider();
		if (mHasAcceptedTOS) {
			hideAcceptTOS(false);
		}
		else {
			showAcceptTOS();
		}
	}

	public void resetSlider() {
		if (mSlideToWidget != null) {
			mSlideToWidget.resetSlider();
		}
	}

	/*
	 * ISlideToListener
	 */

	SlideToWidget.ISlideToListener mSlideListener = new SlideToWidget.ISlideToListener() {
		@Override
		public void onSlideStart() {
		}

		@Override
		public void onSlideAllTheWay() {
			Activity activity = getActivity();
			if (activity instanceof TabletCheckoutActivity) {
				((TabletCheckoutActivity) activity).setCheckoutState(CheckoutState.CVV, true);
			}
		}

		@Override
		public void onSlideAbort() {
		}
	};

	/*
	 * ICheckoutDataListener
	 */

	@Override
	public void onCheckoutDataUpdated() {
		bindAll();
	}

	/*
	 * CheckoutLoginButtonsFragment.ILoginStateChangedListener
	 */

	@Override
	public void onLoginStateChanged() {
		bindAll();
	}

	/*
	 * Show/hide "I accept TOS" button, smoothly
	 */

	private void hideAcceptTOS(final boolean animated) {

		// Short circuit if it's already hidden
		if (mAcceptContainer.getVisibility() == View.INVISIBLE) {
			mSlideToWidget.resetSlider();
			return;
		}

		// Skip animation if requested
		if (!animated) {
			mAcceptContainer.setVisibility(View.INVISIBLE);
			mSlideContainer.setVisibility(View.VISIBLE);
			mSlideToWidget.resetSlider();
			mBookContainer.setVisibility(View.INVISIBLE);
			return;
		}

		View iAccept = Ui.findView(mAcceptContainer, R.id.layout_i_accept);
		View iAcceptLeft = Ui.findView(mAcceptContainer, R.id.i_accept_left_image);
		View iAcceptCenter = Ui.findView(mAcceptContainer, R.id.i_accept_center_text);
		View iAcceptRight = Ui.findView(mAcceptContainer, R.id.i_accept_right_image);
		View labelDoYouAccept = Ui.findView(mAcceptContainer, R.id.do_you_accept_label);
		View sliderImage = Ui.findView(mSlideContainer, R.id.slider_image_holder);

		List<Animator> iAcceptList = new ArrayList<Animator>();

		// Fade out the "do you accept" label
		iAcceptList.add(ObjectAnimator.ofFloat(labelDoYouAccept, "alpha", 1f, 0f));

		// Gracefully morph the "I accept" button into the slide to accept circle
		Rect sliderRect = new Rect();
		sliderImage.getGlobalVisibleRect(sliderRect);

		// I accept layout should move itself and its children over
		// to fit on top of the slide to purchase button.
		Rect iAcceptRect = new Rect();
		iAccept.getGlobalVisibleRect(iAcceptRect);
		float translateX = (float) sliderRect.left - iAcceptRect.left + 28;
		float translateY = (float) sliderRect.top - iAcceptRect.top + 28;
		iAcceptList.add(ObjectAnimator.ofFloat(iAccept, "translationX", 0f, translateX));
		iAcceptList.add(ObjectAnimator.ofFloat(iAccept, "translationY", 0f, translateY));

		// Right half of the I accept button
		// should slide over to butt up against the left half
		translateX = iAcceptLeft.getRight() - iAcceptRight.getLeft();
		iAcceptList.add(ObjectAnimator.ofFloat(iAcceptRight, "translationX", 0f, translateX));

		// Middle of the I accept button should shrink down to nothing
		translateX = -iAcceptCenter.getWidth() / 2.0f;
		iAcceptList.add(ObjectAnimator.ofFloat(iAcceptCenter, "scaleX", 1f, 0f));
		iAcceptList.add(ObjectAnimator.ofFloat(iAcceptCenter, "translationX", 0f, translateX));

		// All of the "I accept" animators put together
		AnimatorSet iAcceptAnim = new AnimatorSet();
		iAcceptAnim.playTogether(iAcceptList);
		iAcceptAnim.setDuration(250);

		// Fade in the "slide to purchase"
		Animator slideToAnim = ObjectAnimator.ofFloat(mSlideContainer, "alpha", 0f, 1f);
		slideToAnim.setDuration(100);

		AnimatorSet allAnim = new AnimatorSet();
		allAnim.playSequentially(iAcceptAnim, slideToAnim);
		allAnim.addListener(new Animator.AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
				mAcceptContainer.setVisibility(View.INVISIBLE);
				mSlideContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAcceptContainer.setVisibility(View.INVISIBLE);
				mSlideContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator arg0) {
				mAcceptContainer.setVisibility(View.VISIBLE);
				ViewHelper.setAlpha(mSlideContainer, 0f);
				mSlideContainer.setVisibility(View.VISIBLE);
			}
		});
		allAnim.start();
	}

	private void showAcceptTOS() {
		mAcceptContainer.setVisibility(View.VISIBLE);
		mSlideContainer.setVisibility(View.INVISIBLE);
		mBookContainer.setVisibility(View.INVISIBLE);
		mSlideToWidget.resetSlider();

		View iAccept = Ui.findView(mAcceptContainer, R.id.layout_i_accept);
		View iAcceptCenter = Ui.findView(mAcceptContainer, R.id.i_accept_center_text);
		View iAcceptRight = Ui.findView(mAcceptContainer, R.id.i_accept_right_image);
		View labelDoYouAccept = Ui.findView(mAcceptContainer, R.id.do_you_accept_label);
		labelDoYouAccept.setAlpha(1f);
		iAccept.setTranslationX(0f);
		iAccept.setTranslationY(0f);
		iAcceptRight.setTranslationX(0f);
		iAcceptCenter.setScaleX(1f);
		iAcceptCenter.setTranslationX(0f);
	}


}
