package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.presenter.shared.StoredCouponAdapter
import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus

class CouponTestUtil {
    companion object {

        fun createStoredCouponAdapterData(couponName: List<String> = listOf("A", "B", "C"), visibility: List<StoredCouponAppliedStatus> = listOf(StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.DEFAULT) ): List<StoredCouponAdapter> {
            val savedCoupon1 = createSavedCoupon(couponName[0], "1")
            val storedCouponAdapter1 = StoredCouponAdapter(savedCoupon1, visibility[0])
            val savedCoupon2 = createSavedCoupon(couponName[1], "2")
            val storedCouponAdapter2 = StoredCouponAdapter(savedCoupon2, visibility[1])
            val savedCoupon3 = createSavedCoupon(couponName[2], "3")
            val storedCouponAdapter3 = StoredCouponAdapter(savedCoupon3, visibility[2])

            return listOf<StoredCouponAdapter>(storedCouponAdapter1, storedCouponAdapter2, storedCouponAdapter3)
        }

        fun createSavedCoupon(name: String, instanceId: String, redemptionStatus: HotelCreateTripResponse.RedemptionStatus = HotelCreateTripResponse.RedemptionStatus.VALID): HotelCreateTripResponse.SavedCoupon {
            val savedCoupon = HotelCreateTripResponse.SavedCoupon()
            savedCoupon.instanceId = instanceId
            savedCoupon.name = name
            savedCoupon.redemptionStatus = redemptionStatus
            return savedCoupon
        }
    }
}
