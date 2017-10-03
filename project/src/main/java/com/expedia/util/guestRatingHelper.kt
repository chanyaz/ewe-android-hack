package com.expedia.util

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R

fun getGuestRatingText(rating: Float, resources: Resources) : String {
    return if (rating < 3.5f) {
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
}
