package com.expedia.bookings.fragment;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.widget.FlightTripView;
import com.mobiata.android.util.Ui;

/**
 * ResultsTripBucketYourTripToFragment: A simple fragment for displaying destination information, in the trip overview column - Tablet 2013
 */
public class ResultsTripBucketFlightFragment extends TripBucketItemFragment {

	public static ResultsTripBucketFlightFragment newInstance() {
		ResultsTripBucketFlightFragment frag = new ResultsTripBucketFlightFragment();
		return frag;
	}

	private FlightLegSummarySectionTablet mFlightSection;
	private FlightTripView mFlightTripView;

	@Override
	protected void doBind() {
		bindToDb();
	}

	private void bindToDb() {
		if (mFlightSection != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
			mFlightSection.bindForTripBucket(Db.getFlightSearch());
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
		mFlightTripView = Ui.findView(mFlightSection, R.id.flight_trip_view);
	}

	@Override
	public void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup) {
		View view = new View(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 200);
		view.setLayoutParams(params);
		view.setBackgroundColor(Color.BLUE);
		viewGroup.addView(view);
	}

	@Override
	public OnClickListener getOnBookClickListener() {
		return mBookOnClick;
	}

	private OnClickListener mBookOnClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			setExpanded(true);
		}
	};
}
