package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketFlightFragment extends TripBucketItemFragment {

	private static final String DESTINATION_IMAGE_INFO_DOWNLOAD_KEY = "DESTINATION_IMAGE_INFO_DOWNLOAD_KEY";

	private FlightTrip mFlightTrip;
	private ImageView mDestinationImageView;
	private HeaderBitmapColorAveragedDrawable mHeaderBitmapDrawable;
	private String mNewDestination;
	private String mPreviousDestination;

	public static TripBucketFlightFragment newInstance() {
		TripBucketFlightFragment frag = new TripBucketFlightFragment();
		return frag;
	}

	private ViewGroup mExpandedView;

	boolean mIsOnCheckout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIsOnCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
	}

	private void refreshFlightTrip() {
		if (mIsOnCheckout) {
			if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
				mFlightTrip = Db.getFlightSearch().getSelectedFlightTrip();
			}
			else {
				mFlightTrip = null;
			}
		}
		else {
			if (Db.getTripBucket() != null && Db.getTripBucket().getFlight() != null
				&& Db.getTripBucket().getFlight().getFlightTrip() != null) {
				mFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
			}
			else {
				mFlightTrip = null;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public CharSequence getBookButtonText() {
		return getString(R.string.trip_bucket_book_flight);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup root) {
		mExpandedView = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, root, false);

		bindExpandedView();

		root.addView(mExpandedView);
	}

	@Override
	public boolean doTripBucketImageRefresh() {
		mNewDestination = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
		if (mPreviousDestination != null && mPreviousDestination.equals(mNewDestination)) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable headerBitmapDrawable) {
		mHeaderBitmapDrawable = headerBitmapDrawable;
		mDestinationImageView = imageView;

		if (!BackgroundDownloader.getInstance().isDownloading(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY)) {
			mDestinationImageView.setImageDrawable(mHeaderBitmapDrawable);

			mHeaderBitmapDrawable.disableOverlay();
			mHeaderBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));
			mDestinationImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					// Let's listen for just one time
					mDestinationImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

					mPreviousDestination = mNewDestination;
					ExpediaImage bgImage = ExpediaImageManager.getInstance().getDestinationImage(mNewDestination,
						mDestinationImageView.getMeasuredWidth(), mDestinationImageView.getMeasuredHeight(), false);

					if (bgImage == null) {
						startDestinationImageDownload();
					}
					else {

						mHeaderBitmapDrawable.enableOverlay();
						mHeaderBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), bgImage.getUrl(),
							R.drawable.bg_itin_placeholder));
					}
				}
			});
		}
	}

	@Override
	public String getNameText() {
		refreshFlightTrip();
		if (mFlightTrip != null) {
			String cityName = StrUtils.getWaypointCityOrCode(mFlightTrip.getLeg(0).getLastWaypoint());
			if (mFlightTrip.getLegCount() > 1) {
				return getResources().getString(R.string.trip_bucket_label_flight_roundtrip, cityName);
			}
			else {
				return getResources().getString(R.string.trip_bucket_label_flight_trip, cityName);
			}
		}
		return null;
	}

	@Override
	public String getDateRangeText() {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		return CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
			| DateUtils.FORMAT_ABBREV_MONTH);
	}

	@Override
	public CharSequence getTripPrice() {
		refreshFlightTrip();
		if (Db.hasBillingInfo() && mFlightTrip != null) {
			return mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney(Money.F_NO_DECIMAL);
		}
		else {
			return null;
		}
	}

	private void bindExpandedView() {
		refreshFlightTrip();

		// Dates
		Calendar depDate = mFlightTrip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
		Calendar retDate = mFlightTrip.getLeg(mFlightTrip.getLegCount() - 1).getLastWaypoint()
			.getMostRelevantDateTime();
		long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();

		String dateRange = DateUtils.formatDateRange(getActivity(), start, end, DateUtils.FORMAT_SHOW_DATE);
		Ui.setText(mExpandedView, R.id.dates_text_view, dateRange);

		// Num travelers
		int numTravelers = Db.getFlightSearch().getSearchParams().getNumTravelers();
		String numTravStr = getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers,
			numTravelers);
		Ui.setText(mExpandedView, R.id.num_travelers_text_view, numTravStr);

		// Price
		if (Db.hasBillingInfo()) {
			TextView priceTextView = Ui.findView(mExpandedView, R.id.price_expanded_bucket_text_view);
			String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney();
			priceTextView.setText(price);
			priceTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showBreakdownDialog(LineOfBusiness.FLIGHTS);
				}
			});
		}
		else {
			Ui.showToast(getActivity(), "TODO fix billing data load timing issue!");
		}
	}

	public void refreshExpandedTripPrice() {
		String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney();
		Ui.setText(getActivity(), R.id.price_expanded_bucket_text_view, price);
	}
	public void refreshTripOnPriceChanged(String priceChangeText) {
		refreshFlightTrip();
		refreshExpandedTripPrice();
		setPriceChangeNotificationText(priceChangeText);
		Db.getTripBucket().getFlight().setHasPriceChanged(true);
	}

	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Db.getTripBucket().selectHotelAndFlight();
			triggerTripBucketBookAction(LineOfBusiness.FLIGHTS);
		}
	};

	private void startDestinationImageDownload() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY)) {
			bd.startDownload(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY, mDestinationImageInfoDownload,
				mDestinationImageInfoDownloadCallback);
		}
	}

	private BackgroundDownloader.Download<ExpediaImage> mDestinationImageInfoDownload = new BackgroundDownloader.Download<ExpediaImage>() {
		@Override
		public ExpediaImage doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY, services);
			return ExpediaImageManager.getInstance().getDestinationImage(mNewDestination,
				mDestinationImageView.getMeasuredWidth(), mDestinationImageView.getMeasuredHeight(), true);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<ExpediaImage> mDestinationImageInfoDownloadCallback = new BackgroundDownloader.OnDownloadComplete<ExpediaImage>() {
		@Override
		public void onDownload(ExpediaImage image) {
			if (image != null) {
				mHeaderBitmapDrawable.enableOverlay();
				mHeaderBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), image.getUrl(),
					R.drawable.bg_itin_placeholder));
			}
		}
	};
}
