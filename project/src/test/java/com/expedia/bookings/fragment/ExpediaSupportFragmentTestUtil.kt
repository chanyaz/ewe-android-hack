package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.widget.LinearLayout
import com.expedia.bookings.R
import org.robolectric.Robolectric

/**
 * Inspired by @see SupportFragmentTestUtil this utils class allows us to create a FragmentActivity
 * using the Expedia NewLaunchTheme and start fragments using this said manager
 */
class ExpediaSupportFragmentTestUtil {

    companion object {

        @JvmStatic fun startFragment(fragmentManager: FragmentManager, fragment: Fragment) {
            fragmentManager.beginTransaction().add(1, fragment, null).commit()
        }

        @JvmStatic fun startFragment(fragment: Fragment) {
            startFragment(buildSupportFragmentManager(), fragment)
        }

        private fun buildSupportFragmentManager(): FragmentManager {
            val activity = Robolectric.setupActivity(FragmentUtilActivity::class.java)
            return activity.supportFragmentManager
        }
    }

    class FragmentUtilActivity : FragmentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val view = LinearLayout(this)
            view.id = 1
            setTheme(R.style.NewLaunchTheme)

            setContentView(view)
        }
    }
}
