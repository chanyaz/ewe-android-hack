package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.data.BaseHotelFilterOptions
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.data.hotel.PriceRange
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelFilterOptions
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.data.packages.PackagesPageUsableData
import com.expedia.bookings.hotel.data.Amenity
import com.expedia.bookings.hotel.widget.OnHotelAmenityFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelNameFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelNeighborhoodFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelPriceFilterChangedListener
import com.expedia.bookings.hotel.widget.OnHotelSortChangedListener
import com.expedia.bookings.hotel.widget.OnHotelVipFilterChangedListener
import com.expedia.bookings.tracking.hotel.FilterTracker
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.GuestRatingValue
import com.expedia.bookings.widget.OnHotelGuestRatingFilterChangedListener
import com.expedia.bookings.widget.OnHotelStarRatingFilterChangedListener
import com.expedia.bookings.widget.StarRatingValue
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.HashSet

abstract class BaseHotelFilterViewModel(val context: Context) {
    var originalResponse: HotelSearchResponse? = null
    var lastUnfilteredSearchParams: HotelSearchParams? = null
    var userFilterChoices = UserFilterChoices()

    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<HotelSearchResponse>()
    val filterChoicesObservable = PublishSubject.create<UserFilterChoices>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val showPreviousResultsObservable = PublishSubject.create<Unit>()

    val clearHotelNameFocusObservable = PublishSubject.create<Unit>()

    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val priceRangeContainerVisibility = BehaviorSubject.create<Boolean>()
    val sortSpinnerObservable = PublishSubject.create<DisplaySort>()
    val isCurrentLocationSearch = BehaviorSubject.createDefault<Boolean>(false)
    val sortContainerVisibilityObservable = BehaviorSubject.create<Boolean>()
    val neighborhoodListObservable = PublishSubject.create<List<Neighborhood>>()
    val newPriceRangeObservable = PublishSubject.create<PriceRange>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    val availableAmenityOptionsObservable = PublishSubject.create<Set<String>>()

    private val filterTracker: FilterTracker = createFilterTracker()
    private var searchedLocationId: String? = null

    private var shouldTrackFilterPriceSlider = true

    var neighborhoodsExist = false
    val presetFilterOptionsUpdatedSubject = PublishSubject.create<UserFilterChoices>()
    var presetFilterOptions = false
    var previousFilterChoices: UserFilterChoices? = null

    init {
        if (!isClientSideFiltering()) {
            doneButtonEnableObservable.onNext(true)
            doneObservable.subscribe {
                filterCountObservable.onNext(userFilterChoices.filterCount())
                if (defaultFilterOptions() && !presetFilterOptions) {
                    originalResponse?.let {
                        filterObservable.onNext(it)
                    }
                } else if (sameFilterOptions()) {
                    showPreviousResultsObservable.onNext(Unit)
                } else {
                    filterChoicesObservable.onNext(userFilterChoices)
                }
                previousFilterChoices = userFilterChoices.copy()
            }
        }

        clearObservable.subscribe {
            resetUserFilters()
            doneButtonEnableObservable.onNext(true)
            filterCountObservable.onNext(userFilterChoices.filterCount())
            finishClear.onNext(Unit)
            sendNewPriceRange()
            previousFilterChoices = null
        }
    }

