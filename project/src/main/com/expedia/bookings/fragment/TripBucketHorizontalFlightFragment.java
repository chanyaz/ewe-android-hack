package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.TripBucketItemState;

public class TripBucketHorizontalFlightFragment extends TripBucketFlightFragment {

	public static TripBucketHorizontalFlightFragment newInstance() {
		TripBucketHorizontalFlightFragment frag = new TripBucketHorizontalFlightFragment();
		return frag;
	}

	@Override
	protected int getRootLayout() {
		return R.layout.fragment_tablet_horizontal_trip_bucket_item;
	}

	@Override
	public TripBucketItemState getItemState() {
		return TripBucketItemState.EXPANDED;
	}
}
