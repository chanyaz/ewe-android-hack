package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
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
        view.setBackgroundColor(Color.parseColor("#979797"))
        return view
    }
}
