package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;

public class CarItinCard extends ItinCard {
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
	protected String getHeaderImageUrl(TripComponent tripComponent) {
		return null;
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		return "Car Card";
	}

	@Override
	protected View getTitleView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	protected void onShareButtonClick(TripComponent tripComponent) {
		// TODO Auto-generated method stub

	}
}