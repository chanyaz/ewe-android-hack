package com.expedia.vm.itin

import android.content.Context
import com.expedia.util.endlessObserver

class AddGuestItinViewModel(val context: Context) {
    var searchClickSubject = endlessObserver<Unit> {
    }
}