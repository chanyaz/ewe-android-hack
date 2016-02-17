package com.expedia.vm

import android.content.Context
import android.widget.EditText
import com.expedia.bookings.widget.ExpandableCardView
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public class CheckoutToolbarViewModel(val context: Context) {

    // inputs
    val toolbarTitle = PublishSubject.create<String>()
    val toolbarSubtitle = PublishSubject.create<String>()
    val menuTitle = PublishSubject.create<String>()
    val enableMenu = PublishSubject.create<Boolean>()
    val enableMenuDone = PublishSubject.create<Boolean>()
    val editText = PublishSubject.create<EditText>()

    // outputs
    val doneClicked = PublishSubject.create<Unit>()
    val nextClicked = PublishSubject.create<Unit>()

    val expanded = PublishSubject.create<ExpandableCardView>()
    val closed = PublishSubject.create<Unit>()
    val showChangePackageMenuObservable = BehaviorSubject.create<Boolean>()
}