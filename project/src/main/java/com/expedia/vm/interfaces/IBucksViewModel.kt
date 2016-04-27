package com.expedia.vm.interfaces

import rx.Observable
import rx.subjects.BehaviorSubject

interface IBucksViewModel {
    //Inlets
    val bucksOpted: BehaviorSubject<Boolean>

    //Outlet
    val bucksMessage: Observable<String>
    val pointsAppliedMessageColor: Observable<Int>
    val bucksWidgetVisibility: Observable<Boolean>
    val payWithRewardsMessage: Observable<String>
    val updateToggle: Observable<Boolean>
}