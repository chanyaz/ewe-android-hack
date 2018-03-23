package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.presenter.TravelersActivity;
import com.expedia.bookings.presenter.packages.PackageHotelPresenter;
import com.expedia.bookings.presenter.packages.PackagePresenter;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.PackageCheckoutPresenter;
import com.expedia.vm.packages.PackageCheckoutViewModel;

import dagger.Component;

@PackageScope
@Component(dependencies = {AppComponent.class}, modules = {PackageModule.class, FeesModule.class})
public interface PackageComponent extends TravelerActivityComponent{
	void inject(PackagePresenter presenter);
	void inject(PackageHotelPresenter presenter);
	void inject(PackageCheckoutPresenter presenter);
	void inject(PackageCheckoutViewModel model);

	PackageServices packageServices();
	ReviewsServices reviewsServices();
	SuggestionV4Services suggestionsService();
	ItinTripServices itinTripServices();
}
