package com.expedia.vm

import android.content.Context
import android.location.Location
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.extensions.toDataItemList
import com.expedia.bookings.extensions.toSearchInfoDataItemList
import com.expedia.bookings.services.ISuggestionV4Services
import com.expedia.bookings.shared.data.SuggestionDataItem
import com.expedia.bookings.shared.util.GaiaNearbyManager
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

abstract class BaseSuggestionAdapterViewModel(
    val context: Context,
    val suggestionsService: ISuggestionV4Services,
    locationObservable: Observable<Location>?,
    private val shouldShowCurrentLocation: Boolean,
    val rawQueryEnabled: Boolean
) {
    protected enum class Category {
        CURRENT_LOCATION,
        NEARBY,
        SEARCH_HISTORY_DEVICE,
        SEARCH_HISTORY_REMOTE,
        ESS
    }

    // Outputs
    val suggestionItemsSubject = PublishSubject.create<List<SuggestionDataItem>>()
    val suggestionSelectedSubject = PublishSubject.create<SearchSuggestion>()
    val searchInfoSelectedSubject = PublishSubject.create<SearchInfo>()

    @VisibleForTesting
    var gaiaManager = GaiaNearbyManager(suggestionsService)

    private var currentLocation: SuggestionV4? = null
    private var nearby: ArrayList<SuggestionV4> = ArrayList()
    private var lastQuery: String = ""
    private var userRecentSearches: List<SearchInfo> = emptyList()

    private val gaiaSubscriptions = CompositeDisposable()

    private val minSuggestionQueryByteLength = 3
    private val currentLocationText = context.getString(R.string.current_location)
    var suggestionToFilterFromHistory: SuggestionV4? = null
        set(value) {
            field = value
            suggestionItemsSubject.onNext(getSuggestionAdapterItems())
        }

    init {
        locationObservable?.subscribe(generateLocationServiceCallback())
    }

    abstract fun getSuggestionService(query: String)
    abstract fun getSuggestionHistoryFile(): String
    abstract fun getLineOfBusinessForGaia(): String
    abstract fun getNearbySortTypeForGaia(): String

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

    fun setUserSearchHistory(userSearchHistory: List<SearchInfo>) {
        userRecentSearches = userSearchHistory
        if (lastQuery.isEmpty()) {
            suggestionItemsSubject.onNext(getSuggestionAdapterItems())
        }
    }

    @VisibleForTesting
    fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : DisposableObserver<List<SuggestionV4>>() {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                val rawQuerySuggestion = if (rawQueryEnabled && !lastQuery.isNullOrBlank()) listOf(suggestionWithRawQueryString(lastQuery)) else emptyList()
                val essAndRawTextSuggestions = ArrayList<SuggestionV4>(rawQuerySuggestion)
                essAndRawTextSuggestions.addAll(essSuggestions)

                val suggestionDataItems = essAndRawTextSuggestions.map { suggestion ->
                    SuggestionDataItem.SuggestionDropDown(suggestion)
                }
                suggestionItemsSubject.onNext(suggestionDataItems)
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }

    @VisibleForTesting
    open fun isMISForRealWorldEnabled(): Boolean = false

    @VisibleForTesting
    open fun getCurrentLocationLabel(): String = ""

    @VisibleForTesting
    open fun getPastSuggestionsLabel(): String = ""

    protected open fun getLineOfBusiness(): LineOfBusiness = LineOfBusiness.NONE
    protected open fun areLabelsEnabled(): Boolean = true
    protected open fun isSuggestionOnOneCharEnabled(): Boolean = false
    protected open fun isSearchHistorySupported(): Boolean = false

    protected open fun getCategoryOrder(): List<Category> {
        return listOf(Category.ESS, Category.CURRENT_LOCATION, Category.NEARBY, Category.SEARCH_HISTORY_DEVICE,
                Category.SEARCH_HISTORY_REMOTE)
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
                    if (nearby.size > 0 && areLabelsEnabled()) {
                        suggestions.add(SuggestionDataItem.Label(getCurrentLocationLabel()))
                    }
                    suggestions.addAll(nearby.toDataItemList())
                }
                Category.SEARCH_HISTORY_DEVICE -> {
                    val loadPastSuggestions = loadPastSuggestions()
                    if (loadPastSuggestions.isNotEmpty() && areLabelsEnabled()) {
                        suggestions.add(SuggestionDataItem.Label(getPastSuggestionsLabel()))
                    }
                    suggestions.addAll(loadPastSuggestions.toDataItemList())
                }
                Category.SEARCH_HISTORY_REMOTE -> {
                    if (isSearchHistorySupported()) {
                        if (userRecentSearches.isNotEmpty() && areLabelsEnabled()) {
                            suggestions.add(SuggestionDataItem.Label(context.getString(R.string.suggestion_label_recent_search)))
                        }
                        suggestions.addAll(userRecentSearches.toSearchInfoDataItemList())
                    }
                }
                else -> {
                }
            }
        }
        return suggestions
    }

    private fun loadPastSuggestions(): List<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, getSuggestionHistoryFile(), suggestionToFilterFromHistory)
    }

    private fun updateNearBy(location: Location, gaiaResults: List<SuggestionV4>) {
        if (shouldShowCurrentLocation) {
            currentLocation = modifySuggestionToCurrentLocation(location, gaiaResults.first())
        }
        nearby.addAll(gaiaResults)
        nearby.forEach { suggestion ->
            suggestion.iconType = SuggestionV4.IconType.CURRENT_LOCATION_ICON
        }
    }

    private fun getSuggestionsForLocation(location: Location) {
        gaiaSubscriptions.add(gaiaManager.suggestionsSubject.subscribe { gaiaSuggestions ->
            updateNearBy(location, gaiaSuggestions)
            val suggestionItems = ArrayList<SuggestionDataItem>()
            getRawQueryAsSuggestion()?.let { rawQuerySuggestion ->
                suggestionItems.add(SuggestionDataItem.SuggestionDropDown(rawQuerySuggestion))
            }
            suggestionItems.addAll(getSuggestionAdapterItems())
            suggestionItemsSubject.onNext(suggestionItems)
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
                getSuggestionsForLocation(location)
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
        gaiaManager.nearBySuggestions(location, getNearbySortTypeForGaia(),
                getLineOfBusinessForGaia(), isMISForRealWorldEnabled())
    }

    private fun getRawQueryAsSuggestion(): SuggestionV4? {
        if (rawQueryEnabled && lastQuery.isNotBlank()) {
            return suggestionWithRawQueryString(lastQuery)
        }
        return null
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
}
