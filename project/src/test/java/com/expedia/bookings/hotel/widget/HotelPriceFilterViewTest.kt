package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.view.MotionEvent
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.PriceRange
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelPriceFilterViewTest {

    private var priceFilterView: HotelPriceFilterView by Delegates.notNull()
    private val priceRange = PriceRange("USD", 0, 300)
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelPriceFilterChangedListener {
        var minPrice = 0
        var maxPrice = 0
        var doTracking = false

        override fun onHotelPriceFilterChanged(minPrice: Int, maxPrice: Int, doTracking: Boolean) {
            this.minPrice = minPrice
            this.maxPrice = maxPrice
            this.doTracking = doTracking
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        priceFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_price_filter_view_test, null) as HotelPriceFilterView

        priceFilterView.setOnHotelPriceFilterChanged(listener)
    }

    @Test
    fun testSetMinMaxPrice() {
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val minPrice = 10
        val maxPrice = 20
        priceFilterView.setMinMaxPrice(minPrice, maxPrice)

        assertEquals(priceRange.toValue(minPrice), priceFilterView.priceRangeBar.minValue)
        assertEquals(priceRange.toValue(maxPrice), priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(maxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(maxPrice, listener.maxPrice)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSetMinMaxPriceZeroMaxPrice() {
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val minPrice = 30
        val maxPrice = 0
        val expectedMaxPrice = priceRange.maxPrice
        priceFilterView.setMinMaxPrice(minPrice, maxPrice)

        assertEquals(priceRange.toValue(minPrice), priceFilterView.priceRangeBar.minValue)
        assertEquals(priceRange.toValue(expectedMaxPrice), priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(expectedMaxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(maxPrice, listener.maxPrice)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSetMinMaxPriceMaxPriceEqualsPriceRangeMaxPrice() {
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val minPrice = 30
        val maxPrice = 300
        val expectedMaxPrice = 0
        priceFilterView.setMinMaxPrice(minPrice, maxPrice)

        assertEquals(priceRange.toValue(minPrice), priceFilterView.priceRangeBar.minValue)
        assertEquals(priceRange.toValue(maxPrice), priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(maxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(expectedMaxPrice, listener.maxPrice)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSetMinMaxPriceMinPriceGreaterThanMaxPrice() {
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val minPrice = 50
        val maxPrice = 20
        priceFilterView.setMinMaxPrice(minPrice, maxPrice)

        assertEquals(priceRange.toValue(minPrice), priceFilterView.priceRangeBar.minValue)
        assertEquals(priceRange.toValue(minPrice) + 1, priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(maxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(maxPrice, listener.maxPrice)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSetMinMaxPriceNegativePrice() {
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val minPrice = -20
        val maxPrice = -5
        priceFilterView.setMinMaxPrice(minPrice, maxPrice)

        assertEquals(0, priceFilterView.priceRangeBar.minValue)
        assertEquals(1, priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(maxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(maxPrice, listener.maxPrice)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSetMinMaxPriceOutOfPriceRange() {
        val priceRange = PriceRange("USD", 1, 2)
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val minPrice = 0
        val maxPrice = 3
        priceFilterView.setMinMaxPrice(minPrice, maxPrice)

        assertEquals(0, priceFilterView.priceRangeBar.minValue)
        assertEquals(priceFilterView.priceRangeBar.upperLimit, priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(maxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(maxPrice, listener.maxPrice)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testOnRangeSeekBarValuesChanged() {
        priceFilterView.newPriceRangeObservable.onNext(priceRange)

        val press = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        priceFilterView.priceRangeBar.onTouchEvent(press)
        val release = MotionEvent.obtain(100, 100, MotionEvent.ACTION_UP, 0f, 0f, 0)
        priceFilterView.priceRangeBar.onTouchEvent(release)

        val minPrice = 0
        val maxPrice = 150
        assertEquals(priceRange.toValue(minPrice), priceFilterView.priceRangeBar.minValue)
        assertEquals(priceRange.toValue(maxPrice), priceFilterView.priceRangeBar.maxValue)

        assertEquals(priceRange.formatPrice(minPrice), priceFilterView.priceRangeMinText.text.toString())
        assertEquals(priceRange.formatPrice(maxPrice), priceFilterView.priceRangeMaxText.text.toString())

        assertEquals(minPrice, listener.minPrice)
        assertEquals(maxPrice, listener.maxPrice)
        assertTrue(listener.doTracking)
    }
}
