package com.expedia.bookings.widget

import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class DebugColorPaletteAdapter : RecyclerView.Adapter<ViewHolder>() {
    private lateinit var colorPaletteList: Array<DebugColorPaletteItem>

    override fun getItemCount(): Int {
        return colorPaletteList.size
    }

    override fun getItemViewType(position: Int): Int {
        return colorPaletteList[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        if (viewType == DebugColorPaletteItem.TITLE) {
            val titleView = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.debug_color_palette_title_cell, parent, false)
            return TitleViewHolder(titleView)
        } else {
            val colorView = LayoutInflater.from(parent?.context)
                    .inflate(R.layout.debug_color_cell, parent, false)
            return ColorViewHolder(colorView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val colorItem = colorPaletteList[position]
        if (holder is ColorViewHolder) {
            colorItem.colorId?.let { color -> holder.bind(color) }
        } else {
            (holder?.itemView as TextView).text = colorItem.title
        }
    }

    fun updateColors(colors: Array<DebugColorPaletteItem>) {
        colorPaletteList = colors
        notifyDataSetChanged()
    }

    private class ColorViewHolder(private val view: View) : ViewHolder(view) {
        val colorIdTextView: TextView by bindView<TextView>(R.id.debug_color_id_name)
        val colorBackgroundView: ImageView by bindView<ImageView>(R.id.debug_color_background)
        val colorHexTextView: TextView by bindView<TextView>(R.id.debug_color_hex_code)

        fun bind( @ColorRes colorId: Int) {
            val context = view.context
            colorIdTextView.text = context.resources.getResourceEntryName(colorId)
            colorBackgroundView.setBackgroundColor(ContextCompat.getColor(context, colorId))
            colorHexTextView.text = Integer.toHexString(ContextCompat.getColor(context, colorId))
        }
    }

    private class TitleViewHolder(val view: View) : ViewHolder(view)
}
