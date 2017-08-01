package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeOnClick
import com.expedia.vm.flights.AdvanceSearchFilter
import io.reactivex.Observer

class AdvanceSearchCheckableFilter(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    val filterLabel: TextView by bindView(R.id.filter_label)
    val checkBox: CheckBox by bindView(R.id.filter_check_box)
    var observer: Observer<AdvanceSearchFilter>? = null
    lateinit var filter: AdvanceSearchFilter

    val checkObserver: Observer<Unit> = endlessObserver {
        checkBox.isChecked = !checkBox.isChecked
        filter.isChecked = checkBox.isChecked
        observer!!.onNext(filter)
        refreshContentDescription()
    }

    fun bind(filter: AdvanceSearchFilter, observer: Observer<AdvanceSearchFilter>) {
        filterLabel.text = context.getString(filter.resId)
        checkBox.isChecked = false
        this.filter = filter
        subscribeOnClick(checkObserver)
        this.observer = observer
        refreshContentDescription()
    }

    fun refreshContentDescription() {
        val contentDesc = StringBuilder(filterLabel.text)
        if (checkBox.isChecked) {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_checked))
        } else {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_unchecked))
        }
        this.contentDescription = contentDesc
    }
}
