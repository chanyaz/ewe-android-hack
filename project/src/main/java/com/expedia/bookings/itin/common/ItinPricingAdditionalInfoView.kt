package com.expedia.bookings.itin.common

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.PublishSubject

class ItinPricingAdditionalInfoView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val toolbar: WebViewToolbar by bindView(R.id.widget_itin_toolbar)
    val container: LinearLayout by bindView(R.id.itin_additional_pricing_info_container)
    val toolbarViewModel = PricingAdditionalInfoToolbarViewModel()

    init {
        View.inflate(context, R.layout.itin_additional_info_view, this)
        toolbar.viewModel = toolbarViewModel
    }

    var viewModel: ItinPricingAdditionInfoViewModelInterface by notNullAndObservable {
        it.toolbarTitleSubject.subscribe {
            toolbarViewModel.toolbarTitleSubject.onNext(it)
        }

        it.additionalInfoItemSubject.subscribe { items ->
            container.removeAllViews()
            items.forEach {
                val itemView: AdditionalInfoItemView = Ui.inflate(R.layout.itin_additional_info_item, container, false)
                itemView.heading.text = HtmlCompat.fromHtml(it.heading)
                itemView.content.text = HtmlCompat.fromHtml(it.content)
                itemView.content.movementMethod = LinkMovementMethod.getInstance()
                container.addView(itemView)
            }
        }
    }
}

class AdditionalInfoItemView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val heading by bindView<TextView>(R.id.additonal_info_item_heading)
    val content by bindView<TextView>(R.id.additonal_info_item_content)
}

class PricingAdditionalInfoToolbarViewModel : NewItinToolbarViewModel {
    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()
}
