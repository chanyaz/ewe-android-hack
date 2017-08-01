package com.expedia.vm

import android.content.Context
import android.view.View
import android.widget.EditText
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.widget.ExpandableCardView
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class CheckoutToolbarViewModel(val context: Context) {

    // inputs
    val toolbarTitle = PublishSubject.create<String>()
    val toolbarSubtitle = PublishSubject.create<String>()
    val menuTitle = PublishSubject.create<String>()
    val formFilledIn = PublishSubject.create<Boolean>()
    val enableMenuItem = PublishSubject.create<Boolean>()
    val menuVisibility = PublishSubject.create<Boolean>()
    val visibleMenuWithTitleDone = PublishSubject.create<Unit>()
    val currentFocus = PublishSubject.create<View>()
    val toolbarNavIcon = PublishSubject.create<ArrowXDrawableUtil.ArrowDrawableType>()
    val toolbarNavIconContentDesc = PublishSubject.create<String>()
    val toolbarCustomTitle = PublishSubject.create<String>()

    // outputs
    val doneClicked = PublishSubject.create<Unit>()
    val nextClicked = PublishSubject.create<Unit>()

    val expanded = PublishSubject.create<ExpandableCardView>()
    val closed = PublishSubject.create<Unit>()
}