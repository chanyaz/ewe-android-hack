package com.expedia.bookings.itin.hotel.details

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.fragment.ScrollableContentDialogFragment
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.Strings
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
        val specialInstructions = itinCardDataHotel.property.specialInstruction
        val shouldShowSpecialInstruction = specialInstructions.isNotEmpty()
        val shouldShowCheckInPolicies = itinCardDataHotel.property.checkInPolicies.isNotEmpty()
        if (shouldShowCheckInPolicies || shouldShowSpecialInstruction) {
            checkInPoliciesDivider.visibility = View.VISIBLE
            checkInOutPoliciesContainer.visibility = View.VISIBLE
            if (shouldShowCheckInPolicies && shouldShowSpecialInstruction) {
                checkInOutPoliciesButtonText.text = context.getString(R.string.itin_hotel_check_in_policies_and_special_instruction)
            } else if (shouldShowSpecialInstruction) {
                checkInOutPoliciesButtonText.text = context.getString(R.string.itin_hotel_special_instruction)
            }
            checkInOutPoliciesContainer.setOnClickListener {
                val fragmentManager = (context as FragmentActivity).supportFragmentManager
                val dialog = if (shouldShowCheckInPolicies && shouldShowSpecialInstruction) {
                    ScrollableContentDialogFragment.newInstance(
                            context.resources.getString(R.string.itin_hotel_check_in_policies_dialog_title),
                            getTitleContent(itinCardDataHotel),
                            context.getString(R.string.itin_hotel_special_instruction_dialog_sub_title),
                            TextUtils.join("<br>", specialInstructions).toString())
                } else if (shouldShowSpecialInstruction) {
                    ScrollableContentDialogFragment.newInstance(
                            context.resources.getString(R.string.itin_hotel_special_instruction),
                            TextUtils.join("<br>", specialInstructions).toString())
                } else {
                    ScrollableContentDialogFragment.newInstance(context.resources.getString(R.string.itin_hotel_check_in_policies_dialog_title),
                            getTitleContent(itinCardDataHotel))
                }
                dialog.show(fragmentManager, DIALOG_TAG)
                TripsTracking.trackHotelItinCheckInPoliciesDialogClick()
            }
            checkInOutPoliciesButtonText.setCompoundDrawablesTint(ContextCompat.getColor(context, R.color.app_primary))
        }
    }

    private fun getTitleContent(itinCardDataHotel: ItinCardDataHotel): String {
        val stringBuilder = StringBuilder()
        val lateArrivalInstructions = (itinCardDataHotel.getTripComponent() as TripHotel).lateArrivalInstructions
        stringBuilder.append(TextUtils.join("<br>", itinCardDataHotel.property.checkInPolicies).toString())
        if (Strings.isNotEmpty(lateArrivalInstructions)) {
            stringBuilder.append("<br>")
            stringBuilder.append(lateArrivalInstructions)
        }
        return stringBuilder.toString()
    }
}
