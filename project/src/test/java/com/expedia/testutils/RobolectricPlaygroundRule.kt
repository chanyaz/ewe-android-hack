package com.expedia.testutils

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.activity.PlaygroundActivity
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment

class RobolectricPlaygroundRule(val layoutId: Int, val style: Int? = null) : TestRule {

    lateinit var activity: Activity

    override fun apply(base: Statement?, description: Description?): Statement =
            object : Statement() {
                @Throws(Throwable::class)
                override fun evaluate() {
                    var intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, layoutId)
                    if (style != null) {
                        intent = PlaygroundActivity.addTheme(intent, style)
                    }
                    val activityController = Robolectric.buildActivity(PlaygroundActivity::class.java, intent)
                    activity = activityController.create().visible().get()
                    base?.evaluate()
                    activity.finish()
                }
            }

    @Suppress("UNCHECKED_CAST")
    fun <T: View> findRoot(): T? {
        val contentView: FrameLayout? = activity.findViewById(android.R.id.content)
        return contentView?.getChildAt(0) as? T
    }
}
