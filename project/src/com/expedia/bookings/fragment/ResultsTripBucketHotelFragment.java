package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.dialog.HotelPriceChangeDialog;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.HotelSummarySection;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketHotelFragment extends TripBucketItemFragment implements RetryErrorDialogFragmentListener {

	private static final String KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE = "KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE";
	private static final String KEY_CREATE_TRIP = "KEY_HOTEL_CREATE_TRIP";
	private static final String TAG_HOTEL_CHECKOUT_PREP_DIALOG = "TAG_HOTEL_CHECKOUT_PREP_DIALOG";
	private static final String TAG_HOTEL_CREATE_TRIP_DIALOG = "TAG_HOTEL_CREATE_TRIP_DIALOG";
	public static final String HOTEL_OFFER_ERROR_DIALOG = "HOTEL_OFFER_ERROR_DIALOG";
	private static final String RETRY_CREATE_TRIP_DIALOG = "RETRY_CREATE_TRIP_DIALOG";

	private static final String INSTANCE_DONE_LOADING_PRICE_CHANGE = "INSTANCE_DONE_LOADING_PRICE_CHANGE";

	private boolean mIsDoneLoadingPriceChange = false;

	public static ResultsTripBucketHotelFragment newInstance() {
		ResultsTripBucketHotelFragment frag = new ResultsTripBucketHotelFragment();
		return frag;
	}

	private HotelSummarySection mHotelSection;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE, mIsDoneLoadingPriceChange);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mIsDoneLoadingPriceChange = savedInstanceState.getBoolean(INSTANCE_DONE_LOADING_PRICE_CHANGE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (getActivity() != null && getActivity().isFinishing()) {
			bd.cancelDownload(KEY_CREATE_TRIP);
			bd.cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
		else {
			bd.unregisterDownloadCallback(KEY_CREATE_TRIP);
			bd.unregisterDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// Create Trip callback
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		boolean isCreatingTrip = bd.isDownloading(KEY_CREATE_TRIP);
		boolean isOnCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
		if (isCreatingTrip && isOnCheckout) {
			bd.registerDownloadCallback(KEY_CREATE_TRIP, mCreateTripCallback);
		}
		// HotelProduct callback
		boolean isDownloadingHotelProd = bd.isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
		if (isDownloadingHotelProd && isOnCheckout) {
			bd.registerDownloadCallback(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE, mHotelProductCallback);
		}
	}

	@Override
	protected void doBind() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (mHotelSection != null && hotel != null) {
			Property property = hotel.getProperty();
			Rate rate = hotel.getRate();

			mHotelSection.bindForTripBucket(property, rate);
		}
	}

	@Override
	public CharSequence getBookButtonText() {
		return getString(R.string.trip_bucket_book_hotel);
	}

	@Override
	public void addTopView(LayoutInflater inflater, ViewGroup viewGroup) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.hotel_card_tablet_add_tripbucket, viewGroup);
		mHotelSection = (HotelSummarySection) root.getChildAt(0);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup vg = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, root, false);

		Rate rate = Db.getTripBucket().getHotel().getRate();

		// TODO remove this null check. it shouldn't be necessary once the active/selected/added rate gets persisted properly
		if (rate != null) {
			// Title stuff
			TextView roomTypeTv = Ui.findView(vg, R.id.primary_title_text_view);
			roomTypeTv.setVisibility(View.VISIBLE);
			roomTypeTv.setText(rate.getRoomDescription());

			TextView bedTypeTv = Ui.findView(vg, R.id.secondary_title_text_view);
			bedTypeTv.setVisibility(View.VISIBLE);
			bedTypeTv.setText(rate.getFormattedBedNames());

			// Dates
			LocalDate checkIn = Db.getHotelSearch().getSearchParams().getCheckInDate();
			LocalDate checkOut = Db.getHotelSearch().getSearchParams().getCheckOutDate();
			String dateRange = JodaUtils.formatDateRange(getActivity(), checkIn, checkOut, DateUtils.FORMAT_SHOW_DATE);
			int numNights = Db.getHotelSearch().getSearchParams().getStayDuration();
			String nightsStr = getResources().getQuantityString(R.plurals.length_of_stay, numNights, numNights);
			String dateStr = getString(R.string.dates_and_nights_TEMPLATE, dateRange, nightsStr);
			Ui.setText(vg, R.id.dates_text_view, dateStr);

			// Num guests
			int numGuests = Db.getHotelSearch().getSearchParams().getNumAdults(); // TODO what about the CHILDREN?
			String numGuestsStr = getResources().getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
			Ui.setText(vg, R.id.num_travelers_text_view, numGuestsStr);

			// Price
			String price = rate.getDisplayTotalPrice().getFormattedMoney();
			Ui.setText(vg, R.id.price_expanded_bucket_text_view, price);

			// Hide price in the picture
			mHotelSection.findViewById(R.id.price_text_view).setVisibility(View.GONE);

			root.addView(vg);
		}
		else {
			Ui.showToast(getActivity(), "TODO: hotel rate isn't present. was it saved to disk properly?");
		}

	}

	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Db.getTripBucket().selectHotelAndFlight();
			getActivity().startActivity(TabletCheckoutActivity.createIntent(getActivity(), LineOfBusiness.HOTELS));
		}
	};

	public void doCheckoutPrep() {
		getFragmentManager().executePendingTransactions();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		if (!bd.isDownloading(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE) && !mIsDoneLoadingPriceChange) {
			ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.calculating_taxes_and_fees));
			df.show(getFragmentManager(), TAG_HOTEL_CHECKOUT_PREP_DIALOG);

			BackgroundDownloader.getInstance().cancelDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE);
			BackgroundDownloader.getInstance().startDownload(KEY_DOWNLOAD_HOTEL_PRODUCT_RESPONSE,
				mHotelProductDownload,
				mHotelProductCallback);
		}
	}

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
			ThrobberDialog df = Ui.findSupportFragment(ResultsTripBucketHotelFragment.this,
				TAG_HOTEL_CHECKOUT_PREP_DIALOG);
			df.dismiss();
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
					mIsDoneLoadingPriceChange = true;
					Db.getHotelSearch().getAvailability(selectedId).updateFrom(selectedRate.getRateKey(), response);
					Db.getHotelSearch().getAvailability(selectedId).setSelectedRate(newRate);
					createTrip();

				}
				else {
					handleHotelProductError(response);
				}
			}
		}

	};

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

	private void createTrip() {
		ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.spinner_text_hotel_create_trip));
		df.show(getFragmentManager(), TAG_HOTEL_CREATE_TRIP_DIALOG);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_CREATE_TRIP)) {
			bd.cancelDownload(KEY_CREATE_TRIP);
		}
		bd.startDownload(KEY_CREATE_TRIP, mCreateTripDownload, mCreateTripCallback);
	}

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
			ThrobberDialog df = Ui.findSupportFragment(ResultsTripBucketHotelFragment.this,
				TAG_HOTEL_CREATE_TRIP_DIALOG);
			df.dismiss();
			if (response == null) {
				showRetryErrorDialog();
			}
			else if (response.hasErrors()) {
				handleCreateTripError(response);
			}
			else {
				Db.getHotelSearch().setCreateTripResponse(response);
			}
		}
	};

	private void handleCreateTripError(CreateTripResponse response) {
		ServerError firstError = response.getErrors().get(0);

		switch (firstError.getErrorCode()) {
		case TRIP_SERVICE_UNKNOWN_ERROR:
			// Let's show a retry dialog here.
		case INVALID_INPUT:
			/*
			 * Since we are only sending [productKey, roomInfoFields] params to the service, don't think users have control over the input.
			 * Hence for now let's show a retry dialog here too (after a chat with API team)
			 */
		default: {
			showRetryErrorDialog();
			break;
		}
		}
	}

	private void showRetryErrorDialog() {
		DialogFragment df = new RetryErrorDialogFragment();
		df.show(getChildFragmentManager(), RETRY_CREATE_TRIP_DIALOG);
	}

	////////////////////////////////////
	/// RetryErrorDialogFragment handlers

	@Override
	public void onRetryError() {
		createTrip();
	}

	@Override
	public void onCancelError() {
		if (getActivity() != null) {
			getActivity().finish();
		}
	}

}
