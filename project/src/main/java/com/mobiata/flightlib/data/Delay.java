package com.mobiata.flightlib.data;

public class Delay {
	public static final int DELAY_NONE = 0;
	public static final int DELAY_GATE_ACTUAL = 1;
	public static final int DELAY_GATE_ESTIMATED = 2;
	public static final int DELAY_RUNWAY_ACTUAL = 3;
	public static final int DELAY_RUNWAY_ESTIMATED = 4;

	/**
	 * Delay in minutes - positive means it's late, negative means it's early, zero means it's on time.
	 */
	public final int mDelay;

	/**
	 * The type of delay this represents.
	 */
	public final int mDelayType;

	public Delay(int delay, int delayType) {
		mDelay = delay;
		mDelayType = delayType;
	}
}
