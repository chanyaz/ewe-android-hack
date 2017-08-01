package com.expedia.bookings.dagger;

import javax.inject.Named;
import android.content.Context;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.FlightServices;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder;
import com.expedia.vm.FlightCheckoutViewModel;
import com.expedia.vm.PaymentViewModel;
import com.expedia.vm.flights.FlightCreateTripViewModel;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Module
public final class FlightModule {

	@Provides
	@FlightScope
	FlightServices provideFlightServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new FlightServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@FlightScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("ESSInterceptor") Interceptor essRequestInterceptor, @Named("GaiaInterceptor") Interceptor gaiaRequestInterceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new SuggestionV4Services(essEndpoint, gaiaEndpoint, client,
			interceptor, essRequestInterceptor, gaiaRequestInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
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
}
