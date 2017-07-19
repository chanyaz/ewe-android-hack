package com.expedia.bookings.hotel.vm

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription

class PackageHotelResultsViewModel(context: Context, private val packageServices: PackageServices?) :
        BaseHotelResultsViewModel(context) {

    private var cachedParams: HotelSearchParams? = null
    private var searchSubscription: Subscription? = null

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })
    }

    fun unsubscribeSearchResponse() {
        searchSubscription?.unsubscribe()
    }

    private fun doSearch(params: HotelSearchParams) {
        cachedParams = params
        titleSubject.onNext(StrUtils.formatCity(params.suggestion))
        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
        if (isRemoveBundleOverviewFeatureEnabled()
                && !(context as AppCompatActivity).intent.hasExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM)) {
            searchPackageHotels(Db.getPackageParams())
        }
    }

    private var productSearchType: ProductSearchType = {
        val isMidApiEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        if (isMidApiEnabled) ProductSearchType.MultiItemHotels else ProductSearchType.OldPackageSearch
    }()

    private fun searchPackageHotels(params: PackageSearchParams) {
        searchSubscription = packageServices?.packageSearch(params, productSearchType)?.subscribe(object : Observer<BundleSearchResponse> {
            override fun onNext(response: BundleSearchResponse) {
                if (response.hasErrors()) {
                    onResponseError(response.firstError)
                } else if (response.getHotels().isEmpty()) {
                    onResponseError(PackageApiError.Code.search_response_null)
                } else {
                    Db.setPackageResponse(response)
                    val currentFlights = arrayOf(response.getFlightLegs()[0].legId, response.getFlightLegs()[1].legId)
                    Db.getPackageParams().currentFlights = currentFlights
                    Db.getPackageParams().defaultFlights = currentFlights.copyOf()
                    PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE)
                    onSearchResponse(HotelSearchResponse.convertPackageToSearchResponse(response))
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val cancelFun = fun() {
                        showHotelSearchViewObservable.onNext(Unit)
                    }
                    val retryFun = fun() {
                        if (cachedParams != null) {
                            doSearch(cachedParams!!)
                        } else {
                            cancelFun()
                        }
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }
        })
    }

    private fun onResponseError(code: PackageApiError.Code?) {
        val intent = Intent()
        intent.putExtra(Constants.PACKAGE_API_ERROR, code)
        (context as android.app.Activity).setResult(Constants.PACKAGE_API_ERROR_RESULT_CODE, intent)
        context.finish()
    }

    private fun isRemoveBundleOverviewFeatureEnabled(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesRemoveBundleOverview)
    }

    private fun onSearchResponse(hotelSearchResponse: HotelSearchResponse) {
        if (titleSubject.value == null || (titleSubject.value != null && titleSubject.value.isEmpty())) {
            titleSubject.onNext(hotelSearchResponse.searchRegionCity)
        }

        if (hotelSearchResponse.hasErrors()) {
            errorObservable.onNext(hotelSearchResponse.firstError)
        } else if (hotelSearchResponse.hotelList.isEmpty()) {
            val error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
            errorObservable.onNext(error)
        } else {
            hotelResultsObservable.onNext(hotelSearchResponse)
        }
    }
}