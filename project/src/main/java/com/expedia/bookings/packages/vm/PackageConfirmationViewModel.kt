package com.expedia.bookings.packages.vm

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.MIDItinDetailsResponse
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
import io.reactivex.Observer
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

open class PackageConfirmationViewModel(private val context: Context) {
    val showConfirmation = PublishSubject.create<Pair<String?, String>>()
    val itinDetailsResponseObservable = PublishSubject.create<MIDItinDetailsResponse>()
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
        setupItinDetailsResponseObservable()
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

    private fun setupItinDetailsResponseObservable() {
        itinDetailsResponseObservable.subscribe { response ->
            val details = response.responseData
            val itinNumber = response.responseData.tripNumber?.toString()
            var email = details.flights.first().passengers.first().emailAddress
            if (email.isNullOrEmpty()) {
                email = details.hotels.first().rooms.first().roomPreferences.primaryOccupant.email
            }
            val hotel = details.hotels.first()
            val guests = details.flights.first().passengers.size
            destinationObservable.onNext(hotel.hotelPropertyInfo.address.city)
            destinationTitleObservable.onNext(hotel.hotelPropertyInfo.name)
            destinationSubTitleObservable.onNext(getHotelSubtitle(hotel.checkInDateTime.toLocalDate().toString(),
                    hotel.checkOutDateTime.toLocalDate().toString(), guests))

            val outboundFlightSegments = details.flights.first().legs.first().segments
            outboundFlightCardTitleObservable.onNext(context.getString(R.string.flight_to,
                    getAirportCodeWithCityTitle(outboundFlightSegments.last().arrivalLocation.airportCode, outboundFlightSegments.last().arrivalLocation.city)))
            outboundFlightCardSubTitleObservable.onNext(getFlightSubtitleFromTripDetails(outboundFlightSegments.first().departureTime.raw, guests))
            val inboundFlightSegments = details.flights.last().legs.last().segments
            inboundFlightCardTitleObservable.onNext(context.getString(R.string.flight_to,
                    getAirportCodeWithCityTitle(inboundFlightSegments.last().arrivalLocation.airportCode, inboundFlightSegments.last().arrivalLocation.city)))

            inboundFlightCardSubTitleObservable.onNext(getFlightSubtitleFromTripDetails(inboundFlightSegments.first().departureTime.raw, guests))
            val itinNumberMessage = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            details.rewardList.firstOrNull()?.totalPoints?.let { points ->
                setRewardsPoints.onNext(points.toString())
            }
            if (!userStateManager.isUserAuthenticated() && !ExpediaBookingApp.isRobolectric()) {
                getItineraryManager().addGuestTrip(email, itinNumber)
            }
        }
    }

    private fun getHotelSubtitle(checkInDate: String, checkOutDate: String, guests: Int): String {
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        var subtitle = Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(checkInDate)))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(formatter.parseLocalDate(checkOutDate)))
                .put("guests", StrUtils.formatGuestString(context, guests))
                .format().toString()

        return subtitle
    }

    private fun getFlightSubtitleFromTripDetails(rawDepartureDateTime: String, guests: Int): String {
        val localDate = DateTime.parse(rawDepartureDateTime).toLocalDate()

        return context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, LocaleBasedDateFormatUtils.localDateToMMMd(localDate),
                FlightV2Utils.formatTimeShort(context, rawDepartureDateTime), StrUtils.formatTravelerString(context, guests))
    }

    private fun getAirportCodeWithCityTitle(airportCode: String, city: String): String {
        return Phrase.from(context, R.string.airport_code_with_city_TEMPLATE)
                .put("airportcode", airportCode)
                .put("city", city)
                .format().toString()
    }

    fun searchForCarRentalsForTripObserver(context: Context): Observer<Unit> {
        return object : DisposableObserver<Unit>() {
            override fun onNext(t: Unit) {
                CarNavUtils.goToCars(context, NavUtils.FLAG_OPEN_SEARCH)
                val activity = context as AppCompatActivity
                activity.setResult(Activity.RESULT_OK)
                activity.finish()
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                throw OnErrorNotImplementedException(e)
            }
        }
    }

    open fun getItineraryManager(): ItineraryManager {
        return ItineraryManager.getInstance()
    }
}
