package com.expedia.bookings.dagger;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.flights.utils.FlightServicesManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.BaggageInfoService;
import com.expedia.bookings.services.FlightServices;
import com.expedia.bookings.services.HolidayCalendarService;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.KongFlightServices;
import com.expedia.bookings.services.KrazyglueServices;
import com.expedia.bookings.services.FlightRichContentService;
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder;
import com.expedia.vm.FlightCheckoutViewModel;
import com.expedia.bookings.flights.vm.FlightWebCheckoutViewViewModel;
import com.expedia.vm.PaymentViewModel;
import com.expedia.bookings.flights.vm.FlightCreateTripViewModel;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
public final class FlightModule {

	@Provides
	@FlightScope
	FlightServices provideFlightServices(Context context, EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		boolean isUserBucketedForAPIMAuth = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsAPIKongEndPoint);
		final String kongEndpointUrl = endpointProvider.getKongEndpointUrl();
		final String endpoint = endpointProvider.getE3EndpointUrl();
		List<Interceptor> interceptorList = new ArrayList<>();
		interceptorList.add(interceptor);
		if (isUserBucketedForAPIMAuth) {
			return new KongFlightServices(kongEndpointUrl, client, interceptorList, AndroidSchedulers.mainThread(), Schedulers.io());
		}
		else {
			return new FlightServices(endpoint, client, interceptorList, AndroidSchedulers.mainThread(), Schedulers.io());
		}
	}

	@Provides
	@FlightScope
	FlightServicesManager provideFlightServicesManager(FlightServices flightServices) {
		return new FlightServicesManager(flightServices);
	}

	@Provides
	@FlightScope
	BaggageInfoService provideBaggageInfoService(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new BaggageInfoService(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@FlightScope
	FlightCreateTripViewModel provideFlightCreateTripViewModel(Context context) {
		return new FlightCreateTripViewModel(context);
	}

	@Provides
	@FlightScope
	FlightCheckoutViewModel provideFlightCheckoutViewModel(Context context) {
		return new FlightCheckoutViewModel(context);
	}

	@Provides
	@FlightScope
	PaymentViewModel providePaymentViewModel(Context context) {
		return new PaymentViewModel(context);
	}

	@Provides
	@FlightScope
	FlightSearchTrackingDataBuilder provideFlightTrackingBuilder() {
		return new FlightSearchTrackingDataBuilder();
	}

	@Provides
	@FlightScope
	ItinTripServices provideItinTripServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new ItinTripServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@FlightScope
	KrazyglueServices provideKrazyglueServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getKrazyglueEndpointUrl();
		return new KrazyglueServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@FlightScope
	FlightWebCheckoutViewViewModel provideFlightWebCheckoutViewViewModel(Context context, EndpointProvider endpointProvider) {
		return new FlightWebCheckoutViewViewModel(context, endpointProvider);
	}

	@Provides
	@FlightScope
	FlightRichContentService provideRichContentService(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		List<Interceptor> interceptorList = new ArrayList<>();
		interceptorList.add(interceptor);
		return new FlightRichContentService(endpointProvider.getKongEndpointUrl(), client, interceptorList,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@FlightScope
	HolidayCalendarService provideHolidayCalendarServices(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor) {
		return new HolidayCalendarService(endpointProvider.getKongEndpointUrl(), client, interceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
