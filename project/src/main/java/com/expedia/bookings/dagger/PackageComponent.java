package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.presenter.packages.PackageHotelPresenter;
import com.expedia.bookings.presenter.packages.PackagePresenter;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Component;

@PackageScope
@Component(dependencies = {AppComponent.class}, modules = {PackageModule.class})
public interface PackageComponent {
	void inject(PackagePresenter presenter);
	void inject(PackageHotelPresenter presenter);
	PackageServices packageServices();
	ReviewsServices reviewsServices();
	SuggestionV4Services suggestionsService();
}
