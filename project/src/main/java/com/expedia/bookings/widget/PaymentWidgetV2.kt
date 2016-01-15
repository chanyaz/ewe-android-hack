package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import javax.inject.Inject

public class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    val remainingBalance: TextView by bindView(R.id.remaining_balance)

    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> {
        it.remainingBalanceDueOnCard.subscribeText(remainingBalance)
    }
        @Inject set

    override fun onFinishInflate() {
        super.onFinishInflate()
        background = null
        Ui.getApplication(context).hotelComponent().inject(this)
    }
}
