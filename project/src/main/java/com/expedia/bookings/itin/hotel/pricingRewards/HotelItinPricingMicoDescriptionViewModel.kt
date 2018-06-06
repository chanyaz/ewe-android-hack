package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.HasProducts
import com.expedia.bookings.itin.tripstore.extensions.isMultiItemCheckout
import com.expedia.bookings.itin.tripstore.extensions.isPackage
import io.reactivex.subjects.PublishSubject

class HotelItinPricingMicoDescriptionViewModel<out S>(val scope: S) : IHotelItinPricingMicoDescriptionViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {
    var itinObserver: LiveDataObserver<Itin>

    override val micoContainerResetSubject: PublishSubject<Unit> = PublishSubject.create()
    override val micoProductDescriptionSubject: PublishSubject<HotelItinMicoItem> = PublishSubject.create()

    init {
        itinObserver = LiveDataObserver { itin ->
            itin ?: return@LiveDataObserver

            when {
                itin.isPackage() -> {
                    micoContainerResetSubject.onNext(Unit)
                    itin.packages?.firstOrNull()?.let {
                        //bundle title
                        setBundleContentsLabel(getProductsDescriptionString(it))
                    }
                }
                itin.isMultiItemCheckout() -> {
                    //bundle title
                    micoContainerResetSubject.onNext(Unit)
                    setBundleContentsLabel(getProductsDescriptionString(itin))
                }
                else -> {
                    //standalone hotel
                    micoContainerResetSubject.onNext(Unit)
                }
            }
        }
    }

    private fun setBundleContentsLabel(micoProductList: MutableList<String>) {
        micoProductDescriptionSubject.onNext(HotelItinMicoItem(scope.strings.fetch(R.string.itin_hotel_details_price_summary_mico_description)))
        micoProductList.forEach {
            val packageContentsItem = HotelItinMicoItem(it)
            micoProductDescriptionSubject.onNext(packageContentsItem)
        }
    }

    fun getProductsDescriptionString(productsContainer: HasProducts): MutableList<String> {
        val listOfProductStrings = mutableListOf<String>()
        val listOfMicoProductStrings = mutableListOf<String>()
        productsContainer.listOfTripProducts().forEach {
            when (it) {
                TripProducts.HOTEL -> listOfProductStrings.add(scope.strings.fetch(R.string.Hotel))
                TripProducts.FLIGHT -> listOfProductStrings.add(scope.strings.fetch(R.string.Flight))
                TripProducts.CAR -> listOfProductStrings.add(scope.strings.fetch(R.string.Car))
                TripProducts.ACTIVITY -> listOfProductStrings.add(scope.strings.fetch(R.string.Activity))
                TripProducts.RAIL -> listOfProductStrings.add(scope.strings.fetch(R.string.Rail))
                TripProducts.CRUISE -> listOfProductStrings.add(scope.strings.fetch(R.string.Cruise))
            }
        }
            listOfProductStrings.forEach {
                listOfMicoProductStrings.add(buildMicoProductString(it))
            }
        return listOfMicoProductStrings
        }

    fun buildMicoProductString(product: String): String {
        return scope.strings.fetchWithPhrase(R.string.itin_hotel_details_price_summary_mico_product_TEMPLATE,
                mapOf("product" to product))
    }
}

data class HotelItinMicoItem(val labelString: String)
