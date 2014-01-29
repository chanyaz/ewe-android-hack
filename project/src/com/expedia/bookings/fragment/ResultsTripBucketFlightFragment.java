package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketFlightFragment extends TripBucketItemFragment {

	public static ResultsTripBucketFlightFragment newInstance() {
		ResultsTripBucketFlightFragment frag = new ResultsTripBucketFlightFragment();
		return frag;
	}

	private FlightLegSummarySectionTablet mFlightSection;

	@Override
	protected void doBind() {
		bindToDb();
	}

	private void bindToDb() {
		if (mFlightSection != null) {
			// TODO this logic is WHACK. improve selected/added FlightTrip notion
			boolean isCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
			boolean hasSelected = Db.getFlightSearch().getSelectedFlightTrip() != null;
			boolean hasAdded = Db.getFlightSearch().getAddedFlightTrip() != null;
			boolean useAddedLeg = isCheckout || hasAdded;
			if (hasSelected || hasAdded) {
				mFlightSection.bindForTripBucket(Db.getFlightSearch(), useAddedLeg);
			}
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
	public void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup) {
		ViewGroup vg = Ui.inflate(inflater, R.layout.snippet_trip_bucket_expanded_dates_view, viewGroup, false);

		FlightTrip trip = Db.getFlightSearch().getAddedFlightTrip();

		// Dates
		Calendar depDate = trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
		Calendar retDate = trip.getLeg(trip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();
		long start = DateTimeUtils.getTimeInLocalTimeZone(depDate).getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(retDate).getTime();

		String dateRange = DateUtils.formatDateRange(getActivity(), start, end, DateUtils.FORMAT_SHOW_DATE);
		Ui.setText(vg, R.id.dates_text_view, dateRange);

		// Num travelers
		int numTravelers = Db.getFlightSearch().getSearchParams().getNumAdults();
		String numTravStr = getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers);
		Ui.setText(vg, R.id.num_travelers_text_view, numTravStr);

		// Price
		String price = trip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney(Money.F_NO_DECIMAL);
		Ui.setText(vg, R.id.price_expanded_bucket_text_view, price);

		// Hide price in the FlightLeg card
		View priceTv = Ui.findView(mFlightSection, R.id.price_text_view);
		priceTv.setVisibility(View.INVISIBLE);

		viewGroup.addView(vg);
	}

	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			getActivity().startActivity(TabletCheckoutActivity.createIntent(getActivity(), LineOfBusiness.FLIGHTS));
		}
	};
}
