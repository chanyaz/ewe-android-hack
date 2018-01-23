package com.expedia.bookings.utils

import android.app.Activity
import android.widget.EditText
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.validation.ValidationError
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CommonSectionValidatorTest {
    var activity: Activity by Delegates.notNull()
    var editText: EditText by Delegates.notNull()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        editText = EditText(activity, null)
    }

    @Test
    fun testNamePatternValidator() {
        val validator = CommonSectionValidators.NAME_PATTERN_VALIDATOR
        editText.setText("")
        assertEquals(ValidationError.ERROR_DATA_MISSING, validator.validate(editText))
        editText.setText("m")
        assertEquals(ValidationError.ERROR_DATA_INVALID, validator.validate(editText))
        editText.setText("mN")
        assertEquals(ValidationError.ERROR_DATA_INVALID, validator.validate(editText))
        editText.setText("MalcolmNguyen")
        assertEquals(ValidationError.ERROR_DATA_INVALID, validator.validate(editText))
        editText.setText("m n")
        assertEquals(ValidationError.NO_ERROR, validator.validate(editText))
        editText.setText("mA n")
        assertEquals(ValidationError.NO_ERROR, validator.validate(editText))
        editText.setText("Malcolm Nguyen")
        assertEquals(ValidationError.NO_ERROR, validator.validate(editText))
    }
}
