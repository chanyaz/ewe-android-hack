package com.expedia.bookings.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;

public class CompleteBookingInfoFragment extends Fragment {

	public static CompleteBookingInfoFragment newInstance() {
		CompleteBookingInfoFragment fragment = new CompleteBookingInfoFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_complete_booking_info, container, false);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((TabletActivity) getActivity()).completeBookingInfo();
			}
		});
		return view;
	}
	
}
