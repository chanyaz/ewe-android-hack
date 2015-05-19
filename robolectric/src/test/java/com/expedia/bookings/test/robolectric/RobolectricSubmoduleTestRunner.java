package com.expedia.bookings.test.robolectric;

import java.lang.reflect.Method;

import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.TestLifecycle;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import android.app.Application;

public class RobolectricSubmoduleTestRunner extends RobolectricGradleTestRunner {

	private static final int MAX_SDK_SUPPORTED_BY_ROBOLECTRIC = 18;

	public RobolectricSubmoduleTestRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	protected String getAndroidManifestPath() {
		return "../project/build/intermediates/manifests/full/expedia/debug/AndroidManifest.xml";
	}

	protected String getResPath() {
		return "../project/build/intermediates/res/expedia/debug/";
	}

	protected String getAssetsPath() {
		return "../project/build/intermediates/assets/expedia/debug/";
	}

	@Override
	protected final AndroidManifest getAppManifest(Config config) {
		AndroidManifest manifest = new AndroidManifest(Fs.fileFromPath(getAndroidManifestPath()),
			Fs.fileFromPath(getResPath()), Fs.fileFromPath(getAssetsPath())) {
			@Override
			public int getTargetSdkVersion() {
				return MAX_SDK_SUPPORTED_BY_ROBOLECTRIC;
			}
		};

		manifest.setPackageName("com.expedia.bookings");
		return manifest;
	}

	@Override
	protected Class<? extends TestLifecycle> getTestLifecycleClass() {
		return SubmoduleTestLifecycle.class;
	}

	public static class SubmoduleTestLifecycle extends DefaultTestLifecycle {
		@Override
		public Application createApplication(final Method method, final AndroidManifest appManifest, Config config) {
			return new TestExpediaBookingApp();
		}
	}
}
