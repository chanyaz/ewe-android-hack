package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.CarScope;
import com.expedia.bookings.presenter.car.CarCheckoutPresenter;
import com.expedia.bookings.presenter.car.CarResultsPresenter;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.CarCheckoutMainViewPresenter;
import dagger.Component;

@CarScope
@Component(dependencies = {AppComponent.class}, modules = {CarModule.class})
public interface CarComponent {
	void inject(CarCheckoutPresenter presenter);
	void inject(CarResultsPresenter presenter);
	void inject(CarCheckoutMainViewPresenter checkoutWidget);

	SuggestionV4Services suggestionsService();
	CarServices getCarServices();
}
