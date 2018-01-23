package com.expedia.bookings.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.ArrayList

// Spinner needs to set selection to count initially to show hint.
class SpinnerAdapterWithHint(val context: Context, val hint: String, val itemLayout: Int,
                             val dropDownLayout: Int? = null, val dropDownTextResourceId: Int? = null) : BaseAdapter() {
    var optionsWithHint = ArrayList<SpinnerItem>()

    init {
        addHint()
    }

    private fun addHint() {
        optionsWithHint.add(SpinnerItem(hint, Any()))
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(itemLayout, parent, false)
        }

        val displayText = getItem(position).value
        if (position == count) {
            (view as TextView).text = ""
            view.hint = displayText
        } else {
            (view as TextView).text = displayText
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {
            val layoutId = dropDownLayout ?: itemLayout
            view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        }
        if (dropDownTextResourceId != null) {
            (view?.findViewById<TextView>(dropDownTextResourceId))?.text = getItem(position).value
        } else {
            (view as TextView).text = getItem(position).value
        }

        return view
    }

    override fun getItem(position: Int): SpinnerItem {
        return optionsWithHint[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return optionsWithHint.size - 1
    }

    data class SpinnerItem(val value: String, val item: Any)

    fun dataSetChanged(options: List<SpinnerItem>) {
        optionsWithHint.clear()
        optionsWithHint.addAll(options)
        addHint()
        notifyDataSetChanged()
    }
}
