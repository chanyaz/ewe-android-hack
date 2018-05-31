package com.expedia.bookings.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.test.espresso.Espresso;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;
import com.expedia.bookings.utils.ExpediaNetUtils;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class TestBootstrap extends ActivityInstrumentationTestCase2<RouterActivity> {

	public static Activity mActivity;

	public TestBootstrap() {
		super(RouterActivity.class);
	}

	//tagged hook to set test environment to Int before running matching scenarios
	@Before("@Int")
	public void setupForTestsOnIntegration() throws Exception {
		Settings.setServer("Integration");
	}

	@Before("@Prod")
	public void setupForTestsOnProduction() throws Exception {
		Settings.setServer("Production");
	}

	@Before
	public void setUp() throws Exception {
		//clear private data
		Settings.clearPrivateData();

		Settings.setMockModeEndPoint();
		Settings.setOnboardingScreenVisibility(false);
		super.setUp();
		mActivity = getActivity();
		assertNotNull(mActivity);

		//Ensure Facebook process isn't running and data is wiped.
		//This will allow a login screen to appear for every test
		String packageName = "com.facebook.katana";
		if (Common.isPackageInstalled(packageName)) {
			Common.forceStopProcess("com.facebook.katana");
			Common.clearProcessData("com.facebook.katana");
		}
	}

	@After
	public void tearDown(Scenario scenario) throws Exception {
		if (scenario.isFailed()) {
			takeScreenShot(scenario);
		}
		Espresso.closeSoftKeyboard();
		ActivityFinisher.finishOpenActivities();
		getActivity().finish();
		ExpediaNetUtils.setFake(true, true);
		//clear map
		TestUtil.storeDataAtRuntime.clear();

		Settings.setFakeCurrentLocation("0", "0");

		//set US locale and POS
		Common.setLocale(new Locale("en", "US"));

		//In FeatureConfiguration for Expedia, Default POS is set as UNITED_KINGDOM
		if (BuildConfig.brand.equals("Expedia")) {
			Common.setPOS(PointOfSaleId.UNITED_STATES);
		}
		else {
			Common.setPOS(ProductFlavorFeatureConfiguration.getInstance().getDefaultPOS());
		}

		super.tearDown();
	}

	private void takeScreenShot(Scenario scenario) throws IOException {
		View rootView = SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView().getRootView();
		View screenView = rootView.getRootView();
		screenView.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
		screenView.setDrawingCacheEnabled(false);
		saveFailedTestCaseImage(bitmap, scenario.getId() + ".png");
	}

	private void saveFailedTestCaseImage(Bitmap bm, String fileName) throws IOException {
		String cucumberImagesDirectoryPath = mActivity.getApplicationContext().getFilesDir().getPath() + "/cucumber-images/";
		File cucumberDirectory = new File(cucumberImagesDirectoryPath);
		if (!cucumberDirectory.exists()) {
			cucumberDirectory.mkdirs();
		}
		File file = new File(cucumberImagesDirectoryPath + fileName);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
