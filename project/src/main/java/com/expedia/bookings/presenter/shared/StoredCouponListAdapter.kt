package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.extensions.safeSubscribe
import io.reactivex.subjects.PublishSubject

class StoredCouponListAdapter(storedCouponsSubject: PublishSubject<List<StoredCouponAdapter>>,
                              val enableStoredCouponsSubject: PublishSubject<Boolean>, errorMessageAndSavedCouponInstanceIDObservable: PublishSubject<Pair<String, String>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var coupons = arrayListOf<StoredCouponAdapter>()
    val applyStoredCouponObservable = PublishSubject.create<Int>()

    init {
        errorMessageAndSavedCouponInstanceIDObservable.subscribe {
            val errorMessage = it.first
            val instanceId = it.second

            coupons.firstOrNull { it.errorMessage.isNotEmpty() }?.errorMessage = ""
            coupons.first { it.savedCoupon.instanceId == instanceId }.errorMessage = errorMessage
            notifyDataSetChanged()
        }

        storedCouponsSubject.safeSubscribe { newCoupons ->
            coupons.clear()
            coupons.addAll(newCoupons!!)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.stored_coupon_view, parent, false)
        return StoredCouponViewHolder(view as ViewGroup, applyStoredCouponObservable)
    }

    override fun getItemCount(): Int {
        return coupons.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val storedCouponHolder = holder as StoredCouponViewHolder
        storedCouponHolder.viewModel.couponName.onNext(coupons[position].savedCoupon.name)
        storedCouponHolder.viewModel.couponStatus.onNext(if (coupons[position].errorMessage.isEmpty()) {
            coupons[position].savedCouponStatus
        } else StoredCouponAppliedStatus.ERROR)
        storedCouponHolder.viewModel.errorObservable.onNext(coupons[position].errorMessage)
        holder.itemView.tag = position
        enableStoredCouponsSubject.subscribe(holder.viewModel.enableViewHolder)
    }
}
