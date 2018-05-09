package com.expedia.bookings.itin.common

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import io.reactivex.subjects.PublishSubject

class LxItinRedeemVoucherViewModel<S>(scope: S) : ItinRedeemVoucherViewModel where S : HasStringProvider, S : HasWebViewLauncher, S : HasLxRepo, S : HasLifecycleOwner, S : HasTripsTracking {

    override val redeemVoucherClickSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    override val showRedeemVoucher: PublishSubject<Unit> = PublishSubject.create()
    var itinLxObserver: LiveDataObserver<ItinLx>
    var itinObserver: LiveDataObserver<Itin>
    val redeemURL: PublishSubject<String> = PublishSubject.create()
    val tripID: PublishSubject<String> = PublishSubject.create()
    var url = ""

    init {
        itinLxObserver = LiveDataObserver { itinLx ->
            if (itinLx != null) {
                if (!(itinLx.lxVoucherPrintURL).isNullOrEmpty()) {
                    url = itinLx.lxVoucherPrintURL!!
                    redeemURL.onNext(url)
                } else if (!(itinLx.voucherPrintURL).isNullOrEmpty()) {
                    url = itinLx.voucherPrintURL!!
                    redeemURL.onNext(url)
                }
            }
        }

        itinObserver = LiveDataObserver { itin ->
            val tripId = itin?.tripId
            if (!tripId.isNullOrEmpty()) {
                tripID.onNext(tripId!!)
            }
        }

        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)

        ObservableOld.zip(redeemURL, tripID, { redeemURL, tripID ->
            object {
                val redeemURL = redeemURL
                val tripID = tripID
            }
        }).subscribe { obj ->
            redeemVoucherClickSubject.subscribe {
                scope.tripsTracking.trackItinLxRedeemVoucher()
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_lx_redeem_voucher, obj.redeemURL, null, obj.tripID)
            }
            showRedeemVoucher.onNext(Unit)
        }
    }
}
