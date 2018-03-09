package com.expedia.bookings.itin.hotel.details

import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityOptionsCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.features.Features
import com.expedia.bookings.itin.common.ItinBookingInfoCardView
import com.expedia.bookings.itin.common.ItinLinkOffCardView
import com.expedia.bookings.itin.hotel.manageBooking.HotelItinManageBookingActivity
import com.expedia.bookings.itin.hotel.pricingRewards.HotelItinPricingRewardsActivity
import com.expedia.bookings.itin.scopes.HotelItinDetailsScope
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.itin.utils.WebViewLauncher
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class HotelItinBookingDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val manageBookingCard: ItinLinkOffCardView by bindView(R.id.itin_hotel_manage_booking_card_view)
    val priceSummaryCard: ItinLinkOffCardView by bindView(R.id.itin_hotel_price_summary_card_view)
    val additionalInfoCard: ItinLinkOffCardView by bindView(R.id.itin_hotel_additional_info_card_view)

    var checkIfReadJsonEnabled = Features.all.readTripJson.enabled()
    var checkIfWriteJsonEnabled = Features.all.itineraryManagerStoreTripsJson.enabled()
    val stringProvider: StringSource = Ui.getApplication(context).appComponent().stringProvider()
    val abacusProvider: AbacusSource = Ui.getApplication(context).appComponent().abacusProvider()
    var readJsonUtil: IJsonToItinUtil = Ui.getApplication(context).tripComponent().jsonUtilProvider()
    val webViewLauncher: IWebViewLauncher = WebViewLauncher(context)
    val tripsTracking: ITripsTracking = TripsTracking
    val newPriceSummaryCard: ItinBookingInfoCardView by bindView(R.id.itin_hotel_booking_info_price_summary)

    init {
        View.inflate(context, R.layout.widget_hotel_itin_booking_details, this)
        this.orientation = LinearLayout.VERTICAL
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        if (itinCardDataHotel.isSharedItin) {
            visibility = View.GONE
        } else {
            manageBookingCard.setIcon(R.drawable.ic_itin_manage_booking_icon)
            manageBookingCard.setHeadingText(context.resources.getText(R.string.itin_hotel_manage_booking_header))
            manageBookingCard.setSubHeadingText(context.resources.getText(R.string.itin_hotel_details_manage_booking_subheading))

            if (checkIfWriteJsonEnabled && checkIfReadJsonEnabled) {
                val itin = readJsonUtil.getItin(itinCardDataHotel.tripId)
                itin?.let { itin ->
                    itin.firstHotel()?.let { hotel ->
                        newPriceSummaryCard.visibility = View.VISIBLE
                        priceSummaryCard.visibility = View.GONE
                        val activityLauncher = ActivityLauncher(context, HotelItinPricingRewardsActivity, itinCardDataHotel.tripId)
                        val scope = HotelItinDetailsScope(itin, hotel, stringProvider, webViewLauncher, tripsTracking, activityLauncher, abacusProvider)
                        val vm = HotelItinPriceSummaryButtonViewModel(scope)
                        newPriceSummaryCard.viewModel = vm
                    }
                }
            } else {
                newPriceSummaryCard.visibility = View.GONE
                priceSummaryCard.visibility = View.VISIBLE
                priceSummaryCard.setIcon(R.drawable.ic_itin_credit_card_icon)
                priceSummaryCard.setHeadingText(context.resources.getText(R.string.itin_hotel_details_price_summary_rewards_heading))
                priceSummaryCard.hideSubheading()
            }

            additionalInfoCard.setIcon(R.drawable.ic_itin_additional_info_icon)
            additionalInfoCard.setHeadingText(context.resources.getText(R.string.itin_hotel_details_additional_info_heading))
            additionalInfoCard.hideSubheading()

            manageBookingCard.setOnClickListener {
                context.startActivity(HotelItinManageBookingActivity.createIntent(context, itinCardDataHotel.id), ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete).toBundle())
                TripsTracking.trackHotelItinManageBookingClick()
            }
            priceSummaryCard.setOnClickListener {
                (context as Activity).startActivityForResult(buildWebViewIntent(R.string.itin_hotel_details_price_summary_heading, itinCardDataHotel.detailsUrl, "price-header", itinCardDataHotel.tripNumber).intent, Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE)
                TripsTracking.trackHotelItinPricingRewardsClick()
            }
            additionalInfoCard.setOnClickListener {
                (context as Activity).startActivityForResult(buildWebViewIntent(R.string.itin_hotel_details_additional_info_heading, itinCardDataHotel.detailsUrl, null, itinCardDataHotel.tripNumber).intent, Constants.ITIN_WEBVIEW_REFRESH_ON_EXIT_CODE)
                TripsTracking.trackHotelItinAdditionalInfoClick()
            }
        }
    }

    private fun buildWebViewIntent(title: Int, url: String, anchor: String?, tripId: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        if (anchor != null) builder.setUrlWithAnchor(url, anchor) else builder.setUrl(url)
        builder.setTitle(title)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.setItinTripIdForRefresh(tripId)
        return builder
    }
}
