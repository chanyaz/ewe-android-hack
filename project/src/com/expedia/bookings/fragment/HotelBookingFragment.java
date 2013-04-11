package com.expedia.bookings.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

/**
 * This is an View-less Fragment which performs a hotel booking.
 * 
 * It is separated into its own Fragment so that it can use the lifecycle on its own (and
 * can be derived from a Fragment, which will help with Google Wallet compatibility)
 */
public class HotelBookingFragment extends Fragment {

	public static final String TAG = HotelBookingFragment.class.toString();

	public static final String BOOKING_DOWNLOAD_KEY = "com.expedia.bookings.hotel.checkout";

	private HotelBookingFragmentListener mListener;

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

			/*
			 * TODO: Re-implement notification system
			StoredCreditCard scc = Db.getBillingInfo().getStoredCard();
			if (scc != null && scc.isGoogleWallet()) {
				// While still in the bg, notify Google of what happened when we tried to book
				NotifyTransactionStatusRequest.Builder notifyBuilder = NotifyTransactionStatusRequest.newBuilder();
				notifyBuilder.setGoogleTransactionId(Db.getBillingInfo().getGoogleWalletTransactionId());
				if (response == null || response.hasErrors()) {
					// TODO: MORE SPECIFIC ERRORS
					notifyBuilder.setStatus(NotifyTransactionStatusRequest.Status.Error.UNKNOWN);
				}
				else {
					notifyBuilder.setStatus(NotifyTransactionStatusRequest.Status.SUCCESS);
				}

				mWalletClient.notifyTransactionStatus(notifyBuilder.build());
			}
			*/

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
	// Listener

	public interface HotelBookingFragmentListener {
		public void onStartBooking();

		public void onBookingResponse(BookingResponse results);
	}
}
