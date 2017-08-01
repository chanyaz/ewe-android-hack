package com.expedia.vm.interfaces

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface IPayWithPointsViewModel {
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
