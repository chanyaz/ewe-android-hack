package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.os.Build
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View

import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.SlidingTabLayout

import butterknife.ButterKnife
import butterknife.InjectView
import com.expedia.bookings.utils.bindView

public class HotelReviewsToolbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {

    val slidingTabLayout: SlidingTabLayout by bindView(R.id.tab_layout)

    init {
        View.inflate(context, R.layout.widget_hotel_reviews_toolbar, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        slidingTabLayout.setCustomTabView(R.layout.actionbar_tab_bg, R.id.tab_text)
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.action_bar_text_yellow))
        slidingTabLayout.setDistributeEvenly(true)
    }

}
