package com.expedia.bookings.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.UserReferralDialogViewModel

class UserReferralDialog(context: Context) {

    val dialog: Dialog by lazy {
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(ratingDialogView)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog
    }

    var viewModel: UserReferralDialogViewModel by notNullAndObservable { vm ->
        vm.closeSubject.subscribe {
            dialog.dismiss()
        }
    }

    val ratingDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_rating_dialog, null)
        val title = view.findViewById(R.id.title_text) as TextView
        val reviewBtn = view.findViewById(R.id.review_btn) as Button
        val feedbackBtn = view.findViewById(R.id.feedback_btn) as Button
        val noThanksBtn = view.findViewById(R.id.no_btn) as Button

        reviewBtn.subscribeOnClick(viewModel.reviewSubject)
        feedbackBtn.subscribeOnClick(viewModel.feedbackSubject)
        noThanksBtn.subscribeOnClick(viewModel.noSubject)

        viewModel.titleTextSubject.subscribeText(title)
        viewModel.reviewTextSubject.subscribeText(reviewBtn)
        viewModel.feedbackTextSubject.subscribeText(feedbackBtn)
        viewModel.closeTextSubject.subscribeText(noThanksBtn)

        viewModel.bindText()

        view
    }

    fun show() {
        if (!dialog.isShowing) {
            viewModel.bindText()
            dialog.show()
        }
    }

    fun hide() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
