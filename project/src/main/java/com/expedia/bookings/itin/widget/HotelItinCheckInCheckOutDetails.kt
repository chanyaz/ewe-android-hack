package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.fragment.ScrollableContentDialogFragment
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.widget.TextView
import java.util.Locale

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

        val formatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEE, MMM d")
        val contDescFormatPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEE, MMM d")
        checkInDateView.text = itinCardDataHotel.startDate.toString(formatPattern)
        checkInDateView.contentDescription = itinCardDataHotel.startDate.toString(contDescFormatPattern)
        checkOutDateView.text = itinCardDataHotel.endDate.toString(formatPattern)
        checkOutDateView.contentDescription = itinCardDataHotel.endDate.toString(contDescFormatPattern)
        checkInTimeView.text = itinCardDataHotel.checkInTime?.toLowerCase()
        checkOutTimeView.text = itinCardDataHotel.checkOutTime?.toLowerCase()
        if (!itinCardDataHotel.property.checkInPolicies.isEmpty()) {
            checkInPoliciesDivider.visibility = View.VISIBLE
            checkInOutPoliciesContainer.visibility = View.VISIBLE
            checkInOutPoliciesContainer.setOnClickListener {
                val fragmentManager = (context as FragmentActivity).supportFragmentManager
                val dialog = ScrollableContentDialogFragment.newInstance(context.resources.getString(R.string.itin_hotel_check_in_policies_dialog_heading),
                        TextUtils.join("\n", itinCardDataHotel.property.checkInPolicies).toString())
                dialog.show(fragmentManager, DIALOG_TAG)
                OmnitureTracking.trackHotelItinCheckInPoliciesDialogClick()
            }
            checkInOutPoliciesButtonText.setCompoundDrawablesTint(ContextCompat.getColor(context, R.color.app_primary))
        }
    }
}