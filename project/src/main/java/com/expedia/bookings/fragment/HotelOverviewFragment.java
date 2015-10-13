package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.HotelMapActivity;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.dialog.CouponDialogFragment;
import com.expedia.bookings.dialog.CouponDialogFragment.CouponDialogFragmentListener;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.dialog.ThrobberDialog.CancelListener;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.HotelBookingFragment.HotelBookingState;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.model.HotelTravelerFlowState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.HotelReceipt;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.expedia.bookings.widget.WalletButton;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest.Builder;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

public class HotelOverviewFragment extends LoadWalletFragment implements AccountButtonClickListener,
	LoginConfirmLogoutDialogFragment.DoLogoutListener,
	CancelListener, CouponDialogFragmentListener {

	public interface BookingOverviewFragmentListener {
		public void checkoutStarted();

		public void checkoutEnded();
	}

	public static final String TAG_SLIDE_TO_PURCHASE_FRAG = "TAG_SLIDE_TO_PURCHASE_FRAG";
	public static final String HOTEL_OFFER_ERROR_DIALOG = "HOTEL_OFFER_ERROR_DIALOG";
	public static final String HOTEL_SOLD_OUT_DIALOG = "HOTEL_SOLD_OUT_DIALOG";
	public static final String HOTEL_EXPIRED_ERROR_DIALOG = "HOTEL_EXPIRED_ERROR_DIALOG";

	private static final String INSTANCE_WAS_LOGGED_IN = "INSTANCE_WAS_LOGGED_IN";
	private static final String INSTANCE_REFRESHED_USER_TIME = "INSTANCE_REFRESHED_USER";
	private static final String INSTANCE_IN_CHECKOUT = "INSTANCE_IN_CHECKOUT";
	private static final String INSTANCE_SHOW_SLIDE_TO_WIDGET = "INSTANCE_SHOW_SLIDE_TO_WIDGET";
	private static final String INSTANCE_DONE_LOADING_PRICE_CHANGE = "INSTANCE_DONE_LOADING_PRICE_CHANGE";
	private static final String INSTANCE_COUPON_CODE = "INSTANCE_COUPON_CODE";
	private static final String INSTANCE_WAS_USING_GOOGLE_WALLET = "INSTANCE_WAS_USING_GOOGLE_WALLET";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	private static final String DIALOG_LOADING_DETAILS = "DIALOG_LOADING_DETAILS";

	private boolean mInCheckout = false;
	private BookingOverviewFragmentListener mBookingOverviewFragmentListener;

	private HotelErrorDialog mBookingUnavailableDialog;

	private BillingInfo mBillingInfo;

	private ScrollView mScrollView;
	private ScrollViewListener mScrollViewListener;

	private HotelReceipt mHotelReceipt;
	private FrameLayout mCheckoutLayout;
	private TouchableFrameLayout mCheckoutLayoutBlocker;

	private AccountButton mAccountButton;
	private WalletButton mWalletButton;
	private LinearLayout mHintContainer;
	private ImageView mCheckoutDivider;
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
	private TextView mCheckoutDisclaimerTextView;
	private View mScrollSpacerView;

	private FrameLayout mSlideToPurchaseFragmentLayout;

	private boolean mShowSlideToWidget;
	private String mSlideToPurchasePriceString;
	private SlideToPurchaseFragment mSlideToPurchaseFragment;

	private boolean mIsDoneLoadingPriceChange = false;

	private CouponDialogFragment mCouponDialogFragment;
	private ThrobberDialog mCouponRemoveThrobberDialog;
	private ThrobberDialog mCreateTripDialog;

	//When we last refreshed user data.
	private long mRefreshedUserTime = 0L;
	private boolean mWasLoggedIn = false;

	// We keep track of if we need to maintain the scroll position
	// This is needed when we call startCheckout before a layout occurs
	// typically on rotation
	private boolean mMaintainStartCheckoutPosition;

	private FragmentModificationSafeLock mFragmentModLock = new FragmentModificationSafeLock();

	private HotelBookingFragment mHotelBookingFragment;

	private Animation mPurchaseViewsAnimation;

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
			mWasLoggedIn = savedInstanceState.getBoolean(INSTANCE_WAS_LOGGED_IN);
		}
		else {
			// Reset Google Wallet state each time we get here
			Db.clearGoogleWallet();

			mWasLoggedIn = User.isLoggedIn(getActivity());
		}

		// #1715: Disable Google Wallet on non-merchant hotels
		Rate rate = Db.getTripBucket().getHotel().getRate();
		if (!Db.getTripBucket().getHotel().getProperty().isMerchant() || rate.isPayLater()) {
			disableGoogleWallet();
		}

		AdTracker.trackHotelCheckoutStarted();

		mHotelBookingFragment = Ui.findSupportFragment(this, HotelBookingFragment.TAG);

		if (mHotelBookingFragment == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			mHotelBookingFragment = new HotelBookingFragment();
			ft.add(mHotelBookingFragment, HotelBookingFragment.TAG);
			ft.commit();
		}
		OmnitureTracking.trackPageLoadHotelsRateDetails();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking_overview, container, false);

		mCouponDialogFragment = Ui.findSupportFragment(this, CouponDialogFragment.TAG);

		mScrollView = Ui.findView(view, R.id.scroll_view);

		mHotelReceipt = Ui.findView(view, R.id.receipt);
		mCheckoutLayout = Ui.findView(view, R.id.checkout_layout);
		mCheckoutLayoutBlocker = Ui.findView(view, R.id.checkout_layout_touch_blocker);

		mAccountButton = Ui.findView(view, R.id.account_button_root);
		mWalletButton = Ui.findView(view, R.id.wallet_button_layout);
		mHintContainer = Ui.findView(view, R.id.hint_container);
		mCheckoutDivider = Ui.findView(view, R.id.checkout_divider);
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
		mCheckoutDisclaimerTextView = Ui.findView(view, R.id.checkout_disclaimer);
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

		// Dont show checkout or let use touch it when receipt is showing
		mCheckoutLayout.setAlpha(0);
		mCheckoutLayoutBlocker.setBlockNewEventsEnabled(true);

		mBillingInfo = Db.getBillingInfo();
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		// Detect user state, update account button accordingly
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval_ms);
			if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
				Log.d("Refreshing user profile...");

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(KEY_REFRESH_USER)) {
					bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
				}
			}
			mAccountButton.bind(false, true, Db.getUser(), LineOfBusiness.HOTELS);
		}
		else {
			mAccountButton.bind(false, false, null, LineOfBusiness.HOTELS);
		}

		// restore
		mHotelReceipt.restoreInstanceState(savedInstanceState);

		// Configure LineOfBusiness
		mStoredCreditCard.setLineOfBusiness(LineOfBusiness.HOTELS);

		// Listeners
		mAccountButton.setListener(this);
		mTravelerButton.setOnClickListener(mOnClickListener);
		mTravelerSection.setOnClickListener(mOnClickListener);
		mPaymentButton.setOnClickListener(mOnClickListener);
		mStoredCreditCard.setOnClickListener(mOnClickListener);
		mCreditCardSectionButton.setOnClickListener(mOnClickListener);
		mCouponButton.setOnClickListener(mOnClickListener);
		mCouponRemoveView.setOnClickListener(mOnClickListener);
		mLegalInformationTextView.setOnClickListener(mOnClickListener);
		mHotelReceipt.setRateBreakdownClickListener(mRateBreakdownClickListener);
		mHotelReceipt.setOnViewMapClickListener(mViewMapClickListener);

		mWalletButton.setPromoVisible(ProductFlavorFeatureConfiguration.getInstance().isGoogleWalletPromoEnabled());
		// Touch events to constituent parts are handled in WalletButton.onInterceptTouchEvent(...)
		mWalletButton.setOnClickListener(mWalletButtonClickListener);


		// We underline the coupon button text in code to avoid re-translating
		mCouponButton.setPaintFlags(mCouponButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		toggleOrMessaging(User.isLoggedIn(getActivity()));
		mAccountButton.setVisibility(ProductFlavorFeatureConfiguration.getInstance().isSigninEnabled() ? View.VISIBLE : View.GONE);

		return view;
	}

	private void toggleOrMessaging(boolean isSignedIn) {
		mHintContainer.setVisibility(!isSignedIn ? View.VISIBLE : View.GONE);
		mCheckoutDivider.setVisibility(!isSignedIn ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();

		Events.register(this);

		mWalletPromoThrobberDialog = Ui.findSupportFragment((FragmentActivity) getActivity(), ThrobberDialog.TAG);
		if (mWalletPromoThrobberDialog != null && mWalletPromoThrobberDialog.isAdded()) {
			mWalletPromoThrobberDialog.setCancelListener(this);
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		HotelErrorDialog errorDialog = (HotelErrorDialog) getFragmentManager().findFragmentByTag(HOTEL_OFFER_ERROR_DIALOG);
		if (errorDialog == null) {
			// When we resume, there is a possibility that:
			// 1. We were using GWallet (with coupon), but are no longer using GWallet
			// 2. We were not using GWallet, but now are doing so (and thus want to apply the GWallet code)
			boolean isUsingGoogleWallet = Db.getBillingInfo().isUsingGoogleWallet();
			final boolean isCouponAvailable = WalletUtils.offerGoogleWalletCoupon(getActivity());
			boolean isCouponApplied = Db.getTripBucket().getHotel().isCouponApplied();
			if (mWasUsingGoogleWallet && !isUsingGoogleWallet && isCouponApplied && usingWalletPromoCoupon()) {
				clearWalletPromoCoupon();
			}
			else if (!mWasUsingGoogleWallet && isUsingGoogleWallet && isCouponAvailable) {
				applyWalletCoupon();
			}

			refreshData();

			if (mHotelBookingFragment != null && !mHotelBookingFragment.isDownloadingCreateTrip() && !mIsDoneLoadingPriceChange) {
				mHotelBookingFragment.startDownload(HotelBookingState.CREATE_TRIP);
				showCreateTripDialog();
			}
		}

		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}

		mFragmentModLock.setSafe(true);

		//We disable this for sign in, but when the user comes back it should be enabled.
		mAccountButton.setEnabled(true);
	}

	@Override
	public void onPause() {
		super.onPause();

		Events.unregister(this);

		mWasUsingGoogleWallet = mBillingInfo.isUsingGoogleWallet();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_REFRESH_USER);
			// Since we are exiting the screen, let's reset coupon.
			if (Db.getTripBucket() != null && Db.getTripBucket().getHotel() != null) {
				Db.getTripBucket().getHotel().setIsCouponApplied(false);
			}
		}
		else {
			bd.unregisterDownloadCallback(KEY_REFRESH_USER);
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
		outState.putBoolean(INSTANCE_WAS_LOGGED_IN, mWasLoggedIn);

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
		else {
			Traveler primary = travelers.get(0);
			travelers.clear();
			travelers.add(primary);
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
			if (mBillingInfo.getStoredCard() != null && !Db.getTripBucket().getHotel().isCardTypeSupported(mBillingInfo.getStoredCard().getType())) {
				mBillingInfo.setStoredCard(null);
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
	}

	private void refreshAccountButtonState() {
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
					&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
				//We have a user (either from memory, or loaded from disk)
				int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval_ms);
				if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
					Log.d("Refreshing user profile...");

					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(KEY_REFRESH_USER)) {
						bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
					}
				}
				Traveler.LoyaltyMembershipTier userTier = Db.getUser().getLoggedInLoyaltyMembershipTier(getActivity());
				if (userTier.isGoldOrSilver() && User.isLoggedIn(getActivity()) != mWasLoggedIn) {
					Db.getTripBucket().getHotel().getCreateTripResponse().setRewardsPoints("");
				}
				mAccountButton.bind(false, true, Db.getUser(), LineOfBusiness.HOTELS);
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, LineOfBusiness.HOTELS);
			}
		}
		else {
			mAccountButton.bind(false, false, null, LineOfBusiness.HOTELS);
		}
	}

	public void updateViews() {
		mLegalInformationTextView.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement());

		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		Property property = hotel.getProperty();
		Rate rate = hotel.getRate();

		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (hotel.isCouponApplied()) {
			rate = hotel.getCouponRate();

			// Show off the savings!
			mCouponSavedTextView.setText(getString(R.string.coupon_saved_TEMPLATE,
				hotel.getCouponRate().getTotalPriceAdjustments().getFormattedMoney()));
		}
		if (PointOfSale.getPointOfSale().showFTCResortRegulations()) {
			updateCheckoutDisclaimerText();
		}
		else {
			mCheckoutDisclaimerTextView.setVisibility(View.GONE);
		}

		mSlideToPurchasePriceString = HotelUtils.getSlideToPurchaseString(getActivity(), property, rate,
			ExpediaBookingApp.useTabletInterface(getActivity()));
		mSlideToPurchaseFragment.setTotalPriceString(mSlideToPurchasePriceString);

		mHotelReceipt.bind(mIsDoneLoadingPriceChange, hotel);
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
		boolean isNotDownloadingCoupon = !mHotelBookingFragment.isDownloadingCoupon();

		mShowSlideToWidget = travelerValid && paymentAddressValid && paymentCCValid && mIsDoneLoadingPriceChange && isNotDownloadingCoupon;
		if (isInCheckout() && mShowSlideToWidget) {
			showPurchaseViews();
		}
		else {
			hidePurchaseViews();
		}

		// Show/hide either the coupon button or the coupon applied layout
		View couponShow;
		if (Db.getTripBucket().getHotel().isCouponApplied()) {
			couponShow = mCouponAppliedContainer;
			mCouponButton.setVisibility(View.GONE);
		}
		else {
			couponShow = mCouponButton;
			mCouponAppliedContainer.setVisibility(View.GONE);
		}

		if (mInCheckout) {
			couponShow.setVisibility(View.VISIBLE);
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

	private void updateCheckoutDisclaimerText() {
		Rate rate = Db.getTripBucket().getHotel().getRate();

		if (rate.showResortFeesMessaging()) {
			mCheckoutDisclaimerTextView.setText(HotelUtils.getCheckoutResortFeesText(getActivity(), rate));
			mCheckoutDisclaimerTextView.setVisibility(View.VISIBLE);
		}
		else if (rate.isPayLater()) {
			mCheckoutDisclaimerTextView.setText(HotelUtils.getCheckoutPayLaterText(getActivity(), rate));
			mCheckoutDisclaimerTextView.setVisibility(View.VISIBLE);
		}
		else {
			mCheckoutDisclaimerTextView.setVisibility(View.GONE);
		}
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
			final int actionBarHeight = ((FragmentActivity) getActivity()).getActionBar().getHeight();

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
					mScrollSpacerView.requestLayout();
				}
			});
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
				int targetY = mShowSlideToWidget ?
					mScrollViewListener.getCheckoutY() + mSlideToPurchaseFragmentLayout.getHeight() :
					mScrollViewListener.getCheckoutY();
				if (animate) {
					mScrollView.smoothScrollTo(0, targetY);
				}
				else {
					mScrollView.scrollTo(0, targetY);
				}
			}
		});
	}

	public void startCheckout(final boolean animate, boolean shouldScrollToCheckout) {
		if (!isInCheckout()) {
			OmnitureTracking.trackPageLoadHotelsCheckoutInfo();
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
			OmnitureTracking.trackPageLoadHotelsRateDetails();
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
			if (mPurchaseViewsAnimation != null) {
				mPurchaseViewsAnimation.setAnimationListener(null);
				mPurchaseViewsAnimation.cancel();
				mPurchaseViewsAnimation = null;
				mSlideToPurchaseFragmentLayout.setVisibility(View.VISIBLE);
			}
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				OmnitureTracking.trackPageLoadHotelsCheckoutSlideToPurchase();
			}
		}).start();

		mSlideToPurchaseFragmentLayout.setVisibility(View.VISIBLE);
		setScrollSpacerViewHeight();

		if (animate) {
			mPurchaseViewsAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
			mPurchaseViewsAnimation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					// Ignore
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// Ignore
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					setScrollSpacerViewHeight();
					mPurchaseViewsAnimation = null;
				}
			});
			mSlideToPurchaseFragmentLayout.startAnimation(mPurchaseViewsAnimation);
		}
	}

	private void hidePurchaseViews() {
		if (mSlideToPurchaseFragmentLayout.getVisibility() != View.VISIBLE) {
			if (mPurchaseViewsAnimation != null) {
				mPurchaseViewsAnimation.setAnimationListener(null);
				mPurchaseViewsAnimation.cancel();
				mPurchaseViewsAnimation = null;
				mSlideToPurchaseFragmentLayout.setVisibility(View.INVISIBLE);
				setScrollSpacerViewHeight();
			}
			return;
		}

		mPurchaseViewsAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
		mPurchaseViewsAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// Ignore
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Ignore
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSlideToPurchaseFragmentLayout.setVisibility(View.INVISIBLE);
				setScrollSpacerViewHeight();
				mPurchaseViewsAnimation = null;
			}
		});

		mSlideToPurchaseFragmentLayout.startAnimation(mPurchaseViewsAnimation);
	}

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {
		if (mAccountButton.isEnabled()) {
			mAccountButton.setEnabled(false);//We open a new activity and reenable accountButton in onResume;
			Bundle args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.HOTELS, null);
			User.signIn(getActivity(), args);
			OmnitureTracking.trackPageLoadHotelsLogin();
		}
	}

	@Override
	public void accountLogoutClicked() {
		LoginConfirmLogoutDialogFragment df = new LoginConfirmLogoutDialogFragment();
		df.show(getChildFragmentManager(), LoginConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		mAccountButton.setEnabled(false);

		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUserTime = 0L;

		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null, LineOfBusiness.HOTELS);

		//After logout this will clear stored cards
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();

		bindAll();
		updateViews();
		updateViewVisibilities();

		mAccountButton.setEnabled(true);
		mWasLoggedIn = false;

		toggleOrMessaging(User.isLoggedIn(getActivity()));

		Events.post(new Events.CreateTripDownloadRetry());
	}

	public void onLoginCompleted() {
		// Let's reset MerEmailOptInStatus to false.
		Db.getTripBucket().getHotel().setIsMerEmailOptIn(false);
		Db.saveTripBucket(getActivity());

		if (User.isLoggedIn(getActivity()) != mWasLoggedIn) {
			if (mHotelBookingFragment != null && !mHotelBookingFragment.isDownloadingCreateTrip()) {
				mHotelBookingFragment.startDownload(HotelBookingState.CREATE_TRIP);
				showCreateTripDialog();
				mWasLoggedIn = true;
			}
		}
		else {
			mAccountButton.bind(false, true, Db.getUser(), LineOfBusiness.HOTELS);
			mRefreshedUserTime = System.currentTimeMillis();

			if (Db.getTripBucket().getHotel().isCouponGoogleWallet()) {
				replaceCoupon(WalletUtils.getWalletCouponCode(getActivity()), false);
			}

			populateTravelerData();
			populatePaymentDataFromUser();
			populateTravelerDataFromUser();

			bindAll();
			updateViews();
			updateViewVisibilities();
		}
		toggleOrMessaging(User.isLoggedIn(getActivity()));
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
				doLogout();
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
				TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
				Rate selectedRate = hotel.getRate();
				boolean isPayLater = selectedRate.isPayLater();
				boolean isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPayLaterCouponMessaging);
				if (isPayLater && isUserBucketedForTest) {
					handlePayLaterCouponError();
					break;
				}

				OmnitureTracking.trackHotelCouponExpand();
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
					Db.getTripBucket().getHotel());
			dialogFrag.show(getFragmentManager(), BreakdownDialogFragment.TAG);
		}
	};

	private HotelReceipt.OnViewMapClickListener mViewMapClickListener = new HotelReceipt.OnViewMapClickListener() {

		@Override
		public void onViewMapClicked() {
			Intent intent = HotelMapActivity.createIntent(getActivity());
			intent.putExtra(HotelMapActivity.INSTANCE_IS_HOTEL_RECEIPT, true);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			startActivity(intent);
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.explode);
		}
	};

	// Scroll Listener

	private class ScrollViewListener extends GestureDetector.SimpleOnGestureListener implements OnScrollListener,
			OnTouchListener, HotelReceipt.OnSizeChangedListener, FrameLayout.OnSizeChangedListener {

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

			mCheckoutLayoutBlocker.setBlockNewEventsEnabled(alpha == 0);
			mCheckoutLayout.setAlpha(alpha);

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
		return Db.getTripBucket().getHotel().getRate().getTotalAmountAfterTax();
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
		boolean applyingCoupon = mHotelBookingFragment.isDownloadingCoupon();
		boolean appliedCoupon = Db.getTripBucket().getHotel().isCouponApplied();
		if (mCouponButton.getVisibility() == View.VISIBLE) {
			mCouponButton.setVisibility(mBillingInfo.isUsingGoogleWallet()
					&& offeredPromo && codeIsPromo && (applyingCoupon || appliedCoupon) ? View.GONE : View.VISIBLE);
		}
	}

	// Coupons

	public void applyCoupon() {
		Log.i("Trying to apply coupon code: " + mCouponCode);

		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_APPLY, mCouponCode);

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

	public void replaceCoupon(String couponCode, final boolean showReplaceWarning) {
		mCouponCode = couponCode;
		Log.i("Trying to replace current coupon with coupon: " + mCouponCode);
		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_REPLACE, mCouponCode);
		updateViewVisibilities();
		// Show a special spinner dialog for wallet
		if (usingWalletPromoCoupon()) {
			mFragmentModLock.runWhenSafe(new Runnable() {
				@Override
				public void run() {
					if (mWalletPromoThrobberDialog == null) {
						mWalletPromoThrobberDialog = ThrobberDialog.newInstance(getString(R.string.wallet_promo_applying));
						mWalletPromoThrobberDialog.setCancelListener(HotelOverviewFragment.this);
					}
					mWalletPromoThrobberDialog.show(getFragmentManager(), ThrobberDialog.TAG);

					Fragment frag = getFragmentManager().findFragmentByTag("WALLET_REPLACE_DIALOG");
					if (showReplaceWarning && isResumed() && frag == null) {
						SimpleDialogFragment df = SimpleDialogFragment.newInstance(null, getString(R.string.coupon_replaced_message));
						df.show(getFragmentManager(), "WALLET_REPLACE_DIALOG");
					}
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

		mHotelBookingFragment.startDownload(HotelBookingState.COUPON_REMOVE);
	}

	private boolean usingWalletPromoCoupon() {
		return WalletUtils.getWalletCouponCode(getActivity()).equals(mCouponCode);
	}

	private boolean appliedWalletPromoCoupon() {
		return mBillingInfo.isUsingGoogleWallet() && WalletUtils.offerGoogleWalletCoupon(getActivity())
				&& usingWalletPromoCoupon() && Db.getTripBucket().getHotel().isCouponApplied();
	}

	private void applyWalletCoupon() {
		// If the user already has a coupon applied, clear it (and tell the user)
		boolean hadCoupon = Db.getTripBucket().getHotel().isCouponApplied();
		String walletCoupon = WalletUtils.getWalletCouponCode(getActivity());
		// Apply this later so that this dialog shows up on top of the progress one
		if (hadCoupon) {
			replaceCoupon(walletCoupon, true);
		}
		else {
			onApplyCoupon(walletCoupon);
		}
	}

	private void clearWalletPromoCoupon() {
		mCouponCode = null;
		clearCoupon();

		if (mWalletPromoThrobberDialog != null) {
			mWalletPromoThrobberDialog.dismiss();
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

		if (mCouponRemoveThrobberDialog != null && mCouponRemoveThrobberDialog.isAdded()) {
			mCouponRemoveThrobberDialog.dismiss();
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
		applyCoupon();
	}

	@Override
	public void onCancelApplyCoupon() {
		mHotelBookingFragment.cancelDownload(HotelBookingState.COUPON_APPLY);
	}

	private void dismissDialogs() {
		if (mCouponDialogFragment != null && mCouponDialogFragment.isAdded()) {
			mCouponDialogFragment.dismiss();
		}
		if (mWalletPromoThrobberDialog != null && mWalletPromoThrobberDialog.isAdded()) {
			mWalletPromoThrobberDialog.dismiss();
		}
		if (mCouponRemoveThrobberDialog != null && mCouponRemoveThrobberDialog.isAdded()) {
			mCouponRemoveThrobberDialog.dismiss();
		}
		if (mCreateTripDialog != null && mCreateTripDialog.isAdded()) {
			mCreateTripDialog.dismiss();
		}
	}

	private void showCreateTripDialog() {
		if (mCreateTripDialog != null) {
			mCreateTripDialog.dismiss();
		}

		mCreateTripDialog = ThrobberDialog.newInstance(getString(R.string.spinner_text_hotel_create_trip));
		mCreateTripDialog.show(getFragmentManager(), DIALOG_LOADING_DETAILS);
	}

	///////////////////////////////////
	/// Otto Event Subscriptions

	@Subscribe
	public void onSimpleDialogClick(Events.SimpleCallBackDialogOnClick event) {
		if (event.callBackId == SimpleCallbackDialogFragment.CODE_WALLET_PROMO_APPLY_ERROR) {
			updateViewVisibilities();
		}
	}

	@Subscribe
	public void onSimpleDialogCancel(Events.SimpleCallBackDialogOnCancel event) {
		// #1687: Make sure to update view visibilities, as the slide-to-purchase may still have a state change yet
		if (event.callBackId == SimpleCallbackDialogFragment.CODE_WALLET_PROMO_APPLY_ERROR) {
			updateViewVisibilities();
		}
	}

	@Subscribe
	public void onCreateTripDownloadSuccess(Events.CreateTripDownloadSuccess event) {
		mIsDoneLoadingPriceChange = true;
		if (event.createTripResponse instanceof CreateTripResponse) {
			// Now we have the valid payments data
			Rate rate = Db.getTripBucket().getHotel().getRate();
			if (!Db.getTripBucket().getHotel().isCardTypeSupported(CreditCardType.GOOGLE_WALLET) || rate.isPayLater()) {
				Log.d("disableGoogleWallet: safeGoogleWalletTripPaymentTypeCheck");
				disableGoogleWallet();
			}
		}

		dismissDialogs();
		refreshData();
	}

	@Subscribe
	public void onCreateTripDownloadError(Events.CreateTripDownloadError event) {
		dismissDialogs();
	}

	@Subscribe
	public void onCreateTripDownloadRetry(Events.CreateTripDownloadRetry event) {
		mHotelBookingFragment.startDownload(HotelBookingState.CREATE_TRIP);
		showCreateTripDialog();
	}

	@Subscribe
	public void onCreateTripDownloadRetryCancel(Events.CreateTripDownloadRetryCancel event) {
		if (getActivity() != null) {
			getActivity().finish();
		}
	}

	@Subscribe
	public void onBookingUnavailable(Events.BookingUnavailable event) {
		dismissDialogs();

		if (getActivity() != null && !getActivity().isFinishing()) {
			if (mBookingUnavailableDialog != null) {
				mBookingUnavailableDialog.dismiss();
			}

			mBookingUnavailableDialog = HotelErrorDialog.newInstance();
			mBookingUnavailableDialog.setMessage(
				Phrase.from(getActivity(), R.string.error_hotel_is_now_sold_out_TEMPLATE).put("brand", BuildConfig.brand).format().toString());
			mBookingUnavailableDialog.show(getFragmentManager(), HOTEL_SOLD_OUT_DIALOG);
		}
	}

	@Subscribe
	public void onCouponApplied(Events.CouponApplyDownloadSuccess event) {
		dismissDialogs();
		refreshData();
	}

	@Subscribe
	public void onCouponRemoved(Events.CouponRemoveDownloadSuccess event) {
		dismissDialogs();
		refreshData();
	}

	@Subscribe
	public void onCouponCancel(Events.CouponDownloadCancel event) {
		dismissDialogs();
	}

	@Subscribe
	public void onCouponDownloadError(Events.CouponDownloadError event) {
		dismissDialogs();
	}

	@Subscribe
	public void onTripItemExpired(Events.TripItemExpired event) {
		dismissDialogs();

		HotelErrorDialog dialog = HotelErrorDialog.newInstance();
		dialog.setMessage(getString(R.string.error_hotel_no_longer_available));
		dialog.show(getFragmentManager(), HOTEL_EXPIRED_ERROR_DIALOG);
	}

	/*
	 * Pay Later Coupon Error Handling
	 */

	private void handlePayLaterCouponError() {
		String errorMessage = getString(R.string.coupon_error_pay_later_hotel);
		DialogFragment df = SimpleDialogFragment.newInstance(null, errorMessage);
		df.show(getChildFragmentManager(), "couponError");
		Events.post(new Events.CouponDownloadError());
	}
}
