package com.expedia.bookings.itin.lx.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.itin.common.GoogleMapsLiteMapView
import com.expedia.bookings.itin.common.GoogleMapsLiteViewModel
import com.expedia.bookings.itin.common.ItinMapWidgetViewModel
import com.expedia.bookings.itin.scopes.HasMapWidgetViewModelSetter
import com.expedia.bookings.itin.scopes.LxItinMapWidgetViewModelScope
import com.expedia.bookings.itin.scopes.MapWidgetViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class LxItinMapWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), MapWidgetViewModelSetter {
    override fun setUpViewModel(vm: LxItinMapWidgetViewModel<LxItinMapWidgetViewModelScope>) {
        viewModel = vm
    }

    val map: GoogleMapsLiteMapView by bindView(R.id.widget_itin_map)
    val directionsButton: ImageView by bindView(R.id.directions_button)
    val addressContainer: LinearLayout by bindView(R.id.widget_itin_address)
    val addressLineFirst: TextView by bindView(R.id.widget_itin_address_line_1)
    val addressLineSecond: TextView by bindView(R.id.widget_itin_address_line_2)

    var viewModel: ItinMapWidgetViewModel by notNullAndObservable { vm ->
        vm.directionsButtonVisibilitySubject.subscribeVisibility(directionsButton)
        vm.addressLineFirstSubject.subscribeTextAndVisibility(addressLineFirst)
        vm.addressLineSecondSubject.subscribeTextAndVisibility(addressLineSecond)
        map.subscribeOnClick(vm.mapClickSubject)
        directionsButton.subscribeOnClick(vm.directionButtonClickSubject)
        vm.latLongSubject.subscribe {
            map.setViewModel(GoogleMapsLiteViewModel(listOf(it)))
        }
        vm.directionsButtonVisibilitySubject.subscribeVisibility(directionsButton)
        directionsButton.subscribeOnClick(vm.directionButtonClickSubject)
        map.subscribeOnClick(vm.mapClickSubject)
    }

    init {
        View.inflate(context, R.layout.widget_lx_itin_map, this)
    }
}