package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseCheckoutViewModel
import com.squareup.phrase.Phrase

// TODO might be worth investigating not using BaseCheckoutViewModel
class RailCheckoutViewModel(context: Context) : BaseCheckoutViewModel(context) {
    override fun injectComponents() {
        Ui.getApplication(context).railComponent().inject(this)
    }

    override fun getTripId(): String {
        throw UnsupportedOperationException()
    }

    override fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?) {
        throw UnsupportedOperationException()
    }

    val totalPriceObserver = endlessObserver<Money> { totalPrice ->
        val slideToPurchaseText = Phrase.from(context, R.string.your_card_will_be_charged_template)
                .put("dueamount", totalPrice.formattedMoneyFromAmountAndCurrencyCode)
                .format().toString()

        sliderPurchaseTotalText.onNext(slideToPurchaseText)
    }
}
