package com.expedia.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.widget.ExpandableCardView
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
    val hideToolbarTitle = PublishSubject.create<Unit>()

    // outputs
    val nextClicked = PublishSubject.create<Unit>()
    val doneClickedMethod = BehaviorSubject.create<() -> Unit>()
    val overflowClicked = PublishSubject.create<Unit>()

    val expanded = PublishSubject.create<ExpandableCardView>()
    val closed = PublishSubject.create<Unit>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onMenuItemClicked(menuButtonTitle: CharSequence): Boolean {
        when (menuButtonTitle) {
            context.getString(R.string.next) -> {
                nextClicked.onNext(Unit)
            }
            context.getString(R.string.done),
            context.getString(R.string.coupon_apply_button),
            context.getString(R.string.coupon_apply_button_ally),
            context.getString(R.string.coupon_submit_button),
            context.getString(R.string.coupon_submit_button_ally) -> {
                doneClickedMethod.value?.invoke()
            }
        }
        return true
    }
}