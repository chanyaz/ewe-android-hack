package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.services.FourSquareServices;

import dagger.Component;

/**
 * Created by nbirla on 14/02/18.
 */


@PackageScope
@Component(dependencies = { AppComponent.class }, modules = { FourSquareModule.class })
public interface FourSquareComponent {

	FourSquareServices fourSquareServices();

}
