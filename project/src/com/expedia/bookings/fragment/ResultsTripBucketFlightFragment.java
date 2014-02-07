package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketFlightFragment extends TripBucketItemFragment {

	private static final String KEY_CREATE_TRIP = "KEY_FLIGHT_CREATE_TRIP";

	private static final String TAG_FLIGHT_LOADING_DIALOG = "TAG_FLIGHT_LOADING_DIALOG";

	private FlightTrip mFlightTrip;

	public static ResultsTripBucketFlightFragment newInstance() {
		ResultsTripBucketFlightFragment frag = new ResultsTripBucketFlightFragment();
		return frag;
	}

	private FlightLegSummarySectionTablet mFlightSection;

	private ViewGroup mExpandedView;

	@Override
	public void onResume() {
		super.onResume();

		// Create Trip callback
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		boolean isDownloading = bd.isDownloading(KEY_CREATE_TRIP);
		boolean isOnCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
		if (isDownloading && isOnCheckout) {
			bd.registerDownloadCallback(KEY_CREATE_TRIP, mFlightDetailsCallback);
		}

		doBind();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Create Trip
		if (getActivity() != null && getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_CREATE_TRIP);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_CREATE_TRIP);
		}
	}

	@Override
	protected void doBind() {
		bindToDb();
	}

	private void bindToDb() {
		if (mFlightSection != null) {
			if (Db.getFlightSearch().getSelectedFlightTrip() != null) {
				mFlightSection.bindForTripBucket(Db.getFlightSearch());
			}
		}
		if (mExpandedView != null) {
			bindExpandedView();
		}
	}

	@Override
	public CharSequence getBookButtonText() {
		return getString(R.string.trip_bucket_book_flight);
	}

	@Override
	public void addTopView(LayoutInflater inflater, ViewGroup viewGroup) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.flight_card_tablet_add_tripbucket, viewGroup);
		mFlightSection = (FlightLegSummarySectionTablet) root.getChildAt(0);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup root) {
		mFlightTrip = Db.getFlightSearch().getSelectedFlightTrip();

		mExpandedView = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, root, false);

		bindExpandedView();

		root.addView(mExpandedView);
	}

	private void bindExpandedView() {
		// Dates
		Calendar depDate = mFlightTrip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
		Calendar retDate = mFlightTrip.getLeg(mFlightTrip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();
		long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();

		String dateRange = DateUtils.formatDateRange(getActivity(), start, end, DateUtils.FORMAT_SHOW_DATE);
		Ui.setText(mExpandedView, R.id.dates_text_view, dateRange);

		// Num travelers
		int numTravelers = Db.getFlightSearch().getSearchParams().getNumAdults();
		String numTravStr = getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers,
			numTravelers);
		Ui.setText(mExpandedView, R.id.num_travelers_text_view, numTravStr);

		// Price
		String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney(Money.F_NO_DECIMAL);
		Ui.setText(mExpandedView, R.id.price_expanded_bucket_text_view, price);

		// Hide price in the FlightLeg card
		View priceTv = Ui.findView(mFlightSection, R.id.price_text_view);
		priceTv.setVisibility(View.INVISIBLE);
	}

	//////////////////////////////////////////////////////
	// Create Trip

	public void doCreateTrip() {
		mFlightTrip = Db.getFlightSearch().getSelectedFlightTrip();

		if (!BackgroundDownloader.getInstance().isDownloading(KEY_CREATE_TRIP) && TextUtils.isEmpty(mFlightTrip.getItineraryNumber())) {
			getFragmentManager().executePendingTransactions();

			// Show some indication of whats going on
			ThrobberDialog df = ThrobberDialog.newInstance(getString(R.string.loading_flight_details));
			df.show(getFragmentManager(), TAG_FLIGHT_LOADING_DIALOG);

			// Kick off the download
			BackgroundDownloader.getInstance().cancelDownload(KEY_CREATE_TRIP);
			BackgroundDownloader.getInstance().startDownload(KEY_CREATE_TRIP, mFlightDetailsDownload, mFlightDetailsCallback);
		}

	}

	private BackgroundDownloader.Download<CreateItineraryResponse> mFlightDetailsDownload = new BackgroundDownloader.Download<CreateItineraryResponse>() {
		@Override
		public CreateItineraryResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_CREATE_TRIP, services);
			return services.createItinerary(mFlightTrip.getProductKey(), 0);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<CreateItineraryResponse> mFlightDetailsCallback = new BackgroundDownloader.OnDownloadComplete<CreateItineraryResponse>() {
		@Override
		public void onDownload(CreateItineraryResponse results) {
			ThrobberDialog df = Ui.findSupportFragment(ResultsTripBucketFlightFragment.this, TAG_FLIGHT_LOADING_DIALOG);
			df.dismiss();

			if (results == null) {
				Ui.showToast(getActivity(), "create/trip results == null. show a retry dialog???");
			}
			else if (results.hasErrors()) {
				handleErrors(results);
			}
			else {
				Db.addItinerary(results.getItinerary());
				Money originalPrice = mFlightTrip.getTotalFare();

				mFlightTrip.updateFrom(results.getOffer());
				doBind();

				Db.kickOffBackgroundFlightSearchSave(getActivity());

				if (mFlightTrip.notifyPriceChanged()) {
					String priceChangeTemplate = getResources().getString(R.string.price_changed_from_TEMPLATE);
					String priceChangeStr = String.format(priceChangeTemplate, originalPrice.getFormattedMoney());

					// TODO notify user with something other than toast.
					Ui.showToast(getActivity(), priceChangeStr);
				}
				else {
					// no price change occurred
				}

			}
		}
	};

	private void handleErrors(CreateItineraryResponse response) {
		ServerError firstError = response.getErrors().get(0);

		switch (firstError.getErrorCode()) {
		case FLIGHT_PRODUCT_NOT_FOUND:
		case FLIGHT_SOLD_OUT:
		case SESSION_TIMEOUT: {
			Ui.showToast(getActivity(), "flight(s) are gone!! pick some new flights!?!");
			return;
		}
		default: {
			Ui.showToast(getActivity(), "show a retry dialog???");
			break;
		}
		}
	}

	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Db.getTripBucket().selectFlight();
			getActivity().startActivity(TabletCheckoutActivity.createIntent(getActivity(), LineOfBusiness.FLIGHTS));
		}
	};
}
