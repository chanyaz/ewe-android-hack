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
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseCostSummaryBreakdownViewModel

class CostSummaryBreakDownView(context: Context,  attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        vm.addRows.subscribe {
            linearLayout.removeAllViews()
            for (breakdown in it) {
                if (breakdown.isDiscount) {
                    linearLayout.addView(createDiscountRow(breakdown))
                } else if (breakdown.isTotalCost) {
                    linearLayout.addView(createTotalCostRow(breakdown))
                } else if (breakdown.isTotalDue) {
                    linearLayout.addView(createTotalDueRow(breakdown))
                } else if (breakdown.isLine) {
                    linearLayout.addView(createLine())
                } else {
                    linearLayout.addView(createRow(breakdown))
                }
            }
        }
    }

    init {
        View.inflate(context, R.layout.cost_summary_breakdown, this)
      }

    private fun createRow(breakdown: BaseCostSummaryBreakdownViewModel.CostSummaryBreakdown): View {
        val row = LayoutInflater.from(context).inflate(R.layout.material_cost_summary_row, null)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        priceDescription.text = breakdown.title
        priceValue.text = breakdown.cost
        if (breakdown.color != null) {
            priceDescription.setTextColor(breakdown.color)
            priceValue.setTextColor(breakdown.color)
        }
        return row
    }

    private fun createDiscountRow(breakdown: BaseCostSummaryBreakdownViewModel.CostSummaryBreakdown): View {
        val row = createRow(breakdown)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        val textColor = ContextCompat.getColor(context, R.color.cost_summary_breakdown_savings_cost_color)
        priceDescription.setTextColor(textColor)
        priceValue.setTextColor(textColor)
        return row
    }

    private fun createTotalDueRow(breakdown: BaseCostSummaryBreakdownViewModel.CostSummaryBreakdown): View {
        val row = createRow(breakdown)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        priceDescription.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
        priceValue.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM)
        return row
    }

    private fun createTotalCostRow(breakdown: BaseCostSummaryBreakdownViewModel.CostSummaryBreakdown): View {
        val row = createRow(breakdown)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        val textColor = ContextCompat.getColor(context, R.color.cost_summary_breakdown_total_cost_color)
        priceDescription.setTextColor(textColor)
        priceValue.setTextColor(textColor)
        return row
    }

    private fun createLine(): View {
        val view = View(getContext(), null);
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.height = 1
        var paddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics()).toInt()
        var paddingSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics()).toInt()
        lp.setMargins(paddingSide, paddingTop, paddingSide, paddingTop)
        view.setLayoutParams(lp)
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.cost_summary_breakdown_line_separator_color))
        return view
    }
}