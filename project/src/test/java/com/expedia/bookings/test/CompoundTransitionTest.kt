package com.expedia.bookings.test

import com.expedia.bookings.presenter.CompoundTransition
import com.expedia.bookings.presenter.TransitionPortion
import com.expedia.bookings.presenter.Presenter
import org.junit.Before
import org.junit.Test
import java.util.LinkedList
import java.util.Queue
import kotlin.test.assertEquals

class TestTransition(val label: String, val queue: Queue<String>) : Presenter.Transition("irrelevant", "doesntmatter") {
    override fun startTransition(forward: Boolean) {
        super.startTransition(forward)
        queue.add("$label start")
    }

    override fun updateTransition(f: Float, forward: Boolean) {
        super.updateTransition(f, forward)
        queue.add("$label ${Math.round((f * 100).toDouble()).toInt()}")
    }

    override fun endTransition(forward: Boolean) {
        super.endTransition(forward)
        queue.add("$label end")
    }
}

class CompoundTransitionTest {

    val eventOrder = LinkedList<String>()

    fun runTransition(trans: Presenter.Transition, forward: Boolean, animationStep: Int) {
        trans.startTransition(forward)
        for (stepVal in (0..100 step animationStep)) {
            trans.updateTransition(stepVal.toFloat() / 100.0f, forward)
        }
        trans.endTransition(forward)
    }

    fun runSmoothTransition(trans: Presenter.Transition, forward: Boolean) {
        runTransition(trans, forward, 1)
    }

    fun runLaggyTransition(trans: Presenter.Transition, forward: Boolean) {
        runTransition(trans, forward, 75)
    }

    @Before
    fun before() {
        eventOrder.clear()
    }

    @Test fun disallowsEmptyCompoundTransition() {
        var errored = false
        try {
            CompoundTransition()
        } catch (err: CompoundTransition.EmptyCompoundTransitionException) {
            errored = true
        } finally {
            assert(errored, { "CompoundTransition needs at least one sub-transition" })
        }
    }

