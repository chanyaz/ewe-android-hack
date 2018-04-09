package com.expedia.bookings.itin.triplist

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBottomNavigationBarEnabled
import com.expedia.bookings.utils.isBrandColorEnabled
import com.expedia.bookings.widget.DisableableViewPager

class TripListFragment : Fragment() {
    private val tripToolbar: Toolbar by bindView(R.id.trip_list_toolbar)

    val viewPager by bindView<DisableableViewPager>(R.id.trip_list_viewpager)
    val tabs by bindView<TabLayout>(R.id.trip_list_tablayout)

    val customFragmentPagerAdapter: CustomFragmentPagerAdapter by lazy {
        CustomFragmentPagerAdapter(childFragmentManager)
    }
    //    private val loadmoreButotn by bindView<Button>(R.id.load_more_button)
//    private var frag1: TestFragment? = null
//    private var frag2: TestFragment? = null
//    private var frag3: TestFragment? = null
    val customViewPagerAdapter: CustomViewPagerAdapter by lazy {
        CustomViewPagerAdapter(context)
    }

    companion object {
        val inputArray = mutableListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val TAB_UPCOMING = 0
        val TAB_PAST = 1
        val TAB_CANCELLED = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_folders_list, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleToolbarBackgroundColor()
        handleToolbarVisibility()

        //viewPager.adapter = customFragmentPagerAdapter
        viewPager.adapter = customViewPagerAdapter

        viewPager.offscreenPageLimit = 1
        viewPager.setPageSwipingEnabled(false)
        tabs.setupWithViewPager(viewPager)
        tabs.setOnTabSelectedListener(TabListener())
//        loadmoreButotn.setOnClickListener {
//            for (i in inputArray.size until inputArray.size.plus(10)) {
//                inputArray.add(i.toString())
//            }
//            frag1?.resetAdapter(inputArray)
//            frag2?.resetAdapter(inputArray)
//            frag3?.resetAdapter(inputArray)
//        }
    }

    private fun handleToolbarBackgroundColor() {
        if (isBrandColorEnabled(context)) {
            tripToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_primary))
        }
    }

    private fun handleToolbarVisibility() {
        tripToolbar.visibility = if (isBottomNavigationBarEnabled(context)) View.VISIBLE else View.GONE
    }

    inner class TabListener : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let {
                when (tab.position) {
                    TAB_UPCOMING -> viewPager.currentItem = TAB_UPCOMING
                    TAB_PAST -> viewPager.currentItem = TAB_PAST
                    TAB_CANCELLED -> viewPager.currentItem = TAB_CANCELLED
                }
            }
        }
    }
}
