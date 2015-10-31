package com.expedia.bookings.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.expedia.bookings.R

public class DialogFactory {

    companion object {
        fun showNoInternetRetryDialog(context: Context, retryFun:() -> Unit, cancelFun:() -> Unit) {
            val b = AlertDialog.Builder(context)
            b.setCancelable(false)
                    .setMessage(context.resources.getString(R.string.error_no_internet))
                    .setPositiveButton(context.resources.getString(R.string.retry), object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                            retryFun()
                        }
                    }).setNegativeButton(context.resources.getString(R.string.cancel), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                    cancelFun()
                }
            }).show()
        }
    }
}
