package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.hotel.vm.BaseHotelResultsViewModel
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageHotelFilterOptions
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.tracking.ApiCallFailing
import io.reactivex.subjects.PublishSubject
import com.expedia.bookings.packages.util.PackageServicesManager
import com.expedia.util.PackageCalendarRules

class PackageHotelResultsViewModel(context: Context, private val packageServicesManager: PackageServicesManager?) :
        BaseHotelResultsViewModel(context) {

    val filterSearchSuccessResponseHandler = PublishSubject.create<Pair<PackageProductSearchType, BundleSearchResponse>>()
    val filterSearchErrorResponseHandler = PublishSubject.create<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()
    val filterSearchErrorDetailsObservable = PublishSubject.create<Pair<PackageApiError.Code, ApiCallFailing>>()
    var isFilteredResponse = false
    private val rules = PackageCalendarRules(context)

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            updateTitleSubtitleFromParams(params)
        })

        filterChoicesSubject.subscribe(endlessObserver { filterChoices ->
            var packageSearchParams = Db.sharedInstance.packageParams
            addFilterCriteriaToSearchParams(filterChoices, packageSearchParams)
            packageSearchParams.latestSelectedOfferInfo.flightPIID = Db.getPackageResponse().getFirstFlightPIID()
            val hotelSearchParamsWithFilters = packageSearchParams.convertToHotelSearchParams(rules.getMaxSearchDurationDays(), rules.getMaxDateRange())
            cachedParams = hotelSearchParamsWithFilters
            paramsSubject.onNext(hotelSearchParamsWithFilters)
            packageServicesManager?.doPackageSearch(packageSearchParams, PackageProductSearchType.MultiItemHotels, filterSearchSuccessResponseHandler, filterSearchErrorResponseHandler)
        })

        filterSearchErrorResponseHandler.subscribe { (_, code, apiCallFailing) ->
            filterSearchErrorDetailsObservable.onNext(Pair(code, apiCallFailing))
        }

        filterSearchSuccessResponseHandler.subscribe { (_, response) ->
            isFilteredResponse = true
            filterResultsObservable.onNext(HotelSearchResponse.convertPackageToSearchResponse(response, true))
        }
    }

    private fun addFilterCriteriaToSearchParams(filterChoices: UserFilterChoices?, searchParams: PackageSearchParams) {
        searchParams.filterOptions = PackageHotelFilterOptions()
        filterChoices?.let {
            searchParams.filterOptions?.filterVipOnly = it.isVipOnlyAccess
            searchParams.filterOptions?.userSort = it.userSort.toServerSort()
            searchParams.filterOptions?.filterStarRatings = it.hotelStarRating.getStarRatingParamsAsList(true)
            it.name.takeIf { it.isNotEmpty() }?.let { searchParams.filterOptions?.filterHotelName = it }
        }
    }

    private fun updateTitleSubtitleFromParams(params: HotelSearchParams) {
        titleSubject.onNext(StrUtils.formatCity(params.suggestion))
        subtitleSubject.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests))
                .format())
        subtitleContDescSubject.onNext(
                Phrase.from(context, R.string.start_to_end_plus_guests_cont_desc_TEMPLATE)
                        .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkIn))
                        .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.checkOut))
                        .put("guests", StrUtils.formatGuestString(context, params.guests))
                        .format().toString())
    }
}
