package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.packages.util.PackageServicesManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.vm.PaymentViewModel;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
public final class PackageModule {
	@Provides
	@PackageScope
	PackageServices providePackageServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new PackageServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@PackageScope
	ReviewsServices provideReviewsServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getReviewsEndpointUrl();
		return new ReviewsServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@PackageScope
	PaymentViewModel providePaymentViewModel(Context context) {
		return new PaymentViewModel(context);
	}

	@Provides
	@PackageScope
	PackageServicesManager providePackageServicesManager(Context context, PackageServices packageServices) {
		return new PackageServicesManager(context, packageServices);
	}

	@Provides
	@PackageScope
	ItinTripServices provideItinTripServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new ItinTripServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}

