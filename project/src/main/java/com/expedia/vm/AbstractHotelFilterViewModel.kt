package com.expedia.vm

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.FilterAmenity
import com.expedia.bookings.utils.Strings
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashSet
import java.util.regex.Pattern

abstract class AbstractHotelFilterViewModel(val context: Context) {
    val doneObservable = PublishSubject.create<Unit>()
    val doneButtonEnableObservable = PublishSubject.create<Boolean>()
    val clearObservable = PublishSubject.create<Unit>()
    val filterObservable = PublishSubject.create<HotelSearchResponse>()

    var originalResponse: HotelSearchResponse? = null
    var filteredResponse: HotelSearchResponse = HotelSearchResponse()

    val hotelStarRatingBar = BehaviorSubject.create<Int>()
    val updateDynamicFeedbackWidget = BehaviorSubject.create<Int>()
    val finishClear = BehaviorSubject.create<Unit>()
    val filterCountObservable = BehaviorSubject.create<Int>()
    val neighborhoodExpandObservable = BehaviorSubject.create<Boolean>()
    val sortContainerObservable = BehaviorSubject.create<Boolean>()
    val priceRangeContainerVisibility = BehaviorSubject.create<Boolean>()
    val sortByObservable = PublishSubject.create<Sort>()
    val sortSpinnerObservable = PublishSubject.create<Sort>()
    val isCurrentLocationSearch = BehaviorSubject.create<Boolean>(false)
    val clientSideFilterObservable = BehaviorSubject.create<Boolean>()
    val sortContainerVisibilityObservable = sortContainerObservable.withLatestFrom(clientSideFilterObservable, { showSort, clientSide ->
        showSort && clientSide
    })
    val priceRangeContainerObservable = priceRangeContainerVisibility.withLatestFrom(clientSideFilterObservable, { showPriceRange, clientSide ->
        showPriceRange && clientSide
    })

    data class StarRatings(var one: Boolean = false, var two: Boolean = false, var three: Boolean = false, var four: Boolean = false, var five: Boolean = false)

    data class UserFilterChoices(var userSort: Sort = ProductFlavorFeatureConfiguration.getInstance().getDefaultSort(),
                                 var isVipOnlyAccess: Boolean = false,
                                 var hotelStarRating: StarRatings = StarRatings(),
                                 var name: String = "",
                                 var minPrice: Int = 0,
                                 var maxPrice: Int = 0,
                                 var amenity: HashSet<Int> = HashSet<Int>(),
                                 var neighborhoods: HashSet<String> = HashSet<String>(),
                                 var favorites: Boolean = false) {

        fun filterCount(): Int {
            var count = 0
            if (hotelStarRating.one) count++
            if (hotelStarRating.two) count++
            if (hotelStarRating.three) count++
            if (hotelStarRating.four) count++
            if (hotelStarRating.five) count++
            if (isVipOnlyAccess) count++
            if (favorites) count++
            if (name.isNotEmpty()) count++
            if (neighborhoods.isNotEmpty()) count += neighborhoods.size
            if (amenity.isNotEmpty()) count += amenity.size
            if (minPrice != 0 || maxPrice != 0) count++
            return count
        }
    }

    data class PriceRange(val currencyCode: String, val minPrice: Int, val maxPrice: Int) {
        val notches = 30
        val defaultMinPriceText = formatValue(toValue(minPrice))
        val defaultMaxPriceText = formatValue(toValue(maxPrice))

        private fun toValue(price: Int): Int = (((price.toFloat() - minPrice) / maxPrice) * notches).toInt()
        private fun toPrice(value: Int): Int = ((value.toFloat() / notches) * (maxPrice - minPrice) + minPrice).toInt()
        fun formatValue(value: Int): String {
            val price = toPrice(value)
            val str = Money(toPrice(value), currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
            if (price == maxPrice) {
                return str + "+"
            } else {
                return str
            }
        }

        fun update(minValue: Int, maxValue: Int): Pair<Int, Int> {
            val newMaxPrice = toPrice(maxValue)
            return Pair(toPrice(minValue), if (newMaxPrice == maxPrice) 0 else newMaxPrice)
        }
    }

    val userFilterChoices = UserFilterChoices()
    val neighborhoodListObservable = PublishSubject.create<List<HotelSearchResponse.Neighborhood>>()
    val amenityOptionsObservable = PublishSubject.create<Map<String, HotelSearchResponse.AmenityOptions>>()
    val newPriceRangeObservable = PublishSubject.create<PriceRange>()
    val amenityMapObservable = BehaviorSubject.create<Map<FilterAmenity, Int>>()
    val filteredZeroResultObservable = PublishSubject.create<Unit>()
    private var previousSortType = Sort.RECOMMENDED
    private var isNeighborhoodExpanded = false

