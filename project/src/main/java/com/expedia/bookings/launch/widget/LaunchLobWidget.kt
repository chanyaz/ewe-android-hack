package com.expedia.bookings.launch.widget

import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.launch.vm.LaunchLobViewModel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.NavigationHelper
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.PlayStoreUtil
import com.expedia.bookings.utils.isBrandColorEnabled
import com.expedia.bookings.utils.shouldPackageForceUpdateBeVisible
import com.expedia.bookings.utils.isShowFlightsCheckoutWebview
import com.expedia.bookings.widget.GridLinesItemDecoration
import com.expedia.util.notNullAndObservable
import io.reactivex.subjects.PublishSubject
import kotlin.properties.Delegates

class LaunchLobWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val backGroundView: View by bindView(R.id.background)
    private val cardView: CardView by bindView(R.id.card_view)
    private val gridRecycler: RecyclerView by bindView(R.id.lob_grid_recycler)
    private val flightNotSupportedDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.resources.getString(R.string.invalid_flights_pos))
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, _ -> dialog.dismiss() })
        builder.create()
    }
    val lobViewHeightChangeSubject = PublishSubject.create<Unit>()

    var adapter: LaunchLobAdapter by Delegates.notNull()
    val nav = NavigationHelper(context)
    var viewModel: LaunchLobViewModel by notNullAndObservable {
        adapter = LaunchLobAdapter(viewModel)
        gridRecycler.adapter = adapter
        viewModel.lobsSubject.subscribe {
            adapter.setLobs(it)
        }
        viewModel.navigationSubject.subscribe {
            when (it.first) {
                LineOfBusiness.HOTELS -> {
                    val animOptions = AnimUtils.createActivityScaleBundle(it.second)
                    nav.goToHotels(animOptions)
                }
                LineOfBusiness.FLIGHTS -> {
                    if (PointOfSale.getPointOfSale().supports(LineOfBusiness.FLIGHTS) || isShowFlightsCheckoutWebview(context)) {
                        nav.goToFlights(null)
                    } else {
                        flightNotSupportedDialog.show()
                        flightNotSupportedDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                ?.setTextColor(ContextCompat.getColor(context, R.color.launch_alert_dialog_button_color))
                    }
                }
                LineOfBusiness.TRANSPORT -> nav.goToTransport(null)
                LineOfBusiness.LX -> nav.goToActivities(null)
                LineOfBusiness.CARS -> nav.goToCars()
                LineOfBusiness.PACKAGES -> {
                    if (shouldPackageForceUpdateBeVisible(context)) {
                        PlayStoreUtil.showForceUpgradeDailogWithMessage(context)
                    } else {
                        nav.goToPackages(null, null)
                    }
                }
                LineOfBusiness.RAILS -> nav.goToRail(null)
                else -> {
                    //Add other lobs navigation in future
                }
            }
        }
        viewModel.hasInternetConnectionChangeSubject?.subscribe {
            adapter.enableLobs(it)
        }
        viewModel.refreshLobsObserver.onNext(Unit)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val layoutManager = GridLayoutManager(context, 2)
        val itemDecoration = GridLinesItemDecoration(
                ContextCompat.getColor(context, R.color.app_divider_on_white),
                context.resources.getDimension(R.dimen.new_launch_lob_divider_stroke_width))
        gridRecycler.addItemDecoration(itemDecoration)

        gridRecycler.layoutManager = layoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }

        adjustBackgroundView()
    }

    private fun adjustBackgroundView() {
        cardView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                val layoutParams = backGroundView.layoutParams
                val topMargin = (cardView.layoutParams as MarginLayoutParams).topMargin
                layoutParams.height = (cardView.height + topMargin) / 2
                backGroundView.requestLayout()
                if (isBrandColorEnabled(context)) {
                    backGroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_primary))
                }
                return false
            }
        })
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        adjustBackgroundView()
        lobViewHeightChangeSubject.onNext(Unit)
    }
}
