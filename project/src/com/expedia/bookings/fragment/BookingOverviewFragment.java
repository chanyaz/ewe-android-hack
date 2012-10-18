package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelBookingActivity;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.HotelReceipt;
import com.expedia.bookings.widget.HotelReceiptMini;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.view.ViewHelper;

public class BookingOverviewFragment extends Fragment {
	public interface BookingOverviewFragmentListener {
		public void checkoutStarted();

		public void checkoutEnded();
	}

	private boolean mInCheckout = false;
	private BookingOverviewFragmentListener mBookingOverviewFragmentListener;

	private BillingInfo mBillingInfo;

	private ScrollView mScrollView;
	private ScrollViewListener mScrollViewListener;

	private HotelReceipt mHotelReceipt;
	private View mCheckoutLayout;

	private AccountButton mAccountButton;
	private TextView mTravelerInfoTextView;
	private View mPaymentInfoLayout;
	private SectionBillingInfo mCreditCardSectionButton;
	private SectionStoredCreditCard mStoredCreditCard;
	private TextView mRulesRestrictionsTextView;
	private TextView mExpediaPointsDisclaimerTextView;
	private View mSlideToPurchaseLayoutSpacerView;

	private HotelReceiptMini mHotelReceiptMini;
	private View mSlideToPurchaseLayout;
	private TextView mCancelationPolicyTextView;

