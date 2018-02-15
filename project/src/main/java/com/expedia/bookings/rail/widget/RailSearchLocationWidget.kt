package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailSearchViewModel
import com.squareup.phrase.Phrase

class RailSearchLocationWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val originLocationText: SearchInputTextView by bindView(R.id.origin_card)
    val destinationLocationText: SearchInputTextView by bindView(R.id.destination_card)
    val swapLocationsButton: ImageView by bindView(R.id.swapLocationsButton)

    init {
        View.inflate(context, R.layout.widget_rail_locations, this)

        swapLocationsButton.setOnClickListener {
            viewModel.swapLocations()
        }
        swapLocationsButton.isEnabled = false
        swapLocationsButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray200))
    }

    var viewModel: RailSearchViewModel by notNullAndObservable { vm ->
        vm.formattedOriginObservable.subscribe { formattedOrigin ->
            originLocationText.text = formattedOrigin
            originLocationText.contentDescription = Phrase.from(context, R.string.rail_going_from_location_cont_desc_TEMPLATE)
                    .put("location", formattedOrigin)
                    .format().toString()
        }
        vm.formattedDestinationObservable.subscribe { formattedDestination ->
            destinationLocationText.text = formattedDestination
            destinationLocationText.contentDescription = Phrase.from(context, R.string.rail_going_to_location_cont_desc_TEMPLATE)
                    .put("location", formattedDestination)
                    .format().toString()
        }
        ObservableOld.combineLatest(
                vm.formattedOriginObservable,
                vm.formattedDestinationObservable,
                { origin, destination ->
                    if (origin.isNullOrBlank() || destination.isNullOrBlank()) {
                        swapLocationsButton.isEnabled = false
                        swapLocationsButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray200))
                    } else {
                        swapLocationsButton.isEnabled = true
                        swapLocationsButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray700))
                    }
                }).subscribe()
    }
}
