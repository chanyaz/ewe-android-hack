package com.expedia.vm

import android.content.Context
import android.content.res.Resources
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.cars.BaseApiResponse
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.extension.isShowAirAttached
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.widget.createHotelMarkerIcon
import com.expedia.util.endlessObserver
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ui.IconGenerator
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class HotelResultsViewModel(private val context: Context, private val hotelServices: HotelServices?, private val lob: LineOfBusiness) {

    // Inputs
    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()

    // Outputs
    val hotelResultsObservable = BehaviorSubject.create<HotelSearchResponse>()
    val mapResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val errorObservable = PublishSubject.create<ApiError>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()
    val showHotelSearchViewObservable = PublishSubject.create<Unit>()

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            val cachedParams: HotelSearchParams? = paramsSubject.value
            val params = HotelSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_hotel_stay))
                    .suggestion(suggestion)
                    .checkIn(cachedParams?.checkIn)
                    .checkOut(cachedParams?.checkOut)
                    .adults(cachedParams?.adults!!)
                    .children(cachedParams?.children!!)
                    .build()
            doSearch(params)
        })

        hotelResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }

        mapResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }
    }

    private fun doSearch(params: HotelSearchParams) {
        val isPackages = lob == LineOfBusiness.PACKAGES
        titleSubject.onNext(if (isPackages) StrUtils.formatCity(params.suggestion) else params.suggestion.regionNames.shortName)

        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests()))
                .format())

        hotelServices?.regionSearch(params)?.subscribe(object : Observer<HotelSearchResponse> {
            override fun onNext(it: HotelSearchResponse) {
                if (it.hasErrors()) {
                    errorObservable.onNext(it.firstError)
                } else if (it.hotelList.isEmpty()) {
                    var error: ApiError
                    if (titleSubject.value == context.getString(R.string.visible_map_area)) {
                        error = ApiError(ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS)
                    } else {
                        error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
                    }
                    errorObservable.onNext(error)
                } else if (titleSubject.value == context.getString(R.string.visible_map_area)) {
                    mapResultsObservable.onNext(it)
                } else {
                    hotelResultsObservable.onNext(it)
                    HotelV2Tracking().trackHotelsV2Search(paramsSubject.value, it)
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        doSearch(paramsSubject.value)
                    }
                    val cancelFun = fun() {
                        showHotelSearchViewObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                }
            }
        })
    }
}

public class HotelResultsPricingStructureHeaderViewModel(private val resources: Resources) {
    // Inputs
    val loadingStartedObserver = PublishSubject.create<Unit>()
    val resultsDeliveredObserver = PublishSubject.create<Pair<List<Hotel>, HotelRate.UserPriceType>>()

    // Outputs
    val pricingStructureHeaderObservable = BehaviorSubject.create<String>()

    init {
        loadingStartedObserver.subscribe {
            pricingStructureHeaderObservable.onNext(resources.getString(R.string.progress_searching_hotels_hundreds))
        }

        resultsDeliveredObserver.subscribe { response ->
            val list = response.first
            val priceType = response.second
            val hotelResultsCount = list.size
            val header =
                    when (priceType) {
                        HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_total_price_for_stay_TEMPLATE, hotelResultsCount, hotelResultsCount)
                        HotelRate.UserPriceType.PER_NIGHT_RATE_NO_TAXES -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_prices_avg_per_night_TEMPLATE, hotelResultsCount, hotelResultsCount)
                        else -> resources.getQuantityString(R.plurals.hotel_results_default_header_TEMPLATE, hotelResultsCount, hotelResultsCount)
                    }

            pricingStructureHeaderObservable.onNext(header)
        }
    }
}

public open class HotelResultsMapViewModel(val context: Context, val currentLocation: Location, val factory: IconGenerator) {

    var hotels: List<Hotel> = emptyList()

    //inputs
    val hotelResultsSubject = PublishSubject.create<HotelSearchResponse>()
    val mapResultsSubject = PublishSubject.create<HotelSearchResponse>()
    val mapPinSelectSubject = PublishSubject.create<Marker>()
    val carouselSwipedObservable = PublishSubject.create<Marker>()
    val mapBoundsSubject = BehaviorSubject.create<LatLng>()

    //outputs
    val markersObservable = PublishSubject.create<List<Hotel>>()
    val newBoundsObservable = BehaviorSubject.create<LatLngBounds>()
    val sortedHotelsObservable = PublishSubject.create<List<Hotel>>()
    val unselectedMarker = PublishSubject.create<Pair<Marker?, Hotel>>()
    val selectMarker = BehaviorSubject.create<Pair<Marker?, Hotel>>()
    val soldOutMarker = BehaviorSubject.create<Pair<Marker?, Hotel>>()
    val soldOutHotel = PublishSubject.create<Hotel>()

    val hotelSoldOutWithIdObserver = endlessObserver<String> { soldOutHotelId ->
        val hotel = hotels.firstOrNull { it.hotelId == soldOutHotelId }
        if (hotel != null) {
            hotel.isSoldOut = true
            soldOutHotel.onNext(hotel)
        }
    }

