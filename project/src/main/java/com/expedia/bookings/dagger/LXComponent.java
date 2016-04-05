package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.presenter.lx.LXCheckoutPresenter;
import com.expedia.bookings.presenter.lx.LXDetailsPresenter;
import com.expedia.bookings.presenter.lx.LXPresenter;
import com.expedia.bookings.presenter.lx.LXResultsPresenter;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.expedia.bookings.widget.LXCheckoutSummaryWidget;
import com.expedia.bookings.widget.LXCheckoutMainViewPresenter;
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
	void inject(LXCheckoutMainViewPresenter lxCheckoutMainViewPresenter);
	void inject(LXCheckoutSummaryWidget lxCheckoutSummaryWidget);
	void inject(LXConfirmationWidget lxConfirmationWidget);
	void inject(LXPresenter lxPresenter);

	Observable<SuggestionV4> currentLocationSuggestionObservable();
}
