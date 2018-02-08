package com.expedia.bookings.dagger;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CardFeeService;
import com.expedia.bookings.utils.HMACInterceptor;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
public class FeesModule {

	@Provides
	@Reusable
	CardFeeService provideCardFeeService(Context context, EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, HMACInterceptor hmacInterceptor) {
		boolean isUserBucketedForAPIMAuth = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsAPIKongEndPoint);
		final String kongEndpointUrl = endpointProvider.getKongEndpointUrl();
		final String endpoint = endpointProvider.getE3EndpointUrl();
		List<Interceptor> interceptorList = new ArrayList<>();
		interceptorList.add(interceptor);
		if (isUserBucketedForAPIMAuth) {
			interceptorList.add(hmacInterceptor);
		}
		return new CardFeeService(isUserBucketedForAPIMAuth ? kongEndpointUrl : endpoint, client,
			interceptorList, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
