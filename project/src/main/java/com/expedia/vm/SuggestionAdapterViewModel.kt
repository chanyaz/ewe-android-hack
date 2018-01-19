package com.expedia.vm

import android.content.Context
import android.location.Location
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.GaiaSuggestionRequest
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionDataItem
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.shared.util.GaiaNearbyManager
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

abstract class SuggestionAdapterViewModel(val context: Context, val suggestionsService: ISuggestionV4Services,
                                          locationObservable: Observable<Location>?,
                                          private val shouldShowCurrentLocation: Boolean, val rawQueryEnabled: Boolean) {
    private enum class Category {
        CURRENT_LOCATION,
        NEARBY,
        SERACH_HISTORY_DEVICE,
        SEARCH_HISTORY_REMOTE,
        ESS
    }
    private val minSuggestionQueryByteLength = 3
    private val currentLocationText = context.getString(R.string.current_location)
    // Outputs
    val suggestionItemsSubject = BehaviorSubject.create<List<SuggestionDataItem>>()
    val suggestionSelectedSubject = PublishSubject.create<SearchSuggestion>()

    private var currentLocation: SuggestionV4? = null
    private var nearby: ArrayList<SuggestionV4> = ArrayList()
    private var lastQuery: String = ""
    private var userRecentSearches: List<SuggestionV4> = emptyList() //TODO eventually, we need to store and display search params+location

    private val gaiaManager = GaiaNearbyManager(suggestionsService)
    private val gaiaSubscriptions = CompositeDisposable()

    init {
        locationObservable?.subscribe(generateLocationServiceCallback())

        gaiaManager.errorSubject.subscribe {
            throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS)
        }
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        lastQuery = query
        if (query.isNotBlank() && !query.equals(currentLocationText) &&
                (isSuggestionOnOneCharEnabled() || query.toByteArray().size >= minSuggestionQueryByteLength)) {
            getSuggestionService(query)
        } else {
            suggestionItemsSubject.onNext(getSuggestionAdapterItems())
        }
    }

    fun getLastQuery(): String {
        return lastQuery
    }

    fun setUserSearchHistory(userSearchHistory: List<SuggestionV4>) {
        userRecentSearches = userSearchHistory
    }


    @VisibleForTesting open fun shouldShowOnlyAirportNearbySuggestions(): Boolean = false

    @VisibleForTesting fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : DisposableObserver<List<SuggestionV4>>() {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                val rawQuerySuggestion = if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList()
                val essAndRawTextSuggestions = ArrayList<SuggestionV4>(rawQuerySuggestion)
                essAndRawTextSuggestions.addAll(essSuggestions)
                suggestionItemsSubject.onNext(essAndRawTextSuggestions.map { SuggestionDataItem.V4(it) })
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }

    private fun getSuggestionAdapterItems(): List<SuggestionDataItem> {
        val suggestions = ArrayList<SuggestionDataItem>()

        for (category in getCategoryOrder()) {
            when (category) {
                Category.CURRENT_LOCATION -> {
                    if (shouldShowCurrentLocation && currentLocation != null) {
                        suggestions.add(SuggestionDataItem.CurrentLocation(currentLocation!!))
                    }
                }
                Category.NEARBY -> {
                    if (nearby.size > 0 && showSuggestionsAndLabel()) {
                        suggestions.add(SuggestionDataItem.Label(getCurrentLocationLabel()))
                    }
                    nearby.forEach { suggestions.add(SuggestionDataItem.V4(it)) }
                }
                Category.SERACH_HISTORY_DEVICE -> {
                    val loadPastSuggestions = loadPastSuggestions()
                    if (loadPastSuggestions.isNotEmpty() && showSuggestionsAndLabel()) {
                        suggestions.add(SuggestionDataItem.Label(getPastSuggestionsLabel()))
                    }
                    loadPastSuggestions.forEach { suggestions.add(SuggestionDataItem.V4(it)) }
                }
                Category.SEARCH_HISTORY_REMOTE -> {
                    if (isSearchHistorySupported()) {
                        if (userRecentSearches.isNotEmpty() && showSuggestionsAndLabel()) {
                            suggestions.add(SuggestionDataItem.Label("Travel Graph Searches"))
                        }
                        userRecentSearches.forEach { suggestions.add(SuggestionDataItem.V4(it)) }
                    }
                }
            }
        }
        return suggestions
    }

    private fun loadPastSuggestions(): List<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, getSuggestionHistoryFile())
    }

    private fun getSuggestionsWithLabel(location: Location) {
        gaiaSubscriptions.clear()
        gaiaSubscriptions.add(gaiaManager.suggestionsSubject.subscribe { gaiaSuggestions ->
            updateNearBy(location, gaiaSuggestions)
            suggestionItemsSubject.onNext(getSuggestionAdapterItems())
        })

        fetchNearBySuggestions(location)
    }

    private fun getSuggestions(location: Location) {
        gaiaSubscriptions.clear()
        gaiaSubscriptions.add(gaiaManager.suggestionsSubject.subscribe { gaiaSuggestions ->
            updateNearBy(location, gaiaSuggestions)

            val suggestions = ArrayList<SuggestionV4>()
            suggestions.addAll(nearby)
            suggestions.addAll(loadPastSuggestions())
            if (isSearchHistorySupported()) {
                suggestions.addAll(userRecentSearches)
            }

            suggestionItemsSubject.onNext(suggestions.map { SuggestionDataItem.V4(it) })
        })

        fetchNearBySuggestions(location)
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
        currentLocation.iconType = SuggestionV4.IconType.CURRENT_LOCATION_ICON
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
                suggestionItemsSubject.onNext(getSuggestionAdapterItems())
            }
        }
    }

    private fun fetchNearBySuggestions(location: Location) {
        val request = GaiaSuggestionRequest(location, getNearbySortTypeForGaia(),
                getLineOfBusinessForGaia(), isMISForRealWorldEnabled())
        gaiaManager.nearBySuggestions(request)
    }

    private fun updateNearBy(location: Location, gaiaResults: List<SuggestionV4>) {
        if (shouldShowCurrentLocation) {
            currentLocation = modifySuggestionToCurrentLocation(location, gaiaResults.first())
        }
        nearby.addAll(gaiaResults)
        nearby.forEach { it.iconType = SuggestionV4.IconType.CURRENT_LOCATION_ICON }
    }

    private fun getCategoryOrder() : List<Category> {
        return listOf(Category.ESS, Category.CURRENT_LOCATION, Category.NEARBY, Category.SERACH_HISTORY_DEVICE,
                Category.SEARCH_HISTORY_REMOTE)
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

    open fun getCurrentLocationLabel(): String = ""

    open fun getPastSuggestionsLabel(): String = ""

    open fun getLineOfBusiness(): LineOfBusiness = LineOfBusiness.NONE

    open fun showSuggestionsAndLabel(): Boolean = true

    open fun isSuggestionOnOneCharEnabled(): Boolean = false

    open fun isSearchHistorySupported(): Boolean = false
}
