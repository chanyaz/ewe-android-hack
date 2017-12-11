package com.expedia.bookings.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.mobiata.android.SocialUtils

class FilghtsBrowserSoftPromptDialogFragment : DialogFragment() {

    val flightsDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_dialog_soft_prompt, null)
        view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view = flightsDialogView
        val planeImage = view.findViewById<ImageView>(R.id.dialog_image)
        val titleTextView = view.findViewById<TextView>(R.id.title)
        val descriptionTextView = view.findViewById<TextView>(R.id.subtitle)
        val openBrowserTextView = view.findViewById<TextView>(R.id.subtitle_openbrowser)
        val enableButton = view.findViewById<Button>(R.id.soft_prompt_enable_button)
        val dismissButton = view.findViewById<Button>(R.id.soft_prompt_disable_text)

        planeImage.setImageResource(R.drawable.india_reboot_plane)
        titleTextView.text = context.resources.getString(R.string.flights_browser_view_soft_prompt_title)
        descriptionTextView.text = context.resources.getString(R.string.flights_browser_viewsoft_prompt_subtitle)
        enableButton.text = context.resources.getString(R.string.flights_browser_viewsoft_prompt_enable)
        openBrowserTextView.text = context.resources.getString(R.string.flights_browser_viewsoft_prompt_openbrowser)
        openBrowserTextView.visibility = View.VISIBLE
        enableButton.setOnClickListener {
            onLaunchBrowserView()
            dismiss()
        }
        dismissButton.setOnClickListener {
            dismiss()
        }

        alertDialogBuilder.setView(view)
        val dialog =  alertDialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    fun onLaunchBrowserView() {
        SocialUtils.openSite(context, "https://www.${ PointOfSale.getPointOfSale().url}/Flights")
    }

}