package com.expedia.vm

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResult
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

abstract class SuggestionAdapterViewModel(val context: Context, val suggestionsService: ISuggestionV4Services,
                                          locationObservable: Observable<Location>?,
                                          private val shouldShowCurrentLocation: Boolean, val rawQueryEnabled: Boolean) {
    private val minSuggestionQueryByteLength = 3
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
    private var userRecentSearches: List<SuggestionV4> = emptyList()     //TODO eventually, we need to store and display search params+location

    init {
        locationObservable?.subscribe(generateLocationServiceCallback())
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        lastQuery = query
        if (query.isNotBlank() && !query.equals(currentLocationText) &&
                (isSuggestionOnOneCharEnabled() || query.toByteArray().size >= minSuggestionQueryByteLength)) {
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
        val suggestions = nearby + loadPastSuggestions()
        //TODO for skeleton - return without labels for now.
        if (isSearchHistorySupported()) {
            return suggestions + userRecentSearches
        }
        return suggestions
    }

    private fun suggestionsListWithNearbyAndLabels(): List<SuggestionType> {
        val suggestions = ArrayList<SuggestionType>()
        if (nearby.size > 0) {
            suggestions.add(SuggestionType.SUGGESTIONLABEL(getCurrentLocationLabel()))
            nearby.forEach {nearbySuggestion ->
                suggestions.add(SuggestionType.SUGGESTIONV4(nearbySuggestion))
            }
        }

        val loadPastSuggestions = loadPastSuggestions()
        if (loadPastSuggestions.size > 0) {
            suggestions.add(SuggestionType.SUGGESTIONLABEL(getPastSuggestionsLabel()))
            loadPastSuggestions.forEach {
                suggestions.add(SuggestionType.SUGGESTIONV4(it))
            }
        }
        return suggestions
    }

    private fun loadPastSuggestions(): List<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, getSuggestionHistoryFile())
    }

    open fun shouldShowOnlyAirportNearbySuggestions(): Boolean = false

    private fun getGaiaNearbySuggestions(location: Location): Observable<MutableList<SuggestionV4>> {
        return suggestionsService
                .suggestNearbyGaia(location.latitude, location.longitude, getNearbySortTypeForGaia(),
                        getLineOfBusinessForGaia(), PointOfSale.getSuggestLocaleIdentifier(), PointOfSale.getPointOfSale().siteId, isMISForRealWorldEnabled())
                .map { gaiaSuggestion ->
                    SuggestionV4Utils.convertToSuggestionV4(gaiaSuggestion)
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
                .subscribe(object : DisposableObserver<List<SuggestionV4>>() {
                    override fun onComplete() {
                    }

                    override fun onError(e: Throwable) {
                        Log.e("Search Suggestion LabelError", e.toString())
                    }

                    override fun onNext(t: List<SuggestionV4>) {
                        val rawQuerySuggestion = (if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList())
                        val essAndRawTextSuggestion = ArrayList<SuggestionType>()
                        essAndRawTextSuggestion += rawQuerySuggestion.map { SuggestionType.SUGGESTIONV4(it) }
                        essAndRawTextSuggestion += suggestionsListWithNearbyAndLabels()
                        suggestionsAndLabelObservable.onNext(essAndRawTextSuggestion)
                    }
                })
    }

    private fun getSuggestions(location: Location) {
        getGaiaNearbySuggestions(location)
                .doOnNext { nearBySuggestions ->
                    nearBySuggestions += loadPastSuggestions()
                    if (isSearchHistorySupported()) {
                        nearBySuggestions+= userRecentSearches
                    }
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
        return object : DisposableObserver<Location>() {
            override fun onNext(location: Location) {
                if (showSuggestionsAndLabel()) {
                    getSuggestionsWithLabel(location)
                } else {
                    getSuggestions(location)
                }
            }

            override fun onComplete() {
                // ignore
            }

            override fun onError(e: Throwable) {
                if (showSuggestionsAndLabel()) {
                    suggestionsAndLabelObservable.onNext(suggestionsListWithNearbyAndLabels())
                } else {
                    suggestionsObservable.onNext(suggestionsListWithNearby())
                }
            }
        }
    }

    fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : DisposableObserver<List<SuggestionV4>>() {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                val rawQuerySuggestion = if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList()
                val essAndRawTextSuggestions = ArrayList<SuggestionV4>(rawQuerySuggestion)
                essAndRawTextSuggestions.addAll(essSuggestions)
                if (showSuggestionsAndLabel()) {
                    suggestionsAndLabelObservable.onNext(essAndRawTextSuggestions.map { SuggestionType.SUGGESTIONV4(it) })
                } else {
                    suggestionsObservable.onNext(essAndRawTextSuggestions)
                }
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }

    fun setUserSearchHistory(userSearchHistory: TravelGraphUserHistoryResult) {
        userRecentSearches = userSearchHistory.convertToSuggestionV4List()
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

    open fun isMISForRealWorldEnabled(): Boolean {
        return false
    }

    abstract fun getNearbySortTypeForGaia(): String

    fun setCustomerSelectingOrigin(isOrigin: Boolean) {
        isCustomerSelectingOrigin = isOrigin
    }

    fun getCustomerSelectingOrigin(): Boolean {
        return isCustomerSelectingOrigin
    }

    open fun getCurrentLocationLabel(): String = ""

    open fun getPastSuggestionsLabel(): String = ""

    open fun getLineOfBusiness(): LineOfBusiness = LineOfBusiness.NONE

    open fun showSuggestionsAndLabel(): Boolean = false

    open fun isSuggestionOnOneCharEnabled(): Boolean = false

    open fun isSearchHistorySupported(): Boolean = false
}
