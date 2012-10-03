package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.MiniReceiptWidget;
import com.expedia.bookings.widget.ReceiptWidget;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class BookingOverviewFragment extends Fragment {
	private boolean mInCheckout = false;

	private BillingInfo mBillingInfo;

	private View mOverviewLayout;
	private View mCheckoutLayout;
	private View mReceiptView;

	private AccountButton mAccountButton;

	private TextView mTravelerInfoTextView;
	private View mPaymentInfoLayout;
	private SectionBillingInfo mCreditCardSectionButton;
	private SectionStoredCreditCard mStoredCreditCard;

	private ReceiptWidget mReceiptWidget;
	private MiniReceiptWidget mMiniReceiptWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		mOverviewLayout = Ui.findView(view, R.id.overview_layout);
		mCheckoutLayout = Ui.findView(view, R.id.checkout_layout);
		mReceiptView = Ui.findView(view, R.id.receipt);

		mAccountButton = Ui.findView(view, R.id.account_button_layout);
		mTravelerInfoTextView = Ui.findView(view, R.id.traveler_info_text_view);
		mPaymentInfoLayout = Ui.findView(view, R.id.payment_info_linear_layout);
		mStoredCreditCard = Ui.findView(view, R.id.stored_creditcard_section_layout);
		mCreditCardSectionButton = Ui.findView(view, R.id.creditcard_section_layout);

		mReceiptWidget = new ReceiptWidget(getActivity(), mReceiptView, false);
		mMiniReceiptWidget = (MiniReceiptWidget) view.findViewById(R.id.receipt_mini);

		mBillingInfo = Db.getBillingInfo();

		if (Db.getSelectedProperty() != null && Db.getSelectedRate() != null) {
			updateReceiptWidget();
			mReceiptWidget.restoreInstanceState(savedInstanceState);
		}

		// Listeners
		mAccountButton.setListener(mAccountButtonClickListener);
		mStoredCreditCard.setOnClickListener(mOnClickListener);
		mCreditCardSectionButton.setOnClickListener(mOnClickListener);
		view.findViewById(R.id.traveler_info_linear_layout).setOnClickListener(mOnClickListener);
		view.findViewById(R.id.payment_info_linear_layout).setOnClickListener(mOnClickListener);

		updateViews();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		populateTravelerData();

		updateViews();
	}

	// Public methods

	public void updateViews() {
		// Detect user state, update account button accordingly
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			mAccountButton.bind(false, true, Db.getUser());
		}
		else {
			mAccountButton.bind(false, false, null);
		}

		// Traveler button
		if (Db.getTravelers() != null && Db.getTravelers().size() > 0) {
			Traveler traveler = Db.getTravelers().get(0);
			mTravelerInfoTextView.setText(StrUtils.formatTravelerName(traveler));
		}

		// Payment button
		mStoredCreditCard.bind(mBillingInfo.getStoredCard());
		mCreditCardSectionButton.bind(mBillingInfo);

		PaymentFlowState state = PaymentFlowState.getInstance(getActivity());
		if (state == null) {
			return;
		}

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : state.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : state.hasValidCardInfo(mBillingInfo);
		if (hasStoredCard) {
			mStoredCreditCard.setVisibility(View.VISIBLE);
			mPaymentInfoLayout.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}
		else if (paymentAddressValid && paymentCCValid) {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentInfoLayout.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
		}
		else {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentInfoLayout.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}
	}

	public void updateReceiptWidget() {
		Rate discountRate = null;
		if (Db.getCreateTripResponse() != null) {
			discountRate = Db.getCreateTripResponse().getNewRate();
		}

		mReceiptWidget.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate(), discountRate);
		mMiniReceiptWidget.bind(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate());
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

	// Private methods

	private void populateTravelerData() {
		List<Traveler> travelers = Db.getTravelers();
		if (travelers == null) {
			travelers = new ArrayList<Traveler>();
			Db.setTravelers(travelers);
		}

		if (travelers.size() == 0) {
			Traveler fp = new Traveler();
			travelers.add(fp);
		}
	}

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.traveler_info_linear_layout: {
				startActivity(new Intent(getActivity(), HotelTravelerInfoOptionsActivity.class));
				break;
			}
			case R.id.payment_info_linear_layout:
			case R.id.stored_creditcard_section_layout:
			case R.id.creditcard_section_layout: {
				startActivity(new Intent(getActivity(), HotelPaymentOptionsActivity.class));
				break;
			}
			}
		}
	};

	private AccountButtonClickListener mAccountButtonClickListener = new AccountButtonClickListener() {
		@Override
		public void accountLogoutClicked() {
			// Stop refreshing user (if we're currently doing so)
			//BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
			//mRefreshedUser = false;

			// Sign out user
			User.signOut(getActivity());

			// Update UI
			mAccountButton.bind(false, false, null);

			//After logout this will clear stored cards
			updateViews();
		}

		@Override
		public void accountLoginClicked() {
			SignInFragment.newInstance(false).show(getFragmentManager(), getString(R.string.tag_signin));
			Log.i("SignInFragment shown");
		}
	};
}