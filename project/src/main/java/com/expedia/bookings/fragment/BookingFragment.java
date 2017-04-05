package com.expedia.bookings.fragment;

import java.math.BigDecimal;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.dialog.BirthDateInvalidDialog;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

public abstract class BookingFragment<T extends Response> extends Fragment {

	private String mDownloadKey;

	// Sometimes we want to display dialogs but can't yet; in that case, defer until onResume()
	private boolean mCanModifyFragmentStack;

	private boolean mDoBookingOnResume;

	private static final String UNHANDLED_ERROR_DIALOG_TAG = "unhandledOrNoResultsErrorDialog";

	//////////////////////////////////////////////////////////////////////////
	// Abstractions/overrideables related to only booking

	public abstract String getBookingDownloadKey();

	public abstract Download<T> getBookingDownload();

	public abstract void clearBookingResponse();

	public abstract Class<T> getBookingResponseClass();

	// Use this method if we need to gather/prepare more information before calling booking download.
	public abstract void doBookingPrep();

	public abstract void handleBookingErrorResponse(Response response);

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Ensure a consistent download key; only grab it once
		mDownloadKey = getBookingDownloadKey();

	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(mDownloadKey)) {
			bd.registerDownloadCallback(mDownloadKey, mCallback);
		}
		else if (mDoBookingOnResume) {
			doBooking();
		}

		mCanModifyFragmentStack = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mCanModifyFragmentStack = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
		BackgroundDownloader.getInstance().unregisterDownloadCallback(mDownloadKey);
	}

	//////////////////////////////////////////////////////////////////////////
	// Booking download

	public boolean isBooking() {
		return BackgroundDownloader.getInstance().isDownloading(mDownloadKey);
	}

	public void doBooking() {
		if (!isAdded()) {
			mDoBookingOnResume = true;
		}
		else {
			startBookingProcess();
		}
	}

	private void startBookingProcess() {
		Events.post(new Events.BookingDownloadStarted());
		doBookingPrep();
	}

	protected void startBookingDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(mDownloadKey)) {
			clearBookingResponse();

			bd.startDownload(mDownloadKey, getBookingDownload(), mCallback);
		}
	}

	private OnDownloadComplete<T> mCallback = new OnDownloadComplete<T>() {
		@Override
		public void onDownload(T results) {
			Events.post(new Events.BookingDownloadResponse(results));
		}
	};

	public void handleBookingErrorResponse(Response response, LineOfBusiness lob) {
		/*
		 *  If response is null let's just show a popup message
		 *  and have users pick from "Retry" "Call Customer Support" or "Cancel" options.
		 */
		if (response == null) {
			showBookingUnhandledErrorDialog(lob);
			return;
		}

		List<ServerError> errors = response.getErrors();

		boolean hasPaymentError = false;
		boolean hasPhoneError = false;
		boolean hasPostalCodeError = false;
		boolean hasFlightMinorError = false;
		boolean hasNameOnCardMismatchError = false;
		boolean hasPassengerCategoryError = false;

		// Log all errors, in case we need to see them
		for (int a = 0; a < errors.size(); a++) {
			ServerError error = errors.get(a);
			Log.v("SERVER ERROR " + a + ": " + error.toJson().toString());

			String field = error.getExtra("field");
			if (TextUtils.isEmpty(field)) {
				continue;
			}

			if (field.equals("creditCardNumber")) {
				hasPaymentError = true;
			}
			else if (field.equals("expirationDate")) {
				hasPaymentError = true;
			}
			else if (field.equals("cvv")) {
				hasPaymentError = true;
			}
			else if (field.equals("cardLimitExceeded")) {
				hasPaymentError = true;
			}
			else if (field.equals("phone")) {
				hasPhoneError = true;
			}
			else if (field.equals("postalCode")) {
				hasPostalCodeError = true;
			}
			else if (field.equals("nameOnCard")) {
				hasNameOnCardMismatchError = true;
			}
			else if (field.equals("mainFlightPassenger.birthDate")) {
				hasPassengerCategoryError = true;
			}
			else if (field.matches("associatedFlightPassengers\\[(\\d+)\\]\\.birthDate")) {
				hasPassengerCategoryError = true;
			}
		}

		// We make the assumption that if we get an error we can handle in a special manner
		// that it will be the first one.  If there are multiple errors, we assume right
		// now that it will require a generic response.
		ServerError firstError = errors.get(0);
		// Check for special errors; return if we handled it
		switch (firstError.getErrorCode()) {
		case PRICE_CHANGE:
			// We get this error for ONLY flights, but we need to be extra defensive
			// due to issues like #3383
			if (lob == LineOfBusiness.FLIGHTS) {
				FlightTrip currentOffer = Db.getTripBucket().getFlight().getFlightTrip();
				FlightTrip newOffer = ((FlightCheckoutResponse) response).getNewOffer();

				// If the debug setting is made to fake a price change, then fake the price here too
				// This is sort of a second price change, to help figure out testing when we have obfees and a price change...
				if (BuildConfig.DEBUG) {
					String val = SettingUtils.get(getActivity(),
						getString(R.string.preference_fake_flight_price_change),
						getString(R.string.preference_fake_price_change_default));
					currentOffer.getTotalPrice().add(new BigDecimal(val));
					newOffer.getTotalPrice().add(new BigDecimal(val));
				}

				FlightPriceChangeDialogFragment fragment = FlightPriceChangeDialogFragment.newInstance(currentOffer, newOffer);
				fragment.show(getFragmentManager(), FlightPriceChangeDialogFragment.TAG);
				OmnitureTracking.trackErrorPageLoadFlightPriceChangeTicket();
				return;
			}
			break;
		case BOOKING_FAILED: {
			if (firstError.getDiagnosticFullText().contains("INVALID_CCNUMBER")) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.error_invalid_card_number), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_INVALID_CC);
				frag.show(getFragmentManager(), "badCcNumberDialog");
				return;
			}
			break;
		}
		case PAYMENT_FAILED:
		case INVALID_INPUT: {

			if (firstError.getErrorCode() == ErrorCode.PAYMENT_FAILED && lob == LineOfBusiness.FLIGHTS) {
				OmnitureTracking.trackErrorPageLoadFlightPaymentFailed();
			}

			if (hasPaymentError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.e3_error_checkout_payment_failed), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_INVALID_PAYMENT);
				frag.show(getFragmentManager(), "badPaymentDialog");

				if (lob == LineOfBusiness.FLIGHTS) {
					OmnitureTracking.trackErrorPageLoadFlightIncorrectCVV();
				}
				return;
			}
			else if (hasPhoneError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.ean_error_invalid_phone_number), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_INVALID_PHONENUMBER);
				frag.show(getFragmentManager(), "badPhoneNumberDialog");
				return;
			}
			else if (hasPostalCodeError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.invalid_postal_code), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_INVALID_POSTALCODE);
				frag.show(getFragmentManager(), "badPostalCodeDialog");
				return;
			}
			else if (hasNameOnCardMismatchError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.error_name_on_card_mismatch), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_NAME_ONCARD_MISMATCH);
				frag.show(getFragmentManager(), "nameOnCardMisMatchDialog");
				return;
			}
			// 1643: Handle an odd API response. This is probably due to the transition
			// to being anble to handle booking tickets for minors. We shouldn't need this in the future.
			else if (hasFlightMinorError) {
				DialogFragment frag = SimpleCallbackDialogFragment.newInstance(null,
					getString(R.string.error_booking_with_minor), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_INVALID_MINOR);
				frag.show(getFragmentManager(), "cannotBookWithMinorDialog");
				return;
			}
			else if (hasPassengerCategoryError) {
				DialogFragment frag = BirthDateInvalidDialog.newInstance(false);
				frag.show(getFragmentManager(), "passengerCategoriesDoNotMatch");
				return;
			}
			break;
		}
		case TRIP_ALREADY_BOOKED:
			// If the trip was already booked, just act like everything is fine and launch the confirmation screen.
			Events.post(new Events.BookingResponseErrorTripBooked());
			return;
		case FLIGHT_SOLD_OUT:
			showBookingUnavailableErrorDialog(lob);
			OmnitureTracking.trackErrorPageLoadFlightSoldOut();
		case SESSION_TIMEOUT:
			showBookingUnavailableErrorDialog(lob);
			if (lob == LineOfBusiness.FLIGHTS) {
				OmnitureTracking.trackErrorPageLoadFlightSearchExpired();
			}
			return;
		case CANNOT_BOOK_WITH_MINOR: {
			DialogFragment frag = SimpleCallbackDialogFragment
				.newInstance(null,
					getString(R.string.error_booking_with_minor), getString(R.string.ok),
					SimpleCallbackDialogFragment.CODE_MINOR);
			frag.show(getFragmentManager(), "cannotBookWithMinorDialog");
			return;
		}
		default:
			if (lob == LineOfBusiness.FLIGHTS) {
				OmnitureTracking.trackErrorPageLoadFlightCheckout();
			}
			break;
		}

		// At this point, we haven't handled the error - use a generic response
		showBookingUnhandledErrorDialog(lob);
	}

	private void showBookingUnhandledErrorDialog(LineOfBusiness lob) {
		String caseNumber;
		if (lob == LineOfBusiness.FLIGHTS) {
			caseNumber = Db.getTripBucket().getFlight().getFlightTrip()
				.getItineraryNumber();
		}
		else {
			caseNumber = "";
		}
		if (getFragmentManager().findFragmentByTag(UNHANDLED_ERROR_DIALOG_TAG) == null) {
			DialogFragment df = UnhandledErrorDialogFragment.newInstance(caseNumber);
			df.show(getFragmentManager(), UNHANDLED_ERROR_DIALOG_TAG);
		}
	}

	private void showBookingUnavailableErrorDialog(LineOfBusiness lob) {
		boolean isPlural = false;
		if (lob == LineOfBusiness.FLIGHTS) {
			isPlural = (Db.getTripBucket().getFlight().getFlightSearchParams().getQueryLegCount() != 1);
		}
		BookingUnavailableDialogFragment df = BookingUnavailableDialogFragment.newInstance(isPlural, lob);
		df.show(getFragmentManager(), "unavailableErrorDialog");
	}

}