	private SlideToWidget mSlideToPurchaseWidget;
	private TextView mPurchaseTotalTextView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof BookingOverviewFragmentListener)) {
			throw new RuntimeException(
					"BookingOverviewFragment Activity must implement BookingOverviewFragmentListener");
		}

		mBookingOverviewFragmentListener = (BookingOverviewFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		mScrollView = Ui.findView(view, R.id.scroll_view);

		mHotelReceipt = Ui.findView(view, R.id.receipt);
		mCheckoutLayout = Ui.findView(view, R.id.checkout_layout);

		mAccountButton = Ui.findView(view, R.id.account_button_layout);
		mTravelerInfoTextView = Ui.findView(view, R.id.traveler_info_text_view);
		mPaymentInfoLayout = Ui.findView(view, R.id.payment_info_linear_layout);
		mStoredCreditCard = Ui.findView(view, R.id.stored_creditcard_section_layout);
		mCreditCardSectionButton = Ui.findView(view, R.id.creditcard_section_layout);
		mRulesRestrictionsTextView = Ui.findView(view, R.id.rules_restrictions_text_view);
		mExpediaPointsDisclaimerTextView = Ui.findView(view, R.id.expedia_points_disclaimer_text_view);
		mCancelationPolicyTextView = Ui.findView(view, R.id.cancellation_policy_text_view);
		mSlideToPurchaseLayoutSpacerView = Ui.findView(view, R.id.slide_to_purchase_layout_spacer_view);

		mHotelReceiptMini = Ui.findView(view, R.id.sticky_receipt_mini);
		mSlideToPurchaseLayout = Ui.findView(view, R.id.slide_to_purchase_layout);

		mSlideToPurchaseWidget = Ui.findView(view, R.id.slide_to_purchase_widget);
		mPurchaseTotalTextView = Ui.findView(view, R.id.purchase_total_text_view);

		mScrollViewListener = new ScrollViewListener(mScrollView.getContext());

		mScrollView.setOnScrollListener(mScrollViewListener);
		mScrollView.setOnTouchListener(mScrollViewListener);
		mHotelReceipt.setOnSizeChangedListener(mScrollViewListener);
		mHotelReceiptMini.setOnSizeChangedListener(mScrollViewListener);

		mSlideToPurchaseWidget.addSlideToListener(mSlideToListener);

		ViewHelper.setAlpha(mCheckoutLayout, 0);

		mBillingInfo = Db.getBillingInfo();

		if (Db.getSelectedProperty() != null && Db.getSelectedRate() != null) {
			updateReceiptWidget();
			mHotelReceipt.restoreInstanceState(savedInstanceState);
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

		// Disclaimers
		mRulesRestrictionsTextView.setText(RulesRestrictionsUtils.getRulesRestrictionsConfirmation(getActivity()));
		mExpediaPointsDisclaimerTextView.setText(R.string.disclaimer_expedia_points);

		Policy cancellationPolicy = Db.getSelectedRate().getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			mCancelationPolicyTextView.setText(Html.fromHtml(cancellationPolicy.getDescription()));
		}
		else {
			mCancelationPolicyTextView.setVisibility(View.GONE);
		}

		// Purchase total
		Money displayedTotal;
		if (LocaleUtils.shouldDisplayMandatoryFees(getActivity())) {
			displayedTotal = Db.getSelectedRate().getTotalPriceWithMandatoryFees();
		}
		else {
			displayedTotal = Db.getSelectedRate().getTotalAmountAfterTax();
		}

		mPurchaseTotalTextView.setText(getString(R.string.your_card_will_be_charged_TEMPLATE,
				displayedTotal.getFormattedMoney()));

		if (mInCheckout && Db.getTravelers() != null && Db.getTravelers().size() > 0 && mBillingInfo != null) {
			showSlideToPurchsaeView();
		}
		else {
			hideSlideToPurchaseView();
		}
	}

	public void updateReceiptWidget() {
		Rate discountRate = null;
		if (Db.getCreateTripResponse() != null) {
			discountRate = Db.getCreateTripResponse().getNewRate();
		}

		mHotelReceipt.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate(), discountRate);
		mHotelReceiptMini.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate());
	}

	public boolean inCheckout() {
		return mInCheckout;
	}

	public void startCheckout() {
		if (mBookingOverviewFragmentListener != null) {
			mBookingOverviewFragmentListener.checkoutStarted();
		}

		// Scroll to checkout
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.smoothScrollTo(0, mScrollViewListener.getMaxY());
			}
		});

		mInCheckout = true;
		//showSlideToPurchsaeView();
	}

	public void endCheckout() {
		if (mBookingOverviewFragmentListener != null) {
			mBookingOverviewFragmentListener.checkoutEnded();
		}

		mInCheckout = false;

		// Scroll to start
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.smoothScrollTo(0, 0);
			}
		});
		hideSlideToPurchaseView();
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

	// Hide/show slide to purchase view

	private void showSlideToPurchsaeView() {
		mSlideToPurchaseLayout.setVisibility(View.VISIBLE);
		mSlideToPurchaseLayoutSpacerView.setVisibility(View.VISIBLE);

		mSlideToPurchaseLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up));
	}

	private void hideSlideToPurchaseView() {
		Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mSlideToPurchaseLayoutSpacerView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSlideToPurchaseLayout.setVisibility(View.GONE);
			}
		});

		mSlideToPurchaseLayout.startAnimation(animation);
	}

	// Listeners

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

	private SlideToWidget.ISlideToListener mSlideToListener = new ISlideToListener() {
		@Override
		public void onSlideStart() {
		}

		@Override
		public void onSlideAllTheWay() {
			startActivity(new Intent(getActivity(), HotelBookingActivity.class));
		}

		@Override
		public void onSlideAbort() {
		}
	};

	// Scroll Listener

	private class ScrollViewListener extends GestureDetector.SimpleOnGestureListener implements OnScrollListener,
			OnTouchListener, HotelReceipt.OnSizeChangedListener, HotelReceiptMini.OnSizeChangedListener {

		private static final float FADE_RANGE = 100.0f;

		private static final int SWIPE_MIN_DISTANCE = 100;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		private GestureDetector mGestureDetector;

		private final float mScaledFadeRange;
		private final float mMarginTop;

		private boolean mTouchDown = false;
		private int mScrollY = 0;

		private int mReceiptHeight;
		private int mReceiptMiniHeight;

		private int mMidY;
		private int mMaxY;

		public ScrollViewListener(Context context) {
			mGestureDetector = new GestureDetector(context, this);
			mScaledFadeRange = FADE_RANGE * getResources().getDisplayMetrics().density;
			mMarginTop = 16 * getResources().getDisplayMetrics().density;
		}

		public void measure() {
			mMidY = (int) ((mReceiptHeight + mMarginTop) / 2);
			mMaxY = mReceiptHeight - mReceiptMiniHeight + (int) mMarginTop;
		}

		public int getMaxY() {
			return mMaxY;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mGestureDetector.onTouchEvent(event)) {
				return false;
			}

			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				mTouchDown = true;
			}
			else if (event.getAction() == MotionEvent.ACTION_UP) {
				mTouchDown = false;

				if (mScrollY < mMidY) {
					endCheckout();
				}
				else if (mScrollY >= mMidY && mScrollY < mMaxY) {
					startCheckout();
				}
				else {
					Log.t("midY: %d - maxY: %d - scrollY: %d", mMidY, mMaxY, mScrollY);
				}
			}

			return false;
		}

		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			mScrollY = y;

			final float alpha = ((float) y - ((mHotelReceipt.getHeight() + mMarginTop - mScaledFadeRange) / 2))
					/ mScaledFadeRange;

			ViewHelper.setAlpha(mCheckoutLayout, alpha);

			mHotelReceiptMini.setVisibility(y >= mMaxY ? View.VISIBLE : View.GONE);

			// If we've lifted our finger that means the scroll view is scrolling
			// with the remaining momentum. If it's scrolling down, and it's gone
			// past the checkout state, stop it at the checkout position.
			if (!mTouchDown && y <= oldy && oldy >= mMaxY && mInCheckout) {
				mScrollView.scrollTo(0, (int) mMaxY);

				mHotelReceipt.showMiniDetailsLayout();
				mHotelReceiptMini.showMiniDetailsLayout();

				return;
			}

			if (y > mMaxY - mScaledFadeRange) {
				mHotelReceipt.showMiniDetailsLayout();
				mHotelReceiptMini.showMiniDetailsLayout();
			}
			else {
				mHotelReceipt.showTotalCostLayout();
				mHotelReceiptMini.showTotalCostLayout();
			}
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) {
				return false;
			}

			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				if (mScrollY < mMaxY) {
					startCheckout();
					return true;
				}
			}
			else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				if (mScrollY < mMaxY) {
					endCheckout();
					return true;
				}
			}

			return false;
		}

		@Override
		public void onReceiptSizeChanged(int w, int h, int oldw, int oldh) {
			mReceiptHeight = h;
			measure();
		}

		@Override
		public void onMiniReceiptSizeChanged(int w, int h, int oldw, int oldh) {
			mReceiptMiniHeight = h;
			measure();
		}
	}
}