package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.utils.bindView

class ServerNeighborhoodFilterView(context: Context, attrs: AttributeSet?) : BaseNeighborhoodFilterView(context, attrs) {
    private val neighborhoodGroup: RadioGroup by bindView(R.id.neighborhood_group)

    init {
        View.inflate(context, R.layout.server_neigborhood_filter_view, this)

        moreLessView.setOnClickListener {
            showMoreLessClick()
        }
    }

    override fun updateNeighborhoods(list: List<Neighborhood>) {
        super.updateNeighborhoods(list)
        neighborhoodGroup.removeAllViews()

        if (list.size > 0) {
            for (i in 0..list.size - 1) {
                val neighborhoodView = LayoutInflater.from(context)
                        .inflate(R.layout.hotel_neighbor_filter_radio_button, neighborhoodGroup, false) as RadioButton

                val neighborhood = list[i]
                neighborhoodView.text = neighborhood.name
                neighborhoodView.setOnClickListener {
                    neighborhoodOnSubject.onNext(neighborhood)
                }
                neighborhoodGroup.addView(neighborhoodView)
            }
        }
    }

    override fun getNeighborhoodContainer(): LinearLayout {
        return neighborhoodGroup
    }

    override fun clear() {
        neighborhoodGroup.clearCheck()
    }
}
