package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SlidingTabLayout

class HotelReviewsToolbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {

    val slidingTabLayout: SlidingTabLayout by bindView(R.id.tab_layout)

    init {
        View.inflate(context, R.layout.widget_hotel_reviews_toolbar, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        slidingTabLayout.setCustomTabView(R.layout.hotel_review_actionbar_tab_bg, R.id.tab_text)
        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(context, R.color.review_screen_tab_indicator))
        slidingTabLayout.setDistributeEvenly(true)
        slidingTabLayout.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                HotelV2Tracking().trackHotelV2ReviewsCategories(position)

            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
        })
    }

}
