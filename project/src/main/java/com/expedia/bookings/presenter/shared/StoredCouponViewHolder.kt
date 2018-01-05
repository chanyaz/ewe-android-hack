package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.StoredCouponViewHolderViewModel

class StoredCouponViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val hotelNameTextView: TextView by bindView(R.id.hotel_coupon_name)

    val viewModel: StoredCouponViewHolderViewModel by lazy {
        val vm = StoredCouponViewHolderViewModel()
        vm.couponName.subscribeText(hotelNameTextView)
        vm
    }
}
