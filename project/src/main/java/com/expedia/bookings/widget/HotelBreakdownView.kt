package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelBreakDownViewModel

public class HotelBreakDownView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.breakdown_container)

    var viewmodel: HotelBreakDownViewModel by notNullAndObservable { vm ->
        vm.addRows.subscribe {
            linearLayout.removeAllViews()
            for (pair in it) {
                linearLayout.addView(createRow(pair))
            }
        }
    }

    init {
        View.inflate(getContext(), R.layout.hotel_breakdown, this)
    }

    private fun createRow(pair: Pair<String, String>): View {
        val row = LayoutInflater.from(getContext()).inflate(R.layout.hotel_cost_summary_row, null)
        val priceDescription = row.findViewById(R.id.price_type_text_view) as TextView
        val priceValue = row.findViewById(R.id.price_text_view) as TextView
        priceDescription.setText(pair.first)
        priceValue.setText(pair.second)
        return row
    }

}