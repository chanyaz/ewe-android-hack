package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightBookingActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class FlightSlideToPurchaseFragment extends Fragment {

	public static FlightSlideToPurchaseFragment newInstance() {
		FlightSlideToPurchaseFragment fragment = new FlightSlideToPurchaseFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutSlideToPurchase(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_slide_to_purchase, container, false);

		View slideToPurchaseView = Ui.findView(v, R.id.slider_container);
		slideToPurchaseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Kick off info save...
				Db.getBillingInfo().save(getActivity());
				Intent intent = new Intent(getActivity(), FlightBookingActivity.class);
				startActivity(intent);
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}
