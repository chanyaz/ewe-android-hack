package com.expedia.bookings.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.model.CustomPojo

open class CustomListAdapter(data: Array<CustomPojo>, context: Context) : BaseAdapter()  {

    var localData = data

    var mInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return localData.size
    }

    override fun getItem(position: Int): Any {
        return localData.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        val view: View?
        val vh: ListRowHolder
        if (convertView == null) {
            view = mInflater?.inflate(R.layout.row_item, parent, false)
            vh = ListRowHolder(view)
            view?.setTag(vh)
        } else {
            view = convertView
            vh = view.getTag() as ListRowHolder
        }
        vh.label.setText(localData.get(index = position).toString())
        return view;
    }

    private class ListRowHolder(row: View?) {
        val label: TextView

        init {
            this.label = getLabel(row)
        }

        private fun getLabel(row: View?): TextView {
            return row?.findViewById<View>(R.id.label12) as TextView
        }
    }

}