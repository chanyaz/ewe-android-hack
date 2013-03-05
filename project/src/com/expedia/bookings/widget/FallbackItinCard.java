package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataFallback;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class FallbackItinCard extends ItinCard<ItinCardDataFallback> {
	public FallbackItinCard(Context context) {
		this(context, null);
	}

	public FallbackItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getTypeIconResId() {
		switch (this.getItinCardData().getType()) {
		case FLIGHT:
			return R.drawable.ic_type_circle_flight;
		case HOTEL:
			return R.drawable.ic_type_circle_hotel;
		case CAR:
			return R.drawable.ic_type_circle_car;
		case CRUISE:
			return R.drawable.ic_type_circle_cruise;
		default:
			return R.drawable.ic_type_circle_activity;
		}
	}

	@Override
	public Type getType() {
		return getItinCardData().getType();
	}

	@Override
	protected String getShareSubject(ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected String getShareTextShort(ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected String getShareTextLong(ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected int getHeaderImagePlaceholderResId() {
		switch (this.getItinCardData().getType()) {
		case FLIGHT:
			return R.drawable.itin_header_placeholder_activities; //TODO: fix this
		case HOTEL:
			return R.drawable.itin_header_placeholder_activities; //TODO: fix this
		case CAR:
			return R.drawable.itin_header_placeholder_cruises; //TODO: fix this
		case CRUISE:
			return R.drawable.itin_header_placeholder_cruises;
		default:
			return R.drawable.itin_header_placeholder_activities;
		}
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected String getHeaderText(ItinCardDataFallback itinCardData) {
		int resId = 0;
		switch (this.getItinCardData().getType()) {
		case FLIGHT:
			resId = R.string.Flight_TEMPLATE;
			break;
		case HOTEL:
			resId = R.string.Hotel_TEMPLATE;
			break;
		case CAR:
			resId = R.string.Car_TEMPLATE;
			break;
		case CRUISE:
			resId = R.string.Cruise_TEMPLATE;
			break;
		default:
			resId = R.string.Activity_TEMPLATE;
			break;
		}

		return getContext().getString(resId, itinCardData.getRelativeDetailsStartDate(getContext()));
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, final ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(final ItinCardDataFallback itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryRightButton(final ItinCardDataFallback itinCardData) {
		return null;
	}

}
