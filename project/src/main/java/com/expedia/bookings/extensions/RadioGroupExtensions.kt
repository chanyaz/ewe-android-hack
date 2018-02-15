package com.expedia.bookings.extensions

import android.widget.RadioGroup
import io.reactivex.Observer

fun RadioGroup.subscribeOnCheckChanged(observer: Observer<Int>) {
    this.setOnCheckedChangeListener { radioGroup: RadioGroup, isChecked: Int ->
        observer.onNext(isChecked)
    }
}
