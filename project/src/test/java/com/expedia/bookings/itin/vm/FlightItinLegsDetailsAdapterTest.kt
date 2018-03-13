package com.expedia.bookings.itin.vm

import android.content.Context
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.itin.flight.manageBooking.FlightItinLegsDetailAdapter
import com.expedia.bookings.itin.flight.manageBooking.FlightItinLegsDetailData
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinLegsDetailsAdapterTest {
    private lateinit var context: Context
    private lateinit var flightItinLegsDetailAdapter: FlightItinLegsDetailAdapter
    private lateinit var adapterView: View

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        flightItinLegsDetailAdapter = FlightItinLegsDetailAdapter(context, getLegsDetailList("0"))
        adapterView = View.inflate(context, R.layout.flight_itin_leg_view, null)
    }

    @Test
    fun testTitle() {
        val viewHolder = flightItinLegsDetailAdapter.ViewHolder(adapterView)
        flightItinLegsDetailAdapter.onBindViewHolder(viewHolder, 0)
        val title = Phrase.from(context, R.string.itin_flight_leg_detail_widget_title_TEMPLATE).put("departure", "SFO").put("arrival", "SEA").format().toString()
        assertEquals(title, viewHolder.title.text.toString())
    }

    @Test
    fun testSubtitleForZeroStop() {
        val viewHolder = flightItinLegsDetailAdapter.ViewHolder(adapterView)
        flightItinLegsDetailAdapter.legsDetailList.clear()
        flightItinLegsDetailAdapter.legsDetailList.add(getLegsDetailList("0").get(0))
        flightItinLegsDetailAdapter.onBindViewHolder(viewHolder, 0)

        val subtitle = "Dec 13 11:39pm - 12:19pm · Nonstop"
        assertEquals(subtitle, viewHolder.subtitle.text.toString())

        val contentDescriptionText = "Dec 13 11:39pm to 12:19pm · Nonstop"
        assertEquals(contentDescriptionText, viewHolder.subtitle.contentDescription.toString())
    }

    @Test
    fun testSubtitleforOneStop() {
        val viewHolder = flightItinLegsDetailAdapter.ViewHolder(adapterView)
        flightItinLegsDetailAdapter.legsDetailList.clear()
        flightItinLegsDetailAdapter.legsDetailList.add(getLegsDetailList("1").get(0))
        flightItinLegsDetailAdapter.onBindViewHolder(viewHolder, 0)

        val subtitle = "Dec 13 11:39pm - 12:19pm · 1 stop"
        assertEquals(subtitle, viewHolder.subtitle.text.toString())

        val contentDescriptionText = "Dec 13 11:39pm to 12:19pm · 1 stop"
        assertEquals(contentDescriptionText, viewHolder.subtitle.contentDescription.toString())
    }

    @Test
    fun testSubtitleforMoreThanOneStop() {
        val viewHolder = flightItinLegsDetailAdapter.ViewHolder(adapterView)
        flightItinLegsDetailAdapter.legsDetailList.clear()
        flightItinLegsDetailAdapter.legsDetailList.add(getLegsDetailList("2").get(0))
        flightItinLegsDetailAdapter.onBindViewHolder(viewHolder, 0)

        val subtitle = "Dec 13 11:39pm - 12:19pm · 2 stops"
        assertEquals(subtitle, viewHolder.subtitle.text.toString())

        val contentDescriptionText = "Dec 13 11:39pm to 12:19pm · 2 stops"
        assertEquals(contentDescriptionText, viewHolder.subtitle.contentDescription.toString())
    }

    private fun getLegsDetailList(numbOfStop: String): ArrayList<FlightItinLegsDetailData> {
        val list = ArrayList<FlightItinLegsDetailData>()
        val imgPath = "https://images.trvl-media.com/media/content/expus/graphics/static_content/fusion/v0.1b/images/airlines/smVX.gif"
        val flightItinLegsDetailData = FlightItinLegsDetailData(imgPath, "SFO", "SEA", "Dec 13", "11:39pm", "Dec 14", "12:19pm", numbOfStop)
        list.add(flightItinLegsDetailData)
        return list
    }
}
