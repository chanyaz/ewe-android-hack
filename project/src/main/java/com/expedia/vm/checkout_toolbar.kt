package com.expedia.vm

import android.content.Context
import rx.subjects.PublishSubject

public class CheckoutToolbarViewModel(val context: Context) {

    // inputs
    val toolbarTitle = PublishSubject.create<String>()
    val menuTitle = PublishSubject.create<String>()
    val enableMenu = PublishSubject.create<Boolean>()

    // outputs
    val doneClicked = PublishSubject.create<Unit>()
    val nextClicked = PublishSubject.create<Unit>()
}