package com.expedia.bookings.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
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
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.widget.TimeSlider
import com.expedia.util.subscribeVisibility
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar
import com.mobiata.android.time.widget.CalendarPicker
import com.squareup.phrase.Phrase
import rx.Subscription
import kotlin.properties.Delegates
import org.joda.time.DateTime

class TimeAndCalendarDialogFragment(val viewModel: SearchViewModelWithTimeSliderCalendar) : CalendarDialogFragment(viewModel) {

    var popupLabel by Delegates.notNull<android.widget.TextView>()
    var pickupTimePopup by Delegates.notNull<android.widget.TextView>()
    var pickupTimePopupContainer by Delegates.notNull<LinearLayout>()
    var pickupTimePopupContainerText by Delegates.notNull<LinearLayout>()
    var pickupTimePopupTail by Delegates.notNull<android.widget.ImageView>()
    var sliderContainer by Delegates.notNull<ViewGroup>()
    private val sliderListener = TimeSliderListener()

    var departTimeSubscription by Delegates.notNull<Subscription>()
    var returnTimeSubscription by Delegates.notNull<Subscription>()
    var departSliderColorSubscription by Delegates.notNull<Subscription>()
    var returnSliderColorSubscription by Delegates.notNull<Subscription>()

    companion object {
        fun createFragment(searchViewModel: SearchViewModelWithTimeSliderCalendar): TimeAndCalendarDialogFragment {
            val fragment = TimeAndCalendarDialogFragment(searchViewModel)
            return fragment
        }
    }

    var departTimeSlider by Delegates.notNull<TimeSlider>()
    var returnTimeSlider by Delegates.notNull<TimeSlider>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val timeStub = calendarDialogView.findViewById(R.id.timesStub) as ViewStub
        timeStub.inflate()

        var params = calendar.layoutParams
        params.height = resources.getDimension(R.dimen.calendar_height_with_time_slider).toInt()
        calendar.layoutParams = params

        sliderContainer = calendarDialogView.findViewById(R.id.slider_container) as ViewGroup
        departTimeSlider = calendarDialogView.findViewById(R.id.depart_time_slider) as TimeSlider
        returnTimeSlider = calendarDialogView.findViewById(R.id.return_time_slider) as TimeSlider
        val returnSliderContainer = calendarDialogView.findViewById(R.id.return_slider_container) as ViewGroup

        pickupTimePopupContainer = calendarDialogView.findViewById(R.id.pickup_time_popup_container) as LinearLayout
        pickupTimePopupContainerText = calendarDialogView.findViewById(R.id.pickup_time_popup_text_container) as LinearLayout
        pickupTimePopupTail = calendarDialogView.findViewById(R.id.pickup_time_popup_tail) as ImageView
        pickupTimePopup = calendarDialogView.findViewById(R.id.pickup_time_popup) as TextView
        popupLabel = calendarDialogView.findViewById(R.id.pop_up_label) as TextView

        departTimeSlider.addOnSeekBarChangeListener(sliderListener)
        returnTimeSlider.addOnSeekBarChangeListener(sliderListener)

        departTimeSubscription = viewModel.departTimeSubject.subscribe {
            departTimeSlider.progress = TimeSlider.convertMillisToProgress(it)
        }

        returnTimeSubscription = viewModel.returnTimeSubject.subscribe {
            returnTimeSlider.progress = TimeSlider.convertMillisToProgress(it)
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

        viewModel.buildDateTimeObserver.onNext(Pair(viewModel.departTimeSubject.value, viewModel.returnTimeSubject.value))
        departTimeSubscription.unsubscribe()
        returnTimeSubscription.unsubscribe()
        departSliderColorSubscription.unsubscribe()
        returnSliderColorSubscription.unsubscribe()
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
        else
            viewModel.getCalendarSliderTooltipEndTimeLabel()

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
                    departTimeSlider.contentDescription =  setContentDescriptionForTimeSlider(seekBar as TimeSlider, true, progress)
                } else if (seekBar.id == R.id.return_time_slider) {
                    viewModel.returnTimeSubject.onNext(TimeSlider.convertProgressToMillis(progress))
                    returnTimeSlider.contentDescription = setContentDescriptionForTimeSlider(seekBar as TimeSlider, false, progress)
                }
            if (fromUser)
                drawSliderTooltip(seekBar as TimeSlider)
        }
    }

     fun setContentDescriptionForTimeSlider(seekBar: TimeSlider, isPickup: Boolean, progress: Int): String {
        val time = seekBar.calculateProgress(progress)
        if (isPickup)
            return Phrase.from(context, R.string.pick_up_slider_cont_desc_TEMPLATE).put("time", time).format().toString()
        else
            return Phrase.from(context, R.string.drop_off_slider_cont_desc_TEMPLATE).put("time", time).format().toString()
    }
}