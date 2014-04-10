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
import com.expedia.bookings.activity.TabletCheckoutActivity;
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

	private boolean mIsDestinationImageFetched;

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
			mFlightTrip = Db.getFlightSearch().getSelectedFlightTrip();
		}
		else {
			mFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
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
	public void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable headerBitmapDrawable) {
		mHeaderBitmapDrawable = headerBitmapDrawable;
		mDestinationImageView = imageView;
		if (!mIsDestinationImageFetched && !BackgroundDownloader.getInstance().isDownloading(DESTINATION_IMAGE_INFO_DOWNLOAD_KEY)) {
			mDestinationImageView.setImageDrawable(mHeaderBitmapDrawable);

			mHeaderBitmapDrawable.setState(HeaderBitmapColorAveragedDrawable.HeaderBitmapColorAveragedState.PLACEHOLDER);
			mHeaderBitmapDrawable.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bg_itin_placeholder));
			ViewTreeObserver vto = mDestinationImageView.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					if (mIsDestinationImageFetched) {
						return;
					}

					String code = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
					ExpediaImage bgImage = ExpediaImageManager.getInstance().getDestinationImage(code,
						mDestinationImageView.getMeasuredWidth(), mDestinationImageView.getMeasuredHeight(), false);

					if (bgImage == null) {
						startDestinationImageDownload();
					}
					else {
						mHeaderBitmapDrawable.setState(HeaderBitmapColorAveragedDrawable.HeaderBitmapColorAveragedState.REFRESH);
						mHeaderBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), bgImage.getUrl(),
							R.drawable.bg_itin_placeholder));
						mIsDestinationImageFetched = true;
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
	public String getTripPrice() {
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
		Calendar retDate = mFlightTrip.getLeg(mFlightTrip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();
		long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();

		String dateRange =  DateUtils.formatDateRange(getActivity(), start, end, DateUtils.FORMAT_SHOW_DATE);
		Ui.setText(mExpandedView, R.id.dates_text_view, dateRange);

		// Num travelers
		int numTravelers = Db.getFlightSearch().getSearchParams().getNumAdults();
		String numTravStr = getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers,
			numTravelers);
		Ui.setText(mExpandedView, R.id.num_travelers_text_view, numTravStr);

		// Price
		if (Db.hasBillingInfo()) {
			String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney();
			Ui.setText(mExpandedView, R.id.price_expanded_bucket_text_view, price);
		}
		else {
			Ui.showToast(getActivity(), "TODO fix billing data load timing issue!");
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
			getActivity().startActivity(TabletCheckoutActivity.createIntent(getActivity(), LineOfBusiness.FLIGHTS));
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
			String code = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();
			return ExpediaImageManager.getInstance().getDestinationImage(code,
				mDestinationImageView.getMeasuredWidth(), mDestinationImageView.getMeasuredHeight(), true);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<ExpediaImage> mDestinationImageInfoDownloadCallback = new BackgroundDownloader.OnDownloadComplete<ExpediaImage>() {
		@Override
		public void onDownload(ExpediaImage image) {
			if (image != null) {
				mHeaderBitmapDrawable.setState(HeaderBitmapColorAveragedDrawable.HeaderBitmapColorAveragedState.REFRESH);
				mHeaderBitmapDrawable.setUrlBitmapDrawable(new UrlBitmapDrawable(getResources(), image.getUrl(),
					R.drawable.bg_itin_placeholder));
				mIsDestinationImageFetched = true;
			}
			else {
				mIsDestinationImageFetched = false;
			}
		}
	};
}
