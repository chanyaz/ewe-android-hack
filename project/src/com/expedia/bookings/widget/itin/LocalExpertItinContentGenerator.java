package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.expedia.bookings.data.trips.ItinCardDataLocalExpert;

public class LocalExpertItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataLocalExpert> {
    public LocalExpertItinContentGenerator(Context context, ItinCardDataLocalExpert itinCardData) {
        super(context, itinCardData);
    }

    @Override
    public View getDetailsView(ViewGroup container) {
        return null;
    }
}
