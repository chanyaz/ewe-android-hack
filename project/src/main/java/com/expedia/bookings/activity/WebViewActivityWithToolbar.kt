package com.expedia.bookings.activity

import android.content.Context
import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.WebViewToolbar
import com.expedia.bookings.itin.common.WebViewToolbarViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable

class WebViewActivityWithToolbar : WebViewActivity() {

    val toolbar: WebViewToolbar by bindView(R.id.widget_itin_toolbar)

    var toolbarViewModel: WebViewToolbarViewModel by notNullAndObservable { vm ->
        vm.navigationBackPressedSubject.subscribe {
            finish()
        }
    }

    override fun inflateView() {
        setContentView(R.layout.web_view_sharable_toolbar)
    }

    override fun setToolBar() {
        toolbarViewModel = WebViewToolbarViewModel()
        toolbar.viewModel = toolbarViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarViewModel = WebViewToolbarViewModel()
        toolbar.viewModel = toolbarViewModel
        toolbarViewModel.toolbarTitleSubject.onNext(title.toString())
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
    }

    class IntentBuilder(context: Context) : WebViewActivity.IntentBuilder(context) {
        init {
            intent.setClass(context, WebViewActivityWithToolbar::class.java)
        }
    }
}
