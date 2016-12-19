package com.expedia.bookings.launch.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.TextView
import rx.subjects.PublishSubject

class LobViewHolder(itemView: View, val navItemSelectedSubject: PublishSubject<Pair<LineOfBusiness, View>>) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val lobText by lazy {
        itemView.findViewById(R.id.lob_cell_text) as TextView
    }

    lateinit var lobInfo: LobInfo
    var isLobEnabled: Boolean = true

    init {
        itemView.setOnClickListener(this)
    }

    fun bindInfo(info: LobInfo, enable: Boolean) {
        lobInfo = info
        lobText.setText(info.labelRes)
        AccessibilityUtil.appendRoleContDesc(lobText, lobText.context.getString(info.labelRes), R.string.accessibility_cont_desc_role_button)

        setEnabled(lobText.context, enable)
    }

    fun setSpanSize(fullSpan: Boolean) {
        val lp = lobText.layoutParams as FrameLayout.LayoutParams?
        if (fullSpan) {
            lp?.gravity = Gravity.CENTER
        } else {
            lp?.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
    }

    override fun onClick(view: View) {
        if (isLobEnabled) {
            navItemSelectedSubject.onNext(Pair(lobInfo.lineOfBusiness, view))
        }
    }

    private fun setEnabled(context: Context, enable: Boolean) {
        isLobEnabled = enable

        val lobDrawable = ContextCompat.getDrawable(context, lobInfo.iconRes).mutate()
        if (isLobEnabled) {
            lobDrawable.setColorFilter(ContextCompat.getColor(context, lobInfo.colorRes), PorterDuff.Mode.SRC_IN)
            lobText.alpha = 1f
        } else {
            lobDrawable.setColorFilter(ContextCompat.getColor(context, LobInfo.disabledColorRes), PorterDuff.Mode.SRC_IN)
            lobText.alpha = 0.25f
        }
        lobText.setCompoundDrawablesWithIntrinsicBounds(lobDrawable, null, null, null)
    }
}