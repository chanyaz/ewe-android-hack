package com.expedia.bookings.presenter

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.expedia.util.scaleValueToRange
import java.util.ArrayList
import java.util.Comparator
import java.util.LinkedList


val startComparator = Comparator { value1: TransitionPortion, value2: TransitionPortion ->  value1.startPercent.compareTo(value2.startPercent)}
val endComparator = Comparator { value1: TransitionPortion, value2: TransitionPortion ->  value1.endPercent.compareTo(value2.endPercent)}

/**
 * Assertions: startPercent < endPercent, startPercent >= 0.0f, endPercent <= 1.0f
 */
class TransitionPortion(val transition: Presenter.Transition, val startPercent: Float = 0f, val endPercent: Float = 1f) {

    class ImpossibleTransitionTimeFrameException(message: String) : Throwable(message, null)

    init {
        if (startPercent >= endPercent) {
            throw ImpossibleTransitionTimeFrameException("PortionalTransition can not be instantiated with a startPercent that is equal to or greater than an endPercent")
        }
        if (startPercent < 0f) {
            throw ImpossibleTransitionTimeFrameException("PortionalTransition can not be instantiated with a startPercent less than 0.0")
        }
        if (endPercent > 1f) {
            throw ImpossibleTransitionTimeFrameException("PortionalTransition can not be instantiated with an endPercent greater than 1.0")
        }
    }
}


/**
 * Compound transitions are stateful - there is one state per instance, and it is modified as it progresses through the animation.
 * Preferably, this is replaced by passing in a new state that is simply adhered to each time? Perhaps. Maybe. One day.
 * Due to this, synchronization work does NOT occur, under the mindset that if anything would be
 * calling this in conflicting ways it is not intentional, and should mess things up so we know about it.
 * Therefore, the assumption we operate on is this:
 *  StartTransition is called before updateTransition calls happen. Every UpdateTransition call
 *  happens before endTransition (as in, updateTransition is never called after endTransition, unless we started a new one)
 */
