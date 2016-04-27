package com.expedia.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils

fun getABTestGuestRatingText(rating: Float, resources: Resources): String {
    return if (isSuperlativeReviewsABTestOn()) {
        if (isWithSuperlative()) {
            if (rating < 3.5f) {
                resources.getString(R.string.hotel_guest_recommend)
            } else if (rating < 4f) {
                resources.getString(R.string.hotel_guest_recommend_good_superlative)
            } else if (rating < 4.3f) {
                resources.getString(R.string.hotel_guest_recommend_very_good_superlative)
            } else if (rating < 4.5f) {
                resources.getString(R.string.hotel_guest_recommend_excellent_superlative)
            } else if (rating < 4.7f) {
                resources.getString(R.string.hotel_guest_recommend_wonderful_superlative)
            } else {
                resources.getString(R.string.hotel_guest_recommend_exceptional_superlative)
            }
        } else if (isWithColorNoSuperlatives()) {
            resources.getString(R.string.hotel_guest_recommend)
        } else {
            ""
        }
    } else {
        getControlGuestRatingText(rating, resources)
    }
}

fun getABTestGuestRatingBackground(rating: Float, context: Context): Drawable {
    val isRatingExceptional = rating > 4.6f

    return if (isSuperlativeReviewsABTestOn() && isShowColorBackground() && isRatingExceptional) {
        ContextCompat.getDrawable(context, R.drawable.user_review_background_green)
    } else {
        getControlGuestRatingBackground(rating, context)
    }
}

fun getControlGuestRatingText(@Suppress("UNUSED_PARAMETER") rating: Float, resources: Resources) : String {
    return resources.getString(R.string.hotel_guest_recommend)
}

fun getControlGuestRatingBackground(@Suppress("UNUSED_PARAMETER") rating: Float, context: Context): Drawable {
    return ContextCompat.getDrawable(context, R.drawable.user_review_background)
}

private fun isWithSuperlative(): Boolean {
    return isWithColorWithSuperlatives() || isNoColorWithSuperlatives()
}

private fun isShowColorBackground(): Boolean {
    return isWithColorNoSuperlatives() || isWithColorWithSuperlatives()
}

private fun isWithColorWithSuperlatives(): Boolean {
    return getSuperlativeReviewsABVariant() == AbacusUtils.HotelSuperlativeReviewsVariate.WITH_COLOR_WITH_SUPERLATIVES.ordinal
}

private fun isNoColorWithSuperlatives(): Boolean {
    return getSuperlativeReviewsABVariant() == AbacusUtils.HotelSuperlativeReviewsVariate.NO_COLOR_WITH_SUPERLATIVES.ordinal
}

private fun isWithColorNoSuperlatives(): Boolean {
    return getSuperlativeReviewsABVariant() == AbacusUtils.HotelSuperlativeReviewsVariate.WITH_COLOR_NO_SUPERLATIVES.ordinal
}

private fun isSuperlativeReviewsABTestOn(): Boolean {
    return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsV2SuperlativeReviewsABTest)
}

private fun getSuperlativeReviewsABVariant(): Int {
    return Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppHotelsV2SuperlativeReviewsABTest)
}
