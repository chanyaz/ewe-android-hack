package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices?) {
    val hotelParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightParamsObservable = PublishSubject.create<PackageSearchParams>()
    val createTripObservable = PublishSubject.create<PackageCreateTripResponse>()

    // Outputs
    val hotelResultsObservable = BehaviorSubject.create<Unit>()
    val flightResultsObservable = BehaviorSubject.create<PackageSearchType>()
    val showBundleTotalObservable = BehaviorSubject.create<Boolean>()
    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val stepOneTextObservable = BehaviorSubject.create<String>()
    val stepTwoTextObservable = BehaviorSubject.create<String>()

    init {
        hotelParamsObservable.subscribe { params ->
            Db.setPackageParams(params)
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                    .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests()))
                    .format().toString())
            packageServices?.packageSearch(params)?.subscribe(makeResultsObserver(PackageSearchType.HOTEL))
        }

        flightParamsObservable.subscribe { params ->
            packageServices?.packageSearch(params)?.subscribe(makeResultsObserver(if (params.isOutboundSearch()) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT))
        }

        createTripObservable.subscribe { trip ->
            val stepOne = Phrase.from(context, R.string.hotel_checkout_overview_TEMPLATE).put("city", trip.packageDetails.hotel.hotelCity)
                    .put("rooms", trip.packageDetails.hotel.numberOfRooms)
                    .put("nights", trip.packageDetails.hotel.numberOfNights)
                    .format().toString()
            stepOneTextObservable.onNext(stepOne)

            val stepTwo = Phrase.from(context, R.string.flight_checkout_overview_TEMPLATE)
                    .put("origin", Db.getPackageParams().origin.hierarchyInfo?.airport?.airportCode)
                    .put("destination", Db.getPackageParams().destination.hierarchyInfo?.airport?.airportCode)
                    .format().toString()
            stepTwoTextObservable.onNext(stepTwo)

        }
    }

    fun makeResultsObserver(type: PackageSearchType): Observer<PackageSearchResponse> {
        return object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                Db.setPackageResponse(response)
                if (type == PackageSearchType.HOTEL) {
                    hotelResultsObservable.onNext(Unit)
                    Db.getPackageParams().currentFlights = response.packageResult.flightsPackage.flights[0].legId + "," + response.packageResult.flightsPackage.flights[1].legId
                } else {
                    flightResultsObservable.onNext(type)
                }
                if (response.packageResult.currentSelectedOffer != null) {
                    showBundleTotalObservable.onNext(true)
                    println("package success, Hotels:" + response.packageResult.hotelsPackage.hotels.size + "  Flights:" + response.packageResult.flightsPackage.flights.size)
                }
            }

            override fun onCompleted() {
                println("package completed")
            }

            override fun onError(e: Throwable?) {
                println("package error: " + e?.message)
            }
        }
    }
}


class BundleHotelViewModel(val context: Context) {
    val showLoadingStateObservable = PublishSubject.create<Boolean>()
    val selectedHotelObservable = PublishSubject.create<Unit>()

    //output
    val hotelTextObservable = BehaviorSubject.create<String>()
    val hotelRoomGuestObservable = BehaviorSubject.create<String>()
    val hotelRoomImageUrlObservable = BehaviorSubject.create<String>()
    val hotelRoomInfoObservable = BehaviorSubject.create<String>()
    val hotelRoomTypeObservable = BehaviorSubject.create<String>()
    val hotelAddressObservable = BehaviorSubject.create<String>()
    val hotelCityObservable = BehaviorSubject.create<String>()
    val hotelFreeCancellationObservable = BehaviorSubject.create<String>()
    val hotelDetailsIconObservable = BehaviorSubject.create<Boolean>()
    val hotelSelectIconObservable = BehaviorSubject.create<Boolean>()
    val hotelIconImageObservable = BehaviorSubject.create<Int>()

    init {
        showLoadingStateObservable.subscribe { isShowing ->
            if (isShowing) {
                hotelTextObservable.onNext(context.getString(R.string.progress_searching_hotels_hundreds))
                hotelIconImageObservable.onNext(R.drawable.packages_hotel_icon)
                hotelSelectIconObservable.onNext(false)
                hotelDetailsIconObservable.onNext(false)
            } else if (!isShowing && Db.getPackageSelectedHotel() == null) {
                hotelTextObservable.onNext(context.getString(R.string.select_hotel_template, StrUtils.formatCityName(Db.getPackageParams().destination)))
                hotelRoomGuestObservable.onNext(Phrase.from(context, R.string.room_with_guests_TEMPLATE)
                        .put("guests", StrUtils.formatGuestString(context, Db.getPackageParams().guests()))
                        .format()
                        .toString())
                hotelSelectIconObservable.onNext(true)
                hotelDetailsIconObservable.onNext(false)
            }
        }
        selectedHotelObservable.subscribe {
            val selectedHotel = Db.getPackageSelectedHotel()
            val selectHotelRoom = Db.getPackageSelectedRoom()
            hotelTextObservable.onNext(selectedHotel.localizedName)
            hotelIconImageObservable.onNext(R.drawable.packages_hotels_checkmark_icon)
            hotelSelectIconObservable.onNext(false)
            hotelDetailsIconObservable.onNext(true)
            if (Strings.isNotEmpty(selectHotelRoom.roomThumbnailUrl)) {
                hotelRoomImageUrlObservable.onNext(Images.getMediaHost() + selectHotelRoom.roomThumbnailUrl)
            }
            hotelRoomInfoObservable.onNext(selectHotelRoom.roomTypeDescription)
            hotelRoomTypeObservable.onNext(selectHotelRoom.roomTypeDescription)
            hotelAddressObservable.onNext(selectedHotel.address)
            hotelFreeCancellationObservable.onNext(selectHotelRoom.freeCancellationWindowDate)
            hotelCityObservable.onNext(selectedHotel.stateProvinceCode + " , " + selectedHotel.countryCode)
        }
    }
}

