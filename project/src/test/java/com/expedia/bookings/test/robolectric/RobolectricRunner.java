package com.expedia.bookings.test.robolectric;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

import android.app.Application;
import android.support.annotation.NonNull;

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
			OmnitureTestUtils.setNormalAnalyticsProvider();
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
