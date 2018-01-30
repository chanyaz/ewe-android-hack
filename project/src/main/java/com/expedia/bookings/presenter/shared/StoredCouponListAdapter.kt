package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.util.safeSubscribe
import io.reactivex.subjects.PublishSubject

class StoredCouponListAdapter(storedCouponsSubject: PublishSubject<List<StoredCouponAdapter>>,
                              val enableStoredCouponsSubject: PublishSubject<Boolean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var coupons = arrayListOf<StoredCouponAdapter>()
    val applyStoredCouponObservable = PublishSubject.create<HotelCreateTripResponse.SavedCoupon>()

    init {
        storedCouponsSubject.safeSubscribe { newCoupons ->
            coupons.clear()
            coupons.addAll(newCoupons!!)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.stored_coupon_view, parent, false)
        return StoredCouponViewHolder(view as ViewGroup)
    }

    override fun getItemCount(): Int {
        return coupons.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val storedCouponHolder = holder as StoredCouponViewHolder
        storedCouponHolder.viewModel.couponName.onNext(coupons[position].savedCoupon.name)
        storedCouponHolder.viewModel.couponStatus.onNext(coupons[position].savedCouponStatus)
        storedCouponHolder.viewModel.couponClickActionSubject.subscribe { viewHolderTag ->
            applyStoredCouponObservable.onNext(coupons[viewHolderTag].savedCoupon)
        }
        holder.itemView.tag = position
        enableStoredCouponsSubject.subscribe(holder.viewModel.enableViewHolder)
    }
}
