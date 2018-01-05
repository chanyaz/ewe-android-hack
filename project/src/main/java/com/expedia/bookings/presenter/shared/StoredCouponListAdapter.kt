package com.expedia.bookings.presenter.shared

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.util.safeSubscribe
import rx.subjects.PublishSubject

class StoredCouponListAdapter(storedCouponsSubject: PublishSubject<List<HotelCreateTripResponse.SavedCoupon>?>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var coupons = arrayListOf<HotelCreateTripResponse.SavedCoupon>()

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
        (holder as StoredCouponViewHolder).viewModel.couponName.onNext(coupons[position].name)
    }

}

