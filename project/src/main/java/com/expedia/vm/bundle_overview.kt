package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices?) {
    val hotelParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightParamsObservable = PublishSubject.create<PackageSearchParams>()
    val createTripObservable = PublishSubject.create<PackageCreateTripResponse>()
    val errorObservable = PublishSubject.create<PackageApiError.Code>()

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
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())
            packageServices?.packageSearch(params)?.subscribe(makeResultsObserver(PackageSearchType.HOTEL))
        }

        flightParamsObservable.subscribe { params ->
            packageServices?.packageSearch(params)?.subscribe(makeResultsObserver(if (params.isOutboundSearch()) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT))
        }

        createTripObservable.subscribe { trip ->
            var hotel = trip.packageDetails.hotel
            val stepOne = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_checkout_overview_TEMPLATE, hotel.numberOfNights.toInt()))
                    .put("number", hotel.numberOfNights)
                    .put("city", hotel.hotelCity)
                    .format().toString()
            stepOneTextObservable.onNext(stepOne)

            val stepTwo = Phrase.from(context, R.string.flight_checkout_overview_TEMPLATE)
                    .put("origin", Db.getPackageParams().origin.hierarchyInfo?.airport?.airportCode)
                    .put("destination", Db.getPackageParams().destination.hierarchyInfo?.airport?.airportCode)
                    .format().toString()
            stepTwoTextObservable.onNext(stepTwo)

            val toolbarTitle = Phrase.from(context, R.string.hotel_city_country_TEMPLATE)
                    .put("city", hotel.hotelCity)
                    .put("country", Db.getPackageParams().destination.hierarchyInfo?.country?.name)
                    .format().toString()
            toolbarTitleObservable.onNext(toolbarTitle)
            val toolbarSubtitle = DateFormatUtils.formatPackageDateRange(context, hotel.checkInDate, hotel.checkOutDate)
            toolbarSubtitleObservable.onNext(toolbarSubtitle)

        }
    }

    fun makeResultsObserver(type: PackageSearchType): Observer<PackageSearchResponse> {
        return object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                }
                Db.setPackageResponse(response)
                if (type == PackageSearchType.HOTEL) {
                    hotelResultsObservable.onNext(Unit)
                    val currentFlights = arrayOf(response.packageResult.flightsPackage.flights[0].legId, response.packageResult.flightsPackage.flights[1].legId)
                    Db.getPackageParams().currentFlights = currentFlights
                    Db.getPackageParams().defaultFlights = currentFlights.copyOf()
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
    val hotelDatesGuestObservable = BehaviorSubject.create<String>()
    val hotelRoomImageUrlObservable = BehaviorSubject.create<String>()
    val hotelRoomInfoObservable = BehaviorSubject.create<String>()
    val hotelRoomTypeObservable = BehaviorSubject.create<String>()
    val hotelAddressObservable = BehaviorSubject.create<String>()
    val hotelCityObservable = BehaviorSubject.create<String>()
    val hotelFreeCancellationObservable = BehaviorSubject.create<String>()
    val hotelNonRefundableObservable = BehaviorSubject.create<String>()
    val hotelPromoTextObservable = BehaviorSubject.create<String>()
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
            } else {
                hotelTextObservable.onNext(context.getString(R.string.select_hotel_template, StrUtils.formatCityName(Db.getPackageParams().destination)))
                hotelDatesGuestObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                        .put("startdate", DateUtils.localDateToMMMd(Db.getPackageParams().checkIn))
                        .put("enddate", DateUtils.localDateToMMMd(Db.getPackageParams().checkOut))
                        .put("guests", StrUtils.formatGuestString(context, Db.getPackageParams().guests))
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
            val bedTypes = (selectHotelRoom.bedTypes ?: emptyList()).map { it.description }.joinToString("")
            hotelRoomTypeObservable.onNext(bedTypes)
            hotelAddressObservable.onNext(selectedHotel.address)

            if (selectHotelRoom.hasFreeCancellation) {
                hotelFreeCancellationObservable.onNext(getCancellationText(selectHotelRoom))
            } else {
                hotelNonRefundableObservable.onNext(context.resources.getString(R.string.non_refundable))
            }

            hotelPromoTextObservable.onNext(selectHotelRoom.promoDescription)
            val cityCountry = Phrase.from(context, R.string.hotel_city_country_TEMPLATE)
                    .put("city", selectedHotel.city)
                    .put("country", selectedHotel.stateProvinceCode ?: Db.getPackageParams().destination.hierarchyInfo?.country?.name)
                    .format().toString()
            hotelCityObservable.onNext(cityCountry)
        }
    }

    private fun getCancellationText(selectHotelRoom: HotelOffersResponse.HotelRoomResponse): String? {
        val cancellationDateString = selectHotelRoom.freeCancellationWindowDate
        if (cancellationDateString != null) {
            val cancellationDate = DateUtils.yyyyMMddHHmmToDateTime(cancellationDateString)
            return Phrase.from(context, R.string.hotel_free_cancellation_TEMPLATE).put("date",
                    DateUtils.dateTimeToMMMdhmma(cancellationDate))
                    .format()
                    .toString()
        } else {
            return context.getString(R.string.free_cancellation)
        }
    }
}

