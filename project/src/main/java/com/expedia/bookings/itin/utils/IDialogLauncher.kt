package com.expedia.bookings.itin.utils

import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager

interface IDialogLauncher {
    fun show(dialog: DialogFragment, manager: FragmentManager, tag: String) {
        DialogFragment().show(manager, tag)
    }
}
