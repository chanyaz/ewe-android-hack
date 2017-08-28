package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.launch.widget.NewPhoneLaunchWidget;

import dagger.Component;

@LaunchScope
@Component(dependencies = {AppComponent.class}, modules = {LaunchModule.class})
public interface LaunchComponent {
	void inject(NewPhoneLaunchWidget newPhoneLaunchWidget);
}
