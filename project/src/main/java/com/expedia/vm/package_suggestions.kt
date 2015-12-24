package com.expedia.vm

import android.content.Context
import android.location.Location
import android.text.Html
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class PackageSuggestionAdapterViewModel(val context: Context, val suggestionsService: SuggestionV4Services, val locationObservable: Observable<Location>?) {
    private val currentLocationText = context.getString(R.string.current_location)
    // Outputs
    val suggestionsObservable = BehaviorSubject.create<List<SuggestionV4>>()
    var suggestions: List<SuggestionV4> = emptyList()
    val suggestionSelectedSubject = PublishSubject.create<SuggestionV4>()

    private var nearby: ArrayList<SuggestionV4> = ArrayList()
    private var lastQuery: String = ""

    init {
        if (locationObservable != null) {
            locationObservable.subscribe(generateLocationServiceCallback());
        } else {
            suggestionsObservable.onNext(suggestionsListWithNearby())
        }
    }

    // Inputs
    val queryObserver = endlessObserver<String> { query ->
        lastQuery = query
        if (query.isNotBlank() && query.length >= 3 && !query.equals(currentLocationText)) {
            suggestionsService.suggestPackagesV4(query, ServicesUtil.generateClientId(context), generateSuggestionServiceCallback())
        } else {
            suggestionsObservable.onNext(suggestionsListWithNearby())
        }
    }

    private fun suggestionsListWithNearby(): List<SuggestionV4> {
        return nearby + loadRecentSuggestions()
    }

    private fun loadRecentSuggestions(): List<SuggestionV4> {
        return SuggestionV4Utils.loadSuggestionHistory(context, SuggestionV4Utils.RECENT_PACKAGE_SUGGESTIONS_FILE)
    }

    private fun getNearbySuggestions(location: Location) {
        val latlong = "" + location.latitude + "|" + location.longitude;
        suggestionsService
                .suggestNearbyFlightsV4(PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().getSiteId(), ServicesUtil.generateClientId(context))
                .doOnNext { nearbySuggestions ->
                    if (nearbySuggestions.size < 1) {
                        throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS)
                    }
                    var suggestion = modifySuggestionToCurrentLocation(location, nearbySuggestions.first())
                    nearbySuggestions.add(0, suggestion)
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

    private fun generateSuggestionServiceCallback(): Observer<List<SuggestionV4>> {
        return object : Observer<List<SuggestionV4>> {
            override fun onNext(essSuggestions: List<SuggestionV4>) {
                suggestionsObservable.onNext(essSuggestions)
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Log.e("Hotel Suggestions Error", e)
            }
        }
    }
}

public class PackageSuggestionViewModel() {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionSelected = PublishSubject.create<SuggestionV4>()

    // Inputs
    val suggestionObserver = BehaviorSubject.create<SuggestionV4>()

    init {
        suggestionObserver.subscribe { suggestion ->

            titleObservable.onNext(Html.fromHtml(suggestion.regionNames.displayName).toString())

            isChildObservable.onNext(suggestion.hierarchyInfo?.isChild ?: false)

            iconObservable.onNext(
                    if (suggestion.iconType == SuggestionV4.IconType.HISTORY_ICON) {
                        R.drawable.recents
                    } else if (suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON) {
                        R.drawable.ic_suggest_current_location
                    } else {
                        R.drawable.airport_suggest
                    }
            )
        }
    }
}
