package com.expedia.bookings.test.pagemodels.common

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.closeSoftKeyboard
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.PickerActions
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withClassName
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiSelector
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith

object TravelerModel {
    object TravelerList

    object TravelerDetails{
        @JvmStatic fun firstName(): ViewInteraction {return onView(withId(R.id.first_name_input))}
        @JvmStatic fun lastName(): ViewInteraction {return onView(withId(R.id.last_name_input))}
        @JvmStatic fun email(): ViewInteraction {return onView(withId(R.id.edit_email_address))}
        @JvmStatic fun phoneNumber(): ViewInteraction {return onView(withId(R.id.edit_phone_number))}
        @JvmStatic fun birthDate(): ViewInteraction {return onView(withId(R.id.edit_birth_date_text_btn))}
        @JvmStatic fun genderButton(): ViewInteraction {return onView(withId(R.id.edit_gender_btn))}
        @JvmStatic fun redressNumber(): ViewInteraction {return onView(withId(R.id.redress_number))}
        @JvmStatic fun doneButton(): ViewInteraction {return onView(withId(R.id.menu_done))}
        @JvmStatic fun datePicker(): ViewInteraction {return onView(withId(R.id.datePicker))}
        @JvmStatic fun datePickerDoneButton(): ViewInteraction {return onView(withId(R.id.datePickerDoneButton))}
        @JvmStatic fun advancedOptions(): ViewInteraction {return onView(withId(R.id.traveler_advanced_options_button))}
        @JvmStatic fun knownTravelerNumber(): ViewInteraction {return onView(withId(R.id.traveler_number))}
        @JvmStatic fun passportCountry(): ViewInteraction {return onView(withId(R.id.passport_country_layout_btn))}

        @JvmStatic fun enterFirstName(firstName: String){
            firstName().perform(typeText(firstName))
        }

        @JvmStatic fun enterLastName(lastName: String){
            lastName().perform(typeText(lastName))
        }

        @JvmStatic fun enterEmail(email: String){
            email().perform(typeText(email))
        }

        @JvmStatic fun enterPhoneNumber(phoneNumber: String){
            phoneNumber().perform(typeText(phoneNumber))
        }

        @JvmStatic fun selectBirthDate(year: Int, month: Int, day: Int){
            birthDate().perform(click())
            closeSoftKeyboard()
            datePicker().perform(waitForViewToDisplay())
            datePicker().perform(PickerActions.setDate(year, month, day))
            datePickerDoneButton().perform(click())
        }

        @JvmStatic fun enterRedressNumber(redressNumber: String){
            TravelerDetails.redressNumber().perform(scrollTo(), typeText(redressNumber))
        }

        @JvmStatic fun clickDone(){
            doneButton().perform(waitForViewToDisplay())
            doneButton().perform(click())
        }

        @JvmStatic fun enterKnownTravelerNumber(knownTravelerNumber: String) {
            TravelerDetails.knownTravelerNumber().perform(typeText(knownTravelerNumber))
        }

        @JvmStatic fun materialSelectGender(genderType: String) {
            genderButton().perform(click())
            onView(withText(genderType)).perform(click())
        }

        @JvmStatic fun clickAdvanced() {
            advancedOptions().perform(scrollTo(),click())
        }
    }

    object SaveTravelerPrompt {
        @JvmStatic fun title(): ViewInteraction {
            return onView(withId(R.id.alertTitle))
        }

        @JvmStatic fun text(): ViewInteraction {
            throw Error("Code not yet implemented")
            //return onView(withId(R.id.message))
        }

        @JvmStatic fun btnNoThanks(): ViewInteraction {
            return onView(withText("No Thanks"))
        }

        @JvmStatic fun btnSave(): ViewInteraction {
            return onView(withText("Save"))
        }

        @JvmStatic fun ifPresentClickSave(){
            Common.delay(1)
            var device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            var saveButton = device.findObject(UiSelector().text("Save"))
            if (saveButton.exists()){
                btnSave().perform(click())
            }
        }

        @JvmStatic fun ifPresentClickNoThanks(){
            Common.delay(1)
            var device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            var saveButton = device.findObject(UiSelector().text("Save"))
            if (saveButton.exists()){
                btnNoThanks().perform(click())
            }
        }
    }
}