package com.expedia.bookings.test.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.shadows.ShadowLooper
import rx.Scheduler
import rx.android.plugins.RxAndroidPlugins
import rx.android.plugins.RxAndroidSchedulersHook
import rx.functions.Func1
import rx.plugins.RxJavaHooks
import rx.schedulers.Schedulers

class RxJavaImmediateSchedulerRule: TestRule {

    private val mRxAndroidSchedulersHook = object : RxAndroidSchedulersHook() {
        override fun getMainThreadScheduler(): Scheduler {
            return Schedulers.immediate()
        }
    }

    private val mRxJavaImmediateScheduler = Func1<Scheduler, Scheduler> { Schedulers.immediate() }

    override fun apply(base: Statement, description: Description): Statement {

        return object : Statement() {

            override fun evaluate() { RxAndroidPlugins.getInstance().reset()
                ShadowLooper.resetThreadLoopers()
                RxAndroidPlugins.getInstance().reset()
                RxJavaHooks.reset()

                RxAndroidPlugins.getInstance().registerSchedulersHook(mRxAndroidSchedulersHook)

                RxJavaHooks.setOnIOScheduler(mRxJavaImmediateScheduler)
                RxJavaHooks.setOnNewThreadScheduler(mRxJavaImmediateScheduler)

                base.evaluate()

                RxAndroidPlugins.getInstance().reset()
                RxJavaHooks.reset()
            }
        }
    }
}
