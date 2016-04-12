package com.expedia.vm.interfaces

import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.BigDecimal

public interface IPayWithPointsViewModel {
    //INLETS
    val userEnteredBurnAmount: PublishSubject<String>
    val pwpOpted: BehaviorSubject<Boolean>
    val hasPwpEditBoxFocus: PublishSubject<Boolean>
    val clearUserEnteredBurnAmount: PublishSubject<Unit>
    val navigatingOutOfPaymentOptions: PublishSubject<Unit>
    val userToggledPwPSwitchWithUserEnteredBurnedAmountSubject: PublishSubject<Pair<Boolean, String>>

    //OUTLETS
    val updatePwPToggle: PublishSubject<Boolean>
    val burnAmountUpdate: Observable<String>
    val pwpWidgetVisibility: Observable<Boolean>
    val totalPointsAndAmountAvailableToRedeem: Observable<String>
    val currencySymbol: Observable<String>
    //Pairs of <Message To Be Displayed To User, Is Success Message>
    val pointsAppliedMessage: Observable<Pair<String, Boolean>>
    val pointsAppliedMessageColor: Observable<Int>
    val payWithPointsMessage: Observable<String>
    val enablePwpEditBox: Observable<Boolean>
}
