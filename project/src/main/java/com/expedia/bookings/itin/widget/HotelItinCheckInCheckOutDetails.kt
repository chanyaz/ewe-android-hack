package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.fragment.ScrollableContentDialogFragment
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class HotelItinCheckInCheckOutDetails(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {
    val checkInDateView: TextView by bindView(R.id.hotel_itin_checkin_date_text)
    val checkInTimeView: TextView by bindView(R.id.hotel_itin_checkin_time_text)
    val checkOutDateView: TextView by bindView(R.id.hotel_itin_checkout_date_text)
    val checkOutTimeView: TextView by bindView(R.id.hotel_itin_checkout_time_text)
    val checkInPoliciesDivider: View by bindView(R.id.hotel_itin_check_in_check_out_divider)
    val checkInOutPoliciesContainer: FrameLayout by bindView(R.id.hotel_itin_check_in_check_out_policies_container)
    val checkInOutPoliciesButtonText: TextView by bindView(R.id.hotel_itin_check_in_check_out_policies_text)
    val DIALOG_TAG = "CHECK_IN_POLICY_DIALOG"

    init {
        View.inflate(context, R.layout.widget_hotel_itin_checkin_checkout_details, this)
    }

    fun setUpWidget(itinCardDataHotel: ItinCardDataHotel) {
        checkInDateView.text = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(itinCardDataHotel.startDate)
        checkInDateView.contentDescription = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(itinCardDataHotel.startDate)
        checkOutDateView.text = LocaleBasedDateFormatUtils.dateTimeToEEEMMMd(itinCardDataHotel.endDate)
        checkOutDateView.contentDescription = LocaleBasedDateFormatUtils.dateTimeToEEEEMMMd(itinCardDataHotel.endDate)
        checkInTimeView.text = itinCardDataHotel.getFallbackCheckInTime(context).toLowerCase()
        checkOutTimeView.text = itinCardDataHotel.getFallbackCheckOutTime(context).toLowerCase()
        if (itinCardDataHotel.property.checkInPolicies.isNotEmpty()) {
            checkInPoliciesDivider.visibility = View.VISIBLE
            checkInOutPoliciesContainer.visibility = View.VISIBLE
            val specialInstructions = itinCardDataHotel.property.specialInstruction
            val shouldShowSpecialInstruction = specialInstructions.isNotEmpty()
            if (shouldShowSpecialInstruction) {
                checkInOutPoliciesButtonText.text = context.getString(R.string.itin_hotel_check_in_policies_and_special_instruction)
            }
            checkInOutPoliciesContainer.setOnClickListener {
                val fragmentManager = (context as FragmentActivity).supportFragmentManager
                val dialog = if (shouldShowSpecialInstruction) {
                    ScrollableContentDialogFragment.newInstance(
                            context.resources.getString(R.string.itin_hotel_check_in_policies_dialog_title),
                            TextUtils.join("<br>", itinCardDataHotel.property.checkInPolicies).toString(),
                            context.getString(R.string.itin_hotel_special_instruction_dialog_sub_title),
                            TextUtils.join("<br>", specialInstructions).toString())
                } else {
                    ScrollableContentDialogFragment.newInstance(context.resources.getString(R.string.itin_hotel_check_in_policies_dialog_title),
                            TextUtils.join("<br>", itinCardDataHotel.property.checkInPolicies).toString())
                }
                dialog.show(fragmentManager, DIALOG_TAG)
                OmnitureTracking.trackHotelItinCheckInPoliciesDialogClick()
            }
            checkInOutPoliciesButtonText.setCompoundDrawablesTint(ContextCompat.getColor(context, R.color.app_primary))
        }
    }
}
