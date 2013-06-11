package com.expedia.bookings.fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.HotelPriceChangeDialog;
import com.expedia.bookings.dialog.HotelRateBreakdownDialog;
import com.expedia.bookings.dialog.TextViewDialog;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.CouponCodeWidget;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelReceipt;
import com.expedia.bookings.widget.LinearLayout;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.expedia.bookings.widget.WalletButton;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;
import com.nineoldandroids.view.ViewHelper;

public class BookingOverviewFragment extends LoadWalletFragment implements AccountButtonClickListener {

	public interface BookingOverviewFragmentListener {
		public void checkoutStarted();

		public void checkoutEnded();
	}

	public static final String TAG_SLIDE_TO_PURCHASE_FRAG = "TAG_SLIDE_TO_PURCHASE_FRAG";

	private static final String INSTANCE_REFRESHED_USER = "INSTANCE_REFRESHED_USER";
	private static final String INSTANCE_IN_CHECKOUT = "INSTANCE_IN_CHECKOUT";
	private static final String INSTANCE_SHOW_SLIDE_TO_WIDGET = "INSTANCE_SHOW_SLIDE_TO_WIDGET";
	private static final String INSTANCE_DONE_LOADING_PRICE_CHANGE = "INSTANCE_DONE_LOADING_PRICE_CHANGE";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";
	private static final String KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE = "KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE";

	private boolean mInCheckout = false;
	private BookingOverviewFragmentListener mBookingOverviewFragmentListener;

	private BillingInfo mBillingInfo;

	private ScrollView mScrollView;
	private ScrollViewListener mScrollViewListener;

	private HotelReceipt mHotelReceipt;
	private LinearLayout mCheckoutLayout;

	private AccountButton mAccountButton;
	private WalletButton mWalletButton;
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

	private FrameLayout mSlideToPurchaseFragmentLayout;

	private boolean mShowSlideToWidget;
	private String mSlideToPurchasePriceString;
	private SlideToPurchaseFragment mSlideToPurchaseFragment;

	private boolean mRefreshedUser;
	private boolean mIsDoneLoadingPriceChange = false;

