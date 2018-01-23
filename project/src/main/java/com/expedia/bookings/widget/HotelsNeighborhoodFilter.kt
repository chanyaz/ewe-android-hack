package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.hotels.HotelSearchResponse.Neighborhood
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeOnClick
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject

class HotelsNeighborhoodFilter(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val neighborhoodSelectedSubject = PublishSubject.create<HotelSearchResponse.Neighborhood>()
    val neighborhoodDeselectedSubject = PublishSubject.create<HotelSearchResponse.Neighborhood>()

    private lateinit var neighborhood: Neighborhood

    private val neighborhoodName: TextView by bindView(R.id.neighborhood_name)
    private val neighborhoodCheckBox: CheckBox by bindView(R.id.neighborhood_check_box)

    private val checkObserver: Observer<Unit> = endlessObserver {
        neighborhoodCheckBox.isChecked = !neighborhoodCheckBox.isChecked
        neighborhoodSelectedSubject.onNext(neighborhood)
        if (neighborhoodCheckBox.isChecked) {
            neighborhoodSelectedSubject.onNext(neighborhood)
        } else {
            neighborhoodDeselectedSubject.onNext(neighborhood)
        }
        refreshContentDescription()
    }

    fun clear() {
        neighborhoodCheckBox.isChecked = false
        neighborhoodDeselectedSubject.onNext(neighborhood)
        refreshContentDescription()
    }

    fun bind(neighborhood: Neighborhood) {
        this.neighborhood = neighborhood
        neighborhoodName.text = neighborhood.name
        neighborhoodCheckBox.isChecked = false

        subscribeOnClick(checkObserver)
        refreshContentDescription()
    }

    private fun refreshContentDescription() {
        val contentDesc = StringBuilder(neighborhoodName.text)
        contentDesc.append(". ")
        if (neighborhoodCheckBox.isChecked) {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_checked))
        } else {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_unchecked))
        }
        this.contentDescription = contentDesc
    }
}
