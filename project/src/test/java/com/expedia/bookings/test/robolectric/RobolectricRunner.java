package com.expedia.bookings.test.robolectric;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.mockito.Mockito;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.ReflectionHelpers;

import android.app.Application;
import android.support.annotation.NonNull;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.OmnitureTestUtils;
import com.expedia.bookings.test.RunForBrands;

public class RobolectricRunner extends RobolectricTestRunner {

	public RobolectricRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected final AndroidManifest getAppManifest(Config config) {
		String brandName = BuildConfig.FLAVOR;

		FsFile mani = Fs.fileFromPath("build/intermediates/manifests/full/" + brandName + "/debug/AndroidManifest.xml");
		FsFile res = Fs.fileFromPath("build/intermediates/res/merged/" + brandName + "/debug/");
		FsFile assets = Fs.fileFromPath("build/intermediates/assets/" + brandName + "/debug/");

		return new AndroidManifest(mani, res, assets, "com.expedia.bookings");
	}

	@NonNull
	@Override
	protected Class<? extends TestLifecycle> getTestLifecycleClass() {
		return CustomTestLifecycle.class;
	}

	public static class CustomTestLifecycle extends DefaultTestLifecycle {
		@Override
		public Application createApplication(final Method method, final AndroidManifest appManifest, Config config) {
			return new TestExpediaBookingApp();
		}

		@Override
		public void afterTest(Method method) {
			Mockito.validateMockitoUsage();
			OmnitureTestUtils.setNormalAnalyticsProvider();
			resetWindowManager();
		}

		// from https://github.com/robolectric/robolectric/issues/2068
		private void resetWindowManager() {
			Class clazz = ReflectionHelpers.loadClass(getClass().getClassLoader(), "android.view.WindowManagerGlobal");
			Object instance = ReflectionHelpers.callStaticMethod(clazz, "getInstance");

			// We essentially duplicate what's in {@link WindowManagerGlobal#closeAll} with what's below.
			// The closeAll method has a bit of a bug where it's iterating through the "roots" but
			// bases the number of objects to iterate through by the number of "views." This can result in
			// an {@link java.lang.IndexOutOfBoundsException} being thrown.
			Object lock = ReflectionHelpers.getField(instance, "mLock");

			ArrayList<Object> roots = ReflectionHelpers.getField(instance, "mRoots");
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (lock) {
				for (int i = 0; i < roots.size(); i++) {
					ReflectionHelpers.callInstanceMethod(instance, "removeViewLocked",
						ReflectionHelpers.ClassParameter.from(int.class, i),
						ReflectionHelpers.ClassParameter.from(boolean.class, false));
				}
			}

			// Views will still be held by this array. We need to clear it out to ensure
			// everything is released.
			Collection<View> dyingViews = ReflectionHelpers.getField(instance, "mDyingViews");
			dyingViews.clear();
		}
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		final RunForBrands runClassForBrands = getTestClass().getAnnotation(RunForBrands.class);
		if (runClassForBrands != null) {
			if (!shouldRunForCurrentBrand(runClassForBrands)) {
				return new ArrayList<>();
			}
		}
		List<FrameworkMethod> allMethods = super.computeTestMethods();
		if (allMethods == null || allMethods.size() == 0) {
			return allMethods;
		}

		final List<FrameworkMethod> filteredMethods = new ArrayList<>(allMethods.size());
		for (final FrameworkMethod method : allMethods) {
			final RunForBrands runForBrands = method.getAnnotation(RunForBrands.class);
			if (runForBrands != null) {
				if (shouldRunForCurrentBrand(runForBrands)) {
					filteredMethods.add(method);
				}
			}
			else {
				filteredMethods.add(method);
			}
		}
		return filteredMethods;
	}

	@Override
	protected void validateInstanceMethods(List<Throwable> errors) {
		validatePublicVoidNoArgMethods(After.class, false, errors);
		validatePublicVoidNoArgMethods(Before.class, false, errors);
		validateTestMethods(errors);
	}

	private boolean shouldRunForCurrentBrand(RunForBrands runForBrands) {
		return Arrays.asList(runForBrands.brands()).contains(BuildConfig.APPLICATION_ID);
	}

}
