package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

interface ItinRedeemVoucherViewModel {
    val redeemVoucherClickSubject: PublishSubject<Unit>
    val showRedeemVoucher: PublishSubject<Unit>
}
