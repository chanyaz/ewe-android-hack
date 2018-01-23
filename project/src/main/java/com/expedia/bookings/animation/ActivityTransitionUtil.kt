package com.expedia.bookings.animation

import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window

class ActivityTransitionUtil {
    companion object {
        fun createPairsWithAndroidComponents(activity: AppCompatActivity, view: View, name: String): Array<android.support.v4.util.Pair<View, String>> {

            val pairs = ArrayList<Pair<View, String>>()
            val pair1 = Pair.create<View, String>(view, name)

            pairs.add(pair1)
            pairs.addAll(ActivityTransitionUtil.getSharedAndroidPairs(activity))

            return pairs.toArray(arrayOfNulls<Pair<View, String>>(pairs.size))
        }

        /*
        You might look at this code and think you can do it in xml and it will be way cleaner.  You might
          think you could set exclude targets on the status bar and navigation bar and it would work.
          You are wrong. Working with the xml caused a flashing of the window background on top of those components.
          This solution solves that for shared component transitions, for all api levels.
          */
        private fun getSharedAndroidPairs(activity: AppCompatActivity): ArrayList<android.support.v4.util.Pair<View, String>> {
            val pairs = ArrayList<android.support.v4.util.Pair<View, String>>()
            val decorView = activity.window.decorView

            val statusBar = decorView.findViewById<View>(android.R.id.statusBarBackground)
            val navBar = decorView.findViewById<View>(android.R.id.navigationBarBackground)

            if (statusBar != null) {
                pairs.add(android.support.v4.util.Pair.create<View, String>(statusBar,
                        Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
            }
            if (navBar != null) {
                pairs.add(android.support.v4.util.Pair.create<View, String>(navBar,
                        Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))
            }

            return pairs
        }
    }
}
