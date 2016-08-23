package com.expedia.bookings.utils;

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
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.PersistentCookieManager;
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
		query.addExperiments(AbacusUtils.getActiveTests());
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
		else if (abacusGuid.equals(mc1Cookie)) {
			Db.setAbacusGuid(abacusGuid);
			return abacusGuid;
		}
		return abacusGuidToMC1Cookie(context, mc1Cookie);
	}

	private static String mc1CookieAndAbacusGuidNewUuid(Context context) {
		String guid = "GUID=" + UUID.randomUUID().toString().replaceAll("-", "");
		CookiesReference cookiesReference = new CookiesReference(context);
		cookiesReference.mCookieManager.setMC1Cookie(guid, getPosUrl());
		SettingUtils.save(context, PREF_ABACUS_GUID, guid);
		Db.setAbacusGuid(guid);
		return guid;
	}

	private static String abacusGuidToMC1Cookie(Context context, String mc1Cookie) {
		SettingUtils.save(context, PREF_ABACUS_GUID, mc1Cookie);
		Db.setAbacusGuid(mc1Cookie);
		return mc1Cookie;
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
