package com.expedia.account.newsignin.viewmodel

import io.reactivex.subjects.PublishSubject

class MultipleSignInOptionsLayoutViewModel {

    val facebookSignInButtonClickObservable = PublishSubject.create<Unit>()
    val googleSignInButtonObservable = PublishSubject.create<Unit>()
}
