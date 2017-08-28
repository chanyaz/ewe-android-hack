package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

abstract class SuggestionAdapterViewModel(val context: Context, val suggestionsService: SuggestionV4Services,
                                          locationObservable: Observable<Location>?,
                                          val shouldShowCurrentLocation: Boolean, val rawQueryEnabled: Boolean) {
    private val currentLocationText = context.getString(R.string.current_location)
    // Outputs
    val suggestionsObservable = BehaviorSubject.create<List<SuggestionV4>>()
    val suggestionsAndLabelObservable = BehaviorSubject.create<List<SuggestionType>>()
    var suggestions: List<SuggestionV4> = emptyList()
    var suggestionsAndLabel: List<SuggestionType> = emptyList()
    val suggestionSelectedSubject = PublishSubject.create<SearchSuggestion>()

    private var nearby: ArrayList<SuggestionV4> = ArrayList()
    private var lastQuery: String = ""
    private var isCustomerSelectingOrigin: Boolean = false

    init {
        locationObservable?.subscribe(generateLocationServiceCallback());
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        lastQuery = query
        if (query.isNotBlank() && !query.equals(currentLocationText) && (isSuggestionOnOneCharEnabled() || query.length >= SuggestionV4Utils.getMinSuggestQueryLength(context))) {
            getSuggestionService(query)
        } else {
            if (showSuggestionsAndLabel()) {
                suggestionsAndLabelObservable.onNext(suggestionsListWithNearbyAndLabels())
            } else {
                suggestionsObservable.onNext(suggestionsListWithNearby())
            }
        }
    }

    fun getLastQuery(): String {
        return lastQuery
    }

    private fun suggestionsListWithNearby(): List<SuggestionV4> {
        return nearby + loadRecentSuggestions()
    }

    private fun suggestionsListWithNearbyAndLabels(): List<SuggestionType> {
        var suggestions = ArrayList<SuggestionType>()
        if (nearby.size > 0) {
            suggestions.add(SuggestionType.SUGGESTIONLABEL(getCurrentLocationLabel()))
            nearby.forEach {
                suggestions.add(SuggestionType.SUGGESTIONV4(it))
            }
        }
        val loadRecentSuggestions = loadRecentSuggestions()
        if (loadRecentSuggestions.size > 0) {
            suggestions.add(SuggestionType.SUGGESTIONLABEL(getRecentSuggestionLabel()))
            loadRecentSuggestions.forEach {
                suggestions.add(SuggestionType.SUGGESTIONV4(it))
            }
        }
        return suggestions
    }

    private fun loadRecentSuggestions(): List<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, getSuggestionHistoryFile())
    }

    open fun shouldShowOnlyAirportNearbySuggestions(): Boolean = false

    private fun getGaiaNearbySuggestions(location: Location): Observable<MutableList<SuggestionV4>> {
        return suggestionsService
                .suggestNearbyGaia(location.latitude, location.longitude, getNearbySortTypeForGaia(),
                        getLineOfBusinessForGaia(), PointOfSale.getSuggestLocaleIdentifier(), PointOfSale.getPointOfSale().siteId)
                .map { it ->
                    SuggestionV4Utils.convertToSuggestionV4(it)
                }
                .doOnNext { nearbySuggestions ->
                    if (nearbySuggestions.size < 1) {
                        throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS)
                    }
                    if (shouldShowCurrentLocation) {
                        val suggestion = modifySuggestionToCurrentLocation(location, nearbySuggestions.first())
                        nearbySuggestions.add(0, suggestion)
                    }
                }
                .doOnNext {
                    nearby.addAll(it)
                    nearby.forEach { it.iconType = SuggestionV4.IconType.CURRENT_LOCATION_ICON }
                }
    }

    private fun getSuggestionsWithLabel(location: Location) {
        getGaiaNearbySuggestions(location)
                .subscribe { nearbySuggestion ->
                    val rawQuerySuggestion = (if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList())
                    val essAndRawTextSuggestion = ArrayList<SuggestionType>()
                    essAndRawTextSuggestion += rawQuerySuggestion.map { SuggestionType.SUGGESTIONV4(it) }
                    essAndRawTextSuggestion += suggestionsListWithNearbyAndLabels()
                    suggestionsAndLabelObservable.onNext(essAndRawTextSuggestion)
                }
    }

    private fun getSuggestions(location: Location) {
        getGaiaNearbySuggestions(location)
                .doOnNext { nearBySuggestions ->
                    nearBySuggestions += loadRecentSuggestions()
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
                if (showSuggestionsAndLabel()) {
                    getSuggestionsWithLabel(location)
                } else {
                    getSuggestions(location)
                }
            }

            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable?) {
                if (showSuggestionsAndLabel()) {
                    suggestionsAndLabelObservable.onNext(suggestionsListWithNearbyAndLabels())
                } else {
                    suggestionsObservable.onNext(suggestionsListWithNearby())
                }
            }
        }
    }

    fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                val rawQuerySuggestion = if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList()
                val essAndRawTextSuggestion = ArrayList<SuggestionV4>(rawQuerySuggestion)
                essAndRawTextSuggestion.addAll(essSuggestions)
                if (showSuggestionsAndLabel()) {
                    suggestionsAndLabelObservable.onNext(essAndRawTextSuggestion.map { SuggestionType.SUGGESTIONV4(it) })
                } else {
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

    abstract fun getLineOfBusinessForGaia(): String

    abstract fun getNearbySortTypeForGaia(): String

    fun setCustomerSelectingOrigin(isOrigin: Boolean) {
        isCustomerSelectingOrigin = isOrigin
    }

    fun getCustomerSelectingOrigin(): Boolean {
        return isCustomerSelectingOrigin
    }

    open fun getCurrentLocationLabel(): String {
        return ""
    }

    open fun getRecentSuggestionLabel(): String {
        return ""
    }

    open fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.NONE;
    }

    open fun showSuggestionsAndLabel(): Boolean {
        return false
    }

    open fun isSuggestionOnOneCharEnabled(): Boolean {
        return false
    }
}
