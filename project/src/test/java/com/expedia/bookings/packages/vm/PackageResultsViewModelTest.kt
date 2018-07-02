package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.RichContentUtils
import io.reactivex.disposables.CompositeDisposable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageResultsViewModelTest {

    private var context: Context = RuntimeEnvironment.application
    private lateinit var sut: PackageResultsViewModel
    val FLIGHT_LEG_ID = "ab64aefca28e772ca024d4a00e6ae131"

    @Before
    fun setup() {
        sut = PackageResultsViewModel(context)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentObserver() {
        val richContentObserver = TestObserver<Map<String, RichContent>>()
        sut.richContentStream.subscribe(richContentObserver)
        sut.makeRichContentObserver().onNext(getRichContentResponse())
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
        assertEquals(RichContentUtils.ScoreExpression.VERY_GOOD.name, richContent.scoreExpression)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testAbortRichContentCallObservable() {
        sut.richContentInboundSubscription = CompositeDisposable()
        sut.richContentOutboundSubscription = CompositeDisposable()
        assertEquals(false, sut.richContentInboundSubscription!!.isDisposed)
        assertEquals(false, sut.richContentOutboundSubscription!!.isDisposed)
        sut.abortRichContentOutboundObservable.onNext(Unit)
        sut.abortRichContentInboundObservable.onNext(Unit)
        assertEquals(true, sut.richContentInboundSubscription!!.isDisposed)
        assertEquals(true, sut.richContentOutboundSubscription!!.isDisposed)
    }

    private fun getRichContentResponse(): RichContentResponse {
        val richContentResponse = RichContentResponse()
        richContentResponse.richContentList = getRichContentList()
        return richContentResponse
    }

    private fun getRichContentList(): List<RichContent> {
        return listOf(getRichContent())
    }

    private fun getRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = FLIGHT_LEG_ID
        richContent.score = 7.9F
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        return richContent
    }
}
