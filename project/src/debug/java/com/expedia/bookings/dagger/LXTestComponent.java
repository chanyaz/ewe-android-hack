package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LXScope;

import dagger.Component;

@LXScope
@Component(dependencies = {AppComponent.class}, modules = {LXModule.class, LXFakeCurrentLocationSuggestionModule.class})
public interface LXTestComponent extends LXComponent {
}
