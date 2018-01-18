package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.StoredCouponViewHolderViewModel

class StoredCouponViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val couponNameTextView: TextView by bindView(R.id.hotel_coupon_name)
    val defaultStateImage: ImageView by bindView(R.id.stored_coupon_default)
    val progressBar: ProgressBar by bindView(R.id.stored_coupon_progress_bar)
    val couponApplied: ImageView by bindView(R.id.coupon_applied_successful)

    val viewModel: StoredCouponViewHolderViewModel by lazy {
        val vm = StoredCouponViewHolderViewModel()
        vm.couponName.subscribeText(couponNameTextView)
        vm.progressBarVisibility.subscribeVisibility(progressBar)
        vm.couponAppliedVisibility.subscribeVisibility(couponApplied)
        vm.defaultStateImageVisibility.subscribeVisibility(defaultStateImage)
        vm.enableViewHolder.subscribe {
            view.isEnabled = it
        }
        vm
    }

    init {
        view.setOnClickListener {
            viewModel.defaultStateImageVisibility.onNext(false)
            viewModel.progressBarVisibility.onNext(true)
            val clickHolderViewTag = itemView.tag as Int
            viewModel.couponClickActionSubject.onNext(clickHolderViewTag)
        }
    }
}
