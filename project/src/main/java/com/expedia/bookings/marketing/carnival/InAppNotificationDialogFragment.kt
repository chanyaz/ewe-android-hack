package com.expedia.bookings.marketing.carnival

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.CardView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.utils.Constants
import com.squareup.picasso.Picasso

class InAppNotificationDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.fragment_dialog_in_app_notification, null)
        val callToActionShopDealButton = view.findViewById<CardView>(R.id.in_app_cta_shop_deal_button)
        val callToActionShopDealButtonText = view.findViewById<TextView>(R.id.in_app_cta_shop_deal_text_view)
        val callToActionCancelButton = view.findViewById<Button>(R.id.in_app_cta_cancel_button)
        val imageView = view.findViewById<ImageView>(R.id.in_app_dialog_image)
        val title = view.findViewById<TextView>(R.id.in_app_title)
        val body = view.findViewById<TextView>(R.id.in_app_main_text)
        val carnivalMessage = this.arguments.get(Constants.CARNIVAL_MESSAGE_DATA) as CarnivalMessage

        val fullPageDealViewModel = FullPageDealViewModel(carnivalMessage)

        if (fullPageDealViewModel.imageUrl != null) {
            Picasso.with(this.context).load(Uri.parse(fullPageDealViewModel.imageUrl)).into(imageView)
        }

        title.text = fullPageDealViewModel.title
        body.text = fullPageDealViewModel.text
        callToActionShopDealButtonText.text = fullPageDealViewModel.shopDealsButtonLabel
        callToActionCancelButton.text = fullPageDealViewModel.cancelDealsButtonLabel

        if (callToActionShopDealButtonText.text.isNullOrEmpty()) {
            callToActionShopDealButtonText.text = resources.getString(R.string.ok)
        }

        if (callToActionCancelButton.text.isNullOrEmpty()) {
            callToActionCancelButton.visibility = View.GONE
        }

        callToActionShopDealButton.setOnClickListener {
            val intent = Intent(context, FullPageDealNotificationActivity::class.java)
            intent.putExtra(Constants.CARNIVAL_MESSAGE_DATA, carnivalMessage)
            startActivity(intent)
            dismiss()
        }

        callToActionCancelButton.setOnClickListener {
            dismiss()
        }

        alertDialogBuilder.setView(view)
        val dialog = alertDialogBuilder.create()
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }
}
