package com.expedia.bookings.widget.itin

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataRails
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.graphics.HeaderBitmapDrawable
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import java.util.ArrayList

class RailsItinContentGenerator(context: Context, val railItinCardData: ItinCardDataRails): ItinContentGenerator<ItinCardDataRails>(context, railItinCardData) {
    override fun hasDetails(): Boolean {
        return false
    }

    override fun getTypeIconResId(): Int {
        return R.drawable.rail_itin_icon
    }

    override fun getType(): TripComponent.Type {
        return TripComponent.Type.RAILS
    }

    override fun getShareSubject(): String? {
        return null
    }

    override fun getShareTextShort(): String? {
        return null
    }

    override fun getShareTextLong(): String? {
        return null
    }

    override fun getHeaderImagePlaceholderResId(): Int {
        return R.drawable.confirmation_background
    }

    override fun getHeaderBitmapDrawable(width: Int, height: Int, target: HeaderBitmapDrawable) {
    }

    override fun getReloadText(): String? {
        return null
    }

    override fun getHeaderText(): String {
        return railItinCardData.tripComponent.parentTrip.title
    }

    override fun getTitleView(convertView: View?, container: ViewGroup?): View? {
        return null
    }

    override fun getDetailsView(convertView: View?, container: ViewGroup?): View? {
        return null
    }

    override fun getSummaryView(convertView: View?, container: ViewGroup?): View {
        var view: TextView
        if (convertView != null) {
            view = convertView as TextView
        }
        else {
            view = layoutInflater.inflate(R.layout.include_itin_card_summary_rail, container, false) as TextView
        }

        view.text = Phrase.from(context, R.string.itin_card_rail_summary_TEMPLATE)
                .put("datetime", DateUtils.dateTimeToEEEMMMdhmma(railItinCardData.startDate)).format()

        return view
    }

    override fun getSummaryLeftButton(): SummaryButton? {
        return null
    }

    override fun getSummaryRightButton(): SummaryButton? {
        return null
    }

    override fun getAddToCalendarIntents(): List<Intent> {
        return ArrayList()
    }
}