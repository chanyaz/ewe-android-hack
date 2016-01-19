package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices) {
    val hotelParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightParamsObservable = PublishSubject.create<PackageSearchParams>()

    // Outputs

    val destinationTextObservable = BehaviorSubject.create<String>()
    val originTextObservable = BehaviorSubject.create<String>()
    val hotelResultsObservable = BehaviorSubject.create<List<Hotel>>()
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()

    init {
        hotelParamsObservable.subscribe { params ->
            Db.setPackageParams(params)
            packageServices.packageSearch(params).subscribe(makeResultsObserver())
            destinationTextObservable.onNext(context.getString(R.string.flights_to_TEMPLATE, params.destination.regionNames.shortName))
            originTextObservable.onNext(context.getString(R.string.flights_to_TEMPLATE, params.origin.regionNames.shortName))
        }

        flightParamsObservable.subscribe { params ->
            packageServices.packageSearch(params).subscribe(makeResultsObserver())
        }

    }

    fun makeResultsObserver(): Observer<PackageSearchResponse> {
        return object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                Db.setPackageResponse(response)
                hotelResultsObservable.onNext(response.packageResult.hotelsPackage.hotels)
                flightResultsObservable.onNext(response.packageResult.flightsPackage.flights)
                println("package success, Hotels:" + response.packageResult.hotelsPackage.hotels.size + "  Flights:" + response.packageResult.flightsPackage.flights.size)
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