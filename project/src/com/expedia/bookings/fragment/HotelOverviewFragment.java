package com.expedia.bookings.fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.CheckoutPriceType;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.dialog.CouponDialogFragment;
import com.expedia.bookings.dialog.CouponDialogFragment.CouponDialogFragmentListener;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.dialog.HotelPriceChangeDialog;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.dialog.ThrobberDialog.CancelListener;
import com.expedia.bookings.fragment.SimpleCallbackDialogFragment.SimpleCallbackDialogFragmentListener;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelReceipt;
import com.expedia.bookings.widget.LinearLayout;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.expedia.bookings.widget.WalletButton;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest.Builder;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.ViewUtils;
import com.nineoldandroids.view.ViewHelper;

public class HotelOverviewFragment extends LoadWalletFragment implements AccountButtonClickListener,
		CancelListener, SimpleCallbackDialogFragmentListener, CouponDialogFragmentListener {

	public interface BookingOverviewFragmentListener {
		public void checkoutStarted();

		public void checkoutEnded();
	}

	public static final String TAG_SLIDE_TO_PURCHASE_FRAG = "TAG_SLIDE_TO_PURCHASE_FRAG";
	public static final String HOTEL_OFFER_ERROR_DIALOG = "HOTEL_OFFER_ERROR_DIALOG";

	private static final String INSTANCE_REFRESHED_USER_TIME = "INSTANCE_REFRESHED_USER";
	private static final String INSTANCE_IN_CHECKOUT = "INSTANCE_IN_CHECKOUT";
	private static final String INSTANCE_SHOW_SLIDE_TO_WIDGET = "INSTANCE_SHOW_SLIDE_TO_WIDGET";
	private static final String INSTANCE_DONE_LOADING_PRICE_CHANGE = "INSTANCE_DONE_LOADING_PRICE_CHANGE";
	private static final String INSTANCE_COUPON_CODE = "INSTANCE_COUPON_CODE";
	private static final String INSTANCE_WAS_USING_GOOGLE_WALLET = "INSTANCE_WAS_USING_GOOGLE_WALLET";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";
	private static final String KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE = "KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE";
	private static final String KEY_APPLY_COUPON = "KEY_APPLY_COUPON";
	private static final String KEY_CREATE_TRIP = "KEY_CREATE_TRIP";

	private static final int CALLBACK_WALLET_PROMO_APPLY_ERROR = 1;

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

	private String mCouponCode;
	private boolean mWasUsingGoogleWallet;
	private ThrobberDialog mWalletPromoThrobberDialog;

	private TextView mCouponButton;
	private ViewGroup mCouponAppliedContainer;
	private TextView mCouponSavedTextView;
	private View mCouponRemoveView;

	private TextView mLegalInformationTextView;
	private View mScrollSpacerView;

	private FrameLayout mSlideToPurchaseFragmentLayout;

	private boolean mShowSlideToWidget;
	private String mSlideToPurchasePriceString;
	private SlideToPurchaseFragment mSlideToPurchaseFragment;

	private boolean mIsDoneLoadingPriceChange = false;

	private CouponDialogFragment mCouponDialogFragment;
	private ThrobberDialog mCouponRemoveThrobberDialog;

	//When we last refreshed user data.
	private long mRefreshedUserTime = 0L;

	// We keep track of if we need to maintain the scroll position
	// This is needed when we call startCheckout before a layout occurs
	// typically on rotation
	private boolean mMaintainStartCheckoutPosition;

	private FragmentModificationSafeLock mFragmentModLock = new FragmentModificationSafeLock();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mBookingOverviewFragmentListener = Ui.findFragmentListener(this, BookingOverviewFragmentListener.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRefreshedUserTime = savedInstanceState.getLong(INSTANCE_REFRESHED_USER_TIME);
			mInCheckout = savedInstanceState.getBoolean(INSTANCE_IN_CHECKOUT);
			mShowSlideToWidget = savedInstanceState.getBoolean(INSTANCE_SHOW_SLIDE_TO_WIDGET);
			mIsDoneLoadingPriceChange = savedInstanceState.getBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE);
			mCouponCode = savedInstanceState.getString(INSTANCE_COUPON_CODE);
			mWasUsingGoogleWallet = savedInstanceState.getBoolean(INSTANCE_WAS_USING_GOOGLE_WALLET);
		}
		else {
			// Reset Google Wallet state each time we get here
			Db.clearGoogleWallet();
		}

		// #1715: Disable Google Wallet on non-merchant hotels
		if (!Db.getHotelSearch().getSelectedProperty().isMerchant()) {
			disableGoogleWallet();
		}

		AdTracker.trackHotelCheckoutStarted();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		mCouponDialogFragment = Ui.findSupportFragment(this, CouponDialogFragment.TAG);

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

		mCouponButton = Ui.findView(view, R.id.coupon_button);
		mCouponAppliedContainer = Ui.findView(view, R.id.coupon_applied_container);
		mCouponSavedTextView = Ui.findView(view, R.id.coupon_saved_text_view);
		mCouponRemoveView = Ui.findView(view, R.id.coupon_clear);

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

			int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval);
			if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
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

		// Listeners
		mAccountButton.setListener(this);
		mWalletButton.setOnClickListener(mWalletButtonClickListener);
		mTravelerButton.setOnClickListener(mOnClickListener);
		mTravelerSection.setOnClickListener(mOnClickListener);
		mPaymentButton.setOnClickListener(mOnClickListener);
		mStoredCreditCard.setOnClickListener(mOnClickListener);
		mCreditCardSectionButton.setOnClickListener(mOnClickListener);
		mCouponButton.setOnClickListener(mOnClickListener);
		mCouponRemoveView.setOnClickListener(mOnClickListener);
		mLegalInformationTextView.setOnClickListener(mOnClickListener);
		mHotelReceipt.setRateBreakdownClickListener(mRateBreakdownClickListener);

		mWalletButton.setPromoVisible(true);

		// We underline the coupon button text in code to avoid re-translating
		mCouponButton.setPaintFlags(mCouponButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		mWalletPromoThrobberDialog = Ui.findSupportFragment((FragmentActivity) getActivity(), ThrobberDialog.TAG);
		if (mWalletPromoThrobberDialog != null && mWalletPromoThrobberDialog.isAdded()) {
			mWalletPromoThrobberDialog.setCancelListener(this);
		}

		OmnitureTracking.trackPageLoadHotelsRateDetails(getActivity());

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		HotelErrorDialog errorDialog = (HotelErrorDialog) getFragmentManager().findFragmentByTag(
				HOTEL_OFFER_ERROR_DIALOG);
		if (errorDialog == null) {
			// When we resume, there is a possibility that:
			// 1. We were using GWallet (with coupon), but are no longer using GWallet
			// 2. We were not using GWallet, but now are doing so (and thus want to apply the GWallet code)
			boolean isUsingGoogleWallet = Db.getBillingInfo().isUsingGoogleWallet();
			final boolean isCouponAvailable = WalletUtils.offerGoogleWalletCoupon(getActivity());
			if (mWasUsingGoogleWallet && !isUsingGoogleWallet && usingWalletPromoCoupon()) {
				clearWalletPromoCoupon();
			}
			else if (!mWasUsingGoogleWallet && isUsingGoogleWallet && isCouponAvailable) {
				applyWalletCoupon();
			}

			refreshData();

			if (bd.isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE)) {
				bd.registerDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductCallback);
			}
			else if (!mIsDoneLoadingPriceChange) {
				bd.startDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductDownload, mHotelProductCallback);
			}
		}

		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}

		if (bd.isDownloading(KEY_APPLY_COUPON)) {
			bd.registerDownloadCallback(KEY_APPLY_COUPON, mCouponCallback);
		}

		if (bd.isDownloading(KEY_CREATE_TRIP)) {
			bd.registerDownloadCallback(KEY_CREATE_TRIP, mCreateTripCallback);
		}

		mFragmentModLock.setSafe(true);

		//We disable this for sign in, but when the user comes back it should be enabled.
		mAccountButton.setEnabled(true);
	}

	@Override
	public void onPause() {
		super.onPause();

		mWasUsingGoogleWallet = mBillingInfo.isUsingGoogleWallet();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_REFRESH_USER);
			bd.cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
			bd.cancelDownload(KEY_APPLY_COUPON);
			bd.cancelDownload(KEY_CREATE_TRIP);
			// Since we are exiting the screen, let's reset coupon.
			Db.getHotelSearch().setCouponApplied(false);
		}
		else {
			bd.unregisterDownloadCallback(KEY_REFRESH_USER);
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
			bd.unregisterDownloadCallback(KEY_APPLY_COUPON);
			bd.unregisterDownloadCallback(KEY_CREATE_TRIP);
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

		outState.putLong(INSTANCE_REFRESHED_USER_TIME, mRefreshedUserTime);
		outState.putBoolean(INSTANCE_IN_CHECKOUT, mInCheckout);
		outState.putBoolean(INSTANCE_SHOW_SLIDE_TO_WIDGET, mShowSlideToWidget);
		outState.putBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE, mIsDoneLoadingPriceChange);
		outState.putString(INSTANCE_COUPON_CODE, mCouponCode);
		outState.putBoolean(INSTANCE_WAS_USING_GOOGLE_WALLET, mWasUsingGoogleWallet);

		mHotelReceipt.saveInstanceState(outState);

		mFragmentModLock.setSafe(false);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mBookingOverviewFragmentListener = null; // Just in case Wallet is leaking
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

	private boolean hasValidTravelers() {
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
					&& !hasSomeManuallyEnteredData(mBillingInfo) && !mBillingInfo.hasStoredCard()) {
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
				int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval);
				if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
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
				User.signOutAsync(getActivity(), null);
				mAccountButton.bind(false, false, null, false);
			}
		}
		else {
			mAccountButton.bind(false, false, null, false);
		}
	}

	public void updateViews() {
		mLegalInformationTextView.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());

		Rate rate = Db.getHotelSearch().getSelectedRate();

		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (Db.getHotelSearch().isCouponApplied()) {
			rate = Db.getHotelSearch().getCouponRate();

			// Show off the savings!
			mCouponSavedTextView.setText(getString(R.string.coupon_saved_TEMPLATE, rate
					.getTotalPriceAdjustments().getFormattedMoney()));
		}

		// Configure slide to purchase string
		int chargeTypeMessageId = 0;
		if (!Db.getHotelSearch().getSelectedProperty().isMerchant()) {
			chargeTypeMessageId = R.string.collected_by_the_hotel_TEMPLATE;
		}
		else if (rate.getCheckoutPriceType() == CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES) {
			chargeTypeMessageId = R.string.Amount_to_be_paid_now_TEMPLATE;
		}
		else {
			chargeTypeMessageId = R.string.your_card_will_be_charged_TEMPLATE;
		}
		mSlideToPurchasePriceString = getString(chargeTypeMessageId, rate.getTotalAmountAfterTax().getFormattedMoney());
		mSlideToPurchaseFragment.setTotalPriceString(mSlideToPurchasePriceString);

		mHotelReceipt.bind(mIsDoneLoadingPriceChange, Db.getHotelSearch().getSelectedProperty(), Db.getHotelSearch()
				.getSearchParams(), rate, appliedWalletPromoCoupon());
	}

	public void updateViewVisibilities() {
		HotelPaymentFlowState state = HotelPaymentFlowState.getInstance(getActivity());
		if (state == null) {
			//This is a rare case that happens when the fragment is attached and then detached quickly
			return;
		}

		boolean hasStoredCard = mBillingInfo.hasStoredCard();
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : state.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : state.hasValidCardInfo(mBillingInfo);
		boolean travelerValid = hasValidTravelers();

		mShowSlideToWidget = travelerValid && paymentAddressValid && paymentCCValid && mIsDoneLoadingPriceChange
				&& !BackgroundDownloader.getInstance().isDownloading(KEY_APPLY_COUPON);
		if (isInCheckout() && mShowSlideToWidget) {
			showPurchaseViews();
		}
		else {
			hidePurchaseViews();
		}

		// Show/hide either the coupon button or the coupon applied layout
		View couponShow;
		if (Db.getHotelSearch().isCouponApplied()) {
			couponShow = mCouponAppliedContainer;
			mCouponButton.setVisibility(View.GONE);
		}
		else {
			couponShow = mCouponButton;
			mCouponAppliedContainer.setVisibility(View.GONE);
		}

		if (mInCheckout) {
			if (Db.getHotelSearch().getSelectedProperty().isMerchant()) {
				couponShow.setVisibility(View.VISIBLE);
			}
			else {
				mCouponButton.setVisibility(View.GONE);
				mCouponAppliedContainer.setVisibility(View.GONE);
			}
			mLegalInformationTextView.setVisibility(View.VISIBLE);
		}
		else {
			couponShow.setVisibility(View.INVISIBLE);
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
						Ui.runOnNextLayout(mScrollView, new Runnable() {
							public void run() {
								scrollToCheckout(false);
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

	public void resetSlider() {
		if (mSlideToPurchaseFragment != null) {
			mSlideToPurchaseFragment.resetSlider();
		}
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
		if (mAccountButton.isEnabled()) {
			mAccountButton.setEnabled(false);//We open a new activity and reenable accountButton in onResume
			Bundle args = LoginActivity.createArgumentsBundle(LineOfBusiness.HOTELS, null);
			User.signIn(getActivity(), args);
			OmnitureTracking.trackPageLoadHotelsLogin(getActivity());
		}
	}

	@Override
	public void accountLogoutClicked() {
		if (mAccountButton.isEnabled()) {
			mAccountButton.setEnabled(false);

			// Stop refreshing user (if we're currently doing so)
			BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
			mRefreshedUserTime = 0L;

			// Sign out user
			User.signOutAsync(getActivity(), null);

			// Update UI
			mAccountButton.bind(false, false, null);

			//After logout this will clear stored cards
			populatePaymentDataFromUser();
			populateTravelerDataFromUser();

			bindAll();
			updateViews();
			updateViewVisibilities();

			mAccountButton.setEnabled(true);
		}
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser());
		mRefreshedUserTime = System.currentTimeMillis();

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
			Rate selectedRate = Db.getHotelSearch().getSelectedRate();
			return services.hotelProduct(Db.getHotelSearch().getSearchParams(), Db.getHotelSearch()
					.getSelectedProperty(), selectedRate);
		}
	};

	private final OnDownloadComplete<HotelProductResponse> mHotelProductCallback = new OnDownloadComplete<HotelProductResponse>() {
		@Override
		public void onDownload(HotelProductResponse response) {
			if (response == null || response.hasErrors()) {
				handleHotelProductError(response);
			}
			else {
				final String selectedId = Db.getHotelSearch().getSelectedPropertyId();
				Rate selectedRate = Db.getHotelSearch().getSelectedRate();
				Rate newRate = response.getRate();

				if (TextUtils.equals(selectedRate.getRateKey(), response.getOriginalProductKey())) {
					if (!AndroidUtils.isRelease(getActivity())) {
						String val = SettingUtils.get(getActivity(),
								getString(R.string.preference_fake_hotel_price_change),
								getString(R.string.preference_fake_price_change_default));
						BigDecimal bigDecVal = new BigDecimal(val);

						//Update total price
						newRate.getDisplayTotalPrice().add(bigDecVal);

						//Update all nights total and per/night totals
						newRate.getNightlyRateTotal().add(bigDecVal);
						if (newRate.getRateBreakdownList() != null) {
							BigDecimal perNightChange = bigDecVal.divide(new BigDecimal(newRate
									.getRateBreakdownList().size()));
							for (RateBreakdown breakdown : newRate.getRateBreakdownList()) {
								breakdown.getAmount().add(perNightChange);
							}
						}

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
					updateViews();
					updateViewVisibilities();
				}
				else {
					handleHotelProductError(response);
				}
			}
		}
	};

	private final Download<CreateTripResponse> mCreateTripDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_CREATE_TRIP, services);
			return services.createTrip(Db.getHotelSearch().getSearchParams(), Db.getHotelSearch().getSelectedProperty());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCreateTripCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			if (response == null) {
				showRetryErrorDialog();
			}
			else if (response.hasErrors()) {
				handleCreateTripError(response);
			}
			else {

				Db.getHotelSearch().setCreateTripResponse(response);

				if (Db.getHotelSearch().isCouponApplied()) {
					Db.getHotelSearch().setCouponApplied(false);
					OmnitureTracking.trackHotelCouponRemoved(getActivity());
					refreshData();
					mCouponRemoveThrobberDialog.dismiss();
				}
				else {
					applyCoupon();
				}
			}
		}
	};

	private void handleCreateTripError(CreateTripResponse response) {
		ServerError firstError = response.getErrors().get(0);

		switch (firstError.getErrorCode()) {
		//TODO: Waiting for error codes. Make sure we handle all of them.
		default: {
			showRetryErrorDialog();
			break;
		}
		}
	}

	private void showRetryErrorDialog() {
		DialogFragment df = new RetryErrorDialogFragment();
		df.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "retryErrorDialog");
	}

	private void handleHotelProductError(HotelProductResponse response) {
		HotelErrorDialog dialog = HotelErrorDialog.newInstance();
		int messageId = R.string.e3_error_hotel_offers_hotel_service_failure;
		if (response != null && response.getErrors() != null) {
			for (ServerError error : response.getErrors()) {
				if (error.getErrorCode() == ServerError.ErrorCode.HOTEL_ROOM_UNAVAILABLE) {
					String selectedId = Db.getHotelSearch().getSelectedPropertyId();
					messageId = R.string.e3_error_hotel_offers_hotel_room_unavailable;
					Db.getHotelSearch().getAvailability(selectedId).removeRate(response.getOriginalProductKey());
				}
			}
		}

		dialog.setMessage(messageId);
		dialog.show(getFragmentManager(), HOTEL_OFFER_ERROR_DIALOG);
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
			case R.id.coupon_button: {
				mCouponDialogFragment = new CouponDialogFragment();
				mCouponDialogFragment.show(getChildFragmentManager(), CouponDialogFragment.TAG);
				break;
			}
			case R.id.coupon_clear: {
				clearCoupon();
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
			BreakdownDialogFragment dialogFrag = BreakdownDialogFragment.buildHotelRateBreakdownDialog(getActivity(),
					Db.getHotelSearch());
			dialogFrag.show(getFragmentManager(), BreakdownDialogFragment.TAG);
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
		Rate selectedRate = Db.getHotelSearch().getBookingRate();
		return selectedRate.getTotalAmountAfterTax();
	}

	@Override
	protected void modifyMaskedWalletBuilder(Builder builder) {
		builder.setCart(WalletUtils.buildHotelCart(getActivity()));

		builder.setPhoneNumberRequired(true);
		builder.setUseMinimalBillingAddress(true);
	}

	/**
	 * Binds the masked wallet to the billing info.  Warning: it WILL
	 * blow away whatever was here before - so only call this when
	 * we want to override the current data with Google Wallet!
	 */
	protected void onMaskedWalletFullyLoaded(boolean fromPreauth) {
		mWasUsingGoogleWallet = true;

		populateTravelerData();

		MaskedWallet maskedWallet = Db.getMaskedWallet();

		// Add the current traveler from the wallet, if it is full of data and we have none at the moment
		Traveler traveler = WalletUtils.addWalletAsTraveler(getActivity(), maskedWallet);
		BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), traveler, LineOfBusiness.HOTELS);

		// Bind credit card data, but only if they explicitly clicked "buy with wallet" or they have
		// no existing credit card info entered
		if (!fromPreauth || (TextUtils.isEmpty(mBillingInfo.getNumber()) && !mBillingInfo.hasStoredCard())) {
			WalletUtils.bindWalletToBillingInfo(maskedWallet, mBillingInfo);

			// Apply the mobile wallet coupon (if enabled, and no other cards are
			// currently being used #1865)
			if (WalletUtils.offerGoogleWalletCoupon(getActivity())) {
				applyWalletCoupon();
			}
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
		mCouponButton.setEnabled(enableButtons);
		mCouponRemoveView.setEnabled(enableButtons);

		// If we're using wallet and the promo code, hide the coupon layout (unless we failed to
		// apply the Google Wallet code).
		boolean offeredPromo = WalletUtils.offerGoogleWalletCoupon(getActivity());
		boolean codeIsPromo = usingWalletPromoCoupon();
		boolean applyingCoupon = BackgroundDownloader.getInstance().isDownloading(KEY_APPLY_COUPON);
		boolean appliedCoupon = Db.getHotelSearch().isCouponApplied();
		if (mCouponButton.getVisibility() == View.VISIBLE) {
			mCouponButton.setVisibility(mBillingInfo.isUsingGoogleWallet()
					&& offeredPromo && codeIsPromo && (applyingCoupon || appliedCoupon) ? View.GONE : View.VISIBLE);
		}

		mHotelReceipt.bind(appliedWalletPromoCoupon());
	}

	// Coupons

	public void applyCoupon() {
		Log.i("Trying to apply coupon code: " + mCouponCode);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_APPLY_COUPON)) {
			Log.w("Somehow, we were already trying to apply a coupon when we tried to apply another one."
					+ "  Cancelling previous coupon code attempt.");
			bd.cancelDownload(KEY_APPLY_COUPON);
		}

		bd.startDownload(KEY_APPLY_COUPON, mCouponDownload, mCouponCallback);

		updateViewVisibilities();

		// Show a special spinner dialog for wallet
		if (usingWalletPromoCoupon()) {
			mFragmentModLock.runWhenSafe(new Runnable() {
				@Override
				public void run() {
					mWalletPromoThrobberDialog = ThrobberDialog.newInstance(getString(R.string.wallet_promo_applying));
					mWalletPromoThrobberDialog.setCancelListener(HotelOverviewFragment.this);
					mWalletPromoThrobberDialog.show(getFragmentManager(), ThrobberDialog.TAG);
				}
			});
		}
	}

	public void clearCoupon() {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				mCouponRemoveThrobberDialog = ThrobberDialog.newInstance(getString(R.string.coupon_removing_dialog));
				mCouponRemoveThrobberDialog.setCancelable(false);
				mCouponRemoveThrobberDialog.show(getFragmentManager(), ThrobberDialog.TAG);
			}
		});

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_APPLY_COUPON)) {
			bd.cancelDownload(KEY_APPLY_COUPON);
		}
		if (bd.isDownloading(KEY_CREATE_TRIP)) {
			bd.cancelDownload(KEY_CREATE_TRIP);
		}
		bd.startDownload(KEY_CREATE_TRIP, mCreateTripDownload, mCreateTripCallback);
	}

	private boolean usingWalletPromoCoupon() {
		return WalletUtils.getWalletCouponCode(getActivity()).equals(mCouponCode);
	}

	private boolean appliedWalletPromoCoupon() {
		return mBillingInfo.isUsingGoogleWallet() && WalletUtils.offerGoogleWalletCoupon(getActivity())
				&& usingWalletPromoCoupon() && Db.getHotelSearch().isCouponApplied();
	}

	private void applyWalletCoupon() {
		// If the user already has a coupon applied, clear it (and tell the user)
		boolean hadCoupon = Db.getHotelSearch().isCouponApplied();

		onApplyCoupon(WalletUtils.getWalletCouponCode(getActivity()));

		// Apply this later so that this dialog shows up on top of the progress one
		if (hadCoupon) {
			mFragmentModLock.runWhenSafe(new Runnable() {
				@Override
				public void run() {
					SimpleDialogFragment df = SimpleDialogFragment.newInstance(null,
							getString(R.string.coupon_replaced_message));
					df.show(getFragmentManager(), "couponReplacedDialog");
				}
			});
		}
	}

	private void clearWalletPromoCoupon() {
		mCouponCode = null;
		BackgroundDownloader.getInstance().cancelDownload(KEY_APPLY_COUPON);
		Db.getHotelSearch().setCreateTripResponse(null);

		if (mWalletPromoThrobberDialog != null) {
			mWalletPromoThrobberDialog.dismiss();
		}
	}

	private final Download<CreateTripResponse> mCouponDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_APPLY_COUPON, services);
			return services.applyCoupon(mCouponCode, Db.getHotelSearch().getSearchParams(), Db
					.getHotelSearch().getSelectedProperty());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCouponCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			// Don't execute if we were killed before finishing
			if (!isAdded()) {
				return;
			}

			if (mWalletPromoThrobberDialog != null && mWalletPromoThrobberDialog.isAdded()) {
				mWalletPromoThrobberDialog.dismiss();
			}

			if (mCouponDialogFragment != null && mCouponDialogFragment.isAdded()) {
				mCouponDialogFragment.dismiss();
			}

			if (response == null) {
				Log.w("Failed to apply coupon code (null response): " + mCouponCode);

				DialogFragment df = SimpleDialogFragment.newInstance(null, getString(R.string.coupon_error_no_code));
				df.show(getChildFragmentManager(), "couponError");

				handleWalletPromoErrorIfApplicable();
			}
			else if (response.hasErrors()) {
				Log.w("Failed to apply coupon code (server errors): " + mCouponCode);

				DialogFragment df = SimpleDialogFragment.newInstance(null, getString(R.string.coupon_error_no_code));
				df.show(getChildFragmentManager(), "couponError");

				handleWalletPromoErrorIfApplicable();
			}
			else {
				Log.i("Applied coupon code: " + mCouponCode);

				Db.getHotelSearch().setCouponApplied(true);
				Db.getHotelSearch().setCreateTripResponse(response);

				OmnitureTracking.trackHotelCouponApplied(getActivity(), mCouponCode);
			}

			// Regardless of what happened, let's refresh the page
			refreshData();
		}
	};

	private void handleWalletPromoErrorIfApplicable() {
		// We're just detecting if the user is using the Google Wallet coupon code for this
		if (usingWalletPromoCoupon()) {
			mFragmentModLock.runWhenSafe(new Runnable() {
				@Override
				public void run() {
					// #1722: If the user tried to book a hotel outside of 2013,
					// then state that as the reason for failure
					LocalDate checkOutDate = Db.getHotelSearch().getSearchParams().getCheckOutDate();
					int errorStrId = checkOutDate.getYear() >= 2014 ? R.string.wallet_promo_expired
							: R.string.error_wallet_promo_cannot_apply;
					SimpleCallbackDialogFragment df = SimpleCallbackDialogFragment.newInstance(null,
							getString(errorStrId), getString(R.string.ok),
							CALLBACK_WALLET_PROMO_APPLY_ERROR);
					df.show(getFragmentManager(), "couponWalletPromoErrorDialog");
				}
			});

			// Reset the coupon code
			mCouponCode = null;
		}
	}

	// Error handling for create retry dialog.

	public void retryCoupon() {
		onApplyCoupon(mCouponCode);
	}

	public void cancelRetryCouponDialog() {
		if (mWalletPromoThrobberDialog != null && mWalletPromoThrobberDialog.isAdded()) {
			mWalletPromoThrobberDialog.dismiss();
		}

		if (mCouponDialogFragment != null && mCouponDialogFragment.isAdded()) {
			mCouponDialogFragment.dismiss();
		}
		onCancelApplyCoupon();
	}

	// CancelListener (for wallet promo dialog)

	@Override
	public void onCancel() {
		clearWalletPromoCoupon();

		updateWalletViewVisibilities();
	}

	//////////////////////////////////////////////////////////////////////////
	// CouponDialogFragmentListener

	@Override
	public void onApplyCoupon(String couponCode) {
		mCouponCode = couponCode;
		refreshData();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_CREATE_TRIP)) {
			bd.cancelDownload(KEY_CREATE_TRIP);
		}
		bd.startDownload(KEY_CREATE_TRIP, mCreateTripDownload, mCreateTripCallback);
	}

	@Override
	public void onCancelApplyCoupon() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_APPLY_COUPON)) {
			bd.cancelDownload(KEY_APPLY_COUPON);
		}
		if (bd.isDownloading(KEY_CREATE_TRIP)) {
			bd.cancelDownload(KEY_CREATE_TRIP);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SimpleCallbackDialogFragmentListener

	@Override
	public void onSimpleDialogClick(int callbackId) {
		onSimpleDialogCancel(callbackId);
	}

	@Override
	public void onSimpleDialogCancel(int callbackId) {
		// #1687: Make sure to update view visibilities, as the slide-to-purchase may still have a state change yet
		if (callbackId == CALLBACK_WALLET_PROMO_APPLY_ERROR) {
			updateViewVisibilities();
		}
	}

}
