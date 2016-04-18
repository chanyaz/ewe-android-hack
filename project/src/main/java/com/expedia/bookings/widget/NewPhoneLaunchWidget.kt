package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.animation.ScrollOverlayController
import com.expedia.bookings.utils.bindView

class PlaceHolderViewHolder(root: TextView) : RecyclerView.ViewHolder(root) {
    val text: TextView = root
}

class NewPhoneLaunchWidget(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    val fab: FloatingActionButton by lazy {
        findViewById(R.id.fab) as FloatingActionButton
    }

    val list: RecyclerView by bindView(R.id.launch_list)
    val lobView: View by bindView(R.id.lobView)
    val touchCatcher: View by bindView (R.id.touch_catcher)
    var controller: ScrollOverlayController? = null


    override fun onFinishInflate() {
        super.onFinishInflate()

        fab.setOnClickListener({
            controller?.toggleOverlay(true)
        })

        onLobVisibilityChanged(true)

        // Set up the list that goes under the Lines of Business - needs a real adapter and layout manager
        list.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        list.adapter = object : RecyclerView.Adapter<PlaceHolderViewHolder>() {
            override fun getItemCount(): Int {
                return 400
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolderViewHolder {
                val viewLocal = TextView(parent.context)
                parent.addView(viewLocal, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                return PlaceHolderViewHolder(viewLocal)
            }

            override fun onBindViewHolder(holder: PlaceHolderViewHolder, position: Int) {
                holder.text.text = "$position";
            }

            override fun getItemViewType(position: Int): Int {
                return 0
            }
        }

        list.viewTreeObserver.addOnGlobalLayoutListener {
            controller = ScrollOverlayController(touchCatcher, lobView, list,
                    findViewById(R.id.darkness), object : ScrollOverlayController.OverlayListener {
                // If there needs to be a difference between when the overlay changes the lobs visibility
                // and when scrolling does, that can be encoded here
                override fun onOverlayScrollVisibilityChanged(overlayVisible: Boolean) {
                    onLobVisibilityChanged(overlayVisible)
                }

                override fun onOverlayStateChanged(overlayVisible: Boolean) {
                    onLobVisibilityChanged(overlayVisible)
                }
            })
        }
    }

    fun onLobVisibilityChanged(lobVisible: Boolean) {
        // Just avoiding null changing while we're looking at it - fab should be replaced by proper binding eliminating this
        val curFab = fab
        if (curFab != null) {
            val fabAnim: ObjectAnimator
            if (lobVisible) {
                // The lines of business are now visible, adjust accordingly.
                // When the actual fab animation happens, don't just move it by a randomly chosen number like this 500 here
                fabAnim = ObjectAnimator.ofFloat(curFab, "translationY", curFab.translationY, 500f)

            } else {
                lobView.setBackgroundResource(R.color.new_launch_toolbar_background_color)
                // The lines of business are no longer visible, adjust accordingly
                fabAnim = ObjectAnimator.ofFloat(curFab, "translationY", curFab.translationY, 0f)
            }
            fabAnim.duration = 250
            fabAnim.interpolator = AccelerateDecelerateInterpolator()
            fabAnim.start()
        }
    }

}
