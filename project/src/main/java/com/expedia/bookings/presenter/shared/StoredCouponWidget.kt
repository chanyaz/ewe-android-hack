package com.expedia.bookings.presenter.shared

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView

class StoredCouponWidget(context: Context, attr: AttributeSet?) : LinearLayout(context, attr) {

    val storedCouponRecyclerView: StoredCouponRecyclerView by bindView(R.id.stored_coupon_recycler_view)
    val viewModel = StoredCouponWidgetViewModel()

    override fun onFinishInflate() {
        super.onFinishInflate()
        storedCouponRecyclerView.adapter = StoredCouponListAdapter(viewModel.storedCouponsSubject, viewModel.enableStoredCouponsSubject, viewModel.errorObservable)
        storedCouponRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}
