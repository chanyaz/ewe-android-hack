package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class HotelItinPricingMicoView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val micoDescriptionContainerView by bindView<LinearLayout>(R.id.hotel_itin_mico_description_container)

    var viewModel: IHotelItinPricingMicoDescriptionViewModel by notNullAndObservable {
        it.micoContainerResetSubject.subscribe {
            micoDescriptionContainerView.removeAllViews()
        }
        it.micoProductDescriptionSubject.subscribe {
            val view: MicoPriceDescriptionItemView = Ui.inflate(R.layout.hotel_itin_price_mico_item_view, micoDescriptionContainerView, false)
            setUpMicoProductLineItem(view, it)
            micoDescriptionContainerView.addView(view)
        }
    }

    private fun setUpMicoProductLineItem(view: MicoPriceDescriptionItemView, item: HotelItinMicoItem) {
        view.visibility = View.VISIBLE

        with(view.labelTextView) {
            text = item.labelString
        }
    }
}

class MicoPriceDescriptionItemView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    val labelTextView: TextView by bindView(R.id.hotel_itin_mico_line_item_view_text)
}
