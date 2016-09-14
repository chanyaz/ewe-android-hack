package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.RailScope;
import com.expedia.bookings.presenter.rail.RailCheckoutPresenter;
import com.expedia.bookings.presenter.rail.RailDetailsPresenter;
import com.expedia.bookings.presenter.rail.RailPresenter;
import com.expedia.bookings.presenter.rail.RailResultsPresenter;
import com.expedia.bookings.presenter.rail.RailSearchPresenter;
import com.expedia.bookings.presenter.rail.RailTripOverviewPresenter;
import com.expedia.bookings.services.RailServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.RailCardsPickerWidget;
import com.expedia.vm.rail.RailCheckoutViewModel;

import dagger.Component;

@RailScope
@Component(dependencies = {AppComponent.class}, modules = {RailModule.class})
public interface RailComponent {
	void inject(RailPresenter presenter);
	void inject(RailSearchPresenter presenter);
	void inject(RailResultsPresenter presenter);
	void inject(RailDetailsPresenter presenter);
	void inject(RailTripOverviewPresenter presenter);
	void inject(RailCheckoutPresenter presenter);
	void inject(RailCardsPickerWidget railCardsPickerWidget);
	void inject(RailCheckoutViewModel viewModel);

	SuggestionV4Services suggestionsService();
	RailServices railService();
}
