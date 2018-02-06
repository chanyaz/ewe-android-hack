package com.expedia.vm

import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus
import io.reactivex.subjects.PublishSubject

class StoredCouponViewHolderViewModel {
    val couponName = PublishSubject.create<String>()
    val couponStatus = PublishSubject.create<StoredCouponAppliedStatus>()
    val enableViewHolder = PublishSubject.create<Boolean>()
    val errorObservable = PublishSubject.create<String>()
    val couponClickActionSubject = PublishSubject.create<Int>()
}
