package com.expedia.bookings.test.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

public class RobolectricSubmoduleTestRunner extends RobolectricTestRunner {

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
		AndroidManifest manifest = new AndroidManifest(Fs.fileFromPath(getAndroidManifestPath()), Fs.fileFromPath(getResPath()), Fs.fileFromPath(getAssetsPath())) {
			@Override
			public int getTargetSdkVersion() {
				return MAX_SDK_SUPPORTED_BY_ROBOLECTRIC;
			}
		};

		manifest.setPackageName("com.expedia.bookings");
		return manifest;
	}
}
