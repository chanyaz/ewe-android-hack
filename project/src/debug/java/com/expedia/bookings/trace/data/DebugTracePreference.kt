package com.expedia.bookings.trace.data

import android.content.Context
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.widget.CheckBox
import com.expedia.bookings.R

class DebugTracePreference(context: Context, val debugTraceData: DebugTraceData) : Preference(context) {

    private var debugTraceCheckBox: CheckBox? = null

    init {
        layoutResource = R.layout.debug_trace_preference
        title = debugTraceData.url
        summary = context.getString(R.string.trace_id) + debugTraceData.traceId
    }

    fun updateSelected(selected: Boolean) {
        debugTraceData.selected = selected
        debugTraceCheckBox?.isChecked = selected
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        debugTraceCheckBox = holder?.findViewById(R.id.debug_trace_check_box) as? CheckBox
        debugTraceCheckBox?.isChecked = debugTraceData.selected
    }
}
