package com.expedia.vm.traveler

import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.section.InvalidCharacterHelper
import com.mobiata.android.validation.ValidationError
import rx.subjects.BehaviorSubject
import java.util.ArrayList

abstract class BaseTravelerValidatorViewModel : InvalidCharacterHelper.InvalidCharacterListener {
    abstract fun isValid(): Boolean

    val textSubject = BehaviorSubject.create<String>()
    val errorSubject = BehaviorSubject.create<Boolean>()
    open val invalidCharacterMode = InvalidCharacterHelper.Mode.NAME

    internal val invalidCharacterListeners = ArrayList<InvalidCharacterHelper.InvalidCharacterListener>()

    override fun onInvalidCharacterEntered(text: CharSequence?, mode: InvalidCharacterHelper.Mode?) {
        for (listener in invalidCharacterListeners) {
            listener.onInvalidCharacterEntered(text, mode)
        }
    }

    fun addInvalidCharacterListener(listener: InvalidCharacterHelper.InvalidCharacterListener) {
        invalidCharacterListeners.add(listener)
    }

    fun validate(): Boolean {
        val valid = isValid()
        errorSubject.onNext(!valid)
        return valid
    }

    fun getText() : String {
       return textSubject.value ?: ""
    }

    protected fun isRequiredNameValid(name: String?) : Boolean {
        return CommonSectionValidators.NON_EMPTY_VALIDATOR.validate(name) == ValidationError.NO_ERROR
                && hasAllValidChars(name)
    }

    protected fun hasAllValidChars(name: String?): Boolean {
        if (name == null) {
            return true
        }
        return CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES_STRING.validate(name) == ValidationError.NO_ERROR
    }
}