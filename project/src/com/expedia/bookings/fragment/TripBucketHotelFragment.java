package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketHotelFragment extends TripBucketItemFragment {

	public static TripBucketHotelFragment newInstance() {
		TripBucketHotelFragment frag = new TripBucketHotelFragment();
		return frag;
	}

	private TextView mNowBookingTv;;
	private TextView mRoomAndBedTv;
	private TextView mRoomTypeTv;
	private TextView mBedTypeTv;
	private TextView mDatesTv;
	private TextView mNumTravelersTv;
	private ViewGroup mPriceContainer;
	private TextView mPriceTv;


	@Override
	public CharSequence getBookButtonText() {
		return getString(R.string.trip_bucket_book_hotel);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup vg = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, root, false);

		// Title stuff
		mRoomTypeTv = Ui.findView(vg, R.id.room_type_text_view);
		if (mRoomTypeTv != null) {
			mRoomTypeTv.setVisibility(View.VISIBLE);
		}

		mBedTypeTv = Ui.findView(vg, R.id.bed_type_text_view);
		if (mBedTypeTv != null) {
			mBedTypeTv.setVisibility(View.VISIBLE);
		}

		// Portrait only
		mNowBookingTv = Ui.findView(vg, R.id.now_booking_text_view);
		if (mNowBookingTv != null) {
			mNowBookingTv.setVisibility(View.VISIBLE);
		}

		mRoomAndBedTv = Ui.findView(vg, R.id.room_and_bed_text_view);
		if (mRoomAndBedTv != null) {
			mRoomAndBedTv.setVisibility(View.VISIBLE);
		}

		mDatesTv = Ui.findView(vg, R.id.dates_text_view);
		mNumTravelersTv = Ui.findView(vg, R.id.num_travelers_text_view);

		// Price
		mPriceContainer = Ui.findView(vg, R.id.price_expanded_bucket_container);
		mPriceTv = Ui.findView(vg, R.id.price_expanded_bucket_text_view);

		mPriceContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showBreakdownDialog(LineOfBusiness.HOTELS);
			}
		});

		if (!getResources().getBoolean(R.bool.show_tripbucket_date)) {
			mDatesTv.setVisibility(View.GONE);
		}

		bindExpandedView(Db.getTripBucket().getHotel());

		root.addView(vg);
	}

	@Override
	public void bind() {
		if (mRootC != null) {
			refreshRate();
		}
		super.bind();
	}

	@Override
	public void bindExpandedView(TripBucketItem item) {
		if (item != null && item instanceof TripBucketItemHotel) {
			TripBucketItemHotel itemHotel = (TripBucketItemHotel) item;
			if (itemHotel.getRate() != null) {
				Rate rate = itemHotel.getRate();

				if (mRoomTypeTv != null) {
					mRoomTypeTv.setText(rate.getRoomDescription());
				}
				if (mBedTypeTv != null) {
					mBedTypeTv.setText(rate.getFormattedBedNames());
				}
				if (mRoomAndBedTv != null) {
					mRoomAndBedTv.setText(Html.fromHtml(getString(R.string.room_and_bed_type_TEMPLATE, rate.getRoomDescription(), rate.getFormattedBedNames())));
				}
				if (mNowBookingTv != null) {
					if (Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null) {
						String hotelName = Db.getHotelSearch().getSelectedProperty().getName();
						mNowBookingTv.setText(Html.fromHtml(getString(R.string.now_booking_TEMPLATE, hotelName).toUpperCase()));
					}
				}

				String price = rate.getDisplayTotalPrice().getFormattedMoney();
				mPriceTv.setText(price);
			}
		}
		bindToDbHotelSearch();
	}

	private void bindToDbHotelSearch() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			HotelSearchParams params = hotel.getHotelSearchParams();
			if (params != null) {
				//Dates
				LocalDate checkIn = params.getCheckInDate();
				LocalDate checkOut = params.getCheckOutDate();
				String dateRange = JodaUtils
					.formatDateRange(getActivity(), checkIn, checkOut, DateUtils.FORMAT_SHOW_DATE);
				int numNights = params.getStayDuration();
				String nightsStr = getResources().getQuantityString(R.plurals.length_of_stay, numNights, numNights);
				String dateStr = getString(R.string.dates_and_nights_TEMPLATE, dateRange, nightsStr);
				mDatesTv.setText(dateStr);

				//Guests
				int numGuests = params.getNumTravelers();
				String numGuestsStr = getResources()
					.getQuantityString(R.plurals.number_of_guests, numGuests, numGuests);
				mNumTravelersTv.setText(numGuestsStr);
			}
		}
	}

	@Override
	public void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable headerBitmapDrawable) {
		int placeholderResId = Ui.obtainThemeResID((Activity) getActivity(), R.attr.HotelRowThumbPlaceHolderDrawable);
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null && hotel.getProperty() != null && hotel.getProperty().getThumbnail() != null) {
			hotel.getProperty().getThumbnail()
				.fillHeaderBitmapDrawable(imageView, headerBitmapDrawable, placeholderResId);
		}
	}

	@Override
	public boolean doTripBucketImageRefresh() {
		return true;
	}

	@Override
	public String getNameText() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			return hotel.getProperty().getName();
		}
		else {
			return null;
		}
	}

	@Override
	public String getDateRangeText() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			HotelSearchParams params = hotel.getHotelSearchParams();
			return CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		}
		else {
			return null;
		}
	}

	@Override
	public CharSequence getTripPrice() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate rate = hotel.getRate();
			String rateString = rate.getDisplayTotalPrice().getFormattedMoney(Money.F_NO_DECIMAL);
			return rateString;
		}
		else {
			return null;
		}
	}


	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	@Override
	public boolean isSelected() {
		if (Db.getTripBucket().getHotel() != null) {
			return Db.getTripBucket().getHotel().isSelected();
		}
		else {
			return false;
		}
	}

	@Override
	public void setSelected(boolean isSelected) {
		Db.getTripBucket().getHotel().setSelected(isSelected);
		if (Db.getTripBucket().getFlight() != null) {
			Db.getTripBucket().getFlight().setSelected(!isSelected);
		}
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Db.getTripBucket().selectHotelAndFlight();
			triggerTripBucketBookAction(LineOfBusiness.HOTELS);
		}
	};

	public void refreshRate() {
		// Update the price in the expanded tripbucket.
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate rate = hotel.getRate();
			String price = rate.getDisplayTotalPrice().getFormattedMoney();
			mPriceTv.setText(price);

			refreshPriceChange();
		}
	}

	@Override
	public CharSequence getPriceChangeMessage() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate oldRate = hotel.getOldRate();
			String amount = oldRate.getTotalAmountAfterTax().getFormattedMoney();
			String message = getString(R.string.price_changed_from_TEMPLATE, amount);
			return message;
		}

		return null;
	}

	@Override
	public TripBucketItemHotel getItem() {
		return Db.getTripBucket().getHotel();
	}

}
