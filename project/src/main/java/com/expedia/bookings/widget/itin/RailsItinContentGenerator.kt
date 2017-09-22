package com.expedia.bookings.widget.itin

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.IMedia
import com.expedia.bookings.data.trips.ItinCardDataRails
import com.expedia.bookings.data.trips.TripComponent
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import java.util.ArrayList
import java.util.Collections

class RailsItinContentGenerator(context: Context, val railItinCardData: ItinCardDataRails): ItinContentGenerator<ItinCardDataRails>(context, railItinCardData) {

    override fun hasDetails(): Boolean {
        return false
    }

    override fun getTypeIconResId(): Int {
        return Ui.obtainThemeResID(context, R.attr.itin_card_list_icon_rail_drawable)
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

    override fun getHeaderBitmapDrawable(): List<IMedia> {
        return Collections.emptyList()
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
        val view: TextView
        if (convertView != null) {
            view = convertView as TextView
        }
        else {
            view = layoutInflater.inflate(R.layout.include_itin_card_summary_rail, container, false) as TextView
        }

        view.text = Phrase.from(context, R.string.itin_card_rail_summary_TEMPLATE)
                .put("datetime", LocaleBasedDateFormatUtils.dateTimeToEEEMMMdhmma(railItinCardData.startDate)).format()

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