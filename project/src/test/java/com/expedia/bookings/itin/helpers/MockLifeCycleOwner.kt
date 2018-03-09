package com.expedia.bookings.itin.helpers

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner

class MockLifecycleOwner : LifecycleOwner {
    override fun getLifecycle(): Lifecycle {
        return MockLifecycle()
    }

    inner class MockLifecycle : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) {
        }

        override fun removeObserver(observer: LifecycleObserver) {
        }

        override fun getCurrentState(): State {
            return State.RESUMED
        }
    }
}
