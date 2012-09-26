package com.expedia.bookings.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.widget.MiniReceiptWidget;
import com.expedia.bookings.widget.ReceiptWidget;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class BookingOverviewFragment extends Fragment {
	private boolean mInCheckout = false;

	private View mOverviewLayout;
	private View mCheckoutLayout;

	private View mReceiptView;

	private ReceiptWidget mReceiptWidget;
	private MiniReceiptWidget mMiniReceiptWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		mReceiptView = view.findViewById(R.id.receipt);
		mOverviewLayout = view.findViewById(R.id.overview_layout);
		mCheckoutLayout = view.findViewById(R.id.checkout_layout);

		mReceiptWidget = new ReceiptWidget(getActivity(), mReceiptView, false);
		mMiniReceiptWidget = (MiniReceiptWidget) view.findViewById(R.id.receipt_mini);

		if (Db.getSelectedProperty() != null && Db.getSelectedRate() != null) {
			updateReceiptWidget();
			mReceiptWidget.restoreInstanceState(savedInstanceState);
		}

		// Listeners
		view.findViewById(R.id.traveler_info_linear_layout).setOnClickListener(mOnClickListener);

		return view;
	}

	public void updateReceiptWidget() {
		Rate discountRate = null;
		if (Db.getCreateTripResponse() != null) {
			discountRate = Db.getCreateTripResponse().getNewRate();
		}

		mReceiptWidget.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate(), discountRate);
		mMiniReceiptWidget.bind(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate());

		//BookingInfoUtils.determineExpediaPointsDisclaimer(getActivity(), mRootView);
		//ConfirmationUtils.determineCancellationPolicy(Db.getSelectedRate(), mRootView);
	}

	public boolean inCheckout() {
		return mInCheckout;
	}

	public void beginCheckout() {
		mInCheckout = true;

		mMiniReceiptWidget.setVisibility(View.VISIBLE);
		mCheckoutLayout.setVisibility(View.VISIBLE);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(ObjectAnimator.ofFloat(mOverviewLayout, "translationY", 0, -mReceiptView.getBottom()),
				ObjectAnimator.ofFloat(mOverviewLayout, "alpha", 1, 0),
				ObjectAnimator.ofFloat(mMiniReceiptWidget, "translationY", -mMiniReceiptWidget.getHeight(), 0),
				ObjectAnimator.ofFloat(mMiniReceiptWidget, "alpha", 0, 1),
				ObjectAnimator.ofFloat(mCheckoutLayout, "translationY", mCheckoutLayout.getBottom(), 0),
				ObjectAnimator.ofFloat(mCheckoutLayout, "alpha", 0, 1));
		set.start();
	}

	public void endCheckout() {
		mInCheckout = false;

		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator arg0) {
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mMiniReceiptWidget.setVisibility(View.INVISIBLE);
				mCheckoutLayout.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		});
		set.playTogether(ObjectAnimator.ofFloat(mOverviewLayout, "translationY", -mReceiptView.getBottom(), 0),
				ObjectAnimator.ofFloat(mOverviewLayout, "alpha", 0, 1),
				ObjectAnimator.ofFloat(mMiniReceiptWidget, "translationY", 0, -mMiniReceiptWidget.getHeight()),
				ObjectAnimator.ofFloat(mMiniReceiptWidget, "alpha", 1, 0),
				ObjectAnimator.ofFloat(mCheckoutLayout, "translationY", 0, mCheckoutLayout.getBottom()),
				ObjectAnimator.ofFloat(mCheckoutLayout, "alpha", 1, 0));
		set.start();
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.traveler_info_linear_layout: {
				startActivity(new Intent(getActivity(), HotelTravelerInfoOptionsActivity.class));
			}
			}
		}
	};
}