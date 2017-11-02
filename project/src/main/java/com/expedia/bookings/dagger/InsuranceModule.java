package com.expedia.bookings.dagger;

import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.InsuranceServices;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Module
public final class InsuranceModule {

	@Provides
	@Reusable
	InsuranceServices provideInsurance(OkHttpClient client, EndpointProvider endpointProvider, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new InsuranceServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}


}
