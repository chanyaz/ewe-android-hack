package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.data.DefaultMedia;
import com.expedia.bookings.data.trips.ItinCardDataFallback;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.utils.Ui;

public class FallbackItinContentGenerator extends ItinContentGenerator<ItinCardDataFallback> {
	@Override
	public List<? extends IMedia> getHeaderBitmapDrawable() {
		ArrayList<DefaultMedia> mediaList = new ArrayList<>();
		DefaultMedia placeholder = new DefaultMedia(Collections.<String>emptyList(), "", getHeaderImagePlaceholderResId());
		placeholder.setIsPlaceholder(true);
		mediaList.add(placeholder);
		return mediaList;
	}

	public FallbackItinContentGenerator(Context context, ItinCardDataFallback data) {
		super(context, data);
	}

	@Override
	public boolean hasDetails() {
		return false;
	}

	@Override
	public int getTypeIconResId() {
		switch (this.getItinCardData().getTripComponentType()) {
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
		return getItinCardData().getTripComponentType();
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
		switch (this.getItinCardData().getTripComponentType()) {
		case FLIGHT:
			return Ui.obtainThemeResID(getContext(), R.attr.skin_itinFlightPlaceholderDrawable);
		case HOTEL:
			return Ui.obtainThemeResID(getContext(), R.attr.skin_itinHotelPlaceholderDrawable);
		case CAR:
			return Ui.obtainThemeResID(getContext(), R.attr.skin_itinCarPlaceholderDrawable);
		case CRUISE:
			return Ui.obtainThemeResID(getContext(), R.attr.skin_itinCruisePlaceholderDrawable);
		default:
			return Ui.obtainThemeResID(getContext(), R.attr.skin_itinDefaultPlaceholderDrawable);
		}
	}

	@Override
	public String getHeaderText() {
		int resId = 0;
		switch (this.getItinCardData().getTripComponentType()) {
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
	public View getTitleView(View convertView, ViewGroup container) {
		return null;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
		return null;
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
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

	@Override
	public List<Intent> getAddToCalendarIntents() {
		return new ArrayList<Intent>();
	}
}
