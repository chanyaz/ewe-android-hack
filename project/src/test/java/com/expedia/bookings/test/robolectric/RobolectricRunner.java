package com.expedia.bookings.test.robolectric;

import java.lang.reflect.Method;

import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.TestLifecycle;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

import android.app.Application;

public class RobolectricRunner extends RobolectricGradleTestRunner {

	public RobolectricRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected final AndroidManifest getAppManifest(Config config) {
		FsFile mani = Fs.fileFromPath("build/intermediates/manifests/full/expedia/debug/AndroidManifest.xml");
		FsFile res = Fs.fileFromPath("build/intermediates/res/merged/expedia/debug/");
		FsFile assets = Fs.fileFromPath("build/intermediates/assets/expedia/debug/");

		AndroidManifest manifest = new AndroidManifest(mani, res, assets);
		manifest.setPackageName("com.expedia.bookings");

		return manifest;
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
}
