package com.expedia.bookings.services

import com.expedia.bookings.data.GaiaSuggestion
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.SuggestionV4Response
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription

open class SuggestionV4Services(essEndpoint: String, gaiaEndPoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                                essInterceptor: Interceptor, gaiaInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : ISuggestionV4Services {

    private val suggestApi: SuggestApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(essEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor)
                        .addInterceptor(essInterceptor).build())
                .build()

        adapter.create<SuggestApi>(SuggestApi::class.java)
    }

    private val gaiaSuggestApi: GaiaSuggestApi by lazy {
        val gson = GsonBuilder().create()
        val adapter = Retrofit.Builder()
                .baseUrl(gaiaEndPoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(gaiaInterceptor)
                        .addInterceptor(interceptor).build())
                .build()

        adapter.create<GaiaSuggestApi>(GaiaSuggestApi::class.java)
    }

    override fun getLxSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, disablePOI: Boolean): Subscription {

        var type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST
        if (disablePOI) {
            type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD
        }

        return suggestV4(query, type, false, "ta_hierarchy", "ACTIVITIES")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions}
                .subscribe(observer)
    }

    override fun getHotelSuggestionsV4(query: String, observer: Observer<List<SuggestionV4>>, sameAsWeb: Boolean, guid: String?): Subscription {
        val regiontype: Int
        val dest: Boolean
        val features: String
        val maxResults: Int?

        if (sameAsWeb) {
            regiontype = SuggestionResultType.ALL_REGION
            dest = true
            features = "ta_hierarchy|postal_code|contextual_ta"
            maxResults = 10
        } else {
            regiontype = SuggestionResultType.REGION
            dest = false
            features = "ta_hierarchy"
            maxResults = null
        }

        return suggestV4(query, regiontype, dest, features, "HOTELS", maxResults = maxResults, guid = guid)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }

    override fun suggestNearbyGaia(lat: Double, lng: Double, sortType: String, lob: String, locale: String, siteId: Int, isMISForRealWorldEnabled: Boolean): Observable<MutableList<GaiaSuggestion>> {
        val limit = 2
        val response = gaiaSuggestApi.gaiaNearBy(lat, lng, limit, lob, sortType, locale, siteId, if (isMISForRealWorldEnabled) "rwg" else null)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
        return response.map { response -> response.toMutableList() }
    }

    override fun suggestPackagesV4(query: String, isDest: Boolean, isMISForRealWorldEnabled: Boolean, observer: Observer<List<SuggestionV4>>): Subscription {
        val suggestType: Int
        if (isMISForRealWorldEnabled) {
            suggestType = SuggestionResultType.AIRPORT or
                    SuggestionResultType.CITY or
                    SuggestionResultType.MULTI_CITY or
                    SuggestionResultType.NEIGHBORHOOD or
                    SuggestionResultType.POINT_OF_INTEREST or
                    SuggestionResultType.AIRPORT_METRO_CODE or
                    SuggestionResultType.MULTI_REGION or
                    SuggestionResultType.TRAIN_STATION
        } else {
            suggestType = SuggestionResultType.AIRPORT or
                    SuggestionResultType.CITY or
                    SuggestionResultType.MULTI_CITY or
                    SuggestionResultType.NEIGHBORHOOD or
                    SuggestionResultType.POINT_OF_INTEREST or
                    SuggestionResultType.AIRPORT_METRO_CODE
        }
        return suggestV4(query, suggestType, isDest, "ta_hierarchy", "PACKAGES", abTest = if (isMISForRealWorldEnabled) "11996.1" else null)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }

    override fun suggestRailsV4(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>): Subscription {
        val suggestType = SuggestionResultType.TRAIN_STATION
        return suggestV4(query, suggestType, isDest, "ta_hierarchy", "RAILS")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)
    }
    
    private var airportSuggestionSubscription: Subscription? = null

    override fun getAirports(query: String, isDest: Boolean, observer: Observer<List<SuggestionV4>>, guid: String): Subscription {

        airportSuggestionSubscription?.unsubscribe()

        val suggestType = SuggestionResultType.NEIGHBORHOOD or SuggestionResultType.POINT_OF_INTEREST or SuggestionResultType.MULTI_CITY or
                SuggestionResultType.CITY or SuggestionResultType.AIRPORT or SuggestionResultType.AIRPORT_METRO_CODE

        airportSuggestionSubscription = suggestV4(query, suggestType, isDest, "ta_hierarchy|nearby_airport", "FLIGHTS", maxResults = 10, guid = guid)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.suggestions ?: emptyList() }
                .subscribe(observer)

        return airportSuggestionSubscription as Subscription
    }

    private fun suggestV4(query: String, suggestType: Int, isDest: Boolean, features: String, lineOfBusiness: String,
                          maxResults: Int? = null, guid: String? = null, abTest: String? = null): Observable<SuggestionV4Response> {
        return suggestApi.suggestV4(query, suggestType, isDest, features, lineOfBusiness, maxResults, guid, abTest)
    }
}
