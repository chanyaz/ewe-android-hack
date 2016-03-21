package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import rx.Observer

class LabeledCheckableFilter<T>(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    val stopsLabel: TextView by bindView(R.id.label)
    val resultsLabel: TextView by bindView(R.id.results_label)
    val checkBox: CheckBox by bindView(R.id.check_box)
    var observer: Observer<T> ? = null
    var value: T? = null

    val checkObserver: Observer<Unit> = endlessObserver {
        checkBox.isChecked = !checkBox.isChecked
        observer?.onNext(value)
        if (checkBox.isChecked) onChecked()
    }

    fun bind(filterName: String, filterValue: T, filterResults: Int?, observer: Observer<T>) {
        this.observer = observer
        stopsLabel.text = filterName
        value = filterValue
        resultsLabel.text = filterResults.toString()
        checkBox.isChecked = false
    }

    /*
    * Implement in case the view should react on checked state
    */
    fun onChecked() {
    }
}
