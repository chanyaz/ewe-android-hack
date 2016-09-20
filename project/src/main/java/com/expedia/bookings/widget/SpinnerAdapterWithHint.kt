package com.expedia.bookings.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache

// Spinner needs to set selection to count initially to show hint.
class SpinnerAdapterWithHint(val context: Context, options: List<SpinnerItem>, hint: String) : BaseAdapter() {
    val optionsWithHint = options.toMutableList()

    init {
        optionsWithHint.add(SpinnerItem(hint, Any()))
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.snippet_rail_card_text_view, parent, false)
        }

        FontCache.setTypeface(view as TextView, FontCache.Font.ROBOTO_LIGHT)
        val displayText = getItem(position).value
        if (position == count) {
            view.text = ""
            view.hint = displayText
        }
        else {
            view.text = displayText
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

}