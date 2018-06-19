package com.expedia.bookings.dagger;

import javax.inject.Named;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.packages.presenter.PackageHotelPresenter;
import com.expedia.bookings.packages.presenter.PackageOverviewPresenter;
import com.expedia.bookings.packages.presenter.PackagePresenter;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.packages.presenter.PackageCheckoutPresenter;
import com.expedia.bookings.packages.vm.PackageCheckoutViewModel;

import dagger.Component;
import okhttp3.Interceptor;

@PackageScope
@Component(dependencies = {AppComponent.class}, modules = {PackageModule.class, FeesModule.class})
public interface PackageComponent {
	void inject(PackagePresenter presenter);
	void inject(PackageHotelPresenter presenter);
	void inject(PackageCheckoutPresenter presenter);
	void inject(PackageCheckoutViewModel model);
	void inject(PackageOverviewPresenter packageWebCheckoutViewViewModel);

	@Named("PackageReviewsInterceptor")
	Interceptor packageReviewsInterceptor();

	PackageServices packageServices();
	ReviewsServices packageReviewsServices();
	SuggestionV4Services suggestionsService();
	ItinTripServices itinTripServices();
}
