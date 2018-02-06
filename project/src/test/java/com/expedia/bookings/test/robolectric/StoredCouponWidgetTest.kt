package com.expedia.bookings.test.robolectric

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus
import com.expedia.bookings.presenter.shared.StoredCouponListAdapter
import com.expedia.bookings.presenter.shared.StoredCouponViewHolder
import com.expedia.bookings.presenter.shared.StoredCouponWidget
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class StoredCouponWidgetTest {
    private var activity: AppCompatActivity by Delegates.notNull()
    lateinit var storedCouponWidget: StoredCouponWidget

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        storedCouponWidget = LayoutInflater.from(activity).inflate(R.layout.stored_coupon_widget, null) as StoredCouponWidget
    }

    @Test
    fun testNoOfItemsShownInStoredCouponRecyclerView() {
        setupStoredCouponWidget()

        assertEquals(3, (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter).itemCount)
    }

    @Test
    fun testStoredCouponShowsError() {
        setupStoredCouponWidgetWithError()
        val storedCouponViewHolderAt2 = findStoredCouponViewHolderAtPosition(1)
        testViewsInStoredCouponViewHolder(View.GONE, "B", View.GONE, storedCouponViewHolderAt2, View.GONE, View.VISIBLE)
    }

    @Test
    fun testStoredCouponErrorAffectsCouponsList() {
        setupStoredCouponWidget()
        storedCouponWidget.viewModel.errorObservable.onNext(Pair("Some error", "1"))
        (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter).coupons.first { it.savedCoupon.instanceId == "1" }.errorMessage = "Some error"
        (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter).coupons.first { it.savedCoupon.instanceId == "1" }.savedCouponStatus = StoredCouponAppliedStatus.ERROR
    }

    @Test
    fun testStoredCouponErrorAffectsOnlyOneCouponInCouponsList() {
        setupStoredCouponWidget()
        storedCouponWidget.viewModel.errorObservable.onNext(Pair("Some error", "1"))

        assertEquals("Some error", (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter).coupons.first { it.savedCoupon.instanceId == "1" }.errorMessage)

        storedCouponWidget.viewModel.errorObservable.onNext(Pair("Some error 2", "2"))

        assertEquals("Some error 2", (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter).coupons.first { it.savedCoupon.instanceId == "2" }.errorMessage)
        assertEquals("", (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter).coupons.first { it.savedCoupon.instanceId == "1" }.errorMessage)
    }

    @Test
    fun testViewHolderDataShownInStoredCouponRecyclerView() {
        setupStoredCouponWidget()
        val storedCouponViewHolderAt0 = findStoredCouponViewHolderAtPosition(0)

        testViewsInStoredCouponViewHolder(View.VISIBLE, "A", View.GONE, storedCouponViewHolderAt0, View.GONE, View.GONE)

        var storedCouponViewHolderAt1 = findStoredCouponViewHolderAtPosition(1)

        testViewsInStoredCouponViewHolder(View.VISIBLE, "B", View.GONE, storedCouponViewHolderAt1, View.GONE, View.GONE)

        val storedCouponViewHolderAt2 = findStoredCouponViewHolderAtPosition(2)

        testViewsInStoredCouponViewHolder(View.VISIBLE, "C", View.GONE, storedCouponViewHolderAt2, View.GONE, View.GONE)

        setupStoredCouponWidget(listOf("A", "BB", "C"), listOf(StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.SUCCESS, StoredCouponAppliedStatus.DEFAULT))
        storedCouponViewHolderAt1 = findStoredCouponViewHolderAtPosition(1)

        testViewsInStoredCouponViewHolder(View.GONE, "BB", View.VISIBLE, storedCouponViewHolderAt1, View.GONE, View.GONE)
    }

    @Test
    fun testFunctionalityOfClickOnStoredCouponViewHolder() {
        val applyStoredCouponTestSubject = TestObserver.create<HotelCreateTripResponse.SavedCoupon>()
        setupStoredCouponWidget()
        val adapter = (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter)
        adapter.applyStoredCouponObservable.subscribe(applyStoredCouponTestSubject)

        val storedCouponViewHolderAt0 = findStoredCouponViewHolderAtPosition(0)
        storedCouponViewHolderAt0.itemView.performClick()

        assertEquals("1", applyStoredCouponTestSubject.values()[0].instanceId)
        assertEquals("A", applyStoredCouponTestSubject.values()[0].name)
        testViewsInStoredCouponViewHolder(View.GONE, "A", View.GONE, storedCouponViewHolderAt0, View.VISIBLE, View.GONE)
    }

    fun setupStoredCouponWidget(couponNames: List<String> = listOf("A", "B", "C"),
                                visibility: List<StoredCouponAppliedStatus> = listOf(StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.DEFAULT)) {
        storedCouponWidget.viewModel.storedCouponsSubject.onNext(CouponTestUtil.createStoredCouponAdapterData(couponNames, visibility))
        storedCouponWidget.storedCouponRecyclerView.measure(0, 0)
        storedCouponWidget.storedCouponRecyclerView.layout(0, 0, 100, 10000)
    }

    private fun setupStoredCouponWidgetWithError() {
        setupStoredCouponWidget(visibility = listOf(StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.ERROR, StoredCouponAppliedStatus.DEFAULT))
    }

    private fun findStoredCouponViewHolderAtPosition(position: Int): StoredCouponViewHolder {
        return (storedCouponWidget.storedCouponRecyclerView.findViewHolderForAdapterPosition(position) as StoredCouponViewHolder)
    }

    fun testViewsInStoredCouponViewHolder(visibilityOfDefaultImage: Int, couponText: String, visibilityOfAppliedImage: Int, viewHolder: StoredCouponViewHolder, visibilityOfProgressBar: Int, visibilityOfErrorImage: Int) {
        assertEquals(couponText, viewHolder.couponNameTextView.text.toString())
        assertEquals(visibilityOfDefaultImage, viewHolder.defaultStateImage.visibility)
        assertEquals(visibilityOfAppliedImage, viewHolder.couponApplied.visibility)
        assertEquals(visibilityOfProgressBar, viewHolder.progressBar.visibility)
    }
}
