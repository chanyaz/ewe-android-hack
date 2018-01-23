package com.expedia.bookings.test

import android.app.Application
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.ValueAddsContainer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ValueAddsContainerTest {
    private lateinit var context: Application
    private var container: ValueAddsContainer by Delegates.notNull()

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        container = ValueAddsContainer(context, null)
    }

    @Test
    fun testValueAdds() {
        assertEquals(0, container.childCount)

        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        container.valueAddsSubject.onNext(valueAdds)
        assertEquals(0, container.childCount)

        val valueAdd1 = HotelOffersResponse.ValueAdds()
        val description1 = "Value Add"
        valueAdd1.description = description1
        valueAdds.add(valueAdd1)
        container.valueAddsSubject.onNext(valueAdds)
        assertEquals(1, container.childCount)
        val textView = container.getChildAt(0) as TextView
        assertEquals(description1, actual = textView.text.toString())

        val valueAdd2 = HotelOffersResponse.ValueAdds()
        val description2 = "Value Add 2"
        valueAdd1.description = description2
        valueAdds.add(valueAdd2)
        container.valueAddsSubject.onNext(valueAdds)
        assertEquals(2, container.childCount)
        val textView2 = container.getChildAt(0) as TextView
        assertEquals(description2, actual = textView2.text.toString())
    }
}
