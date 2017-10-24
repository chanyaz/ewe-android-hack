package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.hotel.activity.HotelAppWidgetConfigureActivity;
import com.expedia.bookings.hotel.activity.HotelCompareResultsActivity;
import com.expedia.bookings.hotel.activity.HotelDetailedCompareActivity;
import com.expedia.bookings.hotel.service.HotelPriceJobService;
import com.expedia.bookings.presenter.hotel.HotelCheckoutMainViewPresenter;
import com.expedia.bookings.presenter.hotel.HotelCheckoutPresenter;
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
import com.expedia.vm.interfaces.IPayWithPointsViewModel;

import org.jetbrains.annotations.NotNull;

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

	SuggestionV4Services suggestionsService();
	ReviewsServices reviewsServices();
	IPayWithPointsViewModel payWithPointsViewModel();

    void inject(@NotNull HotelCompareResultsActivity hotelCompareResultsActivity);
    void inject(@NotNull HotelDetailedCompareActivity hotelDetailedCompareActivity);

    void inject(@NotNull HotelPriceJobService hotelPriceJobService);

    void inject(@NotNull HotelAppWidgetConfigureActivity hotelAppWidgetConfigureActivity);
}
