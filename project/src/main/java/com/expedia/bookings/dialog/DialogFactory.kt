package com.expedia.bookings.dialog

import android.app.AlertDialog
import android.content.Context
import com.expedia.bookings.R

class DialogFactory {

    companion object {
        fun showNoInternetRetryDialog(context: Context, retryFun: () -> Unit, cancelFun: () -> Unit) {
            val b = AlertDialog.Builder(context)
            b.setCancelable(false)
                    .setMessage(context.resources.getString(R.string.error_no_internet))
                    .setPositiveButton(context.resources.getString(R.string.retry)) { dialog, which ->
                        dialog.dismiss()
                        retryFun()
                    }
                    .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                        cancelFun()
                    }
                    .show()
        }
    }
}