class BundlePriceViewModel(val context: Context) {
    val setTextObservable = PublishSubject.create<Pair<String, String>>()
    val createTripObservable = PublishSubject.create<PackageCreateTripResponse>()

    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.create<String>()
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()

    init {
        setTextObservable.subscribe { bundle ->
            totalPriceObservable.onNext(bundle.first)
            savingsPriceObservable.onNext(bundle.second)
        }

        createTripObservable.subscribe { trip ->
            var packageTotalPrice = trip.packageDetails.pricing
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Money(BigDecimal(packageTotalPrice.savings.amount.toDouble()),
                            packageTotalPrice.savings.currencyCode).formattedMoney)
                    .format().toString()

            setTextObservable.onNext(Pair(Money(BigDecimal(packageTotalPrice.packageTotal.amount.toDouble()),
                    packageTotalPrice.packageTotal.currencyCode).formattedMoney, packageSavings))
        }
    }
}

class BundleFlightViewModel(val context: Context) {
    val showLoadingStateObservable = PublishSubject.create<Boolean>()
    val selectedFlightObservable = PublishSubject.create<PackageSearchType>()
    val hotelLoadingStateObservable = BehaviorSubject.create<PackageSearchType>()

    //output
    val flightTextObservable = BehaviorSubject.create<String>()
    val travelInfoTextObservable = BehaviorSubject.create<String>()
    val flightDetailsIconObservable = BehaviorSubject.create<Boolean>()
    val flightSelectIconObservable = BehaviorSubject.create<Boolean>()
    val flightIconImageObservable = BehaviorSubject.create<Pair<Int, Int>>()
    val flightTextColorObservable = BehaviorSubject.create<Int>()
    val flightInfoContainerObservable = BehaviorSubject.create<Boolean>()
    val selectedFlightLegObservable = BehaviorSubject.create<FlightLeg>()
    val totalDurationObserver = BehaviorSubject.create<String>()

    init {
        hotelLoadingStateObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
                travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkIn))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests()))
                        .format()
                        .toString())
            } else {
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
                travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                        .put("travelers", StrUtils.formatTravelerString(context, Db.getPackageParams().guests()))
                        .format()
                        .toString())
            }
            flightInfoContainerObservable.onNext(false)
            flightDetailsIconObservable.onNext(false)
            flightSelectIconObservable.onNext(false)
            flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        }

        showLoadingStateObservable.subscribe { isShowing ->
            if (isShowing) {
                flightSelectIconObservable.onNext(false)
                flightDetailsIconObservable.onNext(false)
                travelInfoTextObservable.onNext("")
            } else {
                flightSelectIconObservable.onNext(true)
                flightTextColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
            }
        }

        selectedFlightObservable.subscribe { searchType ->
            val selectedFlight = if (searchType == PackageSearchType.OUTBOUND_FLIGHT) Db.getPackageSelectedOutboundFlight() else Db.getPackageSelectedInboundFlight()
            val fmt = ISODateTimeFormat.dateTime();
            val localDate = LocalDate.parse(selectedFlight.departureDateTimeISO, fmt)

            flightSelectIconObservable.onNext(false)
            flightDetailsIconObservable.onNext(true)
            flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            travelInfoTextObservable.onNext(context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, DateUtils.localDateToMMMd(localDate),
                    DateUtils.formatTimeShort(selectedFlight.departureDateTimeISO), StrUtils.formatTravelerString(context, Db.getPackageParams().guests())))
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().destination)))
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_checkmark_icon, 0))
            } else {
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(Db.getPackageParams().origin)))
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_checkmark_icon, 0))
            }
            var totalDuration = Phrase.from(context.resources.getString(R.string.package_flight_overview_total_duration_TEMPLATE))
                    .put("duration", PackageFlightUtils.getFlightDurationString(context, selectedFlight))
                    .format().toString()
            totalDurationObserver.onNext(totalDuration)
            selectedFlightLegObservable.onNext(selectedFlight)
        }
    }
}

enum class PackageSearchType {
    HOTEL, OUTBOUND_FLIGHT, INBOUND_FLIGHT
}