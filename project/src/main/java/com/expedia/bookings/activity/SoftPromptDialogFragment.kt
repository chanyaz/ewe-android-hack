package com.expedia.bookings.activity


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.util.requestLocationPermission

class SoftPromptDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.fragment_dialog_soft_prompt, null)

        val enableButton = view.findViewById(R.id.soft_prompt_enable_button) as Button
        enableButton.setOnClickListener { requestLocationPermission(activity) }
        val dismissButton = view.findViewById(R.id.soft_prompt_disable_text) as Button
        dismissButton.setOnClickListener { dismiss() }

        alertDialogBuilder.setView(view)
        val dialog =  alertDialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

}
