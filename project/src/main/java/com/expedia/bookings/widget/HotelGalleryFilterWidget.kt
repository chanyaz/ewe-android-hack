package com.expedia.bookings.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.hotel.widget.HotelDetailGalleryAdapter
import com.expedia.bookings.utils.bindView
import com.expedia.vm.BaseHotelDetailViewModel
import com.mobiata.android.util.Ui
import com.wefika.flowlayout.FlowLayout
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * Created by nbirla on 13/11/17.
 */

class HotelGalleryFilterWidget(context: Context, attrs: AttributeSet?) : ExpandableCardView(context, attrs) {

    override fun getMenuDoneButtonFocus(): Boolean {
        return false
    }

    override fun getMenuButtonTitle(): String {
        return "";
    }

    override fun getActionBarTitle(): String {
        return ""
    }

    override fun onMenuButtonPressed() {
    }

    override fun onLogin() {
    }

    override fun onLogout() {
    }

    override fun isComplete(): Boolean {
        return true
    }

    val clearAll: TextView by bindView(R.id.tv_clear)
    val unexpanded: TextView by bindView(R.id.unexpanded)
    val expanded: LinearLayout by bindView(R.id.expanded)
    val flowLayout: FlowLayout by bindView(R.id.fl_gallery)
    var vm: BaseHotelDetailViewModel by Delegates.notNull()

    init {
        View.inflate(getContext(), R.layout.hotel_gallery_filter_widget, this)
    }

    override fun setExpanded(expand: Boolean, animate: Boolean) {
        super.setExpanded(expand, animate)
        if (expand) {
            background = null
            expanded.visibility = View.VISIBLE
            unexpanded.setRightDrawable(R.drawable.ic_picker_arrow_down_enabled)

        } else {
            setBackgroundResource(R.drawable.card_background)
            expanded.visibility = View.GONE
            unexpanded.setRightDrawable(R.drawable.ic_picker_arrow_up_enabled)
        }
    }

    fun setupGalleryFilters(galleryUrls: ArrayList<HotelMedia>, adapter : HotelDetailGalleryAdapter){
        vm.filterObservable.onNext(adapter.itemCount)
        unexpanded.setOnClickListener {
            setExpanded(!isExpanded)
        }
        clearAll.visibility = View.GONE
        clearAll.setOnClickListener {
            clearAllFilters()
            clearAll.visibility = View.GONE
            adapter.setFilters(emptyList())
            vm.filterObservable.onNext(adapter.itemCount)
            unexpanded.setText("Filter")
        }

        //dynamically add filters to flow layout
        flowLayout.removeAllViews()

        var set = getFilters(galleryUrls)
        for(filter : String in set){
            var tv = TextView(context)
            tv.text = filter
            tv.tag = filter
            tv.layoutParams = FlowLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            var buttonDrawable = resources.getDrawable(R.drawable.selector_packages_hotel_gallery_filter)
            buttonDrawable.mutate()
            tv.background = buttonDrawable;
            tv.setTextColor(resources.getColor(R.color.white))
            tv.setOnClickListener {
                tv.isSelected = !tv.isSelected
                clearAll.visibility = if(isAnyFilterSelected()) View.VISIBLE else View.GONE
                var filters = getSelectedFilters()
                adapter.setFilters(filters)
                vm.filterObservable.onNext(adapter.itemCount)
                if(filters.size == 0){
                    unexpanded.setText("Filter")
                } else {
                    unexpanded.setText("Filter " + "(" + filters.size + ")")
                }
            }
            flowLayout.addView(tv)
        }
    }

    fun setVM(vm: BaseHotelDetailViewModel){
        this.vm = vm
    }

    fun isAnyFilterSelected() : Boolean{
        var i = 0
        while(i < flowLayout.childCount){
            val view = flowLayout.getChildAt(i) as TextView
            if(view.isSelected)
                return true
            i++
        }
        return false
    }

    fun getSelectedFilters() : List<String>{
        val filters = ArrayList<String>()
        var i = 0
        while(i < flowLayout.childCount){
            val view = flowLayout.getChildAt(i) as TextView
            if(view.isSelected){
                filters.add(view.getTag() as String)
            }
            i++
        }
        return filters
    }

    private fun getFilters(galleryUrls: ArrayList<HotelMedia>) : SortedSet<String>{
        var set = TreeSet<String>()
        for(galleryUrl : HotelMedia in galleryUrls){
            set.add(galleryUrl.mDescription)
        }
        return set
    }

    private fun clearAllFilters(){
        var i = 0
        while(i < flowLayout.childCount){
            val view = flowLayout.getChildAt(i) as TextView
            view.isSelected = false
            i++
        }
    }

}