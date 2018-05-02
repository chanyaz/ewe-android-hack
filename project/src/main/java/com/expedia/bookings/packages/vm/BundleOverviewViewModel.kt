package com.expedia.bookings.packages.vm

import android.content.Context
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.PackageErrorDetails
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isBreadcrumbsPackagesEnabled
import com.expedia.bookings.utils.isMidAPIEnabled
import com.google.gson.Gson
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import retrofit2.HttpException

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices?) {
    val hotelParamsObservable = PublishSubject.create<PackageSearchParams>()
    val flightParamsObservable = PublishSubject.create<PackageSearchParams>()
    val createTripObservable = PublishSubject.create<PackageCreateTripResponse>()
    val errorObservable = PublishSubject.create<Pair<PackageApiError.Code, ApiCallFailing>>()
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
    val stepThreeTextObservale = PublishSubject.create<String>()
    val cancelSearchSubject = BehaviorSubject.create<Unit>()
    val airlineFeePackagesWarningTextObservable = PublishSubject.create<String>()
    val splitTicketBaggageFeesLinksObservable = PublishSubject.create<SpannableStringBuilder>()
    val showSplitTicketMessagingObservable = PublishSubject.create<Boolean>()

    var searchPackageSubscriber: Disposable? = null

    init {
        hotelParamsObservable.subscribe { params ->
            Db.setPackageParams(params)
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())

            searchPackageSubscriber = packageServices?.packageSearch(params, ProductSearchType.MultiItemHotels)?.subscribeObserver(makeResultsObserver(PackageSearchType.HOTEL, params.isChangePackageSearch()))
        }

        flightParamsObservable.subscribe { params ->
            val cityName = StrUtils.formatCity(params.destination)
            toolbarTitleObservable.onNext(java.lang.String.format(context.getString(R.string.your_trip_to_TEMPLATE), cityName))
            toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                    .put("guests", StrUtils.formatTravelerString(context, params.guests))
                    .format().toString())
            val type = if (params.isOutboundSearch(isMidAPIEnabled())) PackageSearchType.OUTBOUND_FLIGHT else PackageSearchType.INBOUND_FLIGHT

            searchPackageSubscriber = packageServices?.packageSearch(params, getProductSearchType(params.isOutboundSearch(isMidAPIEnabled())))?.subscribeObserver(makeResultsObserver(type, params.isChangePackageSearch()))
        }

        searchParamsChangeObservable.subscribe {
            stepOneTextObservable.onNext(getStepText(1))
            stepTwoTextObservable.onNext(getStepText(2))
            stepThreeTextObservale.onNext(getStepText(3))
        }

        createTripObservable.subscribe { trip ->
            val hotel = trip.packageDetails.hotel
            setUpTitle(hotel.hotelCity, hotel.numberOfNights)
        }

        cancelSearchObservable.subscribe {
            if (searchPackageSubscriber != null && !searchPackageSubscriber!!.isDisposed) {
                searchPackageSubscriber?.dispose()
                cancelSearchSubject.onNext(Unit)
            }
        }
    }

    private fun setUpTitle(hotelCity: String, numberOfNights: String) {
        val stepOne = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_checkout_overview_TEMPLATE, numberOfNights.toInt()))
                .put("number", numberOfNights)
                .put("city", hotelCity)
                .format().toString()

        stepOneTextObservable.onNext(stepOne)

        val stepOneContentDesc = Phrase.from(context.resources.getQuantityString(R.plurals.hotel_checkout_overview_TEMPLATE_cont_desc, numberOfNights.toInt()))
                .put("number", numberOfNights)
                .put("city", hotelCity)
                .format().toString()
        stepOneContentDescriptionObservable.onNext(stepOneContentDesc)

        val stepTwo = Phrase.from(context, R.string.flight_checkout_overview_TEMPLATE)
                .put("origin", Db.sharedInstance.packageParams.origin?.hierarchyInfo?.airport?.airportCode)
                .put("destination", Db.sharedInstance.packageParams.destination?.hierarchyInfo?.airport?.airportCode)
                .format().toString()
        stepTwoTextObservable.onNext(stepTwo)
        stepThreeTextObservale.onNext("")
        setAirlineFeeTextOnBundleOverview()
        if (isMidAPIEnabled()) {
            setSplitTicketMessagingOnBundleOverview(Db.sharedInstance.packageParams)
        }
    }

    fun getHotelNameAndDaysToSetUpTitle() {
        val packageResponse = Db.getPackageResponse()
        val hotel = Db.getPackageSelectedHotel()
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        val checkInDate = dtf.parseLocalDate(packageResponse.getHotelCheckInDate())
        val checkoutDate = dtf.parseLocalDate(packageResponse.getHotelCheckOutDate())
        val numOfDaysStay = Days.daysBetween(checkInDate, checkoutDate).days.toString()
        setUpTitle(hotel.city, numOfDaysStay)
    }

    private fun getProductSearchType(isOutboundSearch: Boolean): ProductSearchType {
        if (isOutboundSearch) {
            return ProductSearchType.MultiItemOutboundFlights
        } else {
            return ProductSearchType.MultiItemInboundFlights
        }
    }

    private fun getStepText(stepNumber: Number) = when (stepNumber) {
        1 -> context.getString(R.string.step_one)
        2 -> if (isBreadcrumbsPackagesEnabled(context)) context.getString(R.string.step_two_variation) else context.getString(R.string.step_two)
        3 -> if (isBreadcrumbsPackagesEnabled(context)) context.getString(R.string.step_three) else ""
        else -> ""
    }

    private fun getApiCallFailingDetails(type: PackageSearchType, isChangeSearch: Boolean, errorDetails: PackageErrorDetails.PackageAPIErrorDetails): Pair<PackageApiError.Code, ApiCallFailing> {
        val apiCallFailingDetails = when (type) {
            PackageSearchType.HOTEL -> if (isChangeSearch) ApiCallFailing.PackageHotelChange(errorDetails.errorKey) else ApiCallFailing.PackageHotelSearch(errorDetails.errorKey)
            PackageSearchType.OUTBOUND_FLIGHT -> if (isChangeSearch) ApiCallFailing.PackageFlightOutboundChange(errorDetails.errorKey) else ApiCallFailing.PackageFlightOutbound(errorDetails.errorKey)
            PackageSearchType.INBOUND_FLIGHT -> if (isChangeSearch) ApiCallFailing.PackageFlightInboundChange(errorDetails.errorKey) else ApiCallFailing.PackageFlightInbound(errorDetails.errorKey)
        }
        return Pair(errorDetails.errorCode, apiCallFailingDetails)
    }

    private fun makeResultsObserver(type: PackageSearchType, isChangeSearch: Boolean): Observer<BundleSearchResponse> {
        return object : DisposableObserver<BundleSearchResponse>() {
            override fun onNext(response: BundleSearchResponse) {
                if (response.getHotels().isEmpty()) {
                    val errorCode = PackageApiError.Code.search_response_null
                    errorObservable.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails(errorCode.name, errorCode)))
                } else {
                    Db.setPackageResponse(response)
                    if (type == PackageSearchType.HOTEL) {
                        hotelResultsObservable.onNext(Unit)
                        val currentFlights = arrayOf(response.getFlightLegs()[0].legId, response.getFlightLegs()[1].legId)
                        Db.sharedInstance.packageParams.currentFlights = currentFlights
                        Db.sharedInstance.packageParams.defaultFlights = currentFlights.copyOf()
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

            override fun onComplete() {
                Log.i("package completed")
            }

            override fun onError(throwable: Throwable) {
                Log.i("package error: " + throwable.message)
                when {
                    throwable is HttpException -> try {
                        val response = throwable.response().errorBody()
                        val midError = Gson().fromJson(response?.charStream(), MultiItemApiSearchResponse::class.java)
                        errorObservable.onNext(getApiCallFailingDetails(type, isChangeSearch, midError.firstError))
                    } catch (e: Exception) {
                        val errorCode = PackageApiError.Code.pkg_error_code_not_mapped
                        errorObservable.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails(errorCode.name, errorCode)))
                    }
                    RetrofitUtils.isNetworkError(throwable) -> {
                        val retryFun = fun() {
                            if (type == PackageSearchType.HOTEL) {
                                hotelParamsObservable.onNext(Db.sharedInstance.packageParams)
                            } else {
                                flightParamsObservable.onNext(Db.sharedInstance.packageParams)
                            }
                        }
                        val cancelFun = fun() {
                            showSearchObservable.onNext(Unit)
                        }
                        DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                    }
                    else -> {
                        val errorCode = PackageApiError.Code.pkg_error_code_not_mapped
                        errorObservable.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails(errorCode.name, errorCode)))
                    }
                }
            }
        }
    }

    fun setAirlineFeeTextOnBundleOverview() {
        if (PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()) {
            airlineFeePackagesWarningTextObservable.onNext(context.getString(R.string.airline_additional_fee_notice))
        } else {
            airlineFeePackagesWarningTextObservable.onNext("")
        }
    }

    private fun setSplitTicketMessagingOnBundleOverview(packageParams: PackageSearchParams) {
        if (packageParams.latestSelectedOfferInfo.isSplitTicketFlights) {
            val latestSelectedOutboundFlightBaggageFeesUrl = packageParams.latestSelectedOfferInfo.outboundFlightBaggageFeesUrl
            val latestSelectedInboundFlightBaggageFeesUrl = packageParams.latestSelectedOfferInfo.inboundFlightBaggageFeesUrl
            if (latestSelectedOutboundFlightBaggageFeesUrl != null && latestSelectedInboundFlightBaggageFeesUrl != null) {
                splitTicketBaggageFeesLinksObservable.onNext(getSplitTicketBaggageFeesLink(latestSelectedOutboundFlightBaggageFeesUrl, latestSelectedInboundFlightBaggageFeesUrl))
            }
        }
        showSplitTicketMessagingObservable.onNext(packageParams.latestSelectedOfferInfo.isSplitTicketFlights)
    }

    private fun getSplitTicketBaggageFeesLink(outboundBaggageFeesUrl: String, inboundBaggageFeesUrl: String): SpannableStringBuilder {
        val e3EndpointUrl = Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl
        val outboundBaggageLink = e3EndpointUrl + outboundBaggageFeesUrl
        val inboundBaggageLink = e3EndpointUrl + inboundBaggageFeesUrl
        return StrUtils.generateBaggageFeesTextWithClickableLinks(context, outboundBaggageLink, inboundBaggageLink)
    }
}
