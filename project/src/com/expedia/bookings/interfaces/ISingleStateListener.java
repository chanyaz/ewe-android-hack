package com.expedia.bookings.interfaces;

public interface ISingleStateListener {
	public void onStateTransitionStart(boolean isReversed);
	public void onStateTransitionUpdate(boolean isReversed, float percentage);
	public void onStateTransitionEnd(boolean isReversed);
	public void onStateFinalized(boolean isReversed);
}