    val onHotelStarRatingFilterChangedListener = object : OnHotelStarRatingFilterChangedListener {
        override fun onHotelStarRatingFilterChanged(starRatingValue: StarRatingValue, selected: Boolean, doTracking: Boolean) {
            when (starRatingValue) {
                StarRatingValue.One -> {
                    if (doTracking && !userFilterChoices.hotelStarRating.one) {
                        trackHotelRefineRating(starRatingValue.trackingString)
                    }
                    userFilterChoices.hotelStarRating.one = selected
                }
                StarRatingValue.Two -> {
                    if (doTracking && !userFilterChoices.hotelStarRating.two) {
                        trackHotelRefineRating(starRatingValue.trackingString)
                    }
                    userFilterChoices.hotelStarRating.two = selected
                }
                StarRatingValue.Three -> {
                    if (doTracking && !userFilterChoices.hotelStarRating.three) {
                        trackHotelRefineRating(starRatingValue.trackingString)
                    }
                    userFilterChoices.hotelStarRating.three = selected
                }
                StarRatingValue.Four -> {
                    if (doTracking && !userFilterChoices.hotelStarRating.four) {
                        trackHotelRefineRating(starRatingValue.trackingString)
                    }
                    userFilterChoices.hotelStarRating.four = selected
                }
                StarRatingValue.Five -> {
                    if (doTracking && !userFilterChoices.hotelStarRating.five) {
                        trackHotelRefineRating(starRatingValue.trackingString)
                    }
                    userFilterChoices.hotelStarRating.five = selected
                }
            }

            updateFilterCountAndHandleFiltering()
        }
    }
    private fun sameFilterOptions(): Boolean {
        if (previousFilterChoices != null) {
            return userFilterChoices == previousFilterChoices
        }
        return false
    }

    val onHotelGuestRatingFilterChangedListener = object : OnHotelGuestRatingFilterChangedListener {
        override fun onHotelGuestRatingFilterChanged(guestRatingValue: GuestRatingValue, selected: Boolean, doTracking: Boolean) {
            when (guestRatingValue) {
                GuestRatingValue.Three -> {
                    if (doTracking && !userFilterChoices.hotelGuestRating.three) {
                        trackHotelFilterGuestRating(guestRatingValue.trackingString)
                    }
                    userFilterChoices.hotelGuestRating.three = selected
                }
                GuestRatingValue.Four -> {
                    if (doTracking && !userFilterChoices.hotelGuestRating.four) {
                        trackHotelFilterGuestRating(guestRatingValue.trackingString)
                    }
                    userFilterChoices.hotelGuestRating.four = selected
                }
                GuestRatingValue.Five -> {
                    if (doTracking && !userFilterChoices.hotelGuestRating.five) {
                        trackHotelFilterGuestRating(guestRatingValue.trackingString)
                    }
                    userFilterChoices.hotelGuestRating.five = selected
                }
            }
            updateFilterCountAndHandleFiltering()
        }
    }

    val onHotelPriceFilterChangedListener = object : OnHotelPriceFilterChangedListener {
        override fun onHotelPriceFilterChanged(minPrice: Int, maxPrice: Int, doTracking: Boolean) {
            userFilterChoices.minPrice = minPrice
            userFilterChoices.maxPrice = maxPrice

            if (doTracking && shouldTrackFilterPriceSlider) {
                trackHotelFilterPriceSlider()
                shouldTrackFilterPriceSlider = false
            }
            updateFilterCountAndHandleFiltering()
        }
    }

    val onHotelNameFilterChangedListener = object : OnHotelNameFilterChangedListener {
        private var trackingDone = false

        override
        fun onHotelNameFilterChanged(hotelName: CharSequence, doTracking: Boolean) {
            userFilterChoices.name = hotelName.trim().toString()

            if (doTracking && hotelName.length == 1 && !trackingDone) {
                trackingDone = true
                trackHotelFilterByName()
            }
            if (hotelName.isEmpty()) trackingDone = false

            updateFilterCountAndHandleFiltering()
        }
    }

    val onHotelVipFilterChangedListener = object : OnHotelVipFilterChangedListener {
        override
        fun onHotelVipFilterChanged(vipChecked: Boolean, doTracking: Boolean) {
            userFilterChoices.isVipOnlyAccess = vipChecked
            clearHotelNameFocusObservable.onNext(Unit)

            if (doTracking) {
                trackHotelFilterVIP(vipChecked)
            }
            updateFilterCountAndHandleFiltering()
        }
    }

    val onHotelAmenityFilterChangedListener = object : OnHotelAmenityFilterChangedListener {
        override
        fun onHotelAmenityFilterChanged(amenity: Amenity, selected: Boolean, doTracking: Boolean) {
            val id = Amenity.getSearchKey(amenity)
            if (selected) {
                userFilterChoices.amenities.add(id)
            } else {
                userFilterChoices.amenities.remove(id)
            }

            if (doTracking) {
                trackHotelFilterAmenity(amenity)
            }

            updateFilterCountAndHandleFiltering()
        }
    }

