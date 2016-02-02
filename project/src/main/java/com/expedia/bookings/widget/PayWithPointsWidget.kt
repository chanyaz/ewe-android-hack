package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import javax.inject.Inject

public class PayWithPointsWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val currencySymbolView: TextView by bindView(R.id.currency_symbol_view)
    val editAmountView: EditText by bindView(R.id.edit_amount_view)
    val totalPointsAvailableView: TextView by bindView(R.id.total_points_available_view)
    val messageView: TextView by bindView(R.id.message_view)
    val clearBtn: View by bindView(R.id.clear_btn)
    val pwpSwitchView: Switch by bindView(R.id.pwp_switch)
    val pwpEditBoxContainer: View by bindView(R.id.pwp_edit_box_container)

    var payWithPointsViewModel by notNullAndObservable<IPayWithPointsViewModel> { pwpViewModel ->
        pwpViewModel.currencySymbol.subscribeText(currencySymbolView)
        pwpViewModel.totalPointsAndAmountAvailableToRedeem.subscribeText(totalPointsAvailableView)
        pwpViewModel.pwpConversionResponse.map { it.first }.subscribeText(messageView)
        pwpViewModel.pwpConversionResponseMessageColor.subscribeTextColor(messageView)
        clearBtn.subscribeOnClick(pwpViewModel.clearButtonClick)
        pwpViewModel.updateAmountOfEditText.subscribeText(editAmountView)
        pwpSwitchView.subscribeOnCheckChanged(pwpViewModel.pwpStateChange)

        subscribeOnClick(endlessObserver {
            refreshPointsForUpdatedBurnAmount()
        })

        editAmountView.setOnEditorActionListener { textView: android.widget.TextView, actionId: Int, event: KeyEvent? ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                refreshPointsForUpdatedBurnAmount()
                handled = true
            }
            handled
        }

        pwpViewModel.pwpStateChange.filter { it }.subscribe {
            pwpViewModel.amountSubmittedByUser.onNext(editAmountView.text.toString())
        }
        pwpViewModel.pwpWidgetVisibility.subscribeVisibility(this)
        pwpViewModel.pwpStateChange.subscribeVisibility(pwpEditBoxContainer)
        pwpViewModel.pwpStateChange.subscribeVisibility(messageView)
    }
        @Inject set

    fun refreshPointsForUpdatedBurnAmount() {
        if (pwpSwitchView.isChecked) {
            payWithPointsViewModel.amountSubmittedByUser.onNext(editAmountView.text.toString())
            Ui.hideKeyboard(editAmountView)
        }
    }

    init {
        View.inflate(getContext(), R.layout.pay_with_points_widget, this)
        Ui.getApplication(getContext()).hotelComponent().inject(this)
    }
}
