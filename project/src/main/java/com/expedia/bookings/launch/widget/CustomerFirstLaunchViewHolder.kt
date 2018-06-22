package com.expedia.bookings.launch.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.extensions.setTypeface
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Font
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.vm.launch.CustomerFirstLaunchHolderViewModel

class CustomerFirstLaunchViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val customerFirstText: TextView by bindView(R.id.customer_first_text)

    init {
        view.setOnClickListener(this)
        setTextViewFont()
    }

    fun bind(vm: CustomerFirstLaunchHolderViewModel) {
        customerFirstText.text = vm.firstLine
    }

    override fun onClick(view: View) {
        NavUtils.goToCustomerFirstSupportActivity(view.context)
        OmnitureTracking.trackTapCustomerFirstGuaranteeLaunchTile()
    }

    private fun setTextViewFont() {
        customerFirstText.setTypeface(Font.ROBOTO_MEDIUM)
    }
}
