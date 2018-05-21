package com.expedia.bookings.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.vm.DebugSelectLanguageVM
import com.expedia.util.LanguageHelper

class SelectLanguageDialogFragment : DialogFragment() {

    lateinit var viewModel: DebugSelectLanguageVM

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Change Language")
        alertDialogBuilder.setMessage(getActivity().getResources().getString(R.string.language_change_message))
        alertDialogBuilder.setCancelable(false)
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.debug_language_select_layout, null)
        alertDialogBuilder.setView(view)

        view.findViewById<Button>(R.id.button_set_sys_def).setOnClickListener {
            LanguageHelper.revertToDefault(context)
            viewModel.restartAppSubject.onNext(Unit)
            dismiss()
        }

        val et_lang_code = view.findViewById<TextInputEditText>(R.id.et_lang_code)

        view.findViewById<Button>(R.id.button_change).setOnClickListener {
            val lang = et_lang_code.text.toString()
            val region = view.findViewById<TextInputEditText>(R.id.et_reg_code).text.toString()

            if (lang.isEmpty()) {
                et_lang_code.error = "Language Code cannot be empty"
            } else {
                LanguageHelper.setAppLocale(context, lang.trim { it <= ' ' }, region.trim { it <= ' ' })
                viewModel.restartAppSubject.onNext(Unit)
                dismiss()
            }
        }

        view.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            dismiss()
        }

        return alertDialogBuilder.create()
    }
}
