package com.expedia.bookings.itin.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.itin.scopes.MapWidgetViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinMapWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), MapWidgetViewModelSetter {
    override fun setUpViewModel(vm: ItinMapWidgetViewModel) {
        viewModel = vm
    }

    val map: GoogleMapsLiteMapView by bindView(R.id.widget_itin_map)
    val directionsButton: ImageView by bindView(R.id.directions_button)
    val addressLineFirst: TextView by bindView(R.id.widget_itin_address_line_1)
    val addressLineSecond: TextView by bindView(R.id.widget_itin_address_line_2)
    val addressContainer: LinearLayout by bindView(R.id.address_container)

    var viewModel: ItinMapWidgetViewModel by notNullAndObservable { vm ->
        vm.addressLineFirstSubject.subscribeTextAndVisibility(addressLineFirst)
        vm.addressLineSecondSubject.subscribeTextAndVisibility(addressLineSecond)
        vm.addressContainerContentDescription.subscribeContentDescription(addressContainer)
        vm.latLongSubject.subscribe {
            map.setViewModel(GoogleMapsLiteViewModel(listOf(it)))
        }
        directionsButton.subscribeOnClick(vm.directionButtonClickSubject)
        map.subscribeOnClick(vm.mapClickSubject)
        addressContainer.subscribeOnClick(vm.addressClickSubject)
    }

    init {
        View.inflate(context, R.layout.widget_lx_itin_map, this)
    }
}
