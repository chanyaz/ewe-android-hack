package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.FlightType
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLOB
import com.expedia.bookings.itin.tripstore.extensions.HasProducts
import com.expedia.bookings.itin.tripstore.extensions.isMultiItemCheckout
import com.expedia.bookings.itin.tripstore.extensions.isPackage
import io.reactivex.subjects.PublishSubject

class HotelItinPricingBundleDescriptionViewModel<out S>(val scope: S) : IHotelItinPricingBundleDescriptionViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {
    var itinObserver: LiveDataObserver<Itin>

    override val bundleContainerResetSubject: PublishSubject<Unit> = PublishSubject.create()
    override val bundleProductDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val bundleContainerViewVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()

    init {
        itinObserver = LiveDataObserver { itin ->
            itin ?: return@LiveDataObserver

            when {
                itin.isPackage() -> {
                    bundleContainerViewVisibilitySubject.onNext(true)
                    bundleContainerResetSubject.onNext(Unit)
                    itin.packages?.firstOrNull()?.let {
                        //bundle title
                        setBundleContentsLabel(getProductsDescriptionString(it))
                    }
                }
                itin.isMultiItemCheckout() -> {
                    //bundle title
                    bundleContainerViewVisibilitySubject.onNext(true)
                    bundleContainerResetSubject.onNext(Unit)
                    setBundleContentsLabel(getProductsDescriptionString(itin))
                }
                else -> {
                    //standalone hotel
                    bundleContainerViewVisibilitySubject.onNext(false)
                }
            }
        }
        scope.itinHotelRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }

    private fun setBundleContentsLabel(bundleProductList: MutableList<String>) {
        bundleProductDescriptionSubject.onNext(scope.strings.fetch(R.string.itin_hotel_details_price_summary_bundle_description))
        bundleProductList.forEach {
            bundleProductDescriptionSubject.onNext(it)
        }
    }

    fun getProductsDescriptionString(productsContainer: HasProducts): MutableList<String> {
        val listOfProductStrings = mutableListOf<String>()
        productsContainer.listOfTripProducts().forEach {
            when (it) {
                TripProducts.HOTEL -> listOfProductStrings.addAll(buildLobStrings(productsContainer.hotels, scope.strings.fetch(R.string.Hotel)))
                TripProducts.FLIGHT -> listOfProductStrings.addAll(buildFlightStrings(productsContainer))
                TripProducts.CAR -> listOfProductStrings.addAll(buildLobStrings(productsContainer.cars, scope.strings.fetch(R.string.Car)))
                TripProducts.ACTIVITY -> listOfProductStrings.addAll(buildLobStrings(productsContainer.activities, scope.strings.fetch(R.string.Activity)))
                TripProducts.RAIL -> listOfProductStrings.addAll(buildLobStrings(productsContainer.rails, scope.strings.fetch(R.string.Rail)))
                TripProducts.CRUISE -> listOfProductStrings.addAll(buildLobStrings(productsContainer.cruises, scope.strings.fetch(R.string.Cruise)))
            }
        }
        return listOfProductStrings
    }

    private fun buildBundleProductString(product: String): String {
        return scope.strings.fetchWithPhrase(R.string.itin_hotel_details_price_summary_bundle_product_TEMPLATE,
                mapOf("product" to product))
    }

    private fun buildLobStrings(itinLobList: List<ItinLOB>?, description: String): List<String> {
        val list = mutableListOf<String>()
        itinLobList?.forEach {
            list.add(buildBundleProductString(description))
        }
        return list
    }

    private fun buildFlightStrings(productsContainer: HasProducts): List<String> {
        val flightStrings = mutableListOf<String>()
        productsContainer.flights?.forEach {
            val flightType = it.flightType
            if (flightType != null) {
                when (flightType) {
                    FlightType.ROUND_TRIP -> {
                        val flightString: String = buildBundleProductString(scope.strings.fetch(R.string.itin_flight_type_roundtrip))
                        flightStrings.add(flightString)
                    }
                    FlightType.ONE_WAY -> {
                        val flightString: String = buildBundleProductString(scope.strings.fetch(R.string.itin_flight_type_one_way))
                        flightStrings.add(flightString)
                    }
                    FlightType.MULTI_DESTINATION -> {
                        val flightString: String = buildBundleProductString(scope.strings.fetch(R.string.itin_flight_type_multi_destination))
                        flightStrings.add(flightString)
                    }
                }
            } else {
                val flightString: String = buildBundleProductString(scope.strings.fetch(R.string.Flight))
                flightStrings.add(flightString)
            }
        }
        return flightStrings
    }
}
