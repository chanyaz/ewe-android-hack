package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.TextView
import com.expedia.bookings.R


class ToDoList(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var listItem: ArrayList<String> = ArrayList()
    private var mAdapter: TaskAdapter? = null
    var listView: ListView
    private var mImageToBeAttached: Bitmap? = null

    init {
        View.inflate(context, R.layout.activity_task, this)
        mAdapter = TaskAdapter(context, listItem)

        listView = findViewById(R.id.list) as ListView
        listView.setAdapter(mAdapter)
        setListHeader(listView)
    }

    private fun setListHeader(listView: ListView) {
        val header = LayoutInflater.from(context).inflate(R.layout.view_task_create, listView, false) as ViewGroup

        val text = header.findViewById(R.id.text) as EditText
        text.setOnKeyListener(View.OnKeyListener { view, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                val inputText = text.text.toString()
                if (inputText.length > 0) {
                    createTask(inputText, mImageToBeAttached)
                }
                text.setText("")

                return@OnKeyListener true
            }
            false
        })

        listView.addHeaderView(header)
    }

    private fun createTask(title: String, image: Bitmap?) {
        listItem.add(title)
        mAdapter?.notifyDataSetChanged()
    }


    private
    inner class TaskAdapter(context: Context, val toDoList: ArrayList<String>) : BaseAdapter() {
        override fun getItem(p0: Int): Any {
            return listItem[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return listItem.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            if (convertView == null) {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = inflater.inflate(R.layout.view_task, parent, false)
            }

            val text = convertView!!.findViewById(R.id.text) as TextView
            text.text = getItem(position) as String

            val checkBox = convertView.findViewById(R.id.checked) as CheckBox
            checkBox.setOnClickListener { updateCheckedStatus(text, checkBox.isChecked) }
            return convertView
        }

        fun updateCheckedStatus(textView: TextView, isChecked: Boolean) {
            if (isChecked)
                textView.setPaintFlags(textView.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
            else
                textView.setPaintFlags(textView.getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv())

        }
    }

}
