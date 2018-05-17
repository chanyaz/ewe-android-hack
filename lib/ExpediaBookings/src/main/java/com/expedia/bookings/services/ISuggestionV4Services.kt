package com.expedia.bookings.services

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.GaiaSuggestionRequest
import com.expedia.bookings.data.SuggestionV4
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

interface ISuggestionV4Services {
    fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Disposable
    fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>): Disposable
    fun suggestNearbyGaia(request: GaiaSuggestionRequest): Observable<MutableList<GaiaSuggestion>>
    fun suggestPackagesV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String?): Disposable
    fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Disposable
    fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Disposable
}
