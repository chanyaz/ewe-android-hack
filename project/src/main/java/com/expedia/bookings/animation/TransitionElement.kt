package com.expedia.bookings.animation

public data class TransitionElement<T>(val start: T, val end: T) {
    public companion object {
        @JvmStatic fun calculateStep(start: Float, end: Float, percent: Float, forward: Boolean): Float {
            if (forward) {
                return calculateStep(start, end, percent)
            } else {
                return calculateStep(end, start, percent)
            }
        }

        @JvmStatic fun calculateStep(start: Float, end: Float, percent: Float): Float {
            return (start + (percent * (end - start)))
        }
    }
}