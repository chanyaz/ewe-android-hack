package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelBookingActivity;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.CouponCodeWidget;
import com.expedia.bookings.widget.HotelReceipt;
import com.expedia.bookings.widget.HotelReceiptMini;
import com.expedia.bookings.widget.LinearLayout;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;
import com.nineoldandroids.view.ViewHelper;

public class BookingOverviewFragment extends Fragment implements AccountButtonClickListener {
	public interface BookingOverviewFragmentListener {
		public void checkoutStarted();

		public void checkoutEnded();
	}

	private static final String INSTANCE_REFRESHED_USER = "INSTANCE_REFRESHED_USER";
	private static final String INSTANCE_IN_CHECKOUT = "INSTANCE_IN_CHECKOUT";
	private static final String INSTANCE_SHOW_SLIDE_TO_WIDGET = "INSTANCE_SHOW_SLIDE_TO_WIDGET";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	private boolean mInCheckout = false;
	private BookingOverviewFragmentListener mBookingOverviewFragmentListener;

	private BillingInfo mBillingInfo;

	private ScrollView mScrollView;
	private ScrollViewListener mScrollViewListener;

	private HotelReceipt mHotelReceipt;
	private LinearLayout mCheckoutLayout;

	private AccountButton mAccountButton;
	private SectionTravelerInfo mTravelerSection;
	private SectionBillingInfo mCreditCardSectionButton;
	private SectionStoredCreditCard mStoredCreditCard;

	private ViewGroup mTravelerButton;
	private ViewGroup mPaymentButton;

	private CouponCodeWidget mCouponCodeWidget;
	private View mCouponCodeLayout;
	private View mCouponCodeEditText;
	private TextView mLegalInformationTextView;
	private View mScrollSpacerView;

	private LinearLayout mSlideToPurchaseLayout;

	private boolean mShowSlideToWidget;
	private SlideToWidget mSlideToPurchaseWidget;
	private TextView mPurchaseTotalTextView;

	private boolean mRefreshedUser;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRefreshedUser = savedInstanceState.getBoolean(INSTANCE_REFRESHED_USER);
			mInCheckout = savedInstanceState.getBoolean(INSTANCE_IN_CHECKOUT);
			mShowSlideToWidget = savedInstanceState.getBoolean(INSTANCE_SHOW_SLIDE_TO_WIDGET);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		//If we had data on disk, it should already be loaded at this point
		mBillingInfo = Db.getBillingInfo();

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mScrollView = Ui.findView(view, R.id.scroll_view);

		mHotelReceipt = Ui.findView(view, R.id.receipt);
		mCheckoutLayout = Ui.findView(view, R.id.checkout_layout);

		mAccountButton = Ui.findView(view, R.id.account_button_layout);
		mTravelerSection = Ui.findView(view, R.id.traveler_section);
		mStoredCreditCard = Ui.findView(view, R.id.stored_creditcard_section_button);
		mCreditCardSectionButton = Ui.findView(view, R.id.creditcard_section_button);

		mTravelerButton = Ui.findView(view, R.id.traveler_info_btn);
		mPaymentButton = Ui.findView(view, R.id.payment_info_btn);

		mCouponCodeWidget = new CouponCodeWidget(getActivity(), view.findViewById(R.id.coupon_code));
		mCouponCodeLayout = Ui.findView(view, R.id.coupon_code);
		mCouponCodeEditText = Ui.findView(view, R.id.coupon_code_edittext);
		mLegalInformationTextView = Ui.findView(view, R.id.legal_information_text_view);
		mScrollSpacerView = Ui.findView(view, R.id.scroll_spacer_view);

		mSlideToPurchaseLayout = Ui.findView(view, R.id.slide_to_purchase_layout);

		mSlideToPurchaseWidget = Ui.findView(view, R.id.slide_to_purchase_widget);
		mPurchaseTotalTextView = Ui.findView(view, R.id.purchase_total_text_view);

		ViewUtils.setAllCaps((TextView) Ui.findView(view, R.id.checkout_information_header_text_view));

		mScrollViewListener = new ScrollViewListener(mScrollView.getContext());

		mScrollView.setOnScrollListener(mScrollViewListener);
		mScrollView.setOnTouchListener(mScrollViewListener);
		mHotelReceipt.setOnSizeChangedListener(mScrollViewListener);
		mCheckoutLayout.setOnSizeChangedListener(mScrollViewListener);
		mSlideToPurchaseLayout.setOnSizeChangedListener(mScrollViewListener);

