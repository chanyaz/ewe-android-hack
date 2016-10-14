package com.expedia.bookings.widget.itin;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;

public class CruiseItinContentGenerator extends ItinContentGenerator<ItinCardData> {

	public CruiseItinContentGenerator(Context context, ItinCardData itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public boolean hasDetails() {
		return false;
	}

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_cruise;
	}

	@Override
	public Type getType() {
		return Type.CRUISE;
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
	public int getHeaderImagePlaceholderResId() {
		return R.drawable.itin_header_placeholder_cruises;
	}

	@Override
	public void getHeaderBitmapDrawable(int width, int height, HeaderBitmapDrawable target) {
	}

	@Override
	public String getReloadText() {
		return getContext().getString(R.string.itin_card_details_reload_cruise);
	}

	@Override
	public String getHeaderText() {
		return "Cruise Card";
	}

	@Override
	public View getTitleView(View convertView, ViewGroup container) {
		return null;
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		return null;
	}

	@Override
	public View getSummaryView(View convertView, ViewGroup container) {
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
