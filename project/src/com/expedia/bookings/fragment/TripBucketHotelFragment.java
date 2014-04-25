package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketHotelFragment extends TripBucketItemFragment {

	public static TripBucketHotelFragment newInstance() {
		TripBucketHotelFragment frag = new TripBucketHotelFragment();
		return frag;
	}

	@Override
	public CharSequence getBookButtonText() {
		return getString(R.string.trip_bucket_book_hotel);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup vg = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, root, false);

		Rate rate = Db.getTripBucket().getHotel().getRate();

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
		TextView priceTextView = Ui.findView(vg, R.id.price_expanded_bucket_text_view);
		String price = rate.getDisplayTotalPrice().getFormattedMoney();
		priceTextView.setText(price);
		priceTextView.setOnClickListener(getCostBreakdownListener());

		root.addView(vg);

	}

	@Override
	public void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable headerBitmapDrawable) {
		int placeholderResId = Ui.obtainThemeResID((Activity) getActivity(), R.attr.HotelRowThumbPlaceHolderDrawable);
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null && hotel.getProperty() != null && hotel.getProperty().getThumbnail() != null) {
			hotel.getProperty().getThumbnail().fillHeaderBitmapDrawable(imageView, headerBitmapDrawable, placeholderResId);
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
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		return CalendarUtils.formatDateRange(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
			| DateUtils.FORMAT_ABBREV_MONTH);
	}

	@Override
	public String getTripPrice() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate rate = hotel.getRate();
			return rate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL);
		}
		else {
			return null;
		}
	}

	@Override
	public OnClickListener getCostBreakdownListener() {
		return mCostBreakDownListener;
	}

	private OnClickListener mCostBreakDownListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			BreakdownDialogFragment dialogFrag = BreakdownDialogFragment.buildHotelRateBreakdownDialog(getActivity(),
				Db.getHotelSearch());
			dialogFrag.show(getFragmentManager(), BreakdownDialogFragment.TAG);
		}
	};

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

	public void refreshRate() {
		// Update the price in the expanded tripbucket.
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			Rate rate = hotel.getRate();
			String price = rate.getDisplayTotalPrice().getFormattedMoney();
			Ui.setText(getActivity(), R.id.price_expanded_bucket_text_view, price);
		}

	}
}
