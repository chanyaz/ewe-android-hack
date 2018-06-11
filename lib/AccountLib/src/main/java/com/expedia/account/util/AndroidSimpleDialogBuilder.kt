package com.expedia.account.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.annotation.StringRes

class AndroidSimpleDialogBuilder(private val context: Context) : SimpleDialogBuilder {
    override fun showSimpleDialog(@StringRes titleResId: Int, @StringRes messageResId: Int, @StringRes buttonLabelResId: Int, buttonClickListener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(context)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(buttonLabelResId, buttonClickListener)
                .create()
                .show()
    }

    override fun showDialogWithItemList(title: CharSequence, items: Array<CharSequence?>, itemClickListener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(items, itemClickListener)
                .create()
                .show()
    }
}
