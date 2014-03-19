package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.interfaces.ILOBable;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;
import com.expedia.bookings.widget.WalletButton;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import static android.view.View.OnClickListener;

public class CheckoutLoginButtonsFragment extends Fragment implements AccountButtonClickListener, ConfirmLogoutDialogFragment.DoLogoutListener, ILOBable {

	private static final String INSTANCE_REFRESHED_USER_TIME = "INSTANCE_REFRESHED_USER";
	private static final String INSTANCE_WAS_LOGGED_IN = "INSTANCE_WAS_LOGGED_IN";
	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	// LOB
	private static final String STATE_LOB = "STATE_LOB";
	private LineOfBusiness mLob;

	public interface ILoginStateChangedListener {
		public void onLoginStateChanged();
	}

	private AccountButton mAccountButton;
	private WalletButton mWalletButton;

	private ILoginStateChangedListener mListener;
	private boolean mWasLoggedIn = false;

	private long mRefreshedUserTime = 0L;

	public static CheckoutLoginButtonsFragment newInstance(LineOfBusiness lob) {
		CheckoutLoginButtonsFragment frag = new CheckoutLoginButtonsFragment();
		frag.setLob(lob);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, ILoginStateChangedListener.class, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_checkout_login_buttons, null);

		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountButton.setListener(this);

		mWalletButton = Ui.findView(v, R.id.wallet_button_layout);
		//TODO: SET UP WALLET STUFF
		mWalletButton.setOnClickListener(mWalletButtonClickListener);

		bind();

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
		if (User.isLoggedIn(getActivity()) != mWasLoggedIn) {
			BookingInfoUtils.populateTravelerDataFromUser(getActivity(), getLob());
			BookingInfoUtils.populatePaymentDataFromUser(getActivity(), getLob());
			if (mListener != null) {
				mListener.onLoginStateChanged();
			}

			mWasLoggedIn = User.isLoggedIn(getActivity());
		}
	}

	public void bind() {
		if (mAccountButton != null) {
			refreshAccountButtonState();
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
				int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval);
				if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
					Log.d("Refreshing user profile...");

					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(KEY_REFRESH_USER)) {
						bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
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
				String itinNum = Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber();
				String tripId = Db.getItinerary(itinNum).getTripId();
				args = LoginActivity.createArgumentsBundle(getLob(), new UserToTripAssocLoginExtender(
					tripId));
				OmnitureTracking.trackPageLoadFlightLogin(getActivity());
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				args = LoginActivity.createArgumentsBundle(LineOfBusiness.HOTELS, null);
				OmnitureTracking.trackPageLoadHotelsLogin(getActivity());
			}

			User.signIn(getActivity(), args);
		}
	}

	@Override
	public void accountLogoutClicked() {
		ConfirmLogoutDialogFragment df = new ConfirmLogoutDialogFragment();
		df.show(getChildFragmentManager(), ConfirmLogoutDialogFragment.TAG);
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
			Ui.showToast(getActivity(), "We are buying some GOOGLE WALLET STUFF");
		}
	};

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


}
