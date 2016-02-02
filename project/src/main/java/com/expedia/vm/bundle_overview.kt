package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices) {
    val hotelParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightParamsObservable = PublishSubject.create<PackageSearchParams>()

    // Outputs
    val hotelResultsObservable = BehaviorSubject.create<Unit>()
    val flightResultsObservable = BehaviorSubject.create<PackageSearchType>()
    val showBundleTotalObservable = BehaviorSubject.create<Boolean>()

    init {
        hotelParamsObservable.subscribe { params ->
            Db.setPackageParams(params)
            packageServices.packageSearch(params).subscribe(makeResultsObserver(PackageSearchType.HOTEL))
        }

        flightParamsObservable.subscribe { params ->
            packageServices.packageSearch(params).subscribe(makeResultsObserver(if (params.isOutboundSearch()) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT))
        }
    }

    fun makeResultsObserver(type: PackageSearchType): Observer<PackageSearchResponse> {
        return object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                Db.setPackageResponse(response)
                hotelResultsObservable.onNext(Unit)
                if (type != PackageSearchType.HOTEL) {
                    flightResultsObservable.onNext(type)
                }
                if (!response.packageResult.currentSelectedOffer.equals(null)) {
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
    val hotelArrowIconObservable = BehaviorSubject.create<Boolean>()

    init {
        showLoadingStateObservable.subscribe { isShowing ->
            if (isShowing)
                hotelTextObservable.onNext(context.getString(R.string.hotels_in_TEMPLATE, Db.getPackageParams().destination.regionNames.shortName))
        }
        selectedHotelObservable.subscribe {
            val selectedHotel = Db.getPackageSelectedHotel()
            val selectHotelRoom = Db.getPackageSelectedRoom()
            hotelTextObservable.onNext(selectedHotel.localizedName)
            hotelArrowIconObservable.onNext(true)
            hotelRoomGuestObservable.onNext(context.getString(R.string.hotels_guest_TEMPLATE, Db.getPackageParams().guests()))
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

    val totalPriceObservable = BehaviorSubject.create<String>()
    val savingsPriceObservable = BehaviorSubject.create<String>()
    val bundleTextLabelObservable = BehaviorSubject.create<String>()
    val perPersonTextLabelObservable = BehaviorSubject.create<Boolean>()

    init {
        setTextObservable.subscribe { bundle ->
            totalPriceObservable.onNext(bundle.first)
            savingsPriceObservable.onNext(bundle.second)
        }
    }
}

class BundleFlightViewModel(val context: Context) {
    val showLoadingStateObservable = PublishSubject.create<Boolean>()
    val selectedFlightObservable = PublishSubject.create<PackageSearchType>()

    //output
    val flightTextObservable = BehaviorSubject.create<String>()
    val travelInfoTextObservable = BehaviorSubject.create<String>()
    val flightArrowIconObservable = BehaviorSubject.create<Boolean>()


    init {
        selectedFlightObservable.subscribe { searchType ->
            val selectedFlight = if (searchType == PackageSearchType.OUTBOUND_FLIGHT) Db.getPackageSelectedOutboundFlightFlight() else Db.getPackageSelectedInboundFlightFlight()
            val fmt = ISODateTimeFormat.dateTime();
            val localDate = LocalDate.parse(selectedFlight.departureDateTimeISO, fmt)

            val numOfTravelers = Db.getPackageParams().guests()
            val travelersStr = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numOfTravelers, numOfTravelers)
            travelInfoTextObservable.onNext(context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, DateUtils.localDateToMMMd(localDate), selectedFlight.departureTimeShort, travelersStr))
            flightArrowIconObservable.onNext(true)
        }
    }
}

enum class PackageSearchType {
    HOTEL, OUTBOUND_FLIGHT, INBOUND_FLIGHT
}