package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R


class FlightItinBagaggeInfoViewModel(val context: Context) : ItinWebviewInfoButtonViewModel() {

    override fun updateWidgetWithBaggageInfoUrl(webviewLink: String) {
        createWebviewButtonWidgetSubject.onNext(ItinWebviewInfoButtonViewModel.ItinWebviewInfoButtonWidgetParams(
                context.getString(R.string.itin_baggage_info_button_text),
                R.drawable.ic_baggage_info_icon,
                R.color.app_primary,
                webviewLink))
    }

}