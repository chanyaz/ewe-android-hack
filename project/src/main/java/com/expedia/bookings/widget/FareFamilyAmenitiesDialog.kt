package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import android.widget.TextView
import com.expedia.bookings.data.flights.FlightAmenityCategory
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.flights.FareFamilyAmenityItemViewModel
import com.expedia.vm.flights.FareFamilyPrimaryAmenitiesWidgetViewModel
import com.expedia.vm.flights.FlightFareFamilyAmenityDialogViewModel

class FareFamilyAmenitiesDialog(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val fareFamilyNameText: TextView by bindView(R.id.fare_family_name)
    val airlineNameText: TextView by bindView(R.id.airline_name)
    val fareFamilyCabinClassNameText: TextView by bindView(R.id.fare_family_cabin_class_name)
    val amenitiesList: LinearLayout by bindView(R.id.amenities_list)
    val fareFamilyPrimaryAmenitiesWidget: FareFamilyPrimaryAmenitiesWidget by bindView(R.id.fare_family_primary_amenities_widget)

    var viewModel: FlightFareFamilyAmenityDialogViewModel by notNullAndObservable { vm ->
        vm.fareFamilyNameSubject.subscribeTextAndVisibility(fareFamilyNameText)
        vm.airlineNameSubject.subscribeTextAndVisibility(airlineNameText)
        vm.fareFamilyCabinClassNameSubject.subscribeTextAndVisibility(fareFamilyCabinClassNameText)
        fareFamilyPrimaryAmenitiesWidget.viewModel = FareFamilyPrimaryAmenitiesWidgetViewModel(context, vm.fareFamilyComponents)
    }

    init {
        View.inflate(context, R.layout.fare_family_amenities_view, this)
    }

    fun prepareAmenitiesListForDisplay() {
        val amenityFamilyMap = viewModel.prepareAmenityCategories()
        val amenityFamilies = FlightAmenityCategory.values()
        for (amenityFamily in amenityFamilies) {
            val amenityMap = amenityFamilyMap.get(context.resources.getString(amenityFamily.key))
            if (amenityMap != null && amenityMap.isNotEmpty()) {
                val labelTextView = LayoutInflater.from(context).inflate(R.layout.text_label, null) as TextView
                labelTextView.text = context.resources.getString(amenityFamily.dispStr)
                amenitiesList.addView(labelTextView)

                for (amenity in amenityMap) {
                    amenitiesList.addView(getPrimaryAmenityView(FareFamilyAmenityItemViewModel(
                            FlightV2Utils.getAmenityResourceType(context, amenity.key, amenity.value, viewModel.fareFamilyComponents), amenity.value)))
                }
            }
        }
    }

    fun getPrimaryAmenityView(viewModel: FareFamilyAmenityItemViewModel): View {
        val amenityItemWidget = LayoutInflater.from(context).inflate(R.layout.fare_family_amenity_widget, null) as FareFamilyAmenityItemWidget
        amenityItemWidget.viewModel = viewModel
        return amenityItemWidget
    }
}
