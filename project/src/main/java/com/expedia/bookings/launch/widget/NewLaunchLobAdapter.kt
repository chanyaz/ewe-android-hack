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
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.launch.vm.NewLaunchLobViewModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.widget.TextView
import java.util.ArrayList

class NewLaunchLobAdapter(private val newLaunchLobViewModel: NewLaunchLobViewModel) : RecyclerView.Adapter<NewLaunchLobAdapter.LobViewHolder>() {

    private var enableLobs: Boolean = true
    private var lobInfos: List<LobInfo> = ArrayList<LobInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cell_lob_button, parent, false)
        return LobViewHolder(view, newLaunchLobViewModel)
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

    class LobViewHolder(itemView: View, val viewModel: NewLaunchLobViewModel) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val lobText by lazy {
            itemView.findViewById(R.id.lob_cell_text) as TextView
        }

        lateinit var lobInfo: LobInfo
        var isLobEnabled: Boolean = true

        init {
            itemView.setOnClickListener(this)
        }

        private fun getPackageTitleChange(): Int {
            val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppPackagesTitleChange)
            if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal) {
                return R.string.nav_hotel_plus_flight
            } else if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal) {
                return R.string.nav_hotel_plus_flight_deals
            }
            return R.string.nav_packages
        }

        fun bind(info: LobInfo, spansMultipleColumns: Boolean, context: Context, lobEnabled: Boolean) {
            lobInfo = info
            isLobEnabled = lobEnabled
            val showChangedTitle = info == LobInfo.PACKAGES && Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppPackagesTitleChange)
            if (showChangedTitle) {
                lobText.setText(getPackageTitleChange())
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
