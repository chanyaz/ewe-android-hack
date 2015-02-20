package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.presenter.CarCheckoutPresenter;
import com.expedia.bookings.presenter.CarsResultsPresenter;
import com.expedia.bookings.widget.CarSuggestionAdapter;
import dagger.Component;

@Singleton
@Component(modules = CarModule.class)
public interface CarComponent {
	void inject(CarCheckoutPresenter presenter);
	void inject(CarsResultsPresenter presenter);
	void inject(CarSuggestionAdapter adapter);
}
