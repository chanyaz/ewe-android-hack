package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.crashlytics.android.Crashlytics;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.util.ForceBucketPref;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import okhttp3.HttpUrl;
import rx.Observer;

public class AbacusHelperUtils {

	// For Abacus bucketing GUID
	private static final String PREF_ABACUS_GUID = "PREF_ABACUS_GUID";


	public static void downloadBucket(Context context) {
		AbacusServices abacus = Ui.getApplication(context).appComponent().abacus();
		AbacusEvaluateQuery query = getQuery(context);
		abacus.downloadBucket(query, getAbacusSubscriber(context));
	}

	private static Observer<AbacusResponse> getAbacusSubscriber(final Context context) {
		return new Observer<AbacusResponse>() {
			@Override
			public void onCompleted() {
				Log.d("AbacusResponse - onCompleted");
			}

			@Override
			public void onError(Throwable e) {
				// onError is called during debugging & cannot connect to dev endpoint
				// but we still want to modify the tests for debugging and QA purposes
				updateAbacus(new AbacusResponse(), context);
				Log.d("AbacusResponse - onError", e);
			}

			@Override
			public void onNext(AbacusResponse abacusResponse) {
				updateAbacus(abacusResponse, context);
				Log.d("AbacusResponse - onNext");
			}
		};
	}

	public static void updateAbacus(AbacusResponse abacusResponse, Context context) {
		if (ExpediaBookingApp.isAutomation()) {
			return;
		}

		updateAbacusResponse(abacusResponse);
		updateForceBucketedTests(context);


		// Modify the bucket values based on dev settings;
		if (BuildConfig.DEBUG) {
			for (Integer key : AbacusUtils.getActiveTests()) {
				Db.sharedInstance.getAbacusResponse().updateABTestForDebug(key, SettingUtils
					.get(context, String.valueOf(key), AbacusUtils.ABTEST_IGNORE_DEBUG));
			}
		}

		Log.v("AbacusData", Db.sharedInstance.getAbacusResponse().toString());
		Crashlytics.log(Db.sharedInstance.getAbacusResponse().toString());
	}

	@VisibleForTesting
	static void updateForceBucketedTests(Context context) {
		// Modify the bucket values based on forced bucket settings;
		if (ForceBucketPref.isForceBucketed(context)) {
			for (Integer key : AbacusUtils.getActiveTests()) {
				int testVal = ForceBucketPref.getForceBucketedTestValue(context, key, AbacusUtils.ABTEST_IGNORE_DEBUG);
				if (testVal != AbacusUtils.ABTEST_IGNORE_DEBUG) {
					Db.sharedInstance.getAbacusResponse().forceUpdateABTest(key, testVal);
				}
			}
		}
	}

	@VisibleForTesting
	static void updateAbacusResponse(AbacusResponse abacusResponse) {
		if (Db.sharedInstance.getAbacusResponse() == null) {
			Db.sharedInstance.setAbacusResponse(abacusResponse);
		}
		else {
			Db.sharedInstance.getAbacusResponse().updateFrom(abacusResponse);
		}
	}

	public static void downloadBucketWithWait(Context context, Observer<AbacusResponse> observer) {
		AbacusServices abacus = Ui.getApplication(context).appComponent().abacus();
		AbacusEvaluateQuery query = getQuery(context);
		if (ExpediaBookingApp.isAutomation()) {
			// under automation, just emulate an immediate timeout event
			observer.onError(new TimeoutException());
		}
		else {
			abacus.downloadBucket(query, observer);
		}
	}

	private static AbacusEvaluateQuery getQuery(Context context) {
		AbacusEvaluateQuery query = new AbacusEvaluateQuery(generateAbacusGuid(context),
			PointOfSale.getPointOfSale().getTpid(), 0);
		if (ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			query.addExperiments(AbacusUtils.getActiveTests());
		}
		else {
			query.addExperiments(new ArrayList<Integer>());
		}
		return query;
	}

	public static synchronized String generateAbacusGuid(Context context) {
		String abacusGuid = SettingUtils.get(context, PREF_ABACUS_GUID, "");
		String mc1Cookie = DebugInfoUtils.getMC1CookieStr(context);
		if (Strings.isEmpty(mc1Cookie)) {
			return mc1CookieAndAbacusGuidNewUuid(context);
		}
		else if (mc1Cookie.contains(abacusGuid)) {
			Db.sharedInstance.setAbacusGuid(abacusGuid);
			return abacusGuid;
		}
		return abacusGuidFromMC1Cookie(context, mc1Cookie);
	}

	private static String mc1CookieAndAbacusGuidNewUuid(Context context) {
		String abacusGuid = UUID.randomUUID().toString().replaceAll("-", "");
		String mc1Cookie = "GUID=" + abacusGuid;
		HttpUrl url = Ui.getApplication(context).appComponent().endpointProvider().getE3EndpointAsHttpUrl();

		String host = url.host();
		CookiesReference cookiesReference = new CookiesReference(context);
		cookiesReference.mCookieManager.setMC1Cookie(mc1Cookie, host);
		SettingUtils.save(context, PREF_ABACUS_GUID, abacusGuid);
		Db.sharedInstance.setAbacusGuid(abacusGuid);
		return abacusGuid;
	}

	private static String abacusGuidFromMC1Cookie(Context context, String mc1Cookie) {
		String abacusGuid = mc1Cookie.replace("GUID=", "");
		SettingUtils.save(context, PREF_ABACUS_GUID, abacusGuid);
		Db.sharedInstance.setAbacusGuid(abacusGuid);
		return abacusGuid;
	}


	public static class CookiesReference {
		@Inject
		public PersistentCookiesCookieJar mCookieManager;

		public CookiesReference(Context context) {
			if (context == null) {
				throw new RuntimeException("Context passed to AbacusHelperUtils.CookiesReference cannot be null!");
			}
			Ui.getApplication(context).appComponent().inject(this);
		}
	}
}
