package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.data.trips.TripComponent;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

public abstract class ItinButtonContentGenerator<T extends ItinCardData> extends ItinContentGenerator<T>{
	public ItinButtonContentGenerator(Context context, T itinCardData) {
        super(context, itinCardData);
	}

    // Implement these

    // This may seem strange to return a runnable instead of just
    // calling this to execute the code, however this is a content
    // GENERATOR, and it would be a SoC conflict to run this code
    // from within the generator instead of the actual view
    public abstract Runnable getOnItemClickRunnable(Context context);

    // Ignore these

    @Override
    public final int getTypeIconResId() {
        return 0;
    }

    @Override
    public final TripComponent.Type getType() {
        return null;
    }

    @Override
    public final String getShareSubject() {
        return null;
    }

    @Override
    public final String getShareTextShort() {
        return null;
    }

    @Override
    public final String getShareTextLong() {
        return null;
    }

    @Override
    public final int getHeaderImagePlaceholderResId() {
        return 0;
    }

    @Override
    public final UrlBitmapDrawable getHeaderBitmapDrawable(int width, int height) {
        return null;
    }

    @Override
    public final String getHeaderText() {
        return null;
    }

    @Override
    public final String getReloadText() {
        return null;
    }

    @Override
    public final View getTitleView(View convertView, ViewGroup container) {
        return null;
    }

    @Override
    public final View getSummaryView(View convertView, ViewGroup container) {
        return null;
    }

    @Override
    public final SummaryButton getSummaryLeftButton() {
        return null;
    }

    @Override
    public final SummaryButton getSummaryRightButton() {
        return null;
    }
}
