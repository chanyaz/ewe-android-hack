package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;

public class SlideToPurchaseFragment extends Fragment {

	private static final String ARG_TOTAL_PRICE_STRING = "ARG_TOTAL_PRICE";
	private static final String HAS_ACCEPTED_TOS = "HAS_ACCEPTED_TOS";

	private SlideToWidget mSlider;
	private boolean mHasAcceptedTOS;
	private String mTotalPriceString;
	private ISlideToListener mListener;

	public static SlideToPurchaseFragment newInstance(String totalPriceString) {
		SlideToPurchaseFragment fragment = new SlideToPurchaseFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TOTAL_PRICE_STRING, totalPriceString);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_slide_to_purchase, container, false);

		// Click to accept TOS
		if (savedInstanceState != null && savedInstanceState.containsKey(HAS_ACCEPTED_TOS)) {
			mHasAcceptedTOS = savedInstanceState.getBoolean(HAS_ACCEPTED_TOS);
		}
		else {
			mHasAcceptedTOS = !(PointOfSale.getPointOfSale().requiresRulesRestrictionsCheckbox());
		}
		showHideAcceptTOS(v, false);

		// Total price string
		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_TOTAL_PRICE_STRING)) {
			setTotalPriceString(v, savedInstanceState.getString(ARG_TOTAL_PRICE_STRING));
		}
		else {
			setTotalPriceString(v, getArguments().getString(ARG_TOTAL_PRICE_STRING));
		}

		mSlider = Ui.findView(v, R.id.slide_to_purchase_widget);
		mSlider.addSlideToListener(mListener);

		return v;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (!(context instanceof ISlideToListener)) {
			throw new RuntimeException(SlideToPurchaseFragment.class.getSimpleName()
				+ " must bind to an activity that implements "
				+ mListener.getClass().getSimpleName());
		}

		mListener = (ISlideToListener) context;
	}

	@Override
	public void onResume() {
		super.onResume();
		resetSlider();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ACCEPTED_TOS, mHasAcceptedTOS);
		outState.putString(ARG_TOTAL_PRICE_STRING, mTotalPriceString);
	}

	private void showHideAcceptTOS(final View view, final boolean animated) {
		ViewGroup layoutConfirmTOS = Ui.findView(view, R.id.layout_confirm_tos);
		ViewGroup layoutSlideToPurchase = Ui.findView(view, R.id.slide_to_purchase_layout);

		if (mHasAcceptedTOS) {
			if (layoutConfirmTOS.getVisibility() != View.INVISIBLE
				|| layoutSlideToPurchase.getVisibility() != View.VISIBLE) {
				hideAcceptTOS(layoutConfirmTOS, layoutSlideToPurchase, animated);
			}
		}
		else {
			if (layoutConfirmTOS.getVisibility() != View.VISIBLE
				|| layoutSlideToPurchase.getVisibility() != View.INVISIBLE) {
				showAcceptTOS(layoutConfirmTOS, layoutSlideToPurchase);
			}
		}
	}

	public void setTotalPriceString(String totalPriceString) {
		setTotalPriceString(getView(), totalPriceString);
	}

	private void setTotalPriceString(View v, String totalPriceString) {
		mTotalPriceString = totalPriceString;

		// Slide To Purchase
		TextView price = Ui.findView(v, R.id.purchase_total_text_view);
		price.setText(mTotalPriceString);
	}

	public void resetSlider() {
		if (mSlider != null) {
			mSlider.resetSlider();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Animation effects

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		Animation anim;
		if (enter) {
			anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
		}
		else {
			anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
		}
		return anim;
	}

	private void hideAcceptTOS(final View layoutConfirmTOS, final View layoutSlideToPurchase, final boolean animated) {
		if (!animated) {
			layoutConfirmTOS.setVisibility(View.INVISIBLE);
			layoutSlideToPurchase.setVisibility(View.VISIBLE);
			return;
		}

		View iAccept = Ui.findView(layoutConfirmTOS, R.id.layout_i_accept);
		View iAcceptLeft = Ui.findView(layoutConfirmTOS, R.id.i_accept_left_image);
		View iAcceptCenter = Ui.findView(layoutConfirmTOS, R.id.i_accept_center_text);
		View iAcceptRight = Ui.findView(layoutConfirmTOS, R.id.i_accept_right_image);
		View labelDoYouAccept = Ui.findView(layoutConfirmTOS, R.id.do_you_accept_label);
		View sliderImage = Ui.findView(layoutSlideToPurchase, R.id.slider_image_holder);

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
		Animator slideToAnim = ObjectAnimator.ofFloat(layoutSlideToPurchase, "alpha", 0f, 1f);
		slideToAnim.setDuration(100);

		AnimatorSet allAnim = new AnimatorSet();
		allAnim.playSequentially(iAcceptAnim, slideToAnim);
		allAnim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
				layoutConfirmTOS.setVisibility(View.INVISIBLE);
				layoutSlideToPurchase.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				layoutConfirmTOS.setVisibility(View.INVISIBLE);
				layoutSlideToPurchase.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animator arg0) {
				layoutConfirmTOS.setVisibility(View.VISIBLE);
				layoutSlideToPurchase.setAlpha(0f);
				layoutSlideToPurchase.setVisibility(View.VISIBLE);
			}
		});
		allAnim.start();
	}

	private void showAcceptTOS(final View layoutConfirmTOS, final View layoutSlideToPurchase) {
		Ui.findView(layoutConfirmTOS, R.id.layout_i_accept).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHasAcceptedTOS = true;
				hideAcceptTOS(layoutConfirmTOS, layoutSlideToPurchase, true);
			}
		});

		layoutConfirmTOS.setVisibility(View.VISIBLE);
		layoutSlideToPurchase.setVisibility(View.INVISIBLE);
	}
}
