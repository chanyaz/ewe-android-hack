package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class, TestCryptoModule.class})
@Singleton
public interface TestAppComponent extends AppComponent {
}
