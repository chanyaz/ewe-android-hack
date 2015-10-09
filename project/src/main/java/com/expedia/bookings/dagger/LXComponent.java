package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.presenter.lx.LXCheckoutPresenter;
import com.expedia.bookings.presenter.lx.LXDetailsPresenter;
import com.expedia.bookings.presenter.lx.LXResultsPresenter;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.expedia.bookings.widget.LXCheckoutSummaryWidget;
import com.expedia.bookings.widget.LXCheckoutWidget;
import com.expedia.bookings.widget.LXConfirmationWidget;
import com.expedia.bookings.widget.LxSuggestionAdapter;

import dagger.Component;
import rx.Observable;

@LXScope
@Component(dependencies = {AppComponent.class}, modules = {LXModule.class, LXCurrentLocationSuggestionModule.class})
public interface LXComponent {
	void inject(LXResultsPresenter lxResultsPresenter);
	void inject(LXDetailsPresenter lxDetailsPresenter);
	void inject(LXActivityDetailsWidget lxActivityDetailsWidget);
	void inject(LxSuggestionAdapter adapter);
	void inject(LXCheckoutPresenter lxCheckoutPresenter);
	void inject(LXCheckoutWidget lxCheckoutWidget);
	void inject(LXCheckoutSummaryWidget lxCheckoutSummaryWidget);
	void inject(LXConfirmationWidget lxConfirmationWidget);

	Observable<Suggestion> currentLocationSuggestionObservable();
}
