package com.expedia.bookings.test.robolectric;

import org.junit.runners.model.InitializationError;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class RxJavaTestImmediateSchedulerRunner extends RobolectricRunner {

	public RxJavaTestImmediateSchedulerRunner(Class<?> testClass) throws InitializationError {
		super(testClass);

		Function<Scheduler, Scheduler> mRxJavaImmediateScheduler = new Function<Scheduler, Scheduler>() {
			@Override
			public Scheduler apply(Scheduler scheduler) throws Exception {
				return Schedulers.trampoline();
			}
		};

		RxAndroidPlugins.reset();
		RxAndroidPlugins.setMainThreadSchedulerHandler(mRxJavaImmediateScheduler);

		RxJavaPlugins.reset();
		RxJavaPlugins.setIoSchedulerHandler(mRxJavaImmediateScheduler);
		RxJavaPlugins.setNewThreadSchedulerHandler(mRxJavaImmediateScheduler);
	}
}
