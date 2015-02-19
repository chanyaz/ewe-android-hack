package com.expedia.bookings.data.lx;

import android.content.Context;

import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.utils.DbUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class LXDb {
	private static LXServices lxServices;

	public static void inject(Context context) {
		String e3endpoint = EndPoint.getE3EndpointUrl(context, false);
		lxServices = generateLXServices(e3endpoint);
	}

	private static LXServices generateLXServices(String endpoint) {
		return new LXServices(endpoint, DbUtils.generateOkHttpClient(), AndroidSchedulers.mainThread(),
			Schedulers.io());
	}

	public static LXServices getLxServices() {
		return lxServices;
	}
}
