package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;

import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;

public abstract class AttachCardContentGenerator<T extends ItinCardData> {
	private Context mContext;
	private T mItinCardData;

	public AttachCardContentGenerator(Context context, T itinCardData) {
		mContext = context;
		mItinCardData = itinCardData;
	}

	public T getItinCardData() {
		return mItinCardData;
	}

	protected Context getContext() {
		return mContext;
	}

	protected Resources getResources() {
		return mContext.getResources();
	}

	protected LayoutInflater getLayoutInflater() {
		return (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	//////////////////////////////////////////////////////////////////////////
	// Abstract methods to implement

	public abstract int getButtonImageResId();

	public abstract String getButtonText();

	public static AttachCardContentGenerator<? extends ItinCardData> createGenerator(Context context,
			ItinCardData itinCardData) {

		if (itinCardData instanceof ItinCardDataHotelAttach) {
			return new HotelAttachCardContentGenerator(context, (ItinCardDataHotelAttach) itinCardData);
		}

		return null;
	}
}