    private val sortObserver = endlessObserver<Sort> { sort ->
        //TODO server side filters WIP - extract out to client side filter vm
        if (isClientSideFiltering()) {
            if (sort != previousSortType) {
                previousSortType = sort
                val hotels: List<Hotel> = filteredResponse.hotelList

                when (sort) {
                    Sort.RECOMMENDED -> Collections.sort(hotels, popular_comparator)
                    Sort.PRICE -> Collections.sort(hotels, price_comparator)
                    Sort.RATING -> Collections.sort(hotels, rating_comparator_fallback_price)
                    Sort.DEALS -> Collections.sort(hotels, deals_comparator)
                    Sort.PACKAGE_DISCOUNT -> Collections.sort(hotels, package_discount_comparator)
                    Sort.DISTANCE -> Collections.sort(hotels, distance_comparator_fallback_name)
                }
                setFilteredHotelListAndRetainLoyaltyInformation(HotelServices.putSponsoredItemsInCorrectPlaces(hotels))

                val sortByString: String = Strings.capitalizeFirstLetter(sort.toString())
                trackHotelSortBy(sortByString)
            }

            if (filteredResponse.hotelList != null && filteredResponse.hotelList.isNotEmpty()) {
                filteredResponse.isFilteredResponse = true
                filterObservable.onNext(filteredResponse)
            } else {
                filteredZeroResultObservable.onNext(Unit)
            }
        }
    }

    init {
        sortByObservable.subscribe(sortObserver)
        sortByObservable.subscribe(sortSpinnerObservable)

        doneObservable.subscribe { params ->
            sortByObservable.onNext(userFilterChoices.userSort)
        }

        clearObservable.subscribe { params ->
            resetUserFilters()
            setFilteredHotelListAndRetainLoyaltyInformation(originalResponse?.hotelList.orEmpty())
            doneButtonEnableObservable.onNext(true)
            filterCountObservable.onNext(userFilterChoices.filterCount())
            finishClear.onNext(Unit)
            sendNewPriceRange()
        }

        clientSideFilterObservable.onNext(isClientSideFiltering())
    }

    fun handleFiltering() {
        //TODO WIP extract out to client side filter
        if (isClientSideFiltering()) {
            setFilteredHotelListAndRetainLoyaltyInformation(originalResponse?.hotelList.orEmpty().filter { hotel -> isAllowed(hotel) })
            val filterCount = userFilterChoices.filterCount()
            val dynamicFeedbackWidgetCount = if (filterCount > 0) filteredResponse.hotelList.size else -1
            updateDynamicFeedbackWidget.onNext(dynamicFeedbackWidgetCount)
            doneButtonEnableObservable.onNext(filteredResponse.hotelList.size > 0)
            filterCountObservable.onNext(filterCount)
        }
    }

    private fun setFilteredHotelListAndRetainLoyaltyInformation(hotelList: List<Hotel>) {
        filteredResponse.hotelList = hotelList
        filteredResponse.setHasLoyaltyInformation()
    }

    fun resetUserFilters() {
        userFilterChoices.userSort = ProductFlavorFeatureConfiguration.getInstance().getDefaultSort()
        sortSpinnerObservable.onNext(Sort.RECOMMENDED)
        userFilterChoices.isVipOnlyAccess = false
        userFilterChoices.hotelStarRating = StarRatings()
        userFilterChoices.name = ""
        userFilterChoices.minPrice = 0
        userFilterChoices.maxPrice = 0
        userFilterChoices.amenity = HashSet<Int>()
        userFilterChoices.neighborhoods = HashSet<String>()
        userFilterChoices.favorites = false
    }

    fun isAllowed(hotel: Hotel): Boolean {
        return filterIsVipAccess(hotel)
                && filterHotelStarRating(hotel)
                && filterName(hotel)
                && filterPriceRange(hotel)
                && filterAmenity(hotel)
                && filterNeighborhood(hotel)
                && filterFavorites(hotel)
    }

    fun filterIsVipAccess(hotel: Hotel): Boolean {
        if (userFilterChoices.isVipOnlyAccess == false) return true
        return userFilterChoices.isVipOnlyAccess == hotel.isVipAccess
    }

    fun filterHotelStarRating(hotel: Hotel): Boolean {
        if (!userFilterChoices.hotelStarRating.one &&
                !userFilterChoices.hotelStarRating.two &&
                !userFilterChoices.hotelStarRating.three &&
                !userFilterChoices.hotelStarRating.four && !userFilterChoices.hotelStarRating.five) return true

        return (1.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.one) ||
                (2.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.two) ||
                (3.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.three) ||
                (4.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.four) ||
                (5.0f == Math.floor(hotel.hotelStarRating.toDouble()).toFloat() && userFilterChoices.hotelStarRating.five)
    }

