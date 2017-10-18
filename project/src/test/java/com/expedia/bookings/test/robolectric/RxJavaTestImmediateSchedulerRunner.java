package com.expedia.bookings.test.robolectric;

import java.util.concurrent.Callable;

import org.junit.runners.model.InitializationError;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxJavaTestImmediateSchedulerRunner extends RobolectricRunner {

	public RxJavaTestImmediateSchedulerRunner(Class<?> testClass) throws InitializationError {
		super(testClass);

		//TODO PUK: to be fixed
		RxAndroidPlugins.reset();
		RxAndroidPlugins.setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
			@Override
			public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable) throws Exception {
				return Schedulers.trampoline();
			}
		});
		RxAndroidPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
			@Override
			public Scheduler apply(@NonNull Scheduler scheduler) throws Exception {
				return Schedulers.trampoline();
			}
		});
	}
}
