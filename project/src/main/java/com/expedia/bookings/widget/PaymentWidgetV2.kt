package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.havePermissionToAccessCamera
import com.expedia.util.notNullAndObservable
import com.expedia.util.requestCameraPermission
import com.expedia.util.subscribeEnabled
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import com.expedia.vm.interfaces.IPaymentWidgetViewModel
import javax.inject.Inject

class PaymentWidgetV2(context: Context, attr: AttributeSet) : PaymentWidget(context, attr) {
    companion object {
        val CARD_IO_REQUEST_CODE = 620
        val REQUEST_CAMERA = 621
    }

    val remainingBalance: LinearLayout by bindView(R.id.remaining_balance)
    val remainingBalanceAmount: TextView by bindView(R.id.remaining_balance_amount)
    val totalDueTodayAmount: TextView by bindView(R.id.total_due_today_amount)
    val scanCardButton: LinearLayout by bindView(R.id.scan_card_button)
    val rewardWidget: ViewStub by bindView(R.id.reward_widget_stub)
    var paymentSplitsType = PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD
    var isRewardsRedeemable: Boolean = false
    var isFullPayableWithPoints: Boolean = false


    var paymentWidgetViewModel by notNullAndObservable<IPaymentWidgetViewModel> { vm ->
        vm.totalDueToday.subscribeText(totalDueTodayAmount)
        vm.remainingBalanceDueOnCard.subscribeText(remainingBalanceAmount)
        vm.remainingBalanceDueOnCardVisibility.subscribeVisibility(remainingBalance)
        vm.paymentSplitsWithTripTotalAndTripResponse.map { it.isCardRequired() }.subscribeEnabled(sectionCreditCardContainer)
        vm.paymentSplitsWithTripTotalAndTripResponse.subscribe {
            viewmodel.isRedeemable.onNext(it.tripResponse.isRewardsRedeemable())
            viewmodel.splitsType.onNext(it.paymentSplits.paymentSplitsType())
            isRewardsRedeemable = it.tripResponse.isRewardsRedeemable()
            paymentSplitsType = it.paymentSplits.paymentSplitsType()
            isFullPayableWithPoints = !it.isCardRequired()
            vm.burnAmountApiCallResponsePending.onNext(false)
        }

        scanCardButton.setOnClickListener { view ->
            if (!havePermissionToAccessCamera(context)) {
                requestCameraPermission(context as Activity)
            } else {
                viewmodel.startCreditCardScan.onNext(Unit)
            }
        }

        enableToolbarMenuButton.subscribe{ enable ->
            if (paymentOptionsContainer.visibility == View.VISIBLE) {
                viewmodel.enableMenuItem.onNext(enable && isComplete())
            }
        }

        viewmodel.onStoredCardChosen.withLatestFrom(vm.isPwpDirty, { x, y -> y })
                .filter { !it }
                .map { true }
                .subscribe(enableToolbarMenuButton)
    }
        @Inject set

    lateinit var payWithPointsViewModel: IPayWithPointsViewModel
        @Inject set

    override fun init(vm: PaymentViewModel) {
        super.init(vm)
        Ui.getApplication(context).hotelComponent().inject(this)
        val isUserBucketedForCardIOTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelHCKOCardIOTest);
        if (HotelUtils.isCardIoAvailable() && isUserBucketedForCardIOTest) {
            scanCardButton.visibility = View.VISIBLE
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val layoutId = ProductFlavorFeatureConfiguration.getInstance().rewardsLayoutId
        if(layoutId != 0){
            rewardWidget.layoutResource = layoutId
            rewardWidget.inflate();
        }
    }

    override fun shouldShowPaymentOptions(): Boolean {
        return isRewardsRedeemable || super.shouldShowPaymentOptions()
    }

    override fun validateAndBind() {
        if (!isFullPayableWithPoints) {
            super.validateAndBind()
        }
    }

    override fun isComplete(): Boolean {
        return isFullPayableWithPoints || super.isComplete()
    }

    override fun updateLegacyToolbarMenu(forward: Boolean) {
        super.updateLegacyToolbarMenu(forward)
        if (forward) {
            payWithPointsViewModel.hasPwpEditBoxFocus.onNext(false)
            viewmodel.enableMenuItem.onNext(isComplete())
        } else {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
            viewmodel.enableMenuItem.onNext(true)
        }
    }

    override fun back(): Boolean {
        if (currentState == PaymentOption::class.java.name) {
            paymentWidgetViewModel.navigatingOutOfPaymentOptions.onNext(Unit)
            viewmodel.enableMenuItem.onNext(true)
        }
        return super.back()
    }
}