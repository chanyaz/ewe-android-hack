package com.expedia.bookings.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.util.PermissionsUtils.requestLocationPermission

class SoftPromptDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view = activity!!.layoutInflater.inflate(R.layout.fragment_dialog_soft_prompt, null)
        val enableButton = view.findViewById<Button>(R.id.soft_prompt_enable_button)

        enableButton.setOnClickListener {
            activity?.let {
                requestLocationPermission(it)
            }
            OmnitureTracking.trackLocationSoftPrompt(true)
            dismiss()
        }
        val dismissButton = view.findViewById<Button>(R.id.soft_prompt_disable_text)
        dismissButton.setOnClickListener {
            OmnitureTracking.trackLocationSoftPrompt(false)
            if (activity is PhoneLaunchActivity) {
                (activity as PhoneLaunchActivity).isLocationPermissionPending = false
            }
            dismiss()
        }

        alertDialogBuilder.setView(view)
        val dialog = alertDialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}
