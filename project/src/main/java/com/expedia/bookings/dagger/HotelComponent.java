package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter;
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter;
import com.expedia.bookings.presenter.hotel.HotelConfirmationPresenter;
import com.expedia.bookings.presenter.hotel.HotelPresenter;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.vm.HotelCheckoutViewModel;

import dagger.Component;

@HotelScope
@Component(dependencies = {AppComponent.class}, modules = {HotelModule.class})
public interface HotelComponent {
	void inject(HotelPresenter presenter);
	void inject(HotelCheckoutPresenter presenter);
	void inject(HotelCheckoutMainViewPresenter hotelCheckoutWidget);
	void inject(HotelCheckoutViewModel hotelCheckoutViewModel);
	void inject(HotelConfirmationPresenter hotelConfirmationPresenter);

	SuggestionV4Services suggestionsService();
	HotelServices hotelServices();
	ReviewsServices reviewsServices();
}
