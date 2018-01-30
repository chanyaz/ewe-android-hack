package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelApplyCouponCodeParameters
import com.expedia.bookings.data.hotels.HotelApplySavedCodeParameters
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.data.payment.ProgramName
import com.expedia.bookings.data.payment.UserPreferencePointsDetails
import com.expedia.bookings.presenter.shared.StoredCouponAdapter
import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus

class CouponTestUtil {
    companion object {

        fun createStoredCouponAdapterData(couponName: List<String> = listOf("A", "B", "C"), visibility: List<StoredCouponAppliedStatus> = listOf(StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.DEFAULT, StoredCouponAppliedStatus.DEFAULT)): List<StoredCouponAdapter> {
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

        fun storedCouponParam(success: Boolean = true): HotelApplySavedCodeParameters {
            val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
            return HotelApplySavedCodeParameters.Builder()
                    .tripId("bc9fec5d-7539-41e9-ab60-3e593f0912fe")
                    .isFromNotSignedInToSignedIn(false)
                    .instanceId(if (success) "happypath_createtrip_saved_coupons_select" else "not_valid")
                    .userPreferencePointsDetails(listOf(pointsDetails))
                    .build()
        }

        fun applyCouponParam(success: Boolean = true): HotelApplyCouponCodeParameters {
            val pointsDetails = UserPreferencePointsDetails(ProgramName.ExpediaRewards, PointsAndCurrency(1000f, PointsType.BURN, Money("100", "USD")))
            return HotelApplyCouponCodeParameters.Builder()
                    .tripId("bc9fec5d-7539-41e9-ab60-3e593f0912fe")
                    .isFromNotSignedInToSignedIn(false)
                    .couponCode(if (success) "happypath_createtrip_saved_coupons_select" else "not_valid")
                    .userPreferencePointsDetails(listOf(pointsDetails))
                    .build()
        }
    }
}
