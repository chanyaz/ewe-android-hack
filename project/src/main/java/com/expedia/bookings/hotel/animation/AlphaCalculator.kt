package com.expedia.bookings.hotel.animation

class AlphaCalculator {
    companion object {
        /*
            Calculate an alpha value between two bounds, startPoint -> endPoint
            adjusting alpha linearly from 0f -> 1f between startPoint and endPoint
         */
        fun fadeInAlpha(startPoint: Float, endPoint: Float, currentPoint: Float): Float {
            val currentPosition = currentPoint - startPoint
            val totalDistance = endPoint - startPoint
            val percentComplete = currentPosition / totalDistance
            return getBoundedAlpha(percentComplete)
        }

        /*
            Calculate an alpha value between two bounds, startPoint -> endPoint
            adjusting alpha linearly from 1f -> 0f between startPoint and endPoint
        */
        fun fadeOutAlpha(startPoint: Float, endPoint: Float, currentPoint: Float): Float {
            val currentPosition = endPoint - currentPoint
            val totalDistance = endPoint - startPoint
            val percentComplete = currentPosition /totalDistance

            return getBoundedAlpha(percentComplete)
        }

        /*
            Get an alpha value from 0 - 255 based off percentage
        */
        fun getAlphaValue(percentage: Int): Int {
            return 255 * percentage / 100
        }

        private fun getBoundedAlpha(alpha: Float): Float {
            if (alpha > 1f) {
                return 1f
            } else if (alpha < 0f) {
                return 0f
            }
            return alpha
        }
    }
}