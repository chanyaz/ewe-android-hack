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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketFlightFragment extends TripBucketItemFragment {

	private static final String DESTINATION_IMAGE_INFO_DOWNLOAD_KEY = "DESTINATION_IMAGE_INFO_DOWNLOAD_KEY";

	//UI
	private ViewGroup mExpandedView;
	private ImageView mDestinationImageView;
	private HeaderBitmapColorAveragedDrawable mHeaderBitmapDrawable;
	private TextView mPriceTv;
	private TextView mNumTravelersTv;
	private TextView mDatesTv;

	//Other
	private FlightTrip mFlightTrip;
	private String mNewDestination;
	private String mPreviousDestination;
	boolean mIsOnCheckout;

	public static TripBucketFlightFragment newInstance() {
		TripBucketFlightFragment frag = new TripBucketFlightFragment();
		return frag;
	}

	@Override
	public void bind() {
		if (mRootC != null) {
			refreshFlightTrip();
		}
		super.bind();
	}

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
		// Let's refresh the item state to reflect that of the existing bucket item. And update for only SHOWING_CHECKOUT_BUTTON & BOOKING_UNAVAILABLE states.
		if (Db.getTripBucket().getFlight() != null
			&& (Db.getTripBucket().getFlight().getState() == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || Db.getTripBucket().getFlight().getState() == TripBucketItemState.BOOKING_UNAVAILABLE)) {
			setVisibilityState(Db.getTripBucket().getFlight().getState());
		}
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

		mDatesTv = Ui.findView(mExpandedView, R.id.dates_text_view);
		mNumTravelersTv = Ui.findView(mExpandedView, R.id.num_travelers_text_view);
		mPriceTv = Ui.findView(mExpandedView, R.id.price_expanded_bucket_text_view);

		mPriceTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showBreakdownDialog(LineOfBusiness.FLIGHTS);
			}
		});

		bindExpandedView(Db.getTripBucket().getFlight());

		root.addView(mExpandedView);
	}

	@Override
	public boolean doTripBucketImageRefresh() {
		mNewDestination = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
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
			mHeaderBitmapDrawable
				.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));
			mDestinationImageView.getViewTreeObserver()
				.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

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
							mHeaderBitmapDrawable
								.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), bgImage.getUrl(),
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
		TripBucketItemFlight flight = Db.getTripBucket().getFlight();
		if (flight != null) {
			FlightSearchParams params = flight.getFlightSearchParams();
			return CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		}
		else {
			return null;
		}
	}

	@Override
	public CharSequence getTripPrice() {
		refreshFlightTrip();
		if (mFlightTrip != null) {
			return mFlightTrip.getTotalFareWithCardFee(Db.hasBillingInfo() ? Db.getBillingInfo() : null)
				.getFormattedMoney(Money.F_NO_DECIMAL);
		}
		else {
			return null;
		}
	}

	@Override
	public void bindExpandedView(TripBucketItem item) {
		refreshFlightTrip();

		if (mFlightTrip != null) {
			// Dates
			Calendar depDate = mFlightTrip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
			Calendar retDate = mFlightTrip.getLeg(mFlightTrip.getLegCount() - 1).getLastWaypoint()
				.getMostRelevantDateTime();
			long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
			long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();
			String dateRange = DateUtils.formatDateRange(getActivity(), start, end, DateUtils.FORMAT_SHOW_DATE);
			mDatesTv.setText(dateRange);

			String price = mFlightTrip.getTotalFare().getFormattedMoney();
			mPriceTv.setText(price);
		}

		FlightSearch search = Db.getFlightSearch();
		if (search != null) {
			FlightSearchParams params = Db.getFlightSearch().getSearchParams();
			if (params != null) {
				// Num travelers
				int numTravelers = Db.getFlightSearch().getSearchParams().getNumTravelers();
				String numTravStr = getResources()
					.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers,
						numTravelers);
				mNumTravelersTv.setText(numTravStr);
			}
		}
	}

	public void refreshExpandedTripPrice() {
		String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney();
		mPriceTv.setText(price);
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

	@Override
	public boolean isSelected() {
		if (Db.getTripBucket().getFlight() != null) {
			return Db.getTripBucket().getFlight().isSelected();
		}
		else {
			return false;
		}
	}

	@Override
	public void setSelected(boolean isSelected) {
		Db.getTripBucket().getFlight().setSelected(isSelected);
		if (Db.getTripBucket().getHotel() != null) {
			Db.getTripBucket().getHotel().setSelected(!isSelected);
		}
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

	@Override
	public TripBucketItemFlight getItem() {
		return Db.getTripBucket().getFlight();
	}
}