class BundlePriceViewModel(val context: Context) {
    val setTextObservable = PublishSubject.create<Pair<String, String>>()
    val total = PublishSubject.create<Money>()
    val savings = PublishSubject.create<Money>()

    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.create<String>()
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()

    init {
        setTextObservable.subscribe { bundle ->
            totalPriceObservable.onNext(bundle.first)
            savingsPriceObservable.onNext(bundle.second)
        }

        Observable.combineLatest(total, savings, { total, savings ->
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", savings.formattedMoney)
                    .format().toString()
            setTextObservable.onNext(Pair(total.formattedMoney, packageSavings))
        }).subscribe()
    }
}

class BundleFlightViewModel(val context: Context) {
    val showLoadingStateObservable = PublishSubject.create<Boolean>()
    val selectedFlightObservable = PublishSubject.create<PackageSearchType>()
    val hotelLoadingStateObservable = BehaviorSubject.create<PackageSearchType>()
    val date = PublishSubject.create<LocalDate>()
    val guests = BehaviorSubject.create<Int>()
    val suggestion = BehaviorSubject.create<SuggestionV4>()
    val flight = BehaviorSubject.create<FlightLeg>()

    //output
    val flightTextObservable = BehaviorSubject.create<String>()
    val travelInfoTextObservable = BehaviorSubject.create<String>()
    val flightDetailsIconObservable = BehaviorSubject.create<Boolean>()
    val flightSelectIconObservable = BehaviorSubject.create<Boolean>()
    val flightIconImageObservable = BehaviorSubject.create<Pair<Int, Int>>()
    val flightTextColorObservable = BehaviorSubject.create<Int>()
    val flightTravelInfoColorObservable = BehaviorSubject.create<Int>()
    val flightInfoContainerObservable = BehaviorSubject.create<Boolean>()
    val selectedFlightLegObservable = BehaviorSubject.create<FlightLeg>()
    val totalDurationObserver = BehaviorSubject.create<String>()

