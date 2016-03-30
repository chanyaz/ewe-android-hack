package com.expedia.model

import rx.subjects.PublishSubject

class UserLoginStateChangedModel {
    val userLoginStateChanged = PublishSubject.create<Boolean>()
}