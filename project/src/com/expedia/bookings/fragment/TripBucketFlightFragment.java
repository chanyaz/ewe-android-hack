package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class TripBucketFlightFragment extends TripBucketItemFragment {

	private FlightTrip mFlightTrip;

	public static TripBucketFlightFragment newInstance() {
		TripBucketFlightFragment frag = new TripBucketFlightFragment();
		return frag;
	}

	private FlightLegSummarySectionTablet mFlightSection;

	private ViewGroup mExpandedView;

	boolean mIsOnCheckout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIsOnCheckout = getParentFragment() instanceof TabletCheckoutControllerFragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		doBind();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void doBind() {
		bindToDb();
	}

	private void bindToDb() {
		if (mFlightSection != null) {
			FlightTrip trip = null;
			FlightTripLeg[] legs = null;
			boolean isRoundTrip = false;

			if (mIsOnCheckout) {
				trip = Db.getFlightSearch().getSelectedFlightTrip();
				legs = Db.getFlightSearch().getSelectedLegs();
				isRoundTrip = legs.length > 1;
			}
			else {
				if (Db.getTripBucket().getFlight() != null) {
					trip = Db.getTripBucket().getFlight().getFlightTrip();
					int numLegs = Db.getFlightSearch().getSearchParams().isRoundTrip() ? 2 : 1;
					legs = Db.getTripBucket().getFlight().getFlightSearchState().getSelectedLegs(numLegs);
					isRoundTrip = legs.length > 1;
				}
			}
			if (trip != null && legs != null) {
				mFlightSection.bindForTripBucket(trip, legs, isRoundTrip);
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
		if (mIsOnCheckout) {
			mFlightTrip = Db.getFlightSearch().getSelectedFlightTrip();
		}
		else {
			mFlightTrip = Db.getTripBucket().getFlight().getFlightTrip();
		}

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
		if (Db.hasBillingInfo()) {
			String price = mFlightTrip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney();
			Ui.setText(mExpandedView, R.id.price_expanded_bucket_text_view, price);
		}
		else {
			Ui.showToast(getActivity(), "TODO fix billing data load timing issue!");
		}

		// Hide price in the FlightLeg card
		View priceTv = Ui.findView(mFlightSection, R.id.price_text_view);
		priceTv.setVisibility(View.INVISIBLE);
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
}
