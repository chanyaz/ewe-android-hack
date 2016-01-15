package com.expedia.bookings.widget

import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.squareup.phrase.Phrase
import rx.Observable

public interface IPaymentWidgetViewModel {
    //outlet
    val remainingBalanceDueOnCard: Observable<String>
}

public class PaymentWidgetViewModel<T : TripResponse>(paymentModel: PaymentModel<T>, val resources: Resources) : IPaymentWidgetViewModel {
    private fun remainingBalanceDueOnCardMessage(amount: Money) = Phrase.from(resources, R.string.pwp_remaining_balance_due_on_card_TEMPLATE)
            .put("money", amount.formattedMoneyFromAmountAndCurrencyCode).format().toString()

    override val remainingBalanceDueOnCard = paymentModel.paymentSplits.map { remainingBalanceDueOnCardMessage(it.payingWithCards.amount) }
}