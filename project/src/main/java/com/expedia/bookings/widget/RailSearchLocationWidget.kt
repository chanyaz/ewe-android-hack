package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailSearchViewModel

class RailSearchLocationWidget(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val originLocationText: TextView by bindView(R.id.originLocationView)
    val destinationLocationText: TextView by bindView(R.id.destinationLocationView)
    val swapLocationsButton: View by bindView(R.id.swapLocationsButton)

    init {
        View.inflate(context, R.layout.widget_rail_locations, this)
        swapLocationsButton.setOnClickListener {
            viewModel.swapLocations()
        }
    }

    var viewModel: RailSearchViewModel by notNullAndObservable {
        it.railOriginObservable.subscribe {
            this.originLocationText.text = it.regionNames.displayName
        }
        it.railDestinationObservable.subscribe({
            this.destinationLocationText.text = it.regionNames.displayName
        })
        it.railErrorNoLocationsObservable.subscribe {
            AnimUtils.doTheHarlemShake(this)
        }
    }
}