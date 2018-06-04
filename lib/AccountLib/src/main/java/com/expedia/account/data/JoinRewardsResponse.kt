package com.expedia.account.data

import com.google.gson.annotations.SerializedName

class JoinRewardsResponse {
    @SerializedName("loyaltyMemebershipName")
    var loyaltyMembershipName: String? = null
    @SerializedName("loyaltyMemebershipActive")
    var loyaltyMembershipActive: Boolean = false

    var errors: List<MobileError>? = null
    var loyaltyMembershipNumber: String? = null
    var loyaltyPointsAvailable: String? = null
    var loyaltyPointsPending: String? = null
    var loyaltyAmountAvailable: String? = null
    var bookingCurrency: String? = null
    var isAllowedToShopWithPoints: Boolean = false
    var loyaltyMonetaryValue: MobilePrice? = null
    var currentTierCredits: TierInfo? = null
    var reqUpgradeCredits: TierInfo? = null
    var pointsRemainingTillPurchase: String? = null
    var membershipTierName: String? = null
    internal var detailedStatus: String? = null
    internal var detailedStatusMsg: String? = null

    class MobilePrice {
        var amount: String? = null
        var formattedPrice: String? = null
        var formattedWholePrice: String? = null
    }

    class TierInfo {
        var hotelNights: Long? = null
        var tierName: String? = null
        var amount: MobilePrice? = null
    }

    class MobileError {
        var errorCode: ErrorCode? = null
        var errorInfo: ErrorInfo? = null
        var diagnosticId: String? = null
        var getDiagnosticFullText: String? = null
        var activityId: String? = null
    }

    class ErrorInfo {
        var summary: String? = null
        var cause: String? = null
    }

    enum class ErrorCode {
        LOYALTY_REWARDS_NOT_ENABLED_EXCEPTION,
        MUST_BE_SIGNED_IN,
        LOYALTY_USER_ENROLL_EXCEPTION
    }
}