    fun filterName(hotel: Hotel): Boolean {
        val name = userFilterChoices.name
        if (name.isBlank()) return true
        val namePattern = Pattern.compile(".*" + userFilterChoices.name + ".*", Pattern.CASE_INSENSITIVE)
        return namePattern.matcher(hotel.localizedName).find()
    }

    fun filterPriceRange(hotel: Hotel): Boolean {
        if (hotel.isSoldOut) {
            //Check if price filters have not been changed
            return userFilterChoices.minPrice == 0 && userFilterChoices.maxPrice == 0;
        }
        val price = hotel.lowRateInfo.priceToShowUsers
        return (userFilterChoices.minPrice == 0 && price < 0) || (userFilterChoices.minPrice <= price &&
                (userFilterChoices.maxPrice == 0 || price <= userFilterChoices.maxPrice))
    }

    fun filterAmenity(hotel: Hotel): Boolean {
        if (userFilterChoices.amenity.isEmpty()) return true
        if (hotel.amenities == null) return false
        if (hotel.amenityFilterIdList == null) {
            hotel.amenityFilterIdList = mapAmenitiesToFilterId(hotel.amenities)
        }

        for (i in userFilterChoices.amenity) {
            if (!hotel.amenityFilterIdList.contains(i)) {
                return false
            }
        }
        return true
    }

    private fun mapAmenitiesToFilterId(amenities: List<Hotel.HotelAmenity>): List<Int> {
        var list = ArrayList<Int>()
        for (amenity in amenities) {
            list.add(FilterAmenity.amenityIdToFilterId(amenity.id.toInt()))
        }
        return list
    }

    fun filterNeighborhood(hotel: Hotel): Boolean {
        if (userFilterChoices.neighborhoods.isEmpty()) return true
        return userFilterChoices.neighborhoods.contains(hotel.locationDescription)
    }

    fun filterFavorites(hotel: Hotel): Boolean {
        if (!userFilterChoices.favorites) return true
        return HotelFavoriteHelper.isHotelFavorite(context, hotel.hotelId) && !hotel.isSponsoredListing
    }

    val favoriteFilteredObserver: Observer<Boolean> = endlessObserver {
        userFilterChoices.favorites = it
        handleFiltering()
    }

    val vipFilteredObserver: Observer<Boolean> = endlessObserver {
        userFilterChoices.isVipOnlyAccess = it
        handleFiltering()
        trackHotelFilterVIP(it)
    }

    val oneStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.one) {
            userFilterChoices.hotelStarRating.one = true
            hotelStarRatingBar.onNext(1)
        } else {
            userFilterChoices.hotelStarRating.one = false
            hotelStarRatingBar.onNext(6)
        }