	// We keep track of if we need to maintain the scroll position
	// This is needed when we call startCheckout before a layout occurs
	// typically on rotation
	private boolean mMaintainStartCheckoutPosition;

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
			mIsDoneLoadingPriceChange = savedInstanceState.getBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE);
		}
		else {
			// Reset Google Wallet state each time we get here
			Db.clearGoogleWallet();
		}

		AdTracker.trackHotelCheckoutStarted();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		mScrollView = Ui.findView(view, R.id.scroll_view);

		mHotelReceipt = Ui.findView(view, R.id.receipt);
		mCheckoutLayout = Ui.findView(view, R.id.checkout_layout);

		mAccountButton = Ui.findView(view, R.id.account_button_layout);
		mWalletButton = Ui.findView(view, R.id.wallet_button_layout);
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

		mSlideToPurchaseFragmentLayout = Ui.findView(view, R.id.slide_to_purchase_fragment_layout);

		ViewUtils.setAllCaps((TextView) Ui.findView(view, R.id.checkout_information_header_text_view));

		mScrollViewListener = new ScrollViewListener(mScrollView.getContext());

		mScrollView.addOnScrollListener(mScrollViewListener);
		mScrollView.setOnTouchListener(mScrollViewListener);
		mHotelReceipt.setOnSizeChangedListener(mScrollViewListener);
		mCheckoutLayout.setOnSizeChangedListener(mScrollViewListener);
		mSlideToPurchaseFragmentLayout.setOnSizeChangedListener(mScrollViewListener);

		if (mSlideToPurchaseFragment == null) {
			FragmentManager manager = getChildFragmentManager();
			mSlideToPurchaseFragment = (SlideToPurchaseFragment) manager.findFragmentByTag(TAG_SLIDE_TO_PURCHASE_FRAG);
			if (mSlideToPurchaseFragment == null) {
				mSlideToPurchaseFragment = SlideToPurchaseFragment.newInstance(mSlideToPurchasePriceString);
			}

			if (!mSlideToPurchaseFragment.isAdded()) {
				FragmentTransaction transaction = manager.beginTransaction();
				transaction.replace(R.id.slide_to_purchase_fragment_layout, mSlideToPurchaseFragment,
						TAG_SLIDE_TO_PURCHASE_FRAG);
				transaction.setCustomAnimations(0, 0);
				transaction.commit();
			}
		}

		ViewHelper.setAlpha(mCheckoutLayout, 0);

		//We start loading the checkout data on the parent activity, but if it isn't finished we should wait
		if (CheckoutDataLoader.getInstance().isLoading()) {
			CheckoutDataLoader.getInstance().waitForCurrentThreadToFinish();
		}

		mBillingInfo = Db.getBillingInfo();
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

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
		mWalletButton.setOnClickListener(mWalletButtonClickListener);
		mTravelerButton.setOnClickListener(mOnClickListener);
		mTravelerSection.setOnClickListener(mOnClickListener);
		mPaymentButton.setOnClickListener(mOnClickListener);
		mStoredCreditCard.setOnClickListener(mOnClickListener);
		mCreditCardSectionButton.setOnClickListener(mOnClickListener);
		mCouponCodeEditText.setOnClickListener(mOnClickListener);
		mLegalInformationTextView.setOnClickListener(mOnClickListener);
		mHotelReceipt.setRateBreakdownClickListener(mRateBreakdownClickListener);

		mCouponCodeEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
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

		refreshData();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		if (bd.isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE)) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductCallback);
		}
		else if (!mIsDoneLoadingPriceChange) {
			bd.startDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductDownload, mHotelProductCallback);
		}

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
			bd.cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
		else {
			bd.unregisterDownloadCallback(KEY_REFRESH_USER);
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
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
		outState.putBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE, mIsDoneLoadingPriceChange);

		mHotelReceipt.saveInstanceState(outState);
		mCouponCodeWidget.saveInstanceState(outState);
	}

	// Public methods

	public void refreshData() {
		mBillingInfo = Db.getBillingInfo();

		loadUser();

		//Set values
		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();

		bindAll();
		refreshAccountButtonState();
		updateViews();
		updateViewVisibilities();
	}

	private void loadUser() {
		if (Db.getUser() == null) {
			if (User.isLoggedIn(getActivity())) {
				Db.loadUser(getActivity());
			}
		}
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

	private boolean validateTravelers() {
		boolean allTravelersValid = true;
		if (Db.getTravelers() == null || Db.getTravelers().size() <= 0) {
			allTravelersValid = false;
		}
		else {
			HotelTravelerFlowState state = HotelTravelerFlowState.getInstance(getActivity());
			if (state == null) {
				return false;
			}
			List<Traveler> travelers = Db.getTravelers();
			for (int i = 0; i < travelers.size(); i++) {
				allTravelersValid &= (state.hasValidTraveler(travelers.get(i)));
			}
		}
		return allTravelersValid;
	}

	private void setValidationViewVisibility(View view, int validationViewId, boolean valid) {
		View validationView = Ui.findView(view, validationViewId);
		if (validationView != null) {
			validationView.setVisibility(valid ? View.VISIBLE : View.GONE);
		}
	}

	private void populatePaymentDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate Credit Card only if the user doesn't have any manually entered (or selected) data
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() == 1
					&& !hasSomeManuallyEnteredData(mBillingInfo) && mBillingInfo.getStoredCard() == null) {
				mBillingInfo.setStoredCard(Db.getUser().getStoredCreditCards().get(0));
			}
		}
		else if (Db.getMaskedWallet() == null) {
			//Remove stored card(s)
			Db.getBillingInfo().setStoredCard(null);
			//Turn off the save to expedia account flag
			Db.getBillingInfo().setSaveCardToExpediaAccount(false);
		}
	}

	private void populateTravelerDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate traveler data
			BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), Db.getUser().getPrimaryTraveler(),
					LineOfBusiness.HOTELS);
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

	private void refreshAccountButtonState() {
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
					&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
				//We have a user (either from memory, or loaded from disk)
				if (!mRefreshedUser) {
					Log.d("Refreshing user profile...");

					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(KEY_REFRESH_USER)) {
						bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
					}
				}
				mAccountButton.bind(false, true, Db.getUser(), false);
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, false);
			}
		}
		else {
			mAccountButton.bind(false, false, null, false);
		}
	}

	public void updateViews() {
		mLegalInformationTextView.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());

		final String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
		Rate rate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();

		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (Db.getCreateTripResponse() != null) {
			rate = Db.getCreateTripResponse().getNewRate();
		}

		// Configure slide to purchase string
		if (!Db.getHotelSearch().getSelectedProperty().isMerchant()) {
			mCouponCodeLayout.setVisibility(View.GONE);
			mSlideToPurchasePriceString = getString(R.string.collected_by_the_hotel_TEMPLATE,
					rate.getTotalPriceWithMandatoryFees().getFormattedMoney());
		}
		else if (PointOfSale.getPointOfSale().displayMandatoryFees()) {
			mSlideToPurchasePriceString = getString(R.string.Amount_to_be_paid_now_TEMPLATE,
					rate.getTotalAmountAfterTax().getFormattedMoney());
		}
		else {
			mSlideToPurchasePriceString = getString(R.string.your_card_will_be_charged_TEMPLATE,
					rate.getTotalAmountAfterTax().getFormattedMoney());
		}
		mSlideToPurchaseFragment.setTotalPriceString(mSlideToPurchasePriceString);

		mHotelReceipt.bind(mIsDoneLoadingPriceChange, Db.getHotelSearch().getSelectedProperty(), Db.getHotelSearch().getSearchParams(),
				rate);
	}

	public void updateViewVisibilities() {
		HotelPaymentFlowState state = HotelPaymentFlowState.getInstance(getActivity());
		if (state == null) {
			//This is a rare case that happens when the fragment is attached and then detached quickly
			return;
		}

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : state.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : state.hasValidCardInfo(mBillingInfo);
		boolean travelerValid = validateTravelers();

		mShowSlideToWidget = travelerValid && paymentAddressValid && paymentCCValid && mIsDoneLoadingPriceChange;
		if (isInCheckout() && mShowSlideToWidget) {
			showPurchaseViews();
		}
		else {
			hidePurchaseViews();
		}

		if (mInCheckout) {
			if (Db.getHotelSearch().getSelectedProperty().isMerchant()) {
				mCouponCodeLayout.setVisibility(View.VISIBLE);
			}
			else {
				mCouponCodeLayout.setVisibility(View.GONE);
			}
			mLegalInformationTextView.setVisibility(View.VISIBLE);
		}
		else {
			mCouponCodeLayout.setVisibility(View.INVISIBLE);
			mLegalInformationTextView.setVisibility(View.INVISIBLE);
		}

		if (travelerValid) {
			mTravelerButton.setVisibility(View.GONE);
			mTravelerSection.setVisibility(View.VISIBLE);
			setValidationViewVisibility(mTravelerSection, R.id.validation_checkmark, true);
		}
		else {
			mTravelerButton.setVisibility(View.VISIBLE);
			mTravelerSection.setVisibility(View.GONE);
		}

		if (hasStoredCard) {
			mStoredCreditCard.setVisibility(View.VISIBLE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.GONE);
			setValidationViewVisibility(mStoredCreditCard, R.id.validation_checkmark, true);
		}
		else if (paymentAddressValid && paymentCCValid) {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			setValidationViewVisibility(mCreditCardSectionButton, R.id.validation_checkmark, true);
		}
		else {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}

		updateWalletViewVisibilities();
	}

	public void setScrollSpacerViewHeight() {
		int height = 0;
		final int scrollViewHeight = mScrollView.getHeight();
		final int receiptMiniHeight = mScrollViewListener.getReceiptMiniHeight();
		final int checkoutLayoutHeight = mCheckoutLayout.getHeight();
		final int slideToPurchaseFragmentHeight = mSlideToPurchaseFragmentLayout.getHeight();

		final boolean viewsInflated = scrollViewHeight > 0 && receiptMiniHeight > 0 && checkoutLayoutHeight > 0
				&& slideToPurchaseFragmentHeight > 0;

		if (isInCheckout() && mShowSlideToWidget) {
			final int screenHeight = AndroidUtils.getScreenSize(getActivity()).y;
			final int actionBarHeight = ((SherlockFragmentActivity) getActivity()).getSupportActionBar().getHeight();

			// We compute this based on screenHeight incase all of the content fits on the screen
			// For example on a large tablet
			height = screenHeight + actionBarHeight - checkoutLayoutHeight - receiptMiniHeight
					- slideToPurchaseFragmentHeight;

			if (height < slideToPurchaseFragmentHeight) {
				// This means the content fills the height of the screen and then some
				// So we just need to set the spacer so anything hiding behind the slide
				// to purchase is revealed
				final int paddingBottom = (int) (getResources().getDisplayMetrics().density * 16f);
				height = slideToPurchaseFragmentHeight + paddingBottom;
			}
		}
		else {
			final int paddingBottom = (int) (getResources().getDisplayMetrics().density * 8f);
			height = scrollViewHeight - checkoutLayoutHeight - receiptMiniHeight - paddingBottom;
		}

		if (height < 0 || !viewsInflated) {
			height = 0;
		}

		final int finalHeight = height;
		ViewGroup.LayoutParams lp = mScrollSpacerView.getLayoutParams();
		final int initialHeight = lp.height;
		if (initialHeight != finalHeight) {
			lp.height = finalHeight;
			mScrollSpacerView.setLayoutParams(lp);
			mScrollView.post(new Runnable() {
				@Override
				public void run() {
					mScrollView.requestLayout();
					if (mMaintainStartCheckoutPosition) {
						// Now we have to wire this up so we can scroll the page after a layout occurs
						mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
							@Override
							public void onGlobalLayout() {
								scrollToCheckout(false);
								mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
							}
						});
					}
				}
			});
		}
	}

	public boolean isInCheckout() {
		return mInCheckout;
	}

	public void setInCheckout(boolean inCheckout) {
		// #1111: Fixed timing issue with ABS in compat mode; when in compat
		// mode, it fires the action bar update immediately, so the state var
		// needs to be updated before we fire supportInvalidateOptionsMenu()
		boolean wasInCheckout = mInCheckout;
		mInCheckout = inCheckout;

		if (mBookingOverviewFragmentListener != null) {
			if (inCheckout && !wasInCheckout) {
				mBookingOverviewFragmentListener.checkoutStarted();
			}
			else if (!inCheckout && wasInCheckout) {
				mBookingOverviewFragmentListener.checkoutEnded();
			}
		}
	}

	public void startCheckout() {
		startCheckout(true, true);
	}

	private void scrollToCheckout(final boolean animate) {
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
	}

	public void startCheckout(final boolean animate, boolean shouldScrollToCheckout) {
		if (!isInCheckout()) {
			OmnitureTracking.trackPageLoadHotelsCheckoutInfo(getActivity());
		}

		mMaintainStartCheckoutPosition = true;
		setInCheckout(true);
		setScrollSpacerViewHeight();

		// Scroll to checkout
		if (shouldScrollToCheckout) {
			scrollToCheckout(animate);
		}

		if (mShowSlideToWidget) {
			showPurchaseViews(animate);
		}

		updateViewVisibilities();
	}

	public void endCheckout() {
		if (isInCheckout()) {
			OmnitureTracking.trackPageLoadHotelsRateDetails(getActivity());
		}

		mMaintainStartCheckoutPosition = false;

		setInCheckout(false);
		setScrollSpacerViewHeight();

		// Scroll to start
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(0, mScrollViewListener.getScrollY());
				mScrollView.smoothScrollTo(0, 0);
				if (isAdded() && mCouponCodeEditText != null) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mCouponCodeEditText.getWindowToken(), 0);
				}
			}
		});
		hidePurchaseViews();
	}

	// Hide/show slide to purchase view

	private void showPurchaseViews() {
		showPurchaseViews(true);
	}

	private void showPurchaseViews(boolean animate) {
		if (mSlideToPurchaseFragmentLayout.getVisibility() == View.VISIBLE) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				OmnitureTracking.trackPageLoadHotelsCheckoutSlideToPurchase(getActivity());
			}
		}).start();

		mSlideToPurchaseFragmentLayout.setVisibility(View.VISIBLE);
		setScrollSpacerViewHeight();

		if (animate) {
			mSlideToPurchaseFragmentLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up));
		}
	}

	private void hidePurchaseViews() {
		if (mSlideToPurchaseFragmentLayout.getVisibility() != View.VISIBLE) {
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
				mSlideToPurchaseFragmentLayout.setVisibility(View.INVISIBLE);
			}
		});

		setScrollSpacerViewHeight();
		mSlideToPurchaseFragmentLayout.startAnimation(animation);
	}

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {
		Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
		loginIntent.putExtra(LoginActivity.ARG_PATH_MODE, LineOfBusiness.HOTELS.name());
		startActivity(loginIntent);
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

	// Download updated rate information

	private final Download<HotelProductResponse> mHotelProductDownload = new Download<HotelProductResponse>() {
		@Override
		public HotelProductResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, services);
			String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
			Rate selectedRate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();
			return services.hotelProduct(Db.getHotelSearch().getSearchParams(), Db.getHotelSearch().getSelectedProperty(), selectedRate);
		}
	};

	private final OnDownloadComplete<HotelProductResponse> mHotelProductCallback = new OnDownloadComplete<HotelProductResponse>() {
		@Override
		public void onDownload(HotelProductResponse response) {
			if (response == null || response.hasErrors()) {
				handleHotelProductError(response);
			}
			else {
				final String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
				Rate selectedRate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();
				Rate newRate = response.getRate();

				if (TextUtils.equals(selectedRate.getRateKey(), response.getOriginalProductKey())) {
					if (!AndroidUtils.isRelease(getActivity())) {
						String val = SettingUtils.get(getActivity(),
								getString(R.string.preference_fake_price_change),
								getString(R.string.preference_fake_price_change_default));

						newRate.getDisplayTotalPrice().add(new BigDecimal(val));
					}

					int priceChange = selectedRate.compareForPriceChange(newRate);
					if (priceChange != 0) {
						boolean isPriceHigher = priceChange < 0;
						HotelPriceChangeDialog dialog = HotelPriceChangeDialog.newInstance(isPriceHigher,
								selectedRate.getDisplayTotalPrice(), newRate.getDisplayTotalPrice());
						dialog.show(getFragmentManager(), "priceChangeDialog");
					}

					Db.getHotelSearch().getAvailability(selectedId).updateFrom(selectedRate.getRateKey(), response);
					Db.getHotelSearch().getAvailability(selectedId).setSelectedRate(newRate);

					mIsDoneLoadingPriceChange = true;
					mHotelReceipt.bind(mIsDoneLoadingPriceChange, Db.getHotelSearch().getSelectedProperty(),
							Db.getHotelSearch().getSearchParams(), selectedRate);
					updateViewVisibilities();
				}
				else {
					handleHotelProductError(response);
				}
			}
		}
	};

	private void handleHotelProductError(HotelProductResponse response) {
		TextViewDialog dialog = new TextViewDialog();
		boolean isUnavailable = false;
		if (response != null) {
			for (ServerError error : response.getErrors()) {
				if (error.getErrorCode() == ServerError.ErrorCode.HOTEL_ROOM_UNAVAILABLE) {
					isUnavailable = true;
					String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
					Db.getHotelSearch().getAvailability(selectedId).removeRate(response.getOriginalProductKey());
				}
			}
		}

		if (isUnavailable) {
			dialog.setMessage(R.string.e3_error_hotel_offers_hotel_room_unavailable);
		}
		else {
			dialog.setMessage(R.string.e3_error_hotel_offers_hotel_service_failure);
		}

		dialog.setOnDismissListener(new TextViewDialog.OnDismissListener() {
			@Override
			public void onDismissed() {
				getActivity().finish();
			}
		});
		dialog.show(getFragmentManager(), "hotelOfferErrorDialog");
	}

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
				Db.getWorkingTravelerManager().setAttemptToLoadFromDisk(false);
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
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
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

	private View.OnClickListener mRateBreakdownClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			HotelRateBreakdownDialog dialogFrag = new HotelRateBreakdownDialog();
			dialogFrag.show(getFragmentManager(), HotelRateBreakdownDialog.class.toString());
		}
	};

	// Scroll Listener

	private class ScrollViewListener extends GestureDetector.SimpleOnGestureListener implements OnScrollListener,
			OnTouchListener, HotelReceipt.OnSizeChangedListener, LinearLayout.OnSizeChangedListener,
			FrameLayout.OnSizeChangedListener {

		private static final float FADE_RANGE = 100.0f;

		private static final int SWIPE_MIN_DISTANCE = 100;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 200;

		private GestureDetector mGestureDetector;

		private final float mScaledFadeRange;
		private final float mMarginTop;
		private final float mReceiptPaddingBottom;

		private boolean mTouchDown = false;
		private int mScrollY;

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
					Log.t("Ending checkout - ScrollY: %d", mScrollY);
					endCheckout();
				}
				else if (mScrollY >= mMidY && mScrollY <= mCheckoutY) {
					Log.t("Starting checkout - ScrollY: %d", mScrollY);
					startCheckout();
				}
			}

			return false;
		}

		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			Log.t("ScrollY: %d", y);

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
			if (!mTouchDown && y <= oldy && oldy >= mCheckoutY && isInCheckout()) {
				mScrollView.scrollTo(0, (int) mCheckoutY);
				mMaintainStartCheckoutPosition = false;
				return;
			}
			else if (mTouchDown && y >= mCheckoutY && !isInCheckout()) {
				startCheckout(false, false);
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

			if (isInCheckout()) {
				startCheckout(false, true);
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

	//////////////////////////////////////////////////////////////////////////
	// Google Wallet

	private OnClickListener mWalletButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			buyWithGoogleWallet();
		}
	};

	@Override
	protected Money getEstimatedTotal() {
		String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
		Rate selectedRate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();
		return selectedRate.getTotalAmountAfterTax();
	}

	@Override
	protected int getMaskedWalletBuilderFlags() {
		return WalletUtils.F_PHONE_NUMBER_REQUIRED | WalletUtils.F_USE_MINIMAL_BILLING_ADDRESS;
	}

	/**
	 * Binds the masked wallet to the billing info.  Warning: it WILL
	 * blow away whatever was here before - so only call this when
	 * we want to override the current data with Google Wallet!
	 */
	protected void onMaskedWalletFullyLoaded(boolean fromPreauth) {
		populateTravelerData();

		MaskedWallet maskedWallet = Db.getMaskedWallet();

		// Add the current traveler from the wallet, if it is full of data and we have none at the moment
		Traveler traveler = WalletUtils.addWalletAsTraveler(getActivity(), maskedWallet);
		BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), traveler, LineOfBusiness.HOTELS);

		// Bind credit card data, but only if they explicitly clicked "buy with wallet" or they have
		// no existing credit card info entered
		if (!fromPreauth || (TextUtils.isEmpty(mBillingInfo.getNumber()) && mBillingInfo.getStoredCard() == null)) {
			WalletUtils.bindWalletToBillingInfo(maskedWallet, mBillingInfo);
		}

		bindAll();
		updateViews();
		updateViewVisibilities();
	}

	// We may want to update these more often than the rest of the Views
	protected void updateWalletViewVisibilities() {
		boolean showWalletButton = showWalletButton();
		boolean isWalletLoading = isWalletLoading();

		mWalletButton.setVisibility(showWalletButton ? View.VISIBLE : View.GONE);
		mWalletButton.setEnabled(!isWalletLoading);

		// Enable buttons if we're either not showing the wallet button or we're not loading a masked wallet
		boolean enableButtons = !showWalletButton || !isWalletLoading;
		mAccountButton.setEnabled(enableButtons);
		mTravelerButton.setEnabled(enableButtons);
		mTravelerSection.setEnabled(enableButtons);
		mPaymentButton.setEnabled(enableButtons);
		mStoredCreditCard.setEnabled(enableButtons);
		mCreditCardSectionButton.setEnabled(enableButtons);
		mCouponCodeEditText.setEnabled(enableButtons);
	}
}
