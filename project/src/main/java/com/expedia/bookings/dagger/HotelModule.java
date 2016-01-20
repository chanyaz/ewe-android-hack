package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.payment.PaymentModel;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.LoyaltyServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.IPayWithPointsViewModel;
import com.expedia.bookings.widget.PayWithPointsViewModel;
import com.expedia.vm.PaymentWidgetViewModel;
import com.expedia.vm.interfaces.IPaymentWidgetViewModel;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class HotelModule {
	@Provides
	@HotelScope
	HotelServices provideHotelServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new HotelServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@HotelScope
	SuggestionV4Services provideHotelSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@HotelScope
	ReviewsServices provideHotelReviewsServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor,
		RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getReviewsEndpointUrl();
		return new ReviewsServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@HotelScope
	LoyaltyServices provideLoyaltyServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new LoyaltyServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@HotelScope
	PaymentModel<HotelCreateTripResponse> providePaymentModel(LoyaltyServices loyaltyServices) {
		return new PaymentModel<>(loyaltyServices);
	}

	@Provides
	@HotelScope
	IPaymentWidgetViewModel providePaymentWidgetViewModel(PaymentModel<HotelCreateTripResponse> paymentModel, Context context) {
		return new PaymentWidgetViewModel<>(paymentModel, context.getResources());
	}

	@Provides
	@HotelScope
	IPayWithPointsViewModel providePayWithPointsViewModel(PaymentModel<HotelCreateTripResponse> paymentModel, Context context) {
		return new PayWithPointsViewModel<>(paymentModel, context.getResources());
	}
}