    val onHotelNeighborhoodFilterChangedListener = object : OnHotelNeighborhoodFilterChangedListener {
        override
        fun onHotelNeighborhoodFilterChanged(neighborhood: Neighborhood, selected: Boolean, doTracking: Boolean) {
            if (selected) {
                if (isClientSideFiltering()) {
                    onClientSideNeighborhoodFilterSelected(neighborhood, doTracking)
                } else {
                    onServerSideNeighborhoodFilterSelected(neighborhood, doTracking)
                }
            } else {
                onNeighborhoodFilterUnselected(neighborhood)
            }
            updateFilterCountAndHandleFiltering()
        }
    }

    val onHotelSortChangedListener = object : OnHotelSortChangedListener {
        override
        fun onHotelSortChanged(displaySort: DisplaySort, doTracking: Boolean) {
            userFilterChoices.userSort = displaySort

            if (doTracking) {
                val sortByString = if (displaySort == DisplaySort.PACKAGE_DISCOUNT) {
                    "Discounts"
                } else {
                    Strings.capitalizeFirstLetter(displaySort.toString())
                }
                trackHotelSortBy(sortByString)
            }
        }
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

    private fun updateFilterCountAndHandleFiltering() {
        updateFilterCount()
        handleFiltering()
    }

    private fun onClientSideNeighborhoodFilterSelected(neighborhood: Neighborhood, doTracking: Boolean) {
        if (userFilterChoices.neighborhoods.isEmpty() || !userFilterChoices.neighborhoods.contains(neighborhood)) {
            userFilterChoices.neighborhoods.add(neighborhood)
            if (doTracking) {
                trackHotelFilterNeighborhood()
            }
        }
    }

    private fun onServerSideNeighborhoodFilterSelected(neighborhood: Neighborhood, doTracking: Boolean) {
        userFilterChoices.neighborhoods.clear()
        userFilterChoices.neighborhoods.add(neighborhood)
        if (doTracking) {
            trackHotelFilterNeighborhood()
        }
    }

    private fun onNeighborhoodFilterUnselected(neighborhood: Neighborhood) {
        if (!userFilterChoices.neighborhoods.isEmpty() && userFilterChoices.neighborhoods.contains(neighborhood)) {
            userFilterChoices.neighborhoods.remove(neighborhood)
        }
    }

    private fun trackHotelFilterVIP(vipOnly: Boolean) {
        filterTracker.trackHotelFilterVIP(vipOnly)
    }

    private fun trackHotelFilterGuestRating(rating: String) {
        filterTracker.trackHotelFilterGuestRating(rating)
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

    fun trackHotelFilterApplied() {
        filterTracker.trackHotelFilterApplied()
        if (!isClientSideFiltering()) {
            PackagesPageUsableData.HOTEL_FILTERED_RESULTS.pageUsableData.markPageLoadStarted()
        }
    }

    private fun updateFilterCount() {
        filterCountObservable.onNext(userFilterChoices.filterCount())
    }

    private fun resetUserFilters() {
        userFilterChoices.userSort = getDefaultSort()
        sortSpinnerObservable.onNext(userFilterChoices.userSort)
        userFilterChoices.isVipOnlyAccess = false
        userFilterChoices.hotelStarRating = UserFilterChoices.StarRatings()
        userFilterChoices.hotelGuestRating = UserFilterChoices.GuestRatings()
        userFilterChoices.name = ""
        userFilterChoices.minPrice = 0
        userFilterChoices.maxPrice = 0
        userFilterChoices.amenities = HashSet()
        userFilterChoices.neighborhoods = HashSet()
    }

    fun updatePresetOptions(filterOptions: BaseHotelFilterOptions) {
        presetFilterOptions = false
        if (filterOptions.isNotEmpty() && filterOptions is HotelFilterOptions) {
            val filterChoices = UserFilterChoices.fromHotelFilterOptions(filterOptions)
            previousFilterChoices = filterChoices
            presetFilterOptions = true
            presetFilterOptionsUpdatedSubject.onNext(filterChoices)
        }
    }
}
