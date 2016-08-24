package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.rail.RailSearchViewModel
import rx.Observable

class RailSearchLocationWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val originLocationText: SearchInputTextView by bindView(R.id.origin_card)
    val destinationLocationText: SearchInputTextView by bindView(R.id.destination_card)
    val swapLocationsButton: View by bindView(R.id.swapLocationsButton)

    init {
        View.inflate(context, R.layout.widget_rail_locations, this)

        swapLocationsButton.setOnClickListener {
            viewModel.swapLocations()
        }
        swapLocationsButton.isEnabled = false
    }

    var viewModel: RailSearchViewModel by notNullAndObservable { vm ->
        vm.formattedOriginObservable.subscribeText(originLocationText)
        vm.formattedDestinationObservable.subscribeText(destinationLocationText)

        vm.railErrorNoLocationsObservable.subscribe {
            AnimUtils.doTheHarlemShake(this)
        }

        Observable.combineLatest(
                vm.formattedOriginObservable,
                vm.formattedDestinationObservable,
                { origin, destination ->
                    if (origin.isNullOrBlank() || destination.isNullOrBlank()) {
                        swapLocationsButton.isEnabled = false
                    } else {
                        swapLocationsButton.isEnabled = true
                    }
                }).subscribe()

    }
}