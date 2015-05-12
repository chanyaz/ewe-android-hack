package com.expedia.bookings.test.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.ClassInfo;
import org.robolectric.internal.bytecode.InstrumentingClassLoaderConfig;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

import com.squareup.leakcanary.LeakCanary;

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
	protected ClassLoader createRobolectricClassLoader(InstrumentingClassLoaderConfig config, SdkConfig sdkConfig) {
		return super.createRobolectricClassLoader(new DirtyInstrumentingConfig(config), sdkConfig);
	}

	public static class DirtyInstrumentingConfig extends InstrumentingClassLoaderConfig {
		private InstrumentingClassLoaderConfig original;

		public DirtyInstrumentingConfig(InstrumentingClassLoaderConfig original) {
			this.original = original;
		}

		@Override
		public boolean shouldInstrument(ClassInfo info) {
			return original.shouldInstrument(info) || info.getName().equals(LeakCanary.class.getName());
		}
	}
}
