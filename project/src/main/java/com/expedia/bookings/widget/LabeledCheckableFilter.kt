package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.Observer

class LabeledCheckableFilter<T>(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    val stopsLabel: TextView by bindView(R.id.label)
    val resultsLabel: TextView by bindView(R.id.results_label)
    val checkBox: CheckBox by bindView(R.id.check_box)
    var observer: Observer<T> ? = null
    var value: T? = null

    val checkObserver: Observer<Unit> = endlessObserver {
        checkBox.isChecked = !checkBox.isChecked
        value?.let {
            observer?.onNext(it)
        }
        if (checkBox.isChecked) onChecked()
        refreshContentDescription()
    }

    fun bind(filterName: String, filterValue: T, filterResults: Int?, observer: Observer<T>) {
        this.observer = observer
        stopsLabel.text = filterName
        value = filterValue
        resultsLabel.text = filterResults.toString()
        checkBox.isChecked = false
        refreshContentDescription()
    }

    fun bind(filterName: String, filterValue: T, filterResults: Int?) {
        stopsLabel.text = filterName
        value = filterValue
        resultsLabel.text = filterResults.toString()
        checkBox.isChecked = true
        checkBox.isEnabled = false
        setDisabledContentDescription()
        this.isClickable = false
    }

    fun refreshContentDescription() {
        val contentDesc = StringBuilder(Phrase.from(context, R.string.packages_flight_filter_checkbox_cont_desc_TEMPLATE)
                .put("filter_name", stopsLabel.text)
                .put("filter_results", resultsLabel.text)
                .format().toString())
        if (checkBox.isChecked) {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_checked))
        }
        else {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_unchecked))
        }
        this.contentDescription = contentDesc
    }

    fun setDisabledContentDescription() {
        val contentDesc = StringBuilder(Phrase.from(context, R.string.packages_flight_filter_checkbox_disabled_cont_desc_TEMPLATE)
                .put("filter_name", stopsLabel.text)
                .put("filter_results", resultsLabel.text)
                .format().toString())
        if (checkBox.isChecked) {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_checked))
        }
        else {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_unchecked))
        }
        this.contentDescription = contentDesc
    }

    /*
    * Implement in case the view should react on checked state
    */
    fun onChecked() {
    }
}
