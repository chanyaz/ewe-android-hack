package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.TripBucketItemState;

public class TripBucketHorizontalHotelFragment extends TripBucketHotelFragment {

	public static TripBucketHorizontalHotelFragment newInstance() {
		TripBucketHorizontalHotelFragment frag = new TripBucketHorizontalHotelFragment();
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
