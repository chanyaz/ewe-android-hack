package com.expedia.vm.hotel

import android.content.Context
import android.support.annotation.CallSuper
import com.expedia.bookings.data.hotel.PriceRange
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.bookings.tracking.hotel.HotelFilterTracker
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.HashSet

abstract class BaseHotelFilterViewModel(val context: Context) {
    var originalResponse: HotelSearchResponse? = null
    val userFilterChoices = UserFilterChoices()

    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<HotelSearchResponse>()
    val filterByParamsObservable = PublishSubject.create<UserFilterChoices>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()

    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val neighborhoodExpandObservable = BehaviorSubject.create<Boolean>()
    val priceRangeContainerVisibility = BehaviorSubject.create<Boolean>()
    val sortByObservable = PublishSubject.create<Sort>()
    val sortSpinnerObservable = PublishSubject.create<Sort>()
    val isCurrentLocationSearch = BehaviorSubject.create<Boolean>(false)
    val clientSideFilterObservable = BehaviorSubject.create<Boolean>()
    val sortContainerObservable = BehaviorSubject.create<Boolean>()
    val sortContainerVisibilityObservable = sortContainerObservable.withLatestFrom(clientSideFilterObservable, { showSort, clientSide ->
        showSort && clientSide
    })
    val priceRangeContainerObservable = priceRangeContainerVisibility.withLatestFrom(clientSideFilterObservable, { showPriceRange, clientSide ->
        showPriceRange && clientSide
    })
    val neighborhoodListObservable = PublishSubject.create<List<HotelSearchResponse.Neighborhood>>()
    val newPriceRangeObservable = PublishSubject.create<PriceRange>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()

    private var trackingDone = false
    private var isNeighborhoodExpanded = false
    private val filterTracker: FilterTracker = createFilterTracker()

    init {
        sortByObservable.subscribe(sortSpinnerObservable)

        clearObservable.subscribe {
            resetUserFilters()
            doneButtonEnableObservable.onNext(true)
            filterCountObservable.onNext(userFilterChoices.filterCount())
            finishClear.onNext(Unit)
            sendNewPriceRange()
        }

        clientSideFilterObservable.onNext(isClientSideFiltering())
    }

