package com.expedia.bookings.launch.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import rx.subjects.PublishSubject
import java.util.ArrayList

class NewLaunchLobAdapter() : RecyclerView.Adapter<LobViewHolder>() {

    val navigationSubject = PublishSubject.create<Pair<LineOfBusiness, View>>()

    private var enableLobs: Boolean = true
    private var lobInfos: List<LobInfo> = ArrayList<LobInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_lob_button, parent, false)
        return LobViewHolder(view, navigationSubject)
    }

    override fun onBindViewHolder(holder: LobViewHolder, position: Int) {
        holder.bindInfo(lobInfos[position], enableLobs)
        holder.setSpanSize(getSpanSize(position) != 1)
    }

    override fun getItemCount(): Int {
        return lobInfos.size
    }

    fun enableLobs(enable: Boolean) {
        enableLobs = enable
        notifyDataSetChanged()
    }

    fun setLobs(lobs: List<LobInfo>) {
        lobInfos = lobs
        notifyDataSetChanged()
    }

    fun getSpanSize(position: Int): Int {
        val length = itemCount
        return if (length > 0 && position == length - 1 && position % 2 == 0) 2 else 1
    }
}