package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.FareFamilyAmenityItemViewModel
import com.expedia.vm.flights.FareFamilyPrimaryAmenitiesWidgetViewModel

class FareFamilyPrimaryAmenitiesWidget(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val primaryAmenitiesContainer: LinearLayout by bindView(R.id.fare_family_primary_amenities_container)

    init {
        View.inflate(context, R.layout.fare_family_primary_amenities_widget, this)
    }

    var viewModel: FareFamilyPrimaryAmenitiesWidgetViewModel by notNullAndObservable { vm ->
        primaryAmenitiesContainer.removeAllViews()
        displayPrimaryAmenities()
    }

    fun displayPrimaryAmenities() {
        addPrimaryAmenityView(FareFamilyAmenityItemViewModel(
                FlightV2Utils.getBagsAmenityResource(context, viewModel.fareFamilyComponents, viewModel.currencyCode),
                context.resources.getString(R.string.amenity_checked_bags)))
        addPrimaryAmenityView(FareFamilyAmenityItemViewModel(
                FlightV2Utils.getCancellationAmenityResource(context, viewModel.fareFamilyComponents, viewModel.currencyCode),
                context.resources.getString(R.string.amenity_cancellation)))
        addPrimaryAmenityView(FareFamilyAmenityItemViewModel(
                FlightV2Utils.getSeatSelectionAmenityResource(context, viewModel.fareFamilyComponents, viewModel.currencyCode),
                context.resources.getString(R.string.amenity_seat_selection)))
    }

    fun addPrimaryAmenityView(viewModel: FareFamilyAmenityItemViewModel) {
        val amenityItemWidget = LayoutInflater.from(context).inflate(R.layout.fare_family_amenity_widget, null) as FareFamilyAmenityItemWidget
        amenityItemWidget.viewModel = viewModel
        primaryAmenitiesContainer.addView(amenityItemWidget)
    }
}