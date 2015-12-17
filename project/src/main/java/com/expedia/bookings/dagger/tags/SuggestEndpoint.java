package com.expedia.bookings.dagger.tags;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier @Retention(RUNTIME)
public @interface SuggestEndpoint {
	// ignore
}
