package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class CruiseItinCard extends ItinCard {
	public CruiseItinCard(Context context) {
		this(context, null);
	}

	public CruiseItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getTypeIconResId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Type getType() {
		return Type.CRUISE;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardData itinCardData) {
		return null;
	}

	@Override
	protected String getHeaderText(ItinCardData itinCardData) {
		return "Cruise Card";
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardData itinCardData) {
		return null;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, ItinCardData itinCardData) {
		return null;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardData itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(ItinCardData itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryRightButton(ItinCardData itinCardData) {
		return null;
	}

	@Override
	protected void onShareButtonClick(ItinCardData itinCardData) {
	}
}