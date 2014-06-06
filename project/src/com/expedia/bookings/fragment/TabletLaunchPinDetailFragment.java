package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.utils.Ui;

public class TabletLaunchPinDetailFragment extends Fragment {
	private ViewGroup mRootC;

	// TODO: associated map pin data

	public static TabletLaunchPinDetailFragment newInstance() {
		TabletLaunchPinDetailFragment frag = new TabletLaunchPinDetailFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(R.layout.fragment_tablet_launch_pin_detail, container, false);

		return mRootC;
	}

	public void bind(/*TODO: associated map pin data*/) {
		ImageView roundImage = Ui.findView(getView(), R.id.round_image);
		roundImage.setImageDrawable(new RoundBitmapDrawable(getActivity(), R.drawable.mappin_sanfrancisco));
	}

	// These coordinates specify the source view from which the data expands
	public void animateFrom(View view) {
		int circleOriginX = -100;
		int circleOriginY = -210;
		float scaleOrigin = 0.5f;

		int textOriginX = -400;

		ArrayList<Animator> animations = new ArrayList<>();

		PropertyValuesHolder circleX = PropertyValuesHolder.ofFloat("translationX", circleOriginX, 0);
		PropertyValuesHolder circleY = PropertyValuesHolder.ofFloat("translationY", circleOriginY, 0);
		animations.add(ObjectAnimator.ofPropertyValuesHolder(mRootC, circleX, circleY));

		PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", scaleOrigin, 1f);
		PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", scaleOrigin, 1f);
		animations.add(ObjectAnimator.ofPropertyValuesHolder(Ui.findView(mRootC, R.id.round_image), scaleX, scaleY));

		PropertyValuesHolder textX = PropertyValuesHolder.ofFloat("translationX", textOriginX, 0);
		PropertyValuesHolder textAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
		animations.add(ObjectAnimator.ofPropertyValuesHolder(Ui.findView(mRootC, R.id.text_layout), textX, textAlpha, scaleX, scaleY));

		AnimatorSet s = new AnimatorSet();
		s.playTogether(animations);
		s.start();
	}
}
