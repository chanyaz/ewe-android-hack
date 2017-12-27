package com.expedia.bookings.services

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionV4
import rx.Observable
import rx.Observer
import rx.Subscription

interface ISuggestionV4Services {
    fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Subscription
    fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, sameAsWeb: Boolean, guid: String?): Subscription
    fun suggestNearbyGaia(lat: Double, lng: Double, sortType: String, lob: String, locale: String, siteId: Int, isMISForRealWorldEnabled: Boolean = false): Observable<MutableList<GaiaSuggestion>>
    fun suggestPackagesV4(query: String, isDest: Boolean, isMISForRealWorldEnabled: Boolean, observer: Observer<List<SuggestionV4>>): Subscription
    fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Subscription
    fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Subscription
}