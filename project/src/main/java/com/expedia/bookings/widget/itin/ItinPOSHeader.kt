package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.AboutUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.ItinPOSHeaderViewModel
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class ItinPOSHeader(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    lateinit var itinPOSHeaderViewModel: ItinPOSHeaderViewModel
        @Inject set

    val pointOfSaleChangeButton: LinearLayout by bindView(R.id.country_selection_view)
    val imageView: ImageView by lazy {
        pointOfSaleChangeButton.findViewById<ImageView>(R.id.country_selected_imageview)
    }
    val textView: TextView by lazy {
        pointOfSaleChangeButton.findViewById<TextView>(R.id.country_selected_textview)
    }
    val pointOfSaleUrlTextView: TextView by bindView(R.id.pos_trips_signin)

    private var subscriptions: CompositeSubscription? = null

    init {
        View.inflate(context, R.layout.itin_pos_header, this)
        if (!isInEditMode) {
            Ui.getApplication(context).tripComponent().inject(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscriptions?.unsubscribe()
    }

    @VisibleForTesting
    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            bindViewSubscriptions()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupCountryButtonListener()
    }

    private fun bindViewSubscriptions() {
        subscriptions?.unsubscribe()
        val subscriptions = CompositeSubscription()

        subscriptions.add(itinPOSHeaderViewModel.posImageViewSubject.subscribe { resId ->
            imageView.setImageDrawable(ContextCompat.getDrawable(context, resId))
        })

        subscriptions.add(itinPOSHeaderViewModel.posTextViewSubject.subscribeText(textView))

        subscriptions.add(itinPOSHeaderViewModel.posUrlSubject.subscribeText(pointOfSaleUrlTextView))

        this.subscriptions = subscriptions
    }

    private fun setupCountryButtonListener() {
        pointOfSaleChangeButton.setOnClickListener {
            if (PointOfSale.getAllPointsOfSale(context).size > 1) {
                val selectCountryDialog = AboutUtils.CountrySelectDialog()
                val activity = context as AppCompatActivity
                selectCountryDialog.show(activity.supportFragmentManager, "selectCountryDialog")
            }
        }
    }
}
