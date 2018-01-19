package com.expedia.bookings.server.apollo

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.expedia.bookings.apollographql.HotelSearchQuery
import com.expedia.bookings.apollographql.type.AuthState
import com.expedia.bookings.apollographql.type.Context
import com.expedia.bookings.apollographql.type.Date
import com.expedia.bookings.apollographql.type.SearchCriteria
import com.expedia.bookings.apollographql.type.User
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.subscribeObserver
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.OkHttpClient

class GraphQLServices(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val apolloClient: ApolloClient by lazy {
        ApolloClient.builder()
                .serverUrl(endpoint)
                .okHttpClient(okHttpClient)
                .build()
    }

    fun doRxSearch(params: HotelSearchParams, observer : Observer<Response<HotelSearchQuery.Data>>) : Disposable {
        val query : ApolloCall<HotelSearchQuery.Data> = apolloClient.query(buildQuery(params)).responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        return Rx2Apollo.from(query)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribeObserver(observer)
    }

    private fun buildQuery(params: HotelSearchParams): HotelSearchQuery {
        val context = Context.builder()
                .guid("asdf-asdf-asdf-asdf")
                .siteId(1)
                .locale("en_US")
                .user(User.builder().state(AuthState.ANONYMOUS).build())
                .build()

        val searchCriteria = SearchCriteria.builder()
                .startDate(Date.builder()
                        .month(params.checkIn.monthOfYear.toLong())
                        .day(params.checkIn.dayOfMonth.toLong())
                        .year(params.checkIn.year.toLong())
                        .build())
                .endDate(Date.builder()
                        .month(params.checkOut.monthOfYear.toLong())
                        .day(params.checkOut.dayOfMonth.toLong())
                        .year(params.checkOut.year.toLong())
                        .build())
                .regionId(params.suggestion.gaiaId.toLong())
                .build()

        return HotelSearchQuery.builder()
                .context(context)
                .searchCriteria(searchCriteria)
                .build()
    }
}
