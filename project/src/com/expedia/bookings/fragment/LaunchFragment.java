package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.activity.SearchFragmentActivity;
import com.mobiata.android.util.Ui;

public class LaunchFragment extends Fragment {

	public static final String TAG = LaunchFragment.class.toString();

	public static LaunchFragment newInstance() {
		return new LaunchFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_launch, container, false);

		Ui.findView(v, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		return v;
	}

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.hotels_button:
				// #11076 - for Android 3.0, we still use the phone version of the app due to crippling bugs.
				Class<? extends Activity> routingTarget = ExpediaBookingApp.useTabletInterface(getActivity()) ? SearchFragmentActivity.class
						: PhoneSearchActivity.class;

				startActivity(new Intent(getActivity(), routingTarget));
				break;
			case R.id.flights_button:
				startActivity(new Intent(getActivity(), FlightSearchActivity.class));
				break;
			}
		}

	};

}
