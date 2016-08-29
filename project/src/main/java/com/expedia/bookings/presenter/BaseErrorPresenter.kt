package com.expedia.bookings.presenter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.AbstractErrorViewModel

abstract class BaseErrorPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val root: ViewGroup by bindView(R.id.main_container)
    val errorImage: ImageView by bindView(R.id.error_image)
    val errorButton: Button by bindView(R.id.error_action_button)
    val errorText: TextView by bindView(R.id.error_text)
    val standardToolbarContainer: LinearLayout by bindView(R.id.standard_toolbar)
    val standardToolbar: Toolbar by bindView(R.id.error_toolbar)

    var viewmodel: AbstractErrorViewModel by notNullAndObservable { vm ->
        setupViewModel(vm)
    }

    open protected fun setupViewModel(vm: AbstractErrorViewModel) {
        vm.imageObservable.subscribe { errorImage.setImageResource(it) }
        vm.buttonOneTextObservable.subscribeText(errorButton)
        vm.errorMessageObservable.subscribeText(errorText)
        vm.titleObservable.subscribe { standardToolbar.title = it }
        vm.subTitleObservable.subscribe { standardToolbar.subtitle = it }
        errorButton.subscribeOnClick(vm.errorButtonClickedObservable)
    }

    private var navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)

    init {
        View.inflate(getContext(), R.layout.error_widget, this)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        standardToolbar.navigationIcon = navIcon
        standardToolbar.setBackgroundColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
        standardToolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTitleTextAppearance)
        standardToolbar.setSubtitleTextAppearance(getContext(), R.style.ToolbarSubtitleTextAppearance)
        errorButton.setBackgroundColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val statusBar = Ui.setUpStatusBar(getContext(), standardToolbar, root, ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
            addView(statusBar)
        }

        standardToolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
    }

    override fun back(): Boolean {
        viewmodel.clickBack.onNext(Unit)
        return true
    }

    abstract fun getViewModel(): AbstractErrorViewModel

    fun animationUpdate(f: Float, forward: Boolean) {
        val factor = if (forward) f else Math.abs(1 - f)
        navIcon.parameter = factor
    }

    fun animationFinalize() {
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.BACK.type.toFloat()
    }
}
