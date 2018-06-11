package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseCostSummaryBreakdownViewModel

class CostSummaryBreakDownView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        vm.addRows.subscribe {
            linearLayout.removeAllViews()
            for (breakdown in it) {
                linearLayout.addView(
                        if (breakdown.separator) createLine(breakdown.separatorColor)
                        else createRow(breakdown)
                )
            }
        }
        vm.priceSummaryContainerDescription.subscribe { it ->
            this.contentDescription = it
        }
    }

    init {
        View.inflate(context, R.layout.cost_summary_breakdown, this)
    }

    private fun createRow(breakdownRow: BaseCostSummaryBreakdownViewModel.CostSummaryBreakdownRow): View {
        val row = LayoutInflater.from(context).inflate(R.layout.material_cost_summary_row, linearLayout, false)
        val priceDescription = row.findViewById<TextView>(R.id.price_type_text_view)
        val priceValue = row.findViewById<TextView>(R.id.price_text_view)
        priceDescription.text = breakdownRow.title
        priceValue.text = breakdownRow.cost
        breakdownRow.titleColor?.let { color ->
            priceDescription.setTextColor(color)
        }
        breakdownRow.costColor?.let { color ->
            priceValue.setTextColor(color)
        }
        breakdownRow.titleTypeface?.let { typeface ->
            priceDescription.typeface = FontCache.getTypeface(typeface)
        }
        breakdownRow.costTypeface?.let { typeface ->
            priceValue.typeface = FontCache.getTypeface(typeface)
        }
        if (breakdownRow.strikeThrough) {
            priceValue.setPaintFlags(priceValue.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
        }
        breakdownRow.titleTextSize?.let { size ->
            priceDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }
        breakdownRow.costTextSize?.let { size ->
            priceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }
        return row
    }

    private fun createLine(color: Int?): View {
        val view = View(context)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.height = 1
        val paddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()
        val paddingSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
        lp.setMargins(paddingSide, paddingTop, paddingSide, paddingTop)
        view.layoutParams = lp
        view.setBackgroundColor(ContextCompat.getColor(context, color ?: R.color.cost_summary_breakdown_line_separator_color))
        return view
    }
}
