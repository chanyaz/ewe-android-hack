package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.utils.ServicesUtil;
import com.mobiata.android.util.SettingUtils;

import junit.framework.Assert;

@RunWith(RobolectricRunner.class)
public class XebClientHeaderTest {

	private static final String APP_VERSION = "APP_VERSION";

	private Context context;

	@Before
	public void before() {
		context = RuntimeEnvironment.application;
		SettingUtils.remove(context, APP_VERSION);
	}

	@Test
	public void testUpgradeInfoForNewInstall() {
		Assert.assertFalse(isUpgrade());
	}

	@Test
	public void testUpgradeInfoForNoUpgrade() {
		SettingUtils.save(context, APP_VERSION, BuildConfig.VERSION_NAME);
		Assert.assertFalse(isUpgrade());
	}

	@Test
	public void testUpgradeInfoForUpgrade() {
		SettingUtils.save(context, APP_VERSION, "1.0");
		Assert.assertTrue(isUpgrade());
		Assert.assertFalse(isUpgrade());
		Assert.assertFalse(isUpgrade());
	}

	private boolean isUpgrade() {
		String xEbClientString = ServicesUtil.generateXEbClientString(context);
		return xEbClientString.contains(";UPGRADE:true;");
	}
}
