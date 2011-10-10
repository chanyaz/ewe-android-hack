package com.expedia.bookings.fragment;

import java.util.HashSet;
import java.util.Set;

/**
 * This method is responsible for activity<->fragments
 * and fragment<->fragments communication. Fragments register
 * themselves as eventHandlers with the EventManager which then notifies
 * all components of any event that is called to be pushed out.
 * 
 * It is the fragment's responsibility to deregister themselves
 * from the hosting activity, which is typically done in the
 * onDetach phase of the fragment lifecycle.
 * 
 */
public class EventManager {

	private static EventManager singleton;

	private Set<EventHandler> mEvents = new HashSet<EventHandler>();

	public static EventManager getInstance() {
		if (singleton == null) {
			singleton = new EventManager();
		}
		return singleton;
	}

	private EventManager() {
		// singleton
	}

	public boolean registerEventHandler(EventHandler eventHandler) {
		return mEvents.add(eventHandler);
	}

	public boolean unregisterEventHandler(EventHandler eventHandler) {
		return mEvents.remove(eventHandler);
	}

	public void notifyEventHandlers(int eventCode, Object data) {
		for (EventHandler eventHandler : mEvents) {
			eventHandler.handleEvent(eventCode, data);
		}
	}

	public interface EventHandler {
		public void handleEvent(int eventCode, Object data);
	};
}
