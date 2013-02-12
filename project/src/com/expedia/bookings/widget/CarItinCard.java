package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class CarItinCard extends ItinCard<ItinCardDataCar> {
	public CarItinCard(Context context) {
		this(context, null);
	}

	public CarItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_car;
	}

	@Override
	public Type getType() {
		return Type.CAR;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataCar itinCardData) {
		return null;
	}

	@Override
	protected String getHeaderText(ItinCardDataCar itinCardData) {
		return itinCardData.getFormattedCarType();
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataCar itinCardData) {
		TextView view = (TextView) inflater.inflate(R.layout.include_itin_card_title_generic, container, false);
		view.setText(getHeaderText(itinCardData));
		return view;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, ItinCardDataCar itinCardData) {
		View view = inflater.inflate(R.layout.include_itin_card_details_car, container, false);
		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataCar itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(final ItinCardDataCar itinCardData) {
		return new SummaryButton(R.drawable.ic_direction, "DIRECTIONS", new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}

	@Override
	protected SummaryButton getSummaryRightButton(final ItinCardDataCar itinCardData) {
		return new SummaryButton(R.drawable.ic_phone, itinCardData.getVendorName().toUpperCase(),
				new OnClickListener() {
					@Override
					public void onClick(View v) {

					}
				});
	}

	@Override
	protected void onShareButtonClick(ItinCardDataCar itinCardData) {
	}
}