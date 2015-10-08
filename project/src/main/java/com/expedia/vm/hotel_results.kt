package com.expedia.vm

import android.content.Context
import android.content.res.Resources
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.AdImpressionTracking
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.util.endlessObserver
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.squareup.phrase.Phrase
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

public class HotelResultsViewModel(private val context: Context, private val hotelServices: HotelServices) {

    // Inputs
    val paramsSubject = BehaviorSubject.create<HotelSearchParams>()
    val locationParamsSubject = PublishSubject.create<SuggestionV4>()

    // Outputs
    private val hotelDownloadsObservable = PublishSubject.create<Observable<HotelSearchResponse>>()
    private val hotelDownloadResultsObservable = Observable.concat(hotelDownloadsObservable)
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val mapResultsObservable = PublishSubject.create<HotelSearchResponse>()
    val errorObservable = PublishSubject.create<ApiError>()

    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()

    init {
        paramsSubject.subscribe(endlessObserver { params ->
            doSearch(params)
        })

        locationParamsSubject.subscribe(endlessObserver { suggestion ->
            val cachedParams: HotelSearchParams? = paramsSubject.value
            val params = HotelSearchParams.Builder()
                    .suggestion(suggestion)
                    .checkIn(cachedParams?.checkIn)
                    .checkOut(cachedParams?.checkOut)
                    .adults(cachedParams?.adults!!)
                    .children(cachedParams?.children!!)
                    .build()
            doSearch(params)
        })

        hotelDownloadResultsObservable.subscribe {
            if (it.hasErrors()) {
                errorObservable.onNext(it.firstError)
            } else if (it.hotelList.isEmpty()) {
                val error = ApiError(ApiError.Code.HOTEL_SEARCH_NO_RESULTS)
                errorObservable.onNext(error)
            } else if (titleSubject.value == context.getString(R.string.visible_map_area)){
                mapResultsObservable.onNext(it)
            } else {
                hotelResultsObservable.onNext(it)
            }
        }

        hotelResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }

        mapResultsObservable.subscribe {
            AdImpressionTracking.trackAdClickOrImpression(context, it.pageViewBeaconPixelUrl, null)
        }
    }

    private fun doSearch(params: HotelSearchParams) {
        titleSubject.onNext(params.suggestion?.regionNames?.shortName)

        subtitleSubject.onNext(Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", DateUtils.localDateToMMMd(params.checkIn))
                .put("enddate", DateUtils.localDateToMMMd(params.checkOut))
                .put("guests", StrUtils.formatGuestString(context, params.guests()))
                .format())

        hotelDownloadsObservable.onNext(hotelServices.regionSearch(params))
    }
}

public class HotelResultsPricingStructureHeaderViewModel(private val resources: Resources) {
    // Inputs
    val loadingStartedObserver = PublishSubject.create<Unit>()
    val resultsDeliveredObserver = PublishSubject.create<HotelSearchResponse>()

    // Outputs
    val pricingStructureHeaderObservable = BehaviorSubject.create<String>()

    init {
        loadingStartedObserver.subscribe {
            pricingStructureHeaderObservable.onNext(resources.getString(R.string.progress_searching_hotels_hundreds))
        }

        resultsDeliveredObserver.subscribe { response ->
            val hotelResultsCount = response.hotelList?.size() ?: 0
            val header =
                    when (response.userPriceType) {
                        HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_total_price_for_stay_TEMPLATE, hotelResultsCount, hotelResultsCount)
                        else -> resources.getQuantityString(R.plurals.hotel_results_pricing_header_prices_avg_per_night_TEMPLATE, hotelResultsCount, hotelResultsCount)
                    }

            pricingStructureHeaderObservable.onNext(header)
        }
    }
}

public class HotelMapViewModel(val currentLocation: Location) {

    var hotels: List<Hotel> by Delegates.notNull()
    var previousMarker : Marker? = null

    //inputs
    val hotelResultsSubject = PublishSubject.create<HotelSearchResponse>()
    val mapResultsSubject = PublishSubject.create<HotelSearchResponse>()
    val mapPinSelectSubject = PublishSubject.create<Marker>()
    val carouselSwipedObservable = PublishSubject.create<Marker>()
    val mapBoundsSubject = BehaviorSubject.create<LatLngBounds>()

