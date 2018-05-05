package com.expedia.bookings.itin.hotel.pricingRewards

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinPricingSummaryRewardsViewTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
    private val testView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_pricing_summary_rewards_view, null) as HotelItinPricingSummaryRewardsView

    @Test
    fun testHideWidget() {
        val testObserver = TestObserver<Unit>()
        val viewModel = MockPricingRewardsViewModel()
        viewModel.hideWidgetSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        assertTrue(testView.rewardsTitleText.visibility == View.VISIBLE)
        assertTrue(testView.rewardsSection.visibility == View.VISIBLE)
        assertTrue(testView.rewardsButton.visibility == View.VISIBLE)
        viewModel.hideWidgetSubject.onNext(Unit)
        testObserver.assertValueCount(1)
        assertTrue(testView.rewardsTitleText.visibility == View.GONE)
        assertTrue(testView.rewardsSection.visibility == View.GONE)
        assertTrue(testView.rewardsButton.visibility == View.GONE)

        testObserver.dispose()
    }

    @Test
    fun testLogoSubject() {
        val testObserver = TestObserver<String>()
        val viewModel = MockPricingRewardsViewModel()
        viewModel.logoSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        assertTrue(testView.rewardsLogoView.visibility == View.GONE)
        viewModel.logoSubject.onNext("testpath")
        assertTrue(testView.rewardsLogoView.visibility == View.VISIBLE)

        testObserver.dispose()
    }

    @Test
    fun testEarnedPointsSubject() {
        val testObserver = TestObserver<String>()
        val viewModel = MockPricingRewardsViewModel()
        viewModel.earnedPointsSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        assertTrue(testView.earnedPointsText.visibility == View.GONE)
        viewModel.earnedPointsSubject.onNext("You earned 177 Expedia Rewards points")
        testObserver.assertValueCount(1)
        testObserver.assertValue("You earned 177 Expedia Rewards points")
        assertTrue(testView.earnedPointsText.visibility == View.VISIBLE)
        assertTrue(testView.earnedPointsText.text == "You earned 177 Expedia Rewards points")

        testObserver.dispose()
    }

    @Test
    fun testBasePointsSubject() {
        val testObserver = TestObserver<String>()
        val viewModel = MockPricingRewardsViewModel()
        viewModel.basePointsSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        assertTrue(testView.basePointsText.visibility == View.GONE)
        viewModel.basePointsSubject.onNext("177 base points for this trip")
        testObserver.assertValueCount(1)
        testObserver.assertValue("177 base points for this trip")
        assertTrue(testView.basePointsText.visibility == View.VISIBLE)
        assertTrue(testView.basePointsText.text == "177 base points for this trip")

        testObserver.dispose()
    }

    @Test
    fun testBonusPointsSubject() {
        val testObserver = TestObserver<List<String>>()
        val viewModel = MockPricingRewardsViewModel()
        viewModel.bonusPointsSubject.subscribe(testObserver)
        testView.viewModel = viewModel
        val testList = listOf("test string 1", "test string 2")

        testObserver.assertEmpty()
        assertTrue(testView.bonusPointsContainer.visibility == View.GONE)
        viewModel.bonusPointsSubject.onNext(testList)
        testObserver.assertValueCount(1)
        testObserver.assertValue(testList)
        assertTrue(testView.bonusPointsContainer.visibility == View.VISIBLE)
        assertTrue(testView.bonusPointsContainer.childCount == 2)
        val child1 = testView.bonusPointsContainer.getChildAt(0)
        val child2 = testView.bonusPointsContainer.getChildAt(1)
        assertTrue(child1 is BonusPointsView)
        assertTrue(child2 is BonusPointsView)
        assertTrue((child1 as TextView).text == "test string 1")
        assertTrue((child2 as TextView).text == "test string 2")

        //checking if successive calls to subject clears container
        viewModel.bonusPointsSubject.onNext(listOf("test string 3"))
        testObserver.assertValueCount(2)
        assertTrue(testView.bonusPointsContainer.childCount == 1)
        val child = testView.bonusPointsContainer.getChildAt(0)
        assertTrue(child is BonusPointsView)
        assertTrue((child as TextView).text == "test string 3")

        testObserver.dispose()
    }

    @Test
    fun testRewardsButtonClick() {
        val testObserver = TestObserver<Unit>()
        val viewModel = MockPricingRewardsViewModel()
        viewModel.rewardsButtonClickSubject.subscribe(testObserver)
        testView.viewModel = viewModel

        testObserver.assertEmpty()
        testView.rewardsButton.performClick()
        testObserver.assertValueCount(1)

        testObserver.dispose()
    }

    class MockPricingRewardsViewModel : IHotelPricingRewardsViewModel {
        override val hideWidgetSubject: PublishSubject<Unit> = PublishSubject.create()
        override val rewardsButtonClickSubject: PublishSubject<Unit> = PublishSubject.create()
        override val logoSubject: PublishSubject<String> = PublishSubject.create()
        override val earnedPointsSubject: PublishSubject<String> = PublishSubject.create()
        override val basePointsSubject: PublishSubject<String> = PublishSubject.create()
        override val bonusPointsSubject: PublishSubject<List<String>> = PublishSubject.create()
    }
}