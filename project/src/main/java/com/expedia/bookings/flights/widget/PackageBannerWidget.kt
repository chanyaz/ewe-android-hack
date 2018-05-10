package com.expedia.bookings.flights.widget

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.NavigationHelper
import com.expedia.bookings.utils.bindView
import com.squareup.phrase.Phrase

class PackageBannerWidget(context: Context) : FrameLayout(context) {
    private val cardViewPackageBanner: CardView by bindView(R.id.card_view_package_banner)

    init {
        View.inflate(context, R.layout.package_banner_header_cell, this)
    }

    fun bind() {
        setMargins()
        cardViewPackageBanner.contentDescription = Phrase.from(context, R.string.accessibility_announcement_package_banner_TEMPLATE)
                .put("title", context.getString(R.string.flight_package_banner_title))
                .put("description", context.getString(R.string.flight_package_banner_description))
                .put("button", context.getString(R.string.accessibility_cont_desc_role_button))
                .format().toString()
    }

    fun navigateToPackages() {
        FlightsV2Tracking.trackCrossSellPackageBannerClick()
        val nav = NavigationHelper(context)
        val data = Bundle()
        data.putBoolean(Constants.INTENT_PERFORM_HOTEL_SEARCH, true)
        nav.goToPackagesForResult(data, null, Constants.FLIGHT_REQUEST_CODE)
    }

    private fun setMargins() {
        val marginSide = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9f, resources.displayMetrics).toInt()
        val newParams = cardViewPackageBanner.layoutParams as ViewGroup.MarginLayoutParams
        newParams.setMargins(marginSide, 0, marginSide, 0)
        cardViewPackageBanner.layoutParams = newParams
    }
}