    val oneStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.one) {
            userFilterChoices.hotelStarRating.one = true
            trackHotelRefineRating("1")
        } else {
            userFilterChoices.hotelStarRating.one = false
        }

        updateFilterCount()
        handleFiltering()
    }

    val twoStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.two) {
            userFilterChoices.hotelStarRating.two = true
            trackHotelRefineRating("2")
        } else {
            userFilterChoices.hotelStarRating.two = false
        }

        updateFilterCount()
        handleFiltering()
    }

    val threeStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.three) {
            userFilterChoices.hotelStarRating.three = true
            trackHotelRefineRating("3")
        } else {
            userFilterChoices.hotelStarRating.three = false
        }

        updateFilterCount()
        handleFiltering()
    }

    val fourStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.four) {
            userFilterChoices.hotelStarRating.four = true
            trackHotelRefineRating("4")
        } else {
            userFilterChoices.hotelStarRating.four = false
        }

        updateFilterCount()
        handleFiltering()
    }

    val fiveStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.five) {
            userFilterChoices.hotelStarRating.five = true
            trackHotelRefineRating("5")
        } else {
            userFilterChoices.hotelStarRating.five = false
        }

        updateFilterCount()
        handleFiltering()
    }

    val priceRangeChangedObserver = endlessObserver<Pair<Int, Int>> { minMaxPair ->
        userFilterChoices.minPrice = minMaxPair.first
        userFilterChoices.maxPrice = minMaxPair.second
        trackHotelFilterPriceSlider()

        updateFilterCount()
        handleFiltering()
    }

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        userFilterChoices.name = s.toString()
        if (s.length == 1 && !trackingDone) {
            trackingDone = true
            trackHotelFilterByName()
        }
        if (s.length == 0) trackingDone = false

        updateFilterCount()
        handleFiltering()
    }

    val favoriteFilteredObserver: Observer<Boolean> = endlessObserver { filterFavorites ->
        userFilterChoices.favorites = filterFavorites
        updateFilterCount()
        handleFiltering()
    }

    val vipFilteredObserver: Observer<Boolean> = endlessObserver { vipOnly ->
        userFilterChoices.isVipOnlyAccess = vipOnly
        updateFilterCount()
        handleFiltering()
        trackHotelFilterVIP(vipOnly)
    }

    val selectAmenity: Observer<Int> = endlessObserver { amenityId ->
        if (userFilterChoices.amenity.isEmpty() || !userFilterChoices.amenity.contains(amenityId)) {
            userFilterChoices.amenity.add(amenityId)
        } else {
            userFilterChoices.amenity.remove(amenityId)
        }

        updateFilterCount()
        handleFiltering()
    }

    val selectNeighborhood = endlessObserver<String> { region ->
        if (userFilterChoices.neighborhoods.isEmpty() || !userFilterChoices.neighborhoods.contains(region)) {
            userFilterChoices.neighborhoods.add(region)
        } else {
            userFilterChoices.neighborhoods.remove(region)
        }

        updateFilterCount()
        handleFiltering()
    }

    val neighborhoodMoreLessObservable: Observer<Unit> = endlessObserver {
        if (!isNeighborhoodExpanded) {
            isNeighborhoodExpanded = true
        } else {
            isNeighborhoodExpanded = false
        }
        neighborhoodExpandObservable.onNext(isNeighborhoodExpanded)
    }

    open fun isFilteredToZeroResults(): Boolean {
        return false
    }

    open fun setHotelList(response: HotelSearchResponse) {
        originalResponse = response
        neighborhoodListObservable.onNext(response.allNeighborhoodsInSearchRegion)
        sendNewPriceRange()
        isNeighborhoodExpanded = false

        if (isCurrentLocationSearch.value) { // sort by distance on currentLocation search
            sortByObservable.onNext(Sort.DISTANCE)
        } else {
            sortByObservable.onNext(ProductFlavorFeatureConfiguration.getInstance().getDefaultSort())
        }
    }

    fun sendNewPriceRange() {
        val response = originalResponse
        if (response != null && response.priceOptions.isNotEmpty()) {
            val min = response.priceOptions.first().minPrice
            val max = response.priceOptions.last().minPrice
            val currency = response.hotelList.firstOrNull()?.rateCurrencyCode
            if (currency != null) {
                newPriceRangeObservable.onNext(PriceRange(currency, min, max))
            }
        }
    }

    open fun sortItemToRemove(): Sort {
        return Sort.PACKAGE_DISCOUNT
    }

    fun trackClearFilter() {
        filterTracker.trackClearFilter()
    }

    fun trackHotelFilterNeighborhood() {
        filterTracker.trackHotelFilterNeighborhood()
    }

    open protected fun handleFiltering() {
        //nothing by default
    }

    protected open fun createFilterTracker(): FilterTracker {
        return HotelFilterTracker()
    }

    protected fun trackHotelSortBy(sortBy: String) {
        filterTracker.trackHotelSortBy(sortBy)
    }

    private fun trackHotelFilterVIP(vipOnly: Boolean) {
        filterTracker.trackHotelFilterVIP(vipOnly)
    }

    private fun trackHotelFilterPriceSlider() {
        filterTracker.trackHotelFilterPriceSlider()
    }

    private fun trackHotelFilterByName() {
        filterTracker.trackHotelFilterByName()
    }

    private fun trackHotelRefineRating(rating: String) {
        filterTracker.trackHotelRefineRating(rating)
    }

    private fun updateFilterCount() {
        filterCountObservable.onNext(userFilterChoices.filterCount())
    }

    private fun resetUserFilters() {
        userFilterChoices.userSort = ProductFlavorFeatureConfiguration.getInstance().getDefaultSort()
        sortSpinnerObservable.onNext(Sort.RECOMMENDED)
        userFilterChoices.isVipOnlyAccess = false
        userFilterChoices.hotelStarRating = UserFilterChoices.StarRatings()
        userFilterChoices.name = ""
        userFilterChoices.minPrice = 0
        userFilterChoices.maxPrice = 0
        userFilterChoices.amenity = HashSet<Int>()
        userFilterChoices.neighborhoods = HashSet<String>()
        userFilterChoices.favorites = false
    }

    abstract fun showHotelFavorite(): Boolean
    abstract fun isClientSideFiltering(): Boolean

}