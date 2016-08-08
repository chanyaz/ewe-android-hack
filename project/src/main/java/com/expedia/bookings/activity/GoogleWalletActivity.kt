package com.expedia.bookings.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.BookingInfoUtils
import com.expedia.bookings.utils.WalletUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wallet.FullWallet
import com.google.android.gms.wallet.FullWalletRequest
import com.google.android.gms.wallet.MaskedWallet
import com.google.android.gms.wallet.MaskedWalletRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import java.math.BigDecimal
import kotlin.properties.Delegates

class GoogleWalletActivity : AppCompatActivity() {
    val REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET: Int = 1001
    val REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET: Int = 1003
    var googleApiClient: GoogleApiClient by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet_layout)
        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(connectionFailed)
                .addApi(Wallet.API, Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletUtils.getWalletEnvironment(this))
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if  (resultCode == Activity.RESULT_CANCELED || data == null) {
            finish()
            return
        }
        when (requestCode) {
            REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET -> when (resultCode) {
                Activity.RESULT_OK -> {
                    val wallet: MaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET)
                    Db.setMaskedWallet(wallet)
                    WalletUtils.bindWalletToBillingInfo(wallet, Db.getBillingInfo())
                    val traveler = WalletUtils.addWalletAsTraveler(this, wallet)
                    BookingInfoUtils.insertTravelerDataIfNotFilled(this, traveler, LineOfBusiness.HOTELS);
                    getFullWallet(wallet.googleTransactionId)
                    return
                }
            }
            REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET -> when (resultCode) {
                Activity.RESULT_OK -> {
                    if (data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
                        val fullWallet: FullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET)
                        Db.setFullWallet(fullWallet)
                        WalletUtils.bindWalletToBillingInfo(fullWallet, Db.getBillingInfo())
                        setResult(Activity.RESULT_OK)
                        finish()
                        return
                    }
                }
            }
        }
    }

    private fun buildMaskedWalletRequest(): MaskedWalletRequest {
        val rate = Db.getTripBucket().hotelV2.mHotelTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val total = Money(BigDecimal(rate.totalPriceWithMandatoryFees.toDouble()), rate.currencyCode)

        val builder = MaskedWalletRequest.newBuilder()
        builder.setMerchantName(getString(R.string.merchant_name))
        builder.setCurrencyCode(total.currency)
        builder.setEstimatedTotalPrice(WalletUtils.formatAmount(total))

        builder.setCart(WalletUtils.buildHotelV2Cart(this))
        builder.setPhoneNumberRequired(true)

        return builder.build()
    }

    private fun buildFullWalletRequest(googleTransactionId: String): FullWalletRequest {
        var builder = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(WalletUtils.buildHotelV2Cart(this))

        return builder.build();
    }

    private fun loadMaskedWallet() {
        Wallet.Payments.loadMaskedWallet(googleApiClient,
                buildMaskedWalletRequest(),
                REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET);
    }

    private fun getFullWallet(transactionId: String) {
        Wallet.Payments.loadFullWallet(googleApiClient,
                buildFullWalletRequest(transactionId),
                REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
    }


    val connectionCallback = object : GoogleApiClient.ConnectionCallbacks
    {
        override fun onConnectionSuspended(p0: Int) {
        }

        override fun onConnected(p0: Bundle?) {
            loadMaskedWallet()
        }

    }

    val connectionFailed = object : GoogleApiClient.OnConnectionFailedListener
    {
        override fun onConnectionFailed(p0: ConnectionResult) {
        }
    }

}