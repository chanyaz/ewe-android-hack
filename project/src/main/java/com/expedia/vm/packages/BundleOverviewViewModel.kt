package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.isMidAPIEnabled
import com.google.gson.Gson
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import retrofit2.HttpException
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
    val searchParamsChangeObservable = PublishSubject.create<Unit>()

    // Outputs
    val autoAdvanceObservable = BehaviorSubject.create<PackageSearchType>()
    val hotelResultsObservable = BehaviorSubject.create<Unit>()
    val flightResultsObservable = BehaviorSubject.create<PackageSearchType>()
    val showBundleTotalObservable = BehaviorSubject.create<Boolean>()
    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val stepOneTextObservable = BehaviorSubject.create<String>()
    val stepOneContentDescriptionObservable = BehaviorSubject.create<String>()
    val stepTwoTextObservable = BehaviorSubject.create<String>()
    val cancelSearchSubject = BehaviorSubject.create<Unit>()
    val airlineFeePackagesWarningTextObservable = PublishSubject.create<String>()

    var searchPackageSubscriber: Subscription? = null

    init {
        hotelParamsObservable.subscribe { params ->
            Db.setPackageParams(params)
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())

            searchPackageSubscriber = packageServices?.packageSearch(params, if (isMidAPIEnabled(context)) ProductSearchType.MultiItemHotels else ProductSearchType.OldPackageSearch)?.subscribe(makeResultsObserver(PackageSearchType.HOTEL))
        }

        flightParamsObservable.subscribe { params ->
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())
            val type = if (params.isOutboundSearch(isMidAPIEnabled(context))) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT

            searchPackageSubscriber = packageServices?.packageSearch(params, getProductSearchType(params.isOutboundSearch(isMidAPIEnabled(context))))?.subscribe(makeResultsObserver(type))
        }

        searchParamsChangeObservable.subscribe {
            stepOneTextObservable.onNext(context.getString(R.string.step_one))
            stepTwoTextObservable.onNext(context.getString(R.string.step_two))
        }

        createTripObservable.subscribe { trip ->
            val hotel = trip.packageDetails.hotel
            val stepOne = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_checkout_overview_TEMPLATE, hotel.numberOfNights.toInt()))
                    .put("number", hotel.numberOfNights)
                    .put("city", hotel.hotelCity)
                    .format().toString()

            stepOneTextObservable.onNext(stepOne)

            val stepOneContentDesc = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_checkout_overview_TEMPLATE_cont_desc, hotel.numberOfNights.toInt()))
                    .put("number", hotel.numberOfNights)
                    .put("city", hotel.hotelCity)
                    .format().toString()
            stepOneContentDescriptionObservable.onNext(stepOneContentDesc)

            val stepTwo = Phrase.from(context, R.string.flight_checkout_overview_TEMPLATE)
                    .put("origin", Db.getPackageParams().origin?.hierarchyInfo?.airport?.airportCode)
                    .put("destination", Db.getPackageParams().destination?.hierarchyInfo?.airport?.airportCode)
                    .format().toString()
            stepTwoTextObservable.onNext(stepTwo)
            setAirlineFeeTextOnBundleOverview()
        }

        cancelSearchObservable.subscribe {
            if (searchPackageSubscriber != null && !searchPackageSubscriber!!.isUnsubscribed) {
                searchPackageSubscriber?.unsubscribe()
                cancelSearchSubject.onNext(Unit)
            }
        }
    }

    private fun getProductSearchType(isOutboundSearch: Boolean): ProductSearchType {
        if (isMidAPIEnabled(context)) {
            if (isOutboundSearch) {
                return ProductSearchType.MultiItemOutboundFlights
            } else {
                return ProductSearchType.MultiItemInboundFlights
            }
        } else {
            return ProductSearchType.OldPackageSearch
        }
    }

    fun makeResultsObserver(type: PackageSearchType): Observer<BundleSearchResponse> {
        return object : Observer<BundleSearchResponse> {
            override fun onNext(response: BundleSearchResponse) {
                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                } else if (response.getHotels().isEmpty()) {
                    errorObservable.onNext(PackageApiError.Code.search_response_null)
                } else {
                    Db.setPackageResponse(response)
                    if (type == PackageSearchType.HOTEL) {
                        hotelResultsObservable.onNext(Unit)
                        val currentFlights = arrayOf(response.getFlightLegs()[0].legId, response.getFlightLegs()[1].legId)
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
                    autoAdvanceObservable.onNext(type)
                    if (response.getCurrentOfferPrice() != null) {
                        showBundleTotalObservable.onNext(true)
                    }
                }
            }

            override fun onCompleted() {
                Log.i("package completed")
            }

            override fun onError(throwable: Throwable?) {
                Log.i("package error: " + throwable?.message)
                if (throwable is HttpException) {
                    try {
                        val response = throwable.response().errorBody()
                        val midError = Gson().fromJson(response?.charStream(), MultiItemApiSearchResponse::class.java)
                        errorObservable.onNext(midError.firstError)
                    } catch (e: Exception) {
                        errorObservable.onNext(PackageApiError.Code.pkg_error_code_not_mapped)
                    }
                } else if (RetrofitUtils.isNetworkError(throwable)) {
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

    fun setAirlineFeeTextOnBundleOverview(){
        if (PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()) {
            airlineFeePackagesWarningTextObservable.onNext(context.getString(R.string.airline_additional_fee_notice))
        } else {
            airlineFeePackagesWarningTextObservable.onNext("")

        }
    }
}

