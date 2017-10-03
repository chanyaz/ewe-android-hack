package com.expedia.bookings.itin.widget

import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.itin.activity.HotelItinManageBookingActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView

class HotelItinBookingDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val manageBookingCard: ItinLinkOffCardView by bindView(R.id.itin_hotel_manage_booking_card_view)
    val priceSummaryCard: ItinLinkOffCardView by bindView(R.id.itin_hotel_price_summary_card_view)
    val additionalInfoCard: ItinLinkOffCardView by bindView(R.id.itin_hotel_additional_info_card_view)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_booking_details, this)
        this.orientation = LinearLayout.VERTICAL
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.isSharedItin) {
            visibility = View.GONE
        }
        else {
            manageBookingCard.setIcon(R.drawable.ic_itin_manage_booking_icon)
            manageBookingCard.setHeadingText(context.resources.getText(R.string.itin_hotel_manage_booking_header))
            manageBookingCard.setSubHeadingText(context.resources.getText(R.string.itin_hotel_details_manage_booking_subheading))

            priceSummaryCard.setIcon(R.drawable.ic_itin_credit_card_icon)
            priceSummaryCard.setHeadingText(context.resources.getText(R.string.itin_hotel_details_price_summary_heading))
            priceSummaryCard.hideSubheading()

            additionalInfoCard.setIcon(R.drawable.ic_itin_additional_info_icon)
            additionalInfoCard.setHeadingText(context.resources.getText(R.string.itin_hotel_details_additional_info_heading))
            additionalInfoCard.hideSubheading()

            manageBookingCard.setOnClickListener {
                context.startActivity(HotelItinManageBookingActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                OmnitureTracking.trackHotelItinManageBookingClick()
            }
            priceSummaryCard.setOnClickListener {
                (context as Activity).startActivityForResult(buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, itinCardDataHotel.detailsUrl, "price-header", itinCardDataHotel.tripNumber).intent, Constants.ITIN_HOTEL_WEBPAGE_CODE)
                OmnitureTracking.trackHotelItinPricingRewardsClick()
            }
            additionalInfoCard.setOnClickListener {
                (context as Activity).startActivityForResult(buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, itinCardDataHotel.detailsUrl, null, itinCardDataHotel.tripNumber).intent, Constants.ITIN_HOTEL_WEBPAGE_CODE)
                OmnitureTracking.trackHotelItinAdditionalInfoClick()
            }
        }
    }

    private fun buildWebViewIntent(title: Int, url: String, anchor: String?, tripId: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
        builder.setTitle(title)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.setHotelItinTripId(tripId)
        return builder
    }
}