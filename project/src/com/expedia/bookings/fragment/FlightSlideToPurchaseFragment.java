package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightBookingActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;

public class FlightSlideToPurchaseFragment extends Fragment {

	private static final String HAS_ACCEPTED_TOS = "HAS_ACCEPTED_TOS";

	private SlideToWidget mSlider;
	private boolean mHasAcceptedTOS;

	public static FlightSlideToPurchaseFragment newInstance() {
		FlightSlideToPurchaseFragment fragment = new FlightSlideToPurchaseFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_slide_to_purchase, container, false);

		// Click to accept TOS
		if (savedInstanceState != null && savedInstanceState.containsKey(HAS_ACCEPTED_TOS)) {
			mHasAcceptedTOS = savedInstanceState.getBoolean(HAS_ACCEPTED_TOS);
		}
		else {
			mHasAcceptedTOS = !(PointOfSale.getPointOfSale().requiresRulesRestrictionsCheckbox());
		}
		showHideAcceptTOS(v, false);

		// Slide To Purchase
		TextView price = Ui.findView(v, R.id.trip_price);
		String template = getResources().getString(R.string.your_card_will_be_charged_TEMPLATE);
		String text = String.format(template, Db.getFlightSearch().getSelectedFlightTrip().getTotalFare()
				.getFormattedMoney());
		price.setText(text);

		mSlider = Ui.findView(v, R.id.slide_to_wid);
		mSlider.addSlideToListener(new ISlideToListener() {

			@Override
			public void onSlideStart() {
			}

			@Override
			public void onSlideAllTheWay() {

				//Ensure proper email address
				if (User.isLoggedIn(getActivity()) && !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
					//This should always be a valid email because it is the account email
					Db.getBillingInfo().setEmail(Db.getUser().getPrimaryTraveler().getEmail());
				}

				Db.getBillingInfo().save(getActivity());

				Intent intent = new Intent(getActivity(), FlightBookingActivity.class);
				startActivity(intent);

			}

			@Override
			public void onSlideAbort() {
			}

		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mSlider != null) {
			mSlider.resetSlider();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(HAS_ACCEPTED_TOS, mHasAcceptedTOS);
	}

	private void showHideAcceptTOS(final View view, final boolean animated) {
		ViewGroup layoutConfirmTOS = Ui.findView(view, R.id.layout_confirm_tos);
		ViewGroup layoutSlideToPurchase = Ui.findView(view, R.id.layout_slide_to_purchase);

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

	private void hideAcceptTOS(final View layoutConfirmTOS, final View layoutSlideToPurchase, final boolean animated) {
		if (!animated) {
			layoutConfirmTOS.setVisibility(View.INVISIBLE);
			layoutSlideToPurchase.setVisibility(View.VISIBLE);
			return;
		}

		View iAccept = Ui.findView(layoutConfirmTOS, R.id.layout_i_accept);
		View iAcceptLeft = Ui.findView(layoutConfirmTOS, R.id.image_i_accept_left);
		View iAcceptCenter = Ui.findView(layoutConfirmTOS, R.id.text_i_accept_center);
		View iAcceptRight = Ui.findView(layoutConfirmTOS, R.id.image_i_accept_right);
		View labelDoYouAccept = Ui.findView(layoutConfirmTOS, R.id.label_do_you_accept);
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

			@SuppressLint("NewApi")
			@Override
			public void onAnimationStart(Animator arg0) {
				layoutConfirmTOS.setVisibility(View.VISIBLE);
				if (AnimatorProxy.NEEDS_PROXY) {
					AnimatorProxy.wrap(layoutSlideToPurchase).setAlpha(0f);
				}
				else {
					layoutSlideToPurchase.setAlpha(0f);
				}
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
