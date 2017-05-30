package com.expedia.bookings.hotel.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject
import java.util.ArrayList

class HotelSortOptionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val sortSelectedSubject = PublishSubject.create<DisplaySort>()
    val downEventSubject = PublishSubject.create<Unit>()

    private val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)

    private val sortByAdapter = object : ArrayAdapter<DisplaySort>(getContext(), R.layout.spinner_sort_dropdown_item) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView: TextView = super.getView(position, convertView, parent) as TextView
            textView.text = resources.getString(getItem(position).resId)
            return textView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return getView(position, convertView, parent)
        }
    }

    init {
        View.inflate(context, R.layout.hotel_sort_options_view, this)

        if (!isInEditMode) {
            sortByAdapter.setNotifyOnChange(false)
            sortByButtonGroup.adapter = sortByAdapter
            val sortList = DisplaySort.values().toMutableList()
            updateSortItems(sortList)
        }

        sortByButtonGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                sortSelectedSubject.onNext(sortByAdapter.getItem(position))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        sortByButtonGroup.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                downEventSubject.onNext(Unit)
            }
            false
        }
    }

    fun updateSortItems(sortList: List<DisplaySort>) {
        sortByAdapter.clear()
        sortByAdapter.addAll(sortList)
        sortByAdapter.notifyDataSetChanged()
        sortByButtonGroup.setSelection(0, false)
    }

    //exposed for testing
    fun getSortItems(): ArrayList<DisplaySort> {
        val array: ArrayList<DisplaySort> = arrayListOf()
        for (i in 1..sortByAdapter.count)
            array.add(sortByAdapter.getItem(i - 1))
        return array
    }

    fun setSort(sort: DisplaySort) {
        val position = sortByAdapter.getPosition(sort)
        sortByButtonGroup.setSelection(position, false)
    }
}