        handleFiltering()
    }

    val twoStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.two) {
            userFilterChoices.hotelStarRating.two = true
            hotelStarRatingBar.onNext(2)
        } else {
            userFilterChoices.hotelStarRating.two = false
            hotelStarRatingBar.onNext(7)
        }

        handleFiltering()
    }

    val threeStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.three) {
            userFilterChoices.hotelStarRating.three = true
            hotelStarRatingBar.onNext(3)
        } else {
            userFilterChoices.hotelStarRating.three = false
            hotelStarRatingBar.onNext(8)
        }

        handleFiltering()
    }

    val fourStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.four) {
            userFilterChoices.hotelStarRating.four = true
            hotelStarRatingBar.onNext(4)
        } else {
            userFilterChoices.hotelStarRating.four = false
            hotelStarRatingBar.onNext(9)
        }

        handleFiltering()
    }

    val fiveStarFilterObserver: Observer<Unit> = endlessObserver {
        if (!userFilterChoices.hotelStarRating.five) {
            userFilterChoices.hotelStarRating.five = true
            hotelStarRatingBar.onNext(5)
        } else {
            userFilterChoices.hotelStarRating.five = false
            hotelStarRatingBar.onNext(10)
        }

        handleFiltering()
    }

    val priceRangeChangedObserver = endlessObserver<Pair<Int, Int>> { p ->
        userFilterChoices.minPrice = p.first
        userFilterChoices.maxPrice = p.second
        trackHotelFilterPriceSlider()
        handleFiltering()
    }

    var trackingDone = false

    val filterHotelNameObserver = endlessObserver<CharSequence> { s ->
        userFilterChoices.name = s.toString()
        handleFiltering()
        if (s.length == 1 && !trackingDone) {
            trackingDone = true
            trackHotelFilterByName()
        }
        if (s.length == 0) trackingDone = false
    }

    fun setHotelList(response: HotelSearchResponse) {
        originalResponse = response
        neighborhoodListObservable.onNext(response.allNeighborhoodsInSearchRegion)
        setFilteredHotelListAndRetainLoyaltyInformation(ArrayList(response.hotelList))
        filteredResponse.userPriceType = response.userPriceType

        sendNewPriceRange()
        isNeighborhoodExpanded = false
        previousSortType = Sort.RECOMMENDED
        if (isCurrentLocationSearch.value) { // sort by distance on currentLocation search
            sortByObservable.onNext(AbstractHotelFilterViewModel.Sort.DISTANCE)
        } else {
            sortByObservable.onNext(ProductFlavorFeatureConfiguration.getInstance().getDefaultSort())
        }
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

    enum class Sort(@StringRes val resId: Int) {
        RECOMMENDED(R.string.recommended),
        PRICE(R.string.price),
        DEALS(R.string.sort_description_deals),
        PACKAGE_DISCOUNT(R.string.sort_description_package_discount),
        RATING(R.string.rating),
        DISTANCE(R.string.distance);
    }

    private val popular_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        hotel1.sortIndex.compareTo(hotel2.sortIndex)
    }

    private val name_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        hotel1.localizedName.compareTo(hotel2.localizedName)
    }

    private val price_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val lowRate1 = hotel1.lowRateInfo?.priceToShowUsers
        val lowRate2 = hotel2.lowRateInfo?.priceToShowUsers

        if (lowRate1 == null && lowRate2 == null) {
            return@Comparator name_comparator.compare(hotel1, hotel2)
        } else if (lowRate1 == null) {
            return@Comparator 1
        } else if (lowRate2 == null) {
            return@Comparator -1
        }

        lowRate1.compareTo(lowRate2)
    }

    private val deals_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val discountPercent1 = hotel1.lowRateInfo?.discountPercent
        val discountPercent2 = hotel2.lowRateInfo?.discountPercent

        if (discountPercent1 == null && discountPercent2 == null) {
            return@Comparator name_comparator.compare(hotel1, hotel2)
        } else if (discountPercent1 == null) {
            return@Comparator 1
        } else if (discountPercent2 == null) {
            return@Comparator -1
        }

        discountPercent1.compareTo(discountPercent2)
    }

    private val package_discount_comparator: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        hotel2.packageOfferModel.price.tripSavings.amount.compareTo(hotel1.packageOfferModel.price.tripSavings.amount)
    }

    private val rating_comparator_fallback_price: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val comparison = hotel2.hotelGuestRating.compareTo(hotel1.hotelGuestRating)
        if (comparison != 0) comparison else price_comparator.compare(hotel1, hotel2)
    }

    private val distance_comparator_fallback_name: Comparator<Hotel> = Comparator { hotel1, hotel2 ->
        val distance1 = hotel1.proximityDistanceInMiles
        val distance2 = hotel2.proximityDistanceInMiles

        val cmp = distance1.compareTo(distance2)
        if (cmp == 0) {
            name_comparator.compare(hotel1, hotel2)
        } else {
            cmp
        }
    }


    val selectAmenity: Observer<Int> = endlessObserver { amenityId ->
        if (userFilterChoices.amenity.isEmpty() || !userFilterChoices.amenity.contains(amenityId)) {
            userFilterChoices.amenity.add(amenityId)
        } else {
            userFilterChoices.amenity.remove(amenityId)
        }

        handleFiltering()
    }

    val selectNeighborhood = endlessObserver<String> { region ->
        if (userFilterChoices.neighborhoods.isEmpty() || !userFilterChoices.neighborhoods.contains(region)) {
            userFilterChoices.neighborhoods.add(region)
        } else {
            userFilterChoices.neighborhoods.remove(region)
        }

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

    fun isFilteredToZeroResults(): Boolean {
        return userFilterChoices.filterCount() > 0 && filteredResponse.hotelList.isEmpty()
    }

    abstract fun trackHotelSortBy(sortBy: String)
    abstract fun trackHotelFilterVIP(vipOnly: Boolean)
    abstract fun trackHotelFilterPriceSlider()
    abstract fun trackHotelFilterByName()
    abstract fun trackClearFilter()
    abstract fun trackHotelFilterNeighbourhood()
    abstract fun trackHotelRefineRating(rating: String)
    abstract fun sortItemToRemove(): Sort
    abstract fun showHotelFavorite(): Boolean
    abstract fun isClientSideFiltering(): Boolean
}

