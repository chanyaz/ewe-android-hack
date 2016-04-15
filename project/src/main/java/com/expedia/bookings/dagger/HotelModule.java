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
import com.expedia.model.UserLoginStateChangedModel;
import com.expedia.vm.PayWithPointsViewModel;
import com.expedia.vm.PaymentWidgetViewModel;
import com.expedia.vm.ShopWithPointsViewModel;
import com.expedia.vm.interfaces.IPayWithPointsViewModel;
import com.expedia.vm.interfaces.IPaymentWidgetViewModel;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class HotelModule {
	@Provides
	@HotelScope
	HotelServices provideHotelServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new HotelServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@HotelScope
	SuggestionV4Services provideHotelSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@HotelScope
	ReviewsServices provideHotelReviewsServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getReviewsEndpointUrl();
		return new ReviewsServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@HotelScope
	LoyaltyServices provideLoyaltyServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new LoyaltyServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@HotelScope
	PaymentModel<HotelCreateTripResponse> providePaymentModel(LoyaltyServices loyaltyServices) {
		return new PaymentModel<>(loyaltyServices);
	}

	@Provides
	@HotelScope
	IPayWithPointsViewModel providePayWithPointsViewModel(Context context,
		PaymentModel<HotelCreateTripResponse> paymentModel, ShopWithPointsViewModel shopWithPointsViewModel) {
		return new PayWithPointsViewModel<>(paymentModel, shopWithPointsViewModel, context);
	}

	@Provides
	@HotelScope
	IPaymentWidgetViewModel providePaymentWidgetViewModel(Context context, PaymentModel<HotelCreateTripResponse> paymentModel, IPayWithPointsViewModel payWithPointsViewModel) {
		return new PaymentWidgetViewModel<>(context, paymentModel, payWithPointsViewModel);
	}

	@Provides
	@HotelScope
	ShopWithPointsViewModel provideShopWithPointsViewModel(Context context, PaymentModel<HotelCreateTripResponse> paymentModel, UserLoginStateChangedModel userLoginChangedModel) {
		return new ShopWithPointsViewModel(context, paymentModel, userLoginChangedModel);
	}
}
