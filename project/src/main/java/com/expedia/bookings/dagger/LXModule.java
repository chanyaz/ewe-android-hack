package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.LxServices;
import com.expedia.vm.PaymentViewModel;
import com.expedia.bookings.lx.vm.LXCheckoutViewModel;
import com.expedia.bookings.lx.vm.LXCreateTripViewModel;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

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

	@Provides
	@LXScope
	ItinTripServices provideItinTripServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new ItinTripServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
