package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.User;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.interfaces.ILOBable;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.TabletWalletButton;
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class CheckoutLoginButtonsFragment extends LoadWalletFragment
	implements AccountButtonClickListener, LoginConfirmLogoutDialogFragment.DoLogoutListener, ILOBable {
	private static final String INSTANCE_REFRESHED_USER_TIME = "INSTANCE_REFRESHED_USER";
	private static final String INSTANCE_WAS_LOGGED_IN = "INSTANCE_WAS_LOGGED_IN";
	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	// LOB
	private static final String STATE_LOB = "STATE_LOB";
	private LineOfBusiness mLob;

	public interface ILoginStateChangedListener {
		void onLoginStateChanged();
	}

	public interface IWalletButtonStateChangedListener {
		void onWalletButtonStateChanged(boolean enableButtons);
	}

	private AccountButton mAccountButton;
	private TabletWalletButton mWalletButton;

	private ILoginStateChangedListener mListener;
	private IWalletButtonStateChangedListener mWalletListener;
	private IWalletCouponListener mWalletCouponListener;

	private boolean mWasLoggedIn = false;

	private long mRefreshedUserTime = 0L;

	public static CheckoutLoginButtonsFragment newInstance(LineOfBusiness lob) {
		CheckoutLoginButtonsFragment frag = new CheckoutLoginButtonsFragment();
		frag.setLob(lob);
		return frag;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, ILoginStateChangedListener.class, false);
		mWalletListener = Ui.findFragmentListener(this, IWalletButtonStateChangedListener.class, false);
		mWalletCouponListener = Ui.findFragmentListener(this, IWalletCouponListener.class, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Disable Google Wallet on non-merchant hotels
		if (getLob() == LineOfBusiness.HOTELS && (!Db.getTripBucket().getHotel().getProperty().isMerchant() || Db
			.getTripBucket().getHotel().getRate().isPayLater())) {
			disableGoogleWallet();
		}

		if (savedInstanceState != null) {
			mRefreshedUserTime = savedInstanceState.getLong(INSTANCE_REFRESHED_USER_TIME);
			mWasLoggedIn = savedInstanceState.getBoolean(INSTANCE_WAS_LOGGED_IN);
			if (savedInstanceState.containsKey(STATE_LOB)) {
				LineOfBusiness lob = LineOfBusiness.valueOf(savedInstanceState.getString(STATE_LOB));
				if (lob != null) {
					setLob(lob);
				}
			}
		}
		else {
			// Reset Google Wallet state each time we get here
			Db.clearGoogleWallet();
			mWasLoggedIn = User.isLoggedIn(getActivity());

			// TODO: Dirty bandaid. Make sure there are some traveler details here after clearing out
			// google wallet. We should probably TabletCheckoutFormsFragment.buildCheckoutForm()
			// after this at some point.
			if (Db.getTravelers().size() == 0) {
				BookingInfoUtils.populateTravelerData(getLob());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_checkout_login_buttons, null);
		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountButton.setListener(this);
		mWalletButton = Ui.findView(v, R.id.wallet_button_layout);
		mWalletButton.setOnClickListener(mWalletButtonClickListener);

		bind();
		mAccountButton.setVisibility(
			ProductFlavorFeatureConfiguration.getInstance().isSigninEnabled() ? View.VISIBLE : View.GONE);

		return v;

	}

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}

		//We disable this for sign in, but when the user comes back it should be enabled.
		mAccountButton.setEnabled(true);

		testForLoginStateChange();
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(INSTANCE_REFRESHED_USER_TIME, mRefreshedUserTime);
		outState.putBoolean(INSTANCE_WAS_LOGGED_IN, mWasLoggedIn);
		outState.putString(STATE_LOB, mLob.name());
	}

	public void testForLoginStateChange() {
		Context context = getActivity();
		boolean loggedIn = User.isLoggedIn(context);
		if (loggedIn != mWasLoggedIn) {
			BookingInfoUtils.populateTravelerDataFromUser(context, getLob());
			BookingInfoUtils.populatePaymentDataFromUser(context, getLob());
			Db.getWorkingBillingInfoManager().getWorkingBillingInfo()
				.setStoredCard(Db.getBillingInfo().getStoredCard());
			Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
			if (mListener != null) {
				mListener.onLoginStateChanged();
			}

			mWasLoggedIn = loggedIn;
		}
	}

	public void bind() {
		if (mAccountButton != null) {
			refreshAccountButtonState();
		}
		if (mWalletButton != null) {
			onlyUpdateWalletViewVisibilities();
		}
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
					if (getLob() == LineOfBusiness.FLIGHTS) {
						Db.getTripBucket().getFlight().getFlightTrip().setRewardsPoints("");
					}
					else {
						Db.getTripBucket().getHotel().getCreateTripResponse().setRewardsPoints("");
					}
				}
				mAccountButton.bind(false, true, Db.getUser(), getLob());
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, getLob());
			}
		}
		else {
			mAccountButton.bind(false, false, null, getLob());
		}
	}

	@Override
	public void accountLoginClicked() {
		if (mAccountButton.isEnabled()) {
			mAccountButton.setEnabled(false);

			Bundle args = null;
			if (getLob() == LineOfBusiness.FLIGHTS) {
				String tripId = Db.getTripBucket().getFlight().getItinerary().getTripId();
				args = AccountLibActivity.createArgumentsBundle(getLob(), new UserToTripAssocLoginExtender(
					tripId));
				OmnitureTracking.trackPageLoadFlightLogin();
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.HOTELS, null);
				OmnitureTracking.trackPageLoadHotelsLogin();
			}

			User.signIn(getActivity(), args);
		}
	}

	@Override
	public void accountLogoutClicked() {
		LoginConfirmLogoutDialogFragment df = new LoginConfirmLogoutDialogFragment();
		df.show(getChildFragmentManager(), LoginConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUserTime = 0L;

		// Sign out user
		int travCount = Db.getTravelers().size();
		User.signOut(getActivity());

		//Signing out the user clears the travelers. Lets re-add empty ones for now...
		for (int i = 0; i < travCount; i++) {
			Db.getTravelers().add(new Traveler());
		}

		// Update UI
		mAccountButton.bind(false, false, null, getLob());

		testForLoginStateChange();
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), getLob());
		mRefreshedUserTime = System.currentTimeMillis();

		testForLoginStateChange();
	}

	/*
	 * ACCOUNT REFRESH DOWNLOAD
	 */

	private final Download<SignInResponse> mRefreshUserDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REFRESH_USER, services);
			//Why flights AND hotels? Because the api will return blank for loyaltyMembershipNumber on flights
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

	/*
	 * Google Wallet
	 */

	private OnClickListener mWalletButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			buyWithGoogleWallet();
		}
	};

	@Override
	protected Money getEstimatedTotal() {
		LineOfBusiness lob = getLob();
		Money estimatedTotal = null;
		if (lob == LineOfBusiness.FLIGHTS) {
			estimatedTotal = Db.getTripBucket().getFlight().getFlightTrip().getTotalFare();
		}
		else if (lob == LineOfBusiness.HOTELS) {
			estimatedTotal = Db.getTripBucket().getHotel().getRate().getTotalAmountAfterTax();
		}
		return estimatedTotal;
	}

	@Override
	protected void modifyMaskedWalletBuilder(MaskedWalletRequest.Builder builder) {
		LineOfBusiness lob = getLob();
		if (lob == LineOfBusiness.FLIGHTS) {
			modifyFlightsMaskedWalletBuilder(builder);
		}
		else if (lob == LineOfBusiness.HOTELS) {
			modifyHotelsMaskedWalletBuilder(builder);
		}
	}

	private void modifyFlightsMaskedWalletBuilder(MaskedWalletRequest.Builder builder) {
		builder.setCart(WalletUtils.buildFlightCart(getActivity()));
		builder.setPhoneNumberRequired(true);
	}

	private void modifyHotelsMaskedWalletBuilder(MaskedWalletRequest.Builder builder) {
		builder.setCart(WalletUtils.buildHotelCart(getActivity()));
		builder.setPhoneNumberRequired(true);
		builder.setUseMinimalBillingAddress(true);
	}

	@Override
	protected void onMaskedWalletFullyLoaded(boolean fromPreauth) {
		BillingInfo billingInfo = Db.getBillingInfo();
		BookingInfoUtils.populateTravelerData(getLob());

		MaskedWallet maskedWallet = Db.getMaskedWallet();

		// If we don't currently have traveler data, and the wallet gives us sufficient data, use it
		// NOTE: At the moment we are *guaranteed* not to get sufficient data, but there's no reason
		// not to hope someday we will get it!
		Traveler traveler = WalletUtils.addWalletAsTraveler(getActivity(), maskedWallet);

		// If the traveler is null, just set it to our primary user.
		if (traveler == null) {
			traveler = Db.getTravelers().get(0);
		}

		BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), traveler, getLob());

		// Bind credit card data, but only if they explicitly clicked "buy with wallet" or they have
		// no existing credit card info entered
		if (!fromPreauth || (TextUtils.isEmpty(billingInfo.getNumber()) && !billingInfo.hasStoredCard())) {
			StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
			if (currentCC != null) {
				BookingInfoUtils.resetPreviousCreditCardSelectState(getActivity(), currentCC);
			}
			WalletUtils.bindWalletToBillingInfo(maskedWallet, billingInfo);
		}

		if (WalletUtils.offerGoogleWalletCoupon(getActivity()) && getLob() == LineOfBusiness.HOTELS) {
			mWalletCouponListener.applyWalletCoupon();
		}
		Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(billingInfo);
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		bind();
		refreshAccountButtonState();
		updateWalletViewVisibilities();
	}

	@Override
	protected void updateWalletViewVisibilities() {
		boolean enabledButtons = onlyUpdateWalletViewVisibilities();
		mWalletListener.onWalletButtonStateChanged(enabledButtons);
	}

	private boolean onlyUpdateWalletViewVisibilities() {
		boolean showWalletButton = showWalletButton() && canUseGoogleWallet();
		boolean isWalletLoading = isWalletLoading();

		mWalletButton.setVisibility(showWalletButton ? View.VISIBLE : View.GONE);
		mWalletButton.setEnabled(!isWalletLoading);
		mWalletButton.setPromoVisible(ProductFlavorFeatureConfiguration.getInstance().isGoogleWalletPromoEnabled()
			&& getLob() == LineOfBusiness.HOTELS);

		// Enable buttons if we're either not showing the wallet button or we're not loading a masked wallet
		boolean enableButtons = !showWalletButton || !isWalletLoading;
		mAccountButton.setEnabled(enableButtons);

		return enableButtons;
	}

	/*
	 * ILOBable
	 */

	public void setLob(LineOfBusiness lob) {
		if (lob != mLob) {
			mLob = lob;
		}
	}

	public LineOfBusiness getLob() {
		return mLob;
	}

	private boolean canUseGoogleWallet() {
		TripBucketItem item;
		if (mLob == LineOfBusiness.HOTELS) {
			item = Db.getTripBucket().getHotel();
		}
		else if (mLob == LineOfBusiness.FLIGHTS) {
			item = Db.getTripBucket().getFlight();
		}
		else {
			throw new RuntimeException("canUseGoogleWallet() can only evaluate hotel and flight items!");
		}
		return item.isPaymentTypeSupported(PaymentType.WALLET_GOOGLE);
	}

	public interface IWalletCouponListener {
		void applyWalletCoupon();
	}
}
