package com.expedia.bookings.fragment;

import java.util.Locale;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketFlightFragment extends TripBucketItemFragment {

	private static final String DESTINATION_IMAGE_INFO_DOWNLOAD_KEY = "DESTINATION_IMAGE_INFO_DOWNLOAD_KEY";

	//UI
	private ViewGroup mExpandedView;
	private ViewGroup mPriceContainer;
	private TextView mPriceTv;
	private TextView mNumTravelersTv;
	private TextView mDatesTv;
	private TextView mNowBookingTv;;
	private TextView mTotalTitleTv;

	//Other
	private FlightTrip mFlightTrip;
	private String mNewDestination;
	private String mPreviousDestination;

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

	private void refreshFlightTrip() {
		if (Db.getTripBucket() != null && Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null) {
			mFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
		}
		else {
			mFlightTrip = null;
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
		mPriceContainer = Ui.findView(mExpandedView, R.id.price_expanded_bucket_container);
		mPriceTv = Ui.findView(mExpandedView, R.id.price_expanded_bucket_text_view);
		mTotalTitleTv = Ui.findView(mExpandedView, R.id.total_text);

		mPriceContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showBreakdownDialog(LineOfBusiness.FLIGHTS);
			}
		});

		mTotalTitleTv.setText(getResources().getString(R.string.total_with_tax));

		// Portrait only
		mNowBookingTv = Ui.findView(mExpandedView, R.id.now_booking_text_view);
		if (mNowBookingTv != null) {
			mNowBookingTv.setVisibility(View.VISIBLE);
		}


		if (!getResources().getBoolean(R.bool.show_tripbucket_date)) {
			mDatesTv.setVisibility(View.GONE);
		}

		bindExpandedView(Db.getTripBucket().getFlight());

		root.addView(mExpandedView);
	}

	@Override
	public boolean doTripBucketImageRefresh() {
		TripBucketItemFlight flight = Db.getTripBucket().getFlight();
		if (flight != null) {
			mNewDestination = flight.getFlightSearchParams().getArrivalLocation().getDestinationId();
		}

		return mPreviousDestination == null || !mPreviousDestination.equals(mNewDestination);
	}

	@Override
	public void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable drawable) {
		imageView.setImageDrawable(drawable);

		// Note: hotel_flight_card_width is an estimation and is used only for downloading the image
		final int width = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_width);
		final int height = getResources().getDimensionPixelSize(R.dimen.hotel_flight_card_height);

		mPreviousDestination = mNewDestination;

		final String url = new Akeakamai(Images.getTabletDestination(mNewDestination)) //
			.resizeExactly(width, height) //
			.build();

		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.skin_HotelRowThumbPlaceHolderDrawable);
		new PicassoHelper.Builder(getActivity()).setPlaceholder(placeholderResId).setTarget(
			drawable.getCallBack()).disableFallback().build().load(url);
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
			return DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		}
		else {
			return null;
		}
	}

	@Override
	public CharSequence getTripPrice() {
		refreshFlightTrip();
		if (mFlightTrip != null) {
			return mFlightTrip.getTotalFareWithCardFee(Db.hasBillingInfo() ? Db.getBillingInfo() : null, getItem())
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
			FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
			String dateRange = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_NO_YEAR_ABBREV_MONTH_ABBREV_WEEKDAY);
			mDatesTv.setText(dateRange);

			String price;
			if (mFlightTrip.getTotalPrice() != null) {
				price = mFlightTrip.getTotalPrice().getFormattedMoney();
			}
			else {
				Log.e("Trip Flight Bucket total price is null");
				price = mFlightTrip.getTotalFare().getFormattedMoney();
			}
			mPriceTv.setText(price);

			if (mNowBookingTv != null) {
				String cityName = StrUtils.getWaypointCityOrCode(mFlightTrip.getLeg(0).getLastWaypoint());
				String flightTo = getString(R.string.flights_to_TEMPLATE, cityName);
				mNowBookingTv.setText(Html.fromHtml(getString(R.string.now_booking_TEMPLATE, flightTo).toUpperCase(Locale.getDefault())));
			}
		}

		TripBucketItemFlight flight = Db.getTripBucket().getFlight();
		if (flight != null) {
			FlightSearchParams params = flight.getFlightSearchParams();
			if (params != null) {
				// Num travelers
				int numTravelers = Db.getTripBucket().getFlight().getFlightSearchParams().getNumTravelers();
				String numTravStr = getResources()
					.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers,
						numTravelers);
				mNumTravelersTv.setText(numTravStr);
			}
		}
	}

	public void refreshExpandedTripPrice() {
		String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo(), getItem()).getFormattedMoney();
		mPriceTv.setText(price);
	}

	public void refreshTripOnPriceChanged() {
		refreshFlightTrip();
		refreshExpandedTripPrice();
		refreshPriceChange();
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
			triggerTripBucketBookAction(LineOfBusiness.FLIGHTS);
		}
	};

	@Override
	public TripBucketItemFlight getItem() {
		return Db.getTripBucket().getFlight();
	}

	@Override
	public CharSequence getPriceChangeMessage() {
		if (Db.getTripBucket().getFlight() != null) {
			FlightTrip flightTrip = Db.getTripBucket().getFlight().getFlightTrip();
			String originalPrice = flightTrip.getOldTotalPrice().getFormattedMoney();
			return getString(R.string.price_changed_from_TEMPLATE, originalPrice);
		}

		return null;
	}
}
