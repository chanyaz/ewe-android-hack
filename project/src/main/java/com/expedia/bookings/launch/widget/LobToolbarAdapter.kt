package com.expedia.bookings.launch.widget

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.PackageUtil
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
        private val lobIcon: ImageView by bindView(R.id.lob_icon)
        private val selectedIndicator: View by bindView(R.id.lob_selected_indicator)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(lobInfo: LobInfo) {
            lob = lobInfo.lineOfBusiness

            val labelRes = when (lob) {
                LineOfBusiness.PACKAGES ->
                    PackageUtil.packageTitle(itemView.context)
                else ->
                    lobInfo.labelRes
            }

            lobName.text = itemView.resources.getString(labelRes)
            lobIcon.setImageResource(lobInfo.iconRes)
            val selected = defaultLob == lobInfo.lineOfBusiness

            if (selected) {
                updateSelectedState()
            } else {
                updateDeselectedState()
            }
        }

        override fun onClick(v: View?) {
            toolbarItemClickedSubject.onNext(lob)
        }

        private fun updateDeselectedState() {
            selectedIndicator.visibility = View.INVISIBLE
            lobIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.material_tab_text), PorterDuff.Mode.SRC_IN)
            lobIcon.alpha = 0.7f
        }

        private fun updateSelectedState() {
            lobName.setTextColor(ContextCompat.getColor(itemView.context, R.color.material_tab_text_selected))
            lobName.typeface = Typeface.DEFAULT_BOLD
            lobIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.material_tab_text_selected), PorterDuff.Mode.SRC_IN)
            selectedIndicator.visibility = View.VISIBLE
        }
    }
}