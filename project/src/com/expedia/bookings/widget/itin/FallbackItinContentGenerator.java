package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardDataFallback;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class FallbackItinContentGenerator extends ItinContentGenerator<ItinCardDataFallback> {

	public FallbackItinContentGenerator(Context context, ItinCardDataFallback data) {
		super(context, data);
	}

	@Override
	public boolean hasDetails() {
		return false;
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
	public String getShareSubject() {
		return null;
	}

	@Override
	public String getShareTextShort() {
		return null;
	}

	@Override
	public String getShareTextLong() {
		return null;
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload);
	}

	@Override
	public int getHeaderImagePlaceholderResId() {
		switch (this.getItinCardData().getType()) {
		case FLIGHT:
			return R.drawable.bg_itin_fallback_flight;
		case HOTEL:
			return R.drawable.bg_itin_fallback_hotel;
		case CAR:
			return R.drawable.bg_itin_fallback_car;
		case CRUISE:
			return R.drawable.bg_itin_fallback_cruise;
		default:
			return R.drawable.bg_itin_fallback_activity;
		}
	}

	@Override
	public String getHeaderImageUrl() {
		return null;
	}

	@Override
	public String getHeaderText() {
		int resId = 0;
		switch (this.getItinCardData().getType()) {
		case FLIGHT:
			resId = R.string.Flight;
			break;
		case HOTEL:
			resId = R.string.Hotel;
			break;
		case CAR:
			resId = R.string.Car;
			break;
		case CRUISE:
			resId = R.string.Cruise;
			break;
		default:
			resId = R.string.Activity;
			break;
		}

		return getContext().getString(resId);
	}

	@Override
	public View getTitleView(ViewGroup container) {
		return null;
	}

	@Override
	public View getSummaryView(ViewGroup container) {
		return null;
	}

	@Override
	public View getDetailsView(ViewGroup container) {
		return null;
	}

	@Override
	public SummaryButton getSummaryLeftButton() {
		return null;
	}

	@Override
	public SummaryButton getSummaryRightButton() {
		return null;
	}

}
