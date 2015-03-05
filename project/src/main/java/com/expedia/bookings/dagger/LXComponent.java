package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.presenter.lx.LXDetailsPresenter;
import com.expedia.bookings.presenter.lx.LXResultsPresenter;
import com.expedia.bookings.widget.LXActivityDetailsWidget;
import com.expedia.bookings.widget.LxSuggestionAdapter;

import dagger.Component;

@LXScope
@Component(dependencies = {AppComponent.class}, modules = {LXModule.class})
public interface LXComponent {
	void inject(LXResultsPresenter lxResultsPresenter);
	void inject(LXDetailsPresenter lxDetailsPresenter);
	void inject(LXActivityDetailsWidget lxActivityDetailsWidget);
	void inject(LxSuggestionAdapter adapter);
}
