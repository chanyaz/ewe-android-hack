package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import com.expedia.bookings.R
import com.expedia.bookings.utils.DecimalNumberInputFilter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeChecked
import com.expedia.util.subscribeCursorVisible
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import java.util.Locale
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

        editAmountView.setOnClickListener { editAmountView.isCursorVisible = true }
        editAmountView.setOnEditorActionListener { textView: android.widget.TextView, actionId: Int, event: KeyEvent? ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                refreshPointsForUpdatedBurnAmount()
                handled = true
            }
            handled
        }

        pwpViewModel.pointsAppliedMessage.map { it.first }.subscribeText(messageView)
        pwpViewModel.pointsAppliedMessageColor.subscribeTextColor(messageView)
        clearBtn.subscribeOnClick(pwpViewModel.clearUserEnteredBurnAmount)
        pwpViewModel.burnAmountUpdate.subscribeText(editAmountView)
        pwpSwitchView.subscribeOnCheckChanged(pwpViewModel.pwpOpted)
        pwpViewModel.enablePwPToggle.subscribeChecked(pwpSwitchView)
        pwpViewModel.navigatingBackToCheckoutScreen.map { false }.subscribeCursorVisible(editAmountView)

        subscribeOnClick(endlessObserver {
            refreshPointsForUpdatedBurnAmount()
        })
        pwpViewModel.pwpOpted.filter { it }.subscribe {
            pwpViewModel.userEnteredBurnAmount.onNext(editAmountView.text.toString())
        }
        pwpViewModel.pwpWidgetVisibility.subscribeVisibility(this)
        pwpViewModel.pwpOpted.subscribeVisibility(pwpEditBoxContainer)
        pwpViewModel.pwpOpted.subscribeVisibility(messageView)
    }
        @Inject set

    fun refreshPointsForUpdatedBurnAmount() {
        if (this.visibility == VISIBLE && pwpSwitchView.isChecked) {
            editAmountView.isCursorVisible = false
            Ui.hideKeyboard(editAmountView)
            payWithPointsViewModel.userEnteredBurnAmount.onNext(editAmountView.text.toString())
        }
    }

    init {
        View.inflate(getContext(), R.layout.pay_with_points_widget, this)
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        editAmountView.filters = arrayOf(DecimalNumberInputFilter(2))
        editAmountView.hint = "%.2f".format(Locale.getDefault(), 0.0)
    }
}
