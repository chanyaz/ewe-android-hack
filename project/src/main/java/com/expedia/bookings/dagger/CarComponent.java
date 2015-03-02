package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.presenter.CarCheckoutPresenter;
import com.expedia.bookings.presenter.CarResultsPresenter;
import com.expedia.bookings.widget.CarSuggestionAdapter;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, CarModule.class})
public interface CarComponent {
	void inject(CarCheckoutPresenter presenter);
	void inject(CarResultsPresenter presenter);
	void inject(CarSuggestionAdapter adapter);
}
