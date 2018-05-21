package com.expedia.bookings.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.TextView
import com.mobiata.android.SocialUtils

class ItinModifyReservationDialog : DialogFragment() {
    companion object {
        val SUPPORT_NUMBER = "support_number"
        val CONTENT_KEY = "content"
        val ITIN_TYPE = "ITIN_TYPE"
        val FLIGHT_ITIN = "FLIGHT_ITIN"
        val HOTEL_ITIN = "HOTEL_ITIN"

        @JvmStatic
        fun newInstance(content: String, supportNumber: String, itinType: String): ItinModifyReservationDialog {
            val fragment = ItinModifyReservationDialog()
            val arguments = Bundle()
            arguments.putString(SUPPORT_NUMBER, supportNumber)
            arguments.putString(CONTENT_KEY, content)
            arguments.putString(ITIN_TYPE, itinType)
            fragment.arguments = arguments
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = getViewForDialog()
        setUpDialogView(view)
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(view)
        return alertDialogBuilder.create()
    }

    private fun setUpDialogView(view: View) {
        val contentText = view.findViewById<TextView>(R.id.dialog_text_content)
        val customerSupportButton = view.findViewById<TextView>(R.id.dialog_customer_support)
        val goBackButton = view.findViewById<TextView>(R.id.dialog_go_back)
        contentText.text = arguments.getString(CONTENT_KEY)
        val supportNumber = arguments.getString(SUPPORT_NUMBER)

        customerSupportButton.visibility = if (Strings.isEmpty(supportNumber)) View.GONE else View.VISIBLE
        AccessibilityUtil.appendRoleContDesc(customerSupportButton, customerSupportButton.text.toString(), R.string.accessibility_cont_desc_role_button)
        AccessibilityUtil.appendRoleContDesc(goBackButton, goBackButton.text.toString(), R.string.accessibility_cont_desc_role_button)
        setListenerForCustomerSupportView(customerSupportButton, supportNumber)
        setListenerForGoBackButton(goBackButton)
    }

    @SuppressLint("InflateParams")
    private fun getViewForDialog(): View {
        val layoutInflater = activity.layoutInflater
        return layoutInflater.inflate(R.layout.fragment_dialog_flight_modify_reservation, null)
    }

    private fun onCustomerSupportCallButtonClick(supportNumber: String) {
        if (Strings.isNotEmpty(supportNumber)) {
            val pm = context.packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                SocialUtils.call(context, supportNumber)
            } else {
                ClipboardUtils.setText(context, supportNumber)
                Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        }
        val itinType = arguments.getString(ITIN_TYPE)

        when (itinType) {
            FLIGHT_ITIN -> OmnitureTracking.trackItinFlightCallSupport()
            HOTEL_ITIN -> TripsTracking.trackItinHotelCallSupport()
        }
    }

    private fun setListenerForCustomerSupportView(view: TextView, number: String) {
        view.setOnClickListener {
            onCustomerSupportCallButtonClick(number)
        }
    }

    private fun setListenerForGoBackButton(view: TextView) {
        view.setOnClickListener {
            dismiss()
        }
    }
}
