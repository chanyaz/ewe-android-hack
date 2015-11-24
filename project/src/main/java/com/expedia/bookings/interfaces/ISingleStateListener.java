package com.expedia.bookings.interfaces;

public interface ISingleStateListener {
	void onStateTransitionStart(boolean isReversed);

	void onStateTransitionUpdate(boolean isReversed, float percentage);

	void onStateTransitionEnd(boolean isReversed);

	void onStateFinalized(boolean isReversed);
}
