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

    var list = ArrayList<String>()
    var headerPos = ArrayList<Number>()
    var namePos = ArrayList<Number>()

    init {
        for (pojo in data){
            if(pojo.featureList.size > 0) {
                list.add(pojo.monthAndYear)
                headerPos.add(list.size - 1)

                for (s in pojo.featureList) {
                    list.add(s.featureName)
                    namePos.add(list.size - 1)
                    list.add(s.featureDetails)
                }
            }
        }
    }

    var mInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return list.size
    }

    override fun getViewTypeCount(): Int {
        return 3
    }

    override fun getItemViewType(position: Int): Int {
        // 0 is date, 2 is heading, 1 is item
        if (headerPos.contains(position)) {
            return 0
        } else if (namePos.contains(position)) {
            return 2
        } else {
            return 1
        }
    }

    override fun getItem(position: Int): Any {
        return list.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        val view: View?
        val vh: ListRowHolder
        val viewType = getItemViewType(position)
        if (convertView == null) {
            if(viewType == 2) {
                view = mInflater?.inflate(R.layout.row_item_header, parent, false)
            } else if(viewType ==1) {
                view = mInflater?.inflate(R.layout.row_item, parent, false)
            } else {
                view = mInflater?.inflate(R.layout.row_item_date, parent, false)
            }
            vh = ListRowHolder(view)
            view?.setTag(vh)
        } else {
            view = convertView
            vh = view.getTag() as ListRowHolder
        }
        vh.label.setText(list.get(index = position).toString())
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