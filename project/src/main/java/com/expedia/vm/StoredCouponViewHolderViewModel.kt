package com.expedia.vm

import com.expedia.bookings.presenter.shared.StoredCouponAppliedStatus
import rx.subjects.PublishSubject

class StoredCouponViewHolderViewModel {
    val couponName = PublishSubject.create<String>()
    val couponStatus = PublishSubject.create<StoredCouponAppliedStatus>()

    val couponAppliedVisibility = PublishSubject.create<Boolean>()
    val progressBarVisibility = PublishSubject.create<Boolean>()
    val defaultStateImageVisibility = PublishSubject.create<Boolean>()
    val couponClickActionSubject = PublishSubject.create<Int>()

    init {
        couponStatus.subscribe { status ->
            if (status == StoredCouponAppliedStatus.DEFAULT) {
                setCouponViewVisibility(false, false, true)
            } else if (status == StoredCouponAppliedStatus.SUCCESS) {
                setCouponViewVisibility(true, false, false)
            } else {
                //TODO: Handle the error case
            }
        }
    }

    fun setCouponViewVisibility(couponAppliedState: Boolean, progressBarState: Boolean, defaultStateState: Boolean) {
        couponAppliedVisibility.onNext(couponAppliedState)
        progressBarVisibility.onNext(progressBarState)
        defaultStateImageVisibility.onNext(defaultStateState)
    }
}
