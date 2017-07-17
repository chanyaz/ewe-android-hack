package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.launch.widget.LaunchListWidget;
import com.expedia.bookings.launch.widget.NewPhoneLaunchWidget;

import com.expedia.util.SatelliteUtil;
import dagger.Component;

@LaunchScope
@Component(dependencies = {AppComponent.class}, modules = {LaunchModule.class, SatelliteModule.class})
public interface LaunchComponent {
	void inject(NewPhoneLaunchWidget newPhoneLaunchWidget);
	void inject(SatelliteUtil satelliteUtil);
}
