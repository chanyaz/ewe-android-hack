package com.expedia.bookings.test

import com.expedia.bookings.presenter.TransitionPortion
import org.junit.Test
import java.util.LinkedList

class TransitionPortionTest {

    @Test fun disallowsOutOfBoundsInstantiationValues() {
        var errored = false
        try {
            TransitionPortion(startPercent = -1f, transition = TestTransition("a", LinkedList()))
        } catch (err: TransitionPortion.ImpossibleTransitionTimeFrameException) {
            errored = true
        } finally {
            assert(errored, { "PortionalTransition should disallow startPercent < 0.0" })
        }
        errored = false
        try {
            TransitionPortion(endPercent = 1.05f, transition = TestTransition("a", LinkedList()))
        } catch (err: TransitionPortion.ImpossibleTransitionTimeFrameException) {
            errored = true
        } finally {
            assert(errored, { "PortionalTransition should disallow endPercent > 1.0" })
        }
    }

    @Test fun disallowOverlappingInstantiationValues() {
        var errored = false
        try {
            TransitionPortion(startPercent = .45f, endPercent = .45f, transition = TestTransition("a", LinkedList()))
        } catch (err: TransitionPortion.ImpossibleTransitionTimeFrameException) {
            errored = true
        } finally {
            assert(errored, { "PortionalTransition should disallow endPercent <= startPercent" })
        }
    }
}
