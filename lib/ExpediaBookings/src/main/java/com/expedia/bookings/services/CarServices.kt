package com.expedia.bookings.services

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.cars.CarCheckoutParams
import com.expedia.bookings.data.cars.CarCheckoutResponse
import com.expedia.bookings.data.cars.CarCreateTripResponse
import com.expedia.bookings.data.cars.CarFilter
import com.expedia.bookings.data.cars.CarSearch
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.data.cars.CarSearchResponse
import com.expedia.bookings.data.cars.CategorizedCarOffers
import com.expedia.bookings.data.cars.CreateTripCarOffer
import com.expedia.bookings.data.cars.RateTerm
import com.expedia.bookings.data.cars.RateTermDeserializer
import com.expedia.bookings.data.cars.SearchCarOffer
import com.expedia.bookings.utils.Strings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.LocalDate
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.exceptions.OnErrorNotImplementedException
import rx.functions.Action1
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap

class CarServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val ObserveOn: Scheduler, val SubscribeOn: Scheduler) {

    private var cachedCarSearchResponse = CarSearchResponse()

    val carApi: CarApi by lazy {

        val gson = generateGson()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(CarApi::class.java)
    }

    fun carSearch(params: CarSearchParam, observer: Observer<CarSearch>): Subscription {
        val searchByLocationLatLng = params.shouldSearchByLocationLatLng()
        val carSearchResponse = if (searchByLocationLatLng) {
            carApi.roundtripCarSearch(params.pickupLocationLatLng!!.lat, params.pickupLocationLatLng.lng,
                    params.toServerPickupDate(), params.toServerDropOffDate(), 12)
        } else {
            carApi.roundtripCarSearch(params.originLocation, params.toServerPickupDate(), params.toServerDropOffDate())
        }

        return carSearchResponse
                .doOnNext(HANDLE_ERRORS)
                .doOnNext(CACHE_SEARCH_RESPONSE)
                .doOnNext(SORT_OFFERS_BY_LOWEST_TOTAL)
                .flatMap(BUCKET_OFFERS)
                .toSortedList(SORT_BY_LOWEST_TOTAL)
                .map(PUT_IN_CAR_SEARCH)
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    fun carSearchWithProductKey(params: CarSearchParam, productKey: String, observer: Observer<CarSearch>): Subscription {
        val searchByLocationLatLng = params.shouldSearchByLocationLatLng()
        val carSearchResponse = if (searchByLocationLatLng) {
            carApi.roundtripCarSearch(params.pickupLocationLatLng!!.lat, params.pickupLocationLatLng.lng,
                    params.toServerPickupDate(), params.toServerDropOffDate(), 12)
        } else {
            carApi.roundtripCarSearch(params.originLocation, params.toServerPickupDate(), params.toServerDropOffDate())
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

    fun carFilterSearch(observer: Observer<CarSearch>, carFilter: CarFilter): Subscription {
        return Observable.combineLatest(Observable.just(cachedCarSearchResponse),
                Observable.just(carFilter), FILTER_RESULTS)
                .flatMap(BUCKET_OFFERS)
                .toSortedList(SORT_BY_LOWEST_TOTAL)
                .map(PUT_IN_CAR_SEARCH)
                .subscribeOn(SubscribeOn)
                .observeOn(ObserveOn)
                .subscribe(observer)
    }

    fun createTrip(productKey: String, fare: Money, isInsuranceIncluded: Boolean,
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

    fun checkout(offer: CreateTripCarOffer, params: CarCheckoutParams, observer: Observer<CarCheckoutResponse>): Subscription {
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
            throw response.firstError
        }
    }

    private val CACHE_SEARCH_RESPONSE = { response: CarSearchResponse -> cachedCarSearchResponse = response }

    private val BUCKET_OFFERS = { carSearchResponse: CarSearchResponse ->
        val buckets = HashMap<String, CategorizedCarOffers>()
        carSearchResponse.offers.forEach { offer ->
            val label = offer.vehicleInfo.carCategoryDisplayLabel
            val category = offer.vehicleInfo.category
            if (Strings.isEmpty(label)) {
                throw OnErrorNotImplementedException(RuntimeException("" + "" +
                        "offer.vehicle.carCategoryDisplayLabel is empty for productKey=" + offer.productKey))
            }

            var bucket: CategorizedCarOffers? = buckets[label]
            if (bucket == null) {
                bucket = CategorizedCarOffers(label, category)
                buckets.put(label, bucket)
            }
            bucket.add(offer)
        }
        Observable.from(ArrayList(buckets.values))
    }

    private val PUT_IN_CAR_SEARCH = { categories: List<CategorizedCarOffers> ->
        val search = CarSearch()
        search.categories = categories
        search
    }

    private val SORT_BY_LOWEST_TOTAL = { left: CategorizedCarOffers, right: CategorizedCarOffers ->
        val leftMoney = left.lowestTotalPriceOffer.fare.total
        val rightMoney = right.lowestTotalPriceOffer.fare.total
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

    companion object {

        @JvmStatic fun generateGson(): Gson {
            val PATTERN = "yyyy-MM-dd"
            return GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                    .registerTypeAdapter(RateTerm::class.java, RateTermDeserializer())
                    .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter(PATTERN))
                    .create()
        }

        @JvmStatic val SORT_OFFERS_BY_LOWEST_TOTAL = object : Action1<CarSearchResponse> {
            override fun call(carSearchResponse: CarSearchResponse) {
                Collections.sort(carSearchResponse.offers, object : Comparator<SearchCarOffer> {
                    override fun compare(o1: SearchCarOffer, o2: SearchCarOffer): Int {
                        return o1.fare.total.compareTo(o2.fare.total)
                    }
                })
            }
        }

    }
}
