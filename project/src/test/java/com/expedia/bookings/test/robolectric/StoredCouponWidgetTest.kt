package com.expedia.bookings.test.robolectric

import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.presenter.shared.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
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
    fun testViewHolderDataShownInStoredCouponRecyclerView() {
        setupStoredCouponWidget()
        val storedCouponViewHolderAt0 = findStoredCouponViewHolderAtPosition(0)

        assertEquals("A", storedCouponViewHolderAt0.hotelNameTextView.text.toString())
        assertEquals(View.VISIBLE, storedCouponViewHolderAt0.defaultStateImage.visibility)

        var storedCouponViewHolderAt1 = findStoredCouponViewHolderAtPosition(1)

        assertEquals("B", storedCouponViewHolderAt1.hotelNameTextView.text.toString())
        assertEquals(View.VISIBLE, storedCouponViewHolderAt1.defaultStateImage.visibility)

        val storedCouponViewHolderAt2 = findStoredCouponViewHolderAtPosition(2)

        assertEquals("C", storedCouponViewHolderAt2.hotelNameTextView.text.toString())
        assertEquals(View.VISIBLE, storedCouponViewHolderAt2.defaultStateImage.visibility)

        val storedCouponData = createStoredCouponAdapterData()
        storedCouponData[1].savedCouponStatus = StoredCouponAppliedStatus.SUCCESS
        storedCouponData[1].savedCoupon.name = "BB"
        storedCouponWidget.viewModel.storedCouponsSubject.onNext(storedCouponData)
        storedCouponWidget.storedCouponRecyclerView.measure(0, 0)
        storedCouponWidget.storedCouponRecyclerView.layout(0, 0, 100, 10000)

        storedCouponViewHolderAt1 = findStoredCouponViewHolderAtPosition(1)

        assertEquals("BB", storedCouponViewHolderAt1.hotelNameTextView.text.toString())
        assertEquals(View.GONE, storedCouponViewHolderAt1.defaultStateImage.visibility)
        assertEquals(View.VISIBLE, storedCouponViewHolderAt1.couponApplied.visibility)
    }

    @Test
    fun testFunctionalityOfClickOnStoredCouponViewHolder() {
        val applyStoredCouponTestSubject = TestSubscriber.create<String>()
        setupStoredCouponWidget()
        val adapter = (storedCouponWidget.storedCouponRecyclerView.adapter as StoredCouponListAdapter)
        adapter.applyStoredCouponSubject.subscribe(applyStoredCouponTestSubject)

        val storedCouponViewHolderAt0 = findStoredCouponViewHolderAtPosition(0)
        storedCouponViewHolderAt0.itemView.performClick()

        assertEquals("1", applyStoredCouponTestSubject.onNextEvents[0])
        assertEquals(View.VISIBLE, storedCouponViewHolderAt0.progressBar.visibility)
        assertEquals(View.GONE, storedCouponViewHolderAt0.defaultStateImage.visibility)

        findStoredCouponViewHolderAtPosition(2).itemView.performClick()

        assertEquals("3", applyStoredCouponTestSubject.onNextEvents[1])
        assertEquals(View.VISIBLE, storedCouponViewHolderAt0.progressBar.visibility)
        assertEquals(View.GONE, storedCouponViewHolderAt0.defaultStateImage.visibility)
    }


    fun createStoredCouponAdapterData(): List<StoredCouponAdapter> {
        val savedCoupon1 = createSavedCoupon("A", "1")
        val storedCouponAdapter1 = StoredCouponAdapter(savedCoupon1, StoredCouponAppliedStatus.DEFAULT)
        val savedCoupon2 = createSavedCoupon("B", "2")
        val storedCouponAdapter2 = StoredCouponAdapter(savedCoupon2, StoredCouponAppliedStatus.DEFAULT)
        val savedCoupon3 = createSavedCoupon("C", "3")
        val storedCouponAdapter3 = StoredCouponAdapter(savedCoupon3, StoredCouponAppliedStatus.DEFAULT)

        return listOf<StoredCouponAdapter>(storedCouponAdapter1, storedCouponAdapter2, storedCouponAdapter3)
    }

    fun createSavedCoupon(name: String, instanceId: String, redemptionStatus: HotelCreateTripResponse.RedemptionStatus = HotelCreateTripResponse.RedemptionStatus.VALID): HotelCreateTripResponse.SavedCoupon {
        val savedCoupon = HotelCreateTripResponse.SavedCoupon()
        savedCoupon.instanceId = instanceId
        savedCoupon.name = name
        savedCoupon.redemptionStatus = redemptionStatus
        return savedCoupon
    }

    fun setupStoredCouponWidget() {
        storedCouponWidget.viewModel.storedCouponsSubject.onNext(createStoredCouponAdapterData())
        storedCouponWidget.storedCouponRecyclerView.measure(0, 0)
        storedCouponWidget.storedCouponRecyclerView.layout(0, 0, 100, 10000)
    }

    fun findStoredCouponViewHolderAtPosition(position: Int): StoredCouponViewHolder {
        return (storedCouponWidget.storedCouponRecyclerView.findViewHolderForAdapterPosition(position) as StoredCouponViewHolder)
    }

}
