package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.PriceRange
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterRangeSeekBar
import com.expedia.bookings.widget.HotelPriceRangeSeekBar
import io.reactivex.subjects.PublishSubject

interface OnHotelPriceFilterChangedListener {
    fun onHotelPriceFilterChanged(minPrice: Int, maxPrice: Int, doTracking: Boolean)
}

class HotelPriceFilterView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val newPriceRangeObservable = PublishSubject.create<PriceRange>()

    @VisibleForTesting
    val priceRangeMinText: TextView by bindView(R.id.price_range_min_text)
    @VisibleForTesting
    val priceRangeMaxText: TextView by bindView(R.id.price_range_max_text)
    @VisibleForTesting
    val priceRangeBar: HotelPriceRangeSeekBar by bindView(R.id.price_range_bar)

    private var cachedPriceRange: PriceRange? = null

    private var listener: OnHotelPriceFilterChangedListener? = null

    init {
        View.inflate(context, R.layout.hotel_price_range_seekbar, this)

        newPriceRangeObservable.subscribe { priceRange ->
            cachedPriceRange = priceRange

            priceRangeBar.currentA11yStartValue = priceRange.defaultMinPriceText
            priceRangeBar.currentA11yEndValue = priceRange.defaultMaxPriceText

            priceRangeBar.upperLimit = priceRange.notches
            priceRangeMinText.text = priceRange.defaultMinPriceText
            priceRangeMaxText.text = priceRange.defaultMaxPriceText

            priceRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                    val minPrice = priceRange.toPrice(minValue)
                    val maxPrice = priceRange.toPrice(maxValue)

                    updatePrice(priceRange, minPrice, maxPrice, true)
                }

                override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int, thumb: FilterRangeSeekBar.Thumb) {
                    val minPrice = priceRange.toPrice(minValue)
                    val maxPrice = priceRange.toPrice(maxValue)

                    priceRangeBar.currentA11yStartValue = priceRange.formatPrice(minPrice)
                    priceRangeBar.currentA11yEndValue = priceRange.formatPrice(maxPrice)

                    updatePrice(priceRange, minPrice, maxPrice, true)

                    announceForAccessibility(priceRangeBar.getAccessibilityText(thumb))
                }
            })
        }
    }

    fun setOnHotelPriceFilterChanged(listener: OnHotelPriceFilterChangedListener?) {
        this.listener = listener
    }

    fun setMinMaxPrice(minPrice: Int, maxPrice: Int) {
        cachedPriceRange?.let { priceRange ->
            val maxPriceToUse = if (maxPrice == 0) priceRange.maxPrice else maxPrice

            priceRangeBar.minValue = priceRange.toValue(minPrice)
            priceRangeBar.maxValue = priceRange.toValue(maxPriceToUse)
            updatePrice(priceRange, minPrice, maxPriceToUse, false)
        }
    }

    private fun updatePrice(priceRange: PriceRange, minPrice: Int, maxPrice: Int, doTracking: Boolean) {
        priceRangeMinText.text = priceRange.formatPrice(minPrice)
        priceRangeMaxText.text = priceRange.formatPrice(maxPrice)

        val minMaxPair = priceRange.getUpdatedPriceRange(minPrice, maxPrice)

        listener?.onHotelPriceFilterChanged(minMaxPair.first, minMaxPair.second, doTracking)
    }
}
