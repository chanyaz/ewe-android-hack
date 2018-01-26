package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.StoredCouponViewHolderViewModel

class StoredCouponViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val couponNameTextView by bindView<TextView>(R.id.hotel_coupon_name)
    val defaultStateImage by bindView<ImageView>(R.id.stored_coupon_default)
    val progressBar by bindView<ProgressBar>(R.id.stored_coupon_progress_bar)
    val couponApplied by bindView<ImageView>(R.id.coupon_applied_successful)
    val couponErrorTextView by bindView<TextView>(R.id.coupon_error)

    val viewModel: StoredCouponViewHolderViewModel by lazy {
        val vm = StoredCouponViewHolderViewModel()
        vm.couponName.subscribeText(couponNameTextView)
        vm.progressBarVisibility.subscribeVisibility(progressBar)
        vm.couponAppliedVisibility.subscribeVisibility(couponApplied)
        vm.defaultStateImageVisibility.subscribeVisibility(defaultStateImage)
        vm.errorObservable.subscribeTextAndVisibility(couponErrorTextView)
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
