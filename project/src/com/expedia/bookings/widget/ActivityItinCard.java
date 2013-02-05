package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class ActivityItinCard extends ItinCard {
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
	protected String getHeaderImageUrl(TripComponent tripComponent) {
		return null;
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		return "Activity Card";
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		return null;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SummaryButton getSummaryRightButton() {
		// TODO Auto-generated method stub
		return null;
	}
}