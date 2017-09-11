package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ScrollView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseCreditCardFeesViewModel
import com.expedia.vm.rail.RailCreditCardFeesViewModel

class CreditCardFeesView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {
    val container: LinearLayout by bindView(R.id.cc_fees_container)

    var viewModel: BaseCreditCardFeesViewModel by notNullAndObservable { vm ->
        vm.cardFeesObservable.subscribe { cardFees ->
            container.removeAllViews()
            cardFees.forEach { cardFee ->
                container.addView(addCardFeeRow(cardFee)
                )
            }
        }
    }

    init {
        View.inflate(context, R.layout.credit_card_fees_view, this)
    }

    private fun addCardFeeRow(cardFeeRow: RailCreditCardFeesViewModel.CardFeesRow): View {
        val row = LayoutInflater.from(context).inflate(R.layout.card_fee_row, null)
        val cardName = row.findViewById<TextView>(R.id.card_type_text_view)
        val cardFee = row.findViewById<TextView>(R.id.card_fee_text_view)
        cardName.text = cardFeeRow.cardName
        cardFee.text = cardFeeRow.fee

        return row
    }
}