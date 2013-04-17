package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

/**
 * This is an View-less Fragment which performs a hotel booking.
 * 
 * It is separated into its own Fragment so that it can use the lifecycle on its own (and
 * can be derived from a Fragment, which will help with Google Wallet compatibility)
 */
public class HotelBookingFragment extends FullWalletFragment {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String BOOKING_DOWNLOAD_KEY = "com.expedia.bookings.hotel.checkout";

	private HotelBookingFragmentListener mListener;

	// Sometimes we want to display dialogs but can't yet; in that case, defer until onResume() 
	private boolean mCanModifyFragmentStack;

	private boolean mDoBookingOnResume;

	// If we need to defer handling till later
	private int mGoogleWalletErrorCode;

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

		mCanModifyFragmentStack = true;
		if (mGoogleWalletErrorCode != 0) {
			handleError(mGoogleWalletErrorCode);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mCanModifyFragmentStack = false;
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(BOOKING_DOWNLOAD_KEY);
	}

	public boolean isBooking() {
		return BackgroundDownloader.getInstance().isDownloading(BOOKING_DOWNLOAD_KEY);
	}

	public void doBooking() {
		if (willBookViaGoogleWallet()) {
			confirmBookingWithGoogleWallet();
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

			notifyWalletTransactionStatus(response);

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
	// Google Wallet

	public boolean willBookViaGoogleWallet() {
		StoredCreditCard scc = Db.getBillingInfo().getStoredCard();
		return scc != null && scc.isGoogleWallet();
	}

	// FullWalletFragment

	@Override
	protected FullWalletRequest getFullWalletRequest() {
		Property property = Db.getSelectedProperty();
		Rate rate = Db.getSelectedRate();
		Money totalBeforeTax = rate.getTotalAmountBeforeTax();
		Money surcharges = rate.getTotalSurcharge();
		Money totalAfterTax = rate.getTotalAmountAfterTax();

		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(getGoogleWalletTransactionId());

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

		walletRequestBuilder.setCart(cartBuilder.build());
		return walletRequestBuilder.build();
	}

	@Override
	protected String getGoogleWalletTransactionId() {
		return Db.getBillingInfo().getGoogleWalletTransactionId();
	}

	@Override
	protected void onFullWalletLoaded(FullWallet wallet) {
		WalletUtils.bindWalletToBillingInfo(wallet, Db.getBillingInfo());

		startBookingDownload();
	}

	// Error handling

	@Override
	protected void handleError(int errorCode) {
		super.handleError(errorCode);

		if (mCanModifyFragmentStack) {
			mGoogleWalletErrorCode = 0;
			simulateError(errorCode);
		}
		else {
			mGoogleWalletErrorCode = errorCode;
		}
	}

	private void simulateError(int errorCode) {
		BookingResponse response = new BookingResponse();
		ServerError error = new ServerError();
		error.setCode("GOOGLE_WALLET_ERROR");
		response.addError(error);
		mListener.onBookingResponse(response);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelBookingFragmentListener {
		public void onStartBooking();

		public void onBookingResponse(BookingResponse results);
	}
}
