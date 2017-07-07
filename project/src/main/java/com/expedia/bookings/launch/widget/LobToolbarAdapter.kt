package com.expedia.bookings.launch.widget

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.updateVisibility
import rx.subjects.PublishSubject

class LobToolbarAdapter(val defaultLob: LineOfBusiness) : RecyclerView.Adapter<LobToolbarAdapter.LobTabViewHolder>() {
    val toolbarItemClickedSubject = PublishSubject.create<LineOfBusiness>()

    private var lobInfos: List<LobInfo> = ArrayList<LobInfo>()

    fun setLob(lobs: List<LobInfo>) {
        lobInfos = lobs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobToolbarAdapter.LobTabViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(R.layout.lob_toolbar_item, parent, false)
        return LobTabViewHolder(root)
    }

    override fun onBindViewHolder(holder: LobToolbarAdapter.LobTabViewHolder, position: Int) {
        holder.bind(lobInfos[position])
    }

    override fun getItemCount(): Int {
        return lobInfos.size
    }

    inner class LobTabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var lob: LineOfBusiness
        private val lobName: TextView by bindView(R.id.lob_name)
        private val selectedIndicator: View by bindView(R.id.lob_selected_indicator)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(lobInfo: LobInfo) {
            lob = lobInfo.lineOfBusiness
            lobName.text = itemView.resources.getString(lobInfo.labelRes)
            val selected = defaultLob == lobInfo.lineOfBusiness
            selectedIndicator.updateVisibility(selected)
            if (selected) {
                lobName.setTextColor(ContextCompat.getColor(itemView.context, R.color.material_tab_text_selected))
            }
        }

        override fun onClick(v: View?) {
            toolbarItemClickedSubject.onNext(lob)
        }
    }
}