package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.HotelErrorViewModel

public class HotelErrorPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {

    val root: ViewGroup by bindView(R.id.main_container)
    val errorImage: ImageView by bindView(R.id.error_image)
    val errorButton: Button by bindView(R.id.error_action_button)
    val errorText: TextView by bindView(R.id.error_text)
    val toolbar: Toolbar by bindView(R.id.error_toolbar)

    private var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)

    var viewmodel: HotelErrorViewModel by notNullAndObservable { vm ->
        vm.imageObservable.subscribe { errorImage.setImageResource(it) }
        vm.buttonTextObservable.subscribeText(errorButton)
        vm.errorMessageObservable.subscribeText(errorText)
        vm.titleObservable.subscribe { toolbar.title = it }
        vm.subTitleObservable.subscribe {toolbar.subtitle = it }
        errorButton.setOnClickListener { vm.actionObservable.onNext(Unit) }
    }

    init {
        View.inflate(getContext(), R.layout.error_widget, this)

        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.hotels_primary_color))
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.ToolbarSubtitleTextAppearance)

        toolbar.setNavigationOnClickListener {
            viewmodel.actionObservable.onNext(Unit)
        }

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, root, ContextCompat.getColor(context, com.expedia.bookings.R.color.hotels_primary_color))
            addView(statusBar)
        }
    }

    override fun back() : Boolean {
        viewmodel.actionObservable.onNext(Unit)
        return true
    }

    fun animationUpdate(f: Float, forward: Boolean) {
        var factor = if (forward) f else Math.abs(1 - f)
        navIcon.parameter = factor
    }

    fun animationFinalize() {
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
    }

}
