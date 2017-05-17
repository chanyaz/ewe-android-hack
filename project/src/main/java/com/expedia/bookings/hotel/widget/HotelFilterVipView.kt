package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject

class HotelFilterVipView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    val vipCheckedSubject = PublishSubject.create<Boolean>()
    private val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)

    init {
        View.inflate(context, R.layout.hotel_filter_vip_view, this)

        setOnClickListener {
            filterHotelVip.isChecked = !filterHotelVip.isChecked
            vipCheckedSubject.onNext(filterHotelVip.isChecked)
        }
    }

    fun reset() {
        filterHotelVip.isChecked = false
    }
}