open class CompoundTransition(startState: String? = null, endState: String? = null, interpolator: Interpolator = LinearInterpolator(), duration: Int = DEFAULT_ANIMATION_DURATION, vararg parts: TransitionPortion) :
        Presenter.Transition(startState, endState, interpolator, duration) {

    // At some point Kotlin had an issue with crashing if both arguments of Throwable were not supplied, so both are explicitly supplied here just in case.
    class EmptyCompoundTransitionException : Throwable("CompoundTransition must be instantiated with at least 1 TransitionPortion.", null)

    private val startOrder: MutableList<TransitionPortion> = ArrayList()
    private val endOrder: MutableList<TransitionPortion> = ArrayList()
    // A LinkedList is slightly less efficient than a Set in this example, but gives us a definitive order we know active transitions will occur in
    private val active: MutableList<TransitionPortion> = LinkedList()
    // currentStartIndex always refers to the current index in the starts list
    private var currentStartIndex = 0
    // currentEndIndex always refers to the current index in the ends list
    private var currentEndIndex = 0

    init {
        if (parts.size == 0) {
            throw EmptyCompoundTransitionException()
        }
        parts.forEach {
            startOrder.add(it)
            endOrder.add(it)
        }

        startOrder.sortWith(startComparator)
        endOrder.sortWith(endComparator)
    }

    data class ConsumableTransitions(val newIndex: Int, val transitions: List<TransitionPortion>)

    internal fun startInnerTransition(transitionPart: TransitionPortion, forward: Boolean) {
        transitionPart.transition.startTransition(forward)
        active.add(transitionPart)
    }

    internal fun updateInnerTransition(transitionPart: TransitionPortion, forward: Boolean, percentComplete: Float) {
        val scaledCompletion = scaleValueToRange(transitionPart.startPercent, transitionPart.endPercent, 0f, 1f, percentComplete)
        if (forward) {
            transitionPart.transition.updateTransition(scaledCompletion, forward)
        } else {
            transitionPart.transition.updateTransition(1f - scaledCompletion, forward)
        }
    }

    internal fun endInnerTransition(transitionPart: TransitionPortion, forward: Boolean) {
        active.remove(transitionPart)
        transitionPart.transition.endTransition(forward)
    }

    internal fun consumeUntil(percent: Float, list: List<TransitionPortion>, startIndex: Int, forward: Boolean, compareStart: Boolean): ConsumableTransitions {
        var currentIndex = startIndex
        val resultingTransitions = ArrayList<TransitionPortion>()
        if (forward) {
            while (currentIndex < list.size && (if (compareStart) list[currentIndex].startPercent else list[currentIndex].endPercent) <= percent) {
                resultingTransitions.add(list[currentIndex])
                currentIndex += 1
            }
        } else {
            while (currentIndex >= 0 && (if (compareStart) list[currentIndex].startPercent else list[currentIndex].endPercent) >= percent) {
                resultingTransitions.add(list[currentIndex])
                currentIndex -= 1
            }
        }
        return ConsumableTransitions(currentIndex, resultingTransitions)
    }

    override fun startTransition(forward: Boolean) {
        if (forward) {
            currentStartIndex = 0
            currentEndIndex = 0
            val (newStartIndex, startableTransitions) = consumeUntil(0f, startOrder, currentStartIndex, forward, true)
            currentStartIndex = newStartIndex
            startableTransitions.forEach {
                startInnerTransition(it, forward)
            }
        } else {
            // When reversing, starts are ends and ends are starts
            currentStartIndex = startOrder.lastIndex
            currentEndIndex = endOrder.lastIndex
            val (newStartIndex, startableTransitions) = consumeUntil(1f, endOrder, currentEndIndex, forward, false)
            currentEndIndex = newStartIndex
            startableTransitions.forEach {
                startInnerTransition(it, forward)
            }
        }
    }

    override fun updateTransition(percentComplete: Float, forward: Boolean) {
        if (forward) {
            val (newEndIndex, endableTransitions) = consumeUntil(percentComplete, endOrder, currentEndIndex, forward, false)
            currentEndIndex = newEndIndex
            endableTransitions.forEach {
                endInnerTransition(it, forward)
            }
            // Since we don't really know how far after the start we are, we should start it, and then
            // update it in the active loop to make sure it's looking at the right spot
            val (newStartIndex, startableTransitions) = consumeUntil(percentComplete, startOrder, currentStartIndex, forward, true)
            currentStartIndex = newStartIndex
            startableTransitions.forEach {
                startInnerTransition(it, forward)
            }
            active.forEach {
                updateInnerTransition(it, forward, percentComplete)
            }
        } else {
            val percentProgress = 1f - percentComplete
            // Because we are progressing backwards through this, the starts are now the ends and the ends are the starts.
            val (newStartsIndex, endableTransitions) = consumeUntil(percentProgress, startOrder, currentStartIndex, forward, true)
            currentStartIndex = newStartsIndex
            endableTransitions.forEach {
                endInnerTransition(it, forward)
            }
            val (newEndsIndex, startableTransitions) = consumeUntil(percentProgress, endOrder, currentEndIndex, forward, false)
            currentEndIndex = newEndsIndex
            startableTransitions.forEach {
                startInnerTransition(it, forward)
            }
            active.forEach {
                updateInnerTransition(it, forward, percentProgress)
            }
        }
    }

    override fun endTransition(forward: Boolean) {
        // We're done transitioning, we don't need to update the index pointer, just need to finish off the queue
        if (forward) {
            consumeUntil(1f, endOrder, currentEndIndex, forward, false).transitions.forEach {
                endInnerTransition(it, forward)
            }
        } else {
            //When reversing, starts are ends.
            consumeUntil(1f, startOrder, currentStartIndex, forward, true).transitions.forEach {
                endInnerTransition(it, forward)
            }
        }
    }
}
