package com.expedia.bookings.marketing.carnival

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.carnival.sdk.Message
import com.expedia.bookings.R
import com.expedia.bookings.utils.Constants
import com.squareup.picasso.Picasso

class InAppNotificationDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.fragment_dialog_in_app_notification, null)
        val ctaButton1 = view.findViewById<TextView>(R.id.in_app_cta_button1_text_view)
        val ctaButton2 = view.findViewById<Button>(R.id.in_app_cta_button2)
        val imageView = view.findViewById<ImageView>(R.id.in_app_dialog_image)
        val title = view.findViewById<TextView>(R.id.in_app_title)
        val body = view.findViewById<TextView>(R.id.in_app_main_text)
        val carnivalMessage = this.arguments.get(Constants.CARNIVAL_MESSAGE_DATA) as Message

        if (carnivalMessage.imageURL != null) {
            Picasso.with(this.context).load(Uri.parse(carnivalMessage.imageURL)).into(imageView)
        }

        title.text = carnivalMessage.title
        body.text = carnivalMessage.text
        ctaButton1.text = carnivalMessage.attributes.get(Constants.CARNIVAL_IN_APP_BUTTON1_LABEL)
        ctaButton2.text = carnivalMessage.attributes.get(Constants.CARNIVAL_IN_APP_BUTTON2_LABEL)

        if (ctaButton1.text.isNullOrEmpty()) {
            ctaButton1.text = resources.getString(R.string.see_deal)
        }

        if (ctaButton2.text.isNullOrEmpty()) {
            ctaButton2.text = resources.getString(R.string.no_thanks)
        }

        ctaButton1.setOnClickListener {
            //TODO Hookup to Deals page here.
        }

        ctaButton2.setOnClickListener {
            dismiss()
        }

        alertDialogBuilder.setView(view)
        val dialog = alertDialogBuilder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}
