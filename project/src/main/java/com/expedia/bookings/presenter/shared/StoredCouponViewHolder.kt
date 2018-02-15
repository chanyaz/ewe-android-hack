package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.StoredCouponViewHolderViewModel
import io.reactivex.subjects.PublishSubject

class StoredCouponViewHolder(view: View, clickActionObserver: PublishSubject<Int>) : RecyclerView.ViewHolder(view) {
    val couponNameTextView by bindView<TextView>(R.id.hotel_coupon_name)
    val defaultStateImage by bindView<ImageView>(R.id.stored_coupon_default)
    val progressBar by bindView<ProgressBar>(R.id.stored_coupon_progress_bar)
    val couponApplied by bindView<ImageView>(R.id.coupon_applied_successful)
    val errorImageView by bindView<ImageView>(R.id.error_image_view)
    val couponErrorTextView by bindView<TextView>(R.id.coupon_error)

    val viewModel: StoredCouponViewHolderViewModel by lazy {
        val vm = StoredCouponViewHolderViewModel()
        vm.couponName.doOnNext {
            AccessibilityUtil.appendRoleContDesc(couponNameTextView, it, R.string.accessibility_cont_desc_role_button)
        }.subscribeText(couponNameTextView)
        vm.couponStatus.subscribe { status ->
            progressBar.visibility = View.GONE
            if (status != null) {
                when (status) {
                    StoredCouponAppliedStatus.DEFAULT -> {
                        defaultStateImage.visibility = View.VISIBLE
                        errorImageView.visibility = View.GONE
                        couponApplied.visibility = View.GONE
                    }
                    StoredCouponAppliedStatus.SUCCESS -> {
                        defaultStateImage.visibility = View.GONE
                        errorImageView.visibility = View.GONE
                        couponApplied.visibility = View.VISIBLE
                    }
                    StoredCouponAppliedStatus.ERROR -> {
                        defaultStateImage.visibility = View.GONE
                        errorImageView.visibility = View.VISIBLE
                        couponApplied.visibility = View.GONE
                    }
                }
            }
        }

        vm.errorObservable.subscribeTextAndVisibility(couponErrorTextView)
        vm.enableViewHolder.subscribe {
            view.isEnabled = it
        }
        vm
    }

    init {
        view.setOnClickListener {
            errorImageView.visibility = View.GONE
            couponErrorTextView.visibility = View.GONE
            defaultStateImage.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            val clickHolderViewTag = itemView.tag as Int
            clickActionObserver.onNext(clickHolderViewTag)
        }
    }
}
