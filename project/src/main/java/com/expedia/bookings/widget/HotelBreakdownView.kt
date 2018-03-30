package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelBreakDownViewModel
import com.expedia.vm.HotelBreakDownViewModel.BreakdownItem
import com.squareup.phrase.Phrase

class HotelBreakDownView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: HotelBreakDownViewModel by notNullAndObservable { vm ->
        vm.addRows.subscribe {
            linearLayout.removeAllViews()
            for (breakdown in it) {
                when (breakdown.breakdownItem) {
                    BreakdownItem.TRIPTOTAL -> {
                        linearLayout.addView(createLine())
                        linearLayout.addView(createRow(breakdown, false))
                    }

                    BreakdownItem.DATE -> linearLayout.addView(createDateRow(breakdown))

                    BreakdownItem.DISCOUNT -> linearLayout.addView(createRow(breakdown, true))

                    BreakdownItem.OTHER -> linearLayout.addView(createRow(breakdown, false))

                    BreakdownItem.LOCALCURRENCY -> linearLayout.addView(createLocalCurrencyRow(breakdown))
                }
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.hotel_breakdown, this)
    }

    private fun createRow(breakdown: HotelBreakDownViewModel.Breakdown, isDiscount: Boolean): View {
        val row = LayoutInflater.from(context).inflate(R.layout.hotel_cost_summary_row, null)
        val priceDescription = row.findViewById<TextView>(R.id.price_type_text_view)
        val priceValue = row.findViewById<TextView>(R.id.price_text_view)
        priceDescription.text = breakdown.title
        if (isDiscount) {
            priceValue.text = "(" + breakdown.cost + ")"
            priceValue.setTextColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
        } else {
            priceValue.text = breakdown.cost
        }
        return row
    }

    private fun createLocalCurrencyRow(breakdown: HotelBreakDownViewModel.Breakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.hotel_local_currency_row, null)
        val localCurrencyDisplay = row.findViewById<TextView>(R.id.local_currency_display)
        localCurrencyDisplay.text = Phrase.from(context, R.string.local_currency_TEMPLATE)
                .put("cost", breakdown.cost)
                .format().toString()
        return row
    }

    private fun createDateRow(breakdown: HotelBreakDownViewModel.Breakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.hotel_cost_summary_date_row, null)
        val priceDescription = row.findViewById<TextView>(R.id.price_type_text_view)
        val priceValue = row.findViewById<TextView>(R.id.price_text_view)
        priceDescription.text = breakdown.title
        priceValue.text = breakdown.cost
        return row
    }

    private fun createLine(): View {
        val view = View(context, null)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.height = 1
        val paddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val paddingSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        lp.setMargins(paddingSide, paddingTop, paddingSide, paddingTop)
        view.layoutParams = lp
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.gray500))
        return view
    }
}
