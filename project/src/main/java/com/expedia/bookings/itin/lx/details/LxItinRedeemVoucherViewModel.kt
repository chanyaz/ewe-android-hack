package com.expedia.bookings.itin.lx.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinRedeemVoucherViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import io.reactivex.subjects.PublishSubject

class LxItinRedeemVoucherViewModel<S>(scope: S) : ItinRedeemVoucherViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasLxRepo, S : HasLifecycleOwner, S : HasTripsTracking {

    override val redeemVoucherClickSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    override val showRedeemVoucher: PublishSubject<Unit> = PublishSubject.create()
    var itinObserver: LiveDataObserver<Itin>

    init {
        itinObserver = LiveDataObserver { itin ->
            var redeemUrl: String? = null
            val lxVoucherPrintURL = itin?.firstLx()?.lxVoucherPrintURL
            val voucherPrintURL = itin?.firstLx()?.voucherPrintURL

            if (lxVoucherPrintURL != null && !lxVoucherPrintURL.isBlank()) {
                redeemUrl = lxVoucherPrintURL
            } else if (voucherPrintURL != null && !voucherPrintURL.isBlank()) {
                redeemUrl = voucherPrintURL
            }

            redeemUrl?.let { url ->
                showRedeemVoucher.onNext(Unit)
                redeemVoucherClickSubject.subscribe {
                    val tripId = itin?.tripId
                    if (!url.isEmpty() && tripId != null && !tripId.isBlank()) {
                        scope.tripsTracking.trackItinLxRedeemVoucher()
                        scope.webViewLauncher.launchWebViewActivity(R.string.itin_lx_redeem_voucher, url, null, tripId)
                    }
                }
            }
        }
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
