package com.expedia.bookings.services

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.*
import com.expedia.bookings.utils.Strings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.exceptions.OnErrorNotImplementedException
import java.util.ArrayList
import java.util.HashMap
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

public class CarServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor,
                         val ObserveOn: Scheduler, val SubscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    private var cachedCarSearchResponse = CarSearchResponse()

    val carApi: CarApi by Delegates.lazy{

        val gson = generateGson()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .build()

        adapter.create(javaClass<CarApi>())
    }

    public fun carSearch(params: CarSearchParams, observer: Observer<CarSearch>): Subscription {
        val searchByLocationLatLng = params.shouldSearchByLocationLatLng()
        val carSearchResponse = if (searchByLocationLatLng) {
            carApi.roundtripCarSearch(params.pickupLocationLatLng.lat, params.pickupLocationLatLng.lng,
                    params.toServerPickupDate(), params.toServerDropOffDate(), 12)
        } else {
            carApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate())
        }

        return carSearchResponse
                .doOnNext(HANDLE_ERRORS)
                .doOnNext(CACHE_SEARCH_RESPONSE)
                .flatMap(BUCKET_OFFERS)
                .toSortedList(SORT_BY_LOWEST_TOTAL)
                .map(PUT_IN_CAR_SEARCH)
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    public fun carSearchWithProductKey(params: CarSearchParams, productKey: String, observer: Observer<CarSearch>): Subscription {
        val searchByLocationLatLng = params.shouldSearchByLocationLatLng()
        val carSearchResponse = if (searchByLocationLatLng) {
            carApi.roundtripCarSearch(params.pickupLocationLatLng.lat, params.pickupLocationLatLng.lng,
                    params.toServerPickupDate(), params.toServerDropOffDate(), 12)
        } else {
            carApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate())
        }
        return Observable.combineLatest(carSearchResponse, Observable.just(productKey), FIND_PRODUCT_KEY)
                .doOnNext(HANDLE_ERRORS)
                .doOnNext(CACHE_SEARCH_RESPONSE)
                .flatMap(BUCKET_OFFERS)
                .toSortedList(SORT_BY_LOWEST_TOTAL)
                .map(PUT_IN_CAR_SEARCH)
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    public fun carFilterSearch(observer: Observer<CarSearch>, carFilter: CarFilter): Subscription {
        return Observable.combineLatest(Observable.just(cachedCarSearchResponse),
                Observable.just(carFilter), FILTER_RESULTS)
                .flatMap(BUCKET_OFFERS)
                .toSortedList(SORT_BY_LOWEST_TOTAL)
                .map(PUT_IN_CAR_SEARCH)
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    public fun createTrip(productKey: String, fare: Money, isInsuranceIncluded: Boolean,
                          observer: Observer<CarCreateTripResponse>): Subscription {

        return carApi.createTrip(productKey, fare.amount.toString())
                .doOnNext(HANDLE_ERRORS)
                .map { carCreateTripResponse: CarCreateTripResponse ->
                    carCreateTripResponse.carProduct.isInsuranceIncluded = isInsuranceIncluded

                    if (carCreateTripResponse.hasPriceChange()) {
                        carCreateTripResponse.originalPrice = fare.formattedPrice
                    }
                    carCreateTripResponse
                }
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    public fun checkout(offer: CreateTripCarOffer, params: CarCheckoutParams, observer: Observer<CarCheckoutResponse>): Subscription {
        return carApi.checkout(params.toQueryMap())
                .doOnNext(HANDLE_ERRORS)
                .map { carCheckoutResponse: CarCheckoutResponse ->
                    if (carCheckoutResponse.hasPriceChange()) {
                        carCheckoutResponse.originalCarProduct = offer
                    }
                    carCheckoutResponse
                }
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    // Throw an error so the UI can handle it except for price changes.
    // Let the remaining pipline handle those

    private val HANDLE_ERRORS = { response: BaseApiResponse ->
        if (response.hasErrors() && !response.hasPriceChange()) {
            throw response.getFirstError()
        }
    }

    private val CACHE_SEARCH_RESPONSE = { response: CarSearchResponse -> cachedCarSearchResponse = response }

    private val BUCKET_OFFERS = { carSearchResponse: CarSearchResponse ->
        val buckets = HashMap<String, CategorizedCarOffers>()
        carSearchResponse.offers.forEach{ offer ->
            val label = offer.vehicleInfo.carCategoryDisplayLabel
            val category = offer.vehicleInfo.category
            if (Strings.isEmpty(label)) {
                throw OnErrorNotImplementedException(RuntimeException("" + "" +
                        "offer.vehicle.carCategoryDisplayLabel is empty for productKey=" + offer.productKey))
            }

            var bucket: CategorizedCarOffers? = buckets.get(label)
            if (bucket == null) {
                bucket = CategorizedCarOffers(label, category)
                buckets.put(label, bucket)
            }
            bucket.add(offer)
        }
        Observable.from(ArrayList(buckets.values()))
    }

    private val PUT_IN_CAR_SEARCH = { categories: List<CategorizedCarOffers> ->
        val search = CarSearch()
        search.categories = categories
        search
    }

    private val SORT_BY_LOWEST_TOTAL = { left: CategorizedCarOffers, right: CategorizedCarOffers ->
        val leftMoney = left.getLowestTotalPriceOffer().fare.total
        val rightMoney = right.getLowestTotalPriceOffer().fare.total
        leftMoney.compareTo(rightMoney)
    }

    private val FIND_PRODUCT_KEY = { response: CarSearchResponse, productKey: String ->
        if (!response.hasProductKey(productKey)) {
            throw ApiError(ApiError.Code.CAR_PRODUCT_NOT_AVAILABLE)
        }
        val productKeyCarSearchResponse = CarSearchResponse()
        productKeyCarSearchResponse.offers.add(response.getProductKeyResponse(productKey))
        productKeyCarSearchResponse
    }

    private val FILTER_RESULTS = { response: CarSearchResponse, filter: CarFilter ->
        val filteredResponse = CarSearchResponse()
        filteredResponse.offers.addAll(filter.applyFilters(response))
        filteredResponse
    }
    
    public companion object {

        platformStatic public fun generateGson(): Gson {
            return GsonBuilder().registerTypeAdapter(javaClass<DateTime>(), DateTimeTypeAdapter())
                    .registerTypeAdapter(javaClass<RateTerm>(), RateTermDeserializer()).create()
        }
    }
}
