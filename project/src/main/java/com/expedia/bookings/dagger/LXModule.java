package com.expedia.bookings.dagger;

import android.content.Context;
import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.vm.PaymentViewModel;
import com.expedia.vm.lx.LXCheckoutViewModel;
import com.expedia.vm.lx.LXCreateTripViewModel;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class LXModule {
	@Provides
	@LXScope
	LxServices provideLxServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new LxServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	SuggestionV4Services provideLXSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("ESSInterceptor") Interceptor essRequestInterceptor, @Named("GaiaInterceptor") Interceptor gaiaRequestInterceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new SuggestionV4Services(essEndpoint, gaiaEndpoint, client,
			interceptor, essRequestInterceptor, gaiaRequestInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	LXState provideLXState() {
		return new LXState();
	}

	@Provides
	@LXScope
	LXCreateTripViewModel provideLXCreateTripViewModel(Context context) {
		return new LXCreateTripViewModel(context);
	}

	@Provides
	@LXScope
	LXCheckoutViewModel provideLXCheckoutViewModel(Context context) {
		return new LXCheckoutViewModel(context);
	}

	@Provides
	@LXScope
	PaymentViewModel providePaymentViewModel(Context context) {
		return new PaymentViewModel(context);
	}

}
