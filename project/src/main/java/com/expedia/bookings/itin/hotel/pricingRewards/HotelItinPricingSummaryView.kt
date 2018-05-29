package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class HotelItinPricingSummaryView(context: Context?, attrs: AttributeSet?) : CardView(context, attrs) {
    val roomContainerView by bindView<LinearLayout>(R.id.hotel_itin_pricing_summary_room_container)
    val multipleGuestView by bindView<PriceSummaryItemView>(R.id.hotel_itin_pricing_summary_multiple_guest_view)
    val taxesAndFeesView by bindView<PriceSummaryItemView>(R.id.hotel_itin_pricing_summary_taxes_fees_view)
    val couponsView by bindView<PriceSummaryItemView>(R.id.hotel_itin_pricing_summary_coupons_view)
    val pointsView by bindView<PriceSummaryItemView>(R.id.hotel_itin_pricing_summary_points_view)
    val totalPriceView by bindView<PriceSummaryItemView>(R.id.hotel_itin_pricing_summary_total_price)
    val totalPricePosCurrencyView by bindView<PriceSummaryItemView>(R.id.hotel_itin_pricing_summary_total_price_pos_currency)
    val currencyDisclaimerView by bindView<TextView>(R.id.hotel_itin_pricing_summary_currency_disclaimer)
    val additionalPriceInfoButton by bindView<LinearLayout>(R.id.hotel_itin_pricing_summary_additional_pricing_info_button)

    var viewModel: IHotelItinPricingSummaryViewModel by notNullAndObservable {
        it.priceBreakdownContainerClearSubject.subscribe {
            roomContainerView.removeAllViews()
        }

        it.priceBreakdownContainerItemSubject.subscribe { item ->
            val view: PriceSummaryItemView = Ui.inflate(R.layout.hotel_itin_price_summary_item_view, roomContainerView, false)
            setupPriceLineItem(view, item)
            roomContainerView.addView(view)
        }

        it.multipleGuestItemSubject.subscribe { item ->
            setupPriceLineItem(multipleGuestView, item)
        }

        it.taxesAndFeesItemSubject.subscribe { item ->
            setupPriceLineItem(taxesAndFeesView, item)
        }

        it.couponsItemSubject.subscribe { item ->
            setupPriceLineItem(couponsView, item)
            if (context != null) {
                val contDesc = StringBuilder()
                contDesc.append(context.getString(R.string.itin_minus_price_cont_desc))
                        .append(item.priceString.removePrefix("-")).toString()
                couponsView.priceTextView.contentDescription = contDesc
            }
        }

        it.pointsItemSubject.subscribe { item ->
            setupPriceLineItem(pointsView, item)
            if (context != null) {
                val contDesc = StringBuilder()
                contDesc.append(context.getString(R.string.itin_minus_price_cont_desc))
                        .append(item.priceString.removePrefix("-")).toString()
                pointsView.priceTextView.contentDescription = contDesc
            }
        }

        it.currencyDisclaimerSubject.subscribe { text ->
            currencyDisclaimerView.visibility = View.VISIBLE
            currencyDisclaimerView.text = text
        }

        it.totalPriceItemSubject.subscribe { item ->
            setupPriceLineItem(totalPriceView, item)
        }

        it.totalPriceInPosCurrencyItemSubject.subscribe { item ->
            setupPriceLineItem(totalPricePosCurrencyView, item)
        }

        additionalPriceInfoButton.subscribeOnClick(it.additionalPricingInfoSubject)
    }

    private fun setupPriceLineItem(view: PriceSummaryItemView, item: HotelItinPriceLineItem) {
        view.visibility = View.VISIBLE

        val color = ContextCompat.getColor(context, item.colorRes)
        with(view.labelTextView) {
            text = item.labelString
            setTextColor(color)
            textSize = item.textSize
            FontCache.setTypeface(this, item.font)
        }
        with(view.priceTextView) {
            text = item.priceString
            setTextColor(color)
            textSize = item.textSize
            FontCache.setTypeface(this, item.font)
        }
    }
}

class PriceSummaryItemView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    val labelTextView: TextView by bindView(R.id.hotel_itin_line_item_view_label_text)
    val priceTextView: TextView by bindView(R.id.hotel_itin_line_item_view_price_text)
}
