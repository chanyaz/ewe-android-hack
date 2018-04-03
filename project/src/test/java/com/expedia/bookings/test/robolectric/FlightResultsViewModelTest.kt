package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.flights.RouteHappyResponse
import com.expedia.bookings.data.flights.RouteHappyRichContent
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.RouteHappyUtils
import com.expedia.vm.FlightResultsViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightResultsViewModelTest {
    private var context: Context = RuntimeEnvironment.application
    private lateinit var sut: FlightResultsViewModel
    val FLIGHT_LEG_ID = "ab64aefca28e772ca024d4a00e6ae131"

    @Before
    fun setup() {
        sut = FlightResultsViewModel(context)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentObserver() {
        val richContentObserver = TestObserver<Map<String, RouteHappyRichContent>>()
        sut.richContentStream.subscribe(richContentObserver)
        sut.makeRouteHappyObserver().onNext(getRichContentResponse())
        richContentObserver.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentMap() {
        val richContentMap = sut.getRichContentMap(getRichContentList())
        assertEquals(FLIGHT_LEG_ID, richContentMap.keys.first())

        val richContent = richContentMap.values.first()
        assertEquals(FLIGHT_LEG_ID, richContent.legId)
        assertEquals(7.9F, richContent.score)
        assertEquals(RouteHappyUtils.ScoreExpression.VERY_GOOD.name, richContent.scoreExpression)
    }

    private fun getRichContentResponse(): RouteHappyResponse {
        val richContentResponse = RouteHappyResponse()
        richContentResponse.richContentList = getRichContentList()
        return richContentResponse
    }

    private fun getRichContentList(): List<RouteHappyRichContent> {
        return listOf(getRichContentRichContent())
    }

    private fun getRichContentRichContent(): RouteHappyRichContent {
        val richContent = RouteHappyRichContent()
        richContent.legId = FLIGHT_LEG_ID
        richContent.score = 7.9F
        richContent.scoreExpression = RouteHappyUtils.ScoreExpression.VERY_GOOD.name
        return richContent
    }
}
