package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;

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
import com.expedia.util.ForceBucketPref;
import com.expedia.bookings.server.PersistentCookieManager;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import javax.inject.Inject;

import rx.Observer;

public class AbacusHelperUtils {

	// For Abacus bucketing GUID
	private static final String PREF_ABACUS_GUID = "PREF_ABACUS_GUID";


	public static void downloadBucket(Context context) {
		AbacusServices abacus = Ui.getApplication(context).appComponent().abacus();
		AbacusEvaluateQuery query = new AbacusEvaluateQuery(generateAbacusGuid(context), PointOfSale.getPointOfSale().getTpid(), 0);
		if (ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			query.addExperiments(AbacusUtils.getActiveTests());
		}
		else {
			query.addExperiments(new ArrayList<Integer>());
		}
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

		if (Db.getAbacusResponse() == null) {
			Db.setAbacusResponse(abacusResponse);
		}
		else {
			Db.getAbacusResponse().updateFrom(abacusResponse);
		}

		// Modify the bucket values based on forced bucket settings;
		if (ForceBucketPref.isForceBucketed(context)) {
			for (int key : AbacusUtils.getActiveTests()) {
				Db.getAbacusResponse().updateABTest(key, ForceBucketPref.getForceBucketedTestValue(context, String.valueOf(key),
					AbacusUtils.ABTEST_IGNORE_DEBUG));
			}
		}

		// Modify the bucket values based on dev settings;
		if (BuildConfig.DEBUG) {
			for (int key : AbacusUtils.getActiveTests()) {
				Db.getAbacusResponse().updateABTestForDebug(key, SettingUtils
					.get(context, String.valueOf(key), AbacusUtils.ABTEST_IGNORE_DEBUG));
			}
		}

		Log.v("AbacusData", Db.getAbacusResponse().toString());
		Crashlytics.log(Db.getAbacusResponse().toString());
	}

	public static synchronized String generateAbacusGuid(Context context) {
		String abacusGuid = SettingUtils.get(context, PREF_ABACUS_GUID, "");
		String mc1Cookie = DebugInfoUtils.getMC1CookieStr(context);
		if (Strings.isEmpty(mc1Cookie)) {
			return mc1CookieAndAbacusGuidNewUuid(context);
		}
		else if (mc1Cookie.contains(abacusGuid)) {
			Db.setAbacusGuid(abacusGuid);
			return abacusGuid;
		}
		return abacusGuidFromMC1Cookie(context, mc1Cookie);
	}

	private static String mc1CookieAndAbacusGuidNewUuid(Context context) {
		String abacusGuid = UUID.randomUUID().toString().replaceAll("-", "");
		String mc1Cookie = "GUID=" + abacusGuid;
		CookiesReference cookiesReference = new CookiesReference(context);
		cookiesReference.mCookieManager.setMC1Cookie(mc1Cookie, getPosUrl());
		SettingUtils.save(context, PREF_ABACUS_GUID, abacusGuid);
		Db.setAbacusGuid(abacusGuid);
		return abacusGuid;
	}

	private static String abacusGuidFromMC1Cookie(Context context, String mc1Cookie) {
		String abacusGuid = mc1Cookie.replace("GUID=", "");
		SettingUtils.save(context, PREF_ABACUS_GUID, abacusGuid);
		Db.setAbacusGuid(abacusGuid);
		return abacusGuid;
	}

	private static String getPosUrl() {
		PointOfSale info = PointOfSale.getPointOfSale();
		return info.getUrl();
	}

	public static class CookiesReference {
		@Inject
		public PersistentCookieManager mCookieManager;

		public CookiesReference(Context context) {
			if (context == null) {
				throw new RuntimeException("Context passed to AbacusHelperUtils.CookiesReference cannot be null!");
			}
			Ui.getApplication(context).appComponent().inject(this);
		}
	}
}
