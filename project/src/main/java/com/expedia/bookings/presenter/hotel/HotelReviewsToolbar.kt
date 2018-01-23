package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class HotelReviewsTabbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {

    val slidingTabLayout: TabLayout by bindView(R.id.tab_layout)

    init {
        View.inflate(context, R.layout.widget_hotel_reviews_toolbar, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }
}
