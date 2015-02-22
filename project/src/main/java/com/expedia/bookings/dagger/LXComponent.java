package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.presenter.lx.LXDetailsPresenter;
import com.expedia.bookings.presenter.lx.LXResultsPresenter;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, LXModule.class})
public interface LXComponent {
	void inject(LXResultsPresenter lxResultsPresenter);
	void inject(LXDetailsPresenter lxDetailsPresenter);
}
