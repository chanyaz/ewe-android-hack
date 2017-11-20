package com.expedia.vm.packages

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.RewardsUtil
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.navigation.CarNavUtils
import com.expedia.bookings.utils.navigation.NavUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class PackageConfirmationViewModel(private val context: Context) {
    val showConfirmation = PublishSubject.create<Pair<String?, String>>()
    val setRewardsPoints = PublishSubject.create<String>()

    // Outputs
    val itinNumberMessageObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val rewardPointsObservable = PublishSubject.create<String>()
    val destinationTitleObservable = BehaviorSubject.create<String>()
    val destinationSubTitleObservable = BehaviorSubject.create<String>()
    val outboundFlightCardTitleObservable = BehaviorSubject.create<String>()
    val outboundFlightCardSubTitleObservable = BehaviorSubject.create<String>()
    val inboundFlightCardTitleObservable = BehaviorSubject.create<String>()
    val inboundFlightCardSubTitleObservable = BehaviorSubject.create<String>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        showConfirmation.subscribe { pair ->
            val itinNumber = pair.first
            val email = pair.second
            destinationObservable.onNext(Db.getPackageSelectedHotel().city)
            destinationTitleObservable.onNext(Db.getPackageSelectedHotel().localizedName)
            destinationSubTitleObservable.onNext(getHotelSubtitle())
            outboundFlightCardTitleObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
            outboundFlightCardSubTitleObservable.onNext(getFlightSubtitle(Db.getPackageFlightBundle().first))
            inboundFlightCardTitleObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
            inboundFlightCardSubTitleObservable.onNext(getFlightSubtitle(Db.getPackageFlightBundle().second))
            val itinNumberMessage = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            if (!userStateManager.isUserAuthenticated()) {
                getItineraryManager().addGuestTrip(email, itinNumber)
            }
        }

        setRewardsPoints.subscribe { points ->
            if (points != null)
                if (userStateManager.isUserAuthenticated() && PointOfSale.getPointOfSale().shouldShowRewards()) {
                    val rewardPointText = RewardsUtil.buildRewardText(context, points, ProductFlavorFeatureConfiguration.getInstance())
                    if (Strings.isNotEmpty(rewardPointText)) {
                        rewardPointsObservable.onNext(rewardPointText)
                    }
                }
        }
    }

    private fun getHotelSubtitle(): String {
        val hotel = Db.getTripBucket().`package`.mPackageTripResponse.packageDetails.hotel
        val params = Db.getPackageParams()
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        var subtitle = Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(hotel.checkInDate)))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(hotel.checkOutDate)))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format().toString()

        return subtitle
    }

    private fun getFlightSubtitle(selectedFlight: FlightLeg): String {
        val fmt = ISODateTimeFormat.dateTime()
        val localDate = LocalDate.parse(selectedFlight.departureDateTimeISO, fmt)

        return context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, LocaleBasedDateFormatUtils.localDateToMMMd(localDate),
                FlightV2Utils.formatTimeShort(context, selectedFlight.departureDateTimeISO), StrUtils.formatTravelerString(context, Db.getPackageParams().guests))
    }

    fun searchForCarRentalsForTripObserver(context: Context): Observer<Unit> {
        return object : Observer<Unit> {
            override fun onNext(t: Unit?) {
                CarNavUtils.goToCars(context, null, NavUtils.FLAG_OPEN_SEARCH)
                val activity = context as AppCompatActivity
                activity.setResult(Activity.RESULT_OK)
                activity.finish()
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    open fun getItineraryManager(): ItineraryManager {
        return ItineraryManager.getInstance()
    }
}
