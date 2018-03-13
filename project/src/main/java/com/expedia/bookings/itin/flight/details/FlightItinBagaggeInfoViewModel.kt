package com.expedia.bookings.itin.flight.details

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinWebviewInfoButtonViewModel

class FlightItinBagaggeInfoViewModel(val context: Context) : ItinWebviewInfoButtonViewModel() {

    override fun updateWidgetWithBaggageInfoUrl(webviewLink: String) {
        createWebviewButtonWidgetSubject.onNext(ItinWebviewInfoButtonWidgetParams(
                context.getString(R.string.itin_baggage_info_button_text),
                R.drawable.ic_baggage_info_icon,
                R.color.app_primary,
                webviewLink))
    }
}
