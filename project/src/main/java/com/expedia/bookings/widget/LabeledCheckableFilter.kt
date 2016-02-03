package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import rx.Observer

class LabeledCheckableFilter<T>(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val label: TextView by bindView(R.id.label)
    val checkBox: CheckBox by bindView(R.id.check_box)
    var observer: Observer<T> ? = null
    var value: T? = null

    val checkObserver: Observer<Unit> = endlessObserver {
        checkBox.isChecked = !checkBox.isChecked
        observer?.onNext(value)
        if (checkBox.isChecked) onChecked()
    }

    fun bind(filterName: String, filterValue: T, observer: Observer<T>) {
        this.observer = observer
        label.text = filterName
        value = filterValue
        checkBox.isChecked = false
    }

    /*
    * Implement in case the view should react on checked state
    */
    fun onChecked() {
    }
}
