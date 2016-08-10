package com.expedia.bookings.fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.FragmentModificationSafeLock;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.util.SettingUtils;

public class FlightBookingFragment extends BookingFragment<FlightCheckoutResponse> implements
		RetryErrorDialogFragmentListener {

	public static final String TAG = FlightBookingFragment.class.toString();

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flight.checkout";
	public static final String KEY_DOWNLOAD_CREATE_TRIP = "KEY_DOWNLOAD_FLIGHT_CREATE_TRIP";

	private static final String RETRY_CREATE_TRIP_DIALOG = "RETRY_FLIGHT_CREATE_TRIP_DIALOG";
	private static final String FLIGHT_UNAVAILABLE_DIALOG = "FLIGHT_UNAVAILABLE_DIALOG";

	private FragmentModificationSafeLock mFragmentModLock = new FragmentModificationSafeLock();

	private FlightTrip mFlightTrip;

	public enum FlightBookingState {
		DEFAULT,
		CREATE_TRIP
	}

	public static FlightBookingFragment newInstance() {
		return new FlightBookingFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return;
		}

		this.mFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
	}

	@Override
	public String getBookingDownloadKey() {
		return DOWNLOAD_KEY;
	}

	@Override
	public void clearBookingResponse() {
		Db.getTripBucket().getFlight().setCheckoutResponse(null);
	}

	@Override
	public Download<FlightCheckoutResponse> getBookingDownload() {
		return new Download<FlightCheckoutResponse>() {
			@Override
			public FlightCheckoutResponse doDownload() {
				Context context = getActivity();
				ExpediaServices services = new ExpediaServices(context);
				BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);

				BillingInfo billingInfo = Db.getBillingInfo();
				TripBucketItemFlight item = Db.getTripBucket().getFlight();

				//So at this point, billing info has the correct email address, but the api considers the email
				//address of the first traveler the top priority. We dont want to change the email information
				//of our stored travelers, so we make a copy of the first traveler, and alter its email address.
				List<Traveler> travelers = new ArrayList<Traveler>(Db.getTravelers());
				if (travelers != null && travelers.size() > 0) {
					Traveler trav = new Traveler();
					trav.fromJson(travelers.get(0).toJson());
					trav.setEmail(billingInfo.getEmail());
					travelers.set(0, trav);
				}

				return services.flightCheckout(item, billingInfo, travelers, 0);
			}
		};
	}

	@Override
	public Class<FlightCheckoutResponse> getBookingResponseClass() {
		return FlightCheckoutResponse.class;
	}

	@Override
	public void doBookingPrep() {
		// No pre-booking check or prep required for flights so let's just start the booking.
		startBookingDownload();
	}

	@Override
	public void handleBookingErrorResponse(Response response) {
		super.handleBookingErrorResponse(response, LineOfBusiness.FLIGHTS);
	}

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (isDownloadingCreateTrip()) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_CREATE_TRIP, mFlightTripCallback);
		}
		mFragmentModLock.setSafe(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mFragmentModLock.setSafe(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			bd.cancelDownload(KEY_DOWNLOAD_CREATE_TRIP);
		}
		else {
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_CREATE_TRIP);
		}
	}

	public void startDownload(final FlightBookingState state) {
		mFragmentModLock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				switch (state) {
				case CREATE_TRIP:
					startCreateTripDownload();
					break;
				default:
					break;
				}
			}
		});
	}

	public void cancelDownload(FlightBookingState state) {
		switch (state) {
		case CREATE_TRIP:
			cancelCreateTripDownload();
			break;
		}
	}

	private void startCreateTripDownload() {
		// Let's cancel download if already running.
		cancelCreateTripDownload();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.startDownload(KEY_DOWNLOAD_CREATE_TRIP, mFlightTripDownload, mFlightTripCallback);
	}

	private void cancelCreateTripDownload() {
		if (isDownloadingCreateTrip()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_DOWNLOAD_CREATE_TRIP);
		}
	}

	public boolean isDownloadingCreateTrip() {
		return BackgroundDownloader.getInstance().isDownloading(KEY_DOWNLOAD_CREATE_TRIP);
	}

	private BackgroundDownloader.Download<CreateItineraryResponse> mFlightTripDownload = new BackgroundDownloader.Download<CreateItineraryResponse>() {
		@Override
		public CreateItineraryResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DOWNLOAD_CREATE_TRIP, services);
			return services.createItinerary(mFlightTrip.getProductKey(), 0);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<CreateItineraryResponse> mFlightTripCallback = new BackgroundDownloader.OnDownloadComplete<CreateItineraryResponse>() {
		@Override
		public void onDownload(CreateItineraryResponse response) {
			if (response == null || response.hasErrors()) {
				handleCreateTripError(response);
			}
			else {
				onCreateTripSuccess(response);
			}
		}

	};

	private void onCreateTripSuccess(CreateItineraryResponse response) {
		// Debug/Testing only flight price change fake. Let's add desired price change to the response object.
		if (BuildConfig.DEBUG) {
			String val = SettingUtils.get(getActivity(),
				getString(R.string.preference_fake_flight_price_change),
				getString(R.string.preference_fake_price_change_default));
			BigDecimal bigDecVal = new BigDecimal(val);
			Money priceChangeMoney = new Money();
			priceChangeMoney.setAmount(bigDecVal);

			//Update total price and price change amount ONLY if fake price is not zero amount.
			if (!priceChangeMoney.isZero()) {
				Money currentTotalPrice = response.getOffer().getTotalPrice();
				currentTotalPrice.add(bigDecVal);
				response.getOffer().setTotalPrice(currentTotalPrice);
				response.getOffer().setPriceChangeAmount(priceChangeMoney);
			}
		}

		Db.getTripBucket().getFlight().setItineraryResponse(response);
		Db.getTripBucket().getFlight().addValidPayments(response.getValidPayments());
		mFlightTrip.updateFrom(response.getOffer());
		if (Db.getTripBucket() != null && Db.getTripBucket().getFlight() != null && mFlightTrip.notifyPriceChanged()) {
			Db.getTripBucket().getFlight().setHasPriceChanged(true);
		}
		Db.saveTripBucket(getActivity());

		Events.post(new Events.FlightPriceChange());
		Events.post(new Events.CreateTripDownloadSuccess(response));
	}

	private void handleCreateTripError(CreateItineraryResponse response) {
		if (response == null) {
			Events.post(new Events.CreateTripDownloadError(null));
			showRetryErrorDialog();
		}
		else {
			ServerError firstError = response.getErrors().get(0);
			Events.post(new Events.CreateTripDownloadError(firstError));

			switch (firstError.getErrorCode()) {
			case FLIGHT_PRODUCT_NOT_FOUND:
			case SESSION_TIMEOUT:
				if (ExpediaBookingApp.useTabletInterface(getActivity())) {
					Events.post(new Events.TripItemExpired(LineOfBusiness.FLIGHTS));
					// Let's break for Tablet ONLY. Let's handle all cases for flights with just one dialog - below (for now).
					break;
				}
			case FLIGHT_SOLD_OUT:
				if (ExpediaBookingApp.useTabletInterface(getActivity())) {
					// Post event for tablets to show the BookingUnavailableFragment
					Events.post(new Events.BookingUnavailable(LineOfBusiness.FLIGHTS));
				}
				else {
					boolean isPlural = (Db.getTripBucket().getFlight().getFlightSearchParams().getQueryLegCount() != 1);
					BookingUnavailableDialogFragment df = BookingUnavailableDialogFragment.newInstance(isPlural, LineOfBusiness.FLIGHTS);
					df.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), FLIGHT_UNAVAILABLE_DIALOG);
				}
				break;
			default:
				showRetryErrorDialog();
				break;
			}
		}
	}

	private void showRetryErrorDialog() {
		DialogFragment df = new RetryErrorDialogFragment();
		df.show(getChildFragmentManager(), RETRY_CREATE_TRIP_DIALOG);
	}

	@Override
	public void onRetryError() {
		Events.post(new Events.CreateTripDownloadRetry());
	}

	@Override
	public void onCancelError() {
		Events.post(new Events.CreateTripDownloadRetryCancel());
	}
}
