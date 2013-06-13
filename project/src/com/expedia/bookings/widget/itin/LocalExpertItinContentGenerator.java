package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataLocalExpert;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class LocalExpertItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataLocalExpert> {
	public LocalExpertItinContentGenerator(Context context, ItinCardDataLocalExpert itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public Type getType() {
		return Type.HOTEL;
	}

	@Override
	public View getDetailsView(ViewGroup container) {
		return getLayoutInflater().inflate(R.layout.include_itin_button_local_expert, container, false);
	}

	@Override
	public View.OnClickListener getOnItemClickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: start Local Expert fragment
			}
		};
	}
}
