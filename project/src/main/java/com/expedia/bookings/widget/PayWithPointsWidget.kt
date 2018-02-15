package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeChecked
import com.expedia.bookings.extensions.subscribeCursorVisible
import com.expedia.bookings.extensions.subscribeEnabled
import com.expedia.bookings.extensions.subscribeOnCheckChanged
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.DecimalNumberInputFilter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import io.reactivex.subjects.BehaviorSubject
import java.util.Locale
import javax.inject.Inject

class PayWithPointsWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val currencySymbolView: TextView by bindView(R.id.currency_symbol_view)
    val editAmountView: EditText by bindView(R.id.edit_amount_view)
    val payWithPointsMessage: TextView by bindView(R.id.pay_with_points_view)
    val totalPointsAvailableView: TextView by bindView(R.id.total_points_available_view)
    val messageView: TextView by bindView(R.id.message_view)
    val clearBtn: View by bindView(R.id.clear_btn)
    val pwpSwitchView: Switch by bindView(R.id.pwp_switch)
    val pwpEditBoxContainer: View by bindView(R.id.pwp_edit_box_container)

    val wasLastUpdateProgrammatic = BehaviorSubject.createDefault<Boolean>(false)
    var payWithPointsViewModel: IPayWithPointsViewModel by notNullAndObservable<IPayWithPointsViewModel> { pwpViewModel ->
        pwpViewModel.currencySymbol.subscribeText(currencySymbolView)
        pwpViewModel.totalPointsAndAmountAvailableToRedeem.subscribeText(totalPointsAvailableView)

        editAmountView.setOnClickListener {
            editAmountView.isCursorVisible = true
            pwpViewModel.hasPwpEditBoxFocus.onNext(true)
        }

        editAmountView.setOnFocusChangeListener { _, hasFocus ->
            editAmountView.isCursorVisible = hasFocus
        }

        editAmountView.setOnEditorActionListener { _, actionId, _ ->
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
        pwpViewModel.updatePwPToggle.doOnNext { if (pwpSwitchView.isChecked != it) wasLastUpdateProgrammatic.onNext(true) }.subscribeChecked(pwpSwitchView)
        pwpViewModel.navigatingOutOfPaymentOptions.map { false }.subscribeCursorVisible(editAmountView)

        subscribeOnClick(endlessObserver {
            refreshPointsForUpdatedBurnAmount()
        })

        pwpViewModel.pwpOpted.filter { it }.subscribe {
            editAmountView.isCursorVisible = false
        }

        // Send Omniture tracking for PWP toggle only in case of user doing it.
        pwpViewModel.pwpOpted.withLatestFrom(wasLastUpdateProgrammatic, { pwpOpted, wasLastUpdateProgrammatic ->
            object {
                val pwpOpted = pwpOpted
                val wasLastUpdateProgrammatic = wasLastUpdateProgrammatic
            }
        }).subscribe {
            if (!it.wasLastUpdateProgrammatic) {
                pwpViewModel.userToggledPwPSwitchWithUserEnteredBurnedAmountSubject.onNext(Pair(it.pwpOpted, editAmountView.text.toString()))
            } else wasLastUpdateProgrammatic.onNext(false)
        }

        pwpViewModel.pwpWidgetVisibility.subscribeVisibility(this)
        pwpViewModel.pwpOpted.subscribeVisibility(pwpEditBoxContainer)
        pwpViewModel.pwpOpted.subscribeVisibility(messageView)
        pwpViewModel.payWithPointsMessage.subscribeText(payWithPointsMessage)
        pwpViewModel.enablePwpEditBox.subscribeEnabled(editAmountView)
    }
        @Inject set

    fun refreshPointsForUpdatedBurnAmount() {
        if (this.visibility == VISIBLE && pwpSwitchView.isChecked) {
            editAmountView.isCursorVisible = false
            Ui.hideKeyboard(editAmountView)
            payWithPointsViewModel.hasPwpEditBoxFocus.onNext(false)
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
