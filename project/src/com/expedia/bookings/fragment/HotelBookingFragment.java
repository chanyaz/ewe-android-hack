package com.expedia.bookings.fragment;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.ProxyCard;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * This is an View-less Fragment which performs a hotel booking.
 * 
 * It is separated into its own Fragment so that it can use the lifecycle on its own (and
 * can be derived from a Fragment, which will help with Google Wallet compatibility)
 */
public class HotelBookingFragment extends WalletFragment {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String BOOKING_DOWNLOAD_KEY = "com.expedia.bookings.hotel.checkout";

	private static final String STATE_HAS_TRIED_BOOKING = "STATE_HAS_TRIED_BOOKING";

	private HotelBookingFragmentListener mListener;

	// If you're booking using google wallet, we don't want to send multiple requests
	private boolean mHasTriedBookingWithGoogleWallet;

	private boolean mDoBookingOnResume;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mHasTriedBookingWithGoogleWallet = savedInstanceState.getBoolean(STATE_HAS_TRIED_BOOKING);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelBookingFragmentListener)) {
			throw new RuntimeException("HotelBookingFragment Activity must implement listener!");
		}

		mListener = (HotelBookingFragmentListener) activity;
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(BOOKING_DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(BOOKING_DOWNLOAD_KEY, mCallback);
		}
		else if (mDoBookingOnResume) {
			doBooking();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(STATE_HAS_TRIED_BOOKING, mHasTriedBookingWithGoogleWallet);
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(BOOKING_DOWNLOAD_KEY);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		int errorCode = -1;
		if (data != null) {
			errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
		}

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			if (resultCode == Activity.RESULT_OK) {
				mWalletClient.connect();
			}
			else {
				handleUnrecoverableGoogleWalletError(errorCode);
			}
			break;
		case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
			switch (resultCode) {
			case Activity.RESULT_OK:
				if (data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
					onFullWalletReceived((FullWallet) data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET));
				}
				else if (data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
					Db.setMaskedWallet((MaskedWallet) data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET));
					getFullWallet();
				}
				break;
			case Activity.RESULT_CANCELED:
				Log.w("Full wallet request: received RESULT_CANCELED; trying again");
				getFullWallet();
				break;
			default:
				handleError(errorCode);
				break;
			}
			break;
		}
	}

	public boolean willBookViaGoogleWallet() {
		StoredCreditCard scc = Db.getBillingInfo().getStoredCard();
		return scc != null && scc.isGoogleWallet();
	}

	public boolean isBooking() {
		return BackgroundDownloader.getInstance().isDownloading(BOOKING_DOWNLOAD_KEY);
	}

	public void doBooking() {
		if (willBookViaGoogleWallet()) {
			if (mWalletClient != null && mWalletClient.isConnected() && !mHasTriedBookingWithGoogleWallet) {
				getFullWallet();
			}
			else {
				mHandleFullWalletWhenReady = true;
			}
		}
		else {
			if (!isAdded()) {
				mDoBookingOnResume = true;
			}
			else {
				startBookingDownload();
			}
		}
	}

	private void startBookingDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(BOOKING_DOWNLOAD_KEY)) {
			mListener.onStartBooking();

			// Clear current results (if any)
			Db.setBookingResponse(null);

			bd.startDownload(BOOKING_DOWNLOAD_KEY, mDownload, mCallback);
		}
	}

	private Download<BookingResponse> mDownload = new Download<BookingResponse>() {
		@Override
		public BookingResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			String userId = null;
			String tripId = null;
			Long tuid = null;

			if (Db.getCreateTripResponse() != null) {
				tripId = Db.getCreateTripResponse().getTripId();
				userId = Db.getCreateTripResponse().getUserId();
			}

			if (Db.getUser() != null) {
				tuid = Db.getUser().getPrimaryTraveler().getTuid();
			}

			BookingResponse response = services.reservation(Db.getSearchParams(), Db.getSelectedProperty(),
					Db.getSelectedRate(), Db.getBillingInfo(), tripId, userId, tuid);

			if (willBookViaGoogleWallet()) {
				// While still in the bg, notify Google of what happened when we tried to book
				int status = WalletUtils.getStatus(response);
				if (status != 0) {
					NotifyTransactionStatusRequest.Builder notifyBuilder = NotifyTransactionStatusRequest.newBuilder();
					notifyBuilder.setGoogleTransactionId(Db.getBillingInfo().getGoogleWalletTransactionId());
					notifyBuilder.setStatus(status);
					mWalletClient.notifyTransactionStatus(notifyBuilder.build());
				}
			}

			return response;
		}
	};

	private OnDownloadComplete<BookingResponse> mCallback = new OnDownloadComplete<BookingResponse>() {
		@Override
		public void onDownload(BookingResponse results) {
			mListener.onBookingResponse(results);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Google Wallet bookings

	private void getFullWallet() {
		// To create a CVV challenge, we need to do two things:
		// 1. Greatly increase the price (but not above the limit)
		// 2. Add the cvv_challenge line item
		boolean createCvvChallenge = !AndroidUtils.isRelease(getActivity())
				&& SettingUtils.get(getActivity(), getString(R.string.preference_google_wallet_cvv_challenge), false);

		// Build the full wallet request
		Property property = Db.getSelectedProperty();
		BillingInfo billingInfo = Db.getBillingInfo();
		Rate rate = Db.getSelectedRate();
		Money totalBeforeTax = rate.getTotalAmountBeforeTax();
		Money surcharges = rate.getTotalSurcharge();
		Money totalAfterTax = rate.getTotalAmountAfterTax();

		if (createCvvChallenge) {
			// Don't want to reset the existing prices - just up them before we send
			totalBeforeTax = new Money(totalBeforeTax);
			totalBeforeTax.setAmount(new BigDecimal(WalletFragment.MAX_TRANSACTION_CHARGE));
			surcharges = new Money(surcharges);
			surcharges.setAmount(BigDecimal.ZERO);
			totalAfterTax = new Money(totalAfterTax);
			totalAfterTax.setAmount(new BigDecimal(WalletFragment.MAX_TRANSACTION_CHARGE));
		}

		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(billingInfo.getGoogleWalletTransactionId());

		Cart.Builder cartBuilder = Cart.newBuilder();
		cartBuilder.setCurrencyCode(totalAfterTax.getCurrency());
		cartBuilder.setTotalPrice(totalAfterTax.getAmount().toPlainString());

		LineItem.Builder beforeTaxBuilder = LineItem.newBuilder();
		beforeTaxBuilder.setCurrencyCode(totalBeforeTax.getCurrency());
		beforeTaxBuilder.setDescription(property.getName());
		beforeTaxBuilder.setRole(LineItem.Role.REGULAR);
		beforeTaxBuilder.setTotalPrice(totalBeforeTax.getAmount().toPlainString());
		cartBuilder.addLineItem(beforeTaxBuilder.build());

		LineItem.Builder taxesBuilder = LineItem.newBuilder();
		taxesBuilder.setCurrencyCode(surcharges.getCurrency());
		taxesBuilder.setDescription(getString(R.string.taxes_and_fees));
		taxesBuilder.setRole(LineItem.Role.TAX);
		taxesBuilder.setTotalPrice(surcharges.getAmount().toPlainString());
		cartBuilder.addLineItem(taxesBuilder.build());

		if (createCvvChallenge) {
			LineItem.Builder cvvChallengeBuilder = LineItem.newBuilder();
			cvvChallengeBuilder.setCurrencyCode(totalBeforeTax.getCurrency());
			cvvChallengeBuilder.setDescription("cvv_challenge");
			cvvChallengeBuilder.setRole(LineItem.Role.REGULAR);
			cvvChallengeBuilder.setTotalPrice("0.00");
			cartBuilder.addLineItem(cvvChallengeBuilder.build());
		}

		walletRequestBuilder.setCart(cartBuilder.build());
		FullWalletRequest fwRequest = walletRequestBuilder.build();

		// Load the full wallet
		mWalletClient.loadFullWallet(fwRequest, this);
	}

	private void onFullWalletReceived(FullWallet wallet) {
		// Fill out the billing info with proxy data
		BillingInfo billingInfo = Db.getBillingInfo();

		ProxyCard proxyCard = wallet.getProxyCard();
		billingInfo.setNumber(proxyCard.getPan());
		billingInfo.setSecurityCode(proxyCard.getCvn());
		billingInfo.setExpirationDate(new GregorianCalendar(proxyCard.getExpirationYear(), proxyCard
				.getExpirationMonth() - 1, 1));
		billingInfo.setNameOnCard(wallet.getBillingAddress().getName());

		// Start the download
		mHasTriedBookingWithGoogleWallet = true;
		startBookingDownload();
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);

		if (!isBooking() && mHandleFullWalletWhenReady && !mHasTriedBookingWithGoogleWallet) {
			getFullWallet();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFullWalletLoadedListener

	@Override
	public void onFullWalletLoaded(ConnectionResult status, FullWallet wallet) {
		super.onFullWalletLoaded(status, wallet);

		if (status.isSuccess()) {
			// User has pre-authorized the app
			onFullWalletReceived(wallet);
		}
		else if (status.hasResolution()) {
			try {
				status.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
			}
			catch (SendIntentException e) {
				// Retry loading the full wallet
				mProgressDialog.show();
				mHandleFullWalletWhenReady = true;
				getFullWallet();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelBookingFragmentListener {
		public void onStartBooking();

		public void onBookingResponse(BookingResponse results);
	}
}
