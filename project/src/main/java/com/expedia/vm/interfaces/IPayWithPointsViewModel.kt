package com.expedia.vm.interfaces

import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

public interface IPayWithPointsViewModel {
    //INLETS
    val userEnteredBurnAmount: PublishSubject<String>
    val pwpOpted: BehaviorSubject<Boolean>
    val hasPwpEditBoxFocus: PublishSubject<Boolean>
    val clearUserEnteredBurnAmount: PublishSubject<Unit>
    val userSignedIn: PublishSubject<Boolean>
    val navigatingOutOfPaymentOptions: PublishSubject<Unit>
    val userToggledPwPSwitchWithUserEnteredBurnedAmountSubject: PublishSubject<Pair<Boolean, String>>

    //OUTLETS
    val enablePwPToggle: Observable<Boolean>
    val burnAmountUpdate: Observable<String>
    val pwpWidgetVisibility: Observable<Boolean>
    val totalPointsAndAmountAvailableToRedeem: Observable<String>
    val currencySymbol: Observable<String>
    //Pairs of <Message To Be Displayed To User, Is Success Message>
    val pointsAppliedMessage: Observable<Pair<String, Boolean>>
    val pointsAppliedMessageColor: Observable<Int>
    val enablePwpEditBox: Observable<Boolean>
}
