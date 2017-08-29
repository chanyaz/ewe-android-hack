package com.expedia.bookings.dialog

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.widget.TextView

object FlightCheckInDialogBuilder {

    @JvmStatic
    fun onCreateDialog(context: Context, airlineName: String, airlineCode: String, confirmationCode: String, isSplitTicket: Boolean, flightLegs: Int): AlertDialog {
        val dialogLayout = (context as Activity).layoutInflater.inflate(R.layout.itin_check_in_dialog, null)
        val noText = dialogLayout.findViewById(R.id.itin_checkin_no) as TextView
        val dialogMsg = dialogLayout.findViewById(R.id.itin_checkin_msg) as TextView
        val dialogTitle = dialogLayout.findViewById(R.id.dialog_title) as TextView
        val dialogBody = dialogLayout.findViewById(R.id.dialog_body) as ViewGroup
        val yesText = dialogLayout.findViewById(R.id.itin_checkin_yes) as TextView
        dialogMsg.text = context.getString(R.string.itin_checkin_dialog_message, airlineName)
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(dialogLayout)

        val alertDialog = builder.create()
        yesText.setOnClickListener {
            if (dialogTitle.text.toString().equals(context.getString(R.string.itin_checkin_failure_dialog_title), ignoreCase = true)) {
                ClipboardUtils.setText(context, confirmationCode)
            } else {
                OmnitureTracking.trackItinFlightCheckInSuccess(airlineCode, isSplitTicket, flightLegs)
            }
            alertDialog.dismiss()
        }
        noText.setOnClickListener {
            OmnitureTracking.trackItinFlightCheckInFailure(airlineCode, isSplitTicket, flightLegs)
            val fadeOutAnim = ObjectAnimator.ofFloat(dialogBody, "alpha", 1.0f, 0.0f)
            val fadeInAnim = ObjectAnimator.ofFloat(dialogBody, "alpha", 0.0f, 1.0f)

            fadeOutAnim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    builder.setCancelable(true)
                    noText.visibility = View.GONE
                    yesText.text = context.getString(R.string.ok)
                    dialogTitle.text = context.getString(R.string.itin_checkin_failure_dialog_title)
                    dialogMsg.text = context.getString(R.string.itin_checkin_failure_dialog_message, airlineName)
                    fadeInAnim.setDuration(300).start()
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            fadeOutAnim.setDuration(300).start()
        }
        return alertDialog
    }

}
