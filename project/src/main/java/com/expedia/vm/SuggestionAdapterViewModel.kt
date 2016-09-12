package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

abstract class SuggestionAdapterViewModel(val context: Context, val suggestionsService: SuggestionV4Services, locationObservable: Observable<Location>?, val shouldShowCurrentLocation: Boolean, val rawQueryEnabled: Boolean) {
    private val currentLocationText = context.getString(R.string.current_location)
    // Outputs
    val suggestionsObservable = BehaviorSubject.create<List<SuggestionV4>>()
    var suggestions: List<SuggestionV4> = emptyList()
    val suggestionSelectedSubject = PublishSubject.create<SuggestionV4>()

    private var nearby: ArrayList<SuggestionV4> = ArrayList()
    private var lastQuery: String = ""
    private var isCustomerSelectingOrigin: Boolean = false

    init {
        locationObservable?.subscribe(generateLocationServiceCallback());
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        lastQuery = query
        if (query.isNotBlank() && query.length >= SuggestionV4Utils.getMinSuggestQueryLength(context) && !query.equals(currentLocationText)) {
            getSuggestionService(query)
        } else {
            suggestionsObservable.onNext(suggestionsListWithNearby())
        }
    }

    private fun suggestionsListWithNearby(): List<SuggestionV4> {
        return nearby + loadRecentSuggestions()
    }

    private fun loadRecentSuggestions(): List<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, getSuggestionHistoryFile())
    }

    open fun shouldShowOnlyAirportNearbySuggestions(): Boolean = false

    private fun getNearbySuggestions(location: Location) {
        val latlong = "" + location.latitude + "|" + location.longitude;
        suggestionsService
                .suggestNearbyV4(PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().siteId, ServicesUtil.generateClient(context),
                        getNearbyRegionType(), getNearbySortType(), getLineOfBusiness())
                .doOnNext { nearbySuggestions ->
                    if (nearbySuggestions.size < 1) {
                        throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS)
                    }
                    if (shouldShowCurrentLocation) {
                        var suggestion = modifySuggestionToCurrentLocation(location, nearbySuggestions.first())
                        nearbySuggestions.add(0, suggestion)
                    }
                }
                .doOnNext {
                    nearby.addAll(it)
                    nearby.forEach { it.iconType = SuggestionV4.IconType.CURRENT_LOCATION_ICON }
                }
                .doOnNext { nearbySuggestions ->
                    nearbySuggestions.addAll(loadRecentSuggestions())
                }
                .subscribe(generateSuggestionServiceCallback())
    }

    // Utility
    private fun modifySuggestionToCurrentLocation(location: Location, suggestion: SuggestionV4): SuggestionV4 {
        val currentLocation = suggestion.copy()
        currentLocation.gaiaId = null
        currentLocation.regionNames.displayName = context.getString(R.string.current_location)
        val coordinate = SuggestionV4.LatLng()
        coordinate.lat = location.latitude
        coordinate.lng = location.longitude
        currentLocation.coordinates = coordinate
        return currentLocation
    }

    private fun generateLocationServiceCallback(): Observer<Location> {
        return object : Observer<Location> {
            override fun onNext(location: Location) {
                getNearbySuggestions(location)
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                suggestionsObservable.onNext(suggestionsListWithNearby())
            }
        }
    }

    fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                val rawQuerySuggestion = if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList()
                if (essSuggestions.count() == 0) {
                    suggestionsObservable.onNext(rawQuerySuggestion)
                } else {
                    val essAndRawTextSuggestion = ArrayList<SuggestionV4>(rawQuerySuggestion)
                    essAndRawTextSuggestion.addAll(essSuggestions)
                    suggestionsObservable.onNext(essAndRawTextSuggestion)
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }

    private fun suggestionWithRawQueryString(query: String): SuggestionV4 {
        val rawQuerySuggestion = SuggestionV4()
        rawQuerySuggestion.type = Constants.RAW_TEXT_SEARCH
        rawQuerySuggestion.iconType = SuggestionV4.IconType.MAGNIFYING_GLASS_ICON
        rawQuerySuggestion.regionNames = SuggestionV4.RegionNames()
        rawQuerySuggestion.regionNames.displayName = "\"" + query + "\""
        rawQuerySuggestion.regionNames.shortName = query // shown in results toolbar title
        rawQuerySuggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        rawQuerySuggestion.hierarchyInfo?.isChild = false
        rawQuerySuggestion.coordinates = SuggestionV4.LatLng()
        return rawQuerySuggestion
    }

    abstract fun getSuggestionService(query: String)

    abstract fun getSuggestionHistoryFile(): String

    abstract fun getLineOfBusiness(): String

    abstract fun getNearbyRegionType(): Int

    abstract fun getNearbySortType(): String

    fun setCustomerSelectingOrigin(isOrigin: Boolean) {
        isCustomerSelectingOrigin = isOrigin
    }

    fun getCustomerSelectingOrigin(): Boolean {
        return isCustomerSelectingOrigin
    }
}
