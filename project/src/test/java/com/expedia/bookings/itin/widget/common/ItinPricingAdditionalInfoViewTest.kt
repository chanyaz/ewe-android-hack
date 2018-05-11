package com.expedia.bookings.itin.widget.common

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.AdditionalInfoItemView
import com.expedia.bookings.itin.common.ItinAdditionalInfoItem
import com.expedia.bookings.itin.common.ItinPricingAdditionInfoViewModelInterface
import com.expedia.bookings.itin.common.ItinPricingAdditionalInfoView
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinPricingAdditionalInfoViewTest {
    private lateinit var activity: Activity
    private lateinit var testView: ItinPricingAdditionalInfoView
    private val viewModel = MockItinPricingAdditionInfoViewModel()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
        activity.setTheme(R.style.ItinTheme)
        testView = LayoutInflater.from(activity).inflate(R.layout.test_itin_additional_pricing_info_view, null) as ItinPricingAdditionalInfoView
    }

    @Test
    fun testToolbar() {
        val testString = "Additional pricing information"
        val testObserver = TestObserver<String>()
        viewModel.toolbarTitleSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        viewModel.toolbarTitleSubject.onNext(testString)
        testObserver.assertValueCount(1)
        assertEquals(testString, testObserver.values()[0])
        assertEquals(testString, testView.toolbar.toolbarTitleText.text)
    }

    @Test
    fun testAdditionalInfoItem() {
        val infoItems = listOf(
                ItinAdditionalInfoItem("HEADING 1", "CONTENT 1"),
                ItinAdditionalInfoItem("HEADING 2", "CONTENT 2")
        )
        val testObserver = TestObserver<List<ItinAdditionalInfoItem>>()
        viewModel.additionalInfoItemSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        assertEquals(0, testView.container.childCount)
        viewModel.additionalInfoItemSubject.onNext(infoItems)
        testObserver.assertValueCount(1)
        val actualValues: List<ItinAdditionalInfoItem> = testObserver.values()[0]
        assertEquals(infoItems, actualValues)
        assertEquals(2, testView.container.childCount)
        val child1 = testView.container.getChildAt(0) as AdditionalInfoItemView
        val child2 = testView.container.getChildAt(1) as AdditionalInfoItemView
        assertEquals("HEADING 1", child1.heading.text.toString())
        assertEquals("HEADING 2", child2.heading.text.toString())
        assertEquals("CONTENT 1", child1.content.text.toString())
        assertEquals("CONTENT 2", child2.content.text.toString())

        //firing the subject clears the container before adding new views
        viewModel.additionalInfoItemSubject.onNext(infoItems)
        assertEquals(2, testView.container.childCount)
    }

    class MockItinPricingAdditionInfoViewModel : ItinPricingAdditionInfoViewModelInterface {
        override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
        override val additionalInfoItemSubject: PublishSubject<List<ItinAdditionalInfoItem>> = PublishSubject.create()
    }
}