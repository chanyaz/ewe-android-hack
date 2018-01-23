package com.expedia.bookings.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.UserReviewDialogViewModel

class UserReviewRatingDialog(context: Context) {

    val dialog: Dialog by lazy {
        val dialog = AlertDialog.Builder(context).create()
        dialog.setView(ratingDialogView)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog
    }

    var viewModel: UserReviewDialogViewModel by notNullAndObservable { vm ->
        vm.closeSubject.subscribe {
            dialog.dismiss()
        }
    }

    val ratingDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_rating_dialog, null)
        val reviewBtn = view.findViewById<Button>(R.id.review_btn)
        val feedbackBtn = view.findViewById<Button>(R.id.feedback_btn)
        val noThanksBtn = view.findViewById<Button>(R.id.no_btn)

        reviewBtn.subscribeOnClick(viewModel.reviewSubject)
        feedbackBtn.subscribeOnClick(viewModel.feedbackSubject)
        noThanksBtn.subscribeOnClick(viewModel.noSubject)

        view
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun hide() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
