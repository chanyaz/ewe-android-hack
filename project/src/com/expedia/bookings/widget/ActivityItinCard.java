package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class ActivityItinCard extends ItinCard<ItinCardDataActivity> {
	public ActivityItinCard(Context context) {
		this(context, null);
	}

	public ActivityItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public int getTypeIconResId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Type getType() {
		return Type.ACTIVITY;
	}

	@Override
	protected String getHeaderImageUrl(ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected String getHeaderText(ItinCardDataActivity itinCardData) {
		return "Activity Card";
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton(ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected SummaryButton getSummaryRightButton(ItinCardDataActivity itinCardData) {
		return null;
	}

	@Override
	protected void onShareButtonClick(ItinCardDataActivity itinCardData) {
	}
}