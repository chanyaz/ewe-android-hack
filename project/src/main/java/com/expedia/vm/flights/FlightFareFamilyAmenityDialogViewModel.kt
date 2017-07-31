package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.flights.FlightAmenityCategory
import rx.subjects.PublishSubject

class FlightFareFamilyAmenityDialogViewModel(val context: Context,
                                             val fareFamilyComponents: HashMap<String, HashMap<String, String>>,
                                             val currencyCode: String) {

    val fareFamilyNameSubject = PublishSubject.create<String>()
    val airlineNameSubject = PublishSubject.create<String>()
    val fareFamilyCabinClassNameSubject = PublishSubject.create<String>()

    fun prepareAmenityCategories(): HashMap<String, HashMap<String, String>> {
        val amenityFamilyMap = HashMap<String, HashMap<String, String>>()
        val notAvailableAmenityMap = HashMap<String, String>()
        val amenityCategories = FlightAmenityCategory.values()
        for (amenityCategory in amenityCategories) {
            val amenityKeyStr = context.resources.getString(amenityCategory.key)
            val amenities = fareFamilyComponents.get(amenityKeyStr)
            if (amenities != null && amenities.isNotEmpty()) {
                if (amenityCategory == FlightAmenityCategory.NOTOFFERED || amenityCategory == FlightAmenityCategory.UNKNOWN) {
                    notAvailableAmenityMap.putAll(amenities)
                } else {
                    amenityFamilyMap.put(amenityKeyStr, amenities)
                }
            }
        }
        amenityFamilyMap.put(context.resources.getString(FlightAmenityCategory.NOTOFFERED.key), notAvailableAmenityMap)
        return amenityFamilyMap
    }
}