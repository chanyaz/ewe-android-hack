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
import org.robolectric.TestLifecycle;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

import android.accounts.AccountManager;
import android.app.Application;
import android.os.UserManager;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.test.RunForBrands;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RobolectricRunner extends RobolectricGradleTestRunner {

	public RobolectricRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected final AndroidManifest getAppManifest(Config config) {
		String brandName = BuildConfig.FLAVOR;

		FsFile mani = Fs.fileFromPath("build/intermediates/manifests/full/" + brandName + "/debug/AndroidManifest.xml");
		FsFile res = Fs.fileFromPath("build/intermediates/res/merged/" + brandName + "/debug/");
		FsFile assets = Fs.fileFromPath("build/intermediates/assets/" + brandName + "/debug/");

		AndroidManifest manifest = new AndroidManifest(mani, res, assets);
		manifest.setPackageName("com.expedia.bookings");

		return manifest;
	}

	@Override
	public InstrumentationConfiguration createClassLoaderConfig() {
		InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
		builder.addInstrumentedClass(GoogleCloudMessaging.class.getName());
		builder.addInstrumentedClass(UserManager.class.getName());
		builder.addInstrumentedClass(AccountManager.class.getName());
		return builder.build();
	}

	@Override
	protected Class<? extends TestLifecycle> getTestLifecycleClass() {
		return CustomTestLifecycle.class;
	}

	public static class CustomTestLifecycle extends DefaultTestLifecycle {
		@Override
		public Application createApplication(final Method method, final AndroidManifest appManifest, Config config) {
			return new TestExpediaBookingApp();
		}
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		final RunForBrands runClassForBrands = getTestClass().getAnnotation(RunForBrands.class);
		if (runClassForBrands != null) {
			if (!shouldRunForCurrentBrand(runClassForBrands)) {
				return new ArrayList<FrameworkMethod>();
			}
		}
		List<FrameworkMethod> allMethods = super.computeTestMethods();
		if (allMethods == null || allMethods.size() == 0) {
			return allMethods;
		}

		final List<FrameworkMethod> filteredMethods = new ArrayList<FrameworkMethod>(allMethods.size());
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
