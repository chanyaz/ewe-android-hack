package com.expedia.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.animation.ScrollOverlayController

class PlaceHolderViewHolder(root: TextView) : RecyclerView.ViewHolder(root) {
    val text: TextView = root
}

class NewPhoneLaunchActivity : AppCompatActivity() {

    var fab: FloatingActionButton? = null
    var controller: ScrollOverlayController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_phone_launch)

        val list = findViewById(R.id.listView) as RecyclerView
        fab = findViewById(R.id.fab) as FloatingActionButton

        fab?.setOnClickListener({
            controller?.toggleOverlay(true)
        })

        // When there's a toolbar with tabs, the shop one being clicked while this is open can call this:
        // controller?.scrollToTop()

        onLobVisibilityChanged(true)
        // Set up the list that goes under the Lines of Business - needs a real adapter and layout manager
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list.adapter = object : RecyclerView.Adapter<PlaceHolderViewHolder>() {
            override fun getItemCount(): Int {
                return 400
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolderViewHolder {
                val view = TextView(parent.context)
                parent.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                return PlaceHolderViewHolder(view)
            }

            override fun onBindViewHolder(holder: PlaceHolderViewHolder, position: Int) {
                holder.text.text = "$position";
            }

            override fun getItemViewType(position: Int): Int {
                return 0
            }
        }

        list.viewTreeObserver.addOnGlobalLayoutListener {
            controller = ScrollOverlayController(findViewById(R.id.touch_catcher), findViewById(R.id.lobView), list,
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

    // You can use this function to trigger behavior when the LOB hide or show
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
                // The lines of business are no longer visible, adjust accordingly
                fabAnim = ObjectAnimator.ofFloat(curFab, "translationY", curFab.translationY, 0f)
            }
            fabAnim.duration = 250
            fabAnim.interpolator = AccelerateDecelerateInterpolator()
            fabAnim.start()
        }
    }

}