		mSlideToPurchaseWidget.addSlideToListener(mSlideToListener);

		ViewHelper.setAlpha(mCheckoutLayout, 0);

		mBillingInfo = Db.getBillingInfo();

		// Detect user state, update account button accordingly
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (!mRefreshedUser) {
				Log.d("Refreshing user profile...");

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(KEY_REFRESH_USER)) {
					bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
				}
			}
			mAccountButton.bind(false, true, Db.getUser());
		}
		else {
			mAccountButton.bind(false, false, null);
		}

		// restore
		mHotelReceipt.restoreInstanceState(savedInstanceState);
		mCouponCodeWidget.restoreInstanceState(savedInstanceState);

		// Listeners
		mAccountButton.setListener(this);
		mTravelerButton.setOnClickListener(mOnClickListener);
		mTravelerSection.setOnClickListener(mOnClickListener);
		mPaymentButton.setOnClickListener(mOnClickListener);
		mStoredCreditCard.setOnClickListener(mOnClickListener);
		mCreditCardSectionButton.setOnClickListener(mOnClickListener);
		mCouponCodeEditText.setOnClickListener(mOnClickListener);
		mLegalInformationTextView.setOnClickListener(mOnClickListener);

		mCouponCodeEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mScrollView.scrollTo(0, mScrollView.getHeight());
				}
			}
		});

		mCouponCodeWidget.setCouponCodeAppliedListener(new CouponCodeWidget.CouponCodeAppliedListener() {
			@Override
			public void couponCodeApplied() {
				if (isAdded()) {
					refreshData();
				}
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		OmnitureTracking.trackPageLoadHotelsRateDetails(getActivity());

		mCouponCodeWidget.startTextWatcher();
		if (mSlideToPurchaseWidget != null) {
			mSlideToPurchaseWidget.resetSlider();
		}

		refreshData();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_REFRESH_USER);
		}
		else {
			bd.unregisterDownloadCallback(KEY_REFRESH_USER);
		}

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

		outState.putBoolean(INSTANCE_REFRESHED_USER, mRefreshedUser);
		outState.putBoolean(INSTANCE_IN_CHECKOUT, mInCheckout);
		outState.putBoolean(INSTANCE_SHOW_SLIDE_TO_WIDGET, mShowSlideToWidget);

		mHotelReceipt.saveInstanceState(outState);
		mCouponCodeWidget.saveInstanceState(outState);
	}

	// Public methods

	public void refreshData() {
		mBillingInfo = Db.getBillingInfo();

		//Set values
		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();

		bindAll();
		updateViews();
		updateViewVisibilities();
	}

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

	private boolean hasSomeManuallyEnteredData(BillingInfo info) {
		if (info == null) {
			return false;
		}

		if (info.getLocation() == null) {
			return false;
		}
		//Checkout the major fields, if any of them have data, then we know some data has been manually enetered
		if (!TextUtils.isEmpty(info.getLocation().getStreetAddressString())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getCity())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getPostalCode())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getStateCode())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getNameOnCard())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getNumber())) {
			return true;
		}
		return false;
	}

	private boolean hasValidTravler() {
		boolean travelerValid = true;
		if (Db.getTravelers() == null || Db.getTravelers().size() <= 0) {
			travelerValid = false;
		}
		else {
			HotelTravelerFlowState state = HotelTravelerFlowState.getInstance(getActivity());
			if (state == null) {
				return false;
			}
			List<Traveler> travelers = Db.getTravelers();
			for (int i = 0; i < travelers.size(); i++) {
				travelerValid &= (state.hasValidTraveler(travelers.get(i)));
			}
		}
		return travelerValid;
	}

	private void populatePaymentDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate Credit Card only if the user doesn't have any manually entered (or selected) data
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() == 1
					&& !hasSomeManuallyEnteredData(mBillingInfo) && mBillingInfo.getStoredCard() == null) {
				mBillingInfo.setStoredCard(Db.getUser().getStoredCreditCards().get(0));
			}
		}
		else {
			//Remove stored card(s)
			Db.getBillingInfo().setStoredCard(null);
			//Turn off the save to expedia account flag
			Db.getBillingInfo().setSaveCardToExpediaAccount(false);
		}
	}

	private void populateTravelerDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate traveler data
			if (Db.getTravelers() != null && Db.getTravelers().size() >= 1) {
				//If the first traveler is not already all the way filled out, and the default profile for the expedia account has all required data, use the account profile
				HotelTravelerFlowState state = HotelTravelerFlowState.getInstance(getActivity());
				if (!state.hasValidTraveler(Db.getTravelers().get(0))) {
					if (state.hasValidTraveler(Db.getUser().getPrimaryTraveler())) {
						Db.getTravelers().set(0, Db.getUser().getPrimaryTraveler());
					}
				}
			}
		}
		else {
			for (int i = 0; i < Db.getTravelers().size(); i++) {
				//Travelers that have tuids are from the account and thus should be removed.
				if (Db.getTravelers().get(i).hasTuid()) {
					Db.getTravelers().set(i, new Traveler());
				}
				//We can't save travelers to an account if we aren't logged in, so we unset the flag
				Db.getTravelers().get(i).setSaveTravelerToExpediaAccount(false);
			}
		}
	}

	// View binding stuff

	private void bindAll() {
		if (Db.getTravelers() != null && Db.getTravelers().size() > 0) {
			mTravelerSection.bind(Db.getTravelers().get(0));
		}

		mStoredCreditCard.bind(mBillingInfo.getStoredCard());
		mCreditCardSectionButton.bind(mBillingInfo);

		updateViewVisibilities();
	}

	public void updateViews() {
		mLegalInformationTextView.setText(Html.fromHtml(getString(R.string.fare_rules_link)));

		Rate rate = Db.getSelectedRate();
		Rate discountRate = null;

		if (Db.getCreateTripResponse() != null) {
			discountRate = Db.getCreateTripResponse().getNewRate();
		}

		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (discountRate != null) {
			rate = discountRate;
		}

		// Purchase total
		Money displayedTotal;
		if (LocaleUtils.shouldDisplayMandatoryFees(getActivity())) {
			displayedTotal = rate.getTotalPriceWithMandatoryFees();
		}
		else {
			displayedTotal = rate.getTotalAmountAfterTax();
		}

		if (Db.getSelectedProperty().isMerchant()) {
			mPurchaseTotalTextView.setText(getString(R.string.your_card_will_be_charged_TEMPLATE,
					displayedTotal.getFormattedMoney()));
		}
		else {
			mPurchaseTotalTextView.setText(getString(R.string.collected_by_the_hotel_TEMPLATE,
					displayedTotal.getFormattedMoney()));
		}

		mHotelReceipt.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate(), discountRate);
	}

	public void updateViewVisibilities() {
		PaymentFlowState state = PaymentFlowState.getInstance(getActivity());
		if (state == null) {
			//This is a rare case that happens when the fragment is attached and then detached quickly
			return;
		}

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : state.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : state.hasValidCardInfo(mBillingInfo);
		boolean travelerValid = hasValidTravler();

		mShowSlideToWidget = travelerValid && paymentAddressValid && paymentCCValid;
		if (getInCheckout() && mShowSlideToWidget) {
			showPurchaseViews();
		}
		else {
			hidePurchaseViews();
		}

		if (mShowSlideToWidget) {
			mCouponCodeLayout.setVisibility(View.VISIBLE);
			mLegalInformationTextView.setVisibility(View.VISIBLE);
		}
		else {
			mCouponCodeLayout.setVisibility(View.INVISIBLE);
			mLegalInformationTextView.setVisibility(View.INVISIBLE);
		}

		if (travelerValid) {
			mTravelerButton.setVisibility(View.GONE);
			mTravelerSection.setVisibility(View.VISIBLE);
		}
		else {
			mTravelerButton.setVisibility(View.VISIBLE);
			mTravelerSection.setVisibility(View.GONE);
		}

		if (hasStoredCard) {
			mStoredCreditCard.setVisibility(View.VISIBLE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}
		else if (paymentAddressValid && paymentCCValid) {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
		}
		else {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}
	}

	public void setScrollSpacerViewHeight() {
		int height = 0;

		final int scrollViewHeight = mScrollView.getHeight();
		final int receiptMiniHeight = mScrollViewListener.getReceiptMiniHeight();
		final int checkoutLayoutHeight = mCheckoutLayout.getHeight();
		final int slideToPurchaseLayoutHeight = mSlideToPurchaseLayout.getHeight();

		final boolean viewsInflated = scrollViewHeight > 0 && receiptMiniHeight > 0 && checkoutLayoutHeight > 0
				&& slideToPurchaseLayoutHeight > 0;

		if (getInCheckout() && mShowSlideToWidget) {
			final int paddingBottom = (int) (getResources().getDisplayMetrics().density * 16f);
			height = slideToPurchaseLayoutHeight + paddingBottom;
		}
		else {
			final int paddingBottom = (int) (getResources().getDisplayMetrics().density * 8f);
			height = scrollViewHeight - checkoutLayoutHeight - receiptMiniHeight - paddingBottom;
		}

		if (height < 0 || !viewsInflated) {
			height = 0;
		}

		Log.t("SpacerView height: %d", height);

		ViewGroup.LayoutParams lp = mScrollSpacerView.getLayoutParams();
		lp.height = height;

		mScrollSpacerView.setLayoutParams(lp);
	}

	public boolean getInCheckout() {
		return mInCheckout;
	}

	public void setInCheckout(boolean inCheckout) {
		if (mBookingOverviewFragmentListener != null) {
			if (inCheckout && !mInCheckout) {
				mBookingOverviewFragmentListener.checkoutStarted();
			}
			else if (!inCheckout && mInCheckout) {
				mBookingOverviewFragmentListener.checkoutEnded();
			}
		}

		mInCheckout = inCheckout;
	}

	public void startCheckout() {
		startCheckout(true);
	}

	public void startCheckout(final boolean animate) {
		if (!getInCheckout()) {
			OmnitureTracking.trackPageLoadHotelsCheckoutInfo(getActivity());
		}

		setInCheckout(true);
		setScrollSpacerViewHeight();

		// Scroll to checkout
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(0, mScrollViewListener.getScrollY());

				if (animate) {
					mScrollView.smoothScrollTo(0, mScrollViewListener.getCheckoutY());
				}
				else {
					mScrollView.scrollTo(0, mScrollViewListener.getCheckoutY());
				}
			}
		});

		if (mShowSlideToWidget) {
			showPurchaseViews(animate);
		}
	}

	public void endCheckout() {
		if (getInCheckout()) {
			OmnitureTracking.trackPageLoadHotelsRateDetails(getActivity());
		}

		setInCheckout(false);
		setScrollSpacerViewHeight();

		// Scroll to start
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(0, mScrollViewListener.getScrollY());
				mScrollView.smoothScrollTo(0, 0);
			}
		});
		hidePurchaseViews();
	}

	// Hide/show slide to purchase view

	private void showPurchaseViews() {
		showPurchaseViews(true);
	}

	private void showPurchaseViews(boolean animate) {
		if (mSlideToPurchaseLayout.getVisibility() == View.VISIBLE) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				OmnitureTracking.trackPageLoadHotelsCheckoutSlideToPurchase(getActivity());
			}
		}).start();

		mSlideToPurchaseLayout.setVisibility(View.VISIBLE);
		setScrollSpacerViewHeight();

		if (animate) {
			mSlideToPurchaseLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up));
		}
	}

	private void hidePurchaseViews() {
		if (mSlideToPurchaseLayout.getVisibility() != View.VISIBLE) {
			return;
		}

		Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSlideToPurchaseLayout.setVisibility(View.INVISIBLE);
			}
		});

		setScrollSpacerViewHeight();
		mSlideToPurchaseLayout.startAnimation(animation);
	}

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {
		SignInFragment.newInstance(false).show(getFragmentManager(), getString(R.string.tag_signin));
		OmnitureTracking.trackPageLoadHotelsLogin(getActivity());
	}

	@Override
	public void accountLogoutClicked() {
		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUser = false;

		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null);

		//After logout this will clear stored cards
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();

		bindAll();
		updateViews();
		updateViewVisibilities();
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser());
		mRefreshedUser = true;

		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();

		bindAll();
		updateViews();
		updateViewVisibilities();
	}

	//////////////////////////////////////////////////////////////////////////
	// Refresh user

	private final Download<SignInResponse> mRefreshUserDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REFRESH_USER, services);
			return services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mRefreshUserCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			if (results == null || results.hasErrors()) {
				//The refresh failed, so we just log them out. They can always try to login again.
				accountLogoutClicked();
			}
			else {
				// Update our existing saved data
				User user = results.getUser();
				user.save(getActivity());
				Db.setUser(user);

				// Act as if a login just occurred
				onLoginCompleted();
			}
		}
	};

	// Listeners

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.traveler_info_btn:
			case R.id.traveler_section: {
				if (Db.getTravelers().size() > 0 && Db.getTravelers().get(0) != null) {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(0));
				}
				else {
					Db.getWorkingTravelerManager().setWorkingTravelerAndBase(new Traveler());
				}
				Db.getWorkingTravelerManager().setAttemptToLoadFromDisk(true);
				startActivity(new Intent(getActivity(), HotelTravelerInfoOptionsActivity.class));
				break;
			}
			case R.id.payment_info_btn:
			case R.id.stored_creditcard_section_button:
			case R.id.creditcard_section_button: {
				Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(mBillingInfo);
				startActivity(new Intent(getActivity(), HotelPaymentOptionsActivity.class));
				break;
			}
			case R.id.coupon_code_edittext: {
				mScrollView.scrollTo(0, mScrollView.getHeight());
				break;
			}
			case R.id.legal_information_text_view: {
				Intent intent = new Intent(getActivity(), HotelRulesActivity.class);
				startActivity(intent);
				break;
			}
			}
		}
	};

	private SlideToWidget.ISlideToListener mSlideToListener = new ISlideToListener() {
		@Override
		public void onSlideStart() {
		}

		@Override
		public void onSlideAllTheWay() {
			Db.getBillingInfo().save(getActivity());
			startActivity(new Intent(getActivity(), HotelBookingActivity.class));
		}

		@Override
		public void onSlideAbort() {
		}
	};

	// Scroll Listener

	private class ScrollViewListener extends GestureDetector.SimpleOnGestureListener implements OnScrollListener,
			OnTouchListener, HotelReceipt.OnSizeChangedListener, HotelReceiptMini.OnSizeChangedListener,
			LinearLayout.OnSizeChangedListener {

		private static final float FADE_RANGE = 100.0f;

		private static final int SWIPE_MIN_DISTANCE = 100;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		private GestureDetector mGestureDetector;

		private final float mScaledFadeRange;
		private final float mMarginTop;
		private final float mReceiptPaddingBottom;

		private boolean mTouchDown = false;
		private int mScrollY = 0;

		private int mReceiptHeight;
		private int mReceiptMiniHeight;

		private int mMidY;
		private int mCheckoutY;

		public ScrollViewListener(Context context) {
			final float density = getResources().getDisplayMetrics().density;

			mGestureDetector = new GestureDetector(context, this);
			mScaledFadeRange = FADE_RANGE * density;
			mMarginTop = 16 * density;
			mReceiptPaddingBottom = 8 * density;
		}

		public void measure() {
			mMidY = (int) ((mReceiptHeight + mMarginTop) / 2);
			mCheckoutY = mReceiptHeight - mReceiptMiniHeight + (int) mMarginTop - (int) mReceiptPaddingBottom;
		}

		public int getCheckoutY() {
			return mCheckoutY;
		}

		public int getScrollY() {
			return mScrollY;
		}

		public int getReceiptMiniHeight() {
			return mReceiptMiniHeight;
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
				else if (mScrollY >= mMidY && mScrollY <= mCheckoutY) {
					startCheckout();
				}
			}

			return false;
		}

		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			mScrollY = y;

			float alpha = ((float) y - ((mHotelReceipt.getHeight() + mMarginTop - mScaledFadeRange) / 2))
					/ mScaledFadeRange;

			if (alpha < 0) {
				alpha = 0;
			}
			else if (alpha > 100) {
				alpha = 100;
			}

			ViewHelper.setAlpha(mCheckoutLayout, alpha);

			// If we've lifted our finger that means the scroll view is scrolling
			// with the remaining momentum. If it's scrolling down, and it's gone
			// past the checkout state, stop it at the checkout position.
			if (!mTouchDown && y <= oldy && oldy >= mCheckoutY && getInCheckout()) {
				mScrollView.scrollTo(0, (int) mCheckoutY);
				mHotelReceipt.showMiniDetailsLayout();

				return;
			}

			if (y > mCheckoutY - mScaledFadeRange) {
				mHotelReceipt.showMiniDetailsLayout();
			}
			else {
				mHotelReceipt.showTotalCostLayout();
			}
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (e1 == null || e2 == null) {
				return false;
			}

			if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) {
				return false;
			}

			if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				if (mScrollY < mCheckoutY) {
					startCheckout();
					return true;
				}
			}
			else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				if (mScrollY < mCheckoutY) {
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

			if (getInCheckout()) {
				startCheckout(false);
			}
		}

		@Override
		public void onMiniReceiptSizeChanged(int w, int h, int oldw, int oldh) {
			mReceiptMiniHeight = h;
			measure();
			setScrollSpacerViewHeight();
		}

		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			setScrollSpacerViewHeight();
		}
	}
}