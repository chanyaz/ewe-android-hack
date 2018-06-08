package com.expedia.bookings.itin.hotel.pricingRewards

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinPricingBundleViewTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
    private val testView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_price_bundle_item_view, null) as HotelItinPricingBundleView

    private lateinit var bundleContainerResetObserver: TestObserver<Unit>
    private lateinit var bundleContainerViewVisibilityObserver: TestObserver<Boolean>
    private lateinit var bundleProductDescriptionObserver: TestObserver<String>

    @Before
    fun setUp() {
        bundleContainerResetObserver = TestObserver()
        bundleContainerViewVisibilityObserver = TestObserver()
        bundleProductDescriptionObserver = TestObserver()

    }

    @After
    fun tearDown() {
        bundleContainerResetObserver.dispose()
        bundleContainerViewVisibilityObserver.dispose()
        bundleProductDescriptionObserver.dispose()
    }

    @Test
    fun testContainerVisibility() {
        val viewModel = MockHotelItinPricingBundleDescriptionViewModel()
        testView.viewModel = viewModel
        testView.viewModel.bundleContainerViewVisibilitySubject.subscribe(bundleContainerViewVisibilityObserver)
        bundleContainerViewVisibilityObserver.assertEmpty()
        assertTrue(testView.visibility == View.GONE)
        viewModel.bundleContainerViewVisibilitySubject.onNext(true)
        assertTrue(testView.visibility == View.VISIBLE)
        viewModel.bundleContainerViewVisibilitySubject.onNext(false)
        assertFalse(testView.visibility == View.VISIBLE)
    }

    @Test
    fun testBundleDescription() {
        val viewModel = MockHotelItinPricingBundleDescriptionViewModel()
        testView.viewModel = viewModel
        testView.viewModel.bundleProductDescriptionSubject.subscribe(bundleProductDescriptionObserver)
        bundleProductDescriptionObserver.assertEmpty()
        assertEquals(0, testView.childCount)
        viewModel.bundleProductDescriptionSubject.onNext("Some string")
        assertEquals(1, testView.childCount)
        assertEquals(1, bundleProductDescriptionObserver.valueCount())
        assertEquals("Some string", bundleProductDescriptionObserver.values()[0])
        val child = testView.getChildAt(0) as TextView
        assertEquals("Some string", child.text)
    }

    @Test
    fun testBundleContainerReset() {
        val viewModel = MockHotelItinPricingBundleDescriptionViewModel()
        testView.viewModel = viewModel
        testView.viewModel.bundleContainerResetSubject.subscribe(bundleContainerResetObserver)
        bundleContainerResetObserver.assertEmpty()
        val textView = TextView(activity, null)
        testView.addView(textView)
        assertTrue(testView.childCount > 0)
        viewModel.bundleContainerResetSubject.onNext(Unit)
        assertEquals(1, bundleContainerResetObserver.valueCount())
        assertEquals(0, testView.childCount)
    }
}

class MockHotelItinPricingBundleDescriptionViewModel : IHotelItinPricingBundleDescriptionViewModel {
    override val bundleContainerResetSubject = PublishSubject.create<Unit>()
    override val bundleProductDescriptionSubject = PublishSubject.create<String>()
    override val bundleContainerViewVisibilitySubject = PublishSubject.create<Boolean>()
}
