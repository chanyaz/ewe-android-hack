package com.expedia.vm.hotel

import android.content.Context
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.PriceRange
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.util.endlessObserver
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.HashSet

abstract class BaseHotelFilterViewModel(val context: Context) {
    var originalResponse: HotelSearchResponse? = null
    var userFilterChoices = UserFilterChoices()

    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<HotelSearchResponse>()
    val filterByParamsObservable = PublishSubject.create<UserFilterChoices>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val showPreviousResultsObservable = PublishSubject.create<Unit>()

    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val priceRangeContainerVisibility = BehaviorSubject.create<Boolean>()
    val sortSpinnerObservable = PublishSubject.create<DisplaySort>()
    val isCurrentLocationSearch = BehaviorSubject.createDefault<Boolean>(false)
    val sortContainerVisibilityObservable = BehaviorSubject.create<Boolean>()
    val neighborhoodListObservable = PublishSubject.create<List<HotelSearchResponse.Neighborhood>>()
    val newPriceRangeObservable = PublishSubject.create<PriceRange>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    val newSearchOptionsObservable = PublishSubject.create<HotelSearchParams.HotelFilterOptions>()

    private var trackingDone = false
    private val filterTracker: FilterTracker = createFilterTracker()
    private var searchedLocationId: String? = null

    private var shouldTrackFilterPriceSlider = true

    var neighborhoodsExist = false

    init {
        clearObservable.subscribe {
            resetUserFilters()
            doneButtonEnableObservable.onNext(true)
            filterCountObservable.onNext(userFilterChoices.filterCount())
            finishClear.onNext(Unit)
            sendNewPriceRange()
        }
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

        if (shouldTrackFilterPriceSlider) {
            trackHotelFilterPriceSlider()
            shouldTrackFilterPriceSlider = false
        }

        updateFilterCount()
        handleFiltering()
    }

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        userFilterChoices.name = s.toString()
        if (s.length == 1 && !trackingDone) {
            trackingDone = true
            trackHotelFilterByName()
        }
        if (s.isEmpty()) trackingDone = false

        updateFilterCount()
        handleFiltering()
    }

    val vipFilteredObserver: Observer<Boolean> = endlessObserver { vipOnly ->
        userFilterChoices.isVipOnlyAccess = vipOnly
        updateFilterCount()
        handleFiltering()
        trackHotelFilterVIP(vipOnly)
    }

    val selectAmenity: Observer<Amenity> = endlessObserver { amenity ->
        toggleAmenity(amenity, true)
    }

    val deselectAmenity: Observer<Amenity> = endlessObserver { amenity ->
        toggleAmenity(amenity, false)
    }

    val selectNeighborhood = endlessObserver<HotelSearchResponse.Neighborhood> { neighborhood ->
        if (isClientSideFiltering()) {
            if (userFilterChoices.neighborhoods.isEmpty() || !userFilterChoices.neighborhoods.contains(neighborhood)) {
                userFilterChoices.neighborhoods.add(neighborhood)
                trackHotelFilterNeighborhood()
            }
        } else {
            userFilterChoices.neighborhoods.clear()
            userFilterChoices.neighborhoods.add(neighborhood)
            trackHotelFilterNeighborhood()
        }

        updateFilterCount()
        handleFiltering()
    }

    val deselectNeighborhood = endlessObserver<HotelSearchResponse.Neighborhood> { neighborhood ->
        if (!userFilterChoices.neighborhoods.isEmpty() && userFilterChoices.neighborhoods.contains(neighborhood)) {
            userFilterChoices.neighborhoods.remove(neighborhood)
        }

        updateFilterCount()
        handleFiltering()
    }

    abstract fun sortItemToRemove(): DisplaySort
    protected abstract fun createFilterTracker(): FilterTracker
    protected abstract fun isClientSideFiltering(): Boolean

    open fun isFilteredToZeroResults(): Boolean {
        return false
    }

    open fun setHotelList(response: HotelSearchResponse) {
        originalResponse = response
        var neighborhoods = response.allNeighborhoodsInSearchRegion
        if (searchedLocationId != null) {
            neighborhoods = response.allNeighborhoodsInSearchRegion.filter { neighborhood ->
                neighborhood.id != searchedLocationId
            }
        }

        neighborhoodListObservable.onNext(neighborhoods)
        neighborhoodsExist = neighborhoods != null && neighborhoods.size > 0
        sendNewPriceRange()
    }

    private fun toggleAmenity(amenity: Amenity, on: Boolean) {
        val id = Amenity.getSearchKey(amenity)
        if (on) {
            userFilterChoices.amenities.add(id)
        } else {
            userFilterChoices.amenities.remove(id)
        }
        updateFilterCount()
        handleFiltering()
        trackHotelFilterAmenity(amenity)
    }

    private fun sendNewPriceRange() {
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

    fun trackClearFilter() {
        filterTracker.trackClearFilter()
    }

    fun trackHotelFilterNeighborhood() {
        filterTracker.trackHotelFilterNeighborhood()
    }

    fun setSearchLocationId(regionId: String) {
        searchedLocationId = regionId
    }

    fun trackHotelSortBy(sortBy: String) {
        filterTracker.trackHotelSortBy(sortBy)
    }

    fun resetPriceSliderFilterTracking() {
        shouldTrackFilterPriceSlider = true
    }

    protected open fun handleFiltering() {
        //nothing by default
    }

    protected fun defaultFilterOptions(): Boolean {
        return userFilterChoices.filterCount() == 0 && userFilterChoices.userSort == getDefaultSort()
    }

    protected open fun getDefaultSort(): DisplaySort {
        return DisplaySort.getDefaultSort()
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

    private fun trackHotelFilterAmenity(amenity: Amenity) {
        filterTracker.trackHotelFilterAmenity(amenity.toString())
    }

    private fun updateFilterCount() {
        filterCountObservable.onNext(userFilterChoices.filterCount())
    }

    private fun resetUserFilters() {
        userFilterChoices.userSort = getDefaultSort()
        sortSpinnerObservable.onNext(userFilterChoices.userSort)
        userFilterChoices.isVipOnlyAccess = false
        userFilterChoices.hotelStarRating = UserFilterChoices.StarRatings()
        userFilterChoices.name = ""
        userFilterChoices.minPrice = 0
        userFilterChoices.maxPrice = 0
        userFilterChoices.amenities = HashSet()
        userFilterChoices.neighborhoods = HashSet()
    }
}
