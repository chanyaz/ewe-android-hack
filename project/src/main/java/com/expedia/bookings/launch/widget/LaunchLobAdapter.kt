package com.expedia.bookings.launch.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.launch.vm.LaunchLobViewModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.TextView
import com.expedia.util.PackageUtil
import java.util.ArrayList

class LaunchLobAdapter(private val launchLobViewModel: LaunchLobViewModel) : RecyclerView.Adapter<LaunchLobAdapter.LobViewHolder>() {

    private var enableLobs: Boolean = true
    private var lobInfos: List<LobInfo> = ArrayList<LobInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_lob_button, parent, false)
        return LobViewHolder(view, launchLobViewModel)
    }

    override fun onBindViewHolder(holder: LobViewHolder, position: Int) {
        holder.bind(lobInfos[position], getSpanSize(position) != 1, holder.lobText.context, enableLobs)
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

    class LobViewHolder(itemView: View, val viewModel: LaunchLobViewModel) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val lobText by lazy {
            itemView.findViewById<TextView>(R.id.lob_cell_text)
        }

        lateinit var lobInfo: LobInfo
        var isLobEnabled: Boolean = true

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(info: LobInfo, spansMultipleColumns: Boolean, context: Context, lobEnabled: Boolean) {
            lobInfo = info
            isLobEnabled = lobEnabled
            if (info == LobInfo.PACKAGES) {
                lobText.setText(PackageUtil.packageTitle)
            } else {
                lobText.setText(info.labelRes)
            }

            AccessibilityUtil.appendRoleContDesc(lobText, context.getString(info.labelRes), R.string.accessibility_cont_desc_role_button)
            val lobDrawable = ContextCompat.getDrawable(context, lobInfo.iconRes).mutate()
            if (isLobEnabled) {
                lobDrawable.setColorFilter(ContextCompat.getColor(context, lobInfo.colorRes), PorterDuff.Mode.SRC_IN)
                lobText.alpha = 1f
            } else {
                lobDrawable.setColorFilter(ContextCompat.getColor(context, LobInfo.disabledColorRes), PorterDuff.Mode.SRC_IN)
                lobText.alpha = 0.25f
            }
            lobText.setCompoundDrawablesWithIntrinsicBounds(lobDrawable, null, null, null)


            val lp = lobText.layoutParams as FrameLayout.LayoutParams?
            if (spansMultipleColumns) {
                lp?.gravity = Gravity.CENTER
            } else {
                lp?.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
        }

        override fun onClick(view: View) {
            if (isLobEnabled) {
                viewModel.navigationSubject.onNext(Pair(lobInfo.lineOfBusiness, view))
            }
        }
    }
}
