package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import android.graphics.Color
import android.util.TypedValue

class DebugTextStyleAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var textStyleList: Array<Int>

    fun updateStyles(styles: Array<Int>) {
        textStyleList = styles
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ta = context.obtainStyledAttributes(textStyleList[position], R.styleable.DebugCoreTextStyleAttr)
        val textColor = ta.getColor(R.styleable.DebugCoreTextStyleAttr_android_textColor, Color.GREEN)
        val textSize = ta.getDimensionPixelSize(R.styleable.DebugCoreTextStyleAttr_android_textSize, 0)
        val textStyle = ta.getInt(R.styleable.DebugCoreTextStyleAttr_textStyle, 0)
        ta.recycle()

        val styleName = context.resources.getResourceEntryName(textStyleList[position])
        val textView = (holder as TextStyleViewHolder).textView
        textView.setTextColor(textColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        textView.setTypefaceByStyle(textView, textStyle)
        textView.text = styleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TextStyleViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.debug_text_style_cell, parent, false) as TextView)
    }

    override fun getItemCount(): Int {
        return textStyleList.size
    }

    private class TextStyleViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}
