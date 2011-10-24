package com.expedia.bookings.fragment;

import java.util.Locale;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.SupportUtils;
import com.mobiata.android.util.AndroidUtils;

public class NextOptionsFragment extends Fragment {

	public static NextOptionsFragment newInstance() {
		NextOptionsFragment fragment = new NextOptionsFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_next_options, container, false);

		View shareBookingButton =  view.findViewById(R.id.share_booking_info_button);
		shareBookingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String contactText = "";
 
				if (AndroidUtils.hasTelephonyFeature(getActivity())) {
					if (Locale.getDefault().getCountry().toUpperCase().equals("CN")) {
						// Special case for China
						contactText = getString(R.string.contact_phone_china_template, "10-800712-2608",
								"10-800120-2608");
					}
					else if (SupportUtils.hasConfSupportNumber()) {
						contactText = getString(R.string.contact_phone_template, SupportUtils.getConfSupportNumber());
					}
					else {
						contactText = getString(R.string.contact_phone_default_template, "1-800-780-5733",
								"00-800-11-20-11-40");
					}
				}

				Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
				Rate rate = ((TabletActivity) getActivity()).getRoomRateForBooking();
				BookingResponse bookingResponse = ((TabletActivity) getActivity()).getBookingResponse();
				BillingInfo billingInfo = ((TabletActivity) getActivity()).getBillingInfo();
				SearchParams searchParams = ((TabletActivity) getActivity()).getSearchParams();
				ConfirmationUtils.share(getActivity(), searchParams, property, bookingResponse, billingInfo, rate,
						contactText);
			}
		});
		
		View showOnMapButton = view.findViewById(R.id.show_on_map_button);
		showOnMapButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
				startActivity(ConfirmationUtils.generateIntentToShowPropertyOnMap(property));
			}
		});

		View nextSearchButton = view.findViewById(R.id.start_new_search_button);
		nextSearchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((TabletActivity) getActivity()).startNewSearchFromConfirmation();
			}
		});
		return view;
	}
}
