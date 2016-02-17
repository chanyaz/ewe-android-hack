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
import com.expedia.vm.Breakdown
import com.expedia.vm.HotelBreakDownViewModel

public class HotelBreakDownView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: HotelBreakDownViewModel by notNullAndObservable { vm ->
        vm.addRows.subscribe {
            linearLayout.removeAllViews()
            for (breakdown in it) {
                if (it.indexOf(breakdown) == it.size - 1) {
                    linearLayout.addView(createLine())
                }
                if (breakdown.isDate) {
                    linearLayout.addView(createDateRow(breakdown))
                } else {
                    linearLayout.addView(createRow(breakdown, breakdown.isDiscount))
                }

            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.hotel_breakdown, this)
    }

    private fun createRow(breakdown: Breakdown, isDiscount: Boolean): View {
        val row = LayoutInflater.from(context).inflate(R.layout.hotel_cost_summary_row, null)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        priceDescription.text = breakdown.title
        if (isDiscount) {
            priceValue.text = "(" + breakdown.cost + ")"
            priceValue.setTextColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
        }
        else {
            priceValue.text = breakdown.cost
        }
        return row
    }

    private fun createDateRow(breakdown: Breakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.hotel_cost_summary_date_row, null)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        priceDescription.text = breakdown.title
        priceValue.text = breakdown.cost
        return row
    }

    private fun createLine(): View {
        val view = View(context, null);
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.height = 1
        var paddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        var paddingSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        lp.setMargins(paddingSide, paddingTop, paddingSide, paddingTop)
        view.layoutParams = lp
        view.setBackgroundColor(Color.parseColor("#979797"))
        return view
    }
}