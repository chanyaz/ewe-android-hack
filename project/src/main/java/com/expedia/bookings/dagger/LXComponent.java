package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.lob.lx.ui.presenter.LXCheckoutMainViewPresenter;
import com.expedia.bookings.lx.presenter.LXCheckoutPresenter;
import com.expedia.bookings.lx.presenter.LXDetailsPresenter;
import com.expedia.bookings.lx.presenter.LXPresenter;
import com.expedia.bookings.lx.presenter.LXResultsPresenter;
import com.expedia.bookings.lx.presenter.LxCheckoutPresenterV2;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.expedia.bookings.widget.LXCheckoutSummaryWidget;
import com.expedia.bookings.widget.LXConfirmationWidget;
import com.expedia.bookings.widget.LXResultsListAdapter;
import com.expedia.bookings.widget.LXSuggestionAdapter;
import com.expedia.bookings.widget.LXTicketSelectionWidget;
import com.expedia.bookings.lx.vm.LXMapViewModel;
import com.expedia.bookings.lx.vm.LXCheckoutViewModel;
import com.expedia.bookings.lx.vm.LXCreateTripViewModel;
import dagger.Component;
import io.reactivex.Observable;

@LXScope
@Component(dependencies = {AppComponent.class}, modules = {LXModule.class, LXCurrentLocationSuggestionModule.class})
public interface LXComponent {
	void inject(LXResultsPresenter lxResultsPresenter);
	void inject(LXDetailsPresenter lxDetailsPresenter);
	void inject(LXActivityDetailsWidget lxActivityDetailsWidget);
	void inject(LXSuggestionAdapter adapter);
	void inject(LXCheckoutPresenter lxCheckoutPresenter);
	void inject(LXCheckoutMainViewPresenter lxCheckoutMainViewPresenter);
	void inject(LXCheckoutSummaryWidget lxCheckoutSummaryWidget);
	void inject(LXConfirmationWidget lxConfirmationWidget);
	void inject(LXPresenter lxPresenter);
	void inject(LXMapViewModel lxMapViewModel);
	void inject(LXTicketSelectionWidget lxTicketSelectionWidget);
	void inject(LXResultsListAdapter.ViewHolder lxResultsListAdapterViewHolder);

	void inject(LxCheckoutPresenterV2 lxCheckoutPresenterV2);
	void inject(LXCheckoutViewModel lxCheckoutViewModel);
	void inject(LXCreateTripViewModel lxCreateTripViewModel);

	Observable<SuggestionV4> currentLocationSuggestionObservable();
	SuggestionV4Services suggestionsService();
	ItinTripServices itinTripService();
}
