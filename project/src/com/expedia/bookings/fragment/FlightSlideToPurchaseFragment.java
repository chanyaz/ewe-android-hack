package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightBookingActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.SlideToWidget;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;

public class FlightSlideToPurchaseFragment extends Fragment {

	private SlideToWidget mSlider;

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

		TextView price = Ui.findView(v, R.id.trip_price);
		String template = getResources().getString(R.string.your_card_will_be_charged_TEMPLATE);
		String text = String.format(template, Db.getFlightSearch().getSelectedFlightTrip().getTotalFare()
				.getFormattedMoney());
		price.setText(text);

		mSlider = Ui.findView(v, R.id.slide_to_wid);
		mSlider.addSlideToListener(new ISlideToListener() {

			@Override
			public void onSlideStart() {
			}

			@Override
			public void onSlideAllTheWay() {
				Db.getBillingInfo().save(getActivity());
				Intent intent = new Intent(getActivity(), FlightBookingActivity.class);
				startActivity(intent);

			}

			@Override
			public void onSlideAbort() {
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
		if(mSlider != null){
			mSlider.resetSlider();
		}
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
