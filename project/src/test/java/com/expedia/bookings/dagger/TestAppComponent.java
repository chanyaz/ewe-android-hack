package com.expedia.bookings.dagger;

import javax.inject.Singleton;
import dagger.Component;


@Component(modules = { AppModule.class, TestUserModule.class, GalleryModule.class })
@Singleton
public interface TestAppComponent extends AppComponent {
}
