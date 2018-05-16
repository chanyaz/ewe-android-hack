package com.expedia.bookings.hotel.widget

import android.content.Context
import android.os.Handler
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.utils.bindView
import com.mobiata.android.widget.SpinnerWithCloseListener
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList

interface OnHotelSortChangedListener {
    fun onHotelSortChanged(displaySort: DisplaySort, doTracking: Boolean)
}

class HotelSortOptionsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val downEventSubject = PublishSubject.create<Unit>()

    @VisibleForTesting
    val sortByButtonGroup: SpinnerWithCloseListener by bindView(R.id.sort_by_selection_spinner)

    private var listener: OnHotelSortChangedListener? = null

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

    private val sortSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
            onSortItemSelected(position, true)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
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

        addSortSelectedListener()

        sortByButtonGroup.setOnSpinnerCloseListener {
            requestAccessibilityFocusOnSortBySpinner()
        }

        sortByButtonGroup.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                downEventSubject.onNext(Unit)
            }
            false
        }
    }

    fun setOnHotelSortChangedListener(listener: OnHotelSortChangedListener?) {
        this.listener = listener
    }

    fun updateSortItems(sortList: List<DisplaySort>) {
        sortByAdapter.clear()
        sortByAdapter.addAll(sortList)
        sortByAdapter.notifyDataSetChanged()
        removeSortSelectedListener()
        val defaultPosition = 0
        sortByButtonGroup.setSelection(defaultPosition)
        onSortItemSelected(defaultPosition, false)
        addSortSelectedListener()
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
        removeSortSelectedListener()
        sortByButtonGroup.setSelection(position)
        onSortItemSelected(position, false)
        addSortSelectedListener()
    }

    private fun requestAccessibilityFocusOnSortBySpinner() {
        Handler().postDelayed({
            clearFocus()
            sortByButtonGroup.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            sortByButtonGroup.requestFocus()
        }, 500)
    }

    private fun addSortSelectedListener() {
        sortByButtonGroup.onItemSelectedListener = sortSelectedListener
    }

    private fun removeSortSelectedListener() {
        sortByButtonGroup.onItemSelectedListener = null
    }

    private fun onSortItemSelected(position: Int, doTracking: Boolean) {
        sortByButtonGroup.contentDescription = Phrase.from(context, R.string.filter_sort_by_content_description_TEMPLATE)
                .put("sort", resources.getString(sortByAdapter.getItem(position).resId)).format().toString()
        val displaySort = sortByAdapter.getItem(position)
        listener?.onHotelSortChanged(displaySort, doTracking)
    }
}