    //outputs
    val hotelsObservable = PublishSubject.create<List<Hotel>>()
    val newBoundsObservable = PublishSubject.create<LatLngBounds>()
    val mapPinSelectObservable = PublishSubject.create<List<Hotel>>()
    val unselectedMarker = PublishSubject.create<Pair<Marker?, Hotel>>()
    val selectMarker = PublishSubject.create<Pair<Marker?, Hotel>>()

    init {
        hotelsObservable.subscribe {
            previousMarker = null
        }

        hotelResultsSubject.subscribe { response ->
            hotels = response.hotelList
            if (response.hotelList != null && response.hotelList.size() > 0) {
                newBoundsObservable.onNext(getMapBounds(response))
            }
            hotelsObservable.onNext(response.hotelList ?: emptyList())
        }

        mapResultsSubject.subscribe { response ->
            hotels = response.hotelList
            if (response.hotelList != null && response.hotelList.size() > 0 && !hasResultsWithinMap(response)) {
                newBoundsObservable.onNext(getMapBounds(response))
            }
            hotelsObservable.onNext(response.hotelList ?: emptyList())
        }

        mapPinSelectSubject.subscribe {
            val hotel = getHotelWithMarker(it)
            val hotelLocation = Location("selected")
            hotelLocation.latitude = hotel.latitude
            hotelLocation.longitude = hotel.longitude
            val sortedHotels = sortByLocation(hotelLocation, hotels)

            mapPinSelectObservable.onNext(sortedHotels)
        }

        carouselSwipedObservable.subscribe {
            if (previousMarker != null) unselectedMarker.onNext(Pair(previousMarker, getHotelWithMarker(previousMarker)))
            selectMarker.onNext(Pair(it, getHotelWithMarker(it)))
            previousMarker = it
        }
    }

    private fun getHotelWithMarker(marker : Marker?) : Hotel {
        val hotelId = marker?.title
        val hotel = hotels.filter { it.hotelId == hotelId }.first()
        return hotel
    }

    private fun getMapBounds(response: HotelSearchResponse): LatLngBounds {
        val currentLocationLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        val sortedHotels = sortByLocation(currentLocation , response.hotelList)

        val allHotelsBox: LatLngBounds = boxHotels(response.hotelList)
        val isInsideSearchArea = allHotelsBox.contains(currentLocationLatLng)
        val closestHotelWithNeighborhood: Hotel? = sortedHotels.firstOrNull { it.locationId != null }

        if (!isInsideSearchArea) {
            val mostInterestingNeighborhood: HotelSearchResponse.Neighborhood? = response.allNeighborhoodsInSearchRegion.sortedByDescending { it.score }.firstOrNull()

            if (mostInterestingNeighborhood != null && mostInterestingNeighborhood.hotels != null && mostInterestingNeighborhood.hotels.size() > 0) {
                return boxHotels(mostInterestingNeighborhood.hotels)
            }

            return allHotelsBox
        } else if (closestHotelWithNeighborhood == null) {
            return allHotelsBox
        } else {
            val closestNieghborhood = response.neighborhoodsMap.get(closestHotelWithNeighborhood.locationId)
            if (closestNieghborhood == null) {
                return allHotelsBox
            } else {
                return boxHotels(closestNieghborhood.hotels)
            }
        }
    }

    private fun hasResultsWithinMap(response: HotelSearchResponse): Boolean {
        val currentRegion = mapBoundsSubject.value
        response.hotelList.forEach { hotel ->
            if (currentRegion.contains(LatLng(hotel.latitude, hotel.longitude))) {
                return true
            }
        }
        return false
    }

    private fun sortByLocation(location: Location, hotels : List<Hotel>) : List<Hotel> {
        val hotelLocation = Location("other")
        val sortedHotels = hotels.sortedBy { h ->
            hotelLocation.latitude = h.latitude
            hotelLocation.longitude = h.longitude
            location.distanceTo(hotelLocation)
        }
        return sortedHotels
    }

    private fun boxHotels(hotels: List<Hotel>): LatLngBounds {
        val box = LatLngBounds.Builder()
        for (hotel in hotels) {
            box.include(LatLng(hotel.latitude, hotel.longitude))
        }

        return box.build()
    }


}
