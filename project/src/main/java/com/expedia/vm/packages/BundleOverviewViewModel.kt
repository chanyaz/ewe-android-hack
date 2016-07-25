package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices?) {
    val hotelParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightParamsObservable = PublishSubject.create<PackageSearchParams>()
    val createTripObservable = PublishSubject.create<PackageCreateTripResponse>()
    val errorObservable = PublishSubject.create<PackageApiError.Code>()
    val cancelSearchObservable = PublishSubject.create<Unit>()
    val showSearchObservable = PublishSubject.create<Unit>()

    // Outputs
    val hotelResultsObservable = BehaviorSubject.create<Unit>()
    val flightResultsObservable = BehaviorSubject.create<PackageSearchType>()
    val showBundleTotalObservable = BehaviorSubject.create<Boolean>()
    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val stepOneTextObservable = BehaviorSubject.create<String>()
    val stepTwoTextObservable = BehaviorSubject.create<String>()
    val cancelSearchSubject = BehaviorSubject.create<Unit>()

    var searchPackageSubscriber: Subscription? = null

    init {
        hotelParamsObservable.subscribe { params ->
            Db.setPackageParams(params)
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.startDate))
                    .put("enddate", DateUtils.localDateToMMMd(params.endDate))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())
            searchPackageSubscriber = packageServices?.packageSearch(params)?.subscribe(makeResultsObserver(PackageSearchType.HOTEL))
        }

        flightParamsObservable.subscribe { params ->
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", DateUtils.localDateToMMMd(params.startDate))
                    .put("enddate", DateUtils.localDateToMMMd(params.endDate))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())
            searchPackageSubscriber = packageServices?.packageSearch(params)?.subscribe(makeResultsObserver(if (params.isOutboundSearch()) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT))
        }

        createTripObservable.subscribe { trip ->
            var hotel = trip.packageDetails.hotel
            val stepOne = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_checkout_overview_TEMPLATE, hotel.numberOfNights.toInt()))
                    .put("number", hotel.numberOfNights)
                    .put("city", hotel.hotelCity)
                    .format().toString()
            stepOneTextObservable.onNext(stepOne)

            val stepTwo = Phrase.from(context, R.string.flight_checkout_overview_TEMPLATE)
                    .put("origin", Db.getPackageParams().origin?.hierarchyInfo?.airport?.airportCode)
                    .put("destination", Db.getPackageParams().destination?.hierarchyInfo?.airport?.airportCode)
                    .format().toString()
            stepTwoTextObservable.onNext(stepTwo)
        }

        cancelSearchObservable.subscribe {
            if (searchPackageSubscriber != null && !searchPackageSubscriber!!.isUnsubscribed) {
                searchPackageSubscriber?.unsubscribe()
                cancelSearchSubject.onNext(Unit)
            }
        }
    }

    fun makeResultsObserver(type: PackageSearchType): Observer<PackageSearchResponse> {
        return object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                } else if (response.packageResult.hotelsPackage.hotels.isEmpty()) {
                    errorObservable.onNext(PackageApiError.Code.search_response_null)
                } else {
                    Db.setPackageResponse(response)
                    if (type == PackageSearchType.HOTEL) {
                        hotelResultsObservable.onNext(Unit)
                        val currentFlights = arrayOf(response.packageResult.flightsPackage.flights[0].legId, response.packageResult.flightsPackage.flights[1].legId)
                        Db.getPackageParams().currentFlights = currentFlights
                        Db.getPackageParams().defaultFlights = currentFlights.copyOf()
                        PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE)
                    } else {
                        if (type == PackageSearchType.OUTBOUND_FLIGHT) {
                            PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE)
                        } else {
                            PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE)
                        }
                        flightResultsObservable.onNext(type)
                    }
                    if (response.packageResult.currentSelectedOffer != null) {
                        showBundleTotalObservable.onNext(true)
                        println("package success, Hotels:" + response.packageResult.hotelsPackage.hotels.size + "  Flights:" + response.packageResult.flightsPackage.flights.size)
                    }
                }
            }

            override fun onCompleted() {
                println("package completed")
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        if (type.equals(PackageSearchType.HOTEL)) {
                            hotelParamsObservable.onNext(Db.getPackageParams())
                        }
                        else {
                            flightParamsObservable.onNext(Db.getPackageParams())
                        }
                    }
                    val cancelFun = fun() {
                        showSearchObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }
        }
    }
}

