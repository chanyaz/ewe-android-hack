package com.expedia.bookings.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.animation.ScrollOverlayController
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment

class PlaceHolderViewHolder(root: TextView) : RecyclerView.ViewHolder(root) {
    val text: TextView = root
}

class NewPhoneLaunchFragment : Fragment(), IPhoneLaunchActivityLaunchFragment {
    var fab: FloatingActionButton? = null
    var controller: ScrollOverlayController? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (view != null) {
            val list = view.findViewById(R.id.listView) as RecyclerView
            fab = view.findViewById(R.id.fab) as FloatingActionButton

            fab?.setOnClickListener({
                controller?.toggleOverlay(true)
            })

            onLobVisibilityChanged(true)
            // Set up the list that goes under the Lines of Business - needs a real adapter and layout manager
            list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
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
                controller = ScrollOverlayController(view.findViewById(R.id.touch_catcher), view.findViewById(R.id.lobView), list,
                        view.findViewById(R.id.darkness), object : ScrollOverlayController.OverlayListener {
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

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.new_phone_launch, null)
        return view
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

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onPause() {
        super.onPause()
    }
}


