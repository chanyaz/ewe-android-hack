package com.expedia.account.singlepage

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.expedia.account.R
import com.expedia.account.view.TOSLayout
import io.reactivex.subjects.BehaviorSubject

class SinglePageTOSLayout(context: Context, attrs: AttributeSet): TOSLayout(context, attrs) {

    val termOfUseCheckedSubject = BehaviorSubject.createDefault<Boolean>(vTermsOfUseCheckBox.isChecked)

    init {
        vTermsOfUseCheckBox.setOnCheckedChangeListener { compoundButton, checked ->
            fixupColors(vTermsOfUseCheckBox)
            vEnrollInLoyaltyLayout.isEnabled = checked
            vEnrollInLoyaltyCheckBox.isEnabled = checked
            vEnrollInLoyaltyText.isEnabled = checked
            vSpamOptInLayout.isEnabled = checked
            vSpamOptInCheckBox.isEnabled = checked
            vSpamOptInText.isEnabled = checked
            refreshCheckboxContentDesc(vTermsOfUseLayout)
            fixupColors(vEnrollInLoyaltyCheckBox)
            fixupColors(vSpamOptInCheckBox)
            termOfUseCheckedSubject.onNext(checked)
        }
        fixupSpacing()
        vCreateAccountButton.visibility = View.GONE
    }
    
    override fun styleizeFromAccountView(a: TypedArray) {
        super.styleizeFromAccountView(a)

        val singlePageTOSTextColorStateList = context.resources.getColorStateList(R.color.acct__single_page_tos_text_color)
        vTermsOfUseText.setTextColor(singlePageTOSTextColorStateList)
        vTermsOfUseText.setLinkTextColor(singlePageTOSTextColorStateList)
        vEnrollInLoyaltyText.setTextColor(singlePageTOSTextColorStateList)
        vEnrollInLoyaltyText.setLinkTextColor(singlePageTOSTextColorStateList)
        vSpamOptInText.setTextColor(singlePageTOSTextColorStateList)
        vSpamOptInText.setLinkTextColor(singlePageTOSTextColorStateList)
        mCheckedColorResId = resources.getColor(R.color.acct__single_page_checkbox_checked_color)
    }

    override fun fixupColors(v: CheckBox) {
        val res = resources
        val color = if (v.isChecked && v.isEnabled)
            resources.getColor(R.color.acct__single_page_checkbox_checked_color)
        else if (v.isChecked && !v.isEnabled)
            resources.getColor(R.color.acct__single_page_checkbox_checked_disable_color)
        else if (!v.isChecked && v.isEnabled) {
            context.resources.getColorStateList(R.color.acct__single_page_tos_text_color).getColorForState(intArrayOf(android.R.attr.state_enabled), -1)
        } else {
            context.resources.getColorStateList(R.color.acct__single_page_tos_text_color).defaultColor
        }

        val drawable = res.getDrawable(R.drawable.abc_btn_check_material)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        v.buttonDrawable = drawable
    }

    fun forceCheckTermOfUseCheckbox() {
        if (!vTermsOfUseCheckBox.isChecked) {
            val drawable = resources.getDrawable(R.drawable.abc_btn_check_material)
            drawable.setColorFilter(resources.getColor(R.color.acct__single_page_error_color), PorterDuff.Mode.SRC_IN)
            vTermsOfUseCheckBox.buttonDrawable = drawable
        }
    }

    private fun fixupSpacing() {
        (vSpamOptInLayout.layoutParams as MarginLayoutParams).bottomMargin = context.resources.getDimensionPixelSize(R.dimen.acct__single_page_tos_bottom_margin)
        vTermsOfUseCheckBox.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        vEnrollInLoyaltyCheckBox.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        vSpamOptInCheckBox.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}



