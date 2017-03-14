package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.utils.bindView

class ServerNeighborhoodFilterView(context: Context, attrs: AttributeSet?) : BaseNeighborhoodFilterView(context, attrs) {
    private val neighborhoodGroup: RadioGroup by bindView(R.id.neighborhood_group)

    init {
        View.inflate(context, R.layout.server_neigborhood_filter_view, this)

        moreLessView.setOnClickListener {
            showMoreLessClick(neighborhoodGroup)
        }
    }

    override fun updateNeighborhoods(list: List<HotelSearchResponse.Neighborhood>) {
        super.updateNeighborhoods(list)
        neighborhoodGroup.removeAllViews()

        if (list.size > 1) {
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
            neighborhoodGroup.post {
                //http://stackoverflow.com/questions/3602026/linearlayout-height-in-oncreate-is-0/3602144#3602144
                collapse(neighborhoodGroup)
            }
        }
    }

    override fun clear() {
        neighborhoodGroup.clearCheck()
    }
}