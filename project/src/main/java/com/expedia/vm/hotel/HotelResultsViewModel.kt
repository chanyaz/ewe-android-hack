package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.HotelTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelResultsViewModel(private val context: Context, private val hotelServices: HotelServices?, private val lob: LineOfBusiness, private val clientLogBuilder: ClientLog.Builder?) {

    private val INITIAL_RESULTS_TO_BE_LOADED = 25
    private val ALL_RESULTS_TO_BE_LOADED = 200

    // Inputs
    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()

    // Outputs
    val addHotelResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val mapResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()
    val showHotelSearchViewObservable = PublishSubject.create<Unit>()

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            val cachedParams: HotelSearchParams? = paramsSubject.value
            val builder = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                    context.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                    .destination(suggestion)
                    .startDate(cachedParams?.checkIn)
                    .endDate(cachedParams?.checkOut)
                    .adults(cachedParams?.adults!!)
                    .children(cachedParams?.children!!) as HotelSearchParams.Builder
            val params = builder.shopWithPoints(cachedParams?.shopWithPoints ?: false).build()

            doSearch(params)
        })

        hotelResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }

        mapResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }
    }

    private fun doSearch(params: HotelSearchParams) {
        val isPackages = lob == LineOfBusiness.PACKAGES
        titleSubject.onNext(if (isPackages) StrUtils.formatCity(params.suggestion) else params.suggestion.regionNames.shortName)

        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())

        clientLogBuilder?.logTime(DateTime.now())
        searchHotels(params)
    }

    private fun searchHotels(params: HotelSearchParams, isInitial: Boolean = true) {
        val isBucketed = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelResultsPerceivedInstantTest)
        val makeMultipleCalls = isInitial && isBucketed
        hotelServices?.search(params, clientLogBuilder, if (makeMultipleCalls) INITIAL_RESULTS_TO_BE_LOADED else ALL_RESULTS_TO_BE_LOADED)?.subscribe(object : Observer<HotelSearchResponse> {
            override fun onNext(hotelSearchResponse: HotelSearchResponse) {
                onSearchResponse(hotelSearchResponse, isInitial)
                if (makeMultipleCalls) {
                    searchHotels(params, false)
                }
            }


            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        doSearch(paramsSubject.value)
                    }
                    val cancelFun = fun() {
                        showHotelSearchViewObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }
        })
    }

    private fun onSearchResponse(hotelSearchResponse: HotelSearchResponse, isInitial: Boolean) {
        clientLogBuilder?.processingTime(DateTime.now())
        if (hotelSearchResponse.hasErrors()) {
            errorObservable.onNext(hotelSearchResponse.firstError)
        } else if (hotelSearchResponse.hotelList.isEmpty()) {
            var error: ApiError
            if (titleSubject.value == context.getString(R.string.visible_map_area)) {
                error = ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS)
            } else {
                error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
            }
            errorObservable.onNext(error)
        } else if (titleSubject.value == context.getString(R.string.visible_map_area)) {
            mapResultsObservable.onNext(hotelSearchResponse)
        } else {
            if (isInitial) {
                hotelResultsObservable.onNext(hotelSearchResponse)
            } else {
                addHotelResultsObservable.onNext(hotelSearchResponse)
            }
            HotelTracking().trackHotelsSearch(paramsSubject.value, hotelSearchResponse)
        }
    }

}
