package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

interface OnHotelVipFilterChangedListener {
    fun onHotelVipFilterChanged(vipChecked: Boolean, doTracking: Boolean)
}

class HotelFilterVipView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    @VisibleForTesting
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)

    private var listener: OnHotelVipFilterChangedListener? = null

    init {
        View.inflate(context, R.layout.hotel_filter_vip_view, this)

        setOnClickListener {
            filterHotelVip.isChecked = !filterHotelVip.isChecked
            listener?.onHotelVipFilterChanged(filterHotelVip.isChecked, true)
        }
    }

    fun setOnHotelVipFilterChangedListener(listener: OnHotelVipFilterChangedListener?) {
        this.listener = listener
    }

    fun reset() {
        filterHotelVip.isChecked = false
    }

    fun update(checked: Boolean) {
        filterHotelVip.isChecked = checked
        listener?.onHotelVipFilterChanged(filterHotelVip.isChecked, false)
    }
}
