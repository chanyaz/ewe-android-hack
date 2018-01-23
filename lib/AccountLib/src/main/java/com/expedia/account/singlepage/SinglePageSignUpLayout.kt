package com.expedia.account.singlepage

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import com.expedia.account.R
import com.expedia.account.util.Events

class SinglePageSignUpLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val vEmailNamePasswordLayout: SinglePageEmailNamePasswordLayout by lazy {
        findViewById<SinglePageEmailNamePasswordLayout>(R.id.single_page_email_name_password)
    }
    private val vTOSLayout: SinglePageTOSLayout by lazy {
        findViewById<SinglePageTOSLayout>(R.id.single_page_tos_layout)
    }
    private val scrollView: ScrollView by lazy {
        findViewById<ScrollView>(R.id.single_page_scrollview)
    }
    private val fullWideCreateAccountButton: Button by lazy {
        findViewById<Button>(R.id.single_page_button_create_account)
    }
    var currentKeyboardHeight = 0
    val viewRect = Rect()
    val screenHeight: Int by lazy {
        val display = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(display)
        display.heightPixels
    }

    init {
        View.inflate(context, R.layout.acct__widget_single_page_sign_up, this)
        fullWideCreateAccountButton.setOnClickListener {
            if (vEmailNamePasswordLayout.allTextValidSubject.value && vTOSLayout.termOfUseCheckedSubject.value) {
                Events.post(Events.TOSContinueButtonClicked())
            } else {
                vEmailNamePasswordLayout.forceCheckAllFields()
                vTOSLayout.forceCheckTermOfUseCheckbox()
            }
        }

        vTOSLayout.termOfUseCheckedSubject.subscribe { checked ->
            if (checked) {
                scrollView.smoothScrollTo(0, screenHeight)
            }
        }

        fullWideCreateAccountButton.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                vTOSLayout.setPadding(0, 0, 0, fullWideCreateAccountButton.height)
            }
        })
    }

    fun storeDataInNewUser() {
        vEmailNamePasswordLayout.storeDataInNewUser()
    }

    fun addKeyboardChangeListener() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(keyboardChangeListener)
    }
    fun removeKeyboardChangeListener() {
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(keyboardChangeListener)
    }

    private val keyboardChangeListener = ViewTreeObserver.OnGlobalLayoutListener {
        rootView.getWindowVisibleDisplayFrame(viewRect)
        viewRect.bottom = viewRect.bottom
        var keyboardHeight = screenHeight - viewRect.bottom
        var isKeyboardVisible = keyboardHeight > screenHeight * 0.15
        if (keyboardHeight != currentKeyboardHeight) {
            if (isKeyboardVisible) {
                fullWideCreateAccountButton.visibility = View.GONE
                vTOSLayout.visibility = View.GONE
            } else {
                fullWideCreateAccountButton.visibility = View.VISIBLE
                vTOSLayout.visibility = View.VISIBLE
                scrollView.smoothScrollTo(0, 0)
            }
        }
        currentKeyboardHeight = keyboardHeight
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Events.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Events.unregister(this)
    }

    fun styleizeFromAccountView(a: TypedArray) {
        vTOSLayout.styleizeFromAccountView(a)
    }

    fun configurePOS(showSpamOptIn: Boolean, enableSpamByDefault: Boolean, hasUserRewardsEnrollmentCheck: Boolean, shouldAutoEnrollUserInRewards: Boolean,
                  tosText: CharSequence, marketingText: CharSequence, rewardsText: CharSequence) {
        vTOSLayout.configurePOS(
            showSpamOptIn,
            enableSpamByDefault,
            hasUserRewardsEnrollmentCheck,
            shouldAutoEnrollUserInRewards,
            tosText,
            marketingText,
            rewardsText
        )
    }

    fun brandIt(brand: String) {
        vEmailNamePasswordLayout.brandIt(brand)
    }
}
