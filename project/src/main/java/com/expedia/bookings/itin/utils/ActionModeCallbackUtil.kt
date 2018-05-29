package com.expedia.bookings.itin.utils

import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

object ActionModeCallbackUtil {

    fun getActionModeCallBackWithoutPhoneNumberMenuItem(): ActionMode.Callback {
        val callback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    menu?.removeItem(android.R.id.textAssist)
                    return true
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }
        }
        return callback
    }

    fun getActionModeCallbackWithPhoneNumberClickAction(phoneNumberClicked: () -> Unit): ActionMode.Callback {
        val callback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                item?.itemId?.let {
                    if (it == android.R.id.textAssist) phoneNumberClicked()
                }
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }
        }
        return callback
    }
}