    init {
        Observable.combineLatest(hotelLoadingStateObservable, suggestion, date, guests, { searchType, suggestion, date, guests ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(suggestion)))
                travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(date))
                        .put("travelers", StrUtils.formatTravelerString(context, guests))
                        .format()
                        .toString())
            } else {
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(suggestion)))
                travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", DateUtils.localDateToMMMd(date))
                        .put("travelers", StrUtils.formatTravelerString(context, guests))
                        .format()
                        .toString())
            }
            flightInfoContainerObservable.onNext(false)
            flightDetailsIconObservable.onNext(false)
            flightSelectIconObservable.onNext(false)
            flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        }).subscribe()

        showLoadingStateObservable.subscribe { isShowing ->
            if (isShowing) {
                flightSelectIconObservable.onNext(false)
                flightDetailsIconObservable.onNext(false)
                travelInfoTextObservable.onNext("")
            } else {
                flightSelectIconObservable.onNext(true)
                flightTextColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
                flightTravelInfoColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
            }
        }

        Observable.combineLatest(selectedFlightObservable, flight, suggestion, date, guests, { searchType, flight, suggestion, date, guests ->
            val fmt = ISODateTimeFormat.dateTime();
            val localDate = LocalDate.parse(flight.departureDateTimeISO, fmt)

            flightSelectIconObservable.onNext(false)
            flightDetailsIconObservable.onNext(true)
            flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_primary_text))
            flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_secondary_text))
            travelInfoTextObservable.onNext(context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, DateUtils.localDateToMMMd(localDate),
                    DateUtils.formatTimeShort(flight.departureDateTimeISO), StrUtils.formatTravelerString(context, guests)))
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(suggestion)))
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_checkmark_icon, 0))
            } else {
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(suggestion)))
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_checkmark_icon, 0))
            }
            var totalDuration = Phrase.from(context.resources.getString(R.string.package_flight_overview_total_duration_TEMPLATE))
                    .put("duration", PackageFlightUtils.getFlightDurationString(context, flight))
                    .format().toString()
            totalDurationObserver.onNext(totalDuration)
            selectedFlightLegObservable.onNext(flight)
        }).subscribe()
    }
}

data class PackageBreakdown(val title: String, val cost: String, val isDiscount: Boolean, val isTotalDue: Boolean, val isTotalCost: Boolean, val isLine: Boolean)

class PackageBreakdownViewModel(val context: Context) {
    val newDataObservable = PublishSubject.create<PackageCreateTripResponse.PackageDetails>()
    val iconVisibilityObservable = PublishSubject.create<Boolean>()

    val addRows = BehaviorSubject.create<List<PackageBreakdown>>()

    init {
        newDataObservable.subscribe { packageDetails ->
            var breakdowns = arrayListOf<PackageBreakdown>()
            var title: String

            if (packageDetails.pricing.taxesAndFeesIncluded) {
                breakdowns.add(PackageBreakdown(context.getString(R.string.package_breakdown_hotel_flight_summary), getFormattedMoney(packageDetails.pricing.packageTotal.amount.toDouble(), packageDetails.pricing.packageTotal.currencyCode), false, false, false, false))
                title = Phrase.from(context, R.string.package_breakdown_taxes_fees_included_TEMPLATE).put("taxes", getFormattedMoney(packageDetails.pricing.totalTaxesAndFees.amount.toDouble(), packageDetails.pricing.totalTaxesAndFees.currencyCode)).format().toString()
                breakdowns.add(PackageBreakdown(title, "", false, false, false, false))
            } else {
                breakdowns.add(PackageBreakdown(context.getString(R.string.package_breakdown_hotel_flight_summary), getFormattedMoney(packageDetails.pricing.basePrice.amount.toDouble(), packageDetails.pricing.basePrice.currencyCode), false, false, false, false))
                title = context.getString(R.string.package_breakdown_taxes_fees)
                breakdowns.add(PackageBreakdown(title, getFormattedMoney(packageDetails.pricing.totalTaxesAndFees.amount.toDouble(), packageDetails.pricing.totalTaxesAndFees.currencyCode), false, false, false, false))
            }

            // Adding divider line
            breakdowns.add(PackageBreakdown("", "", false, false, false, true))

            title = context.getString(R.string.package_breakdown_total_savings)
            breakdowns.add(PackageBreakdown(title, getFormattedMoney(packageDetails.pricing.savings.amount.toDouble(), packageDetails.pricing.savings.currencyCode), true, false, false, false))

            title = context.getString(R.string.package_breakdown_total_due_today)
            breakdowns.add(PackageBreakdown(title, getFormattedMoney(packageDetails.pricing.packageTotal.amount.toDouble(), packageDetails.pricing.packageTotal.currencyCode), false, true, false, false))

            addRows.onNext(breakdowns)
            iconVisibilityObservable.onNext(true)
        }
    }

    fun getFormattedMoney(amount: Double, currencyCode: String): String {
        return Money(BigDecimal(amount), currencyCode).formattedMoney
    }
}

enum class PackageSearchType {
    HOTEL, OUTBOUND_FLIGHT, INBOUND_FLIGHT
}