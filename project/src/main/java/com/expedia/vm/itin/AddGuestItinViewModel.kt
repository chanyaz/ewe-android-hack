package com.expedia.vm.itin

import android.app.Activity
import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase

class AddGuestItinViewModel(val context: Context) {
    var searchClickSubject = endlessObserver<Unit> {
        println("Supreeth AddGuestItinViewModel searchClickSubject")
    }
}