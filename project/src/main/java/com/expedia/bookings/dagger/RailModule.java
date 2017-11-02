package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.RailScope;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.RailCardFeeServiceProvider;
import com.expedia.bookings.services.RailServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.utils.HMACInterceptor;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.vm.PaymentViewModel;

import java.io.IOException;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@Module
public final class RailModule {

	@Provides
	@RailScope
	RailServices provideRailServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor,
		@Named("RailInterceptor") Interceptor railRequestInterceptor, HMACInterceptor hmacInterceptor) {
		boolean isUserBucketedInAPIMAuth = AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppAPIMAuth);
		return new RailServices(endpointProvider.getRailEndpointUrl(), client, interceptor, railRequestInterceptor, hmacInterceptor, isUserBucketedInAPIMAuth,
				AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@RailScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("ESSInterceptor") Interceptor essRequestInterceptor, @Named("GaiaInterceptor") Interceptor gaiaRequestInterceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new SuggestionV4Services(essEndpoint, gaiaEndpoint, client,
			interceptor, essRequestInterceptor, gaiaRequestInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@RailScope
	PaymentViewModel providePaymentViewModel(Context context) {
		return new PaymentViewModel(context);
	}

	@Provides
	@RailScope
	RailCardFeeServiceProvider provideCardFeeServiceProvider() {
		return new RailCardFeeServiceProvider();
	}

	@Provides
	@RailScope
	@Named("RailInterceptor")
	Interceptor provideRailRequestInterceptor(final Context context, final EndpointProvider endpointProvider) {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				Request.Builder request = chain.request().newBuilder();
				request.addHeader("key", ServicesUtil.getRailApiKey(context));
				Response response = chain.proceed(request.build());
				return response;
			}
		};
	}
}

