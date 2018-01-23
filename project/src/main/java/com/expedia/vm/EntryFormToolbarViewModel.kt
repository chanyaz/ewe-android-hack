package com.expedia.vm

import io.reactivex.subjects.PublishSubject

class EntryFormToolbarViewModel {
    val formFilledIn = PublishSubject.create<Boolean>()

    val doneClicked = PublishSubject.create<Unit>()
    val nextClicked = PublishSubject.create<Unit>()
}
