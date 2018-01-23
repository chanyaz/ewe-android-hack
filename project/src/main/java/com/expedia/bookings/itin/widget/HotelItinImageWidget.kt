package com.expedia.bookings.itin.widget

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityOptionsCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ItinActionsSection
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.itin.SummaryButton
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase

class HotelItinImageWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val hotelImageView: ImageView by bindView(R.id.hotel_image)
    val hotelNameTextView: TextView by bindView(R.id.hotel_name)
    val actionButtons: ItinActionsSection by bindView(R.id.action_button_layout)

    init {
        View.inflate(context, R.layout.hotel_itin_image_container, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        val callActionButton = setupHotelPhone(itinCardDataHotel)
        val messageActionButton = setupHotelMessaging(itinCardDataHotel)

        if (!itinCardDataHotel.property.thumbnail.originalUrl.isNullOrBlank()) {
            val hotelMedia = HotelMedia(itinCardDataHotel.property.thumbnail.originalUrl)
            PicassoHelper.Builder(hotelImageView)
                    .setPlaceholder(R.drawable.room_fallback)
                    .fit()
                    .centerCrop()
                    .build()
                    .load(hotelMedia.getBestUrls(Ui.getScreenSize(context).x / 2))
        }
        hotelNameTextView.text = itinCardDataHotel.propertyName

        if (callActionButton != null || messageActionButton != null) {
            actionButtons.visibility = View.VISIBLE
            actionButtons.bind(callActionButton, messageActionButton)
        }
    }

    private fun setupHotelPhone(itinCardDataHotel: ItinCardDataHotel): SummaryButton? {
        val phoneNumber = itinCardDataHotel.localPhone
        val callButton: SummaryButton

        if (phoneNumber.isNotEmpty()) {
            callButton = SummaryButton(R.drawable.itin_call_hotel, phoneNumber,
                    Phrase.from(context, R.string.itin_hotel_details_call_button_content_description_TEMPLATE)
                            .put("phonenumber", phoneNumber).format().toString(),
                    OnClickListener {
                        OmnitureTracking.trackItinHotelCall()
                        if (phoneNumber.isNotEmpty()) {
                            val pm = context.packageManager
                            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                                SocialUtils.call(context, phoneNumber)
                            } else {
                                ClipboardUtils.setText(context, phoneNumber)
                                Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            return callButton
        } else {
            return null
        }
    }

    private fun setupHotelMessaging(itinCardDataHotel: ItinCardDataHotel): SummaryButton? {
        val messagingUrl = itinCardDataHotel.property.epcConversationUrl
        val messagingButton: SummaryButton
        val messagingText: String = context.getString(R.string.itin_hotel_details_message_hotel_button)

        if (messagingUrl.isNotEmpty() && isHotelMessagingEnabled()) {
            messagingButton = SummaryButton(R.drawable.ic_hotel_message_icon,
                    messagingText,
                    messagingText,
                    OnClickListener {
                        OmnitureTracking.trackItinHotelMessage()
                        context.startActivity(buildWebViewIntent(messagingText, messagingUrl).intent,
                                ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_up_partially, 0)
                                        .toBundle())
                    })
            return messagingButton
        } else {
            return null
        }
    }

    private fun buildWebViewIntent(title: String?, url: String): WebViewActivity.IntentBuilder {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setUrl(url)
        builder.setTitle(title)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(false)
        builder.setDomStorage(true)
        return builder
    }

    private fun isHotelMessagingEnabled(): Boolean {
        return AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppTripsMessageHotel)
    }
}
