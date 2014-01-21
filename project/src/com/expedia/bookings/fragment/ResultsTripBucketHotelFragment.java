package com.expedia.bookings.fragment;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.fragment.base.TripBucketItemFragment;
import com.expedia.bookings.section.HotelSummarySection;

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
		bindToDb();
	}

	private void bindToDb() {
		if (mHotelSection != null && Db.getHotelSearch() != null && Db.getHotelSearch().getAddedProperty() != null) {
			mHotelSection.bind(Db.getHotelSearch().getAddedProperty(), false, 16, false, DistanceUnit.MILES,
					false);
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
	public void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup) {
		View view = new View(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 400);
		view.setLayoutParams(params);
		view.setBackgroundColor(Color.RED);
		viewGroup.addView(view);
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