    init {
        mapBoundsSubject.subscribe {
            // Map bounds has changed(Search this area or map was animated to a region),
            // sort nearest hotels from center of screen
            val currentRegion = it
            val location = Location("currentRegion")
            location.latitude = currentRegion.latitude
            location.longitude = currentRegion.longitude
            val sortedHotels = sortByLocation(location, hotels)
            markersObservable.onNext(sortedHotels)
        }

        hotelResultsSubject.subscribe { response ->
            hotels = response.hotelList
            if (response.hotelList != null && response.hotelList.size > 0) {
                newBoundsObservable.onNext(getMapBounds(response))
            }
        }

        mapResultsSubject.subscribe { response ->
            hotels = response.hotelList
            markersObservable.onNext(hotels)
        }

        mapPinSelectSubject.subscribe {
            // Map pin was click, sort nearest hotels from pin
            val hotel = getHotelWithMarker(it)
            val hotelLocation = Location("selected")
            hotelLocation.latitude = hotel.latitude
            hotelLocation.longitude = hotel.longitude
            val sortedHotels = sortByLocation(hotelLocation, hotels)

            sortedHotelsObservable.onNext(sortedHotels)
            HotelV2Tracking().trackHotelV2MapTapPin()
        }

        carouselSwipedObservable.subscribe {
            val previousMarker = selectMarker.value
            if (previousMarker != null) unselectedMarker.onNext(previousMarker)
            selectMarker.onNext(Pair(it, getHotelWithMarker(it)))
        }

        soldOutMarker.subscribe {
            if (it != null) {
                val marker = it.first
                val hotel = it.second
                marker?.setIcon(createHotelMarkerIcon(context, factory, hotel, false, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut))
            }
        }

        unselectedMarker.subscribe {
            if (it != null) {
                val marker = it.first
                val hotel = it.second
                marker?.setIcon(createHotelMarkerIcon(context, factory, hotel, false, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut))
            }
        }

        selectMarker.subscribe {
            if (it != null) {
                val marker = it.first
                val hotel = it.second
                marker?.setIcon(createHotelMarkerIcon(context, factory, hotel, true, hotel.lowRateInfo.isShowAirAttached(), hotel.isSoldOut))
                marker?.showInfoWindow()
            }
        }
    }

    private fun getHotelWithMarker(marker: Marker?): Hotel {
        val hotelId = marker?.title
        val hotel = hotels.filter { it.hotelId == hotelId }.first()
        return hotel
    }

    fun getMapBounds(res: BaseApiResponse): LatLngBounds {
        val response = res as HotelSearchResponse
        val searchRegionId = response.searchRegionId
        val currentLocationLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        val sortedHotels = sortByLocation(currentLocation, response.hotelList)

        val allHotelsBox: LatLngBounds = boxHotels(response.hotelList)

        //If filtered, show all hotels
        if (response.isFilteredResponse) {
            return allHotelsBox
        }

        val isInsideSearchArea = allHotelsBox.contains(currentLocationLatLng)
        val closestHotelWithNeighborhood: Hotel? = sortedHotels.firstOrNull { it.locationId != null }
        val searchNeighborhood: HotelSearchResponse.Neighborhood? = response.allNeighborhoodsInSearchRegion.filter { it.id == searchRegionId }.firstOrNull()

        //If neighborhood search, zoom to that neighborhood
        if (searchNeighborhood != null && searchNeighborhood.hotels.isNotEmpty()) {
            return boxHotels(searchNeighborhood.hotels)
            //if current location is not within the search results, zoom to most "Dense" neighborhood
        } else if (!isInsideSearchArea) {
            val mostInterestingNeighborhood: HotelSearchResponse.Neighborhood? = response.allNeighborhoodsInSearchRegion.sortedByDescending { it.score }.firstOrNull()

            if (mostInterestingNeighborhood != null && mostInterestingNeighborhood.hotels != null && mostInterestingNeighborhood.hotels.isNotEmpty()) {
                return boxHotels(mostInterestingNeighborhood.hotels)
            }

            return allHotelsBox
            //If current location is near a neighborhood, zoom to closest neighborhood
        } else if (closestHotelWithNeighborhood != null) {
            val closestNieghborhood = response.neighborhoodsMap.get(closestHotelWithNeighborhood.locationId)
            if (closestNieghborhood == null) {
                return allHotelsBox
            } else {
                return boxHotels(closestNieghborhood.hotels)
            }
            //Default, zoom out and show all hotels
        } else {
            return allHotelsBox
        }
    }

    fun sortByLocation(location: Location, hotels: List<Hotel>): List<Hotel> {
        val hotelLocation = Location("other")
        val sortedHotels = hotels.sortedBy { h ->
            hotelLocation.latitude = h.latitude
            hotelLocation.longitude = h.longitude
            location.distanceTo(hotelLocation)
        }
        return sortedHotels
    }

    fun boxHotels(hotels: List<Hotel>): LatLngBounds {
        val box = LatLngBounds.Builder()
        for (hotel in hotels) {
            box.include(LatLng(hotel.latitude, hotel.longitude))
        }

        return box.build()
    }


}
