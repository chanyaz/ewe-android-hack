package com.expedia.bookings.extensions

import android.widget.CompoundButton
import io.reactivex.Observer

fun CompoundButton.subscribeOnClick(observer: Observer<Boolean>) {
    this.setOnClickListener {
        observer.onNext(this.isChecked)
    }
}

fun CompoundButton.subscribeOnCheckChanged(observer: Observer<Boolean>) {
    this.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
        observer.onNext(isChecked)
    }
}
