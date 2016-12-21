package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.launch.widget.NewPhoneLaunchWidget;
import com.expedia.bookings.launch.widget.PhoneLaunchWidget;
import com.expedia.bookings.widget.feeds.FeedsListWidget;

import dagger.Component;

@LaunchScope
@Component(dependencies = {AppComponent.class}, modules = {LaunchModule.class})
public interface LaunchComponent {
	void inject(PhoneLaunchWidget phoneLaunchWidget);
	void inject(FeedsListWidget feedsListWidget);
	void inject(NewPhoneLaunchWidget newPhoneLaunchWidget);
}
