package com.expedia.vm

import android.content.Context
import android.util.TypedValue
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Akeakamai
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.SuggestionStrUtils
import com.mobiata.android.util.AndroidUtils
import org.joda.time.format.DateTimeFormat
import rx.subjects.PublishSubject

class FlightCheckoutOverviewViewModel(context: Context) : BaseCheckoutOverviewViewModel(context,
        FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
                AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview, R.string.preference_show_more_info_on_flight_overview)) {
    val width = AndroidUtils.getScreenSize(context).x
    val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 165f, context.resources.displayMetrics).toInt()
    val params = PublishSubject.create<FlightSearchParams>()

    init {
        params.subscribe { params ->
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            val city = SuggestionStrUtils.formatCityName(HtmlCompat.stripHtml(params.arrivalAirport.regionNames.displayName)).trim()
            val link = Akeakamai(Images.getFlightDestination(params?.arrivalAirport?.hierarchyInfo?.airport?.airportCode))
                    .resizeExactly(width, height)
                    .build()
            val links = listOf<String>(link)

            cityTitle.onNext(city)
            checkIn.onNext(params?.departureDate?.toString(formatter))
            checkOut.onNext(params?.returnDate?.toString(formatter))
            guests.onNext(params.guests)
            url.onNext(links)
        }
    }
}