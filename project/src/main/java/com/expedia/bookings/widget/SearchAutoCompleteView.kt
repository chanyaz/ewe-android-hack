package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.SuggestionAdapterViewModel
import com.mobiata.android.util.AndroidUtils
import kotlin.properties.Delegates
import kotlin.text.isNotEmpty

public class SearchAutoCompleteView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), PopupWindow.OnDismissListener {
    val locationEditText: EditText by bindView(R.id.location_edit_text)
    val locationTextView: TextView by bindView(R.id.location_text_view)
    val clearLocationButton: ImageView by bindView(R.id.clear_location_button)
    val locationDrawable: ImageView by bindView(R.id.location_drawable)
    var suggestionRecyclerView: RecyclerView by Delegates.notNull()
    var suggestionDropDown: PopupWindow by Delegates.notNull()
    var suggestionViewModel: SuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            dismissSuggestions()
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, vm.getSuggestionHistoryFile())
            clearLocationButton.visibility = View.INVISIBLE
            locationEditText.clearFocus()
            com.mobiata.android.util.Ui.hideKeyboard(this)
        }
    }
    var suggestionAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> by notNullAndObservable { adapter ->
        suggestionRecyclerView.adapter = adapter
    }

    var isFromUser = true

    init {
        View.inflate(context, R.layout.widget_search_autocomplete, this)
        orientation = HORIZONTAL
        setStyle(attrs)

        val popupView = View.inflate(context, R.layout.suggestion_popup, null);
        val screenSize = AndroidUtils.getScreenSize(context)
        suggestionDropDown = PopupWindow(popupView, screenSize.x, screenSize.y, false)
        suggestionDropDown.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        suggestionDropDown.setOnDismissListener(this)
        suggestionRecyclerView = popupView.findViewById(R.id.drop_down_list) as RecyclerView
        suggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestionRecyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt(), false))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        locationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                clearLocationButton.visibility = if (Strings.isEmpty(s) || !locationEditText.hasFocus()) View.INVISIBLE else View.VISIBLE
                isFromUser = true
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isFromUser) {
                    suggestionViewModel.queryObserver.onNext(s.toString())
                }
            }
        })

        locationEditText.setOnFocusChangeListener { view, hasFocus ->
            focusChanged(hasFocus)
        }

        locationTextView.setOnClickListener {
            locationEditText.visibility = View.VISIBLE
            locationTextView.visibility = View.GONE
            locationEditText.requestFocus()
            locationEditText.setSelection(locationEditText.text.length)
            com.mobiata.android.util.Ui.showKeyboard(locationEditText, null)
        }

        clearLocationButton.setOnClickListener {
            locationEditText.setText("")
        }
    }

    fun focusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            showSuggestions()
        } else {
            locationTextView.text = locationEditText.text.toString()
            locationEditText.visibility = View.GONE
            locationTextView.visibility = View.VISIBLE
            clearLocationButton.visibility = View.INVISIBLE
            dismissSuggestions()
        }

        if (locationEditText.text.isNotEmpty() && hasFocus) {
            clearLocationButton.visibility = View.VISIBLE
        } else {
            clearLocationButton.visibility = View.INVISIBLE
        }
    }

    private fun setStyle(attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SearchAutoComplete)
        val searchDrawable = ta.getDrawable(R.styleable.SearchAutoComplete_search_drawable)
        val hint = ta.getString(R.styleable.SearchAutoComplete_search_hint)
        ta.recycle()

        locationDrawable.setImageDrawable(searchDrawable)
        locationEditText.hint = hint
        locationTextView.hint = hint
    }

    fun resetFocus() {
        locationEditText.clearFocus()
        clearLocationButton.visibility = View.INVISIBLE
    }

    fun dismissSuggestions() : Boolean {
        val dismissed = suggestionDropDown.isShowing
        suggestionDropDown.dismiss()
        return dismissed
    }

    fun showSuggestions() {
        post {
            suggestionDropDown.showAsDropDown(this)
        }
    }

    override fun onDismiss() {
        focusChanged(false)
    }
}
