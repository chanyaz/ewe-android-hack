package com.expedia.account.util

import android.content.DialogInterface
import android.support.annotation.StringRes

interface SimpleDialogBuilder {
    fun showSimpleDialog(@StringRes titleResId: Int, @StringRes messageResId: Int, @StringRes buttonLabelResId: Int, buttonClickListener: DialogInterface.OnClickListener? = null)
    fun showDialogWithItemList(title: CharSequence, items: Array<CharSequence?>, itemClickListener: DialogInterface.OnClickListener?)
}
