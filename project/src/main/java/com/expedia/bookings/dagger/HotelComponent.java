package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter;
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter;
import com.expedia.bookings.presenter.hotel.HotelDetailPresenter;
import com.expedia.bookings.presenter.hotel.HotelPresenter;
import com.expedia.bookings.presenter.hotel.HotelResultsPresenter;
import com.expedia.bookings.presenter.hotel.HotelSearchPresenter;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.BucksWidget;
import com.expedia.bookings.widget.CouponWidget;
import com.expedia.bookings.widget.PayWithPointsWidget;
import com.expedia.bookings.widget.PaymentWidgetV2;
import com.expedia.bookings.widget.ShopWithPointsWidget;
import com.expedia.bookings.widget.itin.HotelItinContentGenerator;
import com.expedia.vm.HotelConfirmationViewModel;
import com.expedia.vm.HotelSearchViewModel;
import com.expedia.vm.hotel.HotelViewModel;
import com.expedia.vm.interfaces.IPayWithPointsViewModel;

import dagger.Component;

@HotelScope
@Component(dependencies = {AppComponent.class}, modules = {HotelModule.class})
public interface HotelComponent {
	void inject(HotelPresenter presenter);
	void inject(HotelSearchPresenter presenter);
	void inject(HotelItinContentGenerator presenter);
	void inject(HotelCheckoutMainViewPresenter hotelCheckoutWidget);
	void inject(PaymentWidgetV2 paymentWidget);
	void inject(PayWithPointsWidget payWithPointsWidget);
	void inject(BucksWidget bucksWidget);
	void inject(HotelCheckoutPresenter hotelCheckoutPresenter);
	void inject(CouponWidget couponWidget);
	void inject(HotelConfirmationViewModel hotelConfirmationViewModel);
	void inject(ShopWithPointsWidget shopWithPointsWidget);
	void inject(HotelSearchViewModel hotelSearchViewModel);
	void inject(HotelResultsPresenter hotelResultsPresenter);
	void inject(HotelDetailPresenter hotelDetailPresenter);

	SuggestionV4Services suggestionsService();
	ReviewsServices reviewsServices();
	IPayWithPointsViewModel payWithPointsViewModel();
}
