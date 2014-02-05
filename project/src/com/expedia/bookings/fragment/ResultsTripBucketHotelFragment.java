package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.HotelSummarySection;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketHotelFragment extends TripBucketItemFragment {

	public static ResultsTripBucketHotelFragment newInstance() {
		ResultsTripBucketHotelFragment frag = new ResultsTripBucketHotelFragment();
		return frag;
	}

	private HotelSummarySection mHotelSection;

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
			String price = rate.getDisplayTotalPrice().getFormattedMoney(Money.F_NO_DECIMAL);
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
			getActivity().startActivity(TabletCheckoutActivity.createIntent(getActivity(), LineOfBusiness.HOTELS));
		}
	};
}
