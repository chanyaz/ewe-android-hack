package com.expedia.bookings.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.squareup.phrase.Phrase

class DialogFactory {

    companion object {
        fun showNoInternetRetryDialog(context: Context, retryFun: () -> Unit, cancelFun: () -> Unit) {
            if (isContextValidActivity(context)) {
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

        fun createLogoutDialog(context: Context, logoutFun: () -> Unit): AlertDialog {
            val builder = AlertDialog.Builder(context)
            var messageText = Phrase.from(context, R.string.sign_out_confirmation_TEMPLATE)
                    .put("brand", BuildConfig.brand)
                    .format().toString()
            builder.setMessage(messageText)
            builder.setCancelable(false)
            builder.setPositiveButton(context.getString(R.string.sign_out), { dialog, which ->
                logoutFun()
                OmnitureTracking.trackLogOutAction(OmnitureTracking.LogOut.SUCCESS)
            })
            builder.setNegativeButton(context.getString(R.string.cancel), { dialog, which ->
                dialog.dismiss()
                OmnitureTracking.trackLogOutAction(OmnitureTracking.LogOut.CANCEL)
            })
            return builder.create()
        }

        private fun isContextValidActivity(context: Context): Boolean {
            if (context is Activity) {
                return !context.isFinishing && !context.isDestroyed
            }
            return true
        }
    }
}
