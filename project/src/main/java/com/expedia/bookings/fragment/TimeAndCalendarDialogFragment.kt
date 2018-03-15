package com.expedia.bookings.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.shared.CalendarRules
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.Optional
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar
import io.reactivex.disposables.Disposable
import kotlin.properties.Delegates

class TimeAndCalendarDialogFragment(
    val viewModel: SearchViewModelWithTimeSliderCalendar,
    rules: CalendarRules
) : CalendarDialogFragment(viewModel, rules) {

    var popupLabel by Delegates.notNull<android.widget.TextView>()
    var pickupTimePopup by Delegates.notNull<android.widget.TextView>()
    var pickupTimePopupContainer by Delegates.notNull<LinearLayout>()
    var pickupTimePopupContainerText by Delegates.notNull<LinearLayout>()
    var pickupTimePopupTail by Delegates.notNull<android.widget.ImageView>()
    var sliderContainer by Delegates.notNull<ViewGroup>()
    private val sliderListener = TimeSliderListener()

    var departTimeSubscription by Delegates.notNull<Disposable>()
    var returnTimeSubscription by Delegates.notNull<Disposable>()
    var departSliderColorSubscription by Delegates.notNull<Disposable>()
    var returnSliderColorSubscription by Delegates.notNull<Disposable>()

    companion object {
        fun createFragment(searchViewModel: SearchViewModelWithTimeSliderCalendar, rules: CalendarRules): TimeAndCalendarDialogFragment {
            val fragment = TimeAndCalendarDialogFragment(searchViewModel, rules)
            return fragment
        }
    }

    var departTimeSlider by Delegates.notNull<TimeSlider>()
    var returnTimeSlider by Delegates.notNull<TimeSlider>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val timeStub = calendarDialogView.findViewById<ViewStub>(R.id.timesStub)
        timeStub.inflate()

        val params = calendar.layoutParams
        params.height = resources.getDimension(R.dimen.calendar_height_with_time_slider).toInt()
        calendar.layoutParams = params

        sliderContainer = calendarDialogView.findViewById<ViewGroup>(R.id.slider_container)
        departTimeSlider = calendarDialogView.findViewById<TimeSlider>(R.id.depart_time_slider)
        returnTimeSlider = calendarDialogView.findViewById<TimeSlider>(R.id.return_time_slider)
        val returnSliderContainer = calendarDialogView.findViewById<ViewGroup>(R.id.return_slider_container)

        pickupTimePopupContainer = calendarDialogView.findViewById<LinearLayout>(R.id.pickup_time_popup_container)
        pickupTimePopupContainerText = calendarDialogView.findViewById<LinearLayout>(R.id.pickup_time_popup_text_container)
        pickupTimePopupTail = calendarDialogView.findViewById<ImageView>(R.id.pickup_time_popup_tail)
        pickupTimePopup = calendarDialogView.findViewById<TextView>(R.id.pickup_time_popup)
        popupLabel = calendarDialogView.findViewById<TextView>(R.id.pop_up_label)

        departTimeSlider.addOnSeekBarChangeListener(sliderListener)
        returnTimeSlider.addOnSeekBarChangeListener(sliderListener)

        departTimeSubscription = viewModel.departTimeSubject.subscribe {
            departTimeSlider.progress = TimeSlider.convertMillisToProgress(it)
        }

        returnTimeSubscription = viewModel.returnTimeSubject.subscribe {
            it.value?.let {
                returnTimeSlider.progress = TimeSlider.convertMillisToProgress(it)
            }
        }

        departSliderColorSubscription = viewModel.departTimeSliderTooltipColor.subscribe {
            setUpTooltipColor(it)
        }

        returnSliderColorSubscription = viewModel.returnTimeSliderTooltipColor.subscribe {
            setUpTooltipColor(it)
        }

        viewModel.isRoundTripSearchObservable.subscribeVisibility(returnSliderContainer)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        viewModel.returnTimeSubject.value.value?.let {
            viewModel.buildDateTimeObserver.onNext(Pair(viewModel.departTimeSubject.value, it))
        }
        departTimeSubscription.dispose()
        returnTimeSubscription.dispose()
        departSliderColorSubscription.dispose()
        returnSliderColorSubscription.dispose()
    }

    private fun setUpTooltipColor(color: Int) {
        val drawablePopUp = ContextCompat.getDrawable(context, R.drawable.toolbar_bg).mutate()
        drawablePopUp.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        pickupTimePopupContainerText.background = drawablePopUp
        pickupTimePopupTail.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    private fun drawSliderTooltip(seekBar: TimeSlider) {
        calendar.hideToolTip()
        val title = seekBar.calculateProgress(seekBar.progress)
        val subtitle = if (seekBar.id == R.id.depart_time_slider)
            viewModel.getCalendarSliderTooltipStartTimeLabel()
        else viewModel.getCalendarSliderTooltipEndTimeLabel()

        pickupTimePopup.text = title
        popupLabel.text = subtitle

        val thumbRect = seekBar.thumb.bounds
        val x = thumbRect.centerX() + seekBar.left
        val y = sliderContainer.top + seekBar.top - thumbRect.height() / 2

        pickupTimePopupContainer.visibility = View.VISIBLE

        val vto = pickupTimePopupContainer.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                pickupTimePopupContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val p = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT)

                p.setMargins(x - pickupTimePopupContainer.measuredWidth / 2, y - pickupTimePopupContainer.measuredHeight, 0, 0)

                val lp = pickupTimePopupTail.layoutParams as LinearLayout.LayoutParams
                lp.gravity = Gravity.CENTER
                pickupTimePopupTail.layoutParams = lp
                pickupTimePopupContainer.layoutParams = p
            }
        })
    }

    private inner class TimeSliderListener : SeekBar.OnSeekBarChangeListener {

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            drawSliderTooltip(seekBar as TimeSlider)
            TimeSlider.animateToolTip(pickupTimePopupContainer)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            viewModel.validateTimes()
            pickupTimePopupContainer.visibility = View.GONE
        }

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // if not from the user, then the VM already has this info, don't need to notify of update
                if (seekBar.id == R.id.depart_time_slider) {
                    viewModel.departTimeSubject.onNext(TimeSlider.convertProgressToMillis(progress))
                    departTimeSlider.contentDescription = setContentDescriptionForTimeSlider(seekBar as TimeSlider, true, progress)
                } else if (seekBar.id == R.id.return_time_slider) {
                    viewModel.returnTimeSubject.onNext(Optional(TimeSlider.convertProgressToMillis(progress)))
                    returnTimeSlider.contentDescription = setContentDescriptionForTimeSlider(seekBar as TimeSlider, false, progress)
                }
            if (fromUser)
                drawSliderTooltip(seekBar as TimeSlider)
        }
    }

    fun setContentDescriptionForTimeSlider(seekBar: TimeSlider, isDepart: Boolean, progress: Int): String {
        val time = seekBar.calculateProgress(progress)
        if (isDepart)
            return viewModel.getStartTimeContDesc(time)
        else return viewModel.getEndTimeContDesc(time)
    }
}
