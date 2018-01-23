package com.expedia.model

import io.reactivex.subjects.PublishSubject

class UserLoginStateChangedModel {
    val userLoginStateChanged = PublishSubject.create<Boolean>()
}
