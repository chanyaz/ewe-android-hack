package com.expedia.util


fun scaleValueToRange(oldMin: Float, oldMax: Float, newMin: Float, newMax: Float, value: Float): Float{
    val startRange = (oldMax - oldMin)
    if (startRange == 0f) {
        return 0f
    }
    return (((value - oldMin) * (newMax - newMin)) / (oldMax - oldMin)) + newMin
}