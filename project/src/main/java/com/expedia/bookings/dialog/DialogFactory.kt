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
            showRetryCancelDialog(context, context.getString(R.string.error_no_internet),
                    retryFun, cancelFun)
        }

        fun showTimeoutDialog(context: Context, retryFun: () -> Unit, cancelFun: () -> Unit) {
            val message = Phrase.from(context, R.string.error_server_TEMPLATE)
                    .put("brand", BuildConfig.brand).format().toString()

            showRetryCancelDialog(context, message, retryFun, cancelFun)
        }

        fun createLogoutDialog(context: Context, logoutFun: () -> Unit): AlertDialog {
            val builder = AlertDialog.Builder(context)
            val messageText = Phrase.from(context, R.string.sign_out_confirmation_TEMPLATE)
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

        private fun showRetryCancelDialog(context: Context, message: String,
                                          retryFun: () -> Unit, cancelFun: () -> Unit) {
            if (isContextValidActivity(context)) {
                val b = AlertDialog.Builder(context)
                b.setCancelable(false)
                        .setMessage(message)
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

        private fun isContextValidActivity(context: Context): Boolean {
            if (context is Activity) {
                return !context.isFinishing && !context.isDestroyed
            }
            return true
        }
    }
}
