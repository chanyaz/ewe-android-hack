package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterRangeSeekBar
import com.expedia.bookings.widget.HotelPriceRangeSeekBar
import com.expedia.util.notNullAndObservable
import com.expedia.vm.hotel.BaseHotelFilterViewModel

class HotelPriceFilterView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val priceRangeMinText: TextView by bindView(R.id.price_range_min_text)
    private val priceRangeMaxText: TextView by bindView(R.id.price_range_max_text)
    private val priceRangeBar: HotelPriceRangeSeekBar by bindView(R.id.price_range_bar)

    var viewModel: BaseHotelFilterViewModel by notNullAndObservable { vm ->
        vm.newPriceRangeObservable.subscribe { priceRange ->
            priceRangeBar.setPriceRange(priceRange)
            priceRangeMinText.text = priceRange.defaultMinPriceText
            priceRangeMaxText.text = priceRange.defaultMaxPriceText

            priceRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    priceRangeMinText.text = priceRange.formatValue(minValue)
                    priceRangeMaxText.text = priceRange.formatValue(maxValue)
                }

                override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    priceRangeMinText.text = priceRange.formatValue(minValue)
                    priceRangeMaxText.text = priceRange.formatValue(maxValue)
                    vm.priceRangeChangedObserver.onNext(priceRange.getUpdatedPriceRange(minValue, maxValue))
                }
            })
        }
    }

    init {
        View.inflate(context, R.layout.hotel_price_range_seekbar, this)
    }
}