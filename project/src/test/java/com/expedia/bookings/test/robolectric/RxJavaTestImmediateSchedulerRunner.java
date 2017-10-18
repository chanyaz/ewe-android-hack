package com.expedia.bookings.test.robolectric;

import org.junit.runners.model.InitializationError;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.functions.Func1;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

public class RxJavaTestImmediateSchedulerRunner extends RobolectricRunner {

	private final RxAndroidSchedulersHook mRxAndroidSchedulersHook = new RxAndroidSchedulersHook() {
		@Override
		public Scheduler getMainThreadScheduler() {
			return Schedulers.immediate();
		}
	};

	private final Func1<Scheduler, Scheduler> mRxJavaImmediateScheduler =
		new Func1<Scheduler, Scheduler>() {
			@Override
			public Scheduler call(Scheduler scheduler) {
				return Schedulers.immediate();
			}
		};

	public RxJavaTestImmediateSchedulerRunner(Class<?> testClass) throws InitializationError {
		super(testClass);

		RxAndroidPlugins.getInstance().reset();
		RxAndroidPlugins.getInstance().registerSchedulersHook(mRxAndroidSchedulersHook);

		RxJavaHooks.reset();
		RxJavaHooks.setOnIOScheduler(mRxJavaImmediateScheduler);
		RxJavaHooks.setOnNewThreadScheduler(mRxJavaImmediateScheduler);
	}
}
