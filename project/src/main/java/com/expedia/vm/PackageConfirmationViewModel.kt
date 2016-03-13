package com.expedia.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PackageConfirmationViewModel(val context: Context) {
    val showConfirmation = PublishSubject.create<Pair<String?, String>>()
    val setExpediaRewardsPoints = PublishSubject.create<String>()

    // Outputs
    val itinNumberMessageObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val expediaPointsObservable = BehaviorSubject.create<String>()
    val destinationTitleObservable = BehaviorSubject.create<String>()
    val destinationSubTitleObservable = BehaviorSubject.create<String>()
    val outboundFlightCardTitleObservable = BehaviorSubject.create<String>()
    val outboundFlightCardSubTitleObservable = BehaviorSubject.create<String>()
    val inboundFlightCardTitleObservable = BehaviorSubject.create<String>()
    val inboundFlightCardSubTitleObservable = BehaviorSubject.create<String>()

    init {
        showConfirmation.subscribe { pair ->
            val itinNumber = pair.first
            val email = pair.second
            destinationObservable.onNext(Db.getPackageSelectedHotel().city)
            destinationTitleObservable.onNext(Db.getPackageSelectedHotel().localizedName)
            destinationSubTitleObservable.onNext(getHotelSubtitle())
            outboundFlightCardTitleObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
            outboundFlightCardSubTitleObservable.onNext(getFlightSubtitle(Db.getPackageSelectedOutboundFlight()))
            inboundFlightCardTitleObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
            inboundFlightCardSubTitleObservable.onNext(getFlightSubtitle(Db.getPackageSelectedInboundFlight()))
            val itinNumberMessage = Phrase.from(context, R.string.package_itinerary_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            if (!User.isLoggedIn(context)) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
        }
        if (User.isLoggedIn(context)) {
            setExpediaRewardsPoints.subscribe {
                val rewardsPointsText = Phrase.from(context, R.string.package_confirmation_reward_points)
                        .put("rewardpoints", it)
                        .put("brand", BuildConfig.brand)
                        .format().toString()
                expediaPointsObservable.onNext(rewardsPointsText)
            }
        }
    }

    private fun getHotelSubtitle(): String {
        val params = Db.getPackageParams()
        var subtitle = Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests()))
                .format().toString()

        return subtitle
    }

    private fun getFlightSubtitle(selectedFlight: FlightLeg): String {
        val fmt = ISODateTimeFormat.dateTime();
        val localDate = LocalDate.parse(selectedFlight.departureDateTimeISO, fmt)

        return context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, DateUtils.localDateToMMMd(localDate),
                DateUtils.formatTimeShort(selectedFlight.departureDateTimeISO), StrUtils.formatTravelerString(context, Db.getPackageParams().guests()))
    }
}
