package com.expedia.bookings.fragment;

import android.os.Bundle;
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
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;
import com.expedia.bookings.widget.WalletButton;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class CheckoutLoginButtonsFragment extends LobableFragment implements AccountButtonClickListener, ConfirmLogoutDialogFragment.DoLogoutListener {

	private static final String INSTANCE_REFRESHED_USER_TIME = "INSTANCE_REFRESHED_USER";
	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	private AccountButton mAccountButton;
	private WalletButton mWalletButton;

	private long mRefreshedUserTime = 0L;

	public static CheckoutLoginButtonsFragment newInstance(LineOfBusiness lob) {
		CheckoutLoginButtonsFragment frag = new CheckoutLoginButtonsFragment();
		frag.setLob(lob);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRefreshedUserTime = savedInstanceState.getLong(INSTANCE_REFRESHED_USER_TIME);
		}
		else {
			// Reset Google Wallet state each time we get here
			Db.clearGoogleWallet();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_checkout_login_buttons, null);

		if (savedInstanceState != null) {
			mRefreshedUserTime = savedInstanceState.getLong(INSTANCE_REFRESHED_USER_TIME);
		}

		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountButton.setListener(this);

		mWalletButton = Ui.findView(v, R.id.wallet_button_layout);
		//TODO: SET UP WALLET STUFF

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

	public void bind() {
		if (mAccountButton != null) {
			refreshAccountButtonState();
		}
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		// TODO Auto-generated method stub

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
				mAccountButton.bind(false, true, Db.getUser(), true);
			}
			else {
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, true);
			}
		}
		else {
			mAccountButton.bind(false, false, null, true);
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
		mAccountButton.bind(false, false, null, true);
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);
		mRefreshedUserTime = System.currentTimeMillis();
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

}
