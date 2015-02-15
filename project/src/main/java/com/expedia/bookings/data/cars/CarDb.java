package com.expedia.bookings.data.cars;

import android.content.Context;

import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.DbUtils;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;

import retrofit.RequestInterceptor;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class CarDb {

	private static CarServices sCarServices;
	private static SuggestionServices sSuggestionServices;

	public static void inject(final Context context) {
		String e3endpoint = EndPoint.getE3EndpointUrl(context, true /*isSecure*/);

		// Add params to every CarApi request
		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addEncodedQueryParam("clientid", ServicesUtil.generateClientId(context));
				request.addEncodedQueryParam("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					request.addEncodedQueryParam("langid", langid);
				}

				if (EndPoint.requestRequiresSiteId(context)) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}
			}
		};
		sCarServices = generateCarServices(e3endpoint, requestInterceptor);

		String suggestEndpoint = EndPoint.getEssEndpointUrl(context, true /*isSecure*/);
		sSuggestionServices = generateCarSuggestionServices(suggestEndpoint);
	}


	private static CarServices generateCarServices(String endpoint, RequestInterceptor requestInterceptor) {
		return new CarServices(endpoint, DbUtils.generateOkHttpClient(), requestInterceptor, AndroidSchedulers.mainThread(),
			Schedulers.io());
	}

	public static SuggestionServices generateCarSuggestionServices(String endpoint) {
		return new SuggestionServices(endpoint, DbUtils.generateOkHttpClient(), AndroidSchedulers.mainThread(),
			Schedulers.io());
	}

	public static CarServices getCarServices() {
		return sCarServices;
	}

	public static SuggestionServices getSuggestionServices() {
		return sSuggestionServices;
	}

}
