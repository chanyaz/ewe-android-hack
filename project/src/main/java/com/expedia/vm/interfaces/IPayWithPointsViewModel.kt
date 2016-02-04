package com.expedia.vm.interfaces

import rx.Observable
import rx.subjects.PublishSubject

public interface IPayWithPointsViewModel {
    //INLETS
    val userEnteredBurnAmount: PublishSubject<String>
    val pwpOpted: PublishSubject<Boolean>
    val clearUserEnteredBurnAmount: PublishSubject<Unit>
    val userSignedIn: PublishSubject<Boolean>
    val navigatingBackToCheckoutScreen: PublishSubject<Unit>

    //OUTLETS
    val enablePwPToggle: Observable<Boolean>
    val burnAmountUpdate: Observable<String>
    val pwpWidgetVisibility: Observable<Boolean>
    val totalPointsAndAmountAvailableToRedeem: Observable<String>
    val currencySymbol: Observable<String>
    //Pairs of <Message To Be Displayed To User, Is Success Message>
    val pointsAppliedMessage: Observable<Pair<String, Boolean>>
    val pointsAppliedMessageColor: Observable<Int>
}
