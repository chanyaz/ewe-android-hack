package com.expedia.bookings.dagger.tags;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Scope
@Retention(RUNTIME)
public @interface SatelliteScope {
	// ignore
}