    @Test fun properlySortsTransitionTimes() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0.5f, 0.75f),
                TransitionPortion(TestTransition("b", eventOrder), 0.3f, .6f),
                TransitionPortion(TestTransition("c", eventOrder), 0f, 1f)))
        runSmoothTransition(trans, true)

        assert(eventOrder.indexOf("c start") < eventOrder.indexOf("b start"), { "A start time of 0f must occur before a start time of .3f" })
        assert(eventOrder.indexOf("b start") < eventOrder.indexOf("a start"), { "A start time of .3f must occur before a start tie of .5f" })
        assert(eventOrder.indexOf("b end") < eventOrder.indexOf("a end"), { "An end time of .6f must occur before an end time of .75f" })
        assert(eventOrder.indexOf("a end") < eventOrder.indexOf("c end"), { "An end time of .75ff must occur before an end time of 1f" })
    }

    @Test fun transitionOrderNotAffectedByLag() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0.5f, 0.75f),
                TransitionPortion(TestTransition("b", eventOrder), 0.3f, .6f),
                TransitionPortion(TestTransition("c", eventOrder), 0f, 1f)))
        runLaggyTransition(trans, true)
        assert(eventOrder.indexOf("c start") < eventOrder.indexOf("b start"), { "A start time of 0f must occur before a start time of .3f" })
        assert(eventOrder.indexOf("b start") < eventOrder.indexOf("a start"), { "A start time of .3f must occur before a start tie of .5f" })
        assert(eventOrder.indexOf("b end") < eventOrder.indexOf("a end"), { "An end time of .6f must occur before an end time of .75f" })
        assert(eventOrder.indexOf("a end") < eventOrder.indexOf("c end"), { "An end time of .75ff must occur before an end time of 1f" })
    }

    @Test fun equalTimingTransitionsAdhereToDefinedOrder() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0f, 0.5f),
                TransitionPortion(TestTransition("b", eventOrder), 0f, 0.5f),
                TransitionPortion(TestTransition("c", eventOrder), 0.1f, 1f),
                TransitionPortion(TestTransition("d", eventOrder), 0.1f, 1f)))
        runSmoothTransition(trans, true)
        assert(eventOrder.indexOf("a start") < eventOrder.indexOf("b start"), { "Transitions sharing start times must be called in the order they were defined in the CompoundTransition constructor" })
        assert(eventOrder.indexOf("c start") < eventOrder.indexOf("d start"), { "Transitions sharing start times must be called in the order they were defined in the CompoundTransition constructor" })
        assert(eventOrder.indexOf("a end") < eventOrder.indexOf("b end"), { "Transitions sharing end times must be called in the order they were defined in the CompoundTransition constructor" })
        assert(eventOrder.indexOf("c end") < eventOrder.indexOf("d end"), { "Transitions sharing end times must be called in the order they were defined in the CompoundTransition constructor" })
    }

    @Test fun fullTimeTransitionsReceiveAllCalls() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0f, 1f)))
        runTransition(trans, true, 10)
        assert(eventOrder.joinToString(" ") == "a start a 0 a 10 a 20 a 30 a 40 a 50 a 60 a 70 a 80 a 90 a end")
    }

    @Test fun partialTransitionScalesCalls() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), .4f, .8f)))
        runTransition(trans, true, 10)
        assert(eventOrder.joinToString(" ") == "a start a 0 a 25 a 50 a 75 a end")
    }

    @Test fun backwardsAllTransitionsStartAndEnd() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0.5f, 0.75f),
                TransitionPortion(TestTransition("b", eventOrder), 0.3f, .6f),
                TransitionPortion(TestTransition("c", eventOrder), 0f, 1f),
                TransitionPortion(TestTransition("d", eventOrder), 0f, 1f)))
        runTransition(trans, false, 10)
        assert(eventOrder.contains("a start"), { "Transition a did not start" })
        assert(eventOrder.contains("b start"), { "Transition b did not start" })
        assert(eventOrder.contains("c start"), { "Transition c did not start" })
        assert(eventOrder.contains("d start"), { "Transition d did not start" })
        assert(eventOrder.contains("a end"), { "Transition a did not end" })
        assert(eventOrder.contains("b end"), { "Transition b did not end" })
        assert(eventOrder.contains("c end"), { "Transition c did not end" })
        assert(eventOrder.contains("d end"), { "Transition d did not end" })
    }

    @Test fun backwardsStartsAndEndsAreTimedCorrect() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0.5f, 0.75f),
                TransitionPortion(TestTransition("b", eventOrder), 0.3f, .6f),
                TransitionPortion(TestTransition("c", eventOrder), 0f, 1f),
                TransitionPortion(TestTransition("d", eventOrder), 0f, 1f)))
        runTransition(trans, false, 10)
        assert(eventOrder.indexOf("d start") < eventOrder.indexOf("c start"), { "When backwards, two equally timed ends must have the later defined one start first" })
        assert(eventOrder.indexOf("c start") < eventOrder.indexOf("a start"), { "When backwards, an end time of 1f must start before an end time of .75f" })
        assert(eventOrder.indexOf("a start") < eventOrder.indexOf("b start"), { "When backwards, an end time of .75f must start before an end time of .60f" })
        assert(eventOrder.indexOf("a end") < eventOrder.indexOf("b end"), { "When backwards, a start time of .5f should end before a start time of .3f" })
        assert(eventOrder.indexOf("b end") < eventOrder.indexOf("d end"), { "When backwards, a start time of .3f should end before a start time of .0f" })
        assert(eventOrder.indexOf("d end") < eventOrder.indexOf("c end"), { "When backwards, matching start times should end in the opposite order as defined" })
    }

    @Test fun backwardsUpdatesAreSentInTheRightOrder() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0.4f, 0.8f)))
        runTransition(trans, false, 10)
        assertEquals("a start a 0 a 25 a 50 a 75 a end", eventOrder.joinToString(" "))
    }

    @Test fun multipleBackwardsUpdatesHappenInTheRightOrder() {
        val trans = CompoundTransition(parts = *arrayOf(
                TransitionPortion(TestTransition("a", eventOrder), 0.4f, 0.8f),
                TransitionPortion(TestTransition("b", eventOrder), 0.3f, .7f)))
        runTransition(trans, false, 10)
        assertEquals("a start a 0 b start a 25 b 0 a 50 b 25 a 75 b 50 a end b 75 b end", eventOrder.joinToString(" "))
    }
